import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 단순한 CRDP 데모 - 하나의 파일로 모든 기능 제공
 */
public class SimpleDemo {
    
    public static void main(String[] args) {
        // 기본 설정
        String host = "sjrhee.ddns.net";
        int port = 32082;
        String policy = "P01";
        String data = "1234567890123";
        
        // 명령행 처리
        for (int i = 0; i < args.length; i++) {
            if ("--host".equals(args[i]) && i + 1 < args.length) {
                host = args[++i];
            } else if ("--port".equals(args[i]) && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if ("--policy".equals(args[i]) && i + 1 < args.length) {
                policy = args[++i];
            } else if ("--data".equals(args[i]) && i + 1 < args.length) {
                data = args[++i];
            } else if ("--help".equals(args[i])) {
                System.out.println("사용법: java SimpleDemo [옵션]");
                System.out.println("  --host HOST     서버 주소 (기본: sjrhee.ddns.net)");
                System.out.println("  --port PORT     포트 번호 (기본: 32082)");
                System.out.println("  --policy POLICY 정책 이름 (기본: P01)");
                System.out.println("  --data DATA     보호할 데이터 (기본: 1234567890123)");
                System.out.println("  --help          도움말");
                return;
            }
        }
        
        System.out.println("=== CRDP 간단 데모 ===");
        System.out.println("서버: " + host + ":" + port);
        System.out.println("정책: " + policy);
        System.out.println("데이터: " + data);
        System.out.println();
        
        try {
            // 1. 데이터 보호
            System.out.print("1. 데이터 보호 중... ");
            String protectedData = protect(host, port, policy, data);
            if (protectedData != null) {
                System.out.println("성공: " + protectedData);
            } else {
                System.out.println("실패");
                return;
            }
            
            // 2. 데이터 복원
            System.out.print("2. 데이터 복원 중... ");
            String revealedData = reveal(host, port, policy, protectedData);
            if (revealedData != null) {
                System.out.println("성공: " + revealedData);
            } else {
                System.out.println("실패");
                return;
            }
            
            // 3. 검증
            System.out.println();
            System.out.println("3. 검증 결과:");
            System.out.println("   원본: " + data);
            System.out.println("   복원: " + revealedData);
            System.out.println("   일치: " + (data.equals(revealedData) ? "예" : "아니오"));
            
        } catch (Exception e) {
            System.err.println("오류: " + e.getMessage());
        }
    }
    
    /**
     * 데이터 보호
     */
    private static String protect(String host, int port, String policy, String data) {
        String url = "http://" + host + ":" + port + "/v1/protect";
        String json = "{\"protection_policy_name\":\"" + policy + "\",\"data\":\"" + data + "\"}";
        String response = post(url, json);
        return extractValue(response, "protected_data");
    }
    
    /**
     * 데이터 복원
     */
    private static String reveal(String host, int port, String policy, String protectedData) {
        String url = "http://" + host + ":" + port + "/v1/reveal";
        String json = "{\"protection_policy_name\":\"" + policy + "\",\"protected_data\":\"" + protectedData + "\"}";
        String response = post(url, json);
        return extractValue(response, "data");
    }
    
    /**
     * HTTP POST 요청
     */
    private static String post(String urlString, String json) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            // 요청 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            
            // 응답 읽기
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            return response.toString();
            
        } catch (Exception e) {
            System.err.println("HTTP 오류: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * JSON에서 값 추출
     */
    private static String extractValue(String json, String key) {
        if (json == null) return null;
        
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        
        return json.substring(start, end);
    }
}