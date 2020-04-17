package omq.test.python;

import java.util.List;

import omq.Remote;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public interface ContactList extends Remote {
	public void setContact(Contact c);

	public List<Contact> getContacts();
}
