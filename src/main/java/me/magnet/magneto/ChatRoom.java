package me.magnet.magneto;

/**
 * A wrapper for a Chat room where messages can be sent to.
 */
public interface ChatRoom {

	public void sendMessage(String message);

	/**
	 * @return The name of the room.
	 */
	public String getRoom();
}
