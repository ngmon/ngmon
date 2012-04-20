package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Subscription;

public class SubmitToDispatcher implements Handler {
	private final Dispatcher dispatcher;

	public SubmitToDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public Object handle(Object o) {
		dispatcher.submit((Subscription) o);
		return null;
	}
}
