package omq.common.broker;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import omq.Remote;
import omq.client.listener.ResponseListener;
import omq.client.proxy.MultiProxymq;
import omq.client.proxy.Proxymq;
import omq.common.util.OmqConnectionFactory;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;
import omq.exception.AlreadyBoundException;
import omq.exception.InitBrokerException;
import omq.exception.RemoteException;
import omq.server.RemoteObject;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * A "broker" allows a new connection to a RabbitMQ server. Under this
 * connection it can have binded object and proxies.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Broker {

	private static final Logger logger = Logger.getLogger(Broker.class.getName());

	private Connection connection;
	private Channel channel;
	private ResponseListener responseListener;
	private Serializer serializer;
	private boolean clientStarted = false;
	private boolean connectionClosed = false;
	private Properties environment = null;
	private Map<String, RemoteObject> remoteObjs;
	private Map<String, Object> proxies = new Hashtable<String, Object>();
	private Map<String, Object> multiProxies = new Hashtable<String, Object>();

	public Broker() throws Exception {
		Properties env = new Properties();

		remoteObjs = new HashMap<String, RemoteObject>();
		serializer = new Serializer(env);
		environment = env;
		connection = OmqConnectionFactory.getNewConnection(env);
		channel = connection.createChannel();
		addFaultTolerance();
		if (!connection.isOpen() || !channel.isOpen()) {
			if (connection.isOpen()) {
				connection.close();
			}
			throw new InitBrokerException("The connection didn't work");
		}
	}

	public Broker(Properties env) throws Exception {
		// Load log4j configuration
		// URL log4jResource = Broker.class.getResource("/log4j.xml");
		// DOMConfigurator.configure(log4jResource);

		remoteObjs = new HashMap<String, RemoteObject>();
		serializer = new Serializer(env);
		environment = env;
		connection = OmqConnectionFactory.getNewConnection(env);
		channel = connection.createChannel();
		addFaultTolerance();
		if (!connection.isOpen() || !channel.isOpen()) {
			if (connection.isOpen()) {
				connection.close();
			}
			throw new InitBrokerException("The connection didn't work");
		}

		// try {
		// tryConnection(env);
		// } catch (Exception e) {
		// channel.close();
		// connection.close();
		// throw new InitBrokerException("The connection didn't work");
		// }
	}

	/**
	 * This method stops the broker's connection and all the threads created
	 * 
	 * @throws Exception
	 */
	public void stopBroker() throws Exception {
		logger.warn("Stopping broker");
		// Stop the client
		if (clientStarted) {
			responseListener.kill();
			// TODO proxies = null; ??
		}
		// Stop all the remote objects working
		for (String reference : remoteObjs.keySet()) {
			unbind(reference);
		}

		// Close the connection once all the listeners are died
		closeConnection();

		clientStarted = false;
		// connectionClosed = false;
		environment = null;
		remoteObjs = null;
	}

	/**
	 * @return Broker's connection
	 * @throws Exception
	 */
	public Connection getConnection() throws Exception {
		return connection;
	}

	/**
	 * This method close the broker's connection
	 * 
	 * @throws IOException
	 */
	public void closeConnection() throws IOException {
		logger.warn("Clossing connection");
		connectionClosed = true;
		connection.close();
		// connectionClosed = false;
	}

	/**
	 * Return the broker's channel
	 * 
	 * @return Broker's channel
	 * @throws Exception
	 */
	public synchronized Channel getChannel() throws Exception {
		return channel;
	}

	/**
	 * Creates a new channel using the Broker's connection
	 * 
	 * @return newChannel
	 * @throws IOException
	 */
	public synchronized Channel getNewChannel() throws IOException {
		return connection.createChannel();
	}

	/**
	 * 
	 */
	public synchronized void publishMessge(String exchange, String routingKey, BasicProperties props, byte[] bytesRequest) throws IOException {
		if (!channel.isOpen()) {
			logger.error("Broker's channel is closed opening a new one", channel.getCloseReason());
			channel = getNewChannel();
		}
		channel.basicPublish(exchange, routingKey, props, bytesRequest);
	}

	/**
	 * Returns the remote object for specified reference.
	 * 
	 * @param reference
	 *            - Binding name
	 * @param contract
	 *            - Remote Interface
	 * @return newProxy
	 * @throws RemoteException
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends Remote> T lookup(String reference, Class<T> contract) throws RemoteException {
		try {

			if (!clientStarted) {
				initClient();
			}

			if (!proxies.containsKey(reference)) {
				Proxymq proxy = new Proxymq(reference, contract, this);
				Class<?>[] array = { contract };
				Object newProxy = Proxy.newProxyInstance(contract.getClassLoader(), array, proxy);
				proxies.put(reference, newProxy);
				return (T) newProxy;
			}
			return (T) proxies.get(reference);

		} catch (Exception e) {
			throw new RemoteException(e);
		}
	}

	/**
	 * Returns the remote object for specified reference. This function returns
	 * an special type of proxy, every method invoked will be multi and
	 * asynchronous.
	 * 
	 * @param reference
	 *            - Binding name
	 * @param contract
	 *            - Remote Interface
	 * @return newProxy
	 * @throws RemoteException
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends Remote> T lookupMulti(String reference, Class<T> contract) throws RemoteException {
		try {
			if (!multiProxies.containsKey(reference)) {
				MultiProxymq proxy = new MultiProxymq(reference, contract, this);
				Class<?>[] array = { contract };
				Object newProxy = Proxy.newProxyInstance(contract.getClassLoader(), array, proxy);
				multiProxies.put(reference, newProxy);
				return (T) newProxy;
			}
			return (T) multiProxies.get(reference);

		} catch (Exception e) {
			throw new RemoteException(e);
		}
	}

	/**
	 * Binds the reference to the specified remote object. This function uses
	 * the broker's environment
	 * 
	 * @param reference
	 *            - Binding name
	 * @param remote
	 *            - RemoteObject to bind
	 * @throws RemoteException
	 *             If the remote operation failed
	 * @throws AlreadyBoundException
	 *             If name is already bound.
	 */
	public void bind(String reference, RemoteObject remote) throws RemoteException, AlreadyBoundException {
		bind(reference, remote, environment);
	}

	/**
	 * Binds the reference to the specified remote object. This function uses
	 * the broker's environment
	 * 
	 * @param reference
	 *            - Binding name
	 * @param remote
	 *            - RemoteObject to bind
	 * @param env
	 *            - RemoteObject environment. You can set how many threads will
	 *            be listen to the reference, the multiqueue name and the
	 *            properties of the object queue and multiqueue
	 * @throws RemoteException
	 *             If the remote operation failed
	 * @throws AlreadyBoundException
	 *             If name is already bound.
	 */
	public void bind(String reference, RemoteObject remote, Properties env) throws RemoteException, AlreadyBoundException {
		if (remoteObjs.containsKey(reference)) {
			throw new AlreadyBoundException(reference);
		}
		// Try to start the remtoeObject listeners
		try {
			remote.startRemoteObject(reference, this, env);
			remoteObjs.put(reference, remote);
		} catch (Exception e) {
			throw new RemoteException(e);
		}
	}

	/**
	 * Unbinds a remoteObject from its reference and kills all the threads
	 * created.
	 * 
	 * @param reference
	 *            - Binding name
	 * @throws RemoteException
	 *             If the remote operation failed
	 * @throws IOException
	 *             If there are problems while killing the threads
	 */
	public void unbind(String reference) throws RemoteException, IOException {
		if (remoteObjs.containsKey(reference)) {
			RemoteObject remote = remoteObjs.get(reference);
			remote.kill();
		} else {
			throw new RemoteException("The object referenced by 'reference' does not exist in the Broker");
		}

	}

	/**
	 * This method ensures the client will have only one ResponseListener.
	 * 
	 * @throws Exception
	 */
	private synchronized void initClient() throws Exception {
		if (responseListener == null) {
			responseListener = new ResponseListener(this);
			responseListener.start();
			clientStarted = true;
		}
	}

	/**
	 * This function is used to send a ping message to see if the connection
	 * works
	 * 
	 * @param env
	 * @throws Exception
	 */
	public void tryConnection(Properties env) throws Exception {
		Channel channel = connection.createChannel();
		String message = "ping";

		String exchange = env.getProperty(ParameterQueue.USER_NAME) + "ping";
		String queueName = exchange;
		String routingKey = "routingKey";

		channel.exchangeDeclare(exchange, "direct");
		channel.queueDeclare(queueName, false, false, false, null);
		channel.queueBind(queueName, exchange, routingKey);

		channel.basicPublish(exchange, routingKey, null, message.getBytes());

		QueueingConsumer consumer = new QueueingConsumer(channel);

		channel.basicConsume(queueName, true, consumer);
		Delivery delivery = consumer.nextDelivery(1000);

		channel.exchangeDelete(exchange);
		channel.queueDelete(queueName);

		channel.close();

		if (!message.equalsIgnoreCase(new String(delivery.getBody()))) {
			throw new IOException("Ping initialitzation has failed");
		}
	}

	/**
	 * This method adds a ShutdownListener to the Broker's connection. When this
	 * connection falls, a new connection will be created and this will also
	 * have the listener.
	 */
	private void addFaultTolerance() {
		connection.addShutdownListener(new ShutdownListener() {
			@Override
			public void shutdownCompleted(ShutdownSignalException cause) {
				logger.warn("Shutdown message received. Cause: " + cause.getMessage());
				if (!connectionClosed)
					if (cause.isHardError()) {
						if (connection.isOpen()) {
							try {
								connection.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						try {
							connection = OmqConnectionFactory.getNewWorkingConnection(environment);
							channel = connection.createChannel();
							addFaultTolerance();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						Channel channel = (Channel) cause.getReference();
						if (channel.isOpen()) {
							try {
								channel.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
			}
		});
	}

	public Properties getEnvironment() {
		return environment;
	}

	public ResponseListener getResponseListener() {
		return responseListener;
	}

	public Serializer getSerializer() {
		return serializer;
	}
}
