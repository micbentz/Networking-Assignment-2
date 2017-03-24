import java.net.*;
import java.io.*;

/**
 * A <code>Handler</code> is a Thread created by the <code>network</code> to handle
 * the socket connection with a client (receiver or sender)
 */

public class Handler extends Thread{
	private final String TAG = "Handler> ";		// TAG used for debugging
	private String linkedClient;				// Attached client via socket
	private Socket connection;					// Connection to the client (receiver or sender)
	private ObjectInputStream inputStream;		// Stream to read Objects from client
	private ObjectOutputStream outputStream;	// Stream to write Objects to client
	private boolean done = false;				// Flag to stop thread


	public Handler(Socket connection){
		this.connection = connection;
	}

	public void run(){
		try{
			// Initialize Input and Output sreams
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(connection.getInputStream());

			// Read the linkedClient for debugging purposes
			linkedClient = (String)inputStream.readObject();

			while(!done){
				// Receive data from client
				Object object = inputStream.readObject();

				// If it's a sendable
				if (isSendable(object)){
					Sendable sendable = (Sendable) object;	// Get the Sendable object
					passToNetwork(sendable);				// Pass to the network
					sleep(1000);						// Sleep for 1sec for delayed output
				} else{ // Terminating bit
					Byte terminate = (Byte)object;  		// Get the byte
					if (terminate == -1 ){					// If it's equal to -1
						passToNetwork(terminate);			// Pass termination command to network
						done = true;
					}
				}
			}
		}catch(Exception exception){

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

	// Returns the linked client (for debugging purposes)
	public String getLinkedClient(){
		return this.linkedClient;
	}

	// Sends the message to the connected client (receiver or sender)
	public void sendMessage(Sendable sendable){
		try{
			outputStream.writeObject(sendable);
		}catch(IOException ioException){

		}
	}

	// Sends the termination message to the receiver
	public void sendTerminateMessage(Byte terminate){
		try{
			outputStream.writeObject(terminate);
		}catch (IOException ioException){

		}
	}

	// Checks if the object is a Sendable
	private boolean isSendable(Object object){
		return object instanceof Sendable;
	}

	// Passes the message to the network
	private void passToNetwork(Sendable sendable){
		network.getInstance().sendToDestination(sendable,this);
	}

	// Passes the termination code to the network
	private void passToNetwork(Byte terminate){
		network.getInstance().sendToDestination(terminate,this);
	}
}