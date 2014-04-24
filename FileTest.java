import java.io.*;

public class FileTest {

	public static void main(String args[]) {
		try {
			FileOps fOp = new FileOps();
			String[] ret = fOp.shard(new File("magnum-opus.txt"));
			fOp.merge(ret, "merge.txt");
		}
		catch (Exception ex) {
			System.out.println("Error Main : "+ex.toString());
		}
	}
	
}