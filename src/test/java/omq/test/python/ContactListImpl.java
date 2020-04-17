package omq.test.python;

import java.util.ArrayList;
import java.util.List;

import omq.server.RemoteObject;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class ContactListImpl extends RemoteObject implements ContactList {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Contact> list = new ArrayList<Contact>();

	@Override
	public void setContact(Contact c) {
		list.add(c);
		System.out.println("New contact added: " + c);
	}

	@Override
	public List<Contact> getContacts() {
		return list;
	}
}
