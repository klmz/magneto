package me.magnet.magneto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.annotations.RespondTo;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.google.common.collect.Lists;

@Slf4j
public class RequestRouter {

	private final List<Handler> handlers;

	public RequestRouter() {
		this.handlers = Lists.newArrayList();
	}

	public void register(MagnetoPlugin command) {
		Method[] methods = command.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(RespondTo.class)) {
				handlers.add(new Handler(command, method));
			}
		}
	}

	public void route(MultiUserChat chat, User user, String message) {
		boolean handled = false;
		for (Handler handler : handlers) {
			if (handler.accepts(message)) {
				handled = true;
				try {
					String pluginName = handler.getTarget().getClass().getSimpleName();
					log.info("Dispatching message: \"{}\" from: \"{}\" to: \"{}\"",
					        message, user.getFullName(), pluginName);
					handler.handle(chat, user, message);
				} catch (IllegalAccessException | IllegalArgumentException
				        | InvocationTargetException e) {
					log.error(e.getMessage(), e);
					handled = false;
				}
				break;
			}
		}

		if (!handled) {
			log.warn("The message: \"{}\" was not handled!", message);
			try {
				chat.sendMessage("I'm sorry " + user.getFirstName()
				        + " but I don't know what you mean by that.");
			} catch (XMPPException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

}
