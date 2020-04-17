package omq.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates which is the remote interface that can have
 * asynchmethods or syncmethods. By default every method without an annotation
 * will be classified as a SyncMethod. Both annotations can be preceded by the @MultiMethod
 * annotation.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RemoteInterface {
}
