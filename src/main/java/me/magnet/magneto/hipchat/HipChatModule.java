package me.magnet.magneto.hipchat;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.Settings;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

@Slf4j
public class HipChatModule extends AbstractModule {

	@Override
	protected void configure() {

	}

	@Provides
	@Singleton
	HipChatApi hipChatApi(Settings settings, ObjectMapper mapper) {
		if (Strings.isNullOrEmpty(settings.getHipchatToken())) {
			log.warn("You do not have the HipChat Token set. HTML messages will be ignored");
			return new DummyHipChatApi();
		}
		else {
			return new HipChatApiImpl(httpClient(), settings, mapper);
		}
	}

	@Provides
	public ObjectMapper mapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		return mapper;
	}

	private HttpClient httpClient() {
		return HttpClientBuilder.create()
								.setMaxConnTotal(20)
								.build();
	}
}
