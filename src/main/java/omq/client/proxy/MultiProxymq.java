package omq.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;

import omq.common.broker.Broker;
import omq.common.message.Request;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;

/**
 * MultiProxy class. Every proxy created with this class will invoke
 * multi-asynchronous methods.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class MultiProxymq implements InvocationHandler {
	private static final Logger logger = Logger.getLogger(MultiProxymq.class.getName());
	private static final String multi = "multi#";

	private String uid;
	private Broker broker;
	private Serializer serializer;
	private String replyQueueName;
	private String exchange;
	private static final String routingkey = "";
	private transient String serializerType;

	public MultiProxymq(String uid, Class<?> clazz, Broker broker) throws Exception {
		this.uid = uid;
		this.broker = broker;
		serializer = broker.getSerializer();

		Properties env = broker.getEnvironment();
		exchange = multi + uid;
		serializerType = env.getProperty(ParameterQueue.PROXY_SERIALIZER, Serializer.JAVA);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		String corrId = java.util.UUID.randomUUID().toString();
		boolean multi = true;

		Request request = Request.newAsyncRequest(corrId, methodName, args, multi);

		// Add the correlation ID and create a replyTo property
		BasicProperties props = new BasicProperties.Builder().appId(uid).correlationId(corrId).type(serializerType).build();

		byte[] bytesRequest = serializer.serialize(serializerType, request);
		broker.publishMessge(exchange, routingkey, props, bytesRequest);

		logger.debug("Proxymq: " + uid + " invokes " + methodName + ", corrID" + corrId + ", exchange: " + exchange + ", replyQueue: " + replyQueueName
				+ ", serializerType: " + serializerType + ", multi call: " + request.isMulti() + ", async call: " + request.isAsync());

		return null;
	}

}
