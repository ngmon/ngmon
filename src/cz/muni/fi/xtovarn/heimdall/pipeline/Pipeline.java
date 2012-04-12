package cz.muni.fi.xtovarn.heimdall.pipeline;

import cz.muni.fi.xtovarn.heimdall.pipeline.handlers.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pipeline {

	private final List<Handler> handlers;

	public Pipeline(Handler... handlers) {
		this.handlers = new ArrayList<Handler>(Arrays.asList(handlers));
	}

	public Object execute(Object o) {
		for (Handler handler : handlers) {
			o = handler.handle(o);
		}

		return o;
	}
}
