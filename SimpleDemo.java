import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 단순한 CRDP 데모 - 하나의 파일로 모든 기능 제공
 * TLS/HTTPS와 JWT 지원
 */
public class SimpleDemo {
    private static Properties config = new Properties();
    
    // 기본 설정값들 (properties 파일에서 로드됨)
    private static String DEFAULT_HOST = "49.50.138.96";
    private static int DEFAULT_PORT = 32082;
    private static String DEFAULT_POLICY = "P01";
    private static String DEFAULT_DATA = "1234567890123";
    private static int DEFAULT_TIMEOUT = 10;
    private static boolean DEFAULT_TLS = false;
    private static String DEFAULT_TOKEN = "";
    
    static {
        // SimpleDemo.properties 파일 로드
        try (InputStream input = SimpleDemo.class.getClassLoader()
                .getResourceAsStream("SimpleDemo.properties")) {
            if (input != null) {
                config.load(input);
                
                // properties 파일에서 설정값 읽기
                DEFAULT_HOST = config.getProperty("host", DEFAULT_HOST);
                DEFAULT_PORT = Integer.parseInt(
                    config.getProperty("port", String.valueOf(DEFAULT_PORT)));
                DEFAULT_POLICY = config.getProperty("policy", DEFAULT_POLICY);
                DEFAULT_DATA = config.getProperty("data", DEFAULT_DATA);
                DEFAULT_TIMEOUT = Integer.parseInt(
                    config.getProperty("timeout", String.valueOf(DEFAULT_TIMEOUT)));
                DEFAULT_TLS = Boolean.parseBoolean(
                    config.getProperty("tls", String.valueOf(DEFAULT_TLS)));
                DEFAULT_TOKEN = config.getProperty("token", DEFAULT_TOKEN);
            }
        } catch (IOException e) {
            // properties 파일이 없으면 기본값 사용
            System.err.println("경고: SimpleDemo.properties 파일을 찾을 수 없습니다. 기본값을 사용합니다.");
        }
    }
    
    /**
     * 도움말 출력
     */
    private static void showHelp() {
        System.out.println("사용법: java SimpleDemo [옵션]");
        System.out.println("  --host HOST     서버 주소 (기본: " + DEFAULT_HOST + ")");
        System.out.println("  --port PORT     포트 번호 (기본: " + DEFAULT_PORT + ")");
        System.out.println("  --policy POLICY 정책 이름 (기본: " + DEFAULT_POLICY + ")");
        System.out.println("  --data DATA     보호할 데이터 (기본: " + DEFAULT_DATA + ")");
        System.out.println("  --tls           HTTPS 사용 (기본: " + DEFAULT_TLS + ")");
        System.out.println("  --token TOKEN   JWT 토큰 (Authorization 헤더용)");
        System.out.println("  --help          도움말");
    }
    
    public static void main(String[] args) {
        // 기본 설정 (properties 파일에서 로드됨)
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String policy = DEFAULT_POLICY;
        String data = DEFAULT_DATA;
        int timeout = DEFAULT_TIMEOUT;
        boolean useTLS = DEFAULT_TLS;
        String token = DEFAULT_TOKEN;
        
        // 명령행 처리
        for (int i = 0; i < args.length; i++) {
            if ("--host".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    host = args[++i];
                } else {
                    System.err.println("오류: --host 옵션에 값이 필요합니다.");
                    System.err.println();
                    showHelp();
                    return;
                }
            } else if ("--port".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    try {
                        port = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("오류: --port 옵션에 유효한 숫자가 필요합니다: " + args[i]);
                        System.err.println();
                        showHelp();
                        return;
                    }
                } else {
                    System.err.println("오류: --port 옵션에 값이 필요합니다.");
                    System.err.println();
                    showHelp();
                    return;
                }
            } else if ("--policy".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    policy = args[++i];
                } else {
                    System.err.println("오류: --policy 옵션에 값이 필요합니다.");
                    System.err.println();
                    showHelp();
                    return;
                }
            } else if ("--data".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    data = args[++i];
                } else {
                    System.err.println("오류: --data 옵션에 값이 필요합니다.");
                    System.err.println();
                    showHelp();
                    return;
                }
            } else if ("--tls".equals(args[i])) {
                useTLS = true;
            } else if ("--token".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    token = args[++i];
                } else {
                    System.err.println("오류: --token 옵션에 값이 필요합니다.");
                    System.err.println();
                    showHelp();
                    return;
                }
            } else if ("--help".equals(args[i])) {
                showHelp();
                return;
            } else {
                System.err.println("오류: 알 수 없는 옵션입니다: " + args[i]);
                System.err.println();
                showHelp();
                return;
            }
        }
        
        System.out.println("=== CRDP 간단 데모 ===");
        System.out.println("서버: " + (useTLS ? "https" : "http") + "://" + host + ":" + port);
        System.out.println("정책: " + policy);
        System.out.println("데이터: " + data);
        if (useTLS) {
            System.out.println("TLS: 활성화됨");
        }
        if (!token.isEmpty()) {
            System.out.println("JWT: 활성화됨");
        }
        System.out.println();
        
        try {
            // 1. 데이터 보호
            System.out.print("1. 데이터 보호 중... ");
            String protectedData = protect(host, port, policy, data, useTLS, token, timeout);
            if (protectedData != null) {
                System.out.println("성공: " + protectedData);
            } else {
                System.out.println("실패");
                return;
            }
            
            // 2. 데이터 복원
            System.out.print("2. 데이터 복원 중... ");
            String revealedData = reveal(host, port, policy, protectedData, useTLS, token, timeout);
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
    private static String protect(String host, int port, String policy, String data, boolean useTLS, String token, int timeout) {
        String protocol = useTLS ? "https" : "http";
        String url = protocol + "://" + host + ":" + port + "/v1/protect";
        String json = "{\"protection_policy_name\":\"" + policy + "\",\"data\":\"" + data + "\"}";
        String response = post(url, json, token, timeout);
        return extractValue(response, "protected_data");
    }
    
    /**
     * 데이터 복원
     */
    private static String reveal(String host, int port, String policy, String protectedData, boolean useTLS, String token, int timeout) {
        String protocol = useTLS ? "https" : "http";
        String url = protocol + "://" + host + ":" + port + "/v1/reveal";
        String json = "{\"protection_policy_name\":\"" + policy + "\",\"protected_data\":\"" + protectedData + "\"}";
        String response = post(url, json, token, timeout);
        return extractValue(response, "data");
    }
    
    /**
     * HTTP/HTTPS POST 요청
     */
    private static String post(String urlString, String json, String token, int timeout) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn;
            
            if (urlString.startsWith("https")) {
                // HTTPS 연결 설정 (자체 서명 인증서 지원)
                conn = (HttpsURLConnection) url.openConnection();
                HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
                httpsConn.setSSLSocketFactory(getInsecureSslContext().getSocketFactory());
                httpsConn.setHostnameVerifier((hostname, session) -> true);
            } else {
                // HTTP 연결
                conn = (HttpURLConnection) url.openConnection();
            }
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if (!token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeout * 1000);
            conn.setReadTimeout(timeout * 1000);
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
    
    /**
     * 인증서 검증을 비활성화한 SSL 컨텍스트 생성
     * (자체 서명 인증서 또는 테스트 목적용)
     */
    private static SSLContext getInsecureSslContext() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
        
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc;
    }
}