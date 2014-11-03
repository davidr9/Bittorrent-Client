
public class HaveMessage extends Message{
	
	/* The index for the piece */
	public int piece_index;
	
	/**
	 * The ‘Have’ message consists of a 4-byte length prefix and single byte message id, 
	 * followed by a 4-byte payload representing the piece number (0-based index).
	 * @param piece_index
	 */
	public HaveMessage(int piece_index){
		super(5, Message.have);
		this.piece_index = piece_index;
	}
	
	/**
	 * 
	 * @return the index of the piece.
	 */
	public int getPieceIndex() {
		return piece_index;
	}
	
}
