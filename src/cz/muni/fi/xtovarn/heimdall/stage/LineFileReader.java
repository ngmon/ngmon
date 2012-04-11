package cz.muni.fi.xtovarn.heimdall.stage;

import cz.muni.fi.xtovarn.heimdall.pipeline.Pipeline;

import java.io.*;

public class LineFileReader implements Runnable {

	private final Pipeline pipeline;

	public LineFileReader(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	@Override
	public void run() {
		try {
			read("events.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void read(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));

		String s;

		while ((s = in.readLine()) != null) {
			processWithPipeline(s);
		}

		in.close();
	}

	private void processWithPipeline(String json) {
		pipeline.execute(json);
	}
}
