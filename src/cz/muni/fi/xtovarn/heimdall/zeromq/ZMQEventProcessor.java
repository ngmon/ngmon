package cz.muni.fi.xtovarn.heimdall.zeromq;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.zeromq.deprecated.ZMQMessageProcessor;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * ZeroMQ ZMQ Processor Device implementation.
 *
 * @author Daniel Tovarňák, Alois Belaska <alois.belaska@gmail.com>
 */
public class ZMQEventProcessor implements Runnable {

	private final ZMQ.Poller poller;
	private final Socket inSocket;
	private final JSONProcessor messageProcessor;
	private final BlockingQueue<Event> workQueue;

	/**
	 * Class constructor.
	 *
	 * @param context   a 0MQ context previously created.
	 * @param inSocket  input socket
	 */
	public ZMQEventProcessor(Context context, Socket inSocket, BlockingQueue<Event> workQueue) {
		this.inSocket = inSocket;
		this.poller = context.poller(1);
		this.poller.register(inSocket, ZMQ.Poller.POLLIN);

		this.workQueue = workQueue;

		messageProcessor = new JSONProcessor();
	}

	/**
	 * Processing messages.
	 */
	@Override
	public void run() {
		Event event;
		while (!Thread.currentThread().isInterrupted()) {
			List<byte[]> message = new ArrayList<byte[]>(1);
			boolean rcv_more = true;

			try {
				/* wait while there are requests to processEvent */
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
					event = messageProcessor.process(message); // Process byte[] message
					workQueue.put(event); // add to queue
				}

			} catch (ZMQException e) {
				if (ZMQ.Error.ENOTSUP.getCode() == e.getErrorCode()) {
					System.err.println(e.getMessage() + ", ERROR_CODE:" + e.getErrorCode()); // TODO correct error code
					break;
				} else if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) { // context destroyed, exit
					break;
				}
				throw e;
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}
}
