package omq.exception;

import java.io.Serializable;

public class OmqException implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String type;
	private String message;

	public OmqException() {
	}

	public OmqException(String type, String message) {
		this.type = type;
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
