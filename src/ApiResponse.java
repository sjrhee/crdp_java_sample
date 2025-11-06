/**
 * API Response wrapper
 */
public class ApiResponse {
    private final int statusCode;
    private final String responseBody;
    private final double durationSeconds;
    private final String requestUrl;
    private final String requestBody;
    private final String operation;
    
    public ApiResponse(int statusCode, String responseBody, double durationSeconds, 
                      String requestUrl, String requestBody, String operation) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.durationSeconds = durationSeconds;
        this.requestUrl = requestUrl;
        this.requestBody = requestBody;
        this.operation = operation;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public double getDurationSeconds() {
        return durationSeconds;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Extract protected_data from protect response
     */
    public String extractProtectedData() {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return null;
        }
        
        // Simple JSON parsing for "protected_data" field
        String pattern = "\"protected_data\":\"";
        int start = responseBody.indexOf(pattern);
        if (start == -1) {
            return null;
        }
        
        start += pattern.length();
        int end = responseBody.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        
        return responseBody.substring(start, end);
    }
    
    /**
     * Extract external_version from protect response
     */
    public String extractExternalVersion() {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return null;
        }
        
        // Simple JSON parsing for "external_version" field
        String pattern = "\"external_version\":\"";
        int start = responseBody.indexOf(pattern);
        if (start == -1) {
            return null;
        }
        
        start += pattern.length();
        int end = responseBody.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        
        return responseBody.substring(start, end);
    }
    
    /**
     * Extract data from reveal response
     */
    public String extractRevealedData() {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return null;
        }
        
        // Simple JSON parsing for "data" field
        String pattern = "\"data\":\"";
        int start = responseBody.indexOf(pattern);
        if (start == -1) {
            return null;
        }
        
        start += pattern.length();
        int end = responseBody.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        
        return responseBody.substring(start, end);
    }
    
    @Override
    public String toString() {
        return String.format("%s: status=%d, duration=%.4fs, success=%s", 
                           operation, statusCode, durationSeconds, isSuccess());
    }
}