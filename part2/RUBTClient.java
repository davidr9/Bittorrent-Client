import java.io.*;
import java.net.*;
import java.net.URL.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.*;

/**
 * @author Julie Duncan
 * @author David Rubin
 * @author Rosheen Chaudhry
 */

public class RUBTClient extends Thread{
	
	private static int port;
	
	static int uploaded;
	
	static int downloaded;
	
	static int left;
	
	public static String event;
	
	public static int started;
	
	public static int stopped;
	
	public static int completed;
	
	public static ByteBuffer[] pieces;
	
	public static int numPieces;
	
	public static int pieceLength;
	
	public static int lastPieceLength;
	
	public static ArrayList<Piece> verifiedPieces = new ArrayList<Piece>();;
	
	public static byte[] clientID = "davidrrosheencjulied".getBytes(); /*string of length 20 used as local host's ID*/ 
	
	public static TorrentInfo torrentData = null; /*contains parsed data from torrent*/
	
	private static List<Peer> connectedPeers;
	
	private static long beginTime;
	
	public static void main(String[] args) throws UnknownHostException, IOException, NullPointerException, BencodingException {
		
		/*Error handling when user enters incorrect number of arguments*/
        if (args.length != 2)
		{
			System.out.println("Correct Usage: RUBTClient <.torrent file name> <ouptut file name>");
			return;
		}
		
        String inFileName = args[0]; /*torrent file*/
		String outFileName = args[1]; /*file to output successful download to*/
		
		File torrentFile = new File(inFileName); /*torrent file stream*/
		
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
			System.err.println("Cannot proceed if torrent file is null");
			return;
		}
        
        pieces = torrentData.piece_hashes;
        pieceLength = torrentData.piece_length;
        left = torrentData.file_length;
 
        if (torrentData.file_length%torrentData.piece_length == 0){
        	numPieces = torrentData.file_length/torrentData.piece_length;
        } else {
        	numPieces = torrentData.file_length/torrentData.piece_length + 1;
        	lastPieceLength = torrentData.file_length%torrentData.piece_length;
        }
        
        for (int i = 0; i < numPieces; i++){
        	verifiedPieces.add(null);
        }
       
