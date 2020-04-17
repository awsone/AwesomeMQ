package omq.test.multiProcess;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.MultiMethod;
import omq.client.annotation.RemoteInterface;
import omq.client.annotation.SyncMethod;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@RemoteInterface
public interface NumberClient extends Remote {
	@SyncMethod(timeout = 1000)
	public void setNumber(int x);

	@SyncMethod(timeout = 1000)
	public int getNumber();

	@MultiMethod
	@AsyncMethod
	public void setMultiNumber(int x);

	@MultiMethod(waitNum = 2)
	@SyncMethod(timeout = 1000)
	public int[] getMultiNumber();

}
