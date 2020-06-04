package io.github.cottonmc.cotton_scripting.api.exception;

public class EntityNotFoundException extends Exception {
	public EntityNotFoundException() {
		super("Invalid entity selected.");
	}
}
