import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Piece {

	final static int BLOCKSIZE = 16384;
	
	private static int totalBlocks = RUBTClient.pieceLength/BLOCKSIZE;
	
	private ByteBuffer requestedPiece;
	
	private ArrayList<byte[]> blocks;
	
	private int numBlocks;
	
	private byte[] fullPiece;
	
	private int index;
	
	private boolean verified;
	
	public Piece(int numBlocks, int index){
		this.numBlocks = numBlocks;
		this.index = index;
		this.requestedPiece = RUBTClient.pieces[index];
	}
	
	public boolean insertBlock(int blockOffset, byte[] block) throws UnsupportedEncodingException{
		
		if (numBlocks == totalBlocks || verified){
			System.err.println("Piece " + index + "already has all of its blocks");
			return false;
		}
		
		int index = blockOffset/BLOCKSIZE;
		if (index >= totalBlocks){
			System.err.println("The block offset given is not within the size of the piece");
			return false;
		}
		
		blocks.add(index, block);
		numBlocks++;
		if(numBlocks == totalBlocks)
		{
			return verifyPiece();
		}
		
		return false;
	}/*end of insertBlock*/
	
	public boolean verifyPiece() throws UnsupportedEncodingException
	{
		if(blocks.size() != totalBlocks)
		{
			System.err.println("Cannot verify: blocks are missing");
			return false;
		}
		
		String originPieceHash = RUBTClient.escape(requestedPiece.array());
		byte[] fullPiece = new byte[BLOCKSIZE*totalBlocks];
		
		for(int i = 0; i < blocks.size(); i++)
		{
			int offset = i * BLOCKSIZE;
			System.arraycopy(blocks.get(i), 0, fullPiece, offset, BLOCKSIZE);
		}
		
		this.fullPiece = fullPiece;
		String requestedPieceHash = RUBTClient.escape(fullPiece);
		
		if (requestedPieceHash.equals(originPieceHash)){
			verified = true;
		} else {
			verified = false;
		}
		
		return verified;
	}/*end of verifyPiece*/

}/*end of Piece class*/
