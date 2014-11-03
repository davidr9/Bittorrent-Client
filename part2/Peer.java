
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.awt.event.*;

/**
 * @author Julie Duncan
 * @author David Rubin
 * @author Rosheen Chaudhry
 */

public class Peer extends Thread implements Runnable{

	byte[] peerID; /*the id of this peer*/
	
	private String IPAddress; /*the IP address of this peer as a String*/
	
	private int portNum; /*the port number of this peer*/
	
	final byte[] clientID; /*the peer ID of the local client*/
	
	private final byte[] info_hash; /*the info hash of the torrent that this peer is using to communicate*/
	
	private Socket peerSocket; /*the socket connecting the local client to this peer*/
	
	private DataOutputStream outputStream; /*stream for outgoing data to peer*/
	
	private DataInputStream inputStream; /*stream for incoming data to peer*/
	
	private boolean hand_shook = false; /*true if handshake with peer is completes successfully*/
	
	private boolean unchoked; /*true if peer has un-choked the host client*/
	
	private boolean isInterested = false; /*true if peer is interested in downloading from client*/
	
	private long lastMessageSent;
	
	private long beginTime;
	
	public Thread th; /*thread to run this peer*/
	
	private String tName; /*name of thread for this peer*/
	
	final int BLOCKSIZE = 16384; /*generally accepted block size is 2^14*/
	
	public static int pieceLength = RUBTClient.torrentData.piece_length;
	
	private static Timer peerTimer = new Timer(true);
	
	volatile boolean finished = false;
	
	/**
	 * Creates a new peer object
	 * 
	 * @param id byte array containing id of peer
	 * @param ip IP address of peer
	 * @param port port number through which to communicate with peer
	 * @param cid ID of local client host
	 * @param info_hash info hash being used to communicate with peer
	 */
	public Peer (byte[] id, String ip, int port, byte[] cid, byte[] info_hash) throws UnknownHostException, IOException{
		this.peerID = id;
		this.portNum = port;
		this.IPAddress = ip;	
		this.peerSocket = null;
		this.info_hash = info_hash;
		this.clientID = cid;
		
	}/*end of Peer class constructor*/
	
	/*
	 * Find private variable IP address
	 * 
	 * @return IP address of peer as string
	 */
	public String getIP(){
		return this.IPAddress;
	}
	
	/*
	 * Find private variable port number
	 * 
	 * @return port number as integer
	 */
	public int getPort(){
		return this.portNum;
	}
	
	/*
	 * Open a TCP socket on the local machine to contact the peer using the TCP peer protocol
	 * 
	 * @return true if success, false otherwise
	 */
	public boolean openSocket(){
		try {
			this.peerSocket = new Socket(this.IPAddress, this.portNum);
			this.outputStream = new DataOutputStream(this.peerSocket.getOutputStream()); // open output stream for outgoing data
			System.out.println("Data output stream for " + this.IPAddress + " opened...");
			this.inputStream = new DataInputStream(this.peerSocket.getInputStream()); //open input stream for incoming data
			System.out.println("Data input stream for " + this.IPAddress + " opened...");
			return true;
		}catch (UnknownHostException e) { //catch error for incorrect host name and exit program
	            System.out.println("Peer " + peerID + " is unknown");
	            return false;
	    } catch (IOException e) { //catch error for invalid port and exit program
	            System.out.println("Connection to " + peerID + " failed");
	            return false;
	    }  
	}/*end of openSocket method*/

