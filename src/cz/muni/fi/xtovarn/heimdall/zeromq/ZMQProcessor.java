package cz.muni.fi.xtovarn.heimdall.zeromq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

/**
 * ZeroMQ Forwarder Device implementation.
 *
 * @author Alois Belaska <alois.belaska@gmail.com>
 */
public class ZMQProcessor implements Runnable {

	private final ZMQ.Poller poller;
	private final ZMQ.Socket inSocket;
	private final ZMQ.Socket outSocket;
	private final MessageProcessor processor;

	/**
	 * Class constructor.
	 *
	 * @param context	a 0MQ context previously created.
	 * @param inSocket  input socket
	 * @param outSocket output socket
	 */
	public ZMQProcessor(Context context, Socket inSocket, Socket outSocket, MessageProcessor processor) {
		this.inSocket = inSocket;
		this.outSocket = outSocket;

		this.poller = context.poller(1);
		this.poller.register(inSocket, ZMQ.Poller.POLLIN);

		this.processor = processor;
	}

	/**
	 * Forwarding messages.
	 */
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			byte[][] message = null;
			boolean more = true;
			int part = 0;

			try {

				/* wait while there are requests to process */
				if (poller.poll(250000) < 1) {
					continue;
				}

				/* Recieve whole multi-part message */
				while (inSocket.hasReceiveMore()) {
					message[part] = inSocket.recv(0);
					part++;
				}

				if (message[0] != null) {
					byte[][] outputMessage = processor.process(message);

					/* Send whole multi-part message */
					for (int i = 0; i < outputMessage.length; i++) {
						more = (outputMessage.length-i) != 1; // Do we have the last part of the message?
						byte[] messagePart = outputMessage[i];
						outSocket.send(messagePart, more ? ZMQ.SNDMORE : 0);
					}
				}
			} catch (ZMQException e) {

				/* context destroyed, exit */
				if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) {
					break;
				}
				throw e;
			}
		}
	}
}
