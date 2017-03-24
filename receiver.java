import java.net.*;
import java.io.*;

/**
 * The <code>recever</code> uses rdt3.0 receiver protocols to send the ACK of the
 * last correctly (intact && correct seq#) packet.
*/
public class receiver{
	private static final String TAG = "Receiver> receive: ";		// TAG used for debugging
	private static InetAddress network;								// IP of the network
	private static ObjectInputStream inputStream;					// Stream for reading Objects from Handler
	private static ObjectOutputStream outputStream;					// Stream for writing Objects to Handler
	private static int PORT;
	private static int lastPacketReceived = 1;						// Last received packet sequence
	private static int expectedSequence = 0;						// Next expected packet sequence
	private static int totalReceivedPackets = 0;					// Total amount of packets received
	private static String completeMessage = "";						// Compiled contents of received packets
	private static boolean done = false;							// Flag to terminate program

	public static void main(String[] args){
		try{
			network = InetAddress.getByName(args[0]);		// Get the IP
			PORT = Integer.parseInt(args[1]);				// Get the Port
		}catch(UnknownHostException exception){
			System.out.println(TAG + "Network not found");
			System.exit(1);
		}catch(ArrayIndexOutOfBoundsException outOfBounds){
			System.out.println(TAG + "Not all arguments were given.");
			System.exit(1);
		}
		communicateWithServer();
	}

	// Communicates with the Handler
	private static void communicateWithServer(){
		Object object = null;
		Socket requestSocket = null;
		Packet packet = null;
		boolean isCorrupt = false;

		try{
			// Create socket
			requestSocket = new Socket(network,PORT);

			// Initialize inputStream and ObjectOutputStream
			inputStream = new ObjectInputStream(requestSocket.getInputStream());
			outputStream = new ObjectOutputStream(requestSocket.getOutputStream());

			//Send identifier for debugging purposes
			outputStream.writeObject(new String("Receiver"));
			while(!done) {
				try{
					// Read in the object
					object = inputStream.readObject();

					// If the object is a packet
					if(isPacket(object)) {
						packet = (Packet) object;

						// If the packet is corrupt or an unexpected seq # is received
						if (isCorrupt(packet) || unexpectedSequence(packet)) {
							passAck(new ACK((byte) lastPacketReceived, (byte) 0));   // Pass the ACK of the last received sequence
						} else { // The packet is correct
							lastPacketReceived = packet.getSequenceNumber();        // Update the lastPacketReceived with packet seq #
							expectedSequence = (expectedSequence + 1) % 2;          // Update the expectedSequence
							completeMessage += packet.getContent() + " ";           // Append extracted content into message
							passAck(new ACK((byte) lastPacketReceived, (byte) 0));  // Send ACK

							// If the entire message has been received display it
							if (checkEndofMessage(packet)) {
								System.out.println("Message: " + completeMessage);
							}
						}
						System.out.println("Waiting " + expectedSequence + ", " + ++totalReceivedPackets + ", " + packet.info() + ", " + "ACK" + lastPacketReceived);
					}else { // Object is termination message
						Byte terminate = (Byte)object;  // Get the byte
						if (terminate == -1 ){			// If it's equal to -1
							done = true;
						}
					}

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
				System.out.println(TAG + "Disconnected from Network");
			}
		}
	}

	// Checks if the object is a packet
	private static boolean isPacket(Object object){
		return object instanceof Packet;
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
}
