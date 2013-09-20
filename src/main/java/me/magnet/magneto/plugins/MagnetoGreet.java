package me.magnet.magneto.plugins;

import me.magnet.magneto.MagnetoPlugin;
import me.magnet.magneto.annotations.Param;
import me.magnet.magneto.annotations.RespondTo;
import rx.Observable;
import rx.subjects.Subject;

public class MagnetoGreet implements MagnetoPlugin {
	
	@RespondTo("good {moment}")
	public Observable<String> deploy(final @Param("moment") String moment) {
		String[] splitMoment = moment.split(" ");

		// Profanity check
		for(int i = 0; i < splitMoment.length; i++){
			if(splitMoment[i].contains("sex")){
				return Subject.from("http://youtu.be/hpigjnKl7nI");
			}
		}

		return Subject.from("Good " + moment + " to you too!");
	}
}
