package cz.muni.fi.xtovarn.heimdall.runnable;

import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;
import org.picocontainer.Startable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketServer implements Startable {

	private final ExecutorService childExecutor = Executors.newCachedThreadPool();
	private final PipelineFactory pipelineFactory;

	public SocketServer(PipelineFactory pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
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
			childExecutor.submit(pipelineFactory.getPipeline(json));
		}

		in.close();
		this.stop();
	}
}
