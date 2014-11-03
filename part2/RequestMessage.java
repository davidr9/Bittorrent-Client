public class RequestMessage extends Message{

	/*integer specifying the zero-based piece index*/
	public int piece_index;
	/*integer specifying the zero-based byte offset within the piece*/
	public int begin_block;
	/*integer specifying the requested length.*/
	public int block_length;
	
	/**
	 * The ‘Request’ message type consists of the 4-byte message length, 1-byte message 
	 * ID, and a payload composed of a 4-byte piece index (0 based), 4-byte block offset 
	 * within the piece (measured in bytes), and 4-byte block length
	 * @param piece_index
	 * @param begin_block
	 * @param block_length
	 */
	
	public RequestMessage(int piece_index, int begin_block, int block_length){
		super(13, Message.request);
		this.piece_index = piece_index;
		this.begin_block = begin_block;
		this.block_length = block_length;
	}
	
	/**
	 * 
	 * @return integer for the piece index
	 */
	public int getPieceIndex() {
		return piece_index;
	}

	/**
	 * 
	 * @return integer for the index where block begins
	 */
	public int getBeginningOfBlock() {
		return begin_block;
	}

	/**
	 * 
	 * @return integer for the length of the block
	 */
	public int getBlockLength() {
		return block_length;
	}
	
	/**
	 * String representation of a request message
	 */
	@Override
	public String toString(){
		String str = "Request ";
		str = str + this.piece_index + ", "+ this.begin_block + ", " + this.block_length
				+ ")";
		return str;
	}
	
}
