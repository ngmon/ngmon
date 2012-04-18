package cz.muni.fi.xtovarn.heimdalld.localserver;

import cz.muni.fi.xtovarn.heimdalld.pipeline.PipelineFactory;
import org.picocontainer.Startable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Resender implements Startable {

	private final ExecutorService childExecutor = Executors.newFixedThreadPool(10);
	private final PipelineFactory pipelineFactory;

	public Resender(PipelineFactory pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}

	@Override
	public void start() {

	}

	public void pipeline(String json) {
		childExecutor.submit(pipelineFactory.getPipeline(json));
	}

	public void stop() {
		childExecutor.shutdown();
		try {
			childExecutor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
