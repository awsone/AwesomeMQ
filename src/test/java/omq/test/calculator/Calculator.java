package omq.test.calculator;

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
public interface Calculator extends Remote {
	@SyncMethod(timeout = 1500)
	public int add(int x, int y);

	@AsyncMethod
	public void mult(int x, int y);

	@AsyncMethod
	public void sendMessage(Message m);

	@SyncMethod(timeout = 1500)
	public int divideByZero();

}
