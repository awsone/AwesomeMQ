package omq;

import java.io.Serializable;

/**
 * 
 * The Remote interface serves to identify interfaces whose methods may be
 * invoked from a non-local virtual machine. Any object that is a remote object
 * must directly or indirectly implement this interface. Only those methods
 * specified in a "remote interface", an interface that extends omq.Remote are
 * available remotely.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public interface Remote extends Serializable {

	/**
	 * Returns the reference of a RemoteObject
	 * 
	 * @return reference
	 */
	public String getRef();

	public String getUID();

	public void setUID(String uID);
}