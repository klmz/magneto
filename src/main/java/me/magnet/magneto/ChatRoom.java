package me.magnet.magneto;

import org.jivesoftware.smack.XMPPException;

public interface ChatRoom {

	public void sendMessage(String message) throws XMPPException;
}
