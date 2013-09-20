package me.magnet.magneto;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.plugins.MagnetoDeploy;
import me.magnet.magneto.plugins.MagnetoGreet;
import me.magnet.magneto.plugins.MagnetoPagerMe;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

@Slf4j
public class Magneto {

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.load();

		RequestRouter router = new RequestRouter();
		router.register(new MagnetoDeploy());
		router.register(new MagnetoPagerMe());
		router.register(new MagnetoGreet());

		Magneto magneto = new Magneto(router, settings);
		magneto.start();
	}

	private final RequestRouter router;
	private final Settings settings;

	public Magneto(RequestRouter router, Settings settings) {
		this.router = router;
		this.settings = settings;
	}

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

		Collection<HostedRoom> hostedRooms = MultiUserChat.getHostedRooms(connection, settings.getConferenceServerHost());
		for (final HostedRoom room : hostedRooms) {
			listenToRoom(connection, settings, room);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				connection.disconnect();
			}
		}));

		Thread.currentThread().join();

	}

	private void listenToRoom(XMPPConnection connection, Settings settings, final HostedRoom room) throws XMPPException {
		final MultiUserChat chat = new MultiUserChat(connection, room.getJid());
		DiscussionHistory discussionHistory = new DiscussionHistory();
		discussionHistory.setMaxChars(0);
		discussionHistory.setMaxStanzas(0);
		discussionHistory.setSeconds(0);

		chat.join(settings.getUserDisplayName(), null, discussionHistory, 30000);
		log.info("Joined room: " + room.getJid());

		chat.addMessageListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				if (packet instanceof Message) {
					try {
						processMessage(chat, (Message) packet);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void processMessage(final MultiUserChat chat, Message message) throws Exception {
		User user = User.of(message.getFrom());
		if (!accepts(user, message)) {
			return;
		}

		String body = message.getBody().trim();
		body = body.substring(body.indexOf(' ') + 1);
		while (body.contains("  ")) {
			body = body.replace("  ", " ");
		}

		router.route(chat, user, body);
	}

	private boolean accepts(User user, Message message) {
		String body = message.getBody().trim();
		if (!body.contains(" ")) {
			return false;
		}

		String first = body.substring(0, body.indexOf(' '));
		if (!settings.getUserMention().equalsIgnoreCase(first)) {
			return false;
		}

		return true;
	}

}
