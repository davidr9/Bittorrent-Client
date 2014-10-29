
public class PieceMessage extends Message {
	
	/*A Piece message consists of the 4-byte length prefix, 1-byte message ID, and a payload with a 4-byte 
	 * piece index, 4-byte block offset within the piece in bytes (so far the same as for the Request message), 
	 * and a variable length block containing the raw bytes for the requested piece. */
	
	
	/* The index for the piece */
	public int piece_index;
	
	/*integer specifying the zero-based byte offset within the piece*/
	public int begin;
	
	/*block of data, which is a subset of the piece specified by the piece_index.*/
	public byte[] block;
	
	public PieceMessage(int piece_index, int begin, byte[] block ){
		super(block.length+9, Message.piece);
		
		this.piece_index = piece_index;
		this.begin = begin;
		this.block = block;
	}
	
	public int getPieceIndex(){
		return piece_index;
	}
	
	public int getBeginningOfBlock(){
		return begin;
	}
	
	public byte[] getBlock(){
		return block;
	}
	 
	@Override
	public String toString(){
		String str = "Piece (";
		str = str + this.piece_index + ", "+ this.begin + ", " + this.block.length + ")";
		return str;
	}
}