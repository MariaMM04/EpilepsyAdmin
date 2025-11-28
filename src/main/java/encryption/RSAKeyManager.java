package encryption;

import java.io.*;
import java.security.*;
import java.security.spec.*;

/**
 * This {@code RSAKeyManager} class manages the creation of the RSA pair keys for encryption and decryption
 * of passwords in the admin interface.
 * The algorithm used for encryption and decryption is RSA. The class defines:
 * <ul>
 *     <li> A filename that will contain the public key</li>
 *     <li> A filename that will contain the private key</li>
 * </ul>
 *
 *
 * @author pblan
 */
public class RSAKeyManager {
    private static final String publicKeyFile = "public_key"; //Static -> belongs to the class not the object
    private static final String privateKeyFile = "private_key";

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        generator.initialize(2048, secureRandom);
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }
}
