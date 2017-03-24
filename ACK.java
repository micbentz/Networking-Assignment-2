import java.io.Serializable;

/**
 * A <code>ACK</code> is the message sent from the receiver to the sender
 */

public class ACK implements Serializable, Sendable {
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
		contents = "ACK" + sequenceNumber + ", ";
		return contents;
	}
}
