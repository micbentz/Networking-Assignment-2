import java.util.*;
import java.net.*;
import java.io.*;

/**
 * The <code>network</code> is a single instance meant to simulate a lossy channel with bit errors
 * by PASSing, CORRUPTing or DROPing messages between the <code>sender</code>
 * and <code>receiver</code>. The network creates a <code>Handler</code> thread
 * for both sender and receiver.
 */
public class network{
	private static network instance = null;					// single instance of
	private static final String TAG = "Network> ";			// TAG used for debugging
	private static ServerSocket listener;					// ServerSocket
	private static Handler[] handlers;						// Array to hold the Handlers
	private static HashMap<Handler,Handler> destinationMap;	// Map to create a pairing between the Handlers
	private static int PORT;								// PORT
	private static int clientCount = 0;						// # of clients connected i.e. receiver & sender

	// Synchronized getInstance() allows the network to be shared by both Handlers
	public synchronized static network getInstance(){
		if (instance == null){
			instance = new network();
		}
		return instance;
	}

	// TODO fix the ordering of the try catch statements
	public static void main(String[] args){
		int PORT = Integer.parseInt(args[0]);
		System.out.println(TAG + "Network opening port: " + PORT);

		// Initializing variables
		destinationMap = new HashMap<>();
		handlers = new Handler[2];

		try{
			// Setup ServerSocket at the given port
			listener = new ServerSocket(PORT);
		}catch(Exception exception){
			System.out.println(TAG + "Unable to attach to port");
			System.exit(1);
		}

		// TODO check this condition
		while (true){
			try{
				// If there is a receiver and sender create the pairing
				if (clientCount == 2){
					createPairing();
				}
				// Wait until connection attempt is made and accept
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

	// Creates a pairing between the Receiver Handler and Sender Handler
	private static void createPairing(){
		Handler receiver, sender = null;

		// Get the receiver and sender Handler
		receiver = handlers[0];
		sender = handlers[1];

		// Create the mapping from receiver to sender
		// and vice versa
		destinationMap.put(receiver,sender);
		destinationMap.put(sender,receiver);
	}

	// Sends the sendable to the destination given the source Handler
	// and chooses, at random, to: PASS, CORRUPT, or DROP the sendable.
	public void sendToDestination(Sendable sendable, Handler source){
		// Get the destination
		Handler destination = destinationMap.get(source);

		// Generate a random value
		Random random = new Random();
		double value = random.nextDouble();

		// Choose an operation
		if (value <= .50){ // 50% probability to PASS
			passData(sendable, destination);
			printMessage(sendable,"PASS");
		} else if (value <= .75){ // 25% probability to CORRUPT
			 corruptData(sendable, destination);
			 printMessage(sendable,"CORRUPT");
		} else{ // 25% probability to DROP
			dropData();
			printMessage(sendable,"DROP");
		}
	}

	// Passes the data to the destination handler
	public void passData(Sendable sendable, Handler destination){
		destination.sendMessage(sendable);
	}

	// Corrupts the data being passed by incrementing the checksum
	public void corruptData(Sendable sendable, Handler destination){
		// Clone the sendable so that it's original contents aren't changed
		Sendable copy = sendable.clone();
		// Corrupt the data
		copy.corruptData();
		destination.sendMessage(copy);
	}

	// Drops the message and sends ACK2 to the Sender Handler
	public void dropData(){
		Handler senderHandler = handlers[1];
		// Create an ACK
		ACK dropAck = new ACK((byte)2,(byte)0);
		// Send the ack back to the sender
		senderHandler.sendMessage(dropAck);
	}

	// Prints the sendable being sent and the corresponding
	// operation taken on the sendable
	private void printMessage(Sendable sendable, String operation){
		System.out.println("Received: " + sendable.toString() + operation);
	}
}