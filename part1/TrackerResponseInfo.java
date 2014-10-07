import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*Like the TorrentInfo class, but for the tracker response*/
/*Detailed explanation found here: https://wiki.theory.org/BitTorrentSpecification */
public class TrackerResponseInfo {
	/*keys include: failure reason, interval, tracker id, complete, incomplete, peers*/
	
	private byte[] tracker_file_bytes;
	private Map<ByteBuffer, Object> tracker_file_map;
        /*time that the client should wait between sending requests to the tracker (sec)*/ 
	private int interval;
        /*A string that the client should send back on it's next announcements*/
        private String trackerid; 
        /*number of peers with the entire file and number of non-seeder peers*/
        private int complete, incomplete;
        /*list of Peer objects. Contains the peerid, port, and ip*/
        private ArrayList<Peer> peers; 
        
	/*keys to access the hashmap. all of these keys should be in the tracker response*/
	public final static ByteBuffer INTERVAL = ByteBuffer.wrap(new byte[] {'i', 'n', 't', 'e', 'r', 'v', 'a', 'l'}); 
	
	public final static ByteBuffer FAILURE_REASON = ByteBuffer.wrap(new byte[] {'f', 'a', 'i', 'l', 'u', 'r', 'e'});
	
	public final static ByteBuffer COMPLETE = ByteBuffer.wrap(new byte[]{'c', 'o', 'm', 'p', 'l', 'e', 't', 'e'});
	
	public final static ByteBuffer TRACKER_ID = ByteBuffer.wrap(new byte[] {'t', 'r', 'a', 'c', 'k', 'e', 'r', 'i', 'd'});
	
	public final static ByteBuffer INCOMPLETE = ByteBuffer.wrap(new byte[]{'i', 'n', 'c', 'o', 'm', 'p', 'l', 'e', 't', 'e'});
	
	public final static ByteBuffer PEERS = ByteBuffer.wrap(new byte[]{'p', 'e', 'e', 'r', 's'});
        
        public final static ByteBuffer PEERID = ByteBuffer.wrap(new byte[]{'p', 'e', 'e', 'r', ' ', 'i', 'd'});
        
        public final static ByteBuffer IP = ByteBuffer.wrap(new byte[]{'i', 'p'});
        
        public final static ByteBuffer PORT = ByteBuffer.wrap(new byte[]{'p', 'o', 'r', 't'});
		
	public TrackerResponseInfo (byte[] tracker_file_bytes) throws BencodingException, IOException{
                if(tracker_file_bytes == null || tracker_file_bytes.length == 0)
		{
			throw new IllegalArgumentException("Tracker response can't be 0 bytes"); 
		}
                
		this.tracker_file_bytes = tracker_file_bytes; 
		this.tracker_file_map = (Map<ByteBuffer,Object>)Bencoder2.decode(tracker_file_bytes);
		 
		if(tracker_file_map.containsKey(FAILURE_REASON)){
			throw new BencodingException("No keys present from tracker");
		}
		
		if(!tracker_file_map.containsKey(INTERVAL)){
                    System.out.println("Interval is null");
                    this.interval = 0;
                }else{
                    this.interval = (int) tracker_file_map.get(INTERVAL);
                }
		
                if(!tracker_file_map.containsKey(COMPLETE)){
                    System.out.println("There are no peers with the entire file");
                    this.complete = 0;
                }else{
                    this.complete = (int) tracker_file_map.get(COMPLETE);
                }
                
                if(!tracker_file_map.containsKey(INCOMPLETE)){
                    System.out.println("There are no non-seeder pairs");
                    this.incomplete = 0;
                }else{
                    this.incomplete = (int) tracker_file_map.get(INCOMPLETE);
                }
               
                this.peers = new ArrayList<>();
                ArrayList<Map<ByteBuffer, Object>> list_of_peers = (ArrayList<Map<ByteBuffer, Object>>) tracker_file_map.get(PEERS);
                
                for(int i=0; i<list_of_peers.size(); i++){
                    byte[] peerid = (byte[]) list_of_peers.get(i).get(PEERID.array());
                    String ip = list_of_peers.get(i).get(IP).toString();
                    int port = (int) list_of_peers.get(i).get(PORT);
                    Peer currPeer = new Peer(peerid, ip, port);
                }
		
	}
}/*end of TrackerResponseInfo class*/
