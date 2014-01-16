package me.magnet.magneto;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * The Xmmp verison of Magneto. Requires a configuration.properties to be on the classpath.
 */
@Slf4j
public class MagnetoXmmp extends Magneto {

	@Value
	private static class XmmpChatRoomRelay  implements ChatRoom {

		MultiUserChat chat;

		@Override
		public void sendMessage(String message) throws XMPPException {
			chat.sendMessage(message);
		}
	}

	private final RequestRouter router;
	private final Settings settings;

	public MagnetoXmmp(RequestRouter router, Settings settings) {
		super(router, settings.getUserMention());
		this.router = router;
		this.settings = settings;
	}

	@Override
	public void start() throws Exception	{
		SASLAuthentication.supportSASLMechanism("PLAIN", 0);

		String host = settings.getChatServerHost();
		int port = settings.getChatServerPort();

		ConnectionConfiguration config = new ConnectionConfiguration(host, port);
		final XMPPConnection connection = new XMPPConnection(config);
		connection.connect();
		String username = settings.getUserName();
		String password = settings.getUserPassword();

		connection.login(username, password);

		Collection<HostedRoom> hostedRooms =
		  MultiUserChat.getHostedRooms(connection, settings.getConferenceServerHost());
		for (final HostedRoom room : hostedRooms) {
			listenToRoom(connection, settings, room);
		}

		final CountDownLatch waitForDisconnect = new CountDownLatch(1);
		log.info("Disconnecting");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				connection.disconnect();
				waitForDisconnect.countDown();
			}
		}));

		try {
			waitForDisconnect.await(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			log.warn("Exiting before disconnect completed");
		}
		log.info("Shutdown complete");
	}

	private void listenToRoom(XMPPConnection connection, Settings settings, final HostedRoom room)
	  throws XMPPException {
		MultiUserChat chat = new MultiUserChat(connection, room.getJid());
		DiscussionHistory discussionHistory = new DiscussionHistory();
		discussionHistory.setMaxChars(0);
		discussionHistory.setMaxStanzas(0);
		discussionHistory.setSeconds(0);

		chat.join(settings.getUserDisplayName(), null, discussionHistory, 30000);
		log.info("Joined room: " + room.getJid());
		final XmmpChatRoomRelay relay = new XmmpChatRoomRelay(chat);
		chat.addMessageListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				if (packet instanceof Message) {
					try {
						processMessage(relay, (Message) packet);
					}
					catch (Exception e) {
						log.error("Cannot process message: {}", e.getMessage(), e);
					}
				}
			}
		});
	}
}
