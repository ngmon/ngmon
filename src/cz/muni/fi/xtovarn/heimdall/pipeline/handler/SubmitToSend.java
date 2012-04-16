package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import cz.muni.fi.xtovarn.heimdall.pipeline.Send;
import cz.muni.fi.xtovarn.heimdall.pipeline.Subscription;

import java.util.concurrent.ExecutorService;

public class SubmitToSend implements Handler {
	private final ExecutorService executor;

	public SubmitToSend(ExecutorService executor) {
		this.executor = executor;
	}

	@Override
	public Object handle(Object o) {
		Subscription s = (Subscription) o;

		for (String recipient : s.getRecipients()) {
			executor.submit(new Send(recipient, s.getEvent()));
		}

		return null;
	}
}
