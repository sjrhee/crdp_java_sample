import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Simple CRDP Client for protect/reveal operations
 * Uses only Java standard library (no external dependencies)
 */
public class CrdpClient {
    private final String baseUrl;
    private final int timeout;
    
    public CrdpClient(String host, int port, int timeoutSeconds) {
        this.baseUrl = String.format("http://%s:%d", host, port);
        this.timeout = timeoutSeconds * 1000; // Convert to milliseconds
    }
    
    /**
     * Call the protect API
     */
    public ApiResponse protect(String policy, String data) {
        String url = baseUrl + "/v1/protect";
        String requestBody = String.format(
            "{\"protection_policy_name\":\"%s\",\"data\":\"%s\"}", 
            policy, data
        );
        
        return makeRequest(url, requestBody, "PROTECT");
    }
    
    /**
     * Call the reveal API
     */
    public ApiResponse reveal(String policy, String protectedData, String externalVersion, String username) {
        String url = baseUrl + "/v1/reveal";
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{\"protection_policy_name\":\"").append(policy).append("\"");
        requestBody.append(",\"protected_data\":\"").append(protectedData).append("\"");
        
        if (externalVersion != null && !externalVersion.trim().isEmpty()) {
            requestBody.append(",\"external_version\":\"").append(externalVersion).append("\"");
        }
        if (username != null && !username.trim().isEmpty()) {
            requestBody.append(",\"username\":\"").append(username).append("\"");
        }
        requestBody.append("}");
        
        return makeRequest(url, requestBody.toString(), "REVEAL");
    }
    
    private ApiResponse makeRequest(String urlString, String requestBody, String operation) {
        long startTime = System.nanoTime();
        
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // Set request properties
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            
            // Send request body
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody);
                writer.flush();
            }
            
            // Read response
            int statusCode = conn.getResponseCode();
            String responseBody = readResponse(conn);
            
            long endTime = System.nanoTime();
            double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
            
            return new ApiResponse(statusCode, responseBody, durationSeconds, urlString, requestBody, operation);
            
        } catch (Exception e) {
            long endTime = System.nanoTime();
            double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
            return new ApiResponse(0, "ERROR: " + e.getMessage(), durationSeconds, urlString, requestBody, operation);
        }
    }
    
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