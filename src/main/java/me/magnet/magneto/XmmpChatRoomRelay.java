package me.magnet.magneto;

import lombok.Value;
import me.magnet.magneto.hipchat.HipChatApi;
import me.magnet.magneto.hipchat.HipChatNotification;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

@Value
class XmmpChatRoomRelay implements ChatRoom {

	private MultiUserChat chat;
	private HipChatApi hipChatApi;

	@Override
	public void sendMessage(String message) {
		try {
			chat.sendMessage(message);
		}
		catch (XMPPException e) {
			throw new DeliveryException(message, e);
		}
	}

	@Override
	public void sendHtml(HipChatNotification message) {
		hipChatApi.send(message, chat.getRoom());
	}

	@Override
	public void sendHtml(String message) {
		sendHtml(new HipChatNotification(message));
	}


	@Override
	public String getRoom() {
		return chat.getRoom();
	}
}
