package omq.exception;

public class ObjectAlreadyExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ObjectAlreadyExistsException(String ref) {
		super(ref);
	}

}
