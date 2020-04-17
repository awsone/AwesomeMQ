package omq.test.exception;

import java.io.Serializable;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Trailer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int kg;

	public Trailer(int kg) {
		this.kg = kg;
	}

	public int getKg() {
		return kg;
	}

	public void setKg(int kg) {
		this.kg = kg;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Trailer) {
			Trailer t = (Trailer) obj;
			return kg == t.getKg();
		}
		return false;
	}
}
