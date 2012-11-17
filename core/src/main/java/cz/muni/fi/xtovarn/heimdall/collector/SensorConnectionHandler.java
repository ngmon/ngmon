package cz.muni.fi.xtovarn.heimdall.collector;

import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SensorConnectionHandler implements Runnable {

//	private final ExecutorService childExecutor = Executors.newFixedThreadPool(10);
	private final Socket socket;
	private final PipelineFactory pipelineFactory;

	public SensorConnectionHandler(Socket socket, PipelineFactory pipelineFactory) {
		this.socket = socket;
		this.pipelineFactory = pipelineFactory;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String string;

			while ((string = in.readLine()) != null) {
				pipeline(string);
			}

			in.close();
			socket.close();

		} catch (IOException e) {
		}
	}

	public void pipeline(String json) {
//		childExecutor.submit(pipelineFactory.getPipeline(json));
		pipelineFactory.getPipeline(json).run();
	}

/*	public void stop() {
		System.out.println("Closing " + this.getClass() + "...");

		childExecutor.shutdown();
		try {
			childExecutor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/
}
