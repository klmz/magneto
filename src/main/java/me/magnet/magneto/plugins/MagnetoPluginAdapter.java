package me.magnet.magneto.plugins;

/**
 * A adapter for the {@link MagnetoPlugin}.
 */
public class MagnetoPluginAdapter implements MagnetoPlugin {

	/**
	 * @return the classname by default. Override to implement a pretty name.
	 */
	@Override
	public String getName() {
		return getClass().getName();
	}
}
