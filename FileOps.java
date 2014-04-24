import java.io.*;
import java.util.Locale;
import java.net.*;
import java.nio.channels.*;
import java.nio.*;

class FileOps {

	private byte[] fileQueue;
	static final int CHUNK_SIZE = 100; // Number of bytes for each chunk
	private int fileSize = 0;
	private int currentIterator = 0;
	
	private ProxySecurity secproxy;
	private DBTest dbproxy;
	private String publicKey;
	
	private FBConsoleChatApp sender;
	
	public FileOps() {
		dbproxy = new DBTest();
		
		try {
			dbproxy.DBInit();
			secproxy = new ProxySecurity();			
		}
		catch (Exception ex) {
			System.out.println("FileOps Error: "+ex.toString());
		}

	}
	
	public void setKey(String key, FBConsoleChatApp app) {
		publicKey = key;
		secproxy.pKey = key;
		sender = app;
		
		try {
		secproxy.makeKey();
		}
		catch (Exception ex) {
			System.out.println("Make Key : "+ex.toString());
		}
	}
	
	public static byte[] toByte(String fileName) {
		
		File file = new File(fileName);		
		byte[] b = new byte[(int) file.length()];
		
		System.out.println("File Length : "+b.length);
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(b);
			/*for (int i = 0; i < b.length; i++) {
				System.out.print((char)b[i]);
			}*/
			return b;
		} catch (FileNotFoundException e) {
			System.out.println("FileOps : File Not Found.");
			e.printStackTrace();
		}
		catch (IOException e1) {
			System.out.println("FileOps : Error Reading The File.");
			e1.printStackTrace();
		}		
		return null;
	}
	
	public static void toFile(byte[] inArray, String path) {
			
		try {
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(inArray);
			fos.close();
		}
		catch(FileNotFoundException ex)   {
			System.out.println("FileOps : FileNotFoundException : " + ex);
		}
		catch(IOException ioe)  {
			System.out.println("FileOps : IOException : " + ioe);
		}		
		
	}
	
	public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }
	
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }
	
	public String[] shard(File in) throws Exception {
		int len = 0;
		int nShards = 0;
		String[] names = null;
		byte[] temp = new byte[FileOps.CHUNK_SIZE];
		int rem = 0;
		
		fileQueue = secproxy.encrypt(in);
		len = fileQueue.length;
		fileSize = fileQueue.length;
		
		nShards = len / FileOps.CHUNK_SIZE;
		nShards = ( len % FileOps.CHUNK_SIZE != 0 ) ? nShards + 1 : nShards + 0 ; 
		names = new String[nShards];
		
		for (int i = 0; i < nShards ; i++ ) {
			rem = ( len > ( i * FileOps.CHUNK_SIZE + FileOps.CHUNK_SIZE ) ) ? 
				FileOps.CHUNK_SIZE : len - ( (i-1) * FileOps.CHUNK_SIZE + FileOps.CHUNK_SIZE ) ;
			
			System.out.println(rem);
			System.arraycopy(fileQueue, i * FileOps.CHUNK_SIZE, temp, 0, rem);
			FileOps.toFile(temp,"temp.txt");
			names[i] = dbproxy.DBUploadShare("temp.txt", 
											  Integer.toString(i)+".txt");
			ControlPacket cp = new ControlPacket();
			cp.type = 1;
			cp.data = names[i];
			
			sender.sendMessage(names[i]);
			System.out.println(names[i]);
		}
		
		return names;
	}
	
	public void processMesg(String text) {
		try {
			System.out.println("Thank You Jesus");
			System.out.println(text);
			
			ControlPacket cp = (ControlPacket) Base64Coder.fromString(text);
			
			if ( cp.type == 1 ) {
					String dlink = text;
					dbproxy.DBDownloadLink(dlink+"?dl=1", "dlink.txt");
					secproxy.decrypt(new File("dlink.txt"), new File("clean.txt"));
			}
		}
		catch (Exception ex) {
			System.out.println("Error : "+ex.toString());
		}
	}
	
	public void merge(String[] names, String outpath) throws Exception {
		int len = 0;
		int nShards = names.length;
		byte[] temp;
		int rem = 0;
		
		fileQueue = new byte[fileSize];
		
		//nShards = len / FileOps.CHUNK_SIZE;
		//nShards = ( len % FileOps.CHUNK_SIZE != 0 ) ? nShards + 1 : nShards + 0 ; 
		//names = new String[nShards];
		
		for (int i = 0; i < nShards ; i++ ) {
			
			dbproxy.DBDownloadLink(names[i]+"?dl=1", "dlink.txt");
			
			temp = FileOps.toByte("dlink.txt");
			rem = ( ( fileSize - len ) > FileOps.CHUNK_SIZE ) ? 
			FileOps.CHUNK_SIZE : ( fileSize - len ) ;
			len += temp.length;
			
			System.out.println(rem);
			System.arraycopy(temp, 0, fileQueue, i * FileOps.CHUNK_SIZE, rem);
		}
		
		secproxy.decrypt(fileQueue, new File(outpath));
	}
	
}