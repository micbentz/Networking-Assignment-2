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
	private static int lastPacketReceived = -1;
	private static int expectedSequence = 0;

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

			//Send identifier for debugging purposes
			outputStream.writeObject(new String("Receiver"));
			while(true) {
				try{
					response = (Packet)inputStream.readObject();
				}catch (ClassNotFoundException notFound){

				}
				System.out.println(TAG + " " + response.toString());

				if(unexpectedSequence(response)){
					System.out.println(TAG + "unexpected sequence");
					System.out.println(TAG + "Expected: " + expectedSequence);
					if (isCorrupt(response)){
						System.out.println(TAG + "data is corrupted");
					}
				}
				if (isCorrupt(response) || unexpectedSequence(response) ){
					// Pass the ACK of the last received sequence
					passAck(new ACK((byte) lastPacketReceived,(byte)0));
//						System.out.println(TAG + "data is corrupted");
				} else{
					// Update the last packet received
					lastPacketReceived = response.getSequenceNumber();
					// Send ACK
					passAck(new ACK((byte) lastPacketReceived,(byte)0));
					// Update the expectedSequence
					expectedSequence = (expectedSequence + 1) % 2;
					System.out.println(TAG + "data is intact");
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
}
