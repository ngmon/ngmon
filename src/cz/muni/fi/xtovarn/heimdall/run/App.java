package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.netty.UserQueues;
import cz.muni.fi.xtovarn.heimdall.stage.NettyStage;
import cz.muni.fi.xtovarn.heimdall.stage.Stage;
import cz.muni.fi.xtovarn.heimdall.stage.StoreStage;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.store.EventStoreFactory;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQBasicSender;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQContextFactory;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQStringReciever;
import org.zeromq.ZMQ;

import java.io.IOException;
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


		BlockingQueue<String> queue1 = new ArrayBlockingQueue<String>(10);
		BlockingQueue<Event> queue2 = UserQueues.queue("xdanos");

		final ZMQStringReciever rcvrStage = new ZMQStringReciever(reciever, queue1);
		final Stage<String, Event> storeStage = new StoreStage(queue1, queue2, eventStore);
		final NettyStage senderStage = new NettyStage();

		final Thread rcvrStage_thread = new Thread(rcvrStage);
		final Thread storeStage_thread = new Thread(storeStage);
		final Thread senderStage_thread = new Thread(senderStage);

		/* Inner class, handles shutdowns by Ctrl+C */
		class ShutdownHandler implements Runnable {

			@Override
			public void run() {
				storeStage_thread.interrupt();
				rcvrStage_thread.interrupt();
				senderStage_thread.interrupt();

				context.term();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));

		storeStage_thread.start();
		senderStage_thread.start();
		rcvrStage_thread.start();

		rcvrStage_thread.join();
		senderStage_thread.join();
		storeStage_thread.join();

		Thread.sleep(10);

		System.out.println("Shutting down...");

		eventStore.close();
	}
}
