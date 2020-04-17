package omq.server;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import omq.common.util.Serializer;

import org.apache.log4j.Logger;

import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * This class is used to encapsulate the invocationThreads under the
 * RemoteObject.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class RemoteWrapper {
	private static final Logger logger = Logger.getLogger(RemoteWrapper.class.getName());

	private RemoteObject obj;
	private int numThreads;
	private ArrayList<InvocationThread> invocationList;
	private BlockingQueue<Delivery> deliveryQueue;

	public RemoteWrapper(RemoteObject obj, int numThreads, Serializer serializer) {
		this.obj = obj;
		this.numThreads = numThreads;
		invocationList = new ArrayList<InvocationThread>();
		deliveryQueue = new LinkedBlockingDeque<QueueingConsumer.Delivery>();

		logger.info("Object reference: " + obj.getRef() + ", numthreads listening = " + numThreads);

		for (int i = 0; i < numThreads; i++) {
			InvocationThread thread = new InvocationThread(obj, deliveryQueue, serializer);
			invocationList.add(thread);
			thread.start();
		}
	}

	/**
	 * This method notifies a delivery to an invocationThread using a
	 * blockingQueue.
	 * 
	 * @param delivery
	 *            - delivery which contains a Request to be invoked
	 * @throws Exception
	 */
	public void notifyDelivery(Delivery delivery) throws Exception {
		this.deliveryQueue.put(delivery);
	}

	/**
	 * This method interrups all the invocationThreads under this remoteWrapper
	 */
	public void stopRemoteWrapper() {
		logger.warn("Stopping Invocation threads vinculed to " + obj.getRef());
		for (InvocationThread thread : invocationList) {
			thread.interrupt();
		}
	}

	public RemoteObject getObj() {
		return obj;
	}

	public void setObj(RemoteObject obj) {
		this.obj = obj;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public ArrayList<InvocationThread> getInvocationList() {
		return invocationList;
	}

	public void setInvocationList(ArrayList<InvocationThread> invocationList) {
		this.invocationList = invocationList;
	}

	public BlockingQueue<Delivery> getDeliveryQueue() {
		return deliveryQueue;
	}

	public void setDeliveryQueue(BlockingQueue<Delivery> deliveryQueue) {
		this.deliveryQueue = deliveryQueue;
	}
}
