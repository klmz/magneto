package me.magnet.magneto;

import org.jivesoftware.smack.XMPPException;

/**
 * A wrapper for a Chat room where messages can be sent to.
 */
public interface ChatRoom {

	public void sendMessage(String message) throws XMPPException;

	/**
	 * @return The name of the room.
	 */
	public String getRoom();
}
