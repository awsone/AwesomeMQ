package omq.test.persistence;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@RunWith(value = Parameterized.class)
public class PersistentTest {
	private static String MESSAGE = "message";
	private String type;
	private Properties clientProps;
	private Properties serverProps;
	private Properties msgImplProps;

	public PersistentTest(String type) {
		this.type = type;

		/*
		 * Server Properties
		 */
		serverProps = new Properties();
		serverProps.setProperty(ParameterQueue.USER_NAME, "guest");
		serverProps.setProperty(ParameterQueue.USER_PASS, "guest");

		// Set host info of rabbtimq (where it is)
		serverProps.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		serverProps.setProperty(ParameterQueue.RABBIT_PORT, "5672");

		/*
		 * MessageImpl Properties
		 */
		msgImplProps = new Properties();
		msgImplProps.setProperty(ParameterQueue.RPC_EXCHANGE, "rpc_exchange");
		msgImplProps.setProperty(ParameterQueue.DURABLE_QUEUE, "true");
		msgImplProps.setProperty(ParameterQueue.EXCLUSIVE_QUEUE, "false");
		msgImplProps.setProperty(ParameterQueue.AUTO_DELETE_QUEUE, "false");
		msgImplProps.setProperty(ParameterQueue.MULTI_QUEUE_NAME, "multiMessageQueue");
		msgImplProps.setProperty(ParameterQueue.DURABLE_MQUEUE, "true");
		msgImplProps.setProperty(ParameterQueue.EXCLUSIVE_MQUEUE, "true");
		msgImplProps.setProperty(ParameterQueue.AUTO_DELETE_MQUEUE, "false");
		msgImplProps.setProperty(ParameterQueue.DELIVERY_MODE, "1");
		msgImplProps.setProperty(ParameterQueue.RETRY_TIME_CONNECTION, "2000");

		/*
		 * Client Properties
		 */

		clientProps = new Properties();
		clientProps.setProperty(ParameterQueue.USER_NAME, "guest");
		clientProps.setProperty(ParameterQueue.USER_PASS, "guest");

		// Set host info of rabbimq (where it is)
		clientProps.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		clientProps.setProperty(ParameterQueue.RABBIT_PORT, "5672");
		clientProps.setProperty(ParameterQueue.DURABLE_QUEUE, "true");
		clientProps.setProperty(ParameterQueue.PROXY_SERIALIZER, type);
		clientProps.setProperty(ParameterQueue.RPC_EXCHANGE, "rpc_exchange");
		clientProps.setProperty(ParameterQueue.RPC_REPLY_QUEUE, "persistent_message_reply_queue");
		clientProps.setProperty(ParameterQueue.DURABLE_QUEUE, "true");
		clientProps.setProperty(ParameterQueue.EXCLUSIVE_QUEUE, "true");
		clientProps.setProperty(ParameterQueue.AUTO_DELETE_QUEUE, "false");
		clientProps.setProperty(ParameterQueue.DELIVERY_MODE, "2");
		clientProps.setProperty(ParameterQueue.RETRY_TIME_CONNECTION, "2000");
		// TODO msgProps -> rpc_exchange, serializer type and delivery mode
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { Serializer.JAVA }, { Serializer.GSON }, { Serializer.KRYO } };
		return Arrays.asList(data);
	}

	@Test
	public void test() throws Exception {
		System.out.println("Type = " + type);
		String expected = "message";
		String actual = null;

		// Ensure the queues exist
		Broker serverBroker = new Broker(serverProps);
		MessageImpl msgImpl = new MessageImpl();
		serverBroker.bind(MESSAGE, msgImpl, msgImplProps);

		// Stop the serverBroker
		serverBroker.stopBroker();

		Broker clientBroker = new Broker(clientProps);
		Message iMsg = clientBroker.lookup(MESSAGE, Message.class);
		iMsg.setMessage(expected);

		// Restart the rabbitmq
		String password = "unpc";
		String[] command = { "/bin/bash", "-c", "echo " + password + " | sudo -S service rabbitmq-server restart" };

		Runtime runtime = Runtime.getRuntime();
		runtime.exec(command);

		Thread.sleep(15000);

		// Restart the server and listen to the new messages
		serverBroker = new Broker(serverProps);
		msgImpl = new MessageImpl();
		serverBroker.bind(MESSAGE, msgImpl, msgImplProps);

		actual = iMsg.getMessage();

		// Stop both brokers
		serverBroker.stopBroker();
		clientBroker.stopBroker();

		assertEquals(expected, actual);
	}
}
