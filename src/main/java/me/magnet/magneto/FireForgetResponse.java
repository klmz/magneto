package me.magnet.magneto;


import java.util.Queue;

import com.google.common.collect.Lists;


public class FireForgetResponse extends Response {

	private final Queue<String> messagesToSend = Lists.newLinkedList();

	protected FireForgetResponse sendMessage(String message) {
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
		} finally {
			handler.onComplete();
		}
	}
}
