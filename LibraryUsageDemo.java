/**
 * 라이브러리 사용 예제
 * 
 * CrdpClient 라이브러리를 사용하여 얼마나 쉽게 암호화/복호화를 할 수 있는지 보여줍니다.
 */
public class LibraryUsageDemo {
    public static void main(String[] args) {
        // 1. 클라이언트 초기화 (호스트, 포트, 정책, 토큰, 타임아웃)
        // 실제 환경에서는 설정 파일이나 환경 변수에서 값을 가져오세요.
        String host = "192.168.0.233"; // 예시 IP
        int port = 32182;
        String policy = "P01";
        String token = "YOUR_JWT_TOKEN_HERE"; // 실제 토큰으로 교체 필요

        // MinimalDemo.properties가 있다면 거기서 읽어올 수도 있습니다 (테스트 편의상)
        try {
            java.util.Properties config = new java.util.Properties();
            java.io.InputStream input = LibraryUsageDemo.class.getClassLoader()
                    .getResourceAsStream("MinimalDemo.properties");
            if (input != null) {
                config.load(input);
                host = config.getProperty("host", host);
                port = Integer.parseInt(config.getProperty("port", String.valueOf(port)));
                policy = config.getProperty("policy", policy);
                token = config.getProperty("token", token);
            }
        } catch (Exception e) {
            System.out.println("설정 파일 로드 실패, 기본값 사용");
        }

        CrdpClient client = new CrdpClient(host, port, policy, token, 10);

        try {
            String originalData = "Hello, CRDP Library!";
            System.out.println("원본 데이터: " + originalData);

            // 2. 암호화
            String encrypted = client.protect(originalData);
            System.out.println("암호화 결과: " + encrypted);

            // 3. 복호화
            String decrypted = client.reveal(encrypted);
            System.out.println("복호화 결과: " + decrypted);

            // 검증
            if (originalData.equals(decrypted)) {
                System.out.println("검증 성공: 데이터가 일치합니다.");
            } else {
                System.out.println("검증 실패!");
            }

        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
