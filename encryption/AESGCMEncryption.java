
/**
 * AES-GCM encryption implementation
 * Challenge 1: Secure streaming with AES-GCM
 */

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Key;

public class AESGCMEncryption implements StreamEncryption {
  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH = 128; // bits
  private static final int IV_LENGTH = 12; // 96 bits recommended for GCM
  private static final int KEY_SIZE = 256; // bits (can be 128, 192, 256)

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
      KeyGenerator keyGen = KeyGenerator.getInstance("AES");
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

    key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");

    encryptCipher = Cipher.getInstance(ALGORITHM);
    decryptCipher = Cipher.getInstance(ALGORITHM);
  }

  @Override
  public byte[] encrypt(byte[] plaintext) throws Exception {
    byte[] iv = new byte[IV_LENGTH];
    random.nextBytes(iv);

    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    encryptCipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

    byte[] ciphertext = encryptCipher.doFinal(plaintext);

    // Return IV + ciphertext (IV must be transmitted with ciphertext)
    byte[] result = new byte[IV_LENGTH + ciphertext.length];
    System.arraycopy(iv, 0, result, 0, IV_LENGTH);
    System.arraycopy(ciphertext, 0, result, IV_LENGTH, ciphertext.length);

    return result;
  }

  @Override
  public byte[] decrypt(byte[] ciphertext) throws Exception {
    // Extract IV and actual ciphertext
    byte[] iv = new byte[IV_LENGTH];
    System.arraycopy(ciphertext, 0, iv, 0, IV_LENGTH);

    byte[] actualCiphertext = new byte[ciphertext.length - IV_LENGTH];
    System.arraycopy(ciphertext, IV_LENGTH, actualCiphertext, 0, actualCiphertext.length);

    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    decryptCipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

    return decryptCipher.doFinal(actualCiphertext);
  }

  @Override
  public String getAlgorithmName() {
    return "AES-GCM";
  }

  @Override
  public byte[] getHeader() {
    return new byte[] { 0x01 }; // Magic byte for AES-GCM
  }
}
