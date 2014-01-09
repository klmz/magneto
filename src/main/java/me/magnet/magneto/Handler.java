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
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;

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
		this.pattern = Pattern.compile(key);
	}

	public boolean accepts(String message) {
		return pattern.matcher(message).matches();
	}

	@SuppressWarnings("unchecked")
	public void handle(final ChatRoom chat, User user, String message)
	        throws IllegalAccessException,
	        IllegalArgumentException, InvocationTargetException {
		Matcher matcher = pattern.matcher(message);
		matcher.matches();

		Object[] values = parseValues(matcher);

		Observable<String> output = (Observable<String>) method.invoke(target, values);
		output.subscribe(new Observer<String>() {

			private final Logger log = LoggerFactory.getLogger(method.getClass());

			@Override
			public void onCompleted() {
				log.trace("onCompleted");
			}

			@Override
			public void onError(Throwable e) {
				log.error("Error in Observer", e);
			}

			@Override
			public void onNext(String message) {
				try {
					chat.sendMessage(message);
				} catch (XMPPException e) {
					Logger log = LoggerFactory.getLogger(method.getClass());
					log.error("Error in OnNext for {}", method, e);
				}
			}
		});
	}

	private Object[] parseValues(Matcher matcher) {
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