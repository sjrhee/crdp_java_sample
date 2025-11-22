import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * CRDP 최소 데모 (HTTPS + JWT)
 * 
 * 목적: 가장 간단하고 명확한 방법으로 CRDP API를 사용하는 방법을 보여줍니다.
 * 특징: 외부 라이브러리 없음, 복사-붙여넣기로 바로 사용 가능.
 */
public class MinimalDemo {
    
    // 설정값 (MinimalDemo.properties에서 로드)
    private static String HOST;
    private static int PORT;
    private static String POLICY;
    private static String DATA;
    private static String TOKEN;
    private static int TIMEOUT;

    public static void main(String[] args) {
        // 1. 설정 로드
        loadConfig();

        System.out.println("=== CRDP 데모 시작 ===");
        System.out.println("서버: https://" + HOST + ":" + PORT);
        System.out.println("정책: " + POLICY);

        try {
            // 2. 암호화 (Protect)
            System.out.print("\n1. 암호화 중... ");
            String protectedData = protect(DATA);
            System.out.println("성공: " + protectedData);

            // 3. 복호화 (Reveal)
            System.out.print("2. 복호화 중... ");
            String revealedData = reveal(protectedData);
            System.out.println("성공: " + revealedData);

            // 4. 검증
            System.out.println("\n3. 결과 검증:");
            System.out.println("   원본: " + DATA);
            System.out.println("   복원: " + revealedData);
            System.out.println("   일치: " + (DATA.equals(revealedData) ? "예" : "아니오"));

        } catch (Exception e) {
            System.err.println("\n[오류] " + e.getMessage());
            // e.printStackTrace(); // 디버깅 시 주석 해제
        }
    }

    /**
     * 데이터 암호화 요청
     */
    private static String protect(String data) throws Exception {
        String url = String.format("https://%s:%d/v1/protect", HOST, PORT);
        // JSON 생성: {"protection_policy_name":"P01","data":"..."}
        String json = String.format("{\"protection_policy_name\":\"%s\",\"data\":\"%s\"}", POLICY, data);
        
        String response = post(url, json);
        return extractValue(response, "protected_data");
    }

    /**
     * 데이터 복호화 요청
     */
    private static String reveal(String protectedData) throws Exception {
        String url = String.format("https://%s:%d/v1/reveal", HOST, PORT);
        // JSON 생성: {"protection_policy_name":"P01","protected_data":"..."}
        String json = String.format("{\"protection_policy_name\":\"%s\",\"protected_data\":\"%s\"}", POLICY, protectedData);
        
        String response = post(url, json);
        return extractValue(response, "data");
    }

    /**
     * HTTP POST 요청 전송 (HTTPS, JWT 포함)
     */
    private static String post(String urlStr, String json) throws Exception {
        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        
        // SSL 검증 무시 (테스트용)
        conn.setSSLSocketFactory(getInsecureSslContext().getSocketFactory());
        conn.setHostnameVerifier((hostname, session) -> true);

        // 헤더 설정
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + TOKEN);
        conn.setConnectTimeout(TIMEOUT * 1000);
        conn.setReadTimeout(TIMEOUT * 1000);
        conn.setDoOutput(true);

        // 데이터 전송
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        // 응답 확인
        int status = conn.getResponseCode();
        if (status >= 400) {
            throw new RuntimeException("HTTP 오류 " + status + ": 요청이 실패했습니다.");
        }

        // 응답 읽기
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) response.append(line);
        }
        return response.toString();
    }

    /**
     * JSON에서 특정 키의 값을 추출 (간단한 파싱)
     */
    private static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return null;

        start = json.indexOf("\"", start + pattern.length()) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    /**
     * 설정 파일 로드
     */
    private static void loadConfig() {
        Properties config = new Properties();
        try (InputStream input = MinimalDemo.class.getClassLoader().getResourceAsStream("MinimalDemo.properties")) {
            if (input == null) throw new IOException("MinimalDemo.properties 파일을 찾을 수 없습니다.");
            config.load(input);
            
            HOST = config.getProperty("host", "localhost");
            PORT = Integer.parseInt(config.getProperty("port", "443"));
            POLICY = config.getProperty("policy", "P01");
            DATA = config.getProperty("data", "test");
            TOKEN = config.getProperty("token", "").trim();
            TIMEOUT = Integer.parseInt(config.getProperty("timeout", "10"));

            if (TOKEN.isEmpty()) throw new RuntimeException("JWT 토큰이 설정되지 않았습니다.");
            
        } catch (Exception e) {
            System.err.println("설정 로드 실패: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * SSL 인증서 검증 무시 (테스트 환경용)
     */
    private static SSLContext getInsecureSslContext() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new java.security.SecureRandom());
        return sc;
    }
}