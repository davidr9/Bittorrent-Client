/**
* @author Julie Duncan
* @author David Rubin
* @author Rosheen Chaudhry
*/

public class BitfieldMessage extends Message{

	/*The bitfield message is variable length, where X is the length of the bitfield. 
	 * The payload is a bitfield representing the pieces that have been successfully downloaded. 
	 * The high bit in the first byte corresponds to piece index 0. Bits that are cleared 
	 * indicate a missing piece, and set bits indicate a valid and available piece. Spare 
	 * bits at the end are set to zero.
	*/
	private byte[] pieces;
	
	public BitfieldMessage(int length, byte[] pieces){
		super(length, Message.bitfield);
		this.pieces = pieces;
	}
	
	public byte[] getPieces(){
		return this.pieces;
	}
	
}
