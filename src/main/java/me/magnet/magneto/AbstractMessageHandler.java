package me.magnet.magneto;

import lombok.extern.slf4j.Slf4j;

/**
 * A observer that watches for new messages.
 *
 */
@Slf4j
public abstract class AbstractMessageHandler {

	public abstract void newMessage(String message) throws Exception;

	/**
	 * Can be overwritten to handle errors. If not overwritten, it sends the error message
	 * @param e
	 */
	public void onError(Throwable e) {
		try {
			newMessage("Could not complete. Error: " + e.getMessage());
		} catch (Exception e1) {
			log.error("Could not send message, {}", e1.getMessage(), e);
		}
	}

	/**
	 * Is called when the message stream is done.
	 */
	protected void onComplete() {

	}
}
