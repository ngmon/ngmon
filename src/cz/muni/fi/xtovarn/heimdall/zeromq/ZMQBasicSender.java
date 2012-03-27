package cz.muni.fi.xtovarn.heimdall.zeromq;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.JSONEventMapper;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * ZeroMQ ZMQ Processor Device implementation.
 *
 * @author Daniel Tovarňák, Alois Belaska <alois.belaska@gmail.com>
 */
public class ZMQBasicSender implements Runnable {

	private final Socket outSocket;
	private final BlockingQueue<Event> inWorkQueue;

	/**
	 * Class constructor.
	 *
	 * @param inWorkQueue out work Queue
	 * @param outSocket   input socket
	 */
	public ZMQBasicSender(BlockingQueue<Event> inWorkQueue, Socket outSocket) {
		this.inWorkQueue = inWorkQueue;
		this.outSocket = outSocket;
	}

	/**
	 * Dequeue and send messages
	 */
	@Override
	public void run() {
		System.out.println(String.format("%-78s", this.getClass().getSimpleName()).replace(" ", ".") + "STARTED");

		while (!Thread.currentThread().isInterrupted()) {
			try {
				List<byte[]> outputMessage = new ArrayList<byte[]>(1);
				boolean snd_more;
				outputMessage.add(JSONEventMapper.eventAsBytes(inWorkQueue.take()));

				/* Iterate over message parts */
				for (int i = 0; i < outputMessage.size(); i++) {
					snd_more = (outputMessage.size() - i) != 1; // Do we have the last part of the message?
					byte[] messagePart = outputMessage.get(i);
					outSocket.send(messagePart, snd_more ? ZMQ.SNDMORE : 0); // BLOCKING Send
				}

			} catch (ZMQException e) {

				if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) { // context destroyed, exit
					outSocket.close();
					System.err.println(String.format("%-78s", this.getClass().getSimpleName()).replace(" ", ".") + "STOPPED-term");
					break;
				}

			} catch (InterruptedException e) {
				outSocket.close();
				System.err.println(String.format("%-78s", this.getClass().getSimpleName()).replace(" ", ".") + "STOPPED-int");
				break;

			} catch (IOException e) {
				e.printStackTrace();  //TODO exception
			}
		}
	}
}
