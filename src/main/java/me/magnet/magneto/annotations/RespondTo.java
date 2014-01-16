package me.magnet.magneto.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Lets a method respond the the pattern given in this annotation.
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface RespondTo {

	String value();
	
}
