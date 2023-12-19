package fi.utu.tech.telephonegame;

import java.io.Serializable;
import java.util.UUID;

public final class Message implements Serializable {

	// TODO: Missing attributes
	private static final  long serialVersionUID = 1L;

	private UUID id;
	private String message;
	private Integer color;

	public Message(String message, Integer color) {
		this(UUID.randomUUID(), message, color);
	}

	public Message(UUID id, String message, Integer color) {
		this.id = id;
		this.message = message;
		this.color = color;
	}
	// Getter methods
	public UUID getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public Integer getColor() {
		return color;
	}

	// Setter methods
	public void setMessage(String message) {
		this.message = message;
	}

	public void setColor(Integer color) {
		this.color = color;
	}

}
