import java.nio.ByteBuffer;
import java.util.Map;

/*Like the TorrentInfo class, but for the tracker response*/
public class TrackerResponseInfo {
	
	private byte[] tracker_file_bytes;
	public final Map<ByteBuffer,Object> torrent_file_map;

	public TrackerResponseInfo (byte[] tracker_file_bytes) throws BencodingException{
		if(tracker_file_bytes == null || tracker_file_bytes.length == 0)
		{
			throw new IllegalArgumentException("Tracker response can't be 0 bytes"); 
		}
		this.tracker_file_bytes = tracker_file_bytes; 
		this.torrent_file_map = (Map<ByteBuffer,Object>)Bencoder2.decode(tracker_file_bytes);
	}
	
}
