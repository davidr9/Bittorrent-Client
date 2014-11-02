import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Piece {

	final static int BLOCKSIZE = 16384; /*generally accepted size of block is 2^14*/
	
	public static int totalBlocks = RUBTClient.pieceLength/BLOCKSIZE;; /*total number of blocks that should make up the full piece*/
	
	private ByteBuffer requestedPiece; /*the expected SHA-1 hash of the piece*/
	
	private ArrayList<byte[]> blocks; /*list of each block as a byte array*/
	
	private int numBlocks; /*number of blocks added to this piece*/
	
	public byte[] fullPiece; /*concatenation of all blocks representing full piece*/
	
	public int index; /*specified piece number of file*/
	
	public boolean verified; /*true if full piece has been verified and saved by client*/
	
	public Piece(int index){
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
		} else {
			return false;
		}
		
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
	} /*end of verifyPiece*/

}/*end of Piece class*/
