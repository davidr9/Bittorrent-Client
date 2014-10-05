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
        /*prints out the torrent file information. fields are the key value pairs of anounnce, info_hash, etc*/
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

    /*This was really helpful for learning about torrents/bittorrent clients
      https://wiki.theory.org/BitTorrentSpecification*/
    /*sends an HTTP GET request to the tracker and creates connection.*/
    private static void sendRequestToTracker(TorrentInfo torrentInfo){
        /*information needed to communicate with the trackers. need to be escaped, write method for that later*/
        /*also not sure if port, uploaded, and downloaded are strings. need to figure out later*/
        String info_hash, peer_id, port, uploaded, downloaded, left, event;
        String oldURL = torrentInfo.announce_url.toString();
        
        /*steps:
        * 1. convert the info_hash from the torrentInfo into hex (escaping)
        * 2. get port number from info_hash, you can check to see if it's correct with the announce url
        * 3. figure out how to use java.net.url class to send HTTP request
        * 4. all the tracker request parameters are the above declared variables
        * 5. calls captureTrackerResponse to store the data in a variable
        */
        HttpURLConnection connection_to_tracker;
        
    }/*end of sendRequest method*/
    
    /*Converts the info_hash dictionary into hex. This is decoding the SHA1*/
    private static void escape(byte[] torrent_info){
        /*need to escape the data from the info_hash*/
    }
    
    /*gets the peer list and other info from the tracker*/
    private static byte[] captureTrackerResponse(String URL){
        /* 1. use URL to make a HTTPURLConnection to tracker
        *  2. store peer list from tracker. the response is a bencoded dictionary
        */
        byte[] trackerResponse; 
        return null;
        
    }/*end of captureTrackerResponse*/
    
    /*the tracker responds with a bencoded dictionary as the peer list*/
    /*uses the becoder2 class to decode the dictorary and store the peers in a list*/
    private static void decodeTrackerResponse(byte[] tracker_response){
        /*
            1. store the interval and peers as a list (Have a peer object with peerid, ip, port)
            2. Make Object o and call beconder2.decode() with tracker response
            3. You now have all the necessary info for a handshake
        */
    }
}
