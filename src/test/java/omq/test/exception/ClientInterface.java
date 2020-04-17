package omq.test.exception;

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
public interface ClientInterface extends Remote {
	@AsyncMethod
	public void addWheels(int numWheels);

	@AsyncMethod
	public void addHp(int hp);

	@AsyncMethod
	public void addTrailer(Trailer t);

	@AsyncMethod
	public void setPrice(double price);

	@SyncMethod(timeout = 1000)
	public Trailer getTrailer();

	@SyncMethod(timeout = 1000)
	public int getHp();

	@SyncMethod(timeout = 1000)
	public int getWheels();

	@SyncMethod(timeout = 1000)
	public double getPrice();
}
