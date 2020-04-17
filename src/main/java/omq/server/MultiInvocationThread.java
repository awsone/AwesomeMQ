package omq.server;

import omq.common.util.ParameterQueue;

import org.apache.log4j.Logger;

import com.rabbitmq.client.QueueingConsumer;

public class MultiInvocationThread extends AInvocationThread {

	private static final Logger logger = Logger.getLogger(MultiInvocationThread.class.getName());
	private static final String multi = "multi#";

	// Consumer
	private String multiQueue;

	public MultiInvocationThread(RemoteObject obj) throws Exception {
		super(obj);
	}

	@Override
	protected void startQueues() throws Exception {
		// Start channel
		channel = broker.getNewChannel();

		/*
		 * Multi queue, exclusive per each instance
		 */

		// Get info about the multiQueue
		String multiExchange = multi + reference;
		// TODO:String multiExchange = multi + exchange + reference;
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
		logger.info("RemoteObject: " + reference + " declared fanout exchange: " + multiExchange + ", Queue: " + multiQueue + ", Durable: " + multiDurable
				+ ", Exclusive: " + multiExclusive + ", AutoDelete: " + multiAutoDelete);

		/*
		 * Consumer
		 */

		// Disable Round Robin behavior
		boolean autoAck = false;

		// Declare a new consumer
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(multiQueue, autoAck, consumer);
	}

}
