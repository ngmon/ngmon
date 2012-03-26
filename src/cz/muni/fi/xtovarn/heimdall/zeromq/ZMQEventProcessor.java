package cz.muni.fi.xtovarn.heimdall.zeromq;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.proc.AbstractAction;
import cz.muni.fi.xtovarn.heimdall.util.JSONEventMapper;
import cz.muni.fi.xtovarn.heimdall.util.JSONStringParser;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ZeroMQ ZMQ Processor Device implementation.
 *
 * @author Daniel Tovarňák, Alois Belaska <alois.belaska@gmail.com>
 */
public class ZMQEventProcessor implements Runnable {

	private final ZMQ.Poller poller;
	private final Socket inSocket;
	private final Socket outSocket;
	private final AbstractAction actionChain;

	/**
	 * Class constructor.
	 *
	 * @param context   a 0MQ context previously created.
	 * @param inSocket  input socket
	 * @param outSocket output socket
	 * @param actionChain ZMQMessageProcessor instance
	 */
	public ZMQEventProcessor(Context context, Socket inSocket, Socket outSocket, AbstractAction actionChain) {
		this.inSocket = inSocket;
		this.outSocket = outSocket;

		this.poller = context.poller(1);
		this.poller.register(inSocket, ZMQ.Poller.POLLIN);

		this.actionChain = actionChain;
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
					List<byte[]> outputMessage = new ArrayList<byte[]>();
					boolean snd_more;


					/* process message */
					Event event = null;
					try {

						event = JSONStringParser.stringToEvent(new String(message.get(0)).trim());
						actionChain.process(event);
						outputMessage.add(JSONEventMapper.eventAsBytes(event));

					} catch (IOException e) {
						System.err.println(e.getMessage());
					}


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
