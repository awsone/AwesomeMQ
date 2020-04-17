package omq.test.observer;

import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class RemoteObserverImpl extends RemoteObject implements RemoteObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String obsState;
	private RemoteSubject subject;

	@Override
	public void update() {
		obsState = subject.getStringState();
	}

	public String getObsState() {
		return obsState;
	}

	public void setObsState(String obsState) {
		this.obsState = obsState;
	}

	public RemoteSubject getSubject() {
		return subject;
	}

	public void setSubject(RemoteSubject subject) {
		this.subject = subject;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RemoteObserver) {
			RemoteObserver ro = (RemoteObserver) obj;
			this.getRef().endsWith(ro.getRef());
		}
		return false;
	}

}
