package me.magnet.magneto;

import lombok.Value;
import lombok.experimental.Builder;

/**
 * The context of a given command. This can be used in a method to retrieve extra information about the call.
 */
@Builder
@Value
public class Context {

	private User from;
	private String room;
}
