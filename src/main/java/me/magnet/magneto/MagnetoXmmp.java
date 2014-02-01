package me.magnet.magneto;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
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

	private final RequestRouter router;
	private final Settings settings;

	public MagnetoXmmp(RequestRouter router, Settings settings) {
		super(router, settings.getUserMention());
		this.router = router;
		this.settings = settings;
	}

	@Override
	public void start() throws Exception {
		SASLAuthentication.supportSASLMechanism("PLAIN", 0);

		String host = settings.getChatServerHost();
		int port = settings.getChatServerPort();

		ConnectionConfiguration config = new ConnectionConfiguration(host, port);
		final XMPPConnection connection = new XMPPConnection(config);
		connection.connect();
		String username = settings.getUserName();
		String password = settings.getUserPassword();

		connection.login(username, password);


		listenToChatRooms(connection);
		listenForOneToOneChats(connection);
		awaitShutdown(connection);
	}



	private void listenToChatRooms(XMPPConnection connection) throws XMPPException {
		Collection<HostedRoom> hostedRooms =
		  MultiUserChat.getHostedRooms(connection, settings.getConferenceServerHost());
		for (final HostedRoom room : hostedRooms) {
			listenToRoom(connection, settings, room);
		}
	}

	private void listenForOneToOneChats(XMPPConnection connection) {
		connection.getChatManager().addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				final XmmpSinglePersonChatRoomRelay relay = new XmmpSinglePersonChatRoomRelay(chat);
				chat.addMessageListener(new MessageListener() {
					@Override
					public void processMessage(Chat chat, Message message) {
						try {
							MagnetoXmmp.this.processMessage(relay, message);
						}
						catch (Exception e) {
							log.error("Cannot process message: {}", e.getMessage(), e);
						}
					}
				});
			}
		});
	}

	private void listenToRoom(XMPPConnection connection, Settings settings, final HostedRoom room)
	  throws XMPPException {
		final MultiUserChat chat = new MultiUserChat(connection, room.getJid());
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

	private void awaitShutdown(final XMPPConnection connection) {
		final CountDownLatch waitForDisconnect = new CountDownLatch(1);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				connection.disconnect();
				waitForDisconnect.countDown();
			}
		}));

		try {
			waitForDisconnect.await();
			log.info("Disconnecting");
		}
		catch (InterruptedException e) {
			log.warn("Exiting before disconnect completed");
		}
		log.info("Shutdown complete");
	}

	@Value
	private static class XmmpChatRoomRelay implements ChatRoom {

		private MultiUserChat chat;

		@Override
		public void sendMessage(String message) throws XMPPException {
			chat.sendMessage(message);
		}

		@Override
		public String getRoom() {
			return chat.getRoom();
		}
	}

	@Value
	private static class XmmpSinglePersonChatRoomRelay implements ChatRoom {

		private Chat chat;

		@Override
		public void sendMessage(String message) throws XMPPException {
			chat.sendMessage(message);
		}

		@Override
		public String getRoom() {
			return chat.getParticipant();
		}
	}
}
