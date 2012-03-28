package cz.muni.fi.xtovarn.heimdall.zeromq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.concurrent.BlockingQueue;

/**
 * ZeroMQ ZMQ Processor Device implementation.
 *
 * @author Daniel Tovarňák, Alois Belaska <alois.belaska@gmail.com>
 */
public class ZMQStringReciever implements Runnable {

	private final Socket inSocket;
	private final BlockingQueue<String> outWorkQueue;

	public ZMQStringReciever(Socket inSocket, BlockingQueue<String> outWorkQueue) {
		this.inSocket = inSocket;
		this.outWorkQueue = outWorkQueue;
	}

	/**
	 * Recieve and enqueue messages.
	 */
	@Override
	public void run() {

		System.out.println(String.format("%-78s", this.getClass().getSimpleName()).replace(" ", ".") + "STARTED");

		while (!Thread.currentThread().isInterrupted()) {
			byte[] message;

			try {
					message = inSocket.recv(0); // BLOCKING recieve

				/* Push event into queue */
				if (message.length != 0) {
					outWorkQueue.put(work(message)); // add string to outcoming work queue
				}

			} catch (ZMQException e) {
				if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) { // context destroyed, exit
					System.err.println(this.getClass().getSimpleName() + ": ZMQException logged");
					inSocket.close();
					break;
				}

			} catch (InterruptedException e) {
				System.err.println(this.getClass().getSimpleName() + ": InterruptedException logged");
				break;
			}
		}

		System.out.println(String.format("%-78s", this.getClass().getSimpleName()).replace(" ", ".") + "STOPPED");
	}

	public String work(byte[] message) {

		return new String(message).trim();
	}
}
