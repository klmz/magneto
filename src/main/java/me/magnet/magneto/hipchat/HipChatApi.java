package me.magnet.magneto.hipchat;

public interface HipChatApi {
	void send(HipChatNotification notification, String toRoom);
}
