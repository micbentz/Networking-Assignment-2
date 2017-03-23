import java.io.Serializable;

public class ACK implements Serializable, Message{
	private final String TAG = "ACK.class";
	private byte sequenceNumber;		// 1 byte
	private byte checksumValue = 0;		// 1 byte, initially set to 0

	public ACK(byte sequenceNumber, byte checkSumValue){
		this.sequenceNumber = sequenceNumber;
		this.checksumValue = checkSumValue;
	}

	public byte getSequenceNumber(){
		return this.sequenceNumber;
	}

	public byte getChecksumValue(){
		return this.checksumValue;
	}

	@Override
	public void corruptData(){
		this.checksumValue++;
	}

	@Override
	public ACK clone(){
		return new ACK(sequenceNumber,checksumValue);
	}

	@Override
	public String toString(){
		String contents;
//		contents = "sequence number: " + sequenceNumber + '\t';
//		contents += "checksumValue: " + checksumValue + '\t';

		contents = "ACK" + sequenceNumber + ", ";
		return contents;
	}
}
