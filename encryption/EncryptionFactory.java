
/**
 * Factory for creating and managing stream encryption implementations
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

public class EncryptionFactory {

  public enum EncryptionAlgorithm {
    AES_GCM("aes-gcm", AESGCMEncryption.class),
    CHACHA20_POLY1305("chacha20-poly1305", ChaCha20Poly1305Encryption.class),
    DPRG_STREAM("dprg-xor", DPRGStreamCipherEncryption.class),
    NONE("none", null);

    private final String configValue;
    private final Class<?> encryptionClass;

    EncryptionAlgorithm(String configValue, Class<?> encryptionClass) {
      this.configValue = configValue;
      this.encryptionClass = encryptionClass;
    }

    public String getConfigValue() {
      return configValue;
    }

    public Class<?> getEncryptionClass() {
      return encryptionClass;
    }

    public static EncryptionAlgorithm fromConfigValue(String value) {
      for (EncryptionAlgorithm algo : values()) {
        if (algo.configValue.equalsIgnoreCase(value)) {
          return algo;
        }
      }
      return NONE;
    }
  }

  /**
   * Create encryption instance from config file
   * 
   * @param configFile path to config file
   * @return StreamEncryption instance or null if encryption is disabled
   */
  public static StreamEncryption createFromConfig(String configFile) throws Exception {
    InputStream inputStream = new FileInputStream(configFile);
    if (inputStream == null) {
      System.err.println("Configuration file not found!");
      return null;
    }

    Properties properties = new Properties();
    properties.load(inputStream);
    inputStream.close();

    // Get encryption algorithm
    String encryptionAlgo = properties.getProperty("encryption.algorithm", "none").trim();
    EncryptionAlgorithm algo = EncryptionAlgorithm.fromConfigValue(encryptionAlgo);

    if (algo == EncryptionAlgorithm.NONE) {
      System.out.println("Encryption disabled (encryption.algorithm=none)");
      return null;
    }

    // Create encryption instance
    StreamEncryption encryption = createEncryption(algo);

    // Configure encryption
    EncryptionConfig config = new EncryptionConfig(algo.name());

    // Load or generate key
    String keyHex = properties.getProperty("encryption.key");
    if (keyHex != null && !keyHex.isEmpty()) {
      config.setKey(hexToBytes(keyHex));
    }

    // Load or generate seed
    String seedHex = properties.getProperty("encryption.seed");
    if (seedHex != null && !seedHex.isEmpty()) {
      config.setSeed(hexToBytes(seedHex));
    }

    // Initialize
    encryption.initialize(config);

    System.out.println("Encryption initialized: " + encryption.getAlgorithmName());
    if (keyHex != null && !keyHex.isEmpty()) {
      System.out.println("Using preconfigured key");
    }
    if (seedHex != null && !seedHex.isEmpty()) {
      System.out.println("Using preconfigured seed");
    }

    return encryption;
  }

  /**
   * Create encryption instance with specified algorithm
   */
  private static StreamEncryption createEncryption(EncryptionAlgorithm algo) throws Exception {
    if (algo.encryptionClass == null) {
      return null;
    }
    return (StreamEncryption) algo.encryptionClass.getDeclaredConstructor().newInstance();
  }

  /**
   * Convert hex string to bytes
   */
  private static byte[] hexToBytes(String hex) {
    int len = hex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
          + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
  }

  /**
   * Convert bytes to hex string
   */
  public static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
