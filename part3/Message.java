
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class represented as a stream of bytes to communicate messages between peers
 * @author julieduncan
 * @author davidrubin
 * @author rosheenchaudhry
 */
public class Message {

		/*prefix length*/
		int length;
		
		/*Message ID*/
		byte message_id;
		
		 /* ******************************************************************************************/
		 /* keep-alive: <length prefix> is 0. There is no message ID and no payload. 				 */
		 /* These should be sent around once every 2 minutes to prevent peers from closing			 */
		 /*  connections. These only need to be sent if no other messages are sent within a 2-minute */
		 /*  interval.																				 */
		 /* **************************************************************************************** */
		public static final byte keep_alive = -1;
		
		/* choke: <length prefix> is 1 and message ID is 0. There is no payload. */
		public static final byte choke = 0;
		
		/* unchoke: <length prefix> is 1 and the message ID is 1. There is no payload. */
		public static final byte unchoke = 1;
		
		/* interested: <length prefix> is 1 and message ID is 2. There is no payload. */
		public static final byte interested = 2;
		
		/* uninterested: <length prefix> is 1 and message ID is 3. There is no payload.*/
		public static final byte un_interested = 3;
		
		/* have: <length prefix> is 5 and message ID is 4. The payload is a zero-based index of the piece that has just been downloaded and verified.*/
		public static final byte have = 4;
		
		/*request: <length prefix> is 13 and message ID is 6. The payload is as follows: */
		public static final byte request = 6;

		/*bitfield: <length prefix> is 1 + X (X is pices/8) and message ID is 5*/
		public static final byte bitfield = 5;

		/*piece: <length prefix> is 9 + x and message ID 7*/
		public static final byte piece = 7;
		
		/*stores the message that will be sent to the peer*/              
        	byte[] messageSent = null;
                
                /*stores the message that the client will recieve*/
	 	byte[] messageReceived = null; 
                
                //event that indicates user has canceled upload or download
		public static final byte cancel = 8;

		/*represents the possible messages that the client or peer can recieve*/
		public static final Message keep_alive_message = new Message(0, keep_alive);
		public static final Message choke_message = new Message(1, choke);
		public static final Message unchoke_message = new Message(1, unchoke);
		public static final Message uninterested_message = new Message(1, un_interested);
		public static final Message interested_message = new Message(1, interested);
		
		/**
		 *  length prefix is a 4-byte big-endian value and message ID is a single byte.
		 **/
		public Message(int length_prefix, byte message_id){
			this.length = length_prefix;
			this.message_id = message_id;
		}
		
		/**
		 * @return byte array representing the messageID.
		 */
		public byte getID(){
			return this.message_id;
		}
		
		/**
		 * @return integer representing the length of this message
		 */
		public int getLength(){
			return this.length;
		}
		
		/**
		 *Convert length-prefix integer to byte array and add it as the prefix for the message 
		 * @param base10
		 * @param result
		 * @return byte array representing the message prefix
		 */
		public static byte[] addLengthPrefix(int base10, byte[] result) {
			result[0] = (byte) (base10 >> 24);
			result[1] = (byte) (base10 >> 16);
			result[2] = (byte) (base10 >> 8);
			result[3] = (byte) (base10);
			return result;
		}
		
		/**
		 * Perform handshake
		 * The handshake is the first message transmitted by the client.
		 * <pstrlen> is a string length of <pstr> and it is a single byte.
		 * <pstr> is the string identifier of the protocol.
		 * <reserved> are the 8 reserved bytes, which should all be zeros.
		 * <info_hash> is the 20 byte SHA1 hash of the info key of the torrent. It is the same info_hash transmitted in the tracker requests.
		 * <peer_id> is the 20-byte string used as a unique id for the client. Same peer ID transmitted in tracker request. */
		public static byte[] generateHandShake(byte[] info_hash, byte[] peer_id) {
			
			/* Basic error checking */
			if (info_hash == null || info_hash.length != 20)
				throw new IllegalArgumentException(
						"PeerMessage: Info hash must be 20 bytes.");
			if (peer_id == null || peer_id.length < 20)
				throw new IllegalArgumentException(
						"PeerMessage: Peer ID must be at least 20 bytes.");
			
			byte[] handshake = new byte[] /*Handshaking between peers begins with byte nineteen followed by the string 'BitTorrent protocol'*/
					{ 
					19, 'B', 'i', 't', 'T', 'o', 'r', 'r', 'e', 'n', 't',
					' ', 'p', 'r', 'o','t', 'o', 'c', 'o', 'l',
					/*After the fixed headers are 8 reserved bytes which are set to 0*/
					0, 0, 0, 0, 0, 0, 0, 0,
					/*Next is the 20-byte SHA-1 hash of the bencoded form of the info value from the metainfo (.torrent) file.*/
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
					/*The next 20-bytes are the peer id generated by the client*/
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 
					};
			
			/* load the SHA has into its part of the handshake array.*/
			int j=0;
			int len = 28 + info_hash.length; 
			for(int i = 28; i<len; i++){
				handshake[i] = info_hash[j];
				j++;				
			}

			/* load the peer_id into its part of the handshake*/
			int p=0;
			len = 48 + peer_id.length;
			for(int i = 48; i<peer_id.length; i++){
				handshake[i] = info_hash[p];
				p++;				
			}
		
			return handshake;
		}
		

