package omq.test.multiProcess;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;

import org.junit.After;
import org.junit.BeforeClass;
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
public class MultiProcessTest {
	public static Broker broker;
	public static NumberClient remoteNumber;

	public MultiProcessTest(String type) throws Exception {
		Properties env = new Properties();
		env.setProperty(ParameterQueue.USER_NAME, "guest");
		env.setProperty(ParameterQueue.USER_PASS, "guest");

		// Set host info of rabbimq (where it is)
		env.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env.setProperty(ParameterQueue.RABBIT_PORT, "5672");
		env.setProperty(ParameterQueue.DURABLE_QUEUE, "false");
		env.setProperty(ParameterQueue.PROXY_SERIALIZER, type);
		env.setProperty(ParameterQueue.ENABLE_COMPRESSION, "false");

		// Set info about where the message will be sent
		env.setProperty(ParameterQueue.RPC_EXCHANGE, "rpc_exchange");

		// Set info about the queue & the exchange where the ResponseListener
		// will listen to.
		env.setProperty(ParameterQueue.RPC_REPLY_QUEUE, "reply_queue");

		broker = new Broker(env);
		remoteNumber = broker.lookup("number", NumberClient.class);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { Serializer.JAVA }, { Serializer.GSON }, { Serializer.KRYO } };
		return Arrays.asList(data);
	}

	@BeforeClass
	public static void serverTest() throws Exception {
		Properties env = new Properties();
		env.setProperty(ParameterQueue.USER_NAME, "guest");
		env.setProperty(ParameterQueue.USER_PASS, "guest");

		// Get host info of rabbimq (where it is)
		env.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env.setProperty(ParameterQueue.RABBIT_PORT, "5672");
		env.setProperty(ParameterQueue.DURABLE_QUEUE, "false");
		env.setProperty(ParameterQueue.ENABLE_COMPRESSION, "false");

		// Set info about where the message will be sent
		env.setProperty(ParameterQueue.RPC_EXCHANGE, "rpc_exchange");
		env.setProperty(ParameterQueue.RETRY_TIME_CONNECTION, "2000");

		Broker broker = new Broker(env);
		broker.bind("number", new NumberImpl());

		Broker broker2 = new Broker(env);
		broker2.bind("number", new NumberImpl());
	}

	@After
	public void stop() throws Exception {
		broker.stopBroker();
	}

	@Test
	public void multiTest() throws Exception {
		int x = 10;
		remoteNumber.setMultiNumber(x);
		Thread.sleep(200);
		int a = remoteNumber.getNumber();
		int b = remoteNumber.getNumber();
		assertEquals(x, a);
		assertEquals(x, b);
		int[] number = remoteNumber.getMultiNumber();
		assertEquals(x, number[0]);
		assertEquals(x, number[1]);
		remoteNumber.setMultiNumber(0);
		Thread.sleep(200);
	}

}
