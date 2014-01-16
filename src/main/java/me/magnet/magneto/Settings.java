package me.magnet.magneto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import com.google.common.base.Preconditions;

/**
 * A wrapper for the configuration.properties file.
 */
public class Settings {

	private static final String CHAT_SERVER_HOST = "magneto.chat.server.host";
	private static final String CHAT_SERVER_PORT = "magneto.chat.server.port";
	private static final String CONF_SERVER_HOST = "magneto.conf.server.host";
	private static final String CHAT_USER_USERNAME = "magneto.chat.user.username";
	private static final String CHAT_USER_DISPLAYNAME = "magneto.chat.user.displayname";
	private static final String CHAT_USER_PASSWORD = "magneto.chat.user.password";
	private static final String CHAT_USER_MENTION = "magneto.char.user.mention";

	private final Properties properties;

	public Settings() {
		this.properties = new Properties();
	}

	public void load() throws IOException {
		File propsFile = new File("config.properties");
		if (!propsFile.exists()) {
			try {
				URL settings = Settings.class.getResource("/config.properties");
				if (settings == null) {
					settings = Settings.class.getResource("conf/config.properties");
				}
				Preconditions.checkNotNull(settings, "config.properties not found.");
				propsFile = new File(settings.toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException("Whut no URI possible for " + propsFile, e);
			}
		}
		try (InputStream in = new FileInputStream(propsFile)) {
			properties.load(in);
		}
	}

	public String getChatServerHost() {
		return properties.getProperty(CHAT_SERVER_HOST);
	}

	public int getChatServerPort() {
		return Integer.parseInt(properties.getProperty(CHAT_SERVER_PORT, "5222"));
	}

	public String getConferenceServerHost() {
		return properties.getProperty(CONF_SERVER_HOST);
	}

	public String getUserName() {
		return properties.getProperty(CHAT_USER_USERNAME, "magneto");
	}

	public String getUserDisplayName() {
		return properties.getProperty(CHAT_USER_DISPLAYNAME, "Magneto");
	}

	public String getUserPassword() {
		return properties.getProperty(CHAT_USER_PASSWORD);
	}

	public String getUserMention() {
		return properties.getProperty(CHAT_USER_MENTION, "magneto");
	}

}
