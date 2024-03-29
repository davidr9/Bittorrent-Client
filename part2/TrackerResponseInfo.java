import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * @author Julie Duncan
 * @author David Rubin
 * @author Rosheen Chaudhry
 */

/*Similar to the TorrentInfo class, but for the tracker response*/
public class TrackerResponseInfo {

    /*byte array of tracker response*/
    public byte[] tracker_file_bytes;

    /*hashmp of key/value pairs of tracker response*/
    public Map<ByteBuffer, Object> tracker_file_map;

    /*time that the client should wait between sending requests to the tracker (sec)*/
    public int interval;

    /*A string that the client should send back on it's next announcements*/
    public String trackerid;
    
    /*Response from tracker. Can be stopped, completed, or started*/
    public String event; 

    /*Array list of Peer objects. Contains the peerid, port, and ip for each*/
    public ArrayList<Peer> peers;

    /*Keys to access the hash map. All of these keys should be in the tracker response*/
    public final static ByteBuffer INTERVAL = ByteBuffer.wrap(new byte[]{'i', 'n', 't', 'e', 'r', 'v', 'a', 'l'});

    public final static ByteBuffer FAILURE_REASON = ByteBuffer.wrap(new byte[]{'f', 'a', 'i', 'l', 'u', 'r', 'e'});

    public final static ByteBuffer TRACKER_ID = ByteBuffer.wrap(new byte[]{'t', 'r', 'a', 'c', 'k', 'e', 'r', 'i', 'd'});

    public final ByteBuffer PEERS = ByteBuffer.wrap(new byte[]{'p', 'e', 'e', 'r', 's'});

    public final static ByteBuffer PEERID = ByteBuffer.wrap(new byte[]{'p', 'e', 'e', 'r', ' ', 'i', 'd'});

    public final static ByteBuffer IP = ByteBuffer.wrap(new byte[]{'i', 'p'});

    public final static ByteBuffer PORT = ByteBuffer.wrap(new byte[]{'p', 'o', 'r', 't'});
    
    public final static ByteBuffer EVENT = ByteBuffer.wrap(new byte[]{'e', 'v', 'e', 'n', 't'});

    /*TrackerResponseInfo constructor
     * @param = tracker data in byte array
     */
    public TrackerResponseInfo(byte[] tracker_file_bytes) throws BencodingException, IOException {
        /*checks if tracker does not return a valid response*/
        if (tracker_file_bytes == null || tracker_file_bytes.length == 0) {
            throw new IllegalArgumentException("Tracker response can't be 0 bytes");
        }

        this.tracker_file_bytes = tracker_file_bytes;
        this.tracker_file_map = (Map<ByteBuffer, Object>) Bencoder2.decode(tracker_file_bytes);
        /*All if statements update the class variables if key is present in hashmap*/
        if (tracker_file_map.containsKey(FAILURE_REASON)) {
            throw new BencodingException("No keys present from tracker");
        }

        if (!tracker_file_map.containsKey(INTERVAL)) {
            this.interval = 200;
            System.out.println("KEY IS 0");
        } else {
            this.interval = (Integer) tracker_file_map.get(INTERVAL);
        }
        
        if (!tracker_file_map.containsKey(EVENT)) {
        	this.event = "";
        } else {
        	this.event = (String) tracker_file_map.get(EVENT);
        }

        ArrayList<Map<ByteBuffer, Object>> peers = (ArrayList<Map<ByteBuffer, Object>>) tracker_file_map.get(this.PEERS);
        ArrayList<Peer> peerList = new ArrayList<Peer>();

        /*Goes through list of peers in hashmap to get peerid, peerip, and port number
         Stores the values in an arraylist of Peer objects*/
        for (Map<ByteBuffer, Object> peer1 : peers) {
            byte[] id = ((ByteBuffer) peer1.get(TrackerResponseInfo.PEERID)).array();
            int port = (Integer) (peer1.get(TrackerResponseInfo.PORT));
            String ip = new String(((ByteBuffer) peer1.get(TrackerResponseInfo.IP)).array());
            /*only adds peers with these IP addresses to arraylist*/
            if (ip.equals("128.6.171.130") || ip.equals("128.6.171.131")) {
                Peer currPeer = new Peer(id, ip, port, RUBTClient.clientID, RUBTClient.torrentData.info_hash.array());
                peerList.add(currPeer);
            }
        }/*end of for loop*/

        this.peers = peerList;

    }

    /**
    * method to randomly generate peerid, not sure if necessary
    * @return byte array for the peer ID's 
    */
    private byte[] generatePeerID() {
        byte[] peerid = new byte[20];
        peerid[0] = 'R';
        peerid[1] = 'U';
        peerid[2] = 'B';
        peerid[3] = 'T';
        peerid[4] = '1';
        peerid[5] = '1';
        Random num = new Random();
        String alphanumeric = "0123456789abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 6; i < peerid.length; i++) {
            char randomChar = alphanumeric.charAt(num.nextInt(alphanumeric.length()));
            peerid[i] = (byte) randomChar;
        }
        return peerid;
    }
}/*end of TrackerResponseInfo class*/
