package omq.test.observer;

import omq.Remote;
import omq.client.annotation.RemoteInterface;
import omq.client.annotation.SyncMethod;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@RemoteInterface
public interface RemoteObserver extends Remote {
	@SyncMethod(timeout = 1000)
	public void update();
}
