import java.io.*;
import java.net.*;
import java.net.URL.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


public class RUBTClient {
	
	public static String extractIP(URL url){
			System.out.println("HOST IS:" + url.getHost());
			return url.getHost();
		}
		
	public static int extractPort(URL url){
			System.out.println("PORT IS:" + url.getPort());
			return url.getPort();
		}
		
	/*
     * This method takes in an info_hash of type ByteBuffer and returns its string representation.
     * This algorithm can be found anywhere on the internet.
     * */
    @SuppressWarnings("unused")
	private static String infoHashToURL(ByteBuffer info){
    	/*Create a byte array out of the values from info*/ 
    	byte[] info_bytes = new byte[info.capacity()];
    	info.get(info_bytes, 0, info_bytes.length);
    	
    	String hex[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };
    	String output="";
    	byte bt = 0x00;
    	
    	for(int i=0; i<info_bytes.length; i++){
    		/*ASCII*/
    		if((info_bytes[i] >= '0' && info_bytes[i] <= '9')
    				|| (info_bytes[i] >= 'a' && info_bytes[i] <= 'z')
    				|| (info_bytes[i] >= 'A' && info_bytes[i] <= 'Z') 
    				|| (info_bytes[i] == '$') || (info_bytes[i]=='-') || (info_bytes[i] == '_') 
    				|| (info_bytes[i] == '.') || (info_bytes[i] == '+') || info_bytes[i] =='!'){
    			
    			output = output + "" + (char)info_bytes[i];	
    		}
    		else{ /*Hex*/
    			output = output + "%";
				bt = (byte) (info_bytes[i] & 0xF0); 
				bt = (byte) (bt >>> 4); 
				bt = (byte) (bt & 0x0F); 
				output = output + (hex[(int) bt]); 
				bt = (byte) (info_bytes[i] & 0x0F); 
				output = output + (hex[(int) bt]); 
    		}
    	}
    	return output;
    	
    }		

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
        
        extractIP(data_in_torrent.announce_url);
        extractPort(data_in_torrent.announce_url);
        System.out.println("INFO HASH URL IS:" + infoHashToURL(data_in_torrent.info_hash));


		
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
