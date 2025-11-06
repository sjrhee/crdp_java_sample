/**
 * API Response wrapper - 간단한 HTTP 응답 정보
 */
public class ApiResponse {
    private final int statusCode;
    private final String responseBody;
    private final double durationSeconds;
    
    public ApiResponse(int statusCode, String responseBody, double durationSeconds) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.durationSeconds = durationSeconds;
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
    
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Protect 응답에서 보호된 데이터 추출
     */
    public String getProtectedData() {
        if (responseBody == null) return null;
        
        String pattern = "\"protected_data\":\"";
        int start = responseBody.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        int end = responseBody.indexOf("\"", start);
        if (end == -1) return null;
        
        return responseBody.substring(start, end);
    }
    
    /**
     * Reveal 응답에서 원본 데이터 추출
     */
    public String getRevealedData() {
        if (responseBody == null) return null;
        
        String pattern = "\"data\":\"";
        int start = responseBody.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        int end = responseBody.indexOf("\"", start);
        if (end == -1) return null;
        
        return responseBody.substring(start, end);
    }
}