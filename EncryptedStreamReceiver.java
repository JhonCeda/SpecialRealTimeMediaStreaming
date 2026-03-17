
/**
 * Encrypted stream receiver and decryption utility
 * Receives encrypted UDP packets, decrypts them, and saves to file
 * Can be used to verify encrypted stream integrity
 */

import java.io.*;
import java.net.*;

class EncryptedStreamReceiver {

  static public void main(String[] args) throws Exception {
    if (args.length < 2 || args.length > 3) {
      System.out.println("Usage: java EncryptedStreamReceiver <listen-address:port> <output-file> [config-file]");
      System.out.println("Example: java EncryptedStreamReceiver localhost:7777 output.bin config.properties");
      System.exit(-1);
    }

    String listeningAddress = args[0];
    String outputFile = args[1];
    String configFile = args.length == 3 ? args[2] : "config.properties";

    // Parse listening address
    String[] parts = listeningAddress.split(":");
    String host = parts[0];
    int port = Integer.parseInt(parts[1]);

    // Load encryption (optional)
    StreamEncryption encryption = null;
    try {
      encryption = EncryptionFactory.createFromConfig(configFile);
    } catch (Exception e) {
      System.err.println("Warning: Could not load encryption from " + configFile);
      System.err.println("Will receive packets without decryption...");
    }

    // Create listening socket
    DatagramSocket socket = new DatagramSocket(new InetSocketAddress(host, port));
    System.out.println("Listening on " + host + ":" + port);

    // Output stream
    DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));

    byte[] buffer = new byte[65535]; // Max UDP packet size
    long packetCount = 0;
    long byteCount = 0;
    long decryptedByteCount = 0;

    System.out.println("Receiving packets... (Press Ctrl+C to stop)");

    try {
      while (true) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        byte[] data = packet.getData();
        int length = packet.getLength();

        System.out.print(".");

        // Decrypt if encryption is enabled
        if (encryption != null) {
          try {
            byte[] ciphertext = new byte[length];
            System.arraycopy(data, 0, ciphertext, 0, length);
            byte[] plaintext = encryption.decrypt(ciphertext);

            out.writeShort(plaintext.length);
            out.write(plaintext);

            decryptedByteCount += plaintext.length;
          } catch (Exception e) {
            System.err.println("\nDecryption error on packet " + packetCount + ": " + e.getMessage());
            // Continue to next packet
            continue;
          }
        } else {
          // No decryption, just write raw
          out.writeShort(length);
          out.write(data, 0, length);
        }

        byteCount += length;
        packetCount++;
      }
    } catch (Exception e) {
      System.out.println();
      System.out.println("Stopped: " + e.getMessage());
    } finally {
      out.close();
      socket.close();

      System.out.println("\n=== Statistics ===");
      System.out.println("Packets received: " + packetCount);
      System.out.println("Encrypted bytes: " + byteCount);
      if (encryption != null) {
        System.out.println("Decrypted bytes: " + decryptedByteCount);
      }
      System.out.println("Output file: " + outputFile);
    }
  }
}
