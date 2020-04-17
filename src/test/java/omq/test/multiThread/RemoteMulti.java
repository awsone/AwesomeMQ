package omq.test.multiThread;

import omq.server.RemoteObject;

public class RemoteMulti extends RemoteObject implements MultiInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void sleepMethod() throws InterruptedException {
		System.out.println("I'm going to sleep");
		Thread.sleep(40000);
	}

	public int getZero() {
		System.out.println("Zero!!!");
		return 0;
	}

}
