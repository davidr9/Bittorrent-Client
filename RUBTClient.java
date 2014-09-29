import java.io.*;
import java.net.*;
import java.net.URL.*;


public class RUBTClient {

	public static void main(String[] args) throws UnknownHostException, IOException, NullPointerException {
	
		/*Error handling when user enters incorrect number of arguements*/
        if (args.length != 2)
		{
			System.out.println("Correct Usage: RUBTClient <.torrent file name> <ouptut file name>");
			return;
		}
		
		/*arguement 0 is the torrent and argument 1 is a file that will be sent to server*/
        String inFileName = args[0];
		String outFileName = args[1];
		
		
        /*opens the torrent file or throws an exception if file doesn't exist*/
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
		
	}/*end of main method*/

    /*parses the data in the torrent file given by the user*/
    private static void torrentParser(File torrentFile){
         
    }/*end of torrentParser method*/

    private static void sendRequest(){

    }/*end of sendRequest method*/

}
