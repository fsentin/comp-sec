import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

/**
 * This class contains all implementation details.
 * @author Fani
 *
 */
public class Service {
	
	/**
	 * Checks if the user with the specified username exists.
	 * @param username username which is checked if exists
	 * @return true if the user exists, false otherwise.
	 */
	public static boolean userExists(String username) throws IOException {
		Map<String, String> map = Utils.readFile();
		return map.containsKey(username);
	}
	
	/**
	 * Adds a new user to the system.
	 * @param username the specified user's name
	 * @param password character array representing the password of the user
	 * @return
	 */
	public static void addUser(String username, char[] password) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		byte[] salt = Crypto.getRandom(16);
		byte[] hash = Crypto.getHash(password, salt);
		byte[] store = Utils.connect("F".getBytes(), salt, hash);
		
		Map<String, String> map = Utils.readFile();
		map.put(username,  Base64.getEncoder().encodeToString(store));
		Utils.write(map);
	}
	
	/**
	 * Marks the user such that when he logs in next time he is required to change his password.
	 * @param username the specified user's name
	 */
	public static void forcePassword(String username) throws IOException {
		Map<String, String> map = Utils.readFile();
		byte[] value = Base64.getDecoder().decode(map.get(username));
		
		byte[] salt	= new byte[16];
		byte[] change = new byte[1];
		ByteBuffer buff = ByteBuffer.wrap(value);
        buff.get(change);
        buff.get(salt);
        byte[] stored = new byte[buff.remaining()];
        buff.get(stored);
        
        change = "T".getBytes();
        
		byte[] store = Utils.connect(change, salt, stored);
		map.put(username, Base64.getEncoder().encodeToString(store));
		Utils.write(map);
	}
	
	/**
	 * Checks whether the specified user should change his password.
	 * @param username the specified user's name
	 * @return true if the user must change the password, false otherwise.
	 */
	public static boolean forceCheck(String username) throws IOException {
		Map<String, String> map = Utils.readFile();
		byte[] value = Base64.getDecoder().decode(map.get(username));
		byte[] change = new byte[1];
		ByteBuffer.wrap(value).get(change);
        return new String(change).equals("T") ? true : false;
	}
	
	/**
	 * Removes the specified user from the system.
	 * @param username the specified user's name
	 */
	public static void removeUser(String username) throws IOException {
		Map<String, String> map = Utils.readFile();
		map.remove(username);
		Utils.write(map);
	}
	
	/**
	 * Changes the password of the specified user.
	 * @param username the specified user's name
	 * @param oldpass password before change
	 * @param newpass password after change if successful
	 * @return true if the change is succesful, false otherwise.
	 */
	public static boolean changePassword(String username, char[] oldpass, char[] newpass) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		if(Arrays.equals(oldpass, newpass)) return false;
		
		addUser(username, newpass);
		return true;
	}
	
	/**
	 * Checks if the given password matches the stored password.
	 * @param username the specified user's name
	 * @param entered character array representing the password entered by the user
	 * @return true if the entered array is the same as the stored password, false otherwise.
	 */
	public static boolean checkHash(String username, char[] entered) 
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Map<String, String> map = Utils.readFile();
		byte[] value = Base64.getDecoder().decode(map.get(username));
		
		byte[] change = new byte[1];
		byte[] salt	= new byte[16];
		byte[] storedHash;
		
		ByteBuffer buff = ByteBuffer.wrap(value);
        buff.get(change) 
            .get(salt);
        storedHash = new byte[buff.remaining()];
        buff.get(storedHash);
        
        byte[] calculatedHash = Crypto.getHash(entered, salt);
      
		return Arrays.equals(calculatedHash, storedHash);
	}
}