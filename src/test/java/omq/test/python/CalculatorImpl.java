package omq.test.python;

import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class CalculatorImpl extends RemoteObject implements Calculator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void fibonacci(int x) {
		System.out.println(fib(x));
	}

	public int fib(int x) {
		if (x == 0) {
			return 0;
		}
		if (x == 1) {
			return 1;
		}
		return fib(x - 1) + fib(x - 2);
	}

	public int add(int x, int y) {
		System.out.println("add: " + x + "+" + y + "=" + (x + y));
		return x + y;
	}

	@Override
	public double getPi() {
		return 3.1415;
	}

}
