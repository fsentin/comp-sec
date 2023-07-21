import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * This class is used by the users to login to the system.
 * @author Fani
 *
 */
public class Login {
	
	/**
	 * Execution class.
	 * @param args given commands and arguments
	 */
	public static void main(String[] args) {
		if(args.length != 1) error(Utils.IMPROPER);
		
		try {
			login(args[0]);
		} catch (RuntimeException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			error(Utils.FAULT);
		} 
	}
	
	/**
	 * Logs the user into the system if correct password is entered. 
	 * If a password change was requested by admin, the user needs to change it.
	 * @param username 
	 */
	private static void login(String username) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		char[] oldpass =  Utils.readPassword();
		if(oldpass == null) error(Utils.PASSFAIL);
		
		if(!Service.checkHash(username, oldpass)) {
			error(Utils.LOGINFAIL);
		}

		if(Service.forceCheck(username)) {
			
			System.out.println("Enter new password!");
			char[] newpass =  Utils.readNewPassword();
			
			// if repeated password isn't the same
			if(newpass == null) error(Utils.PASSFAIL);
			
			// if new password is not complex enough
			if(!Crypto.complexEnough(newpass)) error(Utils.PASSCOMPLEX);
			
			// if new password is the same as old one
			if(!Service.changePassword(username, oldpass, newpass)) error(Utils.MATCHESOLD);
			Crypto.clean(newpass);
		}
		
		Crypto.clean(oldpass);
		System.out.println("Success!");
		System.exit(0);
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