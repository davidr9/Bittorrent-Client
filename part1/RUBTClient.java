import java.io.*;
import java.net.*;
import java.net.URL.*;
import java.security.*;
import java.util.ArrayList;
import java.nio.*;

/**
 * @author Julie Duncan
 * @author David Rubin
 * @author Rosheen Chaudhry
 */

public class RUBTClient {

	public static byte[] clientID = "davidrrosheencjulied".getBytes(); /*string of length 20 used as local host's ID*/ 
	public static TorrentInfo torrentData = null; /*contains parsed data from torrent*/

	public static void main(String[] args) throws UnknownHostException, IOException, NullPointerException, BencodingException {

		/*Error handling when user enters incorrect number of arguments*/
		if (args.length != 2)
		{
			System.out.println("Correct Usage: RUBTClient <.torrent file name> <ouptut file name>");
			return;
		}
		String inFileName = args[0]; /*torrent file*/
		String outFileName = args[1]; /*file to output successful download to*/
		int connected = -1; /*0 if connected to peer successfully, -1 otherwise*/
		int i = 0;

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



		//This is wrong
		ArrayList<Peer> peers = new ArrayList<Peer>(); /*List of peers received from the tracker*/
		peers = sendRequestToTracker(torrentData);
		Peer singlePeer = null; /*for communication with one peer*/

		/*attempts connection with peers in the list until a successful handshake is made*/
		while (connected != 0){
			if(peers.size() == 0)
			{
				System.out.println("There are no peers in the arraylist of peers");
			}
			singlePeer = peers.get(i);
			if (singlePeer != null){
				connected = singlePeer.shakeHand(); /*attempt handshake with peer*/
			}
			i++;
			/*if end of peer list is reached, exit*/
			if (i == peers.size()){ 
				System.err.println("No valid peer found");
				return;
			}
		}

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
				ArrayList<Peer> test = new ArrayList<Peer>();
				test = findPeerList(trackerResponse);

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
			TrackerResponseInfo trackerData = new TrackerResponseInfo(tracker_response); /*create object from tracker*/
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
