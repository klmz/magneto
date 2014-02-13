package me.magnet.magneto;

public class DeliveryException extends RuntimeException {

	public DeliveryException(String message) {
		super("Could not send " + message);
	}

	public DeliveryException(String message, Exception cause) {
		super("Could not send " + message, cause);
	}
}
