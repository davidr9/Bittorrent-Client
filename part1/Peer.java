
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

public class Peer {

	byte[] peerID; //the id of this peer
	
	private String IPAddress; //the IP address of this peer as a String
	
	private int portNum; //the port number of this peer
	
	final byte[] clientID; //the peer ID of the local client
	
	private final byte[] info_hash; //the info hash of the torrent that this peer is using to communicate
	
	private Socket peerSocket; //the socket connecting the local client to this peer
	
	private DataOutputStream outputStream;
	
	private DataInputStream inputStream;
	
	/* This is initially set to false, but changes to true when the handshake is complete.*/
	private boolean hand_shook = false;
	
	/**
	 * Creates a new peer object. Actual parameters are yet to be determined
	 * @param args
	 */
	public Peer (byte[] id, String ip, int port, byte[] cid, byte[] info_hash) throws UnknownHostException, IOException{
		this.peerID = id;
		this.portNum = port;
		this.IPAddress = ip;	
		this.peerSocket = null;
		this.info_hash = info_hash;
		this.clientID = cid;
		
	}/*end of Peer class constructor*/
	
	/* Handshake
	 * Return -1 if handshake already happened.
	 * Returns 0 if successful 
	 * */
	public int shakeHand(){
		
		if(this.hand_shook){
			/* This means handshake already happened. */
			System.err.append("Handshake already happened. Please move on.");
			return -1;
		}
		
		if(this.clientID == null){
			/* Missing clientID */
			System.err.println("ClientID is missing; could not complete handshake");
			return -1;
		}
		
		if(!this.connectToPeer()){
			/* Connection unsuccessful*/
			System.err.println("Peer connection unsuccesful. Cannot complete handshake");
			return -1;
		}
		
		System.out.println("All of our conditions have been met. Beginning handshaking");
		
		String infoHashString = "";
		
		
		for(int i=0; i<info_hash.length; i++){
			infoHashString = infoHashString + "" + (Integer.toHexString((this.info_hash[i] & 0xFF))+ " ");
		}
		
		System.out.println("PEER (" + this.toString() + "): INFO HASH: \"" + infoHashString.toString() + "\".");
		
		/* Create a handshake array */
		//Message hs = new Message(null, null);
		byte[] handshake_info = Message.generateHandShake(this.info_hash, this.clientID);
		/* now shake the hand */
		try {
			this.peerSocket.setSoTimeout(12000);
			this.outputStream.write(handshake_info);
			this.outputStream.flush();
			System.out.println("HANDSHAKE SENT");
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.err.println("TIMEOUT");
			e.printStackTrace();
			/* Should disconnect .*/
			return -1;
		} catch (IOException e) {
			System.err.println("COULD NOT WRITE HANDSHAKE INFO");
			e.printStackTrace();
			/* Should disconnect */
			return -1;
		}
		
		/* Receive message from client */
		byte[] complete_handshake = new byte[68];
		try {
			this.inputStream.readFully(complete_handshake);
			this.peerSocket.setSoTimeout(130000);
		} catch (IOException e) {
			System.err.print("COULD NOT READ HANDSHAKE");
			e.printStackTrace();
			/* Should disconnect here. */
			return -1;
		}		
		
		infoHashString = "";
		for(int i = 0; i<info_hash.length; i++){
			infoHashString = infoHashString + (Integer.toHexString((complete_handshake[i + 28] & 0xFF)) + " ");
		}
		
	    System.out.println("Peer (" + this.toString() + "): Received info hash: \"" + infoHashString.toString() + "\".");

	    System.out.println("Peer (" + this.toString() + "): Received handshake.");
	    
	    /*Check if info hash matches*/
	    for (int i = 28; i < 48; i++)
	      if (handshake_info[i] != complete_handshake[i]) {
	        System.err.println("INFO HASH ID NOT MATCH");
	        return -1;
	      }
		System.out.println("INFO HASH MATCHES!!!");
	    
		
		/*Check if peerID matches*/
		if (this.peerID != null && this.peerID.length == 20) {
	      byte[] peer_id_array = this.peerID;
	      for (int i = 48; i < 68; i++)
	        if (complete_handshake[i] != peer_id_array[i - 48]) {
	          System.err.println("PEERID DID NOT MATCH");
	          return -1;
	        }

	   System.out.println("PEER ID MATCHED");
	      
		} else if (this.peerID != null && this.peerID.length != 20) {
	      System.err.println("PEERID LENGTH IS INCORRECT");
	      this.peerID = new byte[20];
	      //System.arraycopy(complete_handshake, 48, this.peerID, 0, 20);
	      return -1;

	    } else {
	    	/*
	      log.debug("Peer (" + this.toString()
	          + "): No peer ID available (incoming connection?).");
	      this.peerID = new byte[20];
	      System.arraycopy(received_handshake, 48, this.peerID, 0, 20);

	      log.debug("Peer (" + this.toString() + "): Set peer ID to \""
	          + new String(this.peerID) + "\".");
	          */
	    	System.err.println("NO PEER ID ");
	    }

		System.out.println("HANDSHAKE COMPLETE");
		
		return 0;
	}
	
	public String getIP(){
		return this.IPAddress;
	}
	
	public int getPort(){
		return this.portNum;
	}
	
	/*
	 * Open a TCP socket on the local machine and contact the peer using the BT peer protocol and request
	 *  a piece of the file.
	 */
	public boolean connectToPeer(){
		try {
			this.peerSocket = new Socket(this.IPAddress, this.portNum);
			this.outputStream = new DataOutputStream(this.peerSocket.getOutputStream()); // open output stream for outgoing data
			System.out.println("Data output stream to " + this.peerID + " opened...");
			this.inputStream = new DataInputStream(this.peerSocket.getInputStream()); //open input stream for incoming data
			System.out.println("Data input stream to " + this.peerID + " opened...");
			return true;
		}catch (UnknownHostException e) { //catch error for incorrect host name and exit program
	            System.out.println("Peer " + peerID + " is unknown");
	            return false;
	    } catch (IOException e) { //catch error for invalid port and exit program
	            System.out.println("Connection to " + peerID + " failed");
	            return false;
	    }  
	}/*end of connectToPeer method*/
	
	/*
	 * @params
	 */
	public void requestPiecesFromPeer() throws EOFException, IOException{
		return;
	}
}
