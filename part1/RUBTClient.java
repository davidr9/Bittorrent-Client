import java.io.*;
import java.net.*;
import java.net.URL.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


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
        TorrentInfo torrentData = null; 
        /*opens the torrent file or throws an exception if file doesn't exist*/
        try {
			/*If torrentFile is null, then program exits*/
			if (!torrentFile.exists()){
				System.err.println("Torrent file does not exist");
				return;
			}
			
			/*parses torrent if torrent exists*/
                        torrentData = torrentParser(torrentFile);
                      
		} catch (NullPointerException e)
		{
			System.err.println("torrent file cannot be null");
			return;
		}
        /*prints out the torrent file information. fields are the key value pairs of anounnce, info_hash, etc*/
        ToolKit.print(torrentData.torrent_file_map);
        byte[] trackerResponse = sendRequestToTracker(torrentData);
        
        extractIP(torrentData.announce_url);
        extractPort(torrentData.announce_url);
        System.out.println("INFO HASH URL IS:" + infoHashToURL(torrentData.info_hash));
        
        //decodeTrackerResponse(trackerResponse);

		
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
    private static byte[] sendRequestToTracker(TorrentInfo tInfo) 
    		throws MalformedURLException, IOException, UnknownHostException{
        /*information needed to communicate with the trackers. need to be escaped, write method for that later*/
        /*also not sure if port, uploaded, and downloaded are strings. need to figure out later*/
    	URL newURL = createURL(tInfo);
    	
    	try {
    		byte[] trackerResponse;
    	    HttpURLConnection request = (HttpURLConnection) newURL.openConnection();
    	    request.setRequestMethod("GET");
    	    DataInputStream trackerStream = new DataInputStream(request.getInputStream());
    	    int requestSize = request.getContentLength();
    	    trackerResponse = new byte[requestSize];
    	    trackerStream.readFully(trackerResponse);
    	    trackerStream.close();
    	    return trackerResponse;
    	     
    	} catch (IOException e) {
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	} catch (Exception e) {
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	}
		//return null;
   
        /*steps:
        * 1. convert the info_hash from the torrentInfo into hex (escaping)
        * 2. get port number from info_hash, you can check to see if it's correct with the announce url
        * 3. figure out how to use java.net.url class to send HTTP request
        * 4. all the tracker request parameters are the above declared variables
        * 5. calls captureTrackerResponse to store the data in a variable
        */
        
    }/*end of sendRequest method*/
    
    private static URL createURL(TorrentInfo tInfo) throws MalformedURLException, UnsupportedEncodingException{
    	String workingURL = tInfo.announce_url.toString() + '?';
    	String escapedInfoHash = escape(tInfo.info_hash.array());
    	String escapedPeerID = escape("davidrrosheencjulied".getBytes());
    	String port = Integer.toString(extractPort(tInfo.announce_url));
    	String left = Integer.toString(tInfo.file_length);
    	
    	workingURL = workingURL + "info_hash" + "=" + escapedInfoHash + "&peer_id=" + escapedPeerID + "&port="
    			+ port + "&uploaded=0&downloaded=0&left=" + left;
    	
    	try {
    		URL finalURL = new URL(workingURL);
        	return finalURL;
    	} catch (MalformedURLException e){
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	}
    }
    
    
    /*Converts into hex. This is decoding the SHA1*/
    private static String escape(byte[] unEscaped) throws UnsupportedEncodingException{
            System.out.println("Entering escape()");
            String result = ""; 
            char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

            for (int i = 0; i < unEscaped.length; i++) {
                    if ((unEscaped[i] & 0x80) == 0x080){
                            byte curr = unEscaped[i];
                            byte lo = (byte) (curr & 0x0f);
                            byte hi = (byte) ((curr >> 4) & 0x0f);
                            result = result + '%' + hexDigits[hi] + hexDigits[lo];
                    } else {
                    	try {
                    		 result = result + URLEncoder.encode(new String(new byte[] {unEscaped[i]}),"UTF-8");
                    	} catch (UnsupportedEncodingException e) {
                    		System.out.println("Error: " + e.getMessage());
                    	}
                    }         
            }   

            return result;
    }
    
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
    
    
    /*the tracker responds with a bencoded dictionary as the peer list*/
    /*uses the becoder2 class to decode the dictorary and store the peers in a list*/
    private static void decodeTrackerResponse(byte[] tracker_response) throws BencodingException{
	    /*
	     	1. Decode the tracker response
	        3. Make Object o and call beconder2.decode() with tracker response
	        4. You now have all the necessary info for a handshake
	    */
	            try{
        TrackerResponseInfo decodedResponse = new TrackerResponseInfo(tracker_response);
        }catch(BencodingException e) {
            System.out.println("Tracker Response was not bencoded properly");
        }catch(IOException e){
            System.out.println("User Input/Output error ");
        }catch(Exception e){
            System.out.println("General exception was caught");
        }
    }
}/*end of RUBTClient class*/
