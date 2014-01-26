package me.magnet.magneto;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.packet.Message;

/**
 * The command line version of Magneto. Mainly for testing purposes.
 */
@Slf4j
public class MagnetoCli extends Magneto {

	public MagnetoCli(RequestRouter router) {
		super(router, "magneto");
	}

	@Override
	public void start() throws Exception {
		log.info("Starting magneto CLI");
		System.out.println("You are now user 'CLI'. Magneto responds to 'magneto'. Type 'exit' to exit.");
		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
			while (!Thread.interrupted()) {
				printPrefix();
				String command = in.readLine();
				if (command.equalsIgnoreCase("exit")) {
					break;
				} else {
					runCommand(command);
				}
			}
		}
	}

	private void runCommand(String command) throws Exception {
		Message message = new Message();
		message.setBody(command);
		message.setFrom("CLI");
		processMessage(new ChatRoom() {
			@Override
			public void sendMessage(String message) {
				System.out.println("Response: " + message);
			}

			@Override
			public String getRoom() {
				return "CLI";
			}
		}, message);
	}

	private void printPrefix() {
		System.out.print("[Magneto CLI]$ ");
	}
}
