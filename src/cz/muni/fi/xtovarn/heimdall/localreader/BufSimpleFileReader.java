package cz.muni.fi.xtovarn.heimdall.localreader;

import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;
import org.codehaus.jackson.map.util.ISO8601Utils;
import org.picocontainer.Startable;

import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BufSimpleFileReader implements Startable {

	private final ExecutorService childExecutor = Executors.newFixedThreadPool(2);
	private final PipelineFactory pipelineFactory;

	public BufSimpleFileReader(PipelineFactory pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}

	@Override
	public void start() {
		Scanner console = new Scanner(System.in);
		System.out.print("Press enter.");
		String guess = console.next();

		System.out.println(guess);

		int i = 10000;
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
