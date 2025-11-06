/**
 * Simple CRDP CLI Demo
 * Performs a single protect -> reveal operation
 */
public class CrdpDemo {
    
    public static void main(String[] args) {
        // Default configuration
        String host = "192.168.0.231";
        int port = 32082;
        String policy = "P03";
        String data = "1234567890123";
        String username = null;
        int timeout = 10;
        boolean showBodies = false;
        boolean verbose = false;
        
        // Parse command line arguments
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
                case "--username":
                    if (i + 1 < args.length) username = args[++i];
                    break;
                case "--timeout":
                    if (i + 1 < args.length) timeout = Integer.parseInt(args[++i]);
                    break;
                case "--show-bodies":
                    showBodies = true;
                    break;
                case "--verbose":
                    verbose = true;
                    break;
                case "--help":
                    printHelp();
                    return;
                default:
                    if (args[i].startsWith("--")) {
                        System.err.println("Unknown option: " + args[i]);
                        printHelp();
                        System.exit(1);
                    }
            }
        }
        
        System.out.println("=== CRDP Java Demo ===");
        System.out.printf("Host: %s:%d%n", host, port);
        System.out.printf("Policy: %s%n", policy);
        System.out.printf("Data: %s%n", data);
        if (username != null) {
            System.out.printf("Username: %s%n", username);
        }
        System.out.println();
        
        CrdpClient client = new CrdpClient(host, port, timeout);
        
        try {
            long startTime = System.nanoTime();
            
            // Step 1: Protect
            System.out.println("1. Calling protect API...");
            ApiResponse protectResponse = client.protect(policy, data);
            
            if (verbose) {
                System.out.println("   " + protectResponse);
            }
            
            if (showBodies) {
                System.out.println("   Request URL: " + protectResponse.getRequestUrl());
                System.out.println("   Request Body: " + protectResponse.getRequestBody());
                System.out.println("   Response Body: " + protectResponse.getResponseBody());
                System.out.println();
            }
            
            if (!protectResponse.isSuccess()) {
                System.err.printf("   ERROR: Protect failed with status %d%n", protectResponse.getStatusCode());
                System.err.println("   Response: " + protectResponse.getResponseBody());
                System.exit(1);
            }
            
            String protectedData = protectResponse.extractProtectedData();
            String externalVersion = protectResponse.extractExternalVersion();
            
            if (protectedData == null) {
                System.err.println("   ERROR: Could not extract protected_data from response");
                System.err.println("   Response: " + protectResponse.getResponseBody());
                System.exit(1);
            }
            
            System.out.printf("   SUCCESS: Protected data length = %d%n", protectedData.length());
            if (verbose && externalVersion != null) {
                System.out.printf("   External version: %s%n", externalVersion);
            }
            System.out.println();
            
            // Step 2: Reveal
            System.out.println("2. Calling reveal API...");
            ApiResponse revealResponse = client.reveal(policy, protectedData, externalVersion, username);
            
            if (verbose) {
                System.out.println("   " + revealResponse);
            }
            
            if (showBodies) {
                System.out.println("   Request URL: " + revealResponse.getRequestUrl());
                System.out.println("   Request Body: " + revealResponse.getRequestBody());
                System.out.println("   Response Body: " + revealResponse.getResponseBody());
                System.out.println();
            }
            
            if (!revealResponse.isSuccess()) {
                System.err.printf("   ERROR: Reveal failed with status %d%n", revealResponse.getStatusCode());
                System.err.println("   Response: " + revealResponse.getResponseBody());
                System.exit(1);
            }
            
            String revealedData = revealResponse.extractRevealedData();
            
            if (revealedData == null) {
                System.err.println("   ERROR: Could not extract data from reveal response");
                System.err.println("   Response: " + revealResponse.getResponseBody());
                System.exit(1);
            }
            
            System.out.printf("   SUCCESS: Revealed data = %s%n", revealedData);
            System.out.println();
            
            // Step 3: Verify
            boolean dataMatches = data.equals(revealedData);
            System.out.println("3. Verification:");
            System.out.printf("   Original data:  %s%n", data);
            System.out.printf("   Revealed data:  %s%n", revealedData);
            System.out.printf("   Data matches:   %s%n", dataMatches ? "YES" : "NO");
            System.out.println();
            
            // Summary
            long endTime = System.nanoTime();
            double totalTime = (endTime - startTime) / 1_000_000_000.0;
            
            System.out.println("=== Summary ===");
            System.out.printf("Protect operation:  %s (%.4fs)%n", 
                             protectResponse.isSuccess() ? "SUCCESS" : "FAILED", 
                             protectResponse.getDurationSeconds());
            System.out.printf("Reveal operation:   %s (%.4fs)%n", 
                             revealResponse.isSuccess() ? "SUCCESS" : "FAILED", 
                             revealResponse.getDurationSeconds());
            System.out.printf("Data verification:  %s%n", dataMatches ? "PASSED" : "FAILED");
            System.out.printf("Total time:         %.4fs%n", totalTime);
            
            if (!dataMatches) {
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
    
    private static void printHelp() {
        System.out.println("Usage: java CrdpDemo [OPTIONS]");
        System.out.println();
        System.out.println("Simple CRDP protect/reveal demo");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --host HOST          API host (default: 192.168.0.231)");
        System.out.println("  --port PORT          API port (default: 32082)");
        System.out.println("  --policy POLICY      Protection policy name (default: P03)");
        System.out.println("  --data DATA          Data to protect (default: 1234567890123)");
        System.out.println("  --username USER      Username for reveal operation (optional)");
        System.out.println("  --timeout SECONDS    Request timeout in seconds (default: 10)");
        System.out.println("  --show-bodies        Show request/response bodies");
        System.out.println("  --verbose            Enable verbose output");
        System.out.println("  --help               Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java CrdpDemo");
        System.out.println("  java CrdpDemo --host 10.0.0.100 --port 8080 --policy MyPolicy");
        System.out.println("  java CrdpDemo --data \"my-secret-data\" --username testuser --verbose");
    }
}