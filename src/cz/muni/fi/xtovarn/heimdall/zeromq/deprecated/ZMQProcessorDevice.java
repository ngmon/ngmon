package cz.muni.fi.xtovarn.heimdall.zeromq.deprecated;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.ArrayList;
import java.util.List;

/**
 * ZeroMQ ZMQ Processor Device implementation.
 *
 * @author Daniel Tovarňák, Alois Belaska <alois.belaska@gmail.com>
 */
@Deprecated
public class ZMQProcessorDevice implements Runnable {

	private final ZMQ.Poller poller;
	private final ZMQ.Socket inSocket;
	private final ZMQ.Socket outSocket;

	/**
	 * Class constructor.
	 *
	 * @param context   a 0MQ context previously created.
	 * @param inSocket  input socket
	 * @param outSocket output socket
	 */
	public ZMQProcessorDevice(Context context, Socket inSocket, Socket outSocket) {
		this.inSocket = inSocket;
		this.outSocket = outSocket;

		this.poller = context.poller(1);
		this.poller.register(inSocket, ZMQ.Poller.POLLIN);

	}

	/**
	 * Processing messages.
	 */
	@Override
	public void run() {
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

				/* Send whole multi-part message */
				if (!message.isEmpty()) {
					List<byte[]> outputMessage = message; // processing logic
					boolean snd_more;

					/* Iterate over message parts */
					for (int i = 0; i < outputMessage.size(); i++) {
						snd_more = (outputMessage.size() - i) != 1; // Do we have the last part of the message?
						byte[] messagePart = outputMessage.get(i);
						outSocket.send(messagePart, snd_more ? ZMQ.SNDMORE : ZMQ.NOBLOCK); // TODO non-blocking send?
					}
				}
			} catch (ZMQException e) {
				if (ZMQ.Error.ENOTSUP.getCode() == e.getErrorCode()) {
					System.err.println(e.getMessage() + ", ERROR_CODE:" + e.getErrorCode()); // TODO correct error code
					break;
				} else if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) { // context destroyed, exit
					break;
				}
				throw e;
			}
		}
	}
}
