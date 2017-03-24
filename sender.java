import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The <code>sender</code> uses rdt3.0 protocols to send <code>Packet</code>s
 * to the <code>receiver</code>. The <code>Packet</code>s are created by reading in
 * from a text file.
 */
public class sender{
private static final String TAG = "Sender> ";			// Tag used for debugging
	private static InetAddress network;					// The IP Adress of the network
	private static ObjectInputStream inputStream;		// Stream used for receiving Objects from Handler
	private static ObjectOutputStream outputStream;		// Stream for sending Objects to Handler
	private static int PORT;							// PORT to connect to
	private static ArrayList<Packet> packetList;		// List holding all of the packets created
	private static int currentPacket = 0;				// Current packet being sent from the packetList
	private static int lastPacketSequence = 0;			// Most recent packet seq# that was sent
	private static int totalPacketsSent = 0;			// The total number of packets sent
	private static boolean lastMessageSent = false;		// True if all packets have been sent successfully
	private static HashMap<Integer,String> packetMap;	// Mapping of output message for corresponding ACK
	private static String actionTaken;					// Holds the message taken by sender


	public static void main(String[] args){
		try {
			network = InetAddress.getByName(args[0]);	// Get the IP Address
			PORT = Integer.parseInt(args[1]);			// Get the port number
			String messageFileName = args[2];			// Get the file name
			initPackets(messageFileName);				// Initialize packets given file

			// Initialize and populate the map
			packetMap = new HashMap<>();
			packetMap.put(0,"ACK0");
			packetMap.put(1,"ACK1");
			packetMap.put(2,"DROP");
		}catch(UnknownHostException exception){
			System.out.println(TAG + "Network not found");
			System.exit(1);
		}catch(ArrayIndexOutOfBoundsException outOfBounds){
			System.out.println(TAG + "Not all arguments were given.");
			System.exit(1);
		}catch(FileNotFoundException noFile){
			System.out.println(TAG + "Text file not found.");
			System.exit(1);
		}
		communicateWithServer();
	}

	// Initializes the packets given the file name
	private static void initPackets(String fileName) throws NullPointerException,FileNotFoundException{
		File inputFile;
		Scanner fileScanner;
		int packetCount = 0;

		if (fileName == null) throw new NullPointerException();		// Check if there's a null pointer
		inputFile = new File(fileName);								// Get the file to read from
		if(!inputFile.exists()) throw new FileNotFoundException();	// Check if the file exists
		fileScanner = new Scanner(inputFile);						// Create scanner given the file

		packetList = new ArrayList<>();								// Create list to hold packets
		while (fileScanner.hasNext()){								// While there are words in the file
			String content = fileScanner.next();					// Get the next String

			// Create the packet
			Packet packet = new Packet.PacketBuilder()
					.sequenceNumber((byte)(packetList.size() % 2))	// either 0 or 1
					.ID((byte)++packetCount)						// ID of the packet
					.content(content)								// Set the content as the next String
					.build();
			packetList.add(packet);									// Add the packet to the oacketList
		}
	}

	// Communicate with the network
	private static void communicateWithServer(){
		Socket requestSocket = null;
		String ackReceived = "";
		int sequenceNum = 0;
		ACK ack = null;

		try{
			// Create socket
			requestSocket = new Socket(network,PORT);

			// Initialize inputStream and ObjectOutputStream
			inputStream = new ObjectInputStream(requestSocket.getInputStream());
			outputStream = new ObjectOutputStream(requestSocket.getOutputStream());

			// Send identifier for debugging purposes
			outputStream.writeObject(new String("Sender"));

			// While the last message hasnt been sent
			while(!lastMessageSent){
				try{
					// There are still packets to be sent
					if (withinBounds()){
						outputStream.writeObject(packetList.get(currentPacket));	// Send packet to Handler
						ack = (ACK)inputStream.readObject();						// Read the ACK
						sequenceNum = ack.getSequenceNumber();
						totalPacketsSent++;

						// If the packet was properly received by the receiver
						if (packetReceived(ack)){
							currentPacket++;										// Update the index of the packetList
							if (withinBounds()){									// Need to check again because index was updated
								lastPacketSequence = (lastPacketSequence + 1) % 2;	// Update the last packet that was received
								actionTaken = "send Packet" + lastPacketSequence;	// Update the action
							}
						} else{														// The packet was not received
							actionTaken = "resend Packet" + lastPacketSequence;		// Do nothing, the sender will retransmit the current packet
						}
					} else{ // There are no more packets left to be sent
						lastMessageSent = true;
						actionTaken = "no more packets to send";					// Update the action
						outputStream.writeObject((byte)-1);							// Send termination message
					}
					System.out.println("Waiting: ACK" + lastPacketSequence + ", " + totalPacketsSent + ", " +  packetMap.get(sequenceNum) + ", " + actionTaken);
				}catch(ClassNotFoundException notFound){
					notFound.printStackTrace();
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

	// Checks if the packet was received by the "receiver"
	private static boolean packetReceived(ACK ack){
		// If the right packet was received and if the packet wasn't dropped
		return (lastPacketSequence == ack.getSequenceNumber()) && (ack.getChecksumValue() == 0);
	}

	// Checks if the index is a valid position
	private static boolean withinBounds(){
		return currentPacket < packetList.size();
	}
}
