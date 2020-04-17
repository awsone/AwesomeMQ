package omq.test.workspace;

import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class RemoteWorkspaceImpl extends RemoteObject implements RemoteWorkspace {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Info info;

	public void update(Info info) {
		this.info = info;
	}

	public Info getInfo() {
		return info;
	}

	public void setInfo(Info info) {
		this.info = info;
	}

}
