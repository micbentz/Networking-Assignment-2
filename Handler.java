import java.net.*;
import java.io.*;

public class Handler extends Thread{
	private final String TAG = "Handler> ";
	private String linkedClient;
	private Socket connection;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Message message;


	public Handler(Socket connection){
		this.connection = connection;
	}

	public void run(){
		try{
			// Initialize Input and Output sreams
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(connection.getInputStream());

			try{
				// Read the linkedClient
				linkedClient = (String)inputStream.readObject();
				while(true){
					// Receive data from client
					message = (Message) inputStream.readObject();
					System.out.println(TAG +  "received: " + message.toString() + " from " + linkedClient);
					passToNetwork(message);
				}
			}catch(Exception exception){
				System.out.println(TAG + " could not connect with network");
				exception.printStackTrace();
			}
		}catch(Exception exception){
			exception.printStackTrace();
			System.out.println(TAG + "Disconnected from Network");
		}
		finally{
			// Close the connection
			try{
				inputStream.close();
				outputStream.close();
				connection.close();
			}catch(Exception exception){
				System.out.println(TAG + "Disconnected from Network");
			}
		}
	}

	public String getLinkedClient(){
		return this.linkedClient;
	}

	public void sendMessage(Message message){
		try{
			outputStream.writeObject(message);
		}catch(IOException ioException){

		}
	}

	private void passToNetwork(Message message){
		network.getInstance().sendToDestination(message,this);
	}
}



//	private void passToNetwork(Packet packet){
//		network.getInstance().sendToDestination(packet,this);
//	}
//
//	private void passToNetwork(ACK ack){
//		network.getInstance().sendToDestination(ack,this);
//	}

//	public void sendPacket(Packet packet){
//		try{
//			outputStream.writeObject(packet);
//		}catch(Exception exception){
//			exception.printStackTrace();
//		}
//	}
//
//	public void sendACK(ACK ack){
//		try{
//			outputStream.writeObject(ack);
//		}catch(Exception exception){
//			exception.printStackTrace();
//		}
//	}