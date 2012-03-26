package cz.muni.fi.xtovarn.heimdall.proc;

import cz.muni.fi.xtovarn.heimdall.entity.Event;

public abstract class AbstractAction {
	private AbstractAction successor;

	protected AbstractAction(AbstractAction successor) {
		this.successor = successor;
	}

	protected AbstractAction() {
	}

	public void process(Event event) {
		this.processEvent(event);

		if (successor != null) {
			successor.process(event);
		}
	}

	protected abstract void processEvent(Event event);
}
