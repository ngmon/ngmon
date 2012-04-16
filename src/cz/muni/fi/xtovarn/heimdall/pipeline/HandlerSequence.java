package cz.muni.fi.xtovarn.heimdall.pipeline;

import cz.muni.fi.xtovarn.heimdall.pipeline.handler.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandlerSequence {

	private final List<Handler> handlers;

	public HandlerSequence(Handler... handlers) {
		this.handlers = new ArrayList<Handler>(Arrays.asList(handlers));
	}

	public List<Handler> getHandlers() {
		return handlers;
	}
}
