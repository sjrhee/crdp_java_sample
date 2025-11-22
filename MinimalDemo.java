import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 최소한의 CRDP 데모 - 설정 기반, HTTPS와 JWT만 지원
 */
public class MinimalDemo {
    private static Properties config;
    
    static {
        // MinimalDemo.properties 파일 로드
        config = new Properties();
        try (InputStream input = MinimalDemo.class.getClassLoader()
                .getResourceAsStream("MinimalDemo.properties")) {
            if (input != null) {
                config.load(input);
            } else {
                System.err.println("경고: MinimalDemo.properties 파일을 찾을 수 없습니다.");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("오류: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        String host = config.getProperty("host", "localhost");
        int port = Integer.parseInt(config.getProperty("port", "443"));
        String policy = config.getProperty("policy", "P01");
        String data = config.getProperty("data", "test");
        int timeout = Integer.parseInt(config.getProperty("timeout", "10"));
        String token = config.getProperty("token", "").trim();
        
        // JWT 토큰 필수 확인
        if (token.isEmpty()) {
            System.err.println("오류: JWT 토큰이 필요합니다. MinimalDemo.properties의 token 설정을 확인하세요.");
            System.exit(1);
        }
        
        String protectUrl = String.format("https://%s:%d/v1/protect", host, port);
        String revealUrl = String.format("https://%s:%d/v1/reveal", host, port);
        
        System.out.println("=== CRDP 최소 데모 (HTTPS) ===");
        System.out.println("서버: https://" + host + ":" + port);
        System.out.println("정책: " + policy);
        System.out.println("데이터: " + data);
        
        try {
            // 1. 데이터 보호
            String protectJson = "{\"protection_policy_name\":\"" + policy + "\",\"data\":\"" + data + "\"}";
            String protectedData = post(protectUrl, protectJson, token, timeout);
            
            if (protectedData == null) {
                System.err.println("보호 실패");
                return;
            }
            
            System.out.println("\n1. 데이터 보호 중... 성공: " + protectedData);
            
            // 2. 데이터 복원
            String revealJson = "{\"protection_policy_name\":\"" + policy + "\",\"protected_data\":\"" + protectedData + "\"}";
            String revealedData = post(revealUrl, revealJson, token, timeout);
            
            if (revealedData == null) {
                System.err.println("복원 실패");
                return;
            }
            
            System.out.println("2. 데이터 복원 중... 성공: " + revealedData);
            
            // 3. 검증
            System.out.println("\n3. 검증 결과:");
            System.out.println("   원본: " + data);
            System.out.println("   복원: " + revealedData);
            System.out.println("   일치: " + (data.equals(revealedData) ? "예" : "아니오"));
            
        } catch (Exception e) {
            System.err.println("오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * HTTPS POST 요청
     */
    private static String post(String urlStr, String json, String token, int timeout) {
        try {
            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(getInsecureSslContext().getSocketFactory());
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            conn.setConnectTimeout(timeout * 1000);
            conn.setReadTimeout(timeout * 1000);
            conn.setDoOutput(true);
            
            // 요청 본문 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            
            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                System.err.println("HTTP 오류: " + status);
                return null;
            }
            
            // 응답 읽기
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            return extractValue(response.toString());
            
        } catch (Exception e) {
            System.err.println("요청 실패: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * JSON 응답에서 값 추출 (보호된 데이터 또는 복원된 데이터)
     */
    private static String extractValue(String json) {
        // "data":"value" 또는 "protected_data":"value" 패턴 찾기
        String[] patterns = {"\"data\":", "\"protected_data\":"};
        
        for (String pattern : patterns) {
            int start = json.indexOf(pattern);
            if (start != -1) {
                start = json.indexOf("\"", start + pattern.length()) + 1;
                int end = json.indexOf("\"", start);
                
                if (end > start) {
                    String value = json.substring(start, end);
                    // 이스케이프 문자 처리
                    return value.replace("\\\"", "\"")
                               .replace("\\\\", "\\");
                }
            }
        }
        
        return null;
    }
    
    /**
     * 자체 서명 인증서를 신뢰하는 SSL Context 생성 (사내 환경용)
     */
    private static SSLContext getInsecureSslContext() throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        
        TrustManager[] trustManagers = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };
        
        context.init(null, trustManagers, new java.security.SecureRandom());
        return context;
    }
}
