package omq.exception;

public class EnvironmentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EnvironmentException() {
		super("Environment not found");
	}

}
