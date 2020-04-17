package omq.exception;

public class AlreadyBoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AlreadyBoundException() {
		super();
	}

	public AlreadyBoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AlreadyBoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyBoundException(String message) {
		super(message);
	}

	public AlreadyBoundException(Throwable cause) {
		super(cause);
	}

}
