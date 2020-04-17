package omq.test.workspace;

import java.io.Serializable;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Info implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;

	public Info() {
	}

	public Info(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
