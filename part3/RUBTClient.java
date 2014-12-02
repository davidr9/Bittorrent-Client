import java.awt.Component;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.nio.*;

/**
 * @author Julie Duncan
 * @author David Rubin
 * @author Rosheen Chaudhry
 */

public class RUBTClient extends Thread{
    
    private static int port; /*Port on which this client is downloading/uploading*/
    
    private static int uploaded; /*keeps track of how much of the file has been uploaded to other peers, in bytes*/
    
    private static int downloaded; /*keeps track of how much of the file has been successfully downloaded from other peers, in bytes*/
    
    private static int left; /*keeps track of how much of the file still needs to be written, in bytes*/
    
    public static ByteBuffer[] pieces; /*Expected SHA-1 hashes of each piece of the file given from the torrent*/
    
    private static int numPieces; /*Number of pieces to download form the file*/
    
    private static int pieceLength; /*default length of each piece*/
    
    private static int lastPieceLength; /*length of last piece if smaller than the rest*/
    
    private static ArrayList<Piece> verifiedPieces = new ArrayList<Piece>(); /*shared array list for all already verified pieces*/
    
    private static int numPiecesVerified = 0; /*number of pieces verified thus far*/
    
    private static byte[] clientID = "davidrrosheencjulied".getBytes(); /*string of length 20 used as local host's ID*/ 
    
    private static TorrentInfo torrentData = null; /*contains parsed data from torrent*/
    
    private static List<Peer> connectedPeers; /*Peers to attempt connections with from the peer list given by the torrent*/
    
    private static boolean singlePeer; /*If 3rd argument is specified as IPAddress, we only connect to this peer*/
    
    private static String singlePeerAddress; /*for 3rd argument if specified*/
    
    private static Timer clientTimer = new Timer(true); /*Timer object used to keep track of the time*/
    
    private static long beginTime; /*time the download started*/
    
    private static double downloadTime; /*time it took to download during this session*/
    
    private static int interval; /*interval received from tracker for periodic tracker announces*/
    
    private static byte[] clientBitfield = new byte[numPieces]; /*byte array that shows the pieces the client has downloaded*/
    
    private static boolean[] clientPieces; /*zero-based piece indecis will be true if piece has been downlaoded/verified and 0 o/w*/
    
    private static String fName; /*name of the file in which the pieces will be written to*/

    private static String[] args;
    
    public static void main(String[] arguments) {
        
        /*Error handling when user enters incorrect number of arguments*/
       args = arguments;
        if (args.length > 3 || args.length < 2)
        {
            System.out.println("Correct Usage: RUBTClient <.torrent file name> <ouptut file name> <IP address (optional)>");
            return;
        }
        
        torrentGUI.startGUI();
        
    }/*Main ends here*/
    
