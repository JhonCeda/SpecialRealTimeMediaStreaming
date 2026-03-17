
/**
 * ChaCha20-Poly1305 encryption implementation
 * Challenge 2: Replace AES-GCM with ChaCha20-Poly1305
 * 
 * Note: Requires Java 11+ or Bouncy Castle library
 * For Java 11+, use: javax.crypto.Cipher with "ChaCha20-Poly1305"
 */

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Key;

public class ChaCha20Poly1305Encryption implements StreamEncryption {
  private static final String ALGORITHM = "ChaCha20-Poly1305";
  private static final int KEY_SIZE = 256; // 256 bits for ChaCha20
  private static final int NONCE_LENGTH = 12; // 96 bits for Poly1305

  private Key key;
  private SecureRandom random;
  private Cipher encryptCipher;
  private Cipher decryptCipher;

  @Override
  public void initialize(EncryptionConfig config) throws Exception {
    random = new SecureRandom();

    byte[] keyBytes = config.getKey();
    if (keyBytes == null) {
      // Generate key from config or use seed
      KeyGenerator keyGen = KeyGenerator.getInstance("ChaCha20");
      if (config.getSeed() != null) {
        SecureRandom seededRandom = new SecureRandom(config.getSeed());
        keyGen.init(KEY_SIZE, seededRandom);
      } else {
        keyGen.init(KEY_SIZE);
      }
      Key tempKey = keyGen.generateKey();
      keyBytes = tempKey.getEncoded();
      config.setKey(keyBytes);
    }

    key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "ChaCha20");

    encryptCipher = Cipher.getInstance(ALGORITHM);
    decryptCipher = Cipher.getInstance(ALGORITHM);
  }

  @Override
  public byte[] encrypt(byte[] plaintext) throws Exception {
    byte[] nonce = new byte[NONCE_LENGTH];
    random.nextBytes(nonce);

    IvParameterSpec ivSpec = new IvParameterSpec(nonce);
    encryptCipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

    byte[] ciphertext = encryptCipher.doFinal(plaintext);

    // Return nonce + ciphertext (nonce must be transmitted with ciphertext)
    byte[] result = new byte[NONCE_LENGTH + ciphertext.length];
    System.arraycopy(nonce, 0, result, 0, NONCE_LENGTH);
    System.arraycopy(ciphertext, 0, result, NONCE_LENGTH, ciphertext.length);

    return result;
  }

  @Override
  public byte[] decrypt(byte[] ciphertext) throws Exception {
    // Extract nonce and actual ciphertext
    byte[] nonce = new byte[NONCE_LENGTH];
    System.arraycopy(ciphertext, 0, nonce, 0, NONCE_LENGTH);

    byte[] actualCiphertext = new byte[ciphertext.length - NONCE_LENGTH];
    System.arraycopy(ciphertext, NONCE_LENGTH, actualCiphertext, 0, actualCiphertext.length);

    IvParameterSpec ivSpec = new IvParameterSpec(nonce);
    decryptCipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

    return decryptCipher.doFinal(actualCiphertext);
  }

  @Override
  public String getAlgorithmName() {
    return "ChaCha20-Poly1305";
  }

  @Override
  public byte[] getHeader() {
    return new byte[] { 0x02 }; // Magic byte for ChaCha20-Poly1305
  }
}
