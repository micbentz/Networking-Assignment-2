import java.net.*;
import java.io.*;

/**
* Returns a proper ACK after receiving a packet (Refer to HW2)
* Sends back ACK0 or ACK1 to handle PASS or CORRUPT
*/
// TODO add check if message contains "." if it does the entire message has been received and needs to be printed
// TODO bug if starts with a drop or corrupt
public class receiver{
	private static final String TAG = "Receiver> receive: ";
	private static int count = 0;
	private static InetAddress network;
	private static ObjectInputStream inputStream;
	private static ObjectOutputStream outputStream;
	private static int PORT;
	private static int lastPacketReceived = -1;
	private static int expectedSequence = 0;
	private static int totalReceivedPackets = 0;
	private static String completeMessage = "";
	private static boolean done = false;

	public static void main(String[] args){
		try{
			network = InetAddress.getByName(args[0]);
			PORT = Integer.parseInt(args[1]);
		}catch(UnknownHostException exception){
			System.out.println(TAG + "Network not found");
			System.exit(1);
		}catch(ArrayIndexOutOfBoundsException outOfBounds){
			System.out.println(TAG + "Not all arguments were given.");
			System.exit(1);
		}
		communicateWithServer();
	}

	private static void communicateWithServer(){
		Socket requestSocket = null;
		Packet packet = null;
		boolean isCorrupt = false;

		try{
			requestSocket = new Socket(network,PORT);

			// Initialize inputStream and ObjectOutputStream
			inputStream = new ObjectInputStream(requestSocket.getInputStream());
			outputStream = new ObjectOutputStream(requestSocket.getOutputStream());

			//Send identifier for debugging purposes
			outputStream.writeObject(new String("Receiver"));
			while(!done) {
				try{
					packet = (Packet)inputStream.readObject();
//					if(unexpectedSequence(packet)){
//					System.out.println(TAG + "unexpected sequence");
//					System.out.println(TAG + "Expected: " + expectedSequence);
//						if (isCorrupt(packet)){
//						System.out.println(TAG + "data is corrupted");
//						}
//					}
					if (isCorrupt(packet) || unexpectedSequence(packet) ){
//						System.out.println(TAG + "data is corrupted");
						passAck(new ACK((byte) lastPacketReceived,(byte)0));	// Pass the ACK of the last received sequence
					} else{
//						System.out.println(TAG + "data is intact " + "#: " + response.getSequenceNumber() );
						lastPacketReceived = packet.getSequenceNumber(); 		// Update the lastPacketReceived with packet seq #
						passAck(new ACK((byte) lastPacketReceived,(byte)0)); 	// Send ACK
						expectedSequence = (expectedSequence + 1) % 2;			// Update the expectedSequence
						completeMessage += packet.getContent() + " ";			// Append extracted content into message

						if (checkEndofMessage(packet)){
							System.out.println("Sendable: " + completeMessage);
						}
					}
					System.out.println("Waiting " + expectedSequence + ", " + ++totalReceivedPackets + ", " + packet.info() + ", " + "ACK" + lastPacketReceived);
				}catch (ClassNotFoundException notFound){
				}
			}
		}catch(ConnectException e){
			System.out.println(TAG + "Connection refused. You need to initiate a server first.");
		}catch(UnknownHostException unknownHost){
			System.out.println(TAG + "You are trying to connect to an unknown host!");
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			// Close connections
			try{
				inputStream.close();
				outputStream.close();
				requestSocket.close();
			}catch(Exception exception){
				System.out.println(TAG + "Disconnected from Netowrk");
			}
		}
	}

	// Checks if the packet received was corrupted
	private static boolean isCorrupt(Packet packet){
		String content = packet.getContent();
		int checkSum = 0;
		for(int i = 0; i < content.length(); i++){
			checkSum += (int)content.charAt(i);
		}
		return checkSum != packet.getCheckSum();
	}

	// Checks if the packet had the correct sequence
	private static boolean unexpectedSequence(Packet packet){
		return packet.getSequenceNumber() != expectedSequence;
	}

	// Pass the ACK to the associated Handler
	private static void passAck(ACK ack){
		try{
			outputStream.writeObject(ack);
		}catch(IOException ioException){

		}
	}

	// Checks if the end of the message has been reached
	private static boolean checkEndofMessage(Packet packet){
		return packet.getContent().contains(".");
	}

	private static boolean lastAckReceived(Packet packet){
		return packet.getContent() == "test";
	}
}
