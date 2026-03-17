
/**
 * Encryption Key Generator Utility
 * Generates encryption keys and seeds for all three algorithms
 * Outputs configuration to add to config.properties
 */

import java.security.SecureRandom;
import java.util.Scanner;

class EncryptionKeyGenerator {

  private static final int AES_KEY_SIZE = 32; // 256 bits
  private static final int CHACHA20_KEY_SIZE = 32; // 256 bits
  private static final int DPRG_KEY_SIZE = 32; // 256 bits
  private static final int SEED_SIZE = 32; // 256 bits

  static public void main(String[] args) throws Exception {
    Scanner scanner = new Scanner(System.in);

    System.out.println("========================================");
    System.out.println("  Encryption Key Generator Utility");
    System.out.println("========================================\n");

    System.out.println("Choose encryption algorithm:");
    System.out.println("1. AES-GCM (256-bit key)");
    System.out.println("2. ChaCha20-Poly1305 (256-bit key)");
    System.out.println("3. DPRG-XOR Stream Cipher (256-bit key)");
    System.out.println("4. Generate all keys");
    System.out.print("\nEnter choice (1-4): ");

    int choice = scanner.nextInt();
    scanner.nextLine(); // Consume newline

    System.out.print("Generate seed as well? (y/n) [default: y]: ");
    String seedChoice = scanner.nextLine().trim().toLowerCase();
    boolean generateSeed = !seedChoice.equals("n");

    SecureRandom random = new SecureRandom();

    switch (choice) {
      case 1:
        generateKeysForAlgorithm("AES-GCM", "aes-gcm", AES_KEY_SIZE, random, generateSeed);
        break;
      case 2:
        generateKeysForAlgorithm("ChaCha20-Poly1305", "chacha20-poly1305", CHACHA20_KEY_SIZE, random, generateSeed);
        break;
      case 3:
        generateKeysForAlgorithm("DPRG-XOR", "dprg-xor", DPRG_KEY_SIZE, random, generateSeed);
        break;
      case 4:
        generateKeysForAlgorithm("AES-GCM", "aes-gcm", AES_KEY_SIZE, random, generateSeed);
        generateKeysForAlgorithm("ChaCha20-Poly1305", "chacha20-poly1305", CHACHA20_KEY_SIZE, random, generateSeed);
        generateKeysForAlgorithm("DPRG-XOR", "dprg-xor", DPRG_KEY_SIZE, random, generateSeed);
        break;
      default:
        System.out.println("Invalid choice!");
        System.exit(-1);
    }

    scanner.close();
  }

  static private void generateKeysForAlgorithm(String algoName, String algoValue,
      int keySize, SecureRandom random,
      boolean generateSeed) {
    byte[] key = new byte[keySize];
    random.nextBytes(key);

    System.out.println("\n========================================");
    System.out.println("  " + algoName);
    System.out.println("========================================");
    System.out.println("\nAdd these lines to config.properties:\n");

    System.out.println("# " + algoName + " Configuration");
    System.out.println("encryption.algorithm=" + algoValue);
    System.out.println("encryption.key=" + bytesToHex(key));

    if (generateSeed) {
      byte[] seed = new byte[SEED_SIZE];
      random.nextBytes(seed);
      System.out.println("encryption.seed=" + bytesToHex(seed));
    } else {
      System.out.println("encryption.seed=");
    }

    System.out.println("\nKey size: " + (keySize * 8) + " bits");
    System.out.println("Key (hex): " + bytesToHex(key));
    if (generateSeed) {
      byte[] seed = new byte[SEED_SIZE];
      random.nextBytes(seed);
      System.out.println("Seed (hex): " + bytesToHex(seed));
    }
  }

  static private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
