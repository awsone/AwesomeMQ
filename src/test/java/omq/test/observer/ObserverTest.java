package omq.test.observer;

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
public class ObserverTest {
	private static String SUBJECT = "subject";
	private static String OBSERVER = "observer";
	private static Broker broker;
	private static RemoteSubjectImpl subject;
	private static RemoteSubject remoteSubject;

	public ObserverTest(String type) throws Exception {
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
		remoteSubject = broker.lookup(SUBJECT, RemoteSubject.class);
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

		// Set info about the queue & the exchange where the ResponseListener
		// will listen to.
		env.setProperty(ParameterQueue.RPC_REPLY_QUEUE, "server_reply_queue");

		Broker broker = new Broker(env);
		subject = new RemoteSubjectImpl(broker);
		broker.bind(SUBJECT, subject);

		System.out.println("Server started");
	}

	@After
	public void stop() throws Exception {
		broker.stopBroker();
	}

	@Test
	public void test() throws Exception {
		String expected = "I'm fine";
		String actual = null;

		RemoteObserverImpl observer = new RemoteObserverImpl();
		broker.bind(OBSERVER, observer);
		observer.setSubject(remoteSubject);

		remoteSubject.addObserver(observer.getRef());
		remoteSubject.setState(expected);
		// both proxies are in the same thread for this reason since subject has
		// a proxy inside we need to use subject or create a new thread
		subject.notifyObservers();
		actual = observer.getObsState();

		assertEquals(expected, actual);
		remoteSubject.setState("");
	}

}
