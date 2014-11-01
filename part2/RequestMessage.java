public class RequestMessage extends Message{

	/*integer specifying the zero-based piece index*/
	public int piece_index;
	/*integer specifying the zero-based byte offset within the piece*/
	public int begin_block;
	/*integer specifying the requested length.*/
	public int block_length;
	
	public RequestMessage(int piece_index, int begin_block, int block_length){
		super(13, Message.request);
		this.piece_index = piece_index;
		this.begin_block = begin_block;
		this.block_length = block_length;
	}
	
	public int getPieceIndex() {
		return piece_index;
	}

	public int getBeginningOfBlock() {
		return begin_block;
	}

	public int getBlockLength() {
		return block_length;
	}
	
	@Override
	public String toString(){
		String str = "Request ";
		str = str + this.piece_index + ", "+ this.begin_block + ", " + this.block_length
				+ ")";
		return str;
	}
	
}
