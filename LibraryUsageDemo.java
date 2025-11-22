import java.util.Properties;
import java.io.InputStream;

/**
 * 라이브러리 사용 예제
 * 
 * 초기 설정(Client 생성)과 실행(암/복호화 반복)을 분리하여 보여줍니다.
 */
public class LibraryUsageDemo {

    // 1. 초기 설정 (한 번만 수행)
    private static CrdpClient initializeClient() {
        // 기본값
        String host = "192.168.0.233";
        int port = 32182;
        String policy = "P01";
        // 실제 토큰으로 교체 필요
        String token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjIwNzkzNTU2MjAsImlzcyI6Imp3dC1pc3N1ZXIiLCJzdWIiOiJkZXYtdXNlcjAxIiwiaWF0IjoxNzYzNzc5NjY4fQ.OZ1VDGCEms6_jUEmLaHOWAZjpcBewyI9cBo96Z0Z6ZySWSnHR95vg3nISaLfDiKW6AUO0bl7-9X39r2B8bqVOw";

        // 설정 파일이 있다면 로드 (선택 사항)
        try (InputStream input = LibraryUsageDemo.class.getClassLoader()
                .getResourceAsStream("MinimalDemo.properties")) {
            if (input != null) {
                Properties config = new Properties();
                config.load(input);
                host = config.getProperty("host", host);
                port = Integer.parseInt(config.getProperty("port", String.valueOf(port)));
                policy = config.getProperty("policy", policy);
                token = config.getProperty("token", token);
            }
        } catch (Exception e) {
            System.out.println("설정 파일 로드 건너뜀: " + e.getMessage());
        }

        System.out.println(">>> 클라이언트 초기화 완료 (" + host + ":" + port + ")");
        return new CrdpClient(host, port, policy, token, 10);
    }

    public static void main(String[] args) {
        try {
            // [Step 1] 초기화
            CrdpClient client = initializeClient();

            // [Step 2] 실행 (단일 호출)
            String originalData = "Hello, CRDP!";
            System.out.println("\n>>> 실행 테스트");
            System.out.println("원본: " + originalData);

            // 암호화
            String encrypted = client.protect(originalData);
            System.out.println("암호화: " + encrypted);

            // 복호화
            String decrypted = client.reveal(encrypted);
            System.out.println("복호화: " + decrypted);

            // 검증
            if (originalData.equals(decrypted)) {
                System.out.println("결과: 일치함 (성공)");
            } else {
                System.err.println("결과: 불일치 (실패)");
            }

        } catch (Exception e) {
            System.err.println("오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
