import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * This class can only be used by the system admin.
 * @author Fani
 *
 */
public class UserMan {
	
	/**
	 * Execution method.
	 * @param args given commands and arguments
	 */
	public static void main(String[] args) {
		
		if(args.length != 2) error(Utils.IMPROPER);
		
		try {
			switch(args[0]) {
			case "add":
				add(args[1]);
				break;
				
			case "pswd":
				pswd(args[1]);
				break;
				
			case "force":
				force(args[1]);
				break;
				
			case "del":
				del(args[1]);
				break;
				
			default: error(Utils.IMPROPER);
			}
		} catch (RuntimeException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			e.printStackTrace();
			error(Utils.FAULT);
		} 
	}
	
	/**
	 * Adds the user with a password to the system if it doesn't already exist.
	 * @param username the username of the new user
	 */
	public static void add(String username) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		if(Service.userExists(username)) 
			error("this user already exists.");
		
		char[] password =  Utils.readNewPassword();
		// if repeated password isn't the same
		if(password == null) error(Utils.PASSFAIL);
		
		// if new password is not complex enough
		if(!Crypto.complexEnough(password)) error(Utils.PASSCOMPLEX);
		
		Service.addUser(username, password);
		
		Crypto.clean(password);
		System.out.println("Sucess!");
	}
	
	/**
	 * Changes the password of the specified user.
	 * @param username the username of the user whose password is changed.
	 */
	public static void pswd(String username) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		if(!Service.userExists(username)) 
			error("this user doesn't exist.");
		
		// NEW PASSWORD
		System.out.println("Enter new password!");
		char[] newpass =  Utils.readNewPassword();
		// if repeated password isn't the same
		if(newpass == null) error(Utils.PASSFAIL);
		
		// if new password is not complex enough
		if(!Crypto.complexEnough(newpass)) error(Utils.PASSCOMPLEX);
		
		// misleading funtion name haha
		Service.addUser(username, newpass);
		
		Crypto.clean(newpass);
		System.out.println("Sucess!");
	}
	
	/**
	 * Forces the specified user upon the next login to change his password.
	 * @param username the username of the user
	 */
	public static void force(String username) throws IOException {
		if(!Service.userExists(username)) error("this user doesn't exist.");
		
		Service.forcePassword(username);
		
		System.out.println("User will be requested to change password on next login.");
	}
	
	/**
	 * Deletes the specified user from the system. 
	 * @param username username of the user which is deleted
	 */
	public static void del(String username) throws IOException {
		if(!Service.userExists(username)) error("this user doesn't exist.");
		
		Service.removeUser(username);
		
		System.out.println("Sucess!");
	}
	
	/**
	 * Exits the program with a fitting message and status 1.
	 * @param message description of the reason for exiting the program
	 */
	private static void error(String message) {
		System.out.println("Sorry, " + message);
		System.exit(1);
	}
}