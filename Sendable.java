/**
 * The <code>Sendable</code> interface provides polymorphism to passing data between
 * <code>sender</code> and <code>receiver</code> without having to check for type.
 * Created by Michael on 3/21/17.
 */
public interface Sendable {
	public void corruptData();
	public Sendable clone();
}
