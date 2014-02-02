package me.magnet.magneto;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import me.magnet.magneto.annotations.Param;
import me.magnet.magneto.annotations.RespondTo;
import me.magnet.magneto.plugins.MagnetoPluginAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HandlerTest {

	@Data
	@NoArgsConstructor
	@Accessors(chain = true)
	public static class TestPlugin extends MagnetoPluginAdapter {

		private String first;
		private String second;

		@RespondTo("test {a} and {b}")
		public void deploy(
		  final @Param("a") String first,
		  final @Param("b") String second) {
			this.first = first;
			this.second = second;
		}

		@RespondTo("context {a} and {b}")
		public void deploy(
		  final @Param("a") String first,
		  final @Param("b") String second,
		  final Context context) {
			this.first = first;
			this.second = second;
		}

	}

	private TestPlugin testPlugin;
	private Handler handler;
	private User user;
	private Context context;

	@Mock
	private ChatRoom chat;


	@Before
	public void setup() throws NoSuchMethodException, SecurityException {
		testPlugin = new TestPlugin();
		Method method =
		  testPlugin.getClass().getDeclaredMethod("deploy", String.class, String.class);
		handler = new Handler(testPlugin, method);
		user = new User("Example User");
		context = Context.builder().from(user).room("test").build();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void acceptsMethod() throws Exception {
		String query = "test example1 and example2";
		assertThat(handler.accepts(query), is(true));
		handler.handle(chat, context, query);
		assertThat(testPlugin.getFirst(), is("example1"));
		assertThat(testPlugin.getSecond(), is("example2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void acceptsMultiArgMethod() throws Exception {
		String query = "test example1 example2 and example3 example4";
		assertThat(handler.accepts(query), is(true));
		handler.handle(chat, context, query);
		assertThat(testPlugin.getFirst(), is("example1 example2"));
		assertThat(testPlugin.getSecond(), is("example3 example4"));
	}

	@Test
	public void testContextIsInjected() throws Exception {
		String query = "context a and b";
		Method method =
		  testPlugin.getClass().getDeclaredMethod("deploy", String.class, String.class, Context.class);
		handler = new Handler(testPlugin, method);

		assertThat(handler.accepts(query), is(true));
		handler.handle(chat, context, query);
		assertThat(testPlugin.getFirst(), is("a"));
		assertThat(testPlugin.getSecond(), is("b"));
	}
}
