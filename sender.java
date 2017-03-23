import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
* Reads the message file and converts it into many packetList.
* Sends a packet to the Network and awaits a relayed ACK from the Receiver
* NEEDS to be able to sendPacket a new packet or re-sendPacket the same packet according
* to different network situations such as: PASS, CORRUPT, DROP (determined by ACK)
*/
// TODO add comments for currentPacket and lastpacket sequence and update syouts
public class sender{
	private static final String TAG = "Sender> ";
	private static InetAddress network;
	private static ObjectInputStream inputStream;
	private static ObjectOutputStream outputStream;
	private static int PORT;
	private static String messageFileName;
	private static File inputFile;
	private static Scanner fileScanner;
	private static ArrayList<Packet> packetList;
	private static int currentPacket = 0;
	private static int lastPacketSequence = 0;
	private static int lastPacketReceived = 0;
	private static boolean terminatingChar = false;

	public static void main(String[] args){
		try {
			network = InetAddress.getByName(args[0]);
			PORT = Integer.parseInt(args[1]);
			messageFileName = args[2];
			initPackets(messageFileName);
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

	private static void initPackets(String fileName) throws NullPointerException,FileNotFoundException{
		int packetCount = 0;
		// Check if theres a null pointer
		if (messageFileName == null) throw new NullPointerException();

		// Get the file to read from
		inputFile = new File(messageFileName);

		// Check if the file exists
		if(!inputFile.exists()) throw new FileNotFoundException();

		// Create scanner given the file
		fileScanner = new Scanner(inputFile);
		packetList = new ArrayList<>();
		while (fileScanner.hasNext() || !terminatingChar){
			// Check if there is a "."
			String content = fileScanner.next();
			if (content.contains(".")){
				terminatingChar = true;
				content = content.substring(0,content.length()-1);		// Remove the '.' from the word
			}

			Packet packet = new Packet.PacketBuilder()
					.sequenceNumber((byte)(packetList.size() % 2))	// either 0 or 1
					.ID((byte)++packetCount)						// ID of the packet
					.content(content)								// Set the content as the next String
					.build();
			packetList.add(packet);									// Add the packet to the oacketList
			System.out.println(TAG + "created packet: " + packet.toString());
		}
	}

	private static void communicateWithServer(){
		Socket requestSocket = null;
		ACK response = null;

		try{
			requestSocket = new Socket(network,PORT);

			// Initialize inputStream and ObjectOutputStream
			inputStream = new ObjectInputStream(requestSocket.getInputStream());
			outputStream = new ObjectOutputStream(requestSocket.getOutputStream());

			// Send identifier
			outputStream.writeObject(new String("Sender"));
			while(true){ // last message hasn't been sent
				try{
					if (withinBounds()){
//						System.out.println(TAG + "current packet: " + lastPacketSequence + " is within bounds");
						outputStream.writeObject(packetList.get(currentPacket));
						System.out.println("Sending packet: " + currentPacket);
//						lastPacketSequence++;
//						currentPacket++;
					}

					//TODO add check to see if last packet was received
					response = (ACK)inputStream.readObject();
					System.out.println(TAG + " " + response.toString());

					if (packetReceived(response)){
						// update the lastreceived packet
						System.out.println("Packet: " + lastPacketSequence + " was received");
						lastPacketSequence = (lastPacketSequence + 1) % 2;
						currentPacket++;
//						System.out.println(TAG + "packet received! :D");
					} else{ // packet not received set the lastPacketSequence to lastReceived
						// Do nothing, the packet will be retransmitted
						System.out.println(TAG + "packet not received :(");
					}

//					if ()
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

	private static boolean test(ACK response){
		// Check's if the correct packet was sent
		return response.getChecksumValue() == 0;

	}
	private static boolean packetReceived(ACK response){
//		System.out.println("lastpacketsent: " + lastPacketSequence + " ack#: " + response.getSequenceNumber());
//		System.out.println("check sum of response: " + response.getChecksumValue());

		// If the right packet was received and if the packet wasn't dropped
		return (lastPacketSequence == response.getSequenceNumber()) && (response.getChecksumValue() == 0);
	}

	private static boolean withinBounds(){
		System.out.println("current Packet: " + currentPacket +  " packetlist size: " + packetList.size());
		return currentPacket < packetList.size();
	}

	private static void sendMessage(){
		// Packet packet = new Packet(...);
		currentPacket++;
	}

	private static void recievePacket(Packet packet){
		parseMessage(packet);
		printPacket(packet);
	}

	private static void parseMessage(Packet packet){
		// if DROP
		// resend the packet

	}

	// Every time an ACK or DROP is received
	private static void printPacket(Packet packet){
		// Display: current state, total # of packets sent (incl. failure),
		// packet received, proper action

		// i.e.
		// Waiting ACK0, 8, DROP, resend Packet()
		// Waiting ACK0, 15, ACK0, no more packetList to sender
		// Waiting ACK1, 120, ACK1, sendPacket Packet()
	}
}
