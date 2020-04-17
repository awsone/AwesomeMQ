package omq.test.multiProcess;

import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class NumberImpl extends RemoteObject implements Number {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int x = 0;

	public NumberImpl() {
	}

	public NumberImpl(int x) {
		this.x = x;
	}

	public void setNumber(int x) {
		this.x = x;
	}

	public int getNumber() {
		return x;
	}

	public void setMultiNumber(int x) {
		this.x = x;
	}

	public int getMultiNumber() {
		return x;
	}

}
