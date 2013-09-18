package me.magnet.magneto;

import lombok.Data;

@Data
public class User {
	
	public static User of(String input) {
		if (input.contains("/")) {
			input = input.substring(input.lastIndexOf('/') + 1);
		}
		return new User(input);
	}

	private final String fullName;
	
	public String getFirstName() {
		if (!fullName.contains(" ")) {
			return fullName; 
		}
		return fullName.substring(0, fullName.indexOf(' '));
	}

}
