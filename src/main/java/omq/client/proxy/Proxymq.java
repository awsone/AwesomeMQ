package omq.client.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.MultiMethod;
import omq.client.annotation.SyncMethod;
import omq.client.listener.ResponseListener;
import omq.common.broker.Broker;
import omq.common.message.Request;
import omq.common.message.Response;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;
import omq.exception.OmqException;
import omq.exception.RetryException;
import omq.exception.TimeoutException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * Proxymq class. This class inherits from InvocationHandler, for this reason
 * each proxymq instance has an associated invocation handler. When a method is
 * invoked on a proxymq instance, the method invocation is encoded and
 * dispatched to the invoke method of its invocation handler.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Proxymq implements InvocationHandler, Remote {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Proxymq.class.getName());
	private static final String multi = "multi#";

	private String uid;
	private transient String exchange;
	private transient String multiExchange;
	private transient String replyQueueName;
	private transient String serializerType;
	private transient Broker broker;
	private transient ResponseListener rListener;
	private transient Serializer serializer;
	private transient Properties env;
	private transient Integer deliveryMode = null;
	private transient Map<String, byte[]> results;

	private static final Map<String, Class<?>> primitiveClasses = new HashMap<String, Class<?>>();
	static {
		primitiveClasses.put("byte", Byte.class);
		primitiveClasses.put("short", Short.class);
		primitiveClasses.put("char", Character.class);
		primitiveClasses.put("int", Integer.class);
		primitiveClasses.put("long", Long.class);
		primitiveClasses.put("float", Float.class);
		primitiveClasses.put("double", Double.class);
	}

	/**
	 * Proxymq Constructor.
	 * 
	 * This constructor uses an uid to know which object will call. It also uses
	 * Properties to set where to send the messages
	 * 
	 * @param uid
	 *            The uid represents the unique identifier of a remote object
	 * @param clazz
	 *            It represents the real class of the remote object. With this
	 *            class the system can know the remoteInterface used and it can
	 *            also see which annotations are used
	 * @param env
	 *            The environment is used to know where to send the messages
	 * @throws Exception
	 */
	public Proxymq(String uid, Class<?> clazz, Broker broker) throws Exception {
		this.uid = uid;
		this.broker = broker;
		rListener = broker.getResponseListener();
		serializer = broker.getSerializer();

		// TODO what is better to use a new channel or to use the same?
		// this.channel = Broker.getChannel();
		env = broker.getEnvironment();
		exchange = env.getProperty(ParameterQueue.RPC_EXCHANGE, "");
		multiExchange = multi + uid;
		replyQueueName = env.getProperty(ParameterQueue.RPC_REPLY_QUEUE);

		// set the serializer type
		serializerType = env.getProperty(ParameterQueue.PROXY_SERIALIZER, Serializer.JAVA);
		if (env.getProperty(ParameterQueue.DELIVERY_MODE) != null) {
			deliveryMode = Integer.parseInt(env.getProperty(ParameterQueue.DELIVERY_MODE));
		}

		// Create a new hashmap and registry it in rListener
		results = new HashMap<String, byte[]>();
		rListener.registerProxy(this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
		// Local methods only
		String methodName = method.getName();

		// The local methods will be invoked here
		if (method.getDeclaringClass().equals(Remote.class)) {
			if (methodName.equals("getRef")) {
				return getRef();
			}
		}

		// Create the request
		Request request = createRequest(method, arguments);

		Object response = null;
		// Publish the request
		if (request.isAsync()) {
			publishMessage(request, replyQueueName);
		} else {
			response = publishSyncRequest(request, method.getReturnType());
		}

		return response;
	}

	/**
	 * This method publishes a request
	 * 
	 * @param request
	 *            - this request contains which method and which params will be
	 *            invoked in the server side.
	 * @param replyQueueName
	 *            - this param indicates where the responseListener will be
	 *            listen to.
	 * @throws Exception
	 */
	private void publishMessage(Request request, String replyQueueName) throws Exception {
		String corrId = request.getId();

		// Get the environment properties
		String exchange;
		String routingkey;

		if (request.isMulti()) {
			exchange = multiExchange;
			routingkey = "";
		} else {
			exchange = this.exchange;
			routingkey = uid;
		}

		// Add the correlation ID and create a replyTo property
		BasicProperties props = new BasicProperties.Builder().appId(uid).correlationId(corrId).replyTo(replyQueueName).type(serializerType)
				.deliveryMode(deliveryMode).build();

		// Publish the message
		byte[] bytesRequest = serializer.serialize(serializerType, request);
		broker.publishMessge(exchange, routingkey, props, bytesRequest);
		logger.debug("Proxymq: " + uid + " invokes '" + request.getMethod() + "' , corrID: " + corrId + ", exchange: " + exchange + ", replyQueue: "
				+ replyQueueName + ", serializerType: " + serializerType + ", multi call: " + request.isMulti() + ", async call: " + request.isAsync()
				+ ", delivery mode: " + deliveryMode);
	}

	/**
	 * This method publishes a synchronous request
	 * 
	 * @param request
	 *            - this request contains which method and which params will be
	 *            invoked in the server side.
	 * @param type
	 *            - indicates which return type we are waiting for
	 * @return serverResponse
	 * @throws Exception
	 */
	private Object publishSyncRequest(Request request, Class<?> type) throws Exception {
		String corrId = request.getId();

		int retries = request.getRetries();
		long timeout = request.getTimeout();

		// Publish the message
		int i = 0;
		while (i < retries) {
			try {
				publishMessage(request, replyQueueName);
				if (request.isMulti()) {
					return getResults(corrId, request.getWait(), timeout, type);
				} else {
					return getResult(corrId, timeout, type);
				}

			} catch (TimeoutException te) {
				logger.error(te);
			}
			i++;
		}
		throw new RetryException(retries, timeout);
	}

	/**
	 * This method creates a request using the annotations of the Remote
	 * interface
	 * 
	 * @param method
	 *            - method to invoke in the server side
	 * @param arguments
	 *            - arguments of the method
	 * @return new Request
	 */
	private Request createRequest(Method method, Object[] arguments) {
		String corrId = java.util.UUID.randomUUID().toString();
		String methodName = method.getName();
		boolean multi = false;
		int wait = 0;

		if (method.getAnnotation(MultiMethod.class) != null) {
			multi = true;
			wait = method.getAnnotation(MultiMethod.class).waitNum();
		}

		// Since we need to know whether the method is async and if it has to
		// return using an annotation, we'll only check the AsyncMethod
		// annotation
		if (method.getAnnotation(AsyncMethod.class) == null) {
			int retries = 1;
			long timeout = ParameterQueue.DEFAULT_TIMEOUT;
			if (method.getAnnotation(SyncMethod.class) != null) {
				SyncMethod sync = method.getAnnotation(SyncMethod.class);
				retries = sync.retry();
				timeout = sync.timeout();
			}
			return Request.newSyncRequest(corrId, methodName, arguments, retries, timeout, multi, wait);
		} else {
			return Request.newAsyncRequest(corrId, methodName, arguments, multi);
		}
	}

	private Object getResult(String corrId, long timeout, Class<?> type) throws Exception {
		Response resp = null;

		// Wait for the results.
		long localTimeout = timeout;
		long start = System.currentTimeMillis();
		synchronized (results) {
			// Due to we are using notifyAll(), we need to control the real time
			while (!results.containsKey(corrId) && (timeout - localTimeout) >= 0) {
				results.wait(localTimeout);
				localTimeout = System.currentTimeMillis() - start;
			}
			if ((timeout - localTimeout) <= 0) {
				throw new TimeoutException("Timeout exception time: " + timeout);
			}
			resp = serializer.deserializeResponse(results.get(corrId), type);

			// Remove and indicate the key exists (a hashmap can contain a null
			// object, using this we'll know whether a response has been
			// received before)
			results.put(corrId, null);
		}

		if (resp.getError() != null) {
			OmqException error = resp.getError();
			String name = error.getType();
			String message = error.getMessage();
			throw (Exception) Class.forName(name).getConstructor(String.class).newInstance(message);
		}

		return resp.getResult();
	}

	/**
	 * This method returns an array with length @MultiMethod.waitNum() with all
	 * the responses received.
	 * 
	 * @param corrId
	 *            - Correlation Id of the request
	 * @param wait
	 *            - Array length
	 * @param timeout
	 *            - Timeout read in @SyncMethod.timeout(). If the timeout is set
	 *            in 2 seconds, the system will wait 2 seconds for the arriving
	 *            of all the responses.
	 * @param type
	 *            - Must be an Array type
	 * @return resultArray
	 * @throws Exception
	 */
	private Object getResults(String corrId, int wait, long timeout, Class<?> type) throws Exception {
		Response resp = null;
		// Get the component type of an array
		Class<?> actualType = type.getComponentType();

		Object array = Array.newInstance(actualType, wait);

		int i = 0;
		long localTimeout = timeout;
		long start = System.currentTimeMillis();

		while (i < wait) {
			synchronized (results) {
				// Due to we are using notifyAll(), we need to control the real
				// time
				while (!results.containsKey(corrId) && (timeout - localTimeout) >= 0) {
					results.wait(localTimeout);
					localTimeout = System.currentTimeMillis() - start;
				}
				if ((timeout - localTimeout) <= 0) {
					throw new TimeoutException("Timeout exception time: " + timeout);
				}
				// Remove the corrId to receive new replies
				resp = serializer.deserializeResponse(results.remove(corrId), actualType);
				Array.set(array, i, resp.getResult());
			}
			i++;
		}
		synchronized (results) {
			results.put(corrId, null);
		}

		return array;
	}

	/**
	 * Gets the Map used internally to retreive the response of the server
	 * 
	 * @return a map with all the keys processed. Every key is a correlation id
	 *         of a method invoked remotely
	 */
	public Map<String, byte[]> getResults() {
		return results;
	}

	@Override
	public String getRef() {
		return uid;
	}

}
