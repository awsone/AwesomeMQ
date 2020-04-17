package omq.test.serializer;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;
import omq.test.calculator.Calculator;
import omq.test.calculator.CalculatorImpl;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class CalculatorTest {

	private static Broker broker;
	private static Calculator remoteCalc;

	public CalculatorTest() throws Exception {
		Properties env = new Properties();
		env.setProperty(ParameterQueue.USER_NAME, "guest");
		env.setProperty(ParameterQueue.USER_PASS, "guest");

		// Set host info of rabbimq (where it is)
		env.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env.setProperty(ParameterQueue.RABBIT_PORT, "5672");
		env.setProperty(ParameterQueue.DURABLE_QUEUE, "false");
		env.setProperty(ParameterQueue.ENABLE_COMPRESSION, "false");

		// Set info about where the message will be sent
		env.setProperty(ParameterQueue.RPC_EXCHANGE, "rpc_exchange");
		// env.setProperty(ParameterQueue.DEBUGFILE, "c:\\middlewareDebug");

		// Set info about the queue & the exchange where the ResponseListener
		// will listen to.
		env.setProperty(ParameterQueue.RPC_REPLY_QUEUE, "reply_queue");

		broker = new Broker(env);
		remoteCalc = broker.lookup("calculator1", Calculator.class);
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

	@Test
	public void add() throws Exception {
		int x = 10;
		int y = 20;

		int sync = remoteCalc.add(x, y);
		int sum = x + y;

		assertEquals(sum, sync);
	}
}
