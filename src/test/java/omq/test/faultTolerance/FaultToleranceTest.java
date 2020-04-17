package omq.test.faultTolerance;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;
import omq.test.calculator.Calculator;
import omq.test.calculator.CalculatorImpl;

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
public class FaultToleranceTest {
	private static Broker broker;
	private static Calculator remoteCalc;

	public FaultToleranceTest(String type) throws Exception {
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
		// env.setProperty(ParameterQueue.DEBUGFILE, "c:\\middlewareDebug");

		// Set info about the queue & the exchange where the ResponseListener
		// will listen to.
		env.setProperty(ParameterQueue.RPC_REPLY_QUEUE, "reply_queue");
		env.setProperty(ParameterQueue.RETRY_TIME_CONNECTION, "5000");

		broker = new Broker(env);
		remoteCalc = broker.lookup("calculator1", Calculator.class);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { Serializer.JAVA }, { Serializer.GSON }, { Serializer.KRYO } };
		return Arrays.asList(data);
	}

	@After
	public void stop() throws Exception {
		broker.stopBroker();
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

		CalculatorImpl calc = new CalculatorImpl();

		broker = new Broker(env);
		broker.bind("calculator1", calc);

		System.out.println("Server started");
	}

	@Test
	public void toleranceTest() throws Exception {
		int x = 10;
		int y = 20;
		int sum = 10 + 20;

		int sync = remoteCalc.add(x, y);

		String password = "unpc";
		String[] command = { "/bin/bash", "-c", "echo " + password + " | sudo -S service rabbitmq-server restart" };

		Runtime runtime = Runtime.getRuntime();
		runtime.exec(command);

		Thread.sleep(15000);
		int resp = remoteCalc.add(x, y);

		assertEquals(sum, sync);
		assertEquals(sum, resp);
	}

}
