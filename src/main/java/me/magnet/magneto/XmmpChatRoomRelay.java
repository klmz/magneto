package me.magnet.magneto;

import com.google.common.net.UrlEscapers;
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
		hipChatApi.send(message, getRoomId(chat));
	}

	private String getRoomId(MultiUserChat chat) {
		String room = chat.getRoom();
		int index = room.indexOf('_');
		int atIndex = room.indexOf('@');
		if (index > 0) {
			room = room.substring(index + 1, atIndex).replaceAll("_", " ");
			return UrlEscapers.urlPathSegmentEscaper().escape(room);
		}
		else {
			return room;
		}
	}

	@Override
	public String getRoom() {
		return chat.getRoom();
	}
}
