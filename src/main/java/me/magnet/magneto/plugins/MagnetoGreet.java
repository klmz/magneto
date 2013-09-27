package me.magnet.magneto.plugins;

import java.util.Random;

import me.magnet.magneto.MagnetoPlugin;
import me.magnet.magneto.annotations.RespondTo;
import rx.Observable;
import rx.subjects.Subject;

public class MagnetoGreet implements MagnetoPlugin {

	public static final String[] responses = {
	        "Hi!",
	        "Hello",
	        "Welcome!",
	        "Good day to you!"
	};

	Random randomGen = new Random();

	/*
	 * Answer to greetings with a random predefined response.
	 */
	@RespondTo("\\b([Hh]ello|[Hh]i).*")
	public Observable<String> deploy() {

		// Return a random entry in 'responses'
		int random = randomGen.nextInt(responses.length);
		return Subject.from(responses[random]);
	}
}
