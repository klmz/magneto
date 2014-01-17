package me.magnet.magneto.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a parameter in a message sent to magneto.
 *
 * <p>
 * For example, the parameter bar will be set to <code>test</code> when this plugin is called with the message <code>Foo test</code>.
 * <pre>
 * {@literal @}RespondTo("Foo {bar} ")
 * public Response test(@Param("bar") String bar) { .. }
 * </pre>
 * </p>
 */
@Target({ PARAMETER })
@Retention(RUNTIME)
public @interface Param {

	String value();

}