    public static void begin() throws UnknownHostException, IOException, NullPointerException, BencodingException, InterruptedException {
        /*checks to see if the last argument is a peer*/
        if (args.length == 3){
            singlePeer = true;
            singlePeerAddress = args[2];
        }
        
        String inFileName = args[0]; /*torrent file*/
        String outFileName = args[1]; /*file to output successful download to*/
        fName = outFileName; 
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
        port = extractPort(torrentData.announce_url);
        downloaded = 0;
        uploaded = 0;
 
        if (torrentData.file_length%torrentData.piece_length == 0){
            numPieces = torrentData.file_length/torrentData.piece_length;
        } else {
            numPieces = torrentData.file_length/torrentData.piece_length + 1;
            lastPieceLength = torrentData.file_length%torrentData.piece_length;
        }

        clientPieces = new boolean[numPieces*8];
        
        for (int i = 0; i < numPieces; i++){
            verifiedPieces.add(null);
        }
       
        ArrayList<Peer> peers = sendRequestToTracker(); /*List of peers received from the tracker*/ 
        
        beginTime = System.currentTimeMillis(); /*This is where we will begin to time the download*/
        
        if (singlePeer){
            connectToPeer(peers, singlePeerAddress);
        } else {
            connectToPeers(peers); /*array for peers that hold pieces of the file to download*/
        }
       
        
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
    private static ArrayList<Peer> sendRequestToTracker() 
            throws MalformedURLException, IOException, UnknownHostException{
       
        URL newURL = createURL(""); /*Properly formatted URL*/
        
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
    
    public static void publishToTracker(String event) 
            throws MalformedURLException, IOException, UnknownHostException{

        URL newURL = createURL(event); /*Properly formatted URL*/
        
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
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }
        
    }/*end of sendRequestToTracker method*/
    
    /*
     * Concatenates string with relevant data into proper URL format and returns equivalent URL
     * 
     * @param tInfo contains all info from torrent
     * @return finished URL
     */
    private static URL createURL(String event) throws MalformedURLException, UnsupportedEncodingException{
        String workingURL = torrentData.announce_url.toString() + '?'; /*base URL*/
        
        String escapedInfoHash = escape(torrentData.info_hash.array()); /*escaped version of info_hash*/
        String escapedPeerID = escape(clientID); /*escaped version of id*/
        String port = Integer.toString(extractPort(torrentData.announce_url));
        String l = Integer.toString(left); /*initially set as the size of the file to be downloaded*/
        String d = Integer.toString(downloaded);
        String u = Integer.toString(uploaded);
        
        /*concatenate data into proper URL format*/
        workingURL = workingURL + "info_hash" + "=" + escapedInfoHash + "&peer_id=" + escapedPeerID + "&port="
                + port + "&uploaded=" + u + "&downloaded=" + d + "&left=" + l;
                
        if (event.length() > 0){
            workingURL = workingURL + "&event=" + event;
        }
        
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
                interval = trackerData.interval;
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
    
    /*
     * Connects to peers at a specific IP address and starts threads for each. Joins threads upon completion and prints total download time
     * 
     * @param peers list of all peers from tracker
     */
    private static void connectToPeers(ArrayList<Peer> peers) throws SocketException, IOException, InterruptedException{
        
        connectedPeers = new ArrayList<Peer>();
        
        for (int i = 0; i < peers.size(); i++){
            String ip = peers.get(i).getIP();
            if (ip.equals("128.6.171.130") || ip.equals("128.6.171.131")){
                connectedPeers.add(peers.get(i));
                System.out.println("Added peer at " + peers.get(i).getIP());
            }
        }
        
        startClientThread();
        
        for (int i = 0; i < connectedPeers.size(); i++){
            connectedPeers.get(i).startThread();
        }
        
        
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
        
        startClientThread();
        
        for (int i = 0; i < connectedPeers.size(); i++){
                connectedPeers.get(i).startThread();
        }
        
  
    } /*end of connectToPeers method*/
    
    /*
     * Converts bitfield from a bitfield message to a boolean array with the same number of bits
     * 
     * @param bits payload of bitfield message
     * @param significantBits 8 * the length of bits
     * @return boolean array with true at all indecis where bit is set, and false for those where bit is 0
     */
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
    
    /**
     * Convert the client boolean bitfield into a byte[]
     * @param bitfield thecClient boolean bitfield.
     * @return The byte[] version of the Client bitfield
     */
    private static byte[] convertBooleanField(boolean[] bitfield) {
        //Calcuate the number of bytes needed to contain the bitfield
        byte[] bytes = new byte[(int)Math.ceil(bitfield.length / 8.0)];
        for(int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)0;
            for(int j = 0; j < 8; j++) {
                if((i*8+j)==bitfield.length) {
                    break;
                }
                byte curr = (byte)0;
                //If the boolean array contains a 1, set curr to 0x1.
                if( bitfield[i*8+j] ) {
                    //bitwise or to append the bit to the end of the current byte
                    curr = (byte)1;
                    curr <<= (7-j);
                    bytes[i] = (byte)(bytes[i]|curr);
                }
                
            } //for each bit in a byte
        } //for each byte
        return bytes;
    } /*end of convertBooleanField method*/
    
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
        for(int i = 0; i < verifiedPieces.size(); i++)
        {   
            if(verifiedPieces.get(i) == null)
            {
                continue;
            }else{
                /*writes to file if the index of the arraylist contains data*/
                index = verifiedPieces.get(i).index * verifiedPieces.get(0).fullPiece.length;
                file.seek(index);
                file.write(verifiedPieces.get(i).fullPiece);
            }
        }/*end of for loop*/
        file.close();
        
        System.out.println("FILE SAVED TO DISK");
    }/*end of writeToDisk*/
    
