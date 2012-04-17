package cz.muni.fi.xtovarn.heimdall.dispatcher;

import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import org.picocontainer.Startable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dispatcher implements Startable {
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final SecureChannelGroup secureChannelGroup;

	public Dispatcher(SecureChannelGroup secureChannelGroup) {
		this.secureChannelGroup = secureChannelGroup;
	}

	@Override
	public void start() {

	}

	public boolean submit(Subscription subscription) {
		for (String recipient : subscription.getRecipients()) {
			executor.submit(new Dispatch(secureChannelGroup, recipient, subscription.getEvent()));
		}

		return true;
	}

	@Override
	public void stop() {
		executor.shutdown();
	}
}
