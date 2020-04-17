package omq.test.exception;

import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class OmqServerImpl extends RemoteObject implements ServerInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int numWheels;
	private int hp;
	private int price;

	@Override
	public void addWheels(int numWheels) {
		this.numWheels = numWheels;
	}

	@Override
	public void addHp(int hp) {
		this.hp = hp;
	}

	@Override
	public int getHp() {
		return hp;
	}

	@Override
	public int getWheels() {
		return numWheels;
	}

	@Override
	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public int getPrice() {
		return price;
	}

}
