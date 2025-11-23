/**
 * CRDP 클라이언트 라이브러리 사용 예제
 * 
 * "이렇게 쉽습니다!" - 설정하고, 호출하면 끝.
 */
public class SimpleExample {

    public static void main(String[] args) {
        try {
            // ---------------------------------------------------------------
            // 1. 초기화 (설정 파일에서 자동 로드)
            // ---------------------------------------------------------------
            CrdpClient crdp = CrdpClient.fromConfigFile("crdp.properties");
            System.out.println(">>> CRDP 클라이언트 준비 완료 (설정 파일 로드됨)");

            // ---------------------------------------------------------------
            // 2. 사용 (어디서든 한 줄로 호출)
            // ---------------------------------------------------------------
            String original = "주민등록번호 123456-1234567";

            // 암호화
            String encrypted = crdp.protect(original);
            System.out.println("암호화: " + encrypted);

            // 복호화
            String decrypted = crdp.reveal(encrypted);
            System.out.println("복호화: " + decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
