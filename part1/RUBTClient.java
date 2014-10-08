import java.io.*;
import java.net.*;
import java.net.URL.*;
import java.security.*;
import java.util.ArrayList;
import java.nio.*;

public class RUBTClient {
	
	public static byte[] clientID = "davidrrosheencjulied".getBytes();

	public static void main(String[] args) throws UnknownHostException, IOException, NullPointerException, BencodingException {
	
		/*Error handling when user enters incorrect number of arguments*/
        if (args.length != 2)
		{
			System.out.println("Correct Usage: RUBTClient <.torrent file name> <ouptut file name>");
			return;
		}
		
		/*argument 0 is the torrent and argument 1 is a file that will be sent to server*/
        String inFileName = args[0];
		String outFileName = args[1];
		int connected = -1; //0 if connected to peer successfully, -1 otherwise
		int i = 0;
		
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
        
        
        
        /*prints out the torrent file information. fields are the key value pairs of announce, info_hash, etc*/
        ArrayList<Peer> peers = sendRequestToTracker(torrentData);
        Peer singlePeer = null;
        
        while (connected != 0){
        	singlePeer = peers.get(i);
        	if (singlePeer != null){
        		connected = singlePeer.shakeHand();
        	}
        	i++;
        	if (i == peers.size()){ 
        		System.err.println("No valid peer found");
        		return;
        	}
        }
		
        singlePeer.requestPiecesFromPeer();
        
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
    private static ArrayList<Peer> sendRequestToTracker(TorrentInfo tInfo) 
    		throws MalformedURLException, IOException, UnknownHostException{
       
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
    	    
    	    System.out.println(trackerResponse.toString());
    	    return findPeerList(trackerResponse);
    	} catch (IOException e) {
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	} catch (Exception e) {
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	}
        
    }/*end of sendRequestToTracker method*/
    
    private static URL createURL(TorrentInfo tInfo) throws MalformedURLException, UnsupportedEncodingException{
    	String workingURL = tInfo.announce_url.toString() + '?';
    	String escapedInfoHash = escape(tInfo.info_hash.array());
    	String escapedPeerID = escape(clientID);
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
    }/*end of createURL method*/
    
    
    /*Converts into hex. This is decoding the SHA1*/
    public static String escape(byte[] unEscaped) throws UnsupportedEncodingException{
            String result = ""; //empty string to build upon and return
            char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'}; //list of possible hex digits in base 16

            for (int i = 0; i < unEscaped.length; i++) {
                    if ((unEscaped[i] & 0x80) == 0x80){
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
    }/*end of escape method*/
    
    /*
     * Returns the list of peers generated from the tracker as an array list 
     */
    private static ArrayList<Peer> findPeerList(byte[] tracker_response) throws BencodingException, IOException{
        
    		try{
    			TrackerResponseInfo trackerData = new TrackerResponseInfo(tracker_response);
        		ArrayList<Peer> peers = trackerData.peers;
        		if (peers.isEmpty()){
        			throw new NullPointerException("List of peers is empty");
        		}
        		
        		return peers;
    		} catch (BencodingException e) {
    			System.err.println("Error: " + e.getMessage());
    			return null;
    		} catch (IOException e) {
    			System.err.println("Error: " + e.getMessage());
    			return null;
    		}
    		
           /*1. store the interval and peers as a list (Have a peer object with peerid, ip, port)
           *2. Make Object o and call beconder2.decode() with tracker response
           *3. You now have all the necessary info for a handshake
           */
    }/*end of findPeerList method*/
    
    /*
     * Returns the first peer whose peer_id prefix is "RUBT11"
     */
    private static Peer choosePeer(ArrayList<Peer> peers, int start) throws UnsupportedEncodingException {
		
    	try{
    		for (int i = start ; i < peers.size(); i++){
        		String id = escape(peers.get(i).peerID);
        		if (id.startsWith("RUBT11")){
        			return peers.get(i);
        		}
        	}
    	} catch (UnsupportedEncodingException e){
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	}
    	
    	System.err.println("No valid 'RUBT11' peer found in list");
		return null;
	}/*end of choosePeer method*/
    
    public static int extractPort(URL url){
		return url.getPort();
	}
	
}/*end of RUBTClient class*/
