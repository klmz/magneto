package me.magnet.magneto;

import me.magnet.magneto.hipchat.HipChatNotification;

/**
 * A wrapper for a Chat room where messages can be sent to.
 */
public interface ChatRoom {

	/**
	 * Send plain text message to the chat room.
	 *
	 * @param message The message you want to send.
	 */
	public void sendMessage(String message);

	/**
	 * Send HTML to the client. From the hipchat docs:
	 * <p>Message is rendered as HTML and receives no special treatment.
	 * Must be valid HTML and entities must be escaped (e.g.: '&amp;' instead of '&').
	 * May contain basic tags: <code>a, b, i, strong, em, br, img, pre, code, lists, tables.</code>
	 * Special HipChat features such as @mentions, emoticons, and image previews are
	 * NOT supported when using this format.</p>
	 *
	 * @param message The message you want to send.
	 * @see <a href="https://www.hipchat.com/docs/apiv2/method/send_room_notification">The HipChat API Docs</a>
	 */
	public void sendHtml(HipChatNotification message);

	/**
	 * @param message The message you want to send.
	 * @see {@link #sendHtml(me.magnet.magneto.hipchat.HipChatNotification)}
	 */
	public void sendHtml(String message);

	/**
	 * @return The name of the room.
	 */
	public String getRoom();
}
