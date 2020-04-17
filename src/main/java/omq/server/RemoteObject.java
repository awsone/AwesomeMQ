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
import omq.exception.SerializerException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

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
public abstract class RemoteObject extends Thread implements Remote {

	private static final long serialVersionUID = -1778953938739846450L;
	private static final String multi = "multi#";
	private static final Logger logger = Logger.getLogger(RemoteObject.class.getName());

	private String UID;
	private Properties env;
	private transient Broker broker;
	private transient String multiQueue;
	private transient RemoteWrapper remoteWrapper;
	private transient Map<String, List<Class<?>>> params;
	private transient Channel channel;
	private transient QueueingConsumer consumer;
	private transient boolean killed = false;

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

	public RemoteObject() {
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
		this.UID = reference;
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
		this.remoteWrapper = new RemoteWrapper(this, numThreads, broker.getSerializer());

		startQueues();

		// Start this listener
		this.start();
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				Delivery delivery = consumer.nextDelivery();

				logger.debug(UID + " has received a message, serializer: " + delivery.getProperties().getType());

				remoteWrapper.notifyDelivery(delivery);
			} catch (InterruptedException i) {
				logger.error(i);
			} catch (ShutdownSignalException e) {
				logger.error(e);
				try {
					if (channel.isOpen()) {
						channel.close();
					}
					startQueues();
				} catch (Exception e1) {
					try {
						long milis = Long.parseLong(env.getProperty(ParameterQueue.RETRY_TIME_CONNECTION, "2000"));
						Thread.sleep(milis);
					} catch (InterruptedException e2) {
						logger.error(e2);
					}
					logger.error(e1);
				}
			} catch (ConsumerCancelledException e) {
				logger.error(e);
			} catch (SerializerException e) {
				logger.error(e);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	@Override
	public String getRef() {
		return UID;
	}

	/**
	 * This method kills all the threads waiting for requests
	 * 
	 * @throws IOException
	 *             - If an operation failed.
	 */
	public void kill() throws IOException {
		logger.warn("Killing objectmq: " + this.getRef());
		killed = true;
		interrupt();
		channel.close();
		remoteWrapper.stopRemoteWrapper();
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

	public Channel getChannel() {
		return channel;
	}

	/**
	 * This method starts the queues using the information got in the
	 * environment.
	 * 
	 * @throws Exception
	 */
	private void startQueues() throws Exception {
		// Start channel
		channel = broker.getNewChannel();

		/*
		 * Default queue, Round Robin behaviour
		 */

		// Get info about which exchange and queue will use
		String exchange = env.getProperty(ParameterQueue.RPC_EXCHANGE, "");
		String queue = UID;
		String routingKey = UID;

		// RemoteObject default queue
		boolean durable = Boolean.parseBoolean(env.getProperty(ParameterQueue.DURABLE_QUEUE, "false"));
		boolean exclusive = Boolean.parseBoolean(env.getProperty(ParameterQueue.EXCLUSIVE_QUEUE, "false"));
		boolean autoDelete = Boolean.parseBoolean(env.getProperty(ParameterQueue.AUTO_DELETE_QUEUE, "false"));

		// Declares and bindings
		if (!exchange.equalsIgnoreCase("")) { // Default exchange case
			channel.exchangeDeclare(exchange, "direct");
		}
		channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
		if (!exchange.equalsIgnoreCase("")) { // Default exchange case
			channel.queueBind(queue, exchange, routingKey);
		}
		logger.info("RemoteObject: " + UID + " declared direct exchange: " + exchange + ", Queue: " + queue + ", Durable: " + durable + ", Exclusive: "
				+ exclusive + ", AutoDelete: " + autoDelete);

		/*
		 * Multi queue, exclusive per each instance
		 */

		// Get info about the multiQueue
		String multiExchange = multi + UID;
		multiQueue = env.getProperty(ParameterQueue.MULTI_QUEUE_NAME);

		// Multi queue (exclusive queue per remoteObject)
		boolean multiDurable = Boolean.parseBoolean(env.getProperty(ParameterQueue.DURABLE_MQUEUE, "false"));
		boolean multiExclusive = Boolean.parseBoolean(env.getProperty(ParameterQueue.EXCLUSIVE_MQUEUE, "true"));
		boolean multiAutoDelete = Boolean.parseBoolean(env.getProperty(ParameterQueue.AUTO_DELETE_MQUEUE, "true"));

		// Declares and bindings
		channel.exchangeDeclare(multiExchange, "fanout");
		if (multiQueue == null) {
			multiQueue = channel.queueDeclare().getQueue();
		} else {
			channel.queueDeclare(multiQueue, multiDurable, multiExclusive, multiAutoDelete, null);
		}
		channel.queueBind(multiQueue, multiExchange, "");
		logger.info("RemoteObject: " + UID + " declared fanout exchange: " + multiExchange + ", Queue: " + multiQueue + ", Durable: " + multiDurable
				+ ", Exclusive: " + multiExclusive + ", AutoDelete: " + multiAutoDelete);

		/*
		 * Consumer
		 */

		// Declare a new consumer
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(queue, true, consumer);
		channel.basicConsume(multiQueue, true, consumer);
	}

}
