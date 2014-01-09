package me.magnet.magneto.plugins;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import me.magnet.magneto.RequestRouter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PluginFinderTest {

	@Mock
	private RequestRouter router;

	@Test
	public void testThatPluginsOnTheClassPathAreFound() {
		PluginFinder.addPluginsTo(router);
		verify(router, atLeastOnce()).register(any(MagnetoPlugin.class));
	}
}
