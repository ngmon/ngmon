package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.stage.ParseJSONStage;
import cz.muni.fi.xtovarn.heimdall.stage.SanitizeStage;
import cz.muni.fi.xtovarn.heimdall.stage.Stage;
import cz.muni.fi.xtovarn.heimdall.stage.StoreStage;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.store.EventStoreFactory;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQContextFactory;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQBasicReciever;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class App {

	private static final String RCV_ADDRESS = "tcp://*:359";

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {
		System.out.println("Heimdall is starting...");

		EventStore eventStore = EventStoreFactory.getInstance();
		final ZMQ.Context context = ZMQContextFactory.getInstance();

		// Socket facing clients
		final ZMQ.Socket reciever = context.socket(ZMQ.PULL);
		reciever.bind(RCV_ADDRESS);

		BlockingQueue<List<byte[]>> queue1 = new ArrayBlockingQueue<List<byte[]>>(2);
		BlockingQueue<Event> queue2 = new ArrayBlockingQueue<Event>(2);
		BlockingQueue<Event> queue3 = new ArrayBlockingQueue<Event>(2);
		BlockingQueue<Event> queue4 = new ArrayBlockingQueue<Event>(2);

		ZMQBasicReciever parser = new ZMQBasicReciever(reciever, queue1);
		Stage<List<byte[]>, Event> stage1 = new ParseJSONStage(queue1, queue2);
		Stage<Event, Event> stage2 = new SanitizeStage(queue2, queue3);
		Stage<Event, Event> stage3 = new StoreStage(queue3, queue4, eventStore);

		final Thread ww1 = new Thread(parser);
		final Thread ww2 = new Thread(stage1);
		final Thread ww3 = new Thread(stage2);
		final Thread ww4 = new Thread(stage3);


		/* Inner class, handles shutdowns by Ctrl+C */
		class ShutdownHandler implements Runnable {

			@Override
			public void run() {
				context.term();
				ww2.interrupt();
				ww3.interrupt();
				ww4.interrupt();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));

		ww4.start();
		ww2.start();
		ww3.start();
		ww1.start();


		/*while (!Thread.currentThread().isInterrupted()) {
			queue4.take();
		}*/

		ww1.join();
		ww2.join();
		ww3.join();
		ww4.join();

		Thread.sleep(10);

		System.out.println("Shutting down...");

		eventStore.close();
	}
}
