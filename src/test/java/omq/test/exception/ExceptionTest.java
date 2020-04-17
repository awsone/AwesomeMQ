package omq.test.exception;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.UndeclaredThrowableException;
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
public class ExceptionTest {
	private static Broker broker;
	private static ClientInterface client;

	public ExceptionTest(String type) throws Exception {
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
		client = broker.lookup("server", ClientInterface.class);
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

		OmqServerImpl server = new OmqServerImpl();

		Broker broker = new Broker(env);
		broker.bind("server", server);
	}

	@After
	public void stop() throws Exception {
		broker.stopBroker();
	}

	@Test
	public void addWheels() throws Exception {
		int wheels = 4;
		client.addWheels(wheels);
		Thread.sleep(200);
		int result = client.getWheels();

		assertEquals(wheels, result);
	}

	@Test
	public void addHp() throws Exception {
		int hp = 200;
		client.addHp(hp);
		Thread.sleep(200);
		int result = client.getHp();

		assertEquals(hp, result);
	}

	@Test
	public void addTrailer() throws Exception {
		Trailer t = new Trailer(1200);
		client.addTrailer(t);
		Thread.sleep(200);
	}

	@Test(expected = UndeclaredThrowableException.class)
	// This exception will be caused by java.lang.NoSuchMethodException
	public void getTrailer() throws Exception {
		client.getTrailer();
	}

	@Test
	public void setPrice() throws Exception {
		double price = 4999.99;
		client.setPrice(price);
		Thread.sleep(200);
	}

	@Test(expected = ClassCastException.class)
	public void getPrice() throws Exception {
		client.getPrice();
	}
}
