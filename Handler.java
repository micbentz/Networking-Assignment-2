import java.net.*;
import java.io.*;

public class Handler extends Thread{
	private final String TAG = "Handler> ";
	private String linkedClient;
	private Socket connection;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Sendable sendable;
	private boolean done = false;


	public Handler(Socket connection){
		this.connection = connection;
	}

	public void run(){
		try{
			// Initialize Input and Output sreams
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(connection.getInputStream());
//			try{
				// Read the linkedClient
			linkedClient = (String)inputStream.readObject();
//			}catch(Exception exception){
//				System.out.println(TAG + " could not connect with network");
//				exception.printStackTrace();
//			}

			while(!done){
				// Receive data from client
				Object object = inputStream.readObject();
				if (isSendable(object)){
					sendable = (Sendable) object;
					passToNetwork(sendable);
					sleep(1000);
				} else{
					Byte terminate = (Byte)object;
					if (terminate == -1 ){
						passToNetwork(terminate);
						done = true;
					}
				}
			}
		}catch(Exception exception){
//			exception.printStackTrace();
//			System.out.println(TAG + "Disconnected from Network");
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

	public void sendTerminateMessage(Byte terminate){
		try{
			outputStream.writeObject(terminate);
		}catch (IOException ioException){

		}
	}

	private boolean isSendable(Object object){
		return object instanceof Sendable;
	}

	private void passToNetwork(Sendable sendable){
		network.getInstance().sendToDestination(sendable,this);
	}

	private void passToNetwork(Byte terminate){
		network.getInstance().sendToDestination(terminate,this);
	}
}