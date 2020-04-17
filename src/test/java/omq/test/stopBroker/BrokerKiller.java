package omq.test.stopBroker;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.RemoteInterface;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@RemoteInterface
public interface BrokerKiller extends Remote {
	@AsyncMethod
	public void killServerBroker() throws Exception;
}
