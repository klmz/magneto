package me.magnet.magneto;

import lombok.Value;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

@Value
class XmmpSinglePersonChatRoomRelay implements ChatRoom {

	private Chat chat;

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
		return chat.getParticipant();
	}
}
