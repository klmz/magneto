package me.magnet.magneto.plugins;


import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.RequestRouter;
import org.reflections.Reflections;

@Slf4j
public class PluginFinder {

	public static void addPluginsTo(RequestRouter router) {
		Reflections reflections = new Reflections("me.magnet");
		Set<Class<? extends MagnetoPlugin>> plugins = reflections.getSubTypesOf(MagnetoPlugin.class);
		for (Class<? extends MagnetoPlugin> plugin : plugins) {
			try {
				if (plugin.equals(MagnetoPluginAdapter.class)){
					continue;
				}
				else if (plugin.getEnclosingClass() == null) {
					MagnetoPlugin instance = plugin.newInstance();
					router.register(instance);
					log.info("Added plugin {}", plugin);
				} else {
					// This is how we escape classes from the test classpath.
					log.info("Enclosed classes like {} are skipped.", plugin);
				}
			}
			catch (IllegalAccessException | InstantiationException e) {
				log.warn("Could not load {} because: ", plugin, e.getMessage());
				log.debug("Error was", e);
			}
		}
	}
}