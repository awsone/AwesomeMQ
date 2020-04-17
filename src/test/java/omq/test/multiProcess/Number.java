package omq.test.multiProcess;

import omq.Remote;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public interface Number extends Remote {
	public void setNumber(int x);

	public int getNumber();

	public void setMultiNumber(int x);

	public int getMultiNumber();
}
