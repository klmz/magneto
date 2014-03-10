package me.magnet.magneto;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.Chat;
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

		boolean cliMode = args.length > 0 && "CLI".equalsIgnoreCase(args[0]);

		Magneto magneto = Guice.createInjector(new MagnetoModule(cliMode)).getInstance(Magneto.class);
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
		try {
			router.route(chat, context, body);
		} catch (DeliveryException e) {
			log.warn("Could not deliver a response. Try sending that to the client. Reason: {}", e.getMessage());
			try {
				chat.sendMessage("It seems one of my responses did not reach you. Please try again.");
			} catch (DeliveryException e2) {
				log.error("The warning for a failed response could not be delivered. Giving up...", e2);
			}
		}
	}

	protected boolean accepts(Message message) {
		if (Strings.isNullOrEmpty(message.getBody())) {
			return false;
		}
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
