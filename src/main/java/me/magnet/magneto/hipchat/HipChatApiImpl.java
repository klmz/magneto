package me.magnet.magneto.hipchat;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.IOException;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.DeliveryException;
import me.magnet.magneto.Settings;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

@Singleton
@Slf4j
class HipChatApiImpl implements HipChatApi {

	static final String API_URL_PREFIX = "https://api.hipchat.com/v2/";

	private final String token;
	private final HttpClient client;
	private final ObjectMapper mapper;

	@Inject
	public HipChatApiImpl(HttpClient client, Settings settings, ObjectMapper mapper) {
		this.client = client;
		this.mapper = mapper;
		this.token = settings.getHipchatToken();
	}

	@Override
	public void send(HipChatNotification notification, String toRoom) {
		String url = String.format(HipChatNotification.API_URL_ROOM, toRoom);
		log.debug("Sending {} to {}", notification, url);
		HttpPost post = new HttpPost(url);
		setRequiredHeaders(post);
		try {
			String postBody = mapper.writeValueAsString(notification);
			post.setEntity(new StringEntity(postBody, Charsets.UTF_8));
			HttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
				String body = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
				throw new DeliveryException(notification
				  + " received status code: " + response.getStatusLine().getStatusCode()
				+ " message: " + response.getStatusLine().getReasonPhrase());

			}
		}
		catch (IOException e) {
			throw new DeliveryException(notification.toString(), e);

		}
	}

	private void setRequiredHeaders(HttpPost post) {
		post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		post.setHeader(HttpHeaders.CONTENT_ENCODING, "UTF8");
	}
}
