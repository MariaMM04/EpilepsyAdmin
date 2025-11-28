package encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * Utility class that provides AES symmetric encryption and decryption using AES-GCM algorithm to ensure security
 * and authenticated encryption.
 * It handles the following parameters:
 * <ul>
 *     <li> Key generation with 128-bit strength</li>
 *     <li> Random IV generation</li>
 *     <li> Secure encryption and decryption</li>
 *     <li> Base64 encoding of output for transport over text-based protocols</li>
 * </ul>
 * The AES key must be securely exchanged between parties (encrypted via {@code RSAUtil} and {@code RSAManager}
 *
 * @author pblan
 */
public class TokenUtils {

    private static final int tag_length_bits = 128;
    private static final int iv_length_bytes = 12;


    public static SecretKey generateToken() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128, SecureRandom.getInstanceStrong());
        SecretKey AESkey = keyGenerator.generateKey();
        return AESkey;
    }


    /**
     * Encrypts the plain text using AES-GCM with a random IV. The output is encoded in Base64 containing
     * the IV + cipher text. This encryption allows the {@code decrypt} method to extract the IV for proper decryption
     *
     * @param text      The plain text to encrypt
     * @param AESkey    The shared AES secret key
     * @return          A Base64 encoded string that includes the IV and the ciphertext
     * @throws Exception    if encryption fails for any reason
     */
    public static String encrypt(String text, SecretKey AESkey) throws Exception{
        byte[] iv = new byte[iv_length_bytes];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(tag_length_bits,iv);
        cipher.init(Cipher.ENCRYPT_MODE,AESkey,spec);

        byte[] encryptedText = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        //Use the IV for the encryption
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedText.length);
        buffer.put(iv);
        buffer.put(encryptedText);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     *
     *
     * @param encryptedText     String that contains the encrypted data (previously encoded in Base64)
     * @param AESkey            The shared secret key used for AES-GCM decryption
     * @return
     * @throws Exception    for simplicity
     */
    public static String decrypt(String encryptedText, SecretKey AESkey) throws Exception{
        byte[] decoded = Base64.getDecoder().decode(encryptedText); //decodes the input string into bytes
        ByteBuffer buffer = ByteBuffer.wrap(decoded); //To easily read chunks

        byte[] iv = new byte[iv_length_bytes]; //allocates 12 bytes for the iv
        buffer.get(iv);

        byte[] restEncrypted = new byte[buffer.remaining()]; //message+tag
        buffer.get(restEncrypted);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(tag_length_bits, iv);
        cipher.init(Cipher.DECRYPT_MODE, AESkey,spec);

        byte[] decrypted = cipher.doFinal(restEncrypted);

        return new String(decrypted, StandardCharsets.UTF_8); //readable string
    }

}
