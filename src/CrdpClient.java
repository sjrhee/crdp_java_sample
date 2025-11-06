import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 간단한 CRDP 클라이언트 - protect/reveal API 호출
 */
public class CrdpClient {
    private final String baseUrl;
    private final int timeout;
    
    public CrdpClient(String host, int port, int timeoutSeconds) {
        this.baseUrl = String.format("http://%s:%d", host, port);
        this.timeout = timeoutSeconds * 1000; // 밀리초로 변환
    }
    
    /**
     * 데이터 보호 (암호화/토큰화)
     */
    public ApiResponse protect(String policy, String data) {
        String url = baseUrl + "/v1/protect";
        String requestBody = String.format(
            "{\"protection_policy_name\":\"%s\",\"data\":\"%s\"}", 
            policy, data
        );
        
        return makeRequest(url, requestBody);
    }
    
    /**
     * 데이터 복원 (복호화/디토큰화)
     */
    public ApiResponse reveal(String policy, String protectedData) {
        String url = baseUrl + "/v1/reveal";
        String requestBody = String.format(
            "{\"protection_policy_name\":\"%s\",\"protected_data\":\"%s\"}", 
            policy, protectedData
        );
        
        return makeRequest(url, requestBody);
    }
    
    /**
     * HTTP 요청 실행
     */
    private ApiResponse makeRequest(String urlString, String requestBody) {
        long startTime = System.nanoTime();
        
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            
            // 요청 본문 전송
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody);
                writer.flush();
            }
            
            // 응답 읽기
            int statusCode = conn.getResponseCode();
            String responseBody = readResponse(conn);
            
            long endTime = System.nanoTime();
            double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
            
            return new ApiResponse(statusCode, responseBody, durationSeconds);
            
        } catch (Exception e) {
            long endTime = System.nanoTime();
            double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
            return new ApiResponse(0, "ERROR: " + e.getMessage(), durationSeconds);
        }
    }
    
    /**
     * HTTP 응답 본문 읽기
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream inputStream;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
            inputStream = conn.getInputStream();
        } else {
            inputStream = conn.getErrorStream();
        }
        
        if (inputStream == null) {
            return "";
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}