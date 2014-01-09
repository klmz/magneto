package me.magnet.magneto;

/**
 * A adapter for the {@link me.magnet.magneto.MagnetoPlugin}.
 */
public class MagnetoPluginAdapter implements MagnetoPlugin {

	@Override
	public String getName() {
		return getClass().getName();
	}
}
