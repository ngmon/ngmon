package cz.muni.fi.xtovarn.heimdalld.pipeline.handler;

import cz.muni.fi.xtovarn.heimdalld.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdalld.dispatcher.Subscription;

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
