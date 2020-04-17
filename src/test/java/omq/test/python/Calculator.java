package omq.test.python;

import omq.Remote;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public interface Calculator extends Remote {
	public void fibonacci(int x);

	public int add(int x, int y);

	public double getPi();

}
