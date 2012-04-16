package cz.muni.fi.xtovarn.heimdall.runnable;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.HandlerSequence;
import cz.muni.fi.xtovarn.heimdall.pipeline.Pipeline;
import cz.muni.fi.xtovarn.heimdall.pipeline.handler.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketServer implements Runnable {

	private final ExecutorService parentExecutor;
	private final ExecutorService childExecutor;
	private final EventStore eventStore;
	private final HandlerSequence sequence;

	public SocketServer(ExecutorService parentExecutor, ExecutorService childExecutor) throws FileNotFoundException, DatabaseException {
		this.parentExecutor = parentExecutor;
		this.childExecutor = childExecutor;
		eventStore = EventStoreFactory.getSingleInstance();
		sequence = new HandlerSequence(
				new ParseJSON(),
				new SetDetectionTime(),
				new Store(eventStore),
				new DetermineRecipient(),
				new SubmitToSend(Executors.newSingleThreadExecutor())
		);
	}

	public void start() {
		parentExecutor.submit(this);
	}

	@Override
	public void run() {
		try {
			read("events5.json");
		} catch (IOException e) {
			e.printStackTrace();
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
		this.shutdown();
	}


	public void shutdown() {
		childExecutor.shutdown();
		try {
			childExecutor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		parentExecutor.shutdown();
	}
}
