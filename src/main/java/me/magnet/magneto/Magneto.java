package me.magnet.magneto;

import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.plugins.PluginFinder;
import org.jivesoftware.smack.packet.Message;

/**
 * Entry point class for magneto. Start it using the main method.
 */
@Slf4j
public abstract class Magneto {

	private final RequestRouter router;
	private final String userMention;

	public Magneto(RequestRouter router, String userMention) {
		this.router = router;
		this.userMention = userMention;
	}

	/**
	 * Start Magneto.
	 *
	 * @param args if args is "CLI" the command line interface will be started.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		RequestRouter router = new RequestRouter();

		PluginFinder.addPluginsTo(router);

		Magneto magneto;
		if (args.length > 0 && "CLI".equalsIgnoreCase(args[0])) {
			magneto = new MagnetoCli(router);
		}
		else {
			Settings settings = new Settings();
			settings.load();
			magneto = new MagnetoXmmp(router, settings);
		}
		magneto.start();
	}

	public abstract void start() throws Exception;

	protected void processMessage(final ChatRoom chat, Message message) throws Exception {
		User user = User.of(message.getFrom());
		Context context = Context.builder()
		  .room(chat.getRoom())
		  .from(user)
		  .build();
		if (!accepts(message)) {
			return;
		}

		String body = message.getBody().trim();
		while (body.contains("  ")) {
			body = body.replaceAll("  ", " ");
		}
		body = body.substring(userMention.length() + 1);
		router.route(chat, context, body);
	}

	private boolean accepts(Message message) {
		String body = message.getBody().trim();
		if (!body.contains(" ")) {
			return false;
		}

		String first = body.substring(0, body.indexOf(' '));
		if (!userMention.equalsIgnoreCase(first)) {
			return false;
		}
		return true;
	}

}
