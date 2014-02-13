package me.magnet.magneto;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import me.magnet.magneto.hipchat.HipChatApi;
import org.jivesoftware.smack.packet.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MagnetoTest {

	private Magneto magneto;

	@Mock
	private RequestRouter router;

	@Mock
	private ChatRoom chat;

	@Captor
	ArgumentCaptor<String> bodyCaptor;

	@Mock
	private HipChatApi hipchatApi;

	@Before
	public void setup() throws IOException {
		Settings sets = new Settings();
		sets.load();
		magneto = new MagnetoXmmp(router, sets, hipchatApi);
	}

	@Test
	public void testProcessMessageTrimming() throws Exception {
		Message message = new Message();
		User user = new User("Test User");
		message.setFrom(user.getFullName());
		message.setBody("@magneto   x    y   ");
		magneto.processMessage(chat, message);
		verify(router).route(eq(chat), any(Context.class), bodyCaptor.capture());
		assertThat(bodyCaptor.getValue(), is("x y"));
	}
}
