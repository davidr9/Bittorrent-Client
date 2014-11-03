
/*public class BitfieldMessage extends Message{

	/*The bitfield message is variable length, where X is the length of the bitfield. 
	 * The payload is a bitfield representing the pieces that have been successfully downloaded. 
	 * The high bit in the first byte corresponds to piece index 0. Bits that are cleared 
	 * indicate a missing piece, and set bits indicate a valid and available piece. Spare 
	 * bits at the end are set to zero.*/
	/*
	public byte[] bitfield;
	
	public BitfieldMessage(byte[] bitfield){
		super(bitfield.length+1, Message.bitfield);
		
	}
	
	public byte[] getFieldBytes ()
    {
        return bitfield;
    }
	
	public static boolean isValid (byte[] message) {
        if(message.length >= 2){
        	return true;
        }else{
        	return false;	
        }
    }
}*/

/**
 *  At the start of a connection, the peer can send a ‘Bitfield’ message. 
 *  Bitfield messages are optional and can only be sent as the message immediately 
 *  following the handshake message. Again, the message consists of the 4-byte length 
 *  prefix, 1-byte message ID, and a variable-length payload. The payload for the Bitfield 
 *  message is a way to succinctly describe the pieces that a peer has. Bits set to 1 
 *  indicate pieces the peer has, unset bits (0) indicate missing pieces. The bitfield 
 *  payload comes through as a set of bytes.
 * @author julie david rosheen
 *
 */
public class BitfieldMessage extends Message{

	private byte[] pieces;
	
	public BitfieldMessage(int length, byte[] pieces){
		super(length, Message.bitfield);
		this.pieces = pieces;
	}
	
	/**
	 * 
	 * @return a byte array of the pieces the peer has.
	 */
	public byte[] getPieces(){
		return this.pieces;
	}
}
