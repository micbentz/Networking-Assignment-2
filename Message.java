/**
 * The <code>Message</code> interface provides polymorphism to passing data between
 * <code>sender</code> and <code>receiver</code> without having to check for type.
 * Created by Michael on 3/21/17.
 */
public interface Message {
	public void corruptData();
	public Message clone();
}
