import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class of static functions used for cryptographic purposes in the password manager.
 * 
 * Inspiration for code found at <a>https://mkyong.com/</a>.
 * 
 * @author Fani
 *
 */
public class Utils {
	
	/**
	 * Returns a new random vector.
	 * @param num number of bytes in the generated byte array
	 * @return random byte array of a given size.
	 */
	private static byte[] getRandom(int num) {
        byte[] rand = new byte[num];
        new SecureRandom().nextBytes(rand);
        return rand;
    }
	
	/**
	 * Returns a derived key from the given password and salt.
	 * @param password the master password
	 * @param salt random 12 byte array representing the initial vector
	 * @return derived key for encryption.
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
    private static SecretKey getDerivedPass(char[] password, byte[] salt) 
    		throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); 
        KeySpec spec = new PBEKeySpec(password, salt, 131072, 256); 
        SecretKey derived = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return derived;
    }
    
    /**
     * Encrypts the given plain text using the given master password. 
     * Uses PBKDF2, HMAC, SHA256, AES GCM with no padding.
     * @param plainText data which needs to be protected
     * @param master the master password of the password manager
     * @return byte array with an initial vector, salt vector and encrypted data
     */
    private static byte[] encrypt(String master, byte[] plainText) 
    		throws NoSuchAlgorithmException, NoSuchPaddingException, 
    		InvalidKeyException, InvalidAlgorithmParameterException, 
    		InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {
    	byte[] salt = getRandom(16);
    	byte[] iv = getRandom(12);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getDerivedPass(master.toCharArray(), salt), new GCMParameterSpec(128, iv));
        byte[] cipherText = cipher.doFinal(plainText);

        byte[] result = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
        		.put(salt)
        		.put(iv)
                .put(cipherText)
                .array();

        return result;
    }
    
    /**
     * Decrypts the given cipher byte array with vectors iv and salt (not encrypted).
     * @param master the master password of the password manager
     * @param cipherTextWithVectors byte array containing cipher text, initial vector and salt
     * @return byte array of plain text 
     */
    private static byte[] decrypt(String master, byte[] cipherTextWithVectors) 
    		throws NoSuchAlgorithmException, NoSuchPaddingException,
    		InvalidKeyException, InvalidAlgorithmParameterException,
    		InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException  {
    	byte[] salt = new byte[16];
    	byte[] iv = new byte[12];
    	
        ByteBuffer buff = ByteBuffer.wrap(cipherTextWithVectors);
        buff.get(salt);
        buff.get(iv);

        byte[] cipherText = new byte[buff.remaining()];
        buff.get(cipherText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getDerivedPass(master.toCharArray(), salt), new GCMParameterSpec(128, iv));

        byte[] plainText = cipher.doFinal(cipherText);

        return plainText;
    }
    
    /**
     * Saves the encrypted version of plainText to a file specified by path.
     * @param master the master password of the password manager
     * @param path the path of the file decrypted
     * @param plainText byte array of data which is saved to a file specified by path
     */
    public static void saveEncryptedFile(String master, Path path, byte[] plainText) 
    		throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
    		InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, 
    		BadPaddingException, IOException {
    	Files.write(path, Utils.encrypt(master, plainText));
    }
    
    /**
     * Decrypts the file given by path with given master password.
     * @param path the path of the file decrypted
     * @param master the master password of the password manager
     * @return byte array representing the decrypted data
     */
    public static byte[] decryptFile(String master, Path path) 
    		throws IOException, InvalidKeyException, NoSuchAlgorithmException, 
    		NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, 
    		IllegalBlockSizeException, BadPaddingException {
        byte[] fileContent = Files.readAllBytes(path);
        return decrypt(master, fileContent);
    }
}