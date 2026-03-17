/**
 * Configuration object for encryption settings
 * Contains key, IV, seed, and other parameters
 */

public class EncryptionConfig {
  private String algorithmName;
  private byte[] key;
  private byte[] iv;
  private byte[] seed;
  private int keyLen;

  public EncryptionConfig(String algorithmName) {
    this.algorithmName = algorithmName;
  }

  // Getters and Setters
  public String getAlgorithmName() {
    return algorithmName;
  }

  public byte[] getKey() {
    return key;
  }

  public void setKey(byte[] key) {
    this.key = key;
  }

  public byte[] getIv() {
    return iv;
  }

  public void setIv(byte[] iv) {
    this.iv = iv;
  }

  public byte[] getSeed() {
    return seed;
  }

  public void setSeed(byte[] seed) {
    this.seed = seed;
  }

  public int getKeyLen() {
    return keyLen;
  }

  public void setKeyLen(int keyLen) {
    this.keyLen = keyLen;
  }
}
