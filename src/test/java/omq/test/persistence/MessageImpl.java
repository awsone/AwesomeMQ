package omq.test.persistence;

import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class MessageImpl extends RemoteObject implements Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
