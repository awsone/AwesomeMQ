package omq.test.workspace;

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
public class WorkspaceTest {
	private String type;
	private static Broker serverBroker;
	private static Broker clientBroker1;
	private static Broker clientBroker2;
	private static RemoteWorkspaceImpl w1C1;
	private static RemoteWorkspaceImpl w3C1;
	private static RemoteWorkspaceImpl w1C2;
	private static RemoteWorkspaceImpl w2C2;
	private static String[] workspaces = { "w1", "w2", "w3" };
	private static RemoteWorkspace[] remoteWorks = new RemoteWorkspace[3];

	// In this case the Constructor acts as a server
	public WorkspaceTest(String type) throws Exception {
		this.type = type;
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

		serverBroker = new Broker(env);
		int i = 0;
		for (String w : workspaces) {
			remoteWorks[i++] = serverBroker.lookupMulti(w, RemoteWorkspace.class);
		}

	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { Serializer.JAVA }, { Serializer.GSON }, { Serializer.KRYO } };
		return Arrays.asList(data);
	}

	@BeforeClass
	public static void client() throws Exception {
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

		clientBroker1 = new Broker(env);
		clientBroker2 = new Broker(env);

		// Client 1 will subscribe to changes in the workspaces w1 and w3
		w1C1 = new RemoteWorkspaceImpl();
		w3C1 = new RemoteWorkspaceImpl();

		clientBroker1.bind(workspaces[0], w1C1);
		clientBroker1.bind(workspaces[2], w3C1);

		// Client 2 will subscribe to changes in the workspaces w1 and w2
		w1C2 = new RemoteWorkspaceImpl();
		w2C2 = new RemoteWorkspaceImpl();

		clientBroker2.bind(workspaces[0], w1C2);
		clientBroker2.bind(workspaces[1], w2C2);

		System.out.println("Client 1 & client2 started");
	}

	@After
	public void stop() throws Exception {
		serverBroker.stopBroker();
	}

	@Test
	public void test() throws Exception {
		System.out.println("Starting test: " + type);
		String expected = null;
		String actual = null;

		// The server will notify a change in the w2
		expected = "w2 has changed";
		Info info = new Info(expected);
		remoteWorks[1].update(info);
		Thread.sleep(200);

		// If everything has worked, the client2 will see a change in w2
		actual = w2C2.getInfo().getId();
		assertEquals(expected, actual);

		// The server will notify a change in the w1
		expected = "w1 has changed";
		info.setId(expected);
		remoteWorks[0].update(info);
		Thread.sleep(200);

		// If everything has worked, both clients will see a change in w1
		actual = w1C1.getInfo().getId();
		assertEquals(expected, actual);
		actual = w1C2.getInfo().getId();
		assertEquals(expected, actual);
	}

}
