package me.magnet.magneto;


import java.util.Queue;

import com.google.common.collect.Lists;


/**
 * A non-streaming response. Everything send is send after the response is returned.
 * After that the response is disposed. Any exceptions are reported to the client and put in the log.
 */
public class FireForgetResponse extends Response {

	private final Queue<String> messagesToSend = Lists.newLinkedList();

	public FireForgetResponse sendMessage(String message) {
		messagesToSend.add(message);
		return this;
	}

	protected void setHandler(AbstractMessageHandler handler) {
		try {
			for (String message : messagesToSend) {
				handler.newMessage(message);
			}
		}
		catch (Exception e) {
			handler.onError(e);
		}
		finally {
			handler.onComplete();
		}
	}
}
