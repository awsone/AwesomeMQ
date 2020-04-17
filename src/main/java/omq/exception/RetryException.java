package omq.exception;

public class RetryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5450662697539597010L;

	public RetryException(int retries, long timeout) {
		super("RetyException num retries="+retries+" timeout per retry="+timeout);
	}
	
	

}
