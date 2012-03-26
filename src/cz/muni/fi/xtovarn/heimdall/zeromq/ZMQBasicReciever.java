package cz.muni.fi.xtovarn.heimdall.zeromq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * ZeroMQ ZMQ Processor Device implementation.
 *
 * @author Daniel Tovarňák, Alois Belaska <alois.belaska@gmail.com>
 */
public class ZMQBasicReciever implements Runnable {

	private final ZMQ.Poller poller;
	private final Socket inSocket;
	private final BlockingQueue<List<byte[]>> outWorkQueue;

	/**
	 * Class constructor.
	 *
	 * @param outWorkQueue out work Queue
	 * @param context   a 0MQ context previously created.
	 * @param inSocket  input socket
	 */
	public ZMQBasicReciever(Socket inSocket, BlockingQueue<List<byte[]>> outWorkQueue, Context context) {
		this.inSocket = inSocket;

		this.poller = context.poller(1);
		this.poller.register(inSocket, ZMQ.Poller.POLLIN);

		this.outWorkQueue = outWorkQueue;
	}

	/**
	 * Recieve and enqueue messages.
	 */
	@Override
	public void run() {

		while (!Thread.currentThread().isInterrupted()) {
			List<byte[]> message = new ArrayList<byte[]>(1);
			boolean rcv_more = true;

			try {
				/* Wait while there are requests to processEvent */
				if (poller.poll(250000) < 1) {
					continue;
				}

				/* Recieve whole multi-part message */
				while (rcv_more) {
					message.add(inSocket.recv(0));
					rcv_more = inSocket.hasReceiveMore();
				}

				/* Push event into queue */
				if (!message.isEmpty()) {
					outWorkQueue.put(message); // add message to outcoming work queue
				}

			} catch (ZMQException e) {
				if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) { // context destroyed, exit
					break;
				}
				throw e;
			} catch (InterruptedException e) {
				e.printStackTrace();  // TODO interrupted
			}
		}
	}
}
