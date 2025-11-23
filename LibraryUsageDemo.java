
/**
 * 라이브러리 사용 예제
 * 
 * "이렇게 쉽습니다!" - 설정하고, 호출하면 끝.
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LibraryUsageDemo {

    public static void main(String[] args) {
        try {
            // ---------------------------------------------------------------
            // 1. 설정 로드 (외부 파일)
            // ---------------------------------------------------------------
            Properties config = new Properties();
            try (FileInputStream fis = new FileInputStream("crdp.properties")) {
                config.load(fis);
            } catch (IOException e) {
                System.err.println("설정 파일을 찾을 수 없습니다: crdp.properties");
                System.exit(1);
            }

            String endpoint = config.getProperty("endpoint");
            String policy = config.getProperty("policy");
            String token = config.getProperty("token");

            if (endpoint == null || policy == null || token == null) {
                System.err.println("설정 파일에 필수 항목(endpoint, policy, token)이 누락되었습니다.");
                System.exit(1);
            }

            // ---------------------------------------------------------------
            // 2. 클라이언트 초기화
            // ---------------------------------------------------------------
            CrdpClient client = new CrdpClient(endpoint, policy, token);
            System.out.println(">>> CRDP 클라이언트 준비 완료 (설정 파일 로드됨)");

            // ---------------------------------------------------------------
            // 2. 사용 (어디서든 한 줄로 호출)
            // ---------------------------------------------------------------
            String original = "주민등록번호 123456-1234567";

            // 암호화
            String encrypted = client.protect(original);
            System.out.println("암호화: " + encrypted);

            // 복호화
            String decrypted = client.reveal(encrypted);
            System.out.println("복호화: " + decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
