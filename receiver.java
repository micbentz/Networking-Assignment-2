import java.net.*;
import java.io.*;

/**
* Returns a proper ACK after receiving a packet (Refer to HW2)
* Sends back ACK0 or ACK1 to handle PASS or CORRUPT
*/
public class receiver{
	private static final String TAG = "Receiver> receive: ";
	private static int count = 0;
	private static InetAddress network;
	private static ObjectInputStream inputStream;
	private static ObjectOutputStream outputStream;
	private static int PORT;
	private static int lastReceivedSequence = 0;

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
		Packet response = null;
		boolean isCorrupt = false;

		try{
			requestSocket = new Socket(network,PORT);

			// Initialize inputStream and ObjectOutputStream
			inputStream = new ObjectInputStream(requestSocket.getInputStream());
			outputStream = new ObjectOutputStream(requestSocket.getOutputStream());

			//Send identifier
			outputStream.writeObject(new String("Receiver"));
//			try{
				while(true) {
					try{
						response = (Packet)inputStream.readObject();
					}catch (ClassNotFoundException notFound){

					}
					System.out.println(TAG + " " + response.toString());

					if (isCorrupt(response) || unexpectedSequence(response) ){
						// Pass the ACK of the last received sequence
						passAck(new ACK((byte)lastReceivedSequence,(byte)0));
						System.out.println(TAG + "data is corrupted");
					} else{
						lastReceivedSequence = response.getSequenceNumber();
						passAck(new ACK((byte)lastReceivedSequence,(byte)0));
						System.out.println(TAG + "data is intact");
					}
				}
//			}catch(Exception e){
//				e.printStackTrace();
//			}
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

	private static boolean isCorrupt(Packet packet){
		String content = packet.getContent();
		int checkSum = 0;
		for(int i = 0; i < content.length(); i++){
			checkSum += (int)content.charAt(i);
		}
		return checkSum == packet.getCheckSum();
	}

	private static boolean unexpectedSequence(Packet packet){
		return packet.getSequenceNumber() == lastReceivedSequence;
	}

	private static void passAck(ACK ack){
		try{
			outputStream.writeObject(ack);
		}catch(IOException ioException){

		}
	}

//	private boolean isCorrupt(ACK ack){
//
//	}

	private void receivePacket(Packet packet){
		// Perform checksum and examine the serial number and ID
		// to see if this packet is wanted and not corrupted
		printPacket(packet);
	}

	// Called every time a packet is received
	private void printPacket(Packet packet){
		// Prints the whole packet and the proper ACK to be transmitted
		// Waiting 0, 10, 1 4 x sunshine., ACK0
		// Waiting 1, 11, 1 5 x gators, ACK1

		// If the end of the whole message is recieved, display message
		// with blanks b/w each word:
		// if (message.contains("."))
		// Message: You are my sunshine
	}
}
