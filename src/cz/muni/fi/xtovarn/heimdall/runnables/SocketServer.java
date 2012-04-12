package cz.muni.fi.xtovarn.heimdall.runnables;

import cz.muni.fi.xtovarn.heimdall.pipeline.Pipeline;

import java.io.*;

public class SocketServer implements Runnable {

	private final Pipeline pipeline;

	public SocketServer(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	@Override
	public void run() {
		try {
			read("events.json");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void read(String filename) throws IOException, InterruptedException {
		BufferedReader in = new BufferedReader(new FileReader(filename));

		String s;

		while ((s = in.readLine()) != null) {
			processWithPipeline(s);
			Thread.sleep(3000);
		}

		in.close();
	}

	private void processWithPipeline(String json) {
		pipeline.execute(json);
	}
}
