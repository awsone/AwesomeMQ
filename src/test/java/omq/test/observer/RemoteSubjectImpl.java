package omq.test.observer;

import java.util.ArrayList;
import java.util.List;

import omq.common.broker.Broker;
import omq.exception.RemoteException;
import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class RemoteSubjectImpl extends RemoteObject implements RemoteSubject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String state;
	private Broker broker;
	private List<RemoteObserver> list;

	public RemoteSubjectImpl(Broker broker) {
		this.broker = broker;
		list = new ArrayList<RemoteObserver>();
	}

	@Override
	public void addObserver(String ref) throws RemoteException {
		RemoteObserver obs = broker.lookup(ref, RemoteObserver.class);
		list.add(obs);
	}

	@Override
	public void removeObserver(String ref) throws RemoteException {
		RemoteObserver obs = broker.lookup(ref, RemoteObserver.class);
		list.remove(obs);
	}

	@Override
	public void notifyObservers() {
		for (RemoteObserver o : list) {
			o.update();
		}
	}

	@Override
	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String getStringState() {
		return state;
	}
}
