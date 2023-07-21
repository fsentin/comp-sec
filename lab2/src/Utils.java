import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains useful methods and constants used all over the system.
 * @author Fani
 *
 */
public class Utils {
	
	/** File location .**/
	public static Path PATH = Paths.get("data.txt");
	
	/** Message for improper commands. **/
	public static String IMPROPER =  "the entered command is improper.";
	
	/** Message for errors during execution. **/
	public static String FAULT = "something went wrong.";
	
	/** Message for failed password input. **/
	public static String PASSFAIL = "failed. Password mismatch detected.";
	
	public static String MATCHESOLD = "you can't reuse the old password.";
	
	/** Message for too simple password. **/
	public static String PASSCOMPLEX = "your password is not complex enough. Make sure it meets these requirements:\n"
			+ " > at least 8 characters long\n"
			+ " > contains one or more digits\n"
			+ " > contains one or more lower case letters\n"
			+ " > contains one or more upper case letters\n"
			+ "Try again.";
	
	/** Message for unsuccessful login. **/
	public static String LOGINFAIL = "username or password is incorrect.";
	
	/**
	 * Connects three byte arrays to one.
	 * @return combination of the given byte arrays.
	 */
	public static byte[] connect(byte[] first, byte[] second, byte[] third) {
		byte[] allByteArray = new byte[first.length + second.length + third.length];

		ByteBuffer buff = ByteBuffer.wrap(allByteArray);
		buff.put(first)
		    .put(second)
		    .put(third);

		return buff.array();
	}
	
	/**
	 * Maps the given byte array to a hash map.
	 * @param bytes byte array which is mapped to a hash map
	 * @return a hash map of url, password pairs.
	 * @throws IOException
	 */
	public static Map<String, String> map(byte[] bytes) throws IOException {
		Map<String, String> map = new HashMap<>();
		String line;
	    	
	   	BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
	   	while((line = reader.readLine()) != null) {
	   		String[] parts = line.split(" ", 2);
	   		if(parts.length >= 2){
	   			String key = parts[0];
	            String value = parts[1];
	            map.put(key, value);
	         }
	     }
	    reader.close();
	    return map;
	}
		
	/**
	 * Unmaps the plain text into the regular form.
	 * (entries devided by newline, url and password divided by space)
	 * @param map a map of url password pairs.
	 * @return byte array representing the plain text.
	 */
	public static byte[] unmap(Map<String, String> map) {
	    StringBuilder sb = new StringBuilder();
	    for(Map.Entry<String, String> entry : map.entrySet()) {
	    	sb.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
	    }
	    return sb.toString().getBytes();
	}
	
	/**
	 * Reads the specified file and maps it.
	 * @param path the path of the desired file
	 * @return map of user entries.
	 */
	public static Map<String, String> readFile() throws IOException {
		if(!Files.exists(PATH)) Files.createFile(PATH);
		return map(Files.readAllBytes(PATH));
	}
	
	/**
	 * Writes the map to the specified file.
	 * @param path the path of the desired file
	 * @param map content of the new file version
	 */
	public static void write(Map<String, String> map) throws IOException {
		Files.write(PATH, unmap(map));
	}
	
	/**
	 * Reads the new password from the system console.
	 * @return the password if succefully entered or null if not entered correctly.
	 */
	public static char[] readNewPassword() {
		Console c = System.console();
		char[] pass = c.readPassword("Password: ");
		char[] rtpass = c.readPassword("Repeat password: ");
		return Arrays.equals(rtpass, pass) ? pass : null;
	}
	
	/**
	 * Reads the password from the system console.
	 * @return the entered password.
	 */
	public static char[] readPassword() {
		Console c = System.console();
		char[] pass = c.readPassword("Password: ");
		return pass;
	}
}