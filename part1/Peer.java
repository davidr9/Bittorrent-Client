
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
	
	private Socket socket; //the socket connecting the local client to this peer
	
	/**
	 * Creates a new peer object. Actual parameters are yet to be determined
	 * @param args
	 */
	public Peer (byte[] id, String ip, int port) throws UnknownHostException, IOException{
		this.peerID = id;
		this.portNum = port;
		this.IPAddress = ip;	
		this.socket = new Socket(ip, port);
		this.info_hash = null;
		this.clientID = null;
		
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
	public boolean requestPieceFromPeer(){
		return false;
	}
}
