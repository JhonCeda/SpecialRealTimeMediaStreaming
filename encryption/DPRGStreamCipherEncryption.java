
/**
 * DPRG-based Stream Cipher encryption implementation
 * Challenge 3: Stream cipher based on DPRG with XOR encryption
 * 
 * Uses HMAC-DRBG (Deterministic Random Bit Generator) as DPRG
 * Generates keystream and applies XOR to plaintext
 */

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class DPRGStreamCipherEncryption implements StreamEncryption {
  private static final String DPRG_ALGORITHM = "HmacSHA256";
  private static final int KEY_SIZE = 32; // 256 bits for SHA256-based DRBG
  private static final int BLOCK_SIZE = 32; // SHA256 output is 32 bytes

  private byte[] masterKey;
  private byte[] seed;
  private SecureRandom random;
  private long frameCounter; // Counter for reproducible keystream per frame

  @Override
  public void initialize(EncryptionConfig config) throws Exception {
    random = new SecureRandom();
    frameCounter = 0;

    byte[] keyBytes = config.getKey();
    if (keyBytes == null) {
      keyBytes = new byte[KEY_SIZE];
      if (config.getSeed() != null) {
        SecureRandom seededRandom = new SecureRandom(config.getSeed());
        seededRandom.nextBytes(keyBytes);
      } else {
        random.nextBytes(keyBytes);
      }
      config.setKey(keyBytes);
    }

    this.masterKey = keyBytes;
    this.seed = config.getSeed() != null ? config.getSeed() : new byte[0];
  }

  @Override
  public byte[] encrypt(byte[] plaintext) throws Exception {
    // Generate keystream using DPRG
    byte[] keystream = generateKeystream(plaintext.length, frameCounter);
    frameCounter++;

    // XOR plaintext with keystream
    byte[] ciphertext = new byte[plaintext.length];
    for (int i = 0; i < plaintext.length; i++) {
      ciphertext[i] = (byte) (plaintext[i] ^ keystream[i]);
    }

    // Prepend counter for decryption (allows out-of-order packet handling)
    byte[] result = new byte[8 + ciphertext.length];
    System.arraycopy(longToBytes(frameCounter - 1), 0, result, 0, 8);
    System.arraycopy(ciphertext, 0, result, 8, ciphertext.length);

    return result;
  }

  @Override
  public byte[] decrypt(byte[] ciphertext) throws Exception {
    // Extract counter
    long counter = bytesToLong(ciphertext, 0);

    // Extract actual ciphertext
    byte[] actualCiphertext = new byte[ciphertext.length - 8];
    System.arraycopy(ciphertext, 8, actualCiphertext, 0, actualCiphertext.length);

    // Regenerate keystream with the same counter
    byte[] keystream = generateKeystream(actualCiphertext.length, counter);

    // XOR ciphertext with keystream to recover plaintext
    byte[] plaintext = new byte[actualCiphertext.length];
    for (int i = 0; i < actualCiphertext.length; i++) {
      plaintext[i] = (byte) (actualCiphertext[i] ^ keystream[i]);
    }

    return plaintext;
  }

  /**
   * Generate keystream using HMAC-DRBG as DPRG
   * 
   * @param length  desired keystream length
   * @param counter frame/packet counter for unique keystream per packet
   * @return keystream of requested length
   */
  private byte[] generateKeystream(int length, long counter) throws Exception {
    Mac hmac = Mac.getInstance(DPRG_ALGORITHM);
    SecretKeySpec keySpec = new SecretKeySpec(masterKey, DPRG_ALGORITHM);

    byte[] keystream = new byte[length];
    byte[] state = seed.length > 0 ? seed.clone() : new byte[BLOCK_SIZE];

    int index = 0;
    while (index < length) {
      // Update state with counter to ensure different keystream per frame
      byte[] counterBytes = longToBytes(counter);
      state = hmacHash(keySpec, hmac, concatenate(state, counterBytes));

      int copyLength = Math.min(BLOCK_SIZE, length - index);
      System.arraycopy(state, 0, keystream, index, copyLength);
      index += copyLength;
    }

    return keystream;
  }

  /**
   * Compute HMAC hash
   */
  private byte[] hmacHash(SecretKeySpec keySpec, Mac hmac, byte[] input) throws Exception {
    hmac.init(keySpec);
    return hmac.doFinal(input);
  }

  /**
   * Concatenate two byte arrays
   */
  private byte[] concatenate(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  /**
   * Convert long to bytes (big-endian)
   */
  private byte[] longToBytes(long value) {
    return new byte[] {
        (byte) (value >> 56),
        (byte) (value >> 48),
        (byte) (value >> 40),
        (byte) (value >> 32),
        (byte) (value >> 24),
        (byte) (value >> 16),
        (byte) (value >> 8),
        (byte) (value)
    };
  }

  /**
   * Convert bytes to long (big-endian)
   */
  private long bytesToLong(byte[] bytes, int offset) {
    return ((long) (bytes[offset] & 0xFF) << 56)
        | ((long) (bytes[offset + 1] & 0xFF) << 48)
        | ((long) (bytes[offset + 2] & 0xFF) << 40)
        | ((long) (bytes[offset + 3] & 0xFF) << 32)
        | ((long) (bytes[offset + 4] & 0xFF) << 24)
        | ((long) (bytes[offset + 5] & 0xFF) << 16)
        | ((long) (bytes[offset + 6] & 0xFF) << 8)
        | ((long) (bytes[offset + 7] & 0xFF));
  }

  @Override
  public String getAlgorithmName() {
    return "DPRG-XOR-Stream";
  }

  @Override
  public byte[] getHeader() {
    return new byte[] { 0x03 }; // Magic byte for DPRG Stream Cipher
  }
}
