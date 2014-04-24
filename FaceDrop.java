import java.io.*;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
//import org.apache.commons.io.IOUtils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.math.BigInteger;
import java.util.*;
import java.io.*;
import java.security.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;


import javax.crypto.*;

class FaceDrop {

	public static void showMenu() {
		System.out.println("Please select one of the following menu.");
		System.out.println("1. Initiate key exchange for 2 Parties");
		System.out.println("2. Upload file");
		System.out.println("3. EXIT");
		System.out.print("Your choice [0-5]: ");
	}
	
	public static String readInput() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		return br.readLine();
	}

	public static void main(String[] args) {
	
		String friendKey = null;
		
		if (args.length == 0) {
			System.err.println("Usage: java FBConsoleChatApp [username_facebook] [password]");
			System.exit(-1);
		}

		String username = args[0];
		String password = args[1];

		FBConsoleChatApp app = new FBConsoleChatApp(username, password);

		try {
			FaceDrop.showMenu();
			String data = null;

			while((data = FaceDrop.readInput().trim()) != null) {
				if (!Character.isDigit(data.charAt(0))) {
					System.out.println("Invalid input.Only 1-3 is allowed !");
					FaceDrop.showMenu();
					continue;
				}
				int choice = Integer.parseInt(data);
				if ((choice != 0) && (choice != 1) && (choice != 2) && (choice != 3) && (choice != 4) && (choice != 5)) {
					System.out.println("Invalid input.Only 1-3 is allowed !");
					FaceDrop.showMenu();
					continue;
				}

				switch (choice) {
					case 1: 
							app.getFriends();							
							System.out.println("Type the key number of your friend (e.g. #1)");
							System.out.print("Your friend's Key Number: ");
							friendKey = readInput();						
							String sKey = app.sendECDHkey2P(friendKey);
							System.out.println("2 party SHARED KEY " + sKey);
							FaceDrop.showMenu();   							  
							break;
					case 2: app.sendECDHkey2P("");
							FaceDrop.showMenu();     
							break;    
					case 3: System.exit(0);
				}
			}
			app.disconnect();
		} 
		catch (XMPPException e) {
			if (e.getXMPPError() != null) {
				System.err.println("ERROR-CODE : " + e.getXMPPError().getCode());
				System.err.println("ERROR-CONDITION : " + e.getXMPPError().getCondition());
				System.err.println("ERROR-MESSAGE : " + e.getXMPPError().getMessage());
				System.err.println("ERROR-TYPE : " + e.getXMPPError().getType());
			}
			app.disconnect();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			app.disconnect();
		}
	}

}