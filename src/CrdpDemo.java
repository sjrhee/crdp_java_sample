/**
 * 간단한 CRDP 데모 - 데이터 보호와 복원
 */
public class CrdpDemo {
    
    public static void main(String[] args) {
        // 기본 설정
        String host = "sjrhee.ddns.net";
        int port = 32082;
        String policy = "P03";
        String data = "1234567890123";
        boolean showDetails = false;
        
        // 명령행 옵션 처리
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host":
                    if (i + 1 < args.length) host = args[++i];
                    break;
                case "--port":
                    if (i + 1 < args.length) port = Integer.parseInt(args[++i]);
                    break;
                case "--policy":
                    if (i + 1 < args.length) policy = args[++i];
                    break;
                case "--data":
                    if (i + 1 < args.length) data = args[++i];
                    break;
                case "--details":
                    showDetails = true;
                    break;
                case "--help":
                    showHelp();
                    return;
            }
        }
        
        // 설정 출력
        System.out.println("=== CRDP 데모 ===");
        System.out.printf("서버: %s:%d%n", host, port);
        System.out.printf("정책: %s%n", policy);
        System.out.printf("데이터: %s%n", data);
        System.out.println();
        
        CrdpClient client = new CrdpClient(host, port, 10);
        
        try {
            // 1단계: 데이터 보호
            System.out.println("1. 데이터 보호 중...");
            ApiResponse protectResponse = client.protect(policy, data);
            
            if (!protectResponse.isSuccess()) {
                System.err.printf("   실패: HTTP %d%n", protectResponse.getStatusCode());
                System.err.println("   응답: " + protectResponse.getResponseBody());
                return;
            }
            
            String protectedData = protectResponse.getProtectedData();
            if (protectedData == null) {
                System.err.println("   실패: 보호된 데이터를 찾을 수 없음");
                return;
            }
            
            System.out.printf("   성공: %s%n", protectedData);
            if (showDetails) {
                System.out.printf("   소요시간: %.3f초%n", protectResponse.getDurationSeconds());
            }
            System.out.println();
            
            // 2단계: 데이터 복원
            System.out.println("2. 데이터 복원 중...");
            ApiResponse revealResponse = client.reveal(policy, protectedData);
            
            if (!revealResponse.isSuccess()) {
                System.err.printf("   실패: HTTP %d%n", revealResponse.getStatusCode());
                System.err.println("   응답: " + revealResponse.getResponseBody());
                return;
            }
            
            String revealedData = revealResponse.getRevealedData();
            if (revealedData == null) {
                System.err.println("   실패: 복원된 데이터를 찾을 수 없음");
                return;
            }
            
            System.out.printf("   성공: %s%n", revealedData);
            if (showDetails) {
                System.out.printf("   소요시간: %.3f초%n", revealResponse.getDurationSeconds());
            }
            System.out.println();
            
            // 3단계: 검증
            boolean matches = data.equals(revealedData);
            System.out.println("3. 검증 결과:");
            System.out.printf("   원본: %s%n", data);
            System.out.printf("   복원: %s%n", revealedData);
            System.out.printf("   일치: %s%n", matches ? "예" : "아니오");
            
            if (!matches) {
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showHelp() {
        System.out.println("사용법: java CrdpDemo [옵션]");
        System.out.println();
        System.out.println("CRDP 데이터 보호/복원 데모");
        System.out.println();
        System.out.println("옵션:");
        System.out.println("  --host HOST      서버 주소 (기본값: sjrhee.ddns.net)");
        System.out.println("  --port PORT      서버 포트 (기본값: 32082)");
        System.out.println("  --policy POLICY  보호 정책 (기본값: P03)");
        System.out.println("  --data DATA      보호할 데이터 (기본값: 1234567890123)");
        System.out.println("  --details        상세 정보 표시");
        System.out.println("  --help           도움말 표시");
        System.out.println();
        System.out.println("예시:");
        System.out.println("  java CrdpDemo");
        System.out.println("  java CrdpDemo --data \"비밀데이터\"");
        System.out.println("  java CrdpDemo --policy P01 --details");
    }
}