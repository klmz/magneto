package me.magnet.magneto;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;
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

	private final RequestRouter router;
	private final Settings settings;
	private final Set<String> joinedRooms = Sets.newHashSet();
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

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

		listenForOneToOneChats(connection);
		listenToChatRooms(connection);
		awaitShutdown(connection);
	}


	private void listenToChatRooms(final XMPPConnection connection) throws XMPPException {
		executorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					checkForNewRooms(connection);
				}
				catch (XMPPException | RuntimeException e) {
					log.error("Could not listen to room.", e);
				}
			}
		}, 0, 10, TimeUnit.SECONDS);

	}

	private void checkForNewRooms(XMPPConnection connection) throws XMPPException {
		Collection<HostedRoom> hostedRooms =
		  MultiUserChat.getHostedRooms(connection, settings.getConferenceServerHost());
		Set<String> encountered = Sets.newHashSet();
		synchronized (joinedRooms) {
			for (final HostedRoom room : hostedRooms) {
				encountered.add(room.getJid());
				if (joinedRooms.add(room.getJid())) {
					listenToRoom(connection, settings, room);
				}
			}
			if (joinedRooms.retainAll(encountered)) {
				log.info("One or more rooms were deleted");
			}
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
			executorService.shutdown();
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			log.warn("Exiting before disconnect completed");
		}
		log.info("Shutdown complete");
	}


}
