package omq.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */

public class RemoteThreadPool {
	private static final Logger logger = Logger.getLogger(RemoteThreadPool.class.getName());
	private List<InvocationThread> workers;
	private MultiInvocationThread multiWorker;

	private int numThreads;
	private RemoteObject obj;

	public RemoteThreadPool(int numThreads, RemoteObject obj) {
		this.obj = obj;
		this.numThreads = numThreads;

		workers = new ArrayList<InvocationThread>(numThreads);
	}

	public void startPool() {

		logger.info("ObjectMQ reference: " + obj.getRef() + ", creating: " + numThreads);

		try {
			multiWorker = new MultiInvocationThread(obj);
			multiWorker.start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		for (int i = 0; i < numThreads; i++) {
			try {
				InvocationThread iThread = new InvocationThread(obj);
				workers.add(iThread);
				iThread.start();
			} catch (Exception e) {
				logger.error("Error while creating pool threads", e);
				e.printStackTrace();
			}
		}
	}

	public void kill() throws IOException {
		multiWorker.kill();
		for (InvocationThread iThread : workers) {
			iThread.kill();
		}
	}

	public List<InvocationThread> getWorkers() {
		return workers;
	}

	public void setWorkers(List<InvocationThread> workers) {
		this.workers = workers;
	}

}
