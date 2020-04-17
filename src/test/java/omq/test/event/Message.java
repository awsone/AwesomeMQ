package omq.test.event;

import omq.Remote;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public interface Message extends Remote {
	public void setMessage(String message);
}
