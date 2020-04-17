package omq.test.persistence;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.RemoteInterface;
import omq.client.annotation.SyncMethod;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */

@RemoteInterface
public interface Message extends Remote {
	@AsyncMethod
	public void setMessage(String message);

	@SyncMethod(timeout = 1500)
	public String getMessage();
}
