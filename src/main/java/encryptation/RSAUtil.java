package encryptation;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class RSAUtil {

    /**
     * Encrypts the input password with the public key and returns the encrypted password.
     *
     * @param password      the non-encrypted password
     * @param publicKey     the public key
     * @return              the encrypted password as a String
     * @throws Exception    //TODO habria que crear excepciones
     */
    public static String encrypt (String password, PublicKey publicKey) throws Exception{
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedPassword = encryptCipher.doFinal(passwordBytes);
        return Base64.getEncoder().encodeToString(encryptedPassword);
    }

    /**
     * Decrypts the password and returns the decrypted password.
     *
     * @param password      the encrypted password
     * @param privateKey    the private key
     * @return              the decrypted password
     * @throws Exception    //TODO: habria que crear excepciones
     */
    public static String decrypt (String password, PrivateKey privateKey) throws Exception{
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encodedPassword = Base64.getDecoder().decode(password);
        byte[] passwordBytes = decryptCipher.doFinal(encodedPassword);
        String decryptedPassword = new String(passwordBytes,StandardCharsets.UTF_8);
        return decryptedPassword;
    }
}
