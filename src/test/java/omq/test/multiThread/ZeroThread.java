package omq.test.multiThread;

public class ZeroThread extends Thread {
	private MultiInterface m;
	private boolean received;

	public ZeroThread(MultiInterface m) {
		this.m = m;
		received = false;
	}

	@Override
	public void run() {
		int zero = m.getZero();
		if (zero == 0) {
			received = true;
		}
	}

	public MultiInterface getM() {
		return m;
	}

	public void setM(MultiInterface m) {
		this.m = m;
	}

	public boolean isReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}

}
