package omq.test.observer;

import omq.Remote;
import omq.client.annotation.RemoteInterface;
import omq.client.annotation.SyncMethod;
import omq.exception.RemoteException;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@RemoteInterface
public interface RemoteSubject extends Remote {

	@SyncMethod(timeout = 1000)
	public String getStringState();

	@SyncMethod(timeout = 1000)
	public void setState(String state);

	@SyncMethod(timeout = 1000)
	public void addObserver(String ref) throws RemoteException;

	@SyncMethod(timeout = 1000)
	public void removeObserver(String ref) throws RemoteException;

	@SyncMethod(timeout = 1000)
	public void notifyObservers();
}
