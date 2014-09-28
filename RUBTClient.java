import java.io.*;
import java.net.*;
import java.net.URL.*;


public class RUBTClient {

	public static void main(String[] args) throws UnknownHostException, IOException, NullPointerException {
	
		if (args.length != 2)
		{
			System.out.println("Correct Usage: RUBTClient <.torrent file name> <ouptut file name>");
			return;
		}
		
		String inFileName = args[0];
		String outFileName = args[1];
		
		try {
			File torrentFile = new File(inFileName);
			if (!torrentFile.exists()){
				System.err.println("Torrent file does not exist");
				return;
			}
		} catch (NullPointerException e)
		{
			System.err.println("torrent file cannot be null");
			return;
		}
		
	}

}
