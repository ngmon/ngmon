package cz.muni.fi.xtovarn.heimdalld.localserver;

import cz.muni.fi.xtovarn.heimdalld.pipeline.PipelineFactory;
import org.codehaus.jackson.map.util.ISO8601Utils;
import org.picocontainer.Startable;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BufSimpleFileReader implements Startable {

	private final ExecutorService childExecutor = Executors.newFixedThreadPool(10);
	private final PipelineFactory pipelineFactory;

	public BufSimpleFileReader(PipelineFactory pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}

	@Override
	public void start() {
		System.out.println("Press enter to continue...");
		try {System.in.read();} catch (IOException e){}
		System.out.println("Reading!");

		int i = 1000;
		while (i > 1) {
			i--;
			String json = "{\"Event\":{\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"proc_cron NAme\",\"processId\":\"id005\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":null,\"schemaVersion\":null,\"value\":4648,\"value2\":\"aax4x46aeEF\"}}}";
			childExecutor.submit(pipelineFactory.getPipeline(json));
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
}
