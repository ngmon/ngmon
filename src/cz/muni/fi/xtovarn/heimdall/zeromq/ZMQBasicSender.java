package cz.muni.fi.xtovarn.heimdall.zeromq;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.JSONEventMapper;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * @param outSocket	input socket
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
				List<byte[]> outputMessage = work(inWorkQueue.take());
				boolean snd_more;

				/* Iterate over message parts */
				for (int i = 0; i < outputMessage.size(); i++) {
					snd_more = (outputMessage.size() - i) != 1; // Do we have the last part of the message?
					byte[] messagePart = outputMessage.get(i);
					boolean sent = outSocket.send(messagePart, snd_more ? ZMQ.SNDMORE : 0); // BLOCKING Send
					System.out.println("SENDING[" + sent + "]: {" + messagePart.length + "} " + Arrays.toString(messagePart));
				}

			} catch (ZMQException e) {
				System.err.println(this.getClass().getSimpleName() + ": ZMQException logged");

				if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) { // context destroyed, exit
					System.err.println(this.getClass().getSimpleName() + ": ZMQException logged");
					outSocket.close();
					break;
				}

			} catch (InterruptedException e) {
				System.err.println(this.getClass().getSimpleName() + ": InterruptedException logged");
				outSocket.close();
				break;
			}
		}

		System.out.println(String.format("%-78s", this.getClass().getSimpleName()).replace(" ", ".") + "STOPPED");

	}

//	@Override
	public List<byte[]> work(Event event) {
		List<byte[]> output = new ArrayList<byte[]>(1);

		try {
			output.add(JSONEventMapper.eventAsBytes(event));
		} catch (IOException e) {
			e.printStackTrace(); // TODO HAndle
		}

		return output;
	}
}
