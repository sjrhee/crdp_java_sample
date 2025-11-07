import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 단순한 CRDP 데모 - 하나의 파일로 모든 기능 제공
 */
public class SimpleDemo {
    // 기본 설정값들
    private static final String DEFAULT_HOST = "49.50.138.96";
    private static final int DEFAULT_PORT = 32082;
    private static final String DEFAULT_POLICY = "P01";
    private static final String DEFAULT_DATA = "1234567890123";
    
    public static void main(String[] args) {
        // 기본 설정
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String policy = DEFAULT_POLICY;
        String data = DEFAULT_DATA;
        
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
                System.out.println("  --host HOST     서버 주소 (기본: " + DEFAULT_HOST + ")");
                System.out.println("  --port PORT     포트 번호 (기본: " + DEFAULT_PORT + ")");
                System.out.println("  --policy POLICY 정책 이름 (기본: " + DEFAULT_POLICY + ")");
                System.out.println("  --data DATA     보호할 데이터 (기본: " + DEFAULT_DATA + ")");
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
            
            String responseStr = response.toString();
            return responseStr;
            
        } catch (Exception e) {
            System.err.println("HTTP 오류: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * JSON에서 값 추출 (더 견고한 파싱)
     */
    private static String extractValue(String json, String key) {
        if (json == null || json.trim().isEmpty()) return null;
        
        // JSON 객체 시작과 끝 찾기
        int startBrace = json.indexOf('{');
        int endBrace = json.lastIndexOf('}');
        if (startBrace == -1 || endBrace == -1 || startBrace >= endBrace) return null;
        
        String content = json.substring(startBrace + 1, endBrace).trim();
        
        // 키-값 쌍 찾기
        String keyPattern = "\"" + key + "\":";
        int keyIndex = content.indexOf(keyPattern);
        if (keyIndex == -1) return null;
        
        // 값 시작 위치 찾기
        int valueStart = keyIndex + keyPattern.length();
        
        // 공백 건너뛰기
        while (valueStart < content.length() && Character.isWhitespace(content.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= content.length()) return null;
        
        char firstChar = content.charAt(valueStart);
        if (firstChar == '"') {
            // 문자열 값
            valueStart++; // 따옴표 건너뛰기
            int valueEnd = valueStart;
            while (valueEnd < content.length() && content.charAt(valueEnd) != '"') {
                if (content.charAt(valueEnd) == '\\') {
                    valueEnd++; // 이스케이프 문자 건너뛰기
                }
                valueEnd++;
            }
            return content.substring(valueStart, valueEnd);
        } else {
            // 숫자나 다른 값 (쉼표나 중괄호까지)
            int valueEnd = valueStart;
            while (valueEnd < content.length() && content.charAt(valueEnd) != ',' && content.charAt(valueEnd) != '}') {
                valueEnd++;
            }
            return content.substring(valueStart, valueEnd).trim();
        }
    }
}