import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.MapIterator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.*;

public class FBMessageListener implements MessageListener, Runnable {
 
    private FBMessageListener fbml = this;
    private XMPPConnection conn;
    private BidiMap friends = null;
    private FBConsoleChatApp sender;
    private BasicDHExample dh;
	private FileOps fOps;
 	
 	public String FirstMessage;
 	public String SecondMessage;
 	public Boolean FirstMessageFlag;
 	public Boolean ThreePartyFlag;
 	public Boolean SenderFlag;
	
	private Thread senderThread;
	private Boolean DoneDH = false;
 	
    public FBMessageListener(XMPPConnection conn, FBConsoleChatApp snd, BasicDHExample dh,
							 Thread curr, FileOps fOp) {
		this.conn = conn;
		this.sender = snd;
		this.dh = dh;
		new Thread(this).start();
		FirstMessageFlag = true;
		//ThreePartyFlag = false;
		SenderFlag = false;
		senderThread = curr;
		fOps = fOp;
    }
 
    public void setFriends(BidiMap friends) {
		this.friends = friends;
    }
 
    public void processMessage(Chat chat, Message message) {
		System.out.println();
		MapIterator it = friends.mapIterator();
				
		if ( friends == null || !it.hasNext() ) {
			sender.getFriends();
			it = friends.mapIterator();
		}		
		
		String key = null;
		RosterEntry entry = null;
	
		while (it.hasNext()) {
			key = (String) it.next();
			entry = (RosterEntry) it.getValue();
			if (entry.getUser().equalsIgnoreCase(chat.getParticipant())) {
				break;
			}
		}
		
		try {				
				if ((message != null) && (message.getBody() != null)) {

					FirstMessage = message.getBody();
					System.out.println("You've got Message from " + entry.getName() + "(" + key + ") :" ); 
									   //+ Base64Coder.fromString(message.getBody()));
					
					if ( DoneDH ) {
						if ( !sender.iAmSender3P && !sender.iAmSender2P ) {
						
							if (FirstMessageFlag) {
								FirstMessageFlag = false;
								
								String val = Base64Coder.toString(sender.pk);
								sender.sendECDHkey(val, key);
								
								//Thread.sleep(3000);
								
								sender.pkiPeerB = (PublicKey) Base64Coder.fromString(FirstMessage);
								String sKey = sender.getShareBi();
								System.out.println("2 party SHARED KEY " + sKey);
								DoneDH = true;
								//sender.sendIntKeyes3P(key);
							}						
							else {
								sender.pkiPeerCB = (PublicKey) Base64Coder.fromString(message.getBody());
							}
						}
						else {
							sender.pkiPeerB = (PublicKey) Base64Coder.fromString(FirstMessage);
							DoneDH = true;
							senderThread.interrupt();
						}
					}
				}
		 }
		 catch(Exception ex) {
				System.out.println("Error Msg Recieved : "+ex.toString());
		 } 
	}
		
    
    public String retrieveFirstMessage() throws Exception
    {
        return FirstMessage;
    }
    
    public String retrieveSecondMessage() throws Exception
    {
        return SecondMessage;
    }
        	
    public void run() {
		conn.getChatManager().addChatListener(
			  new ChatManagerListener() {
				  public void chatCreated(Chat chat, boolean createdLocally) {
					  if (!createdLocally) {
						  chat.addMessageListener(fbml);
					  }
				  }
			  }
		);
    }
}