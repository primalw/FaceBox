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

public class FBConsoleChatApp {
 
   public static final String FB_XMPP_HOST = "chat.facebook.com";
   public static final int FB_XMPP_PORT = 5222;
 
   private ConnectionConfiguration config;
   private XMPPConnection connection;
   private BidiMap friends = new DualHashBidiMap();
   private FBMessageListener fbml;
   public BasicDHExample dh;
   
   private String friendKey = null;
   
   private BigInteger secret;
   private BigInteger A;
   private BigInteger B;
   private Random rand;
    
    public static byte[] aShared = null;
    public static String aSharedS = null;
    public static PublicKey pk;
    
    public  PublicKey pkiPeerB = null;
    public  PublicKey pkiPeerC = null;
    public  PublicKey pkiPeerAC = null; //interm key to send
    public  PublicKey pkiPeerCB = null; //interm key for receiving
    public  PublicKey pkiPeerBA = null;
    
    public Boolean iAmSender3P = false;
	public Boolean iAmSender2P = false;
	
	public FBConsoleChatApp(String username, String password) {
		try {
			connect();
			
			if (!login(username, password)) {
				System.err.println("Access Denied...");
				System.exit(-2);
			}
			
			initialSetup();	
			getFriends();	
		}
		catch (Exception ex) {
			System.out.println("FBChatSender error : "+ex.toString());
		}
		
	}
 
   public String connect() throws XMPPException {
   
	  System.out.println("Connecting ..."+FB_XMPP_PORT);
	  
	  // Setting up the security environment
      config = new ConnectionConfiguration(FB_XMPP_HOST, FB_XMPP_PORT);	  
      SASLAuthentication.registerSASLMechanism("DIGEST-MD5"
							,CustomSASLDigestMD5Mechanism.class);							
      config.setSASLAuthenticationEnabled(true);
      config.setDebuggerEnabled(false);
      connection = new XMPPConnection(config);
      connection.connect();   
	  
	  // Instanctiating other two classes
      dh = new BasicDHExample();
      fbml = new FBMessageListener(connection, this, dh, Thread.currentThread());
	  
      return connection.getConnectionID();
   }
 
   public void disconnect() {
      if ((connection != null) && (connection.isConnected())) {
         Presence presence = new Presence(Presence.Type.unavailable);
         presence.setStatus("offline");
         connection.disconnect(presence);
      }
   }
 
	public boolean login(String userName, String password) throws XMPPException {

		if ((connection != null) && (connection.isConnected())) {
			connection.login(userName, password);
			return true;
		}

		return false;
	}
   
   public void initialSetup() {
      dh = new BasicDHExample();
	  pk = dh.dhInit();
   }
   
   
   public void getFriends() {
      if ((connection != null) && (connection.isConnected())) {
         Roster roster = connection.getRoster();
         int i = 1;
         for (RosterEntry entry : roster.getEntries()) {
            Presence presence = roster.getPresence(entry.getUser());
            if ((presence != null) 
               && (presence.getType() != Presence.Type.unavailable)) {
               friends.put("#" + i, entry);
               System.out.println(entry.getName() + "(#" + i + ")");
               i++;
            }
         }
         fbml.setFriends(friends);
      }
   }
   
   public String getShareBi() {
	   try {
		   // 2 parties
		   //PublicKey pkiPeer = (PublicKey) Base64Coder.fromString(fbml.retrieveFirstMessage());
		   //System.out.println("PEER KEY" + pkiPeer.toString());
		   aSharedS = dh.getPeerKey(pkiPeerB);
		   //System.out.println("2 party SHARED KEY " + pkiShared);
		   //System.out.println("MY public KEY" + Base64Coder.toString(pk));
		   return aSharedS;
	   }
	   catch (Exception e) {
		   System.out.println("Get Shared : "+e.toString());
		   return null;
	   }
   }
   
   public void sendMessage(String text, String key) throws XMPPException {
   	  sendMessage((RosterEntry) friends.get(key), text);
   }
 
   public void sendMessage(final RosterEntry friend, String text) 
     throws XMPPException {
      if ((connection != null) && (connection.isConnected())) {
         ChatManager chatManager = connection.getChatManager();
         Chat chat = chatManager.createChat(friend.getUser(), fbml);
         chat.sendMessage(text);
         System.out.println("Your message has been sent to "
            + friend.getName());
      }
   }

   public void sendECDHkey(String text, String key) throws XMPPException {
   	  sendECDHkey((RosterEntry) friends.get(key), text);
   }
        
    public String sendECDHkey2P(String sKey) throws XMPPException, IOException {

	  iAmSender2P = true;
	  friendKey = sKey;
	  
	  TKey key = new TKey();
	  key.type = 1;
	  key.obj = pk;
	  
      sendECDHkey((RosterEntry) friends.get(friendKey), Base64Coder.toString(key) );
	  
	  try {
		  Thread.sleep(Long.MAX_VALUE);
	  }
	  catch (Exception ex) {
	  }
	  
	  return getShareBi();
      
	}
				      
    public void sendECDHkey(final RosterEntry friend, String text)  throws XMPPException {
      if ((connection != null) && (connection.isConnected())) {
         ChatManager chatManager = connection.getChatManager();
         Chat chat = chatManager.createChat(friend.getUser(), fbml);
         chat.sendMessage(text);
         System.out.println("Your Public Key has been sent to "
            + friend.getName());
      }
   }
   
}