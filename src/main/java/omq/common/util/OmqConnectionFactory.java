package omq.common.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * This class creates RabbitMQ connections
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class OmqConnectionFactory {
	private static final Logger logger = Logger.getLogger(OmqConnectionFactory.class.getName());

	private static Connection connection;
	private static int connectionTimeout = 2 * 1000;

	/**
	 * If this class is wanted to use as a singleton class this is the init
	 * function
	 * 
	 * @param env
	 *            - environment that sets the properties needed by RabbitMQ
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void init(Properties env) throws KeyManagementException, NoSuchAlgorithmException, IOException {
		if (connection == null) {
			connection = getNewConnection(env);
		}
	}

	/**
	 * This function returns a working connection.
	 * 
	 * @param env
	 *            - used if it's necessary to create a new connection
	 * @return workingConnection
	 * @throws Exception
	 */
	public static Connection getNewWorkingConnection(Properties env) throws Exception {
		Connection connection = null;
		boolean working = false;

		while (!working) {
			try {
				connection = getNewConnection(env);
				working = true;
			} catch (Exception e) {
				logger.error(e);
				long milis = 2000;
				if (env != null) {
					milis = Long.parseLong(env.getProperty(ParameterQueue.RETRY_TIME_CONNECTION, "2000"));
				}
				Thread.sleep(milis);
			}
		}

		return connection;
	}

	/**
	 * This function creates a new rabbitmq connection using the properties set
	 * in env
	 * 
	 * @param env
	 *            - Properties needed to create a new connection: username,
	 *            password, rabbit_host, rabbit_port, enable_ssl (optional)
	 * @return new Connection
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	public static Connection getNewConnection(Properties env) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		// Get login info of rabbitmq
		String username = env.getProperty(ParameterQueue.USER_NAME);
		String password = env.getProperty(ParameterQueue.USER_PASS);

		// Get host info of rabbimq (where it is)
		String host = env.getProperty(ParameterQueue.RABBIT_HOST);
		int port = Integer.parseInt(env.getProperty(ParameterQueue.RABBIT_PORT));

		boolean ssl = Boolean.parseBoolean(env.getProperty(ParameterQueue.ENABLE_SSL, "false"));

		// Start a new connection and channel
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setHost(host);
		factory.setPort(port);
		factory.setConnectionTimeout(connectionTimeout);
		if (ssl) {
			factory.useSslProtocol();
		}

		Connection connection = factory.newConnection();
		logger.info("New connection created using: username: " + username + ", host: " + host + ", port: " + port + ", connection timeout: "
				+ connectionTimeout + " SSL enabled: " + ssl);
		return connection;
	}

	/**
	 * This method creates a new channel if the singleton pattern is used
	 * 
	 * @return new Channel
	 * @throws IOException
	 */
	public static Channel getNewChannel() throws IOException {
		Channel channel = connection.createChannel();
		logger.info("New channel created using the default connection");
		return channel;
	}
}
