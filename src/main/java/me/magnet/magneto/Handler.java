package me.magnet.magneto;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;
import me.magnet.magneto.annotations.Param;
import me.magnet.magneto.annotations.RespondTo;
import me.magnet.magneto.plugins.MagnetoPlugin;

/**
 * Routes a request to a method that has a {@link me.magnet.magneto.annotations.RespondTo} annotation.
 */
@Data
class Handler {

	private final Object target;
	private final Method method;
	private final Pattern pattern;

	public Handler(MagnetoPlugin target, Method method) {
		String key = method.getAnnotation(RespondTo.class).value();
		key = key.replace("{", "(?<");
		key = key.replace("}", ">.+)");
		key = "^" + key + "$";

		this.target = target;
		this.method = method;
		this.pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * @param message The incoming message
	 * @return If this handled is applicable for the given message.
	 */
	public boolean accepts(String message) {
		return pattern.matcher(message).matches();
	}

	/**
	 * @param chat    The room messages have to be sent to.
	 * @param context The user that sent the message.
	 * @param message The incoming message.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public void handle(final ChatRoom chat, Context context, String message)
	  throws IllegalAccessException,
	  IllegalArgumentException, InvocationTargetException {
		Matcher matcher = pattern.matcher(message);
		matcher.matches();

		Object[] values = parseValues(matcher, context, chat);
		method.invoke(target, values);
	}


	private Object[] parseValues(Matcher matcher, Context context, ChatRoom chat) {
		Param[] params = getParams(method);
		Class<?>[] types = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		Object[] values = new Object[annotations.length];
		for (int index = 0; index < annotations.length; index++) {
			if (types[index].isAssignableFrom(Context.class)) {
				values[index] = context;
			}
			else if (types[index].isAssignableFrom(ChatRoom.class)) {
				values[index] = chat;
			}
			else {
				String name = params[index].value();
				String value = matcher.group(name);
				Class<?> type = types[index];
				values[index] = parse(value, type);
			}
		}
		return values;
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

		throw new IllegalArgumentException("Cannot parse value: " + value + " as: "
		  + type.getSimpleName());
	}

}