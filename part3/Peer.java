
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * @author Julie Duncan
 * @author David Rubin
 * @author Rosheen Chaudhry
 */
public class Peer implements Runnable {

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

    private long lastMessageSent; /*keeps track of the last message sent from peer*/

    public Thread th; /*thread to run this peer*/

    private String tName; /*name of thread for this peer*/

    private boolean peerIsUnchoked = false;

    final int BLOCKSIZE = 16384; /*generally accepted block size is 2^14*/

    public static int pieceLength = RUBTClient.torrentData.piece_length; /*length of a piece in the info_hash*/

    private static Timer peerTimer = new Timer(true); /*Timer object used to keep track of the time*/

    volatile boolean finished = false; /*event that is updated when all pieces have been downloaded*/

    boolean[] whichPieces = null;    /*stores which pieces the peer has verified and can send to the client (index corresponds to 0-based piece index)*/


    /**
     * Creates a new peer object
     *
     * @param id byte array containing id of peer
     * @param ip IP address of peer
     * @param port port number through which to communicate with peer
     * @param cid ID of local client host
     * @param info_hash info hash being used to communicate with peer
     */
    public Peer(byte[] id, String ip, int port, byte[] cid, byte[] info_hash) throws UnknownHostException, IOException {
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

    public String getIP() {
        return this.IPAddress;
    }

    /*
     * Find private variable port number
     * 
     * @return port number as integer
     */
    public int getPort() {
        return this.portNum;
    }

    /*
     * Open a TCP socket on the local machine to contact the peer using the TCP peer protocol
     * 
     * @return true if success, false otherwise
     */
    public boolean openSocket() {
        try {
            this.peerSocket = new Socket(this.IPAddress, this.portNum);
            this.outputStream = new DataOutputStream(this.peerSocket.getOutputStream()); // open output stream for outgoing data
            System.out.println("Data output stream for " + this.IPAddress + " opened...");
            this.inputStream = new DataInputStream(this.peerSocket.getInputStream()); //open input stream for incoming data
            System.out.println("Data input stream for " + this.IPAddress + " opened...");
            return true;
        } catch (UnknownHostException e) { //catch error for incorrect host name and exit program
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

    public int shakeHand() throws IOException, SocketException {

        if (this.hand_shook) {
            /* This means handshake already happened. */
            System.err.append("Handshake already made. Please move on.");
            return -1;
        }

        if (this.clientID == null) {
            /* Missing clientID */
            System.err.println("ClientID is missing; could not complete handshake");
            return -1;
        }

        /*open the socket*/
        if (!this.openSocket()) {
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
        for (int i = 28; i < 48; i++) {
            if (handshake_info[i] != complete_handshake[i]) {
                System.err.println("INFO HASH DID NOT MATCH");
                this.peerSocket.close();
                return -1;
            }
        }

        /*Check if peerID matches*/
        if (this.peerID != null && this.peerID.length == 20) {
            byte[] peer_id_array = this.peerID;
            for (int i = 48; i < 68; i++) {
                if (complete_handshake[i] != peer_id_array[i - 48]) {
                    System.err.println("PEERID DID NOT MATCH");
                    this.peerSocket.close();
                    return -1;
                }
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
    private void downloadPieces() throws EOFException, IOException, NoSuchAlgorithmException {

        boolean readSuccessfully = false; /*true if peer successfully reads sent message*;

         /*loop continues until all the pieces have been downloaded or user exits the program*/

        while (!finished) {
            /*sending the initial interested message to the peers*/
            Message interested = new Message(1, (byte) 2);
            readSuccessfully = Message.writeMessage(outputStream, interested);
            lastMessageSent = System.currentTimeMillis();

            if (!readSuccessfully) {
                continue;
            }

            /*stores the response of the peer after the client sends a message*/
            Message peerResponse;

            System.out.println("CALLING broadcastToPeer");
            broadcastToPeer();

            /*numPieces = the torrent file length / piece length. 100 / 10 = 10 pieces made of many blocks*/
            /*downloads pieces of the file while the peer is unchoked*/
            while (unchoked) {

                for (int i = 0; i < RUBTClient.numPieces; i++) {

                    /*continues if we have downloaded the piece already or peer does not have that piece*/
                    if (RUBTClient.verifiedPieces.get(i) != null || whichPieces[i] == false) {
                        System.out.println(RUBTClient.verifiedPieces.get(i).fullPiece.length);
                        continue;
                    }

                    /*creating pieces by inserting blocks into a Piece object until complete*/
                    Piece completePiece = requestBlocks(i);
                    if (!unchoked) {
                        break;
                    }
                    if (completePiece != null) {
                        RUBTClient.verifiedPieces.set(i, completePiece);
                        RUBTClient.numPiecesVerified++;
                        RUBTClient.downloaded += completePiece.fullPiece.length;
                        RUBTClient.left -= completePiece.fullPiece.length;
                        RUBTClient.updateClientPieces(i);
                        System.out.println("Num Pieces verified: " + RUBTClient.numPiecesVerified);

                        if (RUBTClient.numPiecesVerified == RUBTClient.numPieces) {
                            RUBTClient.downloadTime = (System.currentTimeMillis() - RUBTClient.beginTime);
                            RUBTClient.publishToTracker("completed");
                            System.out.println("ALL THE PIECES HAVE BEEN VERIFIED");
                            finished = true;
                            Message uninterested = new Message(1, (byte) 3);
                            readSuccessfully = Message.writeMessage(outputStream, uninterested);
                            lastMessageSent = System.currentTimeMillis();
                            break;
                        }
                    }

                }/*end of for*/

                if (!unchoked) {
                    continue;
                }
                peerResponse = Message.readMessage(inputStream);
                System.out.println("len is: " + peerResponse.length + " & peer response message id is: " + peerResponse.message_id);
                if (peerResponse.message_id == Message.choke) {
                    unchoked = false;
                }

            }/*end of inner while*/

        }/*end of outer while loop*/

        //uploadOnly();

    }/*end of downloadPieces*/


    public void broadcastToPeer() throws IOException {
        System.out.println("IN BROADCAST TO PEER METHOD");
        Message peerResponse = Message.readMessage(inputStream);
        System.out.println("len is: " + peerResponse.length + " & peer response message id is: " + peerResponse.message_id);

        /*store bitfield response from peer*/
        if (peerResponse.message_id == Message.bitfield) {
            BitfieldMessage bitResponse = (BitfieldMessage) peerResponse;
            byte[] bitfield = bitResponse.getPieces();
            whichPieces = RUBTClient.convertBitfield(bitfield, bitfield.length * 8);
        } else if (peerResponse.message_id == Message.unchoke) {
            unchoked = true;
        } else if (peerResponse.message_id == Message.interested) {
            //send the peer that sent us an interested message the client's bitfield and an unchoke message
            peerIsUnchoked = true;
            byte[] clientBitfield = RUBTClient.getClientBitfield();
            int len = clientBitfield.length;
            BitfieldMessage cBitfield = new BitfieldMessage(len, clientBitfield);
            Message.writeMessage(outputStream, cBitfield);
            System.out.println("SENT BITFIELD MESSAGE TO PEER");
            Message unchokePeer = new Message(1, (byte) 1);
            Message.writeMessage(outputStream, unchokePeer);
            System.out.println("SENT UNCHOKE MESSAGE TO PEER");
        } else if (peerResponse.message_id == Message.request) {
            System.out.println("WE RECIEVED THE REQUEST FROM PEER");
            uploadPiece((RequestMessage) peerResponse);
        } else if (peerResponse.message_id == Message.keep_alive) {
            System.out.println("WE RECIEVED KEEP ALIVE MESSAGE FROM PEER");
        } else if (peerResponse.message_id == Message.have) {
            System.out.println("CLIENT HAS RECIVED HAVE MESSAGE");
        } else {
            System.out.println("DID NOT RECIEVE ANY RELEVANT MESSAGES FROM THE PEER");
        }
    }
    /*Method will continue to upload currently downloaded Pieces after all the Pieces have been downloaded*/

    private void uploadOnly() throws IOException {
        Message peerResponse = Message.readMessage(inputStream);

        /*Client should keep uploading until the user enters q*/
        while (true) {
            System.out.println("ATTEMPTING TO UPLOAD NOW");
            broadcastToPeer();

        }

    }/*end of uploadOnly*/


    /**
     * Sends a request message for blocks within the specified piece.
     *
     * @param piece
     * @return
     * @throws IOException
     */
    public Piece requestBlocks(int piece) throws IOException, NoSuchAlgorithmException {

        boolean readSuccessfully;
        boolean pieceVerified = false;
        Piece newPiece = new Piece(piece);

        /*totalBlocks = length of piece / blocksize*/
        /*sending a request for each block to the peer*/
        for (int i = 0; i < newPiece.totalBlocks; i++) {
            int offset = BLOCKSIZE * i; /*gets index of block*/

            RequestMessage rmessage;

            if (newPiece.lastPiece && i == newPiece.totalBlocks - 1) {
                /*index of piece, index of block, size of block*/
                rmessage = new RequestMessage(piece, offset, newPiece.lastBLOCKSIZE);
            } else {
                rmessage = new RequestMessage(piece, offset, BLOCKSIZE);
            }

            /*sends the request message to the peer and returns true if message was read*/
            readSuccessfully = Message.writeMessage(outputStream, rmessage);
            lastMessageSent = System.currentTimeMillis();

            if (!readSuccessfully) {
                System.err.println("Could not read block from piece " + piece);
                i--;
                continue;
            }

            /*if we were able to read the peer response properly, we check what message it is*/
            Message peerResponse = Message.readMessage(inputStream);
            System.out.println("len is: " + peerResponse.length + " & peer response message id is: " + peerResponse.message_id);

            /*checks the message after the peer response*/
            if (peerResponse.message_id == Message.piece) {
                /*PieceMessage should include the block requested*/
                PieceMessage peerPiece = (PieceMessage) peerResponse;

                if (peerPiece.getPieceIndex() != piece) {
                    System.err.println("Peer responded with block from unrequested piece");
                    i--;
                    continue;
                } else if (peerPiece.getBeginningOfBlock() != offset) {
                    System.err.println("Peer responded wrong block from requested piece");
                    i--;
                    continue;
                } else {/*inserts the block requested into the Piece*/

                    pieceVerified = newPiece.insertBlock(offset, peerPiece.block);
                }

            } else if (peerResponse.message_id == Message.choke) {
                unchoked = false;
                return null;
            } else {
                return null;
            }
        }/*end of for*/

        if (pieceVerified) {
            return newPiece;
        } else {
            return null;
        }

    } /*end of requestBlocks*/

    /*method uploads the piece message requested by the peer*/

    public void uploadPiece(RequestMessage peerResponse) throws IOException {
        System.out.println("IN UPLOAD PIECE");
        int size = peerResponse.block_length;
        int piece_index = peerResponse.getPieceIndex();
        int begin = peerResponse.getBeginningOfBlock();
        byte[] requestedBlock = new byte[size];
        byte[] pieceInfo = RUBTClient.verifiedPieces.get(piece_index).fullPiece;
        System.arraycopy(pieceInfo, begin, requestedBlock, 0, size);
        PieceMessage peerPiece = new PieceMessage(piece_index, begin, requestedBlock);

        if (Message.writeMessage(outputStream, peerPiece)) {
            System.out.println("BLOCKS UPLOADED: " + size);
            RUBTClient.updateUploaded(size);
        }
    }/*end of uploadPieces*/

    /*
     * Creates new thread for Runnable Peer and starts it
     * 
     * @throws UnsupportedEncodingException for escape 
     */

    public void startThread() throws UnsupportedEncodingException {

        if (th == null) {
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
    public void run() {

        System.out.println("Thread " + tName + " has begun running");
        int connected;

        try {

            connected = shakeHand();
            if (connected == -1) {
                System.err.print("Could not connect with peer " + IPAddress);
                System.err.println(". Some pieces of the file to download may be lost.");
            } else {
                peerTimer.schedule(new KeepAlive(), 120000, 120000);
                downloadPieces();
                RUBTClient.writeToDisk(RUBTClient.fName);
                System.out.println("FINISHED WRITING TO DISK NOW CALLING UPLOAD ONLY");
                uploadOnly();
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
            peerTimer.purge();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    } /*end of run*/

    /**
     * specific timer task subclass used to send keep-alive message to peer
     * every two minutes
     */
    class KeepAlive extends TimerTask {

        public void run() {

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
