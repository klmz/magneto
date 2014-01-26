package me.magnet.magneto.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Lets a method respond the the pattern given in this annotation. All expressions are case insensitive.
 *
 * <p>
 * For example, the following method will respond the message "hello"
 * <pre>
 * {@literal @}RespondTo("hello")
 * public Response hello() { .. }
 * </pre>
 *
 * It also works with regular expressions. This method responds to "hello" and "hi"
 * <pre>
 * {@literal @}RespondTo(""\\b(hello|hi).*"")
 * public Response hello() { .. }
 * </pre>
 * </p>
 *
 * You can also inject the Context of a command by insertin the {@link me.magnet.magneto.Context}.
 * <pre>
 * {@literal @}RespondTo(""\\b(hello|hi).*"")
 * public Response hello(Context contex) {
 * 	User from = context.getFrom();
 * }
 * </pre>
 * </p>
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface RespondTo {

	/**
	 * A string or regular expression that is applied to every incoming message to see if it applies.
	 * @return
	 */
	String value();
	
}