        ArrayList<Peer> peers = sendRequestToTracker(torrentData); /*List of peers received from the tracker*/   
        connectToPeers(peers); /*array for peers that hold pieces of the file to download*/
        
        
	}/*end of main method*/

	/*parses the data in the torrent file given by the user*/
    private static TorrentInfo torrentParser(File torrentFile) throws BencodingException, FileNotFoundException, IOException{
                try {
                		/*Create streams*/
                        FileInputStream torrentStream = new FileInputStream(torrentFile);
                        DataInputStream torrentReader = new DataInputStream(torrentStream);
                        int length = (int) torrentFile.length();
                        
                        /*Read the torrent file into a byte array*/
                        byte[] tFile_byte = new byte[length];
                        torrentReader.readFully(tFile_byte);
                        
                        /*Close streams*/
                        torrentReader.close();
                        torrentStream.close();
                        
                        /*Use byte array to create object that holds torrent data*/
                        TorrentInfo tInfo = new TorrentInfo(tFile_byte);
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

    /*sends an HTTP GET request to the tracker and creates connection.*/
    private static ArrayList<Peer> sendRequestToTracker(TorrentInfo tInfo) 
    		throws MalformedURLException, IOException, UnknownHostException{
       
    	URL newURL = createURL(tInfo); /*Properly formatted URL*/
    	
    	try {
    		/*send HTTP GET request to tracker*/
    		byte[] trackerResponse;
    	    HttpURLConnection request = (HttpURLConnection) newURL.openConnection();
    	    request.setRequestMethod("GET");
    	    DataInputStream trackerStream = new DataInputStream(request.getInputStream());
    	   
    	    /*get tracker response*/
    	    int requestSize = request.getContentLength(); 
    	    trackerResponse = new byte[requestSize];
    	    trackerStream.readFully(trackerResponse);
    	    trackerStream.close();
    	    
    	    return findPeerList(trackerResponse); /*return list of peers given by tracker*/
    	} catch (IOException e) {
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	} catch (Exception e) {
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	}
        
    }/*end of sendRequestToTracker method*/
    
    /*Concatenates string with relevant data into proper URL format and returns equivalent URL*/
    private static URL createURL(TorrentInfo tInfo) throws MalformedURLException, UnsupportedEncodingException{
    	String workingURL = tInfo.announce_url.toString() + '?'; /*base URL*/
    	
    	String escapedInfoHash = escape(tInfo.info_hash.array()); /*escaped version of info_hash*/
    	String escapedPeerID = escape(clientID); /*escaped version of id*/
    	String port = Integer.toString(extractPort(tInfo.announce_url));
    	String left = Integer.toString(tInfo.file_length); /*initially set as the size of the file to be downloaded*/
   
    	/*concatenate data into proper URL format*/
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
            		/*if character in question is invalid for URL encoding*/
                    if ((unEscaped[i] & 0x80) == 0x80){
                            byte curr = unEscaped[i];
                            byte lo = (byte) (curr & 0x0f); /*retrieves least significant byte in hex form*/
                            byte hi = (byte) ((curr >> 4) & 0x0f); /*retrieves most significant byte in hex form*/
                            result = result + '%' + hexDigits[hi] + hexDigits[lo]; /*append the hex representation preceded by '%'*/
                    } else {
                    	try {
                    		 result = result + URLEncoder.encode(new String(new byte[] {unEscaped[i]}),"UTF-8"); /*append character as is*/
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
    			TrackerResponseInfo trackerData = new TrackerResponseInfo(tracker_response); /*create object with data from tracker*/
        		ArrayList<Peer> peers = trackerData.peers; /*get list of peers from tracker*/
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
    		
    }/*end of findPeerList method*/
    
    /*
     * Returns the first peer whose peer_id prefix is "RUBT11"
     */
    private static Peer choosePeer(ArrayList<Peer> peers, int start) throws UnsupportedEncodingException {
		
    	for (int i = start ; i < peers.size(); i++){
			String ip = peers.get(i).getIP();
			if (ip.equals("128.6.171.130") || ip.equals("128.6.171.131")){
				return peers.get(i);
			}
		}
    	
    	System.err.println("No valid peer found in list");
		return null;
	}/*end of choosePeer method*/
    
    public static int extractPort(URL url){
		return url.getPort();
	}
    
    private static void connectToPeers(ArrayList<Peer> peers) throws SocketException, IOException{
    	
    	connectedPeers = new ArrayList<Peer>();
    	System.out.println(peers.size());
    	
    	for (int i = 0; i < peers.size(); i++){
			String ip = peers.get(i).getIP();
			if (ip.equals("128.6.171.130") || ip.equals("128.6.171.131")){
				connectedPeers.add(peers.get(i));
				System.out.println("Added peer at " + peers.get(i).getIP());
			}
    	}
    	
    	int connected = 0;
    	
    	beginTime = System.nanoTime(); /*This is where we will begin to time the download*/
    	
    	for (int i = 0; i < connectedPeers.size(); i++){
				connectedPeers.get(i).startThread();
		}
    } /*end of connectToPeers method*/
    
    /*Prints the connection information to the tracker*/
	public static void publishTrackerInfo()
	{
		System.out.println("Uploaded: " + uploaded);
		System.out.println("Downloaded: " + downloaded);
		System.out.println("Left: " + left); 
		System.out.println("Completed: " + completed);
		System.out.println("Started: " + started);
		System.out.println("Stopped: " + stopped);
	}/*end of publishTrackerInfo*/
	
	public static boolean[] convertBitfield(byte[] bits, int significantBits) {
		boolean[] retVal = new boolean[significantBits];
		int boolIndex = 0;
		for (int byteIndex = 0; byteIndex < bits.length; ++byteIndex) {
			for (int bitIndex = 7; bitIndex >= 0; --bitIndex) {
				if (boolIndex >= significantBits) {
					// Bad to return within a loop, but it's the easiest way
					return retVal;
				}

				retVal[boolIndex++] = (bits[byteIndex] >> bitIndex & 0x01) == 1 ? true: false;
			}
		}
		return retVal;
	}
	
	/*Goes through arraylist of pieces and writes all pieces to file*/
        public static void writeToDisk(String outFile) throws FileNotFoundException, IOException
        {
            /*outFile is the name you want to save the file to*/
            RandomAccessFile file = new RandomAccessFile(outFile, "rw");
            long len = numPieces * pieceLength; 
            /*sets the file length*/
            file.setLength(len);
            /*index in which the byte[] will be written*/
            long index = 0;
            
            /*goes through pieces array and writes pieces to file*/
            for(int i = 0; i < verifiedPieces.size()-1; i++)
            {
                if(verifiedPieces.get(i) == null)
                {
                    continue;
                }else{
                    System.out.println("Writing to file");
                    index = (long) verifiedPieces.get(i).index;
                    file.seek(index);
                    file.write(verifiedPieces.get(i).fullPiece);
                }    
            }/*end of for loop*/
            file.close();
            
        }/*end of writeToDisk*/
	
}/*end of RUBTClient class*/
