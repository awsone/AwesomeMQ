package omq.server;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;

import omq.common.message.Request;
import omq.common.message.Response;
import omq.common.util.Serializer;
import omq.exception.OmqException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * An invocationThread waits for requests an invokes them.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class InvocationThread extends Thread {
	private static final Logger logger = Logger.getLogger(InvocationThread.class.getName());
	private RemoteObject obj;
	private transient Serializer serializer;
	private BlockingQueue<Delivery> deliveryQueue;
	private boolean killed = false;

	public InvocationThread(RemoteObject obj, BlockingQueue<Delivery> deliveryQueue, Serializer serializer) {
		this.obj = obj;
		this.deliveryQueue = deliveryQueue;
		this.serializer = serializer;
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				// Get the delivery
				Delivery delivery = deliveryQueue.take();

				String serializerType = delivery.getProperties().getType();

				// Deserialize the json
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

					Channel channel = obj.getChannel();

					BasicProperties props = delivery.getProperties();

					BasicProperties replyProps = new BasicProperties.Builder().appId(obj.getRef()).correlationId(props.getCorrelationId()).build();

					byte[] bytesResponse = serializer.serialize(serializerType, resp);
					channel.basicPublish("", props.getReplyTo(), replyProps, bytesResponse);
					logger.debug("Publish sync response -> Object: " + obj.getRef() + ", method: " + methodName + " corrID: " + requestID + " replyTo: "
							+ props.getReplyTo());
				}

			} catch (InterruptedException i) {
				logger.error(i);
				killed = true;
			} catch (Exception e) {
				logger.error("Object: " + obj.getRef(), e);
			}

		}
	}

	public RemoteObject getObj() {
		return obj;
	}

	public void setObj(RemoteObject obj) {
		this.obj = obj;
	}

	public BlockingQueue<Delivery> getDeliveryQueue() {
		return deliveryQueue;
	}

	public void setDeliveryQueue(BlockingQueue<Delivery> deliveryQueue) {
		this.deliveryQueue = deliveryQueue;
	}
}
