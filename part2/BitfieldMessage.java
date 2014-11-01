public class BitfieldMessage extends Message{

	private byte[] pieces;
	
	public BitfieldMessage(int length, byte[] pieces){
		super(length, Message.bitfield);
		this.pieces = pieces;
	}
	
	public byte[] getPieces(){
		return this.pieces;
	}
}
