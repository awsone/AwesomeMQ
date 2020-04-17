package omq.test.python;

import java.util.Properties;

import org.junit.Test;

import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Server {

	@Test
	public void test() throws Exception {
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

		broker.bind("calculator", new CalculatorImpl());

		Broker broker2 = new Broker(env);

		broker2.bind("calculator", new CalculatorImpl());

		broker.bind("list", new ContactListImpl());

		Thread.sleep(60 * 1000);
	}

}
