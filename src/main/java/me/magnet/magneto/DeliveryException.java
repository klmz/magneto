package me.magnet.magneto;

import org.jivesoftware.smack.XMPPException;

public class DeliveryException extends RuntimeException {

	public DeliveryException(XMPPException cause, String message) {
		super("Could not send " + message, cause);
	}
}
