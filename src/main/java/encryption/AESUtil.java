package encryption;

import network.Server;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AESUtil {

    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey AESkey = keyGenerator.generateKey();
        return AESkey;
    }

    public static byte[] encrypt(String text, SecretKey AESkey) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, AESkey);
        return cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
    }

    public static String decrypt(byte[] encryptedText, SecretKey AESkey) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, AESkey);
        byte[] decrypted = cipher.doFinal(encryptedText);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    //TODO: No se para que es esto
    /*
    public static byte[] getKeyBytes(SecretKey key) {
        return key.getEncoded();
    }

    public static SecretKey restoreKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }
     */
}
