import java.net.*;
import java.io.*;

public class Handler extends Thread{
	private final String TAG = "Handler> ";
	private String linkedClient;
	private Socket connection;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Sendable sendable;


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
					sendable = (Sendable) inputStream.readObject();
					passToNetwork(sendable);
					sleep(1000);
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

	public void sendMessage(Sendable sendable){
		try{
			outputStream.writeObject(sendable);
		}catch(IOException ioException){

		}
	}

	private void passToNetwork(Sendable sendable){
		network.getInstance().sendToDestination(sendable,this);
	}
}