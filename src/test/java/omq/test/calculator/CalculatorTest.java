package omq.test.calculator;

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
public class CalculatorTest {

	private static Broker broker;
	private static Calculator remoteCalc;
	private static Calculator remoteCalc2;

	public CalculatorTest(String type) throws Exception {
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
		remoteCalc = broker.lookup("calculator1", Calculator.class);
		remoteCalc2 = broker.lookup("calculator2", Calculator.class);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { Serializer.JAVA }, { Serializer.GSON }, { Serializer.KRYO } };
		return Arrays.asList(data);
	}

	@BeforeClass
	public static void server() throws Exception {
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

		CalculatorImpl calc = new CalculatorImpl();
		CalculatorImpl calc2 = new CalculatorImpl();

		Broker broker = new Broker(env);
		broker.bind("calculator1", calc);
		broker.bind("calculator2", calc2);

		System.out.println("Server started");
	}

	@After
	public void stop() throws Exception {
		broker.stopBroker();
	}

	@Test
	public void add() throws Exception {
		int x = 10;
		int y = 20;

		int sync = remoteCalc.add(x, y);
		int sum = x + y;

		assertEquals(sum, sync);
	}

	@Test
	public void add2() throws Exception {
		int x = 10;
		int y = 20;

		int sync = remoteCalc2.add(x, y);
		int sum = x + y;

		assertEquals(sum, sync);
	}

	@Test
	public void mult() throws Exception {
		int x = 5;
		int y = 15;

		remoteCalc.mult(x, y);
		Thread.sleep(200);
	}

	@Test
	public void sendMessage() throws Exception {
		Message m = new Message(2334, "Hello objectmq");
		remoteCalc.sendMessage(m);
	}

	@Test(expected = ArithmeticException.class)
	public void divideByZero() {
		remoteCalc.divideByZero();
	}
}
