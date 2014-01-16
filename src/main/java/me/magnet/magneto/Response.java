package me.magnet.magneto;


public abstract class Response {

	public static Response fireAndForget() {
		return new FireForgetResponse();
	}

	/**
	 * Send a message to the client.
	 *
	 * @param message The message.
	 * @return The response for method chaining.
	 */
	public abstract Response sendMessage(String message);

	/**
	 * Set a message handler for this response.
	 *
	 * @param handler The handler.
	 */
	protected abstract void setHandler(AbstractMessageHandler handler);


}
