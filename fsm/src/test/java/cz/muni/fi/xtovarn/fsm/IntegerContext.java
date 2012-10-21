package cz.muni.fi.xtovarn.fsm;

import cz.muni.fi.xtovarn.fsm.action.ActionContext;

public class IntegerContext implements ActionContext {

	private int integer;

	public IntegerContext(int integer) {
		this.integer = integer;
	}

	public int getInteger() {
		return integer;
	}

	public void setInteger(int integer) {
		this.integer = integer;
	}
}
