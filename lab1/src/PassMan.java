import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


/**
 * My Password Manager :)
 * @author Fani
 *
 */
public class PassMan {
	
	/** File location for all encrypted passwords.**/
	private static Path path = Paths.get("data.txt");
	
	
	/** Message for improper commands. **/
	private static String IMPROPER =  "the entered command is improper.";
	
	/** Message for non allowed commands. **/
	private static String NOT_ALLOWED =  "the entered command is not allowed.";
	
	/** Message for errors during execution. **/
	private static String FAULT = "something went wrong.";
	
	
	/**
	 * Execution class.
	 * @param args given commands and arguments
	 */
	public static void main(String[] args) {
		int len = args.length;
		
		if(len < 2) error(IMPROPER);
		
		try {
			switch(args[0]) {
			case "init":
				if(len != 2) error(IMPROPER);
				init(args[1]);
				break;
				
			case "put":
				if(len != 4) error(IMPROPER);
				put(args[1], args[2], args[3]);
				break;
				
			case "get":
				if(len != 3) error(IMPROPER);
				get(args[1], args[2]);
				break;
				
			default: error(IMPROPER);		
			}
		} catch (Exception e) {
			error(FAULT);
		}
	}
	
	/**
	 * Initializes the password manager with a master password.
	 * @param master the master password
	 * @throws Exception
	 */
	public static void init(String master) throws Exception  {
		// cannot initialize password manager twice:
		if(new File("data.txt").exists()) error(NOT_ALLOWED);
		
		// create file and write the first entry "hello world\n" - for the sake of conveniance
		Files.createFile(path);
		Utils.saveEncryptedFile(master, path, "hello world\n".getBytes(StandardCharsets.UTF_8));
		
		System.out.println("Password manager initialized.");
	}
	
	/**
	 * Saves new password in the password manager.
	 * @param master the master password of the password manager
	 * @param url specified address for which the password is saved
	 * @param password the password for the given url
	 * @throws Exception
	 */
	public static void put(String master, String url, String password) throws Exception {
		// exit if the password manager is not initialized
		checkExists();
		
		// decrypts the file and maps it to a hash map
		Map<String, String> mapOfPlainText = map(Utils.decryptFile(master, path));
		
		// saves the new entry
		mapOfPlainText.put(url, password);
		
		// encrypts the updated file and saves it
		Utils.saveEncryptedFile(master, path, unmap(mapOfPlainText));
		System.out.println("Stored password for " + url);
	}
	
	/**
	 * Returns the password for the given url.
	 * @param master the master password of the password manager
	 * @param url specified address for which the password is retrieved
	 * @throws Exception
	 */
	public static void get(String master, String url) throws Exception {
		// exit if the password manager is not initialized
		checkExists();
		
		// decrypts the file and maps it to a hash map
		Map<String, String> map = map(Utils.decryptFile(master, path));
		
		//retrieves the password for the given url
		String password = map.get(url);
		
		if(password != null) {
			System.out.println("Password for " + url + " is: " + password);
		} else {
			System.out.println("No such password.");
		}
	}
	
	/**
	 * Maps the given byte array to a hash map.
	 * @param bytes byte array which is mapped to a hash map
	 * @return a hash map of url, password pairs.
	 * @throws IOException
	 */
	private static Map<String, String> map(byte[] bytes) throws IOException {
		Map<String, String> map = new HashMap<>();
		String line;
    	
    	BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        while ((line = reader.readLine()) != null) {
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
	private static byte[] unmap(Map<String, String> map) {
    	StringBuilder sb = new StringBuilder();
    	for(Map.Entry<String, String> entry : map.entrySet()) {
    		sb.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
    	}
    	return sb.toString().getBytes();
    }
	
	/**
	 * Exits the program if the database of passwords doesn't exist. 
	 */
	private static void checkExists() {
		if(!new File("data.txt").exists()) 
			error(NOT_ALLOWED);
	}
	
	/**
	 * Exits the program with a fitting message.
	 * @param message description of the reason for exiting the program
	 */
	private static void error(String message) {
		System.out.println("Sorry, " + message);
		System.exit(1);
	}
}