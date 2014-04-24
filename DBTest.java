// Include the Dropbox SDK.
import com.dropbox.core.*;
import java.io.*;
import java.util.Locale;
import java.net.*;
import java.nio.channels.*;
import java.nio.*;

public class DBTest {

	// Get your app key and secret from the Dropbox developers website.
	private final String APP_KEY = "ujl3c7k3btk828q";
	private final String APP_SECRET = "yp4y4ja9iy9omtu";
	
	private DbxClient client;
	
	public void DBInit () throws IOException, DbxException {
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        DbxRequestConfig config = new DbxRequestConfig("JavaTutorial/1.0",
													   Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
		
        // Have the user sign in and authorize your app.
        String authorizeUrl = webAuth.start();
        System.out.println("1. Go to: " + authorizeUrl);
        // System.out.println("2. Click \"Allow\" (you might have to log in first)");
        // System.out.println("3. Copy the authorization code.");
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
		
        // This will fail if the user enters an invalid authorization code.
        DbxAuthFinish authFinish = webAuth.finish(code);
        String accessToken = authFinish.accessToken;
		
		client = new DbxClient(config, accessToken);
		
        System.out.println("Linked account: " + client.getAccountInfo().displayName);
	}
	
	public boolean DBUpload(String src, String dst) throws IOException {
	
		FileInputStream inputStream = null;
		File inputFile = new File(src);
		
        try {
			inputStream = new FileInputStream(inputFile);
			System.out.println("Uploading ... ");
            DbxEntry.File uploadedFile = client.uploadFile("/"+dst,
														   DbxWriteMode.add(), inputFile.length(), inputStream);
            System.out.println("Uploaded: " + uploadedFile.toString());
			inputStream.close();
			return true;
		}
		catch (Exception ex) {
			System.out.println("Error in uploading " + ex.toString() );
            inputStream.close();
			return false;
        }			
	}
	
	public void DBDirList() throws DbxException  { 
	
		DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
        System.out.println("Files in the root path:");
		
        for (DbxEntry child : listing.children) {
            System.out.println("	" + child.name + ": " + child.toString());
        }
		
	}
	
	public boolean DBDownload( String src, String dst ) throws IOException {
		FileOutputStream outputStream = null;
        try {
			outputStream = new FileOutputStream(dst);
            DbxEntry.File downloadedFile = client.getFile("/"+src, null,
														  outputStream);
            System.out.println("Metadata: " + downloadedFile.toString());
			outputStream.close();
			return true;
        } catch (Exception ex) {
            System.out.println("Error in uploading " + ex.toString() );
			outputStream.close();
			return false;
        }
	}
	
	public String DBCreateLink(String path) {
		String link = null;
		
		try {
			link = client.createShareableUrl("/"+path);
		}
		catch (DbxException ex) {
			System.out.println("Error : Create Link " + ex.toString());
		}

		return link;
	}
	
	public boolean DBDownloadLink(String link, String dst) {
	
		boolean ret = false;

		try{
			URL download=new URL(link);
			ReadableByteChannel rbc=Channels.newChannel(download.openStream());
			FileOutputStream fileOut = new FileOutputStream(dst);
			fileOut.getChannel().transferFrom(rbc, 0, 1 << 24);
			fileOut.flush();
			fileOut.close();
			rbc.close();
			ret = true;
		}
		catch(Exception e) { 
			e.printStackTrace(); 
		}
		
		return ret;
	}
	
	public boolean DBDelete(String path) {
		
		try {
			client.delete("/"+path);
			return true;
		}
		catch (Exception ex) {
			System.out.println("Error : Delete " + ex.toString() );
			return false;
		}
	}

	public String DBUploadShare(String src, String dst) {
		
		try {
			DBUpload(src, dst);
			return DBCreateLink(dst);
		}
		catch (Exception ex) {
			System.out.println("Error UploadShare : " + ex.toString());
		}
	
		return null;
	}
	
    /*public static void main(String[] args) {	
		String sLink;
		DBTest dbproxy = new DBTest();
		try {
			ProxySecurity secproxy = new ProxySecurity();
			
			dbproxy.DBInit();
			secproxy.makeKey();

			secproxy.encrypt(new File("magnum-opus.txt") , new File("enc.txt") );
			System.out.println("Thank You Jesus");
			
			dbproxy.DBUpload("enc.txt", "magnum-opus.txt" );
			//dbproxy.DBDownload( "magnum-opus.txt", "working-draft-1.txt" );
			
			sLink = dbproxy.DBCreateLink("magnum-opus.txt");
			System.out.println(sLink);
			
			dbproxy.DBDownloadLink(sLink+"?dl=1", "dlink.txt");
			
			secproxy.decrypt(new File("dlink.txt") , new File("plain-out.txt") );
			
			dbproxy.DBDelete("magnum-opus.txt");
			//secproxy.decrypt(new File("enc.txt") , new File("enc-out.txt") );
		}
		catch ( Exception ex ) {
			System.out.println("Error : " + ex.toString() );
		}
    }*/
}