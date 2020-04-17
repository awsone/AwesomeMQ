package omq.test.workspace;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.MultiMethod;
import omq.client.annotation.RemoteInterface;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@RemoteInterface
public interface RemoteWorkspace extends Remote {
	@MultiMethod
	@AsyncMethod
	public void update(Info info);
}
