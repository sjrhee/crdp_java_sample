/**
 * 라이브러리 사용 예제
 * 
 * "이렇게 쉽습니다!" - 설정하고, 호출하면 끝.
 */
public class LibraryUsageDemo {

    public static void main(String[] args) {
        try {
            // ---------------------------------------------------------------
            // 1. 초기화 (딱 한 번만 설정)
            // ---------------------------------------------------------------
            String endpoint = "192.168.0.233:32182";
            String policy = "P01";
            String token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjIwNzkzNTU2MjAsImlzcyI6Imp3dC1pc3N1ZXIiLCJzdWIiOiJkZXYtdXNlcjAxIiwiaWF0IjoxNzYzNzc5NjY4fQ.OZ1VDGCEms6_jUEmLaHOWAZjpcBewyI9cBo96Z0Z6ZySWSnHR95vg3nISaLfDiKW6AUO0bl7-9X39r2B8bqVOw";

            CrdpClient client = new CrdpClient(endpoint, policy, token, 10);
            System.out.println(">>> CRDP 클라이언트 준비 완료");

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
