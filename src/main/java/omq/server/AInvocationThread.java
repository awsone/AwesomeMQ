package omq.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.message.Request;
import omq.common.message.Response;
import omq.common.util.Serializer;
import omq.exception.OmqException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public abstract class AInvocationThread extends Thread {

	private static final Logger logger = Logger.getLogger(AInvocationThread.class.getName());

	// RemoteObject
	protected RemoteObject obj;
	protected String reference;
	protected String UID;
	protected Properties env;

	// Broker
	protected Broker broker;
	protected Serializer serializer;

	// Consumer
	protected Channel channel;
	protected QueueingConsumer consumer;
	protected boolean killed = false;

	public AInvocationThread(RemoteObject obj) throws Exception {
		this.obj = obj;
		this.UID = obj.getUID();
		this.reference = obj.getRef();
		this.env = obj.getEnv();
		this.broker = obj.getBroker();
		this.serializer = broker.getSerializer();
	}

	@Override
	public synchronized void start() {
		try {
			startQueues();
			super.start();
		} catch (Exception e) {
			logger.error("Cannot start a remoteObject", e);
		}

	}

	/**
	 * This method starts the queues using the information got in the
	 * environment.
	 * 
	 * @throws Exception
	 */
	protected abstract void startQueues() throws Exception;

	protected void executeTask(Delivery delivery) throws Exception {
		String serializerType = delivery.getProperties().getType();

		// Deserialize the request
		Request request = serializer.deserializeRequest(serializerType, delivery.getBody(), obj);
		String methodName = request.getMethod();
		String requestID = request.getId();

		logger.debug("Object: " + obj.getRef() + ", method: " + methodName + " corrID: " + requestID + ", serializerType: " + serializerType);

		// Invoke the method
		Object result = null;
		OmqException error = null;
		try {
			result = obj.invokeMethod(request.getMethod(), request.getParams());
		} catch (InvocationTargetException e) {
			Throwable throwable = e.getTargetException();
			logger.error("Object: " + obj.getRef() + " at method: " + methodName + ", corrID" + requestID, throwable);
			error = new OmqException(throwable.getClass().getCanonicalName(), throwable.getMessage());
		} catch (NoSuchMethodException e) {
			logger.error("Object: " + obj.getRef() + " cannot find method: " + methodName);
			error = new OmqException(e.getClass().getCanonicalName(), e.getMessage());
		}

		// Reply if it's necessary
		if (!request.isAsync()) {
			Response resp = new Response(request.getId(), obj.getRef(), result, error);

			BasicProperties props = delivery.getProperties();

			BasicProperties replyProps = new BasicProperties.Builder().appId(obj.getRef()).correlationId(props.getCorrelationId()).build();

			byte[] bytesResponse = serializer.serialize(serializerType, resp);
			channel.basicPublish("", props.getReplyTo(), replyProps, bytesResponse);
			logger.debug("Publish sync response -> Object: " + obj.getRef() + ", method: " + methodName + " corrID: " + requestID + " replyTo: "
					+ props.getReplyTo());
		}

		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	}

	public void kill() throws IOException {
		logger.info("Killing objectmq: " + reference + " thread id");
		killed = true;
		interrupt();
		channel.close();
	}

	public RemoteObject getObj() {
		return obj;
	}

	public void setObj(RemoteObject obj) {
		this.obj = obj;
	}
}