package cz.muni.fi.xtovarn.fsm;

import cz.muni.fi.xtovarn.fsm.action.Action;

public class StringBasedFSM extends AbstractFiniteStateMachine<MyStateType, String, IntegerContext> {

	public StringBasedFSM() {
		super(MyStateType.STARTED, new MyStateType[]{MyStateType.ENDED}, MyStateType.class);

		addTransition(MyStateType.STARTED, "symbol1", MyStateType.RUNNING, new Action<IntegerContext>() {
			@Override
			public boolean perform(IntegerContext context) {
				FSMTestWithAction.actionControl = context.getInteger();

				return true;
			}
		});

		addTransition(MyStateType.RUNNING, "symbol2", MyStateType.ENDED, new Action<IntegerContext>() {
			@Override
			public boolean perform(IntegerContext context) {
				FSMTestWithAction.actionControl = context.getInteger();

				return true;
			}
		});
	}
}

