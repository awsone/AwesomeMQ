package omq.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates a method as Synchronous. It can have two
 * parameters: timeout and retry which will give you how long you have to wait a
 * synchronous method and how many times you'll wait for the response.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SyncMethod {
	/**
	 * Timeout of a synchronous method
	 * 
	 * @return how long we'll wait for a response
	 */
	long timeout() default 60000L;

	/**
	 * Number of retries of a synchronous method
	 * 
	 * @return how many retries we'll make. If the timeout is set, every timeout
	 *         will use it
	 */
	int retry() default 1;
}
