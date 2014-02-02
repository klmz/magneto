package me.magnet.magneto;

import lombok.Value;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

@Value
class XmmpChatRoomRelay implements ChatRoom {

	private MultiUserChat chat;

	@Override
	public void sendMessage(String message)  {
		try {
			chat.sendMessage(message);
		}
		catch (XMPPException e) {
			throw new DeliveryException(e, message);
		}
	}

	@Override
	public String getRoom() {
		return chat.getRoom();
	}
}
