package cz.muni.fi.xtovarn.heimdalld.pipeline;

import cz.muni.fi.xtovarn.heimdalld.pipeline.handler.Handler;

import java.util.ArrayList;
import java.util.List;

public class Pipeline implements Runnable {

	private List<Handler> handlers = new ArrayList<Handler>(1);
	private Object o;

	public Pipeline(Object o) {
		this.o = o;
	}

	@Override
	public void run() {
		for (Handler handler : handlers) {
			o = handler.handle(o);
		}
	}

	public void addHandler(Handler handler) {
		handlers.add(handler);
	}

}
