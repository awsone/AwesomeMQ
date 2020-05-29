package omq.common.message;

import java.io.Serializable;

/**
 * Serializable request information. This class is used to send the information
 * to the server. It has information about which method is wanted to invoke, its
 * parameters, its correlation id and if a response is needed -asynchronous
 * method-.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Request implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6366255840200365083L;

	private String method;
	private Object[] params;
	private String id;
	private boolean async = false;

	private transient boolean multi;
	private transient long timeout;
	private transient int retries;

	// This constructor is used by kryo
	public Request() {
	}

	public Request(String id, String method, boolean async, Object[] params) {
		this.id = id;
		this.method = method;
		this.async = async;
		this.params = params;
	}

	public Request(String id, String method, boolean async, Object[] params, boolean multi) {
		this.id = id;
		this.method = method;
		this.async = async;
		this.params = params;
		this.multi = multi;
	}

	/**
	 * This method creates a new synchronous request
	 * 
	 * @param id
	 *            - correlation id of this invocation
	 * @param method
	 *            - method name wanted to call
	 * @param params
	 *            - parameters of this method
	 * @return - new SyncRequest
	 */
	public static Request newSyncRequest(String id, String method, Object[] params) {
		return new Request(id, method, false, params);
	}

	/**
	 * This method creates a new synchronous request
	 * 
	 * @param id
	 *            - correlation id of this invocation
	 * @param method
	 *            - method name wanted to call
	 * @param params
	 *            - parameters of this method
	 * @param retries
	 *            - How many retries will be done
	 * @param timeout
	 *            - Timeout for every retry
	 * @param multi
	 *            - If the method is multi
	 * @return - new SyncRequest
	 */
	public static Request newSyncRequest(String id, String method, Object[] params, int retries, long timeout, boolean multi) {
		Request req = new Request(id, method, false, params, multi);
		req.setRetries(retries);
		req.setTimeout(timeout);
		return req;
	}

	/**
	 * This method creates a new asynchronous request
	 * 
	 * @param id
	 *            - correlation id of this invocation
	 * @param method
	 *            - method name wanted to call
	 * @param params
	 *            - parameters of this method
	 * @param multi
	 *            - If the method is multi
	 * @return new AsyncRequest
	 */
	public static Request newAsyncRequest(String id, String method, Object[] params, boolean multi) {
		return new Request(id, method, true, params, multi);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public boolean isMulti() {
		return multi;
	}

	public void setMulti(boolean multi) {
		this.multi = multi;
	}

}