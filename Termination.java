/**
 * Created by Michael on 3/23/17.
 */
public class Termination implements Sendable{
	private Byte terminateMessage;

	public Termination(){
		this.terminateMessage = -1;
	}

	public Byte getTerminateMessage(){
		return this.terminateMessage;
	}

	@Override
	public void corruptData() {

	}

	@Override
	public Sendable clone() {
		return null;
	}


}