		/**
		 * Takes a message ID and returns a human readable string to identify the message.
		 * @param id
		 * @return string representation of a particular message.
		 */
		public static String getMessageName(byte id){
			if(id==0){
				return "Choke";
			}
			if(id==1){
				return "Unchoke";
			}
			if(id==2){
				return "Interested";
			}
			if(id==3){
				return "Not Interested";
			}
			if(id==4){
				return "Have";
			}
			if(id==5){
				return "Bitfield";
			}
			if(id==6){
				return "Request";
			}
			if(id==7){
				return "Piece";
			}
			if(id==8){
				return "Cancel";
			}else{
				return "null message";
			}
		}

		/**
		 * Reads a message provided in datainputstream. 
		 * @param data_input
		 * @return Message
		 * @throws IOException
		 */
		public static Message readMessage(DataInputStream data_input) throws IOException{
			
			/*read the data_input length*/
			int length = data_input.readInt();
			
			if(length<0 || length > 131081){
				throw new IOException("Message length -> "+length+" <- is invalid");
			}
			
			if(length==0) /*Then it's a keep_alive message*/
			{
				return keep_alive_message;
			}
			
			byte message_type = data_input.readByte();
			
			if(message_type >= 0 && message_type < 9){
				System.out.println("Message type is: "+Message.getMessageName(message_type));
			}
			else{
				/*If we get here, the next byte is invalid.*/
				throw new IOException("Unknown message. ID: "
					+ Integer.toHexString(message_type & 0xFF) + " Length: " + length);
			}
			
			/*For messages with no payload: choke, unchoke, interested, uninterested*/
			if(length==1){
				if(message_type==choke){
					return choke_message;
				}
				if(message_type==unchoke){
					return unchoke_message;
				}
				if(message_type==interested){
					return interested_message;
				}
				if(message_type==un_interested){
					return uninterested_message;
				}else{
					throw new IOException(
							"Message of length 1 could not be reccognized. ID: "
									+ Integer.toHexString(message_type & 0xFF));
				}
			}
			
			/* For Have messages. */
			if(message_type==have && length == 5){
				int piece = data_input.readInt();
				return new HaveMessage(piece);
			}
			
			/*For request messages*/
			if(message_type==request && length == 13){
				int piece_index = data_input.readInt();
				int block_start = data_input.readInt();
				int block_length = data_input.readInt();
				return new RequestMessage(piece_index, block_start, block_length);
			}
			
			
			/*For piece messages*/
			if(message_type==piece && length >=9){
				int index = data_input.readInt();
				int begin = data_input.readInt();
				int piece_length = length-9;
				byte[] piece = new byte[piece_length];
				data_input.readFully(piece);
				System.out.println("PeerMessage: Piece block received (" + index + ","
					+ begin + "," + piece_length + ").");
				return new PieceMessage(index, begin, piece);
			}
			
			if(message_type == bitfield){
				byte[] payload = new byte[length - 1];
				int numBytes = data_input.read(payload, 0, length -1);
				return new BitfieldMessage(length, payload);
			}
			
			/*If we reach here, the message was not recognized*/
			throw new IOException("Message not recognized: Length is " + 
					Integer.valueOf(length) + ", Type is " +  Integer.toHexString(message_type & 0xFF));
			
		}
		
		/**Writes the message to the outputstream and returns the number of bytes
		 * written.
		 * @param data_output
		 * @param message
		 * @return boolean representing whether or not message was succesfully written.
		 *  */
		public static boolean writeMessage(DataOutputStream data_output, Message message) throws IOException{
			
			if(message==null){
				throw new IOException("Message was null. Could not write to output stream");
			}
			
			try{
				System.out.println("Writing message...");
				
				data_output.writeInt(message.getLength());
				
				if(message.getLength() > 0){
					data_output.writeByte(message.getID());
						if(message.getID() == Message.bitfield){
							/*Convert the bitfield message bits into boolean and store them in a byte array. */
						}
						if(message.getID() == Message.piece){
							PieceMessage piece_message = (PieceMessage) message;
							data_output.writeInt(piece_message.getPieceIndex());
							data_output.writeInt(piece_message.getBeginningOfBlock());
							data_output.write(piece_message.getBlock());
							
						}
						if(message.getID() == Message.request){
							RequestMessage req_message = (RequestMessage) message;
							data_output.writeInt(req_message.getPieceIndex());
							data_output.writeInt(req_message.getBeginningOfBlock());
							data_output.writeInt(req_message.getBlockLength());
							
						}
						if(message.getID()==Message.have){
							HaveMessage have_message = (HaveMessage) message;
							data_output.writeInt(have_message.getPieceIndex());
							
						}
						data_output.flush();
				}
			}
			
			catch(NullPointerException null_ptr){
				throw new IOException("Could not write to stream because it is null.");
			}
			
			return true;
		}
		
		/**
		 * @return String representation of a particular message.
		 */
		public String toString(){
			if(this.message_id == Message.keep_alive){
				return "keep alive";
			}
	
			String str = Message.getMessageName(this.message_id);
			return str;
		}
			
		/**Get the payload from a Have or Request message received from the 
		 * peer
		 * @return byte array 
		 * */
		public byte[] getPayload(){
			byte[] payload = null;
			
			switch (this.message_id){
				case (byte) 4: /*Have message payload is 4 bytes*/
					payload = new byte[4];
					System.arraycopy(this.messageReceived, 5, payload, 0, 4);
					return payload;
				case (byte) 6: /*Request message payload is 12 bytes*/
					payload = new byte[12];
					System.arraycopy(this.messageReceived, 5, payload, 0, 12);
					return payload;
				default: /* Message has no payload*/
					return null;
			}
		}
		
}
