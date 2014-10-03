import java.io.*;
import java.net.*;
import java.net.URL.*;


public class RUBTClient {

	public static void main(String[] args) throws UnknownHostException, IOException, NullPointerException, BencodingException {
	
		/*Error handling when user enters incorrect number of arguements*/
        if (args.length != 2)
		{
			System.out.println("Correct Usage: RUBTClient <.torrent file name> <ouptut file name>");
			return;
		}
		
		/*arguement 0 is the torrent and argument 1 is a file that will be sent to server*/
                String inFileName = args[0];
		String outFileName = args[1];
		
	File torrentFile = new File(inFileName);
        TorrentInfo data_in_torrent = null; 
        /*opens the torrent file or throws an exception if file doesn't exist*/
        try {
			/*If torrentFile is null, then program exits*/
			if (!torrentFile.exists()){
				System.err.println("Torrent file does not exist");
				return;
			}
			
			/*parses torrent if torrent exists*/
                        data_in_torrent = torrentParser(torrentFile);
                      
		} catch (NullPointerException e)
		{
			System.err.println("torrent file cannot be null");
			return;
		}
        
        ToolKit.print(data_in_torrent.torrent_file_map);
        sendRequestToTracker(data_in_torrent);
		
	}/*end of main method*/

    /*parses the data in the torrent file given by the user*/
    private static TorrentInfo torrentParser(File torrentFile) throws BencodingException, FileNotFoundException, IOException{
                try {
                        FileInputStream torrentStream = new FileInputStream(torrentFile);
                        DataInputStream torrentReader = new DataInputStream(torrentStream);
                        int length = (int) torrentFile.length();
                        byte[] tFile_byte = new byte[length];
                        torrentReader.readFully(tFile_byte);
                        torrentReader.close();
                        torrentStream.close();
                        TorrentInfo tInfo = new TorrentInfo(tFile_byte);
                        System.out.println("Torrent File is parsed");
                        return tInfo;
                } catch (BencodingException e){
                        System.err.println(e.getMessage());
                        return null;
                } catch (FileNotFoundException e){
                        System.err.println(e.getMessage());
                        return null;
                } catch (IOException e){
                        System.err.println(e.getMessage());
                        return null;
                }

        }/*end of torrentParser method*/

    /*sends an HTTP GET request to the tracker and creates connection*/
    private static void sendRequestToTracker(TorrentInfo torrentInfo){
        /*information needed to communicate with the trackers. need to be escaped, write method for that later*/
        /*also not sure if port, uploaded, and downloaded are strings. need to figure out later*/
        String info_hash, peer_id, left, event, port, uploaded, downloaded;
        HttpURLConnection connection_to_tracker;
        
    }/*end of sendRequest method*/
    
    /*gets IP address of tracker from torrent*/
    private static void extractIP(TorrentInfo torrentInfo){
        
    }
    
    /*gets port number of tracker for torrent*/
    private static void extractPort(TorrentInfo torrentInfo){
        
    }
    private static void captureTrackerResponse(){
        
    }/*end of captureTrackerResponse*/

}