	/* 
	 * This method sends a Handshake to the peer. It checks the peers response for
	 * a matching peerID and info hash
	 * 
	 * @throws IOException and SocketException if error is detected
	 * @return 0 if successful 
	 * @return -1 if handshake is unsuccessful for any reason
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
			return -1;
		}
		
		/*open the socket*/
		if(!this.openSocket()){
			/* Connection unsuccessful*/
			System.err.println("Peer connection unsuccesful. Cannot complete handshake");
			this.peerSocket.close();
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
			this.peerSocket.close();
			return -1;
		} catch (IOException e) {
			System.err.println("COULD NOT WRITE HANDSHAKE INFO");
			e.printStackTrace();
			this.peerSocket.close();
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
	        System.err.println("INFO HASH DID NOT MATCH");
	        this.peerSocket.close();
	        return -1;
	      }
	    }
		
		/*Check if peerID matches*/
		if (this.peerID != null && this.peerID.length == 20) {
	      byte[] peer_id_array = this.peerID;
	      for (int i = 48; i < 68; i++)
	        if (complete_handshake[i] != peer_id_array[i - 48]) {
	          System.err.println("PEERID DID NOT MATCH");
	          this.peerSocket.close();
	          return -1;
	        }
	      
		} else if (this.peerID != null && this.peerID.length != 20) {
	      System.err.println("PEERID LENGTH IS INCORRECT");
	      this.peerID = new byte[20];
	      this.peerSocket.close();
	      /*System.arraycopy(complete_handshake, 48, this.peerID, 0, 20);*/
	      return -1;

	    } else {
	    	System.err.println("NO PEER ID ");
	    	this.peerSocket.close();
	    }

		System.out.println("HANDSHAKE COMPLETE");
		
		return 0;
	}
	
	/*
	 * Downloads all pieces from this peer that we have not already downloaded.
	 */
	private void downloadPieces() throws EOFException, IOException, NoSuchAlgorithmException{
		
		boolean readSuccessfully = false; /*true if peer successfully reads sent message*/
		boolean[] whichPieces = null; /*stores which pieces the peer has verified and can send to the client (index corresponds to 0-based piece index)*/

		while (!finished){
			Message interested = new Message(1, (byte) 2);
			readSuccessfully = Message.writeMessage(outputStream, interested);
			lastMessageSent = System.currentTimeMillis();
			
			if (!readSuccessfully){
				continue;
			}
			
			Message peerResponse = Message.readMessage(inputStream);
			System.out.println("len is: " + peerResponse.length + " & peer response message id is: " + peerResponse.message_id);
			
			if (peerResponse.message_id == Message.bitfield){
				BitfieldMessage bitResponse = (BitfieldMessage) peerResponse;
				byte[] bitfield = bitResponse.getPieces();
				whichPieces = RUBTClient.convertBitfield(bitfield, bitfield.length * 8);				
			}
			
			if (peerResponse.message_id == Message.unchoke){
				unchoked = true;
			}
			
			while (unchoked){
				
				for (int i = 0; i < RUBTClient.numPieces; i++){
					
					if (RUBTClient.verifiedPieces.get(i) != null || whichPieces[i] == false){
						System.out.println(RUBTClient.verifiedPieces.get(i).fullPiece.length);
						continue;
					}
					
					Piece completePiece = requestBlocks(i);
					if (completePiece != null){
						RUBTClient.verifiedPieces.set(i, completePiece);
						RUBTClient.numPiecesVerified++;
						RUBTClient.downloaded += completePiece.fullPiece.length;
						RUBTClient.left -= completePiece.fullPiece.length;
						System.out.println("Num Pieces verified: " + RUBTClient.numPiecesVerified);
						
						if (RUBTClient.numPiecesVerified == RUBTClient.numPieces){
							RUBTClient.event = "completed";
							System.out.println("ALL THE PIECES HAVE BEEN VERIFIED HOOOOORAYYYY");
							finished = true;
							Message uninterested = new Message(1, (byte) 3);
							readSuccessfully = Message.writeMessage(outputStream, uninterested);
							lastMessageSent = System.currentTimeMillis();
							break;
						}
					}
					
				}/*end of for*/
				
				peerResponse = Message.readMessage(inputStream);
				System.out.println("len is: " + peerResponse.length + " & peer response message id is: " + peerResponse.message_id);
				if (peerResponse.message_id == Message.choke){
					unchoked = false;
				}

			}/*end of while*/

		}/*end of infinite while loop*/
		
	}/*end of downloadPieces*/
	
	public Piece requestBlocks(int piece) throws IOException, NoSuchAlgorithmException{
		
		boolean readSuccessfully;
		boolean pieceVerified = false;
		Piece newPiece = new Piece(piece);
		
		/*sending a request for each block to the peer*/
		for (int i = 0; i < newPiece.totalBlocks; i++){
			int offset = BLOCKSIZE*i;
			
			RequestMessage rmessage;
			
			if (newPiece.lastPiece && i == newPiece.totalBlocks - 1){
				rmessage = new RequestMessage(piece, offset, newPiece.lastBLOCKSIZE);
			} else {
				rmessage = new RequestMessage(piece, offset, BLOCKSIZE);
			}
			
			readSuccessfully = Message.writeMessage(outputStream, rmessage);
			lastMessageSent = System.currentTimeMillis();
			
			if (!readSuccessfully){
				System.err.println("Could not read block from piece " + piece);
				i--;
				continue;
			}
			
			/*if we were able to read the peer response properly, we check what message it is*/
			Message peerResponse = Message.readMessage(inputStream);
			System.out.println("len is: " + peerResponse.length + " & peer response message id is: " + peerResponse.message_id);
			
			/*checks the message after the peer response*/
			if (peerResponse.message_id == Message.piece){
				
				PieceMessage peerPiece = (PieceMessage) peerResponse;
				
				if (peerPiece.getPieceIndex() != piece){
					System.err.println("Peer responded with block from unrequested piece");
					i--;
					continue;
				} else if (peerPiece.getBeginningOfBlock() != offset){
					System.err.println("Peer responded wrong block from requested piece");
					i--;
					continue;
				} else {
					pieceVerified = newPiece.insertBlock(offset, peerPiece.block);
				}
				
			} else if (peerResponse.message_id == Message.choke){
				unchoked = false;
				return null;
			} else {
				return null;
			}	
		}/*end of for*/
		
		if (pieceVerified){
			return newPiece;
		} else {
			return null;
		}
		
	} /*end of requestBlocks*/
	
	/*
	 * Creates new thread for Runnable Peer and starts it
	 * 
	 * @throws UnsupportedEncodingException for escape 
	 */
	public void startThread() throws UnsupportedEncodingException{
		
		if (th == null){
			tName = RUBTClient.escape(peerID);
			th = new Thread(this, tName);
			th.start();
		}
	}
	
	/*
	 * Run function for peer threads.
	 * 
	 * Creates new thread with peerID as thread name. Begins downloading pieces from
	 * thread, then closes socket and all streams when finished.
	 */
	
	@Override
	public void run(){
		
		
		System.out.println("Thread " + tName + " has begun running");
		int connected;
		
		try {	
			
			connected = shakeHand();
			if (connected == -1){
				System.err.print("Could not connect with peer " + IPAddress);
				System.err.println(". Some pieces of the file to download may be lost.");
			} else {
				peerTimer.schedule(new KeepAlive(), System.currentTimeMillis(), 120000);
				downloadPieces();
			}
			
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
			return;
		} catch (EOFException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		try {
			inputStream.close();
			outputStream.close();
			peerSocket.close();
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	} /*end of run*/
	
	/*specific timer task subclass used to send keep-alive message to peer every two minutes*/
	class KeepAlive extends TimerTask {
		public void run(){
			
			System.out.println("Sending keep-alive message to peer at " + IPAddress);
			Message kaMessage = new Message(0, (byte) -1);
			try {
				Message.writeMessage(outputStream, kaMessage);
			} catch (IOException e) {
				System.err.println("Peer at " + IPAddress + " could not read Keep-Alive message");
			}
		}
	}/*end of KeepAlive class*/
	
	
}