    public static void startClientThread() throws UnsupportedEncodingException{
        Thread clientThread = null; /*thread to run this client*/
        String tName; /*name of thread for this client*/
        
        if (clientThread == null){
            tName = escape(clientID);
            clientThread = new RUBTClient();
            clientThread.start();
        }
    }
    
    
    public void run(){
    	
        try {
            System.out.println("Thread " + escape(clientID) + " has begun running");
            publishToTracker("started");
            clientTimer.schedule(new publishStatus(), 0, interval*1000);
            /* Only implement this block without the GUI
            while(true){  
                Scanner sc = new Scanner(System.in);
                
                String line = sc.next();
                if(line.equals("q") || line.equals("Q") || line.equals("quit") || line.equals("Quit") || line.equals("stop")){
                    for (int i = 0; i < connectedPeers.size(); i++){
                        connectedPeers.get(i).th.stop();
                    }
                    
                    if (downloadTime == 0){
                        downloadTime = (System.currentTimeMillis() - beginTime);
                    }
                    System.out.println("Total time of download: " + downloadTime + " ms");
                    publishToTracker("stopped");
                    break;
                }

            }
            */   
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
   /*Task that publishes the connection information to the tracker*/
   class publishStatus extends TimerTask {
        public void run(){
            System.out.println("Current status being published to tracker:");
            System.out.println("Uploaded = " + uploaded);
            System.out.println("Downloaded = " + downloaded);
            System.out.println("Left = " + left); 
            
            try {
                publishToTracker("");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        
        }
        
    }/*end of publishTrackerInfo*/
   
   public static int extractPort(URL url){
       return url.getPort();
   }
   
   /*The remaining methods are simple get and update methods*/
   
   /*
    * Updates clientPieces after downloading and verifying a piece
    * 
    * @param pieceIndex the zero-based piece index of the piece being updated
    * @return updated boolean array clientPieces
    */
   public static void updateClientPieces(int pieceIndex){
       clientPieces[pieceIndex] = true;
       clientBitfield = convertBooleanField(clientPieces);
   }/*end of updateClientPieces*/
   
   /*method updates the number of blocks uploaded*/
   public static void updateUploaded(int size){
       uploaded += size; 
   }/*end of updateUploaded*/
   
   public static byte[] getClientBitfield(){
       return clientBitfield; 
   }
   
   /*
    * Add new piece to global list of client's verified pieces, incrementing the number of verified pieces
    * 
    * @param index zero-based piece index
    */
   public static void setVerifiedPieces(int index, Piece piece){
	   verifiedPieces.set(index, piece);
	   numPiecesVerified++;
   }
   
   /*Update the number of bytes downloaded and the number of bytes now left to download in the file
    * 
    * @param sizeInBytes number of bytes by which to update the count
    */
   public static void updateDownloaded(int sizeInBytes){
	   downloaded += sizeInBytes;
	   left -= sizeInBytes;
   }

   public static int getNumPiecesVerified() {
	   return numPiecesVerified;
   }

   public static int getNumPieces() {
	   return numPieces;
   }

   public static void updateDownloadTime(long currentTime) {
	   downloadTime = (currentTime - beginTime)/1000.0;
   }

   public static int getDownloaded() {
	   return downloaded;
   }
   
   public static int getUploaded(){
	   return uploaded;
   }
   
   public static Piece getVerifiedPiece(int index){
	   return verifiedPieces.get(index);
   }
   
   public static TorrentInfo getTorrentData(){
	   return torrentData;
   }

   public static int getPieceLength() {
	   return pieceLength;
   }

   public static int getLastPieceLength() {
	   return lastPieceLength;
   }

   public static List<Peer> getConnectedPeers() {
	   return connectedPeers;
   }

   public static double getDownloadTime() {
	   return downloadTime;
   }

   public static String getfName() {
	   return fName;
   }

   public static byte[] getClientID() {
	   return clientID;
   }
   
   public static int getInterval(){
	   return interval;
   }
    
}/*end of RUBTClient class*/
