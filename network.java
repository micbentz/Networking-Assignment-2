import java.util.*;
import java.net.*;
import java.io.*;

/**
* The <code>Network</code> relays a packet and acknowledgement
* between a sender and receiver.
* Lossy channel with bit errors is emulated using PASS, CORRUPT and DROP
* PASS = 0.5%
* CORRUPT = 0.25%
* DROP = 0.25%
*/
public class network{
	private static network instance = null;
	private static final String TAG = "Network> ";
	private static ServerSocket listener;
	private static Handler[] handlers;
	private static HashMap<Handler,Handler> destinationMap;
	private static int PORT;
	private static int clientCount = 0;

	private static void initStorage() {
		destinationMap = new HashMap<>();
		handlers = new Handler[2];
	}

	public synchronized static network getInstance(){
		if (instance == null){
			instance = new network();
		}
		return instance;
	}

	// TODO fix the ordering of the try catch statements
	public static void main(String[] args){
		// Initialize the variables
		initStorage();
		PORT = Integer.parseInt(args[0]);
		System.out.println(TAG + "Network opening port: " + PORT);
		try{
			listener = new ServerSocket(PORT);
		}catch(Exception exception){
			System.out.println(TAG + "Unable to attach to port");
			System.exit(1);
		}

		while (true){
			try{
				// If there is a receiver and sender
				if (clientCount == 2){
					createPairing();
				}
				// Wait until connection attempt is made
				Socket client = listener.accept();
				// Create a Handler and pass the client socket
				Handler clientHandler = new Handler(client);
				// Start the Handler thread
				clientHandler.start();

				// Add the handler and increment count
				handlers[clientCount] = clientHandler;
				clientCount++;
			}catch(IOException ioException){
				System.out.println(TAG + "Could not accept client connection");
			}
		}
	}

	private static void createPairing(){
		Handler receiver, sender = null;

		// Get the receiver and sender Handler
		receiver = handlers[0];
		sender = handlers[1];

		// Create the mapping from receiver to sender
		destinationMap.put(receiver,sender);
		destinationMap.put(sender,receiver);
	}

	public void sendToDestination(Message message, Handler source){
		System.out.println(TAG + "getting ready to send to destination");
		// Get the destination
		Handler destination = destinationMap.get(source);

		// Generate a random value
		Random random = new Random();
		double value = random.nextDouble();

		// TODO uncomment other messages
		// Choose an operation
		if (value <= .50){
			passData(message, destination);
		} else if (value <= .75){
//			passData(message, destination);
			 corruptData(message, destination);
		} else{
//			passData(message,destination);
//			corruptData(message, destination);
			dropData();
		}
	}

	/**
	* Drop packet or ACK, sendPacket a DROP message to the sender
	* Use ACK2 for DROP message, i.e., ACK with seq# 2 to
	* emulate a fake timeout
	*/
	public void dropData(){
		Handler senderHandler = handlers[1];
		// Create an ACK
		ACK dropAck = new ACK((byte)2,(byte)0);
		// Send the ack back to the sender
		senderHandler.sendMessage(dropAck);
		System.out.println(TAG + "Data was dropped enroute. Sending ACK(2)");
//		if(source == handlers[1]) {
//			source.sendMessage(dropAck);
//		}
	}


	/**
	 * Sends the data to the receiver
	 */
	public void passData(Message message, Handler destination){
		destination.sendMessage(message);
		System.out.println(TAG + "Sending packet to: " + destination.getLinkedClient());
	}

	/**
	 * Add 1 to the checksum field
	 */
	public void corruptData(Message message, Handler destination){
		// Clone the message so that it's original contents aren't changed
		Message copy = message.clone();
		// Corrupt the data
		copy.corruptData();
		destination.sendMessage(copy);
		System.out.println(TAG + "Corrupted the packet's data going to: " + destination.getLinkedClient());
	}


	/**
	* When a packet is received it is printed out
	* packet type, ID (no ID for ACK) and the content it picks
	*/
	private void printPacket(Packet packet){
		// Received: Packet0, 5, DROP
		// Received ACK1, PASS
	}
}


//	public void sendToDestination(Packet packet, Handler source){
//		// Get the destination
//		Handler destination = destinationMap.get(source);
//
//		// Generate a random value
//		Random random = new Random();
//		double value = random.nextDouble();
//
//		// Choose an operation
//		if (value <= .50){
//			passData(packet, destination);
//		} else if (value <= .75){
//			passData(packet, destination);
////			 corruptData(packet, destination);
//		} else{
//			passData(packet, destination);
////			dropData(source);
//		}
//
//	}
//
//	public void sendToDestination(ACK ack, Handler source){
//		// Get the destination
//		Handler destination = destinationMap.get(source);
//
//		// Generate a random value
//		Random random = new Random();
//		double value = random.nextDouble();
//
//		// Choose an operation
//		if (value <= .50){
//			passData(ack, destination);
//		} else if (value <= .75){
//			corruptData(ack, destination);
//		} else{
//			dropData(source);
//		}
//	}

//	/**
//	* Sends the data to the receiver
//	*/
//	public void passData(Packet packet, Handler destination){
//		destination.sendPacket(packet);
//		System.out.println(TAG + "Sending packet to: " + destination.getLinkedClient());
//	}
//
//	/**
//	* Add 1 to the checksum field
//	*/
//	public void corruptData(Packet packet, Handler destination){
//		// Corrupt the data
//		packet.corruptData();
//		// Send the packet to the destination
//		destination.sendPacket(packet);
//		System.out.println(TAG + "Corrupted the packet's data going to: " + destination.getLinkedClient());
//	}