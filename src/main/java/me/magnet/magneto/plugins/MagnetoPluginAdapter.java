package me.magnet.magneto.plugins;

/**
 * A adapter for the {@link MagnetoPlugin}.
 */
public class MagnetoPluginAdapter implements MagnetoPlugin {

	@Override
	public String getName() {
		return getClass().getName();
	}
}
