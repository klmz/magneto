package me.magnet.magneto;

import javax.inject.Singleton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.hipchat.HipChatApi;
import me.magnet.magneto.hipchat.HipChatModule;
import me.magnet.magneto.plugins.PluginFinder;

@Slf4j
@AllArgsConstructor
public class MagnetoModule extends AbstractModule {

	private final boolean cliMode;

	@Override
	protected void configure() {
		RequestRouter router = new RequestRouter();
		PluginFinder.addPluginsTo(router);
		bind(RequestRouter.class).toInstance(router);

		install(new HipChatModule());

		if (cliMode) {
			bind(Magneto.class).to(MagnetoCli.class).asEagerSingleton();
		}
		else {
			bind(Magneto.class).to(MagnetoXmmp.class).asEagerSingleton();
		}



	}

	@Provides
	@Singleton
	@SneakyThrows
	Settings settings() {
		Settings settings = new Settings();
		settings.load();
		return settings;
	}
}
