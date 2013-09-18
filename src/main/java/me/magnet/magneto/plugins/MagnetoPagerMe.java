package me.magnet.magneto.plugins;

import me.magnet.magneto.MagnetoPlugin;
import me.magnet.magneto.annotations.Param;
import me.magnet.magneto.annotations.RespondTo;
import rx.Observable;
import rx.subjects.Subject;

public class MagnetoPagerMe implements MagnetoPlugin {

	@RespondTo("pager me {minutes}")
	public Observable<String> deploy(final @Param("minutes") long minutes) {
		return Subject.from("You're now on pager duty for " + minutes + " minutes", "(Okay, that was a joke...)");
	}
	
}
