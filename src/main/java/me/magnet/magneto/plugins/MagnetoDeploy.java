package me.magnet.magneto.plugins;

import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.MagnetoPlugin;
import me.magnet.magneto.annotations.Param;
import me.magnet.magneto.annotations.RespondTo;
import rx.Observable;
import rx.subjects.ReplaySubject;

@Slf4j
public class MagnetoDeploy implements MagnetoPlugin {

	@RespondTo("deploy {what} to {where}")
	public Observable<String> deploy(final @Param("what") String what, final @Param("where") String where) {
		final ReplaySubject<String> subject = ReplaySubject.create();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				subject.onNext("Deploying: " + what + " to " + where + "...");
				for (int i = 1; i <= 3; i++) {
					subject.onNext("Removing: " + where + "-" + i + " from application pool...");
					delay(1000);
					
					subject.onNext("Terminating: " + what + " on " + where + "-" + i + " ...");
					delay(2000);
					
					subject.onNext("Upgrading: " + what + " on " + where + "-" + i + " ...");
					delay(2000);
					
					subject.onNext("Starting: " + what + " on " + where + "-" + i + " ...");
					delay(3000);
					
					subject.onNext("Adding: " + where + "-" + i + " to application pool...");
					delay(1000);
				}
				subject.onNext("Deployment completed");
			}
		}).start();
		
		return subject;
	}
	
	private void delay(int millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

}
