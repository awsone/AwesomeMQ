package omq.client.listener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import omq.client.proxy.Proxymq;
import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Class that inherits from Thread. It's used in the client side. This class
 * gets the deliveries from the server and stores them into the different
 * proxies created.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class ResponseListener extends Thread {
	private final Logger logger = Logger.getLogger(ResponseListener.class.getName());

	private Broker broker;
	private Channel channel;
	private QueueingConsumer consumer;
	private boolean killed = false;
	private Map<String, Map<String, byte[]>> results;
	private Properties env;

	/**
	 * ResponseListener constructor
	 * 
	 * @param broker
	 * @throws Exception
	 */
	public ResponseListener(Broker broker) throws Exception {
		this.broker = broker;
		env = broker.getEnvironment();

		// Init the hashtable (it's concurrent)
		results = new Hashtable<String, Map<String, byte[]>>();

		startRPCQueue();
	}

	@Override
	public void run() {
		logger.info("ResponseListener started");
		Delivery delivery;
		String uid_request;

		while (!killed) {
			try {
				// Get the delivery

				delivery = consumer.nextDelivery();

				BasicProperties props = delivery.getProperties();

				// Get the response with its uid
				uid_request = delivery.getProperties().getCorrelationId();
				logger.debug("Response received -> proxy reference: " + props.getAppId() + ", corrId: " + uid_request);

				// Stores the new response
				Map<String, byte[]> proxyResults = results.get(props.getAppId());

				// Put the result into the proxy results and notify him
				synchronized (proxyResults) {
					// If we haven't received this response before, we store it
					if (!proxyResults.containsKey(uid_request)) {
						proxyResults.put(uid_request, delivery.getBody());
						proxyResults.notifyAll();
					}
				}
			} catch (InterruptedException i) {
				logger.error(i.toString(), i);
			} catch (ShutdownSignalException e) {
				logger.error(e.toString(), e);
				try {
					if (channel.isOpen()) {
						channel.close();
					}
					startRPCQueue();
				} catch (Exception e1) {
					e1.printStackTrace();
					try {
						long milis = Long.parseLong(env.getProperty(ParameterQueue.RETRY_TIME_CONNECTION, "2000"));
						Thread.sleep(milis);
					} catch (InterruptedException e2) {
						logger.error(e2.toString(), e2);
					}
				}
			} catch (ConsumerCancelledException e) {
				logger.error(e.toString(), e);
			} catch (Exception e) {
				logger.error(e.toString(), e);
			}
		}
	}

	/**
	 * This function is used to start the response client queue
	 * 
	 * @throws Exception
	 */
	private void startRPCQueue() throws Exception {
		channel = broker.getNewChannel();

		Map<String, Object> args = null;

		String reply_queue = env.getProperty(ParameterQueue.RPC_REPLY_QUEUE);
		boolean durable = Boolean.parseBoolean(env.getProperty(ParameterQueue.DURABLE_QUEUE, "false"));
		boolean exclusive = Boolean.parseBoolean(env.getProperty(ParameterQueue.EXCLUSIVE_QUEUE, "true"));
		boolean autoDelete = Boolean.parseBoolean(env.getProperty(ParameterQueue.AUTO_DELETE_QUEUE, "true"));

		int ttl = Integer.parseInt(env.getProperty(ParameterQueue.MESSAGE_TTL_IN_QUEUES, "-1"));
		if (ttl > 0) {
			args = new HashMap<String, Object>();
			args.put("x-message-ttl", ttl);
		}

		if (reply_queue == null) {
			reply_queue = channel.queueDeclare().getQueue();
			env.setProperty(ParameterQueue.RPC_REPLY_QUEUE, reply_queue);
		} else {
			channel.queueDeclare(reply_queue, durable, exclusive, autoDelete, args);
		}
		logger.info("ResponseListener creating queue: " + reply_queue + ", durable: " + durable + ", exclusive: " + exclusive + ", autoDelete: " + autoDelete
				+ ", TTL: " + (ttl > 0 ? ttl : "not set"));

		// Declare a new consumer
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(reply_queue, true, consumer);
	}

	/**
	 * 
	 * @param key
	 * @return whether the map has the param key
	 */
	public boolean containsKey(String key) {
		return results.containsKey(key);
	}

	/**
	 * Interrupt and kill the Thread
	 * 
	 * @throws IOException
	 */
	public void kill() throws IOException {
		logger.warn("Killing ResponseListener");
		interrupt();
		killed = true;
		channel.close();
	}

	/**
	 * This method registers a new proxy into this responseListener
	 * 
	 * @param proxy
	 */
	public void registerProxy(Proxymq proxy) {
		// Since results is a hashtable this method doesn't need to be
		// synchronized
		if (!results.containsKey(proxy.getRef())) {
			results.put(proxy.getRef(), proxy.getResults());
		}
	}
}
