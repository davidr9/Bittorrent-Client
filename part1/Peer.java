
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author Julie Duncan
 * @author David Rubin
 * @author Rosheen Chaudhry
 */

public class Peer {

	byte[] peerID; /*the id of this peer*/
	
	private String IPAddress; /*the IP address of this peer as a String*/
	
	private int portNum; /*the port number of this peer*/
	
	final byte[] clientID; /*the peer ID of the local client*/
	
	private final byte[] info_hash; /*the info hash of the torrent that this peer is using to communicate*/
	
	private Socket peerSocket; /*the socket connecting the local client to this peer*/
	
	private DataOutputStream outputStream; /*stream for outgoing data to peer*/
	
	private DataInputStream inputStream; /*stream for incoming data to peer*/
	
	private boolean hand_shook = false; /*true if handshake with peer is completes successfully*/
	
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
	
	public String getIP(){
		return this.IPAddress;
	}
	
	public int getPort(){
		return this.portNum;
	}
	
	/*
	 * Open a TCP socket on the local machine to contact the peer using the BT peer protocol
	 */
	public boolean connectToPeer(){
		try {
			this.peerSocket = new Socket(this.IPAddress, this.portNum);
			this.outputStream = new DataOutputStream(this.peerSocket.getOutputStream()); // open output stream for outgoing data
			System.out.println("Data output stream for " + RUBTClient.escape(this.peerID) + " opened...");
			this.inputStream = new DataInputStream(this.peerSocket.getInputStream()); //open input stream for incoming data
			System.out.println("Data input stream for " + RUBTClient.escape(this.peerID) + " opened...");
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
	
	/* 
	 * Handshake
	 * Return -1 if handshake is unsuccessful for any reason
	 * Returns 0 if successful 
	 */
	public int shakeHand() throws IOException, SocketException{
		
		if(this.hand_shook){
			/* This means handshake already happened. */
			System.err.append("Handshake already made. Please move on.");
			return -1;
		}
		
		if(this.clientID == null){
			/* Missing clientID */
			System.err.println("ClientID is missing; could not complete handshake");
			peerSocket.close();
			return -1;
		}
		
		/*open the socket*/
		if(!this.connectToPeer()){
			/* Connection unsuccessful*/
			System.err.println("Peer connection unsuccesful. Cannot complete handshake");
			peerSocket.close();
			return -1;
		}
		
		System.out.println("All of our conditions have been met. Beginning handshaking");
		
		String infoHashString = "";
		
		infoHashString = RUBTClient.escape(this.info_hash);
		
		
		System.out.println("PEER (" + RUBTClient.escape(this.peerID) + "): INFO HASH: " + infoHashString);
		
		/* Create an array to store handshake data */
		byte[] handshake_info = Message.generateHandShake(this.info_hash, this.clientID);
		/* initiate handshake */
		try {
			this.peerSocket.setSoTimeout(12000); /*wait a given time for success*/
			this.outputStream.write(handshake_info);
			this.outputStream.flush();
			System.out.println("HANDSHAKE SENT");
			
		} catch (SocketException e) {
			System.err.println("TIMEOUT");
			e.printStackTrace();
			peerSocket.close();
			return -1;
		} catch (IOException e) {
			System.err.println("COULD NOT WRITE HANDSHAKE INFO");
			e.printStackTrace();
			peerSocket.close();
			return -1;
		}
		
		/* Receive message from client */
		byte[] complete_handshake = new byte[68];
		try {
			this.inputStream.readFully(complete_handshake);
			this.peerSocket.setSoTimeout(130000);
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.err.print("COULD NOT READ HANDSHAKE");
			e.printStackTrace();
			this.peerSocket.close();
			return -1;
		}		
	    
	    /*Check if info hash matches*/
	    for (int i = 28; i < 48; i++){
	      if (handshake_info[i] != complete_handshake[i]) {
	        System.err.println("INFO HASH ID NOT MATCH");
	        peerSocket.close();
	        return -1;
	      }
	    }
		
		/*Check if peerID matches*/
		if (this.peerID != null && this.peerID.length == 20) {
	      byte[] peer_id_array = this.peerID;
	      for (int i = 48; i < 68; i++)
	        if (complete_handshake[i] != peer_id_array[i - 48]) {
	          System.err.println("PEERID DID NOT MATCH");
	          peerSocket.close();
	          return -1;
	        }
	      
		} else if (this.peerID != null && this.peerID.length != 20) {
	      System.err.println("PEERID LENGTH IS INCORRECT");
	      this.peerID = new byte[20];
	      peerSocket.close();
	      /*System.arraycopy(complete_handshake, 48, this.peerID, 0, 20);*/
	      return -1;

	    } else {
	    	System.err.println("NO PEER ID ");
	    	peerSocket.close();
	    }

		System.out.println("HANDSHAKE COMPLETE");
		
		return 0;
	}
}
