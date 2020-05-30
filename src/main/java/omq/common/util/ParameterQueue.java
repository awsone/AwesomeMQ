package omq.common.util;

/**
 * This class is used to create new environments.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class ParameterQueue {

	/*
	 * Connection info
	 */

	/**
	 * Set the clients username
	 */
	public static String USER_NAME = "omq.username";

	/**
	 * Set the clients password
	 */
	public static String USER_PASS = "omq.pass";

	/**
	 * Set the ip where the rabbitmq server is.
	 */
	public static String RABBIT_HOST = "omq.host";

	/**
	 * Set the port that rabbitmq uses.
	 */
	public static String RABBIT_PORT = "omq.port";

	/**
	 * Set if the system will use ssl
	 */
	public static String ENABLE_SSL = "omq.enable_ssl";

	/**
	 * Set how many time we have to wait to retry the connection with the server
	 * when this goes down
	 */
	public static String RETRY_TIME_CONNECTION = "omq.retry_connection";

	/*
	 * Queues info
	 */

	/**
	 * Set the exchange where the objectmq are listening
	 */
	public static String RPC_EXCHANGE = "omq.rpc_exchange";

	/**
	 * Set the clients reply queue. Every client must have a different queue
	 * name.
	 */
	public static String RPC_REPLY_QUEUE = "omq.reply_queue_rpc";

	/**
	 * Set the specific name of a multi queue in a specific object
	 */
	public static String MULTI_QUEUE_NAME = "omq.multi_queue_name";

	/**
	 * Set if a queue must be durable. The queue won't be lost when RabbitMQ
	 * crashes if DURABLE_QUEUE is set true.
	 */
	public static String DURABLE_QUEUE = "omq.durable_queue";

	/**
	 * Set if server will delete a queue when is no longer in use
	 */
	public static String AUTO_DELETE_QUEUE = "omq.auto_delete";

	/**
	 * Set if we are declaring an exclusive queue (restricted to this
	 * connection)
	 */
	public static String EXCLUSIVE_QUEUE = "omq.exclusive_queue";

	/**
	 * Set if a queue must be durable. The queue won't be lost when RabbitMQ
	 * crashes if DURABLE_QUEUE is set true.
	 */
	public static String DURABLE_MQUEUE = "omq.durable_mqueue";

	/**
	 * Set if server will delete a queue when is no longer in use
	 */
	public static String AUTO_DELETE_MQUEUE = "omq.auto_mdelete";

	/**
	 * Set if we are declaring an exclusive queue (restricted to this
	 * connection)
	 */
	public static String EXCLUSIVE_MQUEUE = "omq.exclusive_mqueue";

	/**
	 * The MESSAGE_TTL_IN_QUEUES controls for how long a message published to
	 * the queues can live before it is discarded. A message that has been in
	 * the queue for longer than the configured TTL is said to be dead.
	 * 
	 * This property must be a non-negative 32 bit integer (0 <= n <= 2^32-1),
	 * describing the TTL period in milliseconds.
	 */
	public static String MESSAGE_TTL_IN_QUEUES = "omq.message_ttl_queue";

	/*
	 * Message info
	 */

	/**
	 * Set the proxy's serializer method
	 */
	public static String PROXY_SERIALIZER = "omq.serializer";

	/**
	 * Set whether the messages must be compressed or not
	 */
	public static String ENABLE_COMPRESSION = "omq.compression";

	/**
	 * Set 1 to indicate the message will be nonpersistent and 2 to indicate it
	 * will be persistent
	 */
	public static String DELIVERY_MODE = "omq.delivery_mode";

	/*
	 * ObjectMQ info
	 */

	/**
	 * Set how many threads will be created to invoke remote methods
	 */
	public static String NUM_THREADS = "omq.num_threads";

	/**
	 * Time in milis by default is set in a minute
	 */
	public static long DEFAULT_TIMEOUT = 1 * 1000 * 60;

	public static String DEFAULT_USER = "guest";
	public static String DEFAULT_PASS = "guest";
	public static String DEFAULT_RABBIT_HOST = "localhost";
	public static String DEFAULT_PORT = "5672";

}
