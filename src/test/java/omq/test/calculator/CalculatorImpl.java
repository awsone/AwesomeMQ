package omq.test.calculator;

import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class CalculatorImpl extends RemoteObject implements Calculator {
	private int mult = 0;

	public CalculatorImpl() throws Exception {
		super();
	}

	private static final long serialVersionUID = 1L;

	@Override
	public int add(int x, int y) {
		return x + y;
	}

	@Override
	public void mult(int x, int y) {
		mult = x * y;
	}

	public int getMult() {
		return mult;
	}

	public void setMult(int mult) {
		this.mult = mult;
	}

	@Override
	public void sendMessage(Message m) {
		System.out.println("Code = " + m.getCode());
		System.out.println("Message = " + m.getMessage());
	}

	@Override
	public int divideByZero() {
		int x = 2 / 0;
		return x;
	}

}
