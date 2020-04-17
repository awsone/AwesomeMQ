package omq.server;

import omq.common.util.ParameterQueue;
import omq.exception.SerializerException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * An invocationThread waits for requests an invokes them.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class InvocationThread extends AInvocationThread {

	private static final Logger logger = Logger.getLogger(InvocationThread.class.getName());

	// RemoteObject

	public InvocationThread(RemoteObject obj) throws Exception {
		super(obj);
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				// Get the delivery
				Delivery delivery = consumer.nextDelivery();

				executeTask(delivery);

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
				e.printStackTrace();
				logger.error(e);
			}

		}
		logger.info("ObjectMQ ('" + obj.getRef() + "') InvocationThread " + Thread.currentThread().getId() + " is killed");
	}

	/**
	 * This method starts the queues using the information got in the
	 * environment.
	 * 
	 * @throws Exception
	 */
	protected void startQueues() throws Exception {
		// Start channel
		channel = broker.getNewChannel();

		// Get info about which exchange and queue will use
		String exchange = env.getProperty(ParameterQueue.RPC_EXCHANGE, "");
		String queue = reference;
		String routingKey = reference;

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
		logger.info("RemoteObject: " + reference + " declared direct exchange: " + exchange + ", Queue: " + queue + ", Durable: " + durable + ", Exclusive: "
				+ exclusive + ", AutoDelete: " + autoDelete);

		/*
		 * UID queue
		 */

		if (UID != null) {

			boolean uidDurable = false;
			boolean uidExclusive = true;
			boolean uidAutoDelete = true;

			channel.queueDeclare(UID, uidDurable, uidExclusive, uidAutoDelete, null);
			if (!exchange.equalsIgnoreCase("")) { // Default exchange case
				channel.queueBind(UID, exchange, UID);
			}
			// TODO logger queue
			// TODO UID queue should be reference + UID
		}

		/*
		 * Consumer
		 */

		// Disable Round Robin behavior
		boolean autoAck = false;

		int prefetchCount = 1;
		channel.basicQos(prefetchCount);

		// Declare a new consumer
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(queue, autoAck, consumer);
		if (UID != null) {
			channel.basicConsume(UID, autoAck, consumer);
		}
	}
}
