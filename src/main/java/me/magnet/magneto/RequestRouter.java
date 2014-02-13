package me.magnet.magneto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.annotations.RespondTo;
import me.magnet.magneto.hipchat.HipChatNotification;
import me.magnet.magneto.hipchat.HtmlTableBuilder;
import me.magnet.magneto.plugins.MagnetoPlugin;

/**
 * Routers messages to the appriopiate handler. Once they're at their handler the router routes the responses
 * back to the client.
 */
@Slf4j
public class RequestRouter {

	private final List<Handler> handlers;
	private final ListMultimap<String, RespondTo> pluginCommands = ArrayListMultimap.create();

	public RequestRouter() {
		this.handlers = Lists.newArrayList();
	}

	public void register(MagnetoPlugin command) {
		if (!pluginCommands.containsKey(command.getName())) {
			log.info("Adding plugin {}", command.getName());
			Method[] methods = command.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(RespondTo.class)) {
					handlers.add(new Handler(command, method));
					pluginCommands.put(command.getName(), method.getAnnotation(RespondTo.class));
				}
			}
		}
		else {
			log.error("A plugin with name {} was already bound. Skipping {}");
		}
	}


	public void route(ChatRoom chat, Context context, String message) {
		boolean handled = false;
		if (message.equals("help")) {
			printHelp(chat);
			return;
		}

		for (Handler handler : handlers) {
			if (handler.accepts(message)) {
				handled = true;
				try {
					String pluginName = handler.getTarget().getClass().getSimpleName();
					log.info("Dispatching message: \"{}\" from: \"{}\" to: \"{}\"",
					  message, context.getFrom(), pluginName);
					handler.handle(chat, context, message);
				}
				catch (IllegalAccessException | IllegalArgumentException e) {
					log.error(e.getMessage(), e);
					chat.sendMessage("Whoops, there was an error: " + e.getMessage());
				}
				catch (InvocationTargetException e) {
					log.error(e.getCause().getMessage(), e);
					chat.sendMessage("Whoops, there was an error: " + e.getCause().getMessage());
				}
				break;
			}
		}

		if (!handled) {
			log.warn("The message: \"{}\" was not handled!", message);
			chat.sendMessage("I'm sorry " + context.getFrom().getFirstName()
			  + " but I don't know what you mean by that.");
		}

	}

	private void printHelp(ChatRoom chat) {
		StringBuilder sb = new StringBuilder();
		sb.append("The commands available to you are:<br>");
		for (String key : pluginCommands.keySet()) {
			sb.append("<strong>Plugin \"").append(key).append("\":</strong>");
			HtmlTableBuilder tableBuilder = new HtmlTableBuilder(sb);
			tableBuilder.openRow().append("Expression:").append("Description:").append("Example:").closeRow();
			for (RespondTo command : pluginCommands.get(key)) {
				tableBuilder.openRow().append(command.regex())
							.append(command.description())
							.append(command.example())
							.closeRow();
			}
			tableBuilder.close().append("</br>");
		}
		chat.sendHtml(new HipChatNotification(sb.toString()));
	}
}
