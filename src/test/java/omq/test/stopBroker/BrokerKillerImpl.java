package omq.test.stopBroker;

import omq.client.annotation.AsyncMethod;
import omq.common.broker.Broker;
import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class BrokerKillerImpl extends RemoteObject implements BrokerKiller {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Broker broker;

	public BrokerKillerImpl(Broker broker) {
		this.broker = broker;
	}

	@Override
	@AsyncMethod
	public void killServerBroker() throws Exception {
		System.out.println("Kill broker");

		// A remote method cannot stop the Broker because the stop method is
		// thought to wait for the methods finish before it stops. For this
		// reason it actually cannot stop itself
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
					broker.stopBroker();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();

	}

}
