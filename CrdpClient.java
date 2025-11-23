import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * CRDP 클라이언트 라이브러리
 * 
 * 이 클래스는 CRDP API와의 통신을 캡슐화하여 개발자가 쉽게 암호화/복호화 기능을 사용할 수 있게 합니다.
 * 성능 최적화 및 견고한 오류 처리가 포함되어 있습니다.
 */
public class CrdpClient {

    private final String baseUrl;
    private final String policy;
    private final String token;
    private static final int TIMEOUT = 5; // 5초 고정

    /**
     * 설정 파일에서 클라이언트 생성 (Factory Method)
     * 
     * @param filePath 설정 파일 경로 (예: "crdp.properties")
     * @return 초기화된 CrdpClient 객체
     * @throws IOException 설정 파일을 읽을 수 없거나 필수 항목이 누락된 경우
     */
    public static CrdpClient fromConfigFile(String filePath) throws IOException {
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            config.load(fis);
        }

        String endpoint = config.getProperty("crdp_endpoint");
        String policy = config.getProperty("crdp_policy");
        String token = config.getProperty("crdp_jwt");

        if (endpoint == null || policy == null || token == null) {
            throw new IOException("설정 파일(" + filePath + ")에 필수 항목(crdp_endpoint, crdp_policy, crdp_jwt)이 누락되었습니다.");
        }

        return new CrdpClient(endpoint, policy, token);
    }

    /**
     * 생성자
     * 
     * @param endpoint CRDP 서버 주소 (예: "192.168.0.1:443")
     * @param policy   보호 정책 이름 (예: "P01")
     * @param token    JWT 인증 토큰
     */
    public CrdpClient(String endpoint, String policy, String token) {
        this.baseUrl = "https://" + endpoint;
        this.policy = policy;
        this.token = token;
    }

    /**
     * 데이터 암호화 (Protect)
     * 
     * @param plaintext 보호할 원본 데이터
     * @return 암호화된 데이터 (토큰)
     * @throws Exception 통신 오류 또는 서버 오류 발생 시
     */
    public String protect(String plaintext) throws Exception {
        if (plaintext == null)
            throw new IllegalArgumentException("입력 데이터는 null일 수 없습니다.");

        String url = baseUrl + "/v1/protect";
        // JSON 이스케이프 처리
        String safeData = escapeJson(plaintext);
        String json = String.format("{\"protection_policy_name\":\"%s\",\"data\":\"%s\"}", policy, safeData);

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
        if (ciphertext == null)
            throw new IllegalArgumentException("입력 데이터는 null일 수 없습니다.");

        String url = baseUrl + "/v1/reveal";
        // JSON 이스케이프 처리
        String safeData = escapeJson(ciphertext);
        String json = String.format("{\"protection_policy_name\":\"%s\",\"protected_data\":\"%s\"}", policy, safeData);

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
        conn.setConnectTimeout(TIMEOUT * 1000);
        conn.setReadTimeout(TIMEOUT * 1000);
        conn.setDoOutput(true);

        // 요청 데이터 전송
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        if (stream == null) {
            throw new RuntimeException("CRDP 서버 오류 (HTTP " + status + "): 응답 없음");
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null)
                response.append(line);
        }

        if (status >= 400) {
            throw new RuntimeException("CRDP 서버 오류 (HTTP " + status + "): " + response.toString());
        }

        return response.toString();
    }

    /**
     * JSON 파싱 (내부 사용)
     * 단순 문자열 파싱보다 견고하게 개선됨
     */
    private String extractValue(String json, String key) {
        if (json == null)
            return null;

        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1)
            return null;

        // 키 뒤의 콜론(:) 찾기
        int colonIndex = json.indexOf(":", keyIndex + searchKey.length());
        if (colonIndex == -1)
            return null;

        // 값의 시작 따옴표(") 찾기
        int valueStartIndex = json.indexOf("\"", colonIndex + 1);
        if (valueStartIndex == -1)
            return null;

        // 값의 끝 따옴표(") 찾기 - 이스케이프 문자 처리
        int valueEndIndex = valueStartIndex + 1;
        while (valueEndIndex < json.length()) {
            char c = json.charAt(valueEndIndex);
            if (c == '\\') {
                valueEndIndex += 2; // 이스케이프 문자 건너뛰기
                continue;
            }
            if (c == '"') {
                break;
            }
            valueEndIndex++;
        }

        if (valueEndIndex >= json.length())
            return null;

        return json.substring(valueStartIndex + 1, valueEndIndex);
    }

    /**
     * JSON 특수 문자 이스케이프 처리
     */
    private String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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
