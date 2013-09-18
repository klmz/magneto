package me.magnet.magneto;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.annotations.Param;
import me.magnet.magneto.annotations.RespondTo;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import rx.Observable;
import rx.Observer;

import com.google.common.collect.Lists;

@Slf4j
public class RequestRouter {

	private final List<Handler> handlers;

	public RequestRouter() {
		this.handlers = Lists.newArrayList();
	}

	public void register(Object command) {
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
					log.info("Dispatching message: \"{}\" from: \"{}\" to: \"{}\"", new Object[] { message, user.getFullName(), pluginName});
					handler.handle(chat, user, message);
				} 
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error(e.getMessage(), e);
					handled = false;
				}
				break;
			}
		}
		
		if (!handled) {
			log.warn("The message: \"" + message + "\" was not handled!");
			try {
				chat.sendMessage("I'm sorry " + user.getFirstName() + " but I don't know what you mean by that.");
			} 
			catch (XMPPException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Data
	private static class Handler {

		private final Object target;
		private final Method method;
		private final Pattern pattern;

		public Handler(Object target, Method method) {
			String key = method.getAnnotation(RespondTo.class).value();
			key = key.replace("{", "(?<");
			key = key.replace("}", ">.+)");
			key = "^" + key + "$";

			this.target = target;
			this.method = method;
			this.pattern = Pattern.compile(key);
		}

		public boolean accepts(String message) {
			return pattern.matcher(message).matches();
		}

		@SuppressWarnings("unchecked")
		public void handle(final MultiUserChat chat, User user, String message) throws IllegalAccessException,
				IllegalArgumentException, InvocationTargetException {
			Matcher matcher = pattern.matcher(message);
			matcher.matches();

			Param[] params = getParams(method);
			Class<?>[] types = method.getParameterTypes();
			Annotation[][] annotations = method.getParameterAnnotations();
			Object[] values = new Object[annotations.length];
			for (int index = 0; index < annotations.length; index++) {
				String name = params[index].value();
				String value = matcher.group(name);
				Class<?> type = types[index];
				values[index] = parse(value, type);
			}

			Observable<String> output = (Observable<String>) method.invoke(target, values);
			output.subscribe(new Observer<String>() {
				@Override
				public void onCompleted() {}

				@Override
				public void onError(Throwable arg0) {}

				@Override
				public void onNext(String arg0) {
					try {
						chat.sendMessage(arg0);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
			});
		}

		private Param[] getParams(Method method) {
			Param[] parameters = new Param[method.getParameterAnnotations().length];
			for (int index = 0; index < method.getParameterAnnotations().length; index++) {
				Annotation[] annotations = method.getParameterAnnotations()[index];
				for (Annotation annotation : annotations) {
					if (annotation instanceof Param) {
						parameters[index] = (Param) annotation;
						break;
					}
				}
			}
			return parameters;
		}

		private Object parse(String value, Class<?> type) {
			if (String.class.equals(type)) {
				return value;
			}
			else if (Long.class.equals(type) || long.class.equals(type)) {
				return Long.parseLong(value);
			}
			else if (Integer.class.equals(type) || int.class.equals(type)) {
				return Integer.parseInt(value);
			}
			else if (Byte.class.equals(type) || byte.class.equals(type)) {
				return Byte.parseByte(value);
			}
			else if (Short.class.equals(type) || short.class.equals(type)) {
				return Short.parseShort(value);
			}
			else if (Float.class.equals(type) || float.class.equals(type)) {
				return Float.parseFloat(value);
			}
			else if (Double.class.equals(type) || double.class.equals(type)) {
				return Double.parseDouble(value);
			}
			else if ((Character.class.equals(type) || char.class.equals(type)) && value.length() == 1) {
				return value.charAt(0);
			}
			else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
				return Boolean.parseBoolean(value);
			}

			throw new IllegalArgumentException("Cannot parse value: " + value + " as: " + type.getSimpleName());
		}

	}

}
