package me.magnet.magneto.plugins;

import java.util.Random;

import me.magnet.magneto.annotations.RespondTo;
import rx.Observable;
import rx.subjects.Subject;

public class MagnetoPoliteness implements MagnetoPlugin {

	public static final String[] RESPONSES = {
	  "You're welcome.",
	  "No problem.",
	  "Anytime.",
	  "That's what I'm here for!",
	  "You are more than welcome.",
	  "You don't have to thank me, I'm your loyal servant.",
	  "Don't mention it."
	};

	public static final String[] WELCOMES = {
	  "Hi!",
	  "Hello",
	  "Welcome!",
	  "Good day to you!"
	};

	Random randomGen = new Random();

	/*
	 * Responds to basic expressions of gratitude with a random predefined response.
	 */
	@RespondTo("\\b([tT]hanks|ty|TY|[tT]hank you).*")
	public Observable<String> thanks() {
		int random = randomGen.nextInt(RESPONSES.length);
		return Subject.from(RESPONSES[random]);
	}

	/*
	 * Answer to greetings with a random predefined response.
	 */
	@RespondTo("\\b([Hh]ello|[Hh]i).*")
	public Observable<String> hi() {
		int random = randomGen.nextInt(RESPONSES.length);
		return Subject.from(WELCOMES[random]);
	}

	@Override
	public String getName() {
		return "Magneto politeness";
	}
}
