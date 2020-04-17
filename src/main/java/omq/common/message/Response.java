package omq.common.message;

import java.io.Serializable;

import omq.exception.OmqException;

/**
 * Serializable response information. This class is used to send the information
 * to the client proxy. It has information about which remoteObject has invoked
 * the method and its correlation id. This class also has the result of the
 * invoke if everything has gone fine in the server or an error otherwise.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Response implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3368363997012527189L;

	private Object result;
	private OmqException error;
	private String id;
	private String idOmq;

	// Used by kryo
	public Response() {
	}

	/**
	 * Creates a new Response object to be serialized
	 * 
	 * @param id
	 *            - correlation id of the invoke
	 * @param idOmq
	 *            - objectmq's identifier -bind reference-
	 * @param result
	 *            - result of the invocation
	 * @param error
	 *            - error thrown by the invocation
	 */
	public Response(String id, String idOmq, Object result, OmqException error) {
		this.id = id;
		this.idOmq = idOmq;
		this.result = result;
		this.error = error;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdOmq() {
		return idOmq;
	}

	public void setIdOmq(String idOmq) {
		this.idOmq = idOmq;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public OmqException getError() {
		return error;
	}

	public void setError(OmqException error) {
		this.error = error;
	}

}
