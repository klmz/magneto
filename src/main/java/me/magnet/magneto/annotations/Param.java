package me.magnet.magneto.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a parameter in a message sent to magneto.
 * 
 * For example, the parameter bar will be set to "test" when this plugin is called with the message "Foo test".
 *<p><blockquote><pre>
 * @RespondTo("Foo {bar} ")
 * 	public Response test(@Param("bar") String bar) { .. }
 * </p></blockquote></pre>
 */
@Target({ PARAMETER })
@Retention(RUNTIME)
public @interface Param {

	String value();

}
