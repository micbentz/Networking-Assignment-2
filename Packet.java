import java.io.*;

public class Packet implements Serializable, Sendable {
	private final String TAG = "Packet.class";
	private byte sequenceNumber;	// 1 byte, either 0 or 1
	private byte ID;				// 1 byte position of the packet in the message, starts at 1
	private int checkSum;			// 4 bytes
	public String content;

	public Packet(PacketBuilder builder){
		this.sequenceNumber = builder.sequenceNumber;
		this.ID = builder.ID;
		this.content = builder.content;
		this.checkSum = builder.checkSum;
	}

	public byte getSequenceNumber(){
		return this.sequenceNumber;
	}

	public byte getID(){
		return this.ID;
	}

	public int getCheckSum(){
		return this.checkSum;
	}

	public String getContent(){
		return this.content;
	}

	public String info(){
		String packetInfo;
//		info = "sequenceNumber: " + sequenceNumber + ", ";
//		info += "ID: " + ID + ", ";
//		info += "checkSum: " + checkSum + ", ";
//		info += "content: " + content;

		packetInfo =  sequenceNumber + ", ";
		packetInfo += ID + ", ";
		packetInfo += checkSum + ", ";
		packetInfo += content;

		return packetInfo;
	}

	@Override
	public void corruptData(){
		this.checkSum++;
	}

	@Override
	public Packet clone(){
		return new Packet.PacketBuilder()
				.sequenceNumber(this.sequenceNumber)
				.ID(this.ID)
				.content(this.content)
				.build();
	}

	@Override
	public String toString(){
		String packetContents;
		packetContents = "Packet" + sequenceNumber + ", " + ID + ", ";
		return packetContents;
	}

	public static class PacketBuilder{
		private byte sequenceNumber;
		private byte ID;
		private int checkSum;
		private String content;

		public PacketBuilder(){}

		public PacketBuilder sequenceNumber(byte sequenceNumber){
			this.sequenceNumber = sequenceNumber;
			return this;
		}

		public PacketBuilder ID(byte ID){
			this.ID = ID;
			return this;
		}

		public PacketBuilder content(String content){
			this.content = content;
			for(int i = 0; i < content.length(); i++){
				this.checkSum += (int)content.charAt(i);
			}
			return this;
		}

		public Packet build(){
			Packet packet = new Packet(this);
//			validatePacketObject(packet);
			return packet;
		}

		private void validatePacketObject(Packet packet){

		}
	}
}
