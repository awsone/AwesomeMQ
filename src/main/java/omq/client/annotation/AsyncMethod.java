package omq.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates a method as Asynchronous.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AsyncMethod {
}
