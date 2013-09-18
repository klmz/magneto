package me.magnet.magneto;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class MagnetoTest {

	private Magneto magneto;

	@Mock
	private RequestRouter router;

	@Mock
	private MultiUserChat chat;

	@Captor
	ArgumentCaptor<String> bodyCaptor;

	@Before
	public void setup() {
		magneto = new Magneto(router, new Settings());
	}

	@Test
	public void testProcessMessageTrimming() throws Exception {
		Message message = new Message();
		message.setBody("   x    y   ");
		magneto.processMessage(chat, message);
		User user = new User("Test User");
		verify(router).route(chat, user, bodyCaptor.capture());
		assertThat(bodyCaptor.getValue(), is("x y"));
	}
}
