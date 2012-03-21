package cz.muni.fi.xtovarn.heimdall.zeromq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.ArrayList;
import java.util.List;

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
			List<byte[]> message =  new ArrayList<byte[]>(1);
			boolean rcv_more = true;

			try {
				/* wait while there are requests to process */
				if (poller.poll(250000) < 1) {
					continue;
				}

				/* Recieve whole multi-part message */
				while(rcv_more) {
					message.add(inSocket.recv(0));
					rcv_more = inSocket.hasReceiveMore();
				}

				if (!message.isEmpty()) {
					List<byte[]> outputMessage = processor.process(message);

					/* Send whole multi-part message */
					for (int i = 0; i < outputMessage.size(); i++) {
						snd_more = (outputMessage.size() -i) != 1; // Do we have the last part of the message?
						byte[] messagePart = outputMessage.get(i);
						outSocket.send(messagePart, rcv_more ? ZMQ.SNDMORE : 0);
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
