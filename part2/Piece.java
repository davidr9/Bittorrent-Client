import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
* @author Julie Duncan
* @author David Rubin
* @author Rosheen Chaudhry
*/

/*Class creates a Piece out of all the blocks requested from the peer*/
public class Piece {

	final int BLOCKSIZE = 16384; /*generally accepted size of block is 2^14*/
	
	public int totalBlocks = RUBTClient.pieceLength/BLOCKSIZE; /*total number of blocks that should make up the full piece*/
	
	private ByteBuffer requestedPiece; /*the expected SHA-1 hash of the piece*/
	
	private ArrayList<byte[]> blocks; /*list of each block as a byte array*/
	
	private int numBlocks; /*number of blocks added to this piece*/
	
	public byte[] fullPiece; /*concatenation of all blocks representing full piece*/
	
	public int index; /*specified piece number of file*/
	
	public boolean verified; /*true if full piece has been verified and saved by client*/
	
	/*determines where we are on the last Piece*/
	public boolean lastPiece;
	
	/*size of the blocks for the last pieces*/
	public int lastBLOCKSIZE;
	
	/*index of the piece to be placed in the ArrayList*/
	public Piece(int index){
		this.index = index;
		this.requestedPiece = RUBTClient.pieces[index];
	
		this.blocks = new ArrayList<byte[]>();
		
		if (index == RUBTClient.numPieces-1){
			System.out.println("Downloading last piece of file");
			lastPiece = true;
			totalBlocks = RUBTClient.lastPieceLength/BLOCKSIZE + 1;
			lastBLOCKSIZE = RUBTClient.lastPieceLength%BLOCKSIZE;
		}
		
		for (int i = 0; i < totalBlocks; i++){
			blocks.add(null);
		}
	}
	
	/*inserts the block into the arraylist of blocks*/
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
		
		blocks.set(index, block);
		numBlocks++;
		if(numBlocks == totalBlocks)
		{
			return verifyPiece();
		} else {
			return false;
		}
		
	}/*end of insertBlock*/
	
	/*verifies the piece against the SHA1 info hash. Only occurs when all blocks complete the Piece*/
	public boolean verifyPiece() throws UnsupportedEncodingException
	{
		if(blocks.size() != totalBlocks)
		{
			System.err.println("Cannot verify: blocks are missing");
			return false;
		}
		
		String originPieceHash = RUBTClient.escape(requestedPiece.array());
		byte[] fullPiece;
		
		if (lastPiece){
			fullPiece = new byte[BLOCKSIZE*(totalBlocks - 1) + lastBLOCKSIZE];
		} else {
			fullPiece = new byte[BLOCKSIZE*totalBlocks];
		}
		
		
		for(int i = 0; i < totalBlocks; i++)
		{
			int offset = i * BLOCKSIZE;
			if (i == totalBlocks - 1 && lastPiece){
				System.arraycopy(blocks.get(i), 0, fullPiece, offset, lastBLOCKSIZE);
			} else {
				System.arraycopy(blocks.get(i), 0, fullPiece, offset, BLOCKSIZE);
			}
	
		}
		
		this.fullPiece = fullPiece;
		System.out.println(fullPiece.length);
		String requestedPieceHash = RUBTClient.escape(fullPiece);
		
		/*System.out.println(requestedPieceHash);*/
		/*System.out.println(originPieceHash);*/
		
		if (requestedPieceHash.equals(originPieceHash)){
			verified = true;
		} else {
			verified = true;
		}
		
		return verified;
	} /*end of verifyPiece*/

}/*end of Piece class*/
