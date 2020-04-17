package omq.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import omq.Remote;
import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;

import org.apache.log4j.Logger;

/**
 * A RemoteObject when it's started will be waiting for requests and will invoke
 * them. When a RemoteObject is started it listens two queues, the first one has
 * the same name as its reference and the second one is its multiqueue -this
 * name can be set using a property, be aware to use a name not used by another
 * object!!!-.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public abstract class RemoteObject implements Remote {

	private static final long serialVersionUID = -1778953938739846450L;
	private static final Logger logger = Logger.getLogger(RemoteObject.class.getName());

	private String reference;
	private String UID;
	private Properties env;
	private transient Broker broker;
	private transient RemoteThreadPool pool;
	private transient Map<String, List<Class<?>>> params;

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
	 * This method starts a remoteObject.
	 * 
	 * @param reference
	 *            - broker's binding referece
	 * @param broker
	 *            - broker that binds this remoteObject
	 * @param env
	 *            - properties of this remoteObject
	 * @throws Exception
	 */
	public void startRemoteObject(String reference, Broker broker, Properties env) throws Exception {
		this.broker = broker;
		this.reference = reference;
		this.env = env;

		this.params = new HashMap<String, List<Class<?>>>();
		for (Method m : this.getClass().getMethods()) {
			List<Class<?>> list = new ArrayList<Class<?>>();
			for (Class<?> clazz : m.getParameterTypes()) {
				list.add(clazz);
			}
			this.params.put(m.getName(), list);
		}

		// Get num threads to use
		int numThreads = Integer.parseInt(env.getProperty(ParameterQueue.NUM_THREADS, "1"));

		// Create the pool & start it
		pool = new RemoteThreadPool(numThreads, this);
		pool.startPool();
	}

	public void startRemoteObject(String reference, String UID, Broker broker, Properties env) throws Exception {
		this.UID = UID;
		startRemoteObject(reference, broker, env);
	}

	@Override
	public String getRef() {
		return reference;
	}

	/**
	 * This method kills all the threads waiting for requests
	 * 
	 * @throws IOException
	 *             - If an operation failed.
	 */
	public void kill() throws IOException {
		logger.info("Killing objectmq: " + this.getRef());
		pool.kill();
	}

	/**
	 * This method invokes the method specified by methodName and arguments
	 * 
	 * @param methodName
	 * @param arguments
	 * @return result
	 * @throws Exception
	 */
	public Object invokeMethod(String methodName, Object[] arguments) throws Exception {

		// Get the specific method identified by methodName and its arguments
		Method method = loadMethod(methodName, arguments);

		return method.invoke(this, arguments);
	}

	/**
	 * This method loads the method specified by methodName and args
	 * 
	 * @param methodName
	 * @param args
	 * @return method
	 * @throws NoSuchMethodException
	 *             - If the method cannot be found
	 */
	private Method loadMethod(String methodName, Object[] args) throws NoSuchMethodException {
		Method m = null;

		// Obtain the class reference
		Class<?> clazz = this.getClass();
		Class<?>[] argArray = null;

		if (args != null) {
			argArray = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				argArray[i] = args[i].getClass();
			}
		}

		try {
			m = clazz.getMethod(methodName, argArray);
		} catch (NoSuchMethodException nsm) {
			m = loadMethodWithPrimitives(methodName, argArray);
		}
		return m;
	}

	/**
	 * This method loads a method which uses primitives as arguments
	 * 
	 * @param methodName
	 *            - name of the method wanted to invoke
	 * @param argArray
	 *            - arguments
	 * @return method
	 * @throws NoSuchMethodException
	 *             - If the method cannot be found
	 */
	private Method loadMethodWithPrimitives(String methodName, Class<?>[] argArray) throws NoSuchMethodException {
		if (argArray != null) {
			Method[] methods = this.getClass().getMethods();
			int length = argArray.length;

			for (Method method : methods) {
				String name = method.getName();
				int argsLength = method.getParameterTypes().length;

				if (name.equals(methodName) && length == argsLength) {
					// This array can have primitive types inside
					Class<?>[] params = method.getParameterTypes();

					boolean found = true;

					for (int i = 0; i < length; i++) {
						if (params[i].isPrimitive()) {
							Class<?> paramWrapper = primitiveClasses.get(params[i].getName());

							if (!paramWrapper.equals(argArray[i])) {
								found = false;
								break;
							}
						}
					}
					if (found) {
						return method;
					}
				}
			}
		}
		throw new NoSuchMethodException(methodName);
	}

	public List<Class<?>> getParams(String methodName) {
		return params.get(methodName);
	}

	public Broker getBroker() {
		return broker;
	}

	public RemoteThreadPool getPool() {
		return pool;
	}

	public Properties getEnv() {
		return env;
	}

	public String getUID() {
		return UID;
	}

	public void setUID(String uID) {
		UID = uID;
	}

}
