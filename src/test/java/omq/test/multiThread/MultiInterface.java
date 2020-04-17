package omq.test.multiThread;

import omq.Remote;
import omq.client.annotation.RemoteInterface;
import omq.client.annotation.SyncMethod;

@RemoteInterface
public interface MultiInterface extends Remote {
	@SyncMethod(retry = 1, timeout = 2000)
	public void sleepMethod() throws InterruptedException;

	@SyncMethod(retry = 1, timeout = 2000)
	public int getZero();
}
