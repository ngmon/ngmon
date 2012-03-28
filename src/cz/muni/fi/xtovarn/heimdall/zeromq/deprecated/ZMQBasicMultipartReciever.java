package cz.muni.fi.xtovarn.heimdall.zeromq.deprecated;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Deprecated
public class ZMQBasicMultipartReciever implements Runnable {

	private final Socket inSocket;
	private final BlockingQueue<List<byte[]>> outWorkQueue;
	private boolean stoppedFlag;

	public ZMQBasicMultipartReciever(Socket inSocket, BlockingQueue<List<byte[]>> outWorkQueue) {
		this.inSocket = inSocket;
		this.outWorkQueue = outWorkQueue;
	}

	/**
	 * Recieve and enqueue messages.
	 */
	@Override
	public void run() {

		while (!stoppedFlag) {

			List<byte[]> message = new ArrayList<byte[]>(1);
			boolean rcv_more = true;

			try {
				/* Recieve whole multi-part message */
				while (rcv_more) {
					message.add(inSocket.recv(0)); // BLOCKING recieve
					rcv_more = inSocket.hasReceiveMore();
				}

				/* Push event into queue */
				if (!message.isEmpty()) {
					outWorkQueue.put(message); // add message to outcoming work queue
				}

			} catch (ZMQException e) {

				if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) { // context destroyed, exit
					inSocket.close();
					break;
				}

			} catch (InterruptedException e) {
				e.printStackTrace();  // TODO interrupted
			}
		}
	}
}
