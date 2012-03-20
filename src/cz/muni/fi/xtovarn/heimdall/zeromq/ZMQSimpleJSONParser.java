package cz.muni.fi.xtovarn.heimdall.zeromq;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.JSONStringParser;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.io.IOException;

/**
 * ZeroMQ Forwarder Device implementation.
 *
 * @author Alois Belaska <alois.belaska@gmail.com>
 */
public class ZMQSimpleJSONParser implements Runnable {

	private final ZMQ.Poller poller;
	private final ZMQ.Socket inSocket;
	private final ZMQ.Socket outSocket;

	/**
	 * Class constructor.
	 *
	 * @param context	a 0MQ context previously created.
	 * @param inSocket  input socket
	 * @param outSocket output socket
	 */
	public ZMQSimpleJSONParser(Context context, Socket inSocket, Socket outSocket) {
		this.inSocket = inSocket;
		this.outSocket = outSocket;

		this.poller = context.poller(1);
		this.poller.register(inSocket, ZMQ.Poller.POLLIN);
	}

	/**
	 * Forwarding messages.
	 */
	@Override
	public void run() {
		byte[] msg = null;
		boolean more = true;
		String jsonMsg;
		
		while (!Thread.currentThread().isInterrupted()) {
			try {
				// wait while there are requests to process
				if (poller.poll(250000) < 1) {
					continue;
				}

				msg = inSocket.recv(0);

				more = inSocket.hasReceiveMore();

				if (msg != null) {
					jsonMsg = (new String(msg)).trim();
					Event event = JSONStringParser.stringToEvent(jsonMsg);
					System.out.println(event.toString());
				}
			} catch (ZMQException e) {
				// context destroyed, exit
				if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) {
					break;
				}
				throw e;
			} catch (JsonParseException e) {
				System.err.println(e.getMessage());
				
			} catch (JsonMappingException e) {
				System.err.println(e.getMessage());
						
			} catch (IOException e) {
				e.printStackTrace();  
			}
		}
	}
}
