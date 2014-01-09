package me.magnet.magneto;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

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
import rx.Observable;
import rx.Observer;

@RunWith(MockitoJUnitRunner.class)
public class HandlerTest {

	@Data
	@NoArgsConstructor
	@Accessors(chain = true)
	public static class TestPlugin extends MagnetoPluginAdapter {

		private Observable<String> observable;
		private String first;
		private String second;

		@RespondTo("test {a} and {b}")
		public Observable<String> deploy(
		  final @Param("a") String first,
		  final @Param("b") String second) {
			this.first = first;
			this.second = second;
			return observable;
		}

	}

	private TestPlugin testPlugin;
	private Handler handler;
	private User user;

	@Mock
	private ChatRoom chat;

	@Mock
	private Observable<String> observable;

	@Before
	public void setup() throws NoSuchMethodException, SecurityException {
		testPlugin = new TestPlugin().setObservable(observable);
		Method method =
		  testPlugin.getClass().getDeclaredMethod("deploy", String.class, String.class);
		handler = new Handler(testPlugin, method);
		user = new User("Example User");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void acceptsMethod() throws Exception {
		String query = "test example1 and example2";
		assertThat(handler.accepts(query), is(true));
		handler.handle(chat, user, query);
		assertThat(testPlugin.getFirst(), is("example1"));
		assertThat(testPlugin.getSecond(), is("example2"));
		verify(observable).subscribe(any(Observer.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void acceptsMultiArgMethod() throws Exception {
		String query = "test example1 example2 and example3 example4";
		assertThat(handler.accepts(query), is(true));
		handler.handle(chat, user, query);
		assertThat(testPlugin.getFirst(), is("example1 example2"));
		assertThat(testPlugin.getSecond(), is("example3 example4"));
		verify(observable).subscribe(any(Observer.class));
	}
}
