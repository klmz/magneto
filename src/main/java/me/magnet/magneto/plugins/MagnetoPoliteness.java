package me.magnet.magneto.plugins;

import java.util.Random;

import me.magnet.magneto.MagnetoPlugin;
import me.magnet.magneto.annotations.RespondTo;
import rx.Observable;
import rx.subjects.Subject;

public class MagnetoPoliteness implements MagnetoPlugin {

	public static final String[] responses = {
	        "You're welcome.",
	        "No problem.",
	        "Anytime.",
	        "That's what I'm here for!",
	        "You are more than welcome.",
	        "You don't have to thank me, I'm your loyal servant.",
	        "Don't mention it."
	};

	/*
	 * Responds to basic expressions of gratitude with a random predefined response.
	 */
	@RespondTo("\\b([tT]hanks|ty|TY|[tT]hank you).*")
	public Observable<String> deploy() {
		int random = new Random().nextInt(responses.length);
		return Subject.from(responses[random]);
	}
}
