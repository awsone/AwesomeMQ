package omq.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates a method as Multi.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MultiMethod {
	/**
	 * If @MultiMethod is followed by @SyncMethod waitNum indicates how many
	 * responses we will wait for.
	 * 
	 * @return length of the array of responses we are waiting for.
	 */
	int waitNum() default 1;
}