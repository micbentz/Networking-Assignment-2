/**
 * Used so that in the Handler we don't need to use instance of OR overload the functions passdata,
 * etc.. in <code>network</code>
 * Created by Michael on 3/21/17.
 */
public interface Message {
	public void corruptData();
	public Message clone();
}
