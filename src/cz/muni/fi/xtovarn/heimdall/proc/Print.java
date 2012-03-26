package cz.muni.fi.xtovarn.heimdall.proc;

import cz.muni.fi.xtovarn.heimdall.entity.Event;

public class Print extends AbstractAction {

	public Print(AbstractAction successor) {
		super(successor);
	}

	public Print() {
	}

	@Override
	protected void processEvent(Event event) {
		System.out.println("event recieved::>" + event);
	}
}
