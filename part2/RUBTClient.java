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
	
	private static int port; /*Port on which this client is downloading/uploading*/
	
	static int uploaded; /*keeps track of how much of the file has been uploaded to other peers, in bytes*/
	
	static int downloaded; /*keeps track of how much of the file has been successfully downloaded from other peers, in bytes*/
	
	static int left; /*keeps track of how much of the file still needs to be written, in bytes*/
	
	public static String event = ""; /*"started" when client starts, "completed" when download completes, or "stopped" when downloading ceases. Empty o/w*/
	
	public static ByteBuffer[] pieces; /*Expected SHA-1 hashes of each piece of the file given from the torrent*/
	
	public static int numPieces; /*Number of pieces to download form the file*/
	
	public static int pieceLength; /*default length of each piece*/
	
	public static int lastPieceLength; /*length of last piece if smaller than the rest*/
	
	public static ArrayList<Piece> verifiedPieces = new ArrayList<Piece>(); /*shared array list for all already verified pieces*/
	
	public static int numPiecesVerified = 0; /*number of pieces verified thus far*/
	
	public static byte[] clientID = "davidrrosheencjulied".getBytes(); /*string of length 20 used as local host's ID*/ 
	
	public static TorrentInfo torrentData = null; /*contains parsed data from torrent*/
	
	private static List<Peer> connectedPeers; /*Peers to attempt connections with from the peer list given by the torrent*/
	
	private static boolean singlePeer; /*If 3rd argument is specified as IPAddress, we only connect to this peer*/
	
	public static String singlePeerAddress; /*for 3rd argument if specified*/
	
	private static long beginTime; /*time the download started*/
	
	private static long downloadTime; /*time it took to download during this session*/
	
	public static void main(String[] args) throws UnknownHostException, IOException, NullPointerException, BencodingException, InterruptedException {
		
		/*Error handling when user enters incorrect number of arguments*/
        if (args.length > 3 || args.length < 2)
		{
			System.out.println("Correct Usage: RUBTClient <.torrent file name> <ouptut file name>");
			return;
		}
        
        if (args.length == 3){
        	singlePeer = true;
        	singlePeerAddress = args[2];
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
        downloaded = 0;
        uploaded = 0;
 
        if (torrentData.file_length%torrentData.piece_length == 0){
        	numPieces = torrentData.file_length/torrentData.piece_length;
        } else {
        	numPieces = torrentData.file_length/torrentData.piece_length + 1;
        	lastPieceLength = torrentData.file_length%torrentData.piece_length;
        }
        
        System.out.println("Numpieces is " + numPieces);
        System.out.println("lastPieceLength is " + lastPieceLength);
        
        for (int i = 0; i < numPieces; i++){
        	verifiedPieces.add(null);
        }
       
        ArrayList<Peer> peers = sendRequestToTracker(torrentData); /*List of peers received from the tracker*/ 
        
        if (singlePeer){
        	connectToPeer(peers, singlePeerAddress);
        } else {
        	connectToPeers(peers); /*array for peers that hold pieces of the file to download*/
        }
        writeToDisk(outFileName);
        
	}/*end of main method*/

	/*
	 * parses the data in the torrent file given by the user
	 * 
	 * @param torrentFile name of torrent file to be parsed
	 * @return torrent info object with all parsed info
	 * */
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

    /*
     * sends an HTTP GET request to the tracker and creates connection
     * 
     * @param tInfo contains all info from torrent
     * @return Array List of peers given by tracker
     */
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
    
    /*
     * Concatenates string with relevant data into proper URL format and returns equivalent URL
     * 
     * @param tInfo contains all info from torrent
     * @return finished URL
     */
    private static URL createURL(TorrentInfo tInfo) throws MalformedURLException, UnsupportedEncodingException{
    	String workingURL = tInfo.announce_url.toString() + '?'; /*base URL*/
    	
    	String escapedInfoHash = escape(tInfo.info_hash.array()); /*escaped version of info_hash*/
    	String escapedPeerID = escape(clientID); /*escaped version of id*/
    	String port = Integer.toString(extractPort(tInfo.announce_url));
    	String l = Integer.toString(left); /*initially set as the size of the file to be downloaded*/
    	String d = Integer.toString(downloaded);
    	String u = Integer.toString(uploaded);
    	
    	/*concatenate data into proper URL format*/
    	workingURL = workingURL + "info_hash" + "=" + escapedInfoHash + "&peer_id=" + escapedPeerID + "&port="
    			+ port + "&uploaded=" + u + "&downloaded=" + d + "&left=" + l + "&event=" + event;
    	
    	try {
    		URL finalURL = new URL(workingURL);
        	return finalURL;
    	} catch (MalformedURLException e){
    		System.err.println("Error: " + e.getMessage());
    		return null;
    	}
    }/*end of createURL method*/
    
    
    /*
     * Converts a byte array into hex. This is decoding the SHA1
     * 
     * @param unEscaped byte array to be converted
     * @return string representation of SHA-1 hash
     * */
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
     * Returns the first peer whose IPAddress is valid
     * 
     * @param peers arraylist of peers retrieved from tracker
     * @param start index from which to begin searching within peers
     * 
     * @return peer object with given IPAddress
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
    
    /*
     * Connects to peers at a specific IP address and starts threads for each. Joins threads upon completion and prints total download time
     * 
     * @param peers list of all peers from tracker
     */
    private static void connectToPeers(ArrayList<Peer> peers) throws SocketException, IOException, InterruptedException{
    	
    	connectedPeers = new ArrayList<Peer>();
    	System.out.println(peers.size());
    	
    	for (int i = 0; i < peers.size(); i++){
			String ip = peers.get(i).getIP();
			if (ip.equals("128.6.171.130") || ip.equals("128.6.171.131")){
				connectedPeers.add(peers.get(i));
				System.out.println("Added peer at " + peers.get(i).getIP());
			}
    	}
    	
    	beginTime = System.currentTimeMillis(); /*This is where we will begin to time the download*/
    	event = "started";
    	
    	for (int i = 0; i < connectedPeers.size(); i++){
			connectedPeers.get(i).startThread();
		}
    	
    	for (int i = 0; i < connectedPeers.size(); i++){
    		connectedPeers.get(i).th.join();
    	}
    	
    	downloadTime = System.currentTimeMillis() - beginTime;
    	System.out.println("Total time of download: " + downloadTime + " ms");
    	
    } /*end of connectToPeers method*/
    
    /*
     * Connects to single specific peer at IP address given by command line. Otherwise, does the same as connectToPeers()
     * 
     * @param peers list of all peers form tracker
     * @param ipAddress IP address of peer to communicate with
     */
    private static void connectToPeer(ArrayList<Peer> peers, String ipAddress) throws SocketException, IOException, InterruptedException{
    	
    	connectedPeers = new ArrayList<Peer>();
    	System.out.println(peers.size());
    	
    	for (int i = 0; i < peers.size(); i++){
			String ip = peers.get(i).getIP();
			if (ip.equals(ipAddress)){
				connectedPeers.add(peers.get(i));
				System.out.println("Added peer at " + peers.get(i).getIP());
				break;
			}
    	}
    	
    	beginTime = System.currentTimeMillis(); /*This is where we will begin to time the download*/
    	event = "started";
    	
    	for (int i = 0; i < connectedPeers.size(); i++){
				connectedPeers.get(i).startThread();
		}
    	
    	for (int i = 0; i < connectedPeers.size(); i++){
    		connectedPeers.get(i).th.join();
    	}
    	
    	downloadTime = System.currentTimeMillis() - beginTime;
    	System.out.println("Total time of download: " + downloadTime + " ms");
    } /*end of connectToPeers method*/
    
    /*Prints the connection information to the tracker*/
	public static void publishTrackerInfo()
	{
		System.out.println("Uploaded: " + uploaded);
		System.out.println("Downloaded: " + downloaded);
		System.out.println("Left: " + left); 
		System.out.println("Event: " + event);
	}/*end of publishTrackerInfo*/
	
	public static boolean[] convertBitfield(byte[] bits, int significantBits) {
		boolean[] retVal = new boolean[significantBits];
		int boolIndex = 0;
		for (int byteIndex = 0; byteIndex < bits.length; ++byteIndex) {
			for (int bitIndex = 7; bitIndex >= 0; --bitIndex) {
				if (boolIndex >= significantBits) {
					return retVal;
				}

				retVal[boolIndex++] = (bits[byteIndex] >> bitIndex & 0x01) == 1 ? true: false;
			}
		}
		return retVal;
	}
	
	/*
	 * Goes through array list of verified pieces and writes all pieces to file
	 * 
	 * @param outFile name of file to write to given by command line
	 */
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
