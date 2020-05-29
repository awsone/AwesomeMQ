package omq.server;

import omq.common.util.ParameterQueue;

import org.apache.log4j.Logger;

import com.rabbitmq.client.QueueingConsumer;

/**
 * An invocationThread waits for requests an invokes them.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class InvocationThread extends AInvocationThread {

	public static final String TYPE = "normalResponse";
	private static final Logger logger = Logger.getLogger(InvocationThread.class.getName());

	// RemoteObject

	public InvocationThread(RemoteObject obj) throws Exception {
		super(obj);
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

	@Override
	public String getType() {
		return TYPE;
	}
}
