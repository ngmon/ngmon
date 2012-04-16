package cz.muni.fi.xtovarn.heimdall.pipeline;

import cz.muni.fi.xtovarn.heimdall.pipeline.handler.Handler;

public class Pipeline implements Runnable {

	private final HandlerSequence sequence;
	private Object o;

	public Pipeline(Object o, HandlerSequence sequence) {

		this.o = o;
		this.sequence = sequence;
	}

	@Override
	public void run() {
		for (Handler handler : sequence.getHandlers()) {
			o = handler.handle(o);
		}
	}
}
