
/**
 * Stream Decryption Utility
 * Decrypts previously-captured encrypted stream files
 * Useful for offline analysis and testing
 */

import java.io.*;

class StreamDecryptor {

  static public void main(String[] args) throws Exception {
    if (args.length < 2 || args.length > 3) {
      System.out.println("Usage: java StreamDecryptor <input-encrypted-file> <output-decrypted-file> [config-file]");
      System.out.println("Example: java StreamDecryptor encrypted.bin decrypted.bin config.properties");
      System.exit(-1);
    }

    String inputFile = args[0];
    String outputFile = args[1];
    String configFile = args.length == 3 ? args[2] : "config.properties";

    // Check input file exists
    if (!new File(inputFile).exists()) {
      System.err.println("Error: Input file not found: " + inputFile);
      System.exit(-1);
    }

    // Load encryption configuration
    StreamEncryption encryption = null;
    try {
      encryption = EncryptionFactory.createFromConfig(configFile);
    } catch (Exception e) {
      System.err.println("Error: Could not load encryption from " + configFile);
      System.err.println(e.getMessage());
      System.exit(-1);
    }

    if (encryption == null) {
      System.err.println("Error: Encryption not configured in " + configFile);
      System.exit(-1);
    }

    // Decrypt stream
    decryptFile(inputFile, outputFile, encryption);
  }

  static private void decryptFile(String inputPath, String outputPath, StreamEncryption encryption) throws Exception {
    DataInputStream in = new DataInputStream(new FileInputStream(inputPath));
    DataOutputStream out = new DataOutputStream(new FileOutputStream(outputPath));

    long packetCount = 0;
    long encryptedBytes = 0;
    long decryptedBytes = 0;
    long errorCount = 0;

    System.out.println("Decrypting " + inputPath + " -> " + outputPath);
    System.out.println("Using: " + encryption.getAlgorithmName());
    System.out.println();

    try {
      while (true) {
        try {
          // Read packet length
          short length = in.readShort();

          // Read encrypted packet
          byte[] encryptedData = new byte[length];
          in.readFully(encryptedData);

          encryptedBytes += length;
          packetCount++;

          try {
            // Decrypt
            byte[] plaintext = encryption.decrypt(encryptedData);
            decryptedBytes += plaintext.length;

            // Write to output
            out.writeShort(plaintext.length);
            out.write(plaintext);

            System.out.print(".");
          } catch (Exception e) {
            System.out.print("E");
            errorCount++;
            System.err.println("\nError decrypting packet " + packetCount + ": " + e.getMessage());
          }

        } catch (EOFException e) {
          // End of file, normal termination
          break;
        }
      }
    } finally {
      in.close();
      out.close();
    }

    System.out.println();
    System.out.println();
    System.out.println("=== Decryption Statistics ===");
    System.out.println("Packets processed: " + packetCount);
    System.out.println("Encrypted bytes: " + encryptedBytes);
    System.out.println("Decrypted bytes: " + decryptedBytes);
    System.out.println("Errors: " + errorCount);
    System.out.println("Success rate: " + (100 * (packetCount - errorCount) / packetCount) + "%");
    System.out.println("Compression ratio: " + (100.0 * decryptedBytes / encryptedBytes) + "%");
    System.out.println();
    System.out.println("Output file: " + outputPath);

    if (errorCount > 0) {
      System.err.println("\nWarning: " + errorCount + " packets failed to decrypt!");
    }
  }
}
