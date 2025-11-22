import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * CRDP 클라이언트 라이브러리
 * 
 * 이 클래스는 CRDP API와의 통신을 캡슐화하여 개발자가 쉽게 암호화/복호화 기능을 사용할 수 있게 합니다.
 */
public class CrdpClient {

    private final String host;
    private final int port;
    private final String policy;
    private final String token;
    private final int timeout;

    /**
     * 생성자
     * 
     * @param host    CRDP 서버 호스트 (예: "192.168.0.1")
     * @param port    CRDP 서버 포트 (예: 443)
     * @param policy  보호 정책 이름 (예: "P01")
     * @param token   JWT 인증 토큰
     * @param timeout 타임아웃 (초)
     */
    public CrdpClient(String host, int port, String policy, String token, int timeout) {
        this.host = host;
        this.port = port;
        this.policy = policy;
        this.token = token;
        this.timeout = timeout;
    }

    /**
     * 데이터 암호화 (Protect)
     * 
     * @param plaintext 보호할 원본 데이터
     * @return 암호화된 데이터 (토큰)
     * @throws Exception 통신 오류 또는 서버 오류 발생 시
     */
    public String protect(String plaintext) throws Exception {
        String url = String.format("https://%s:%d/v1/protect", host, port);
        String json = String.format("{\"protection_policy_name\":\"%s\",\"data\":\"%s\"}", policy, plaintext);

        String response = post(url, json);
        return extractValue(response, "protected_data");
    }

    /**
     * 데이터 복호화 (Reveal)
     * 
     * @param ciphertext 복호화할 암호문 (토큰)
     * @return 복원된 원본 데이터
     * @throws Exception 통신 오류 또는 서버 오류 발생 시
     */
    public String reveal(String ciphertext) throws Exception {
        String url = String.format("https://%s:%d/v1/reveal", host, port);
        String json = String.format("{\"protection_policy_name\":\"%s\",\"protected_data\":\"%s\"}", policy,
                ciphertext);

        String response = post(url, json);
        return extractValue(response, "data");
    }

    /**
     * HTTP POST 요청 (내부 사용)
     */
    private String post(String urlStr, String json) throws Exception {
        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        // SSL 검증 무시 (테스트/사내망용)
        conn.setSSLSocketFactory(getInsecureSslContext().getSocketFactory());
        conn.setHostnameVerifier((hostname, session) -> true);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setConnectTimeout(timeout * 1000);
        conn.setReadTimeout(timeout * 1000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status >= 400) {
            throw new RuntimeException("CRDP 서버 오류 (HTTP " + status + ")");
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null)
                response.append(line);
        }
        return response.toString();
    }

    /**
     * JSON 파싱 (내부 사용)
     */
    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1)
            return null;

        start = json.indexOf("\"", start + pattern.length()) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    /**
     * SSL 컨텍스트 생성 (내부 사용)
     */
    private SSLContext getInsecureSslContext() throws Exception {
        TrustManager[] trustAll = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new java.security.SecureRandom());
        return sc;
    }
}
