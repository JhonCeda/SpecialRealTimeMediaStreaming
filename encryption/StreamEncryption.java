/**
 * Interface for different stream encryption algorithms
 * Supports AES-GCM, ChaCha20-Poly1305, and DPRG-based stream cipher
 */

public interface StreamEncryption {

  /**
   * Initialize the cipher with configuration
   * 
   * @param config EncryptionConfig containing key, seed, and other parameters
   */
  void initialize(EncryptionConfig config) throws Exception;

  /**
   * Encrypt a frame/packet
   * 
   * @param plaintext input data to encrypt
   * @return encrypted data (includes IV/nonce when needed)
   */
  byte[] encrypt(byte[] plaintext) throws Exception;

  /**
   * Decrypt a frame/packet
   * 
   * @param ciphertext encrypted data
   * @return decrypted plaintext
   */
  byte[] decrypt(byte[] ciphertext) throws Exception;

  /**
   * Get the name of this encryption algorithm
   */
  String getAlgorithmName();

  /**
   * Get header bytes that should be prepended to encrypted packets
   * (useful for indicating algorithm and format version)
   */
  byte[] getHeader();
}
