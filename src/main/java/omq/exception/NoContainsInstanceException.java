package omq.exception;

public class NoContainsInstanceException extends Exception {

	public NoContainsInstanceException(String reference) {
		super("Reference: " + reference + " not found");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
