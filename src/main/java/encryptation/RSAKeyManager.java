package encryptation;

import java.io.*;
import java.security.*;

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

    /**
     * Generates the RSA key pair of the administrator: both a public and a private key. The library used is
     * {@link java.security}.
     *
     * @return              the KeyPair containing both the private and the public key
     * @throws Exception
     *
     * @see KeyPair
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048); //The generated key will have a size of 2048 bits
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }

    //Storing the key pair in memory is not always a good option. Mostly, the keys will stay unchanges for a
    //long time, so it is convenient to store them in files

    /**
     * Saves a single key into the desired file from the generated Key Pair.
     *
     * @param pair
     * @param filename
     */
    public static void saveKey (KeyPair pair, String filename){
        //Create a file object with reference to a file path
        File publicFile = new File(filename + "_public_key");
        File privateFile = new File (filename + "_private_key");

        if (publicFile.exists() || privateFile.exists()){
            System.out.println("The key files already exist. No overwriting.");
            return;
        }

        try (FileOutputStream publicOut = new FileOutputStream(publicFile);
             FileOutputStream privateOut = new FileOutputStream(privateFile); ){ //To automatically close resources at the end of the try block
            publicOut.write(pair.getPublic().getEncoded()); //writes the public key in bytes
            privateOut.write(pair.getPrivate().getEncoded()); //writes the private key in bytes

            System.out.println("Public and private keys saved successfully.");

        } catch (IOException e) {
            throw new RuntimeException("Error saving keys: "+e.getMessage(),e);
        }

    }

    public static PublicKey retrievePublicKey (String filename){
        try(FileInputStream publicIn = new FileInputStream(filename + "_public_key")){

        }catch(IOException e){
            throw new RuntimeException("Error retrieving public key: "+e.getMessage(), e);
        }
        return null;
    }

    public static PrivateKey retrievePrivateKey (String filename){
        return null;
    }






}
