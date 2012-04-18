package cz.muni.fi.xtovarn.heimdall.runnable;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.pipeline.HandlerSequence;
import cz.muni.fi.xtovarn.heimdall.pipeline.Pipeline;
import cz.muni.fi.xtovarn.heimdall.pipeline.handler.*;
import org.picocontainer.Startable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketServer implements Startable {

	private final ExecutorService childExecutor = Executors.newCachedThreadPool();
	private final HandlerSequence sequence;

	public SocketServer(EventStore eventStore, Dispatcher dispatcher) throws FileNotFoundException, DatabaseException {
		sequence = new HandlerSequence(
				new ParseJSON(),
				new SetDetectionTime(),
				new Store(eventStore),
				new DetermineRecipient(),
				new SubmitToDispatcher(dispatcher));
	}

	@Override
	public void start() {
		try {
			read("events.json");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		childExecutor.shutdown();
		try {
			childExecutor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void read(String filename) throws IOException, InterruptedException {
		BufferedReader in = new BufferedReader(new FileReader(filename));

		String json;

		while ((json = in.readLine()) != null) {
			childExecutor.submit(new Pipeline(json, sequence));
			Thread.sleep(7000);
		}

		in.close();
		this.stop();
	}
}
