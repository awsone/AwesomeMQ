package omq.test.exception;

import omq.Remote;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public interface ServerInterface extends Remote {
	public void addWheels(int numWheels);

	public void addHp(int hp);

	public int getHp();

	public int getWheels();

	public void setPrice(int price);

	public int getPrice();
}
