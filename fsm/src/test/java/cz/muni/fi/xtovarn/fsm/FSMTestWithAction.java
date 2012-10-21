package cz.muni.fi.xtovarn.fsm;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// TODO Rewrite test
public class FSMTestWithAction {


	private StringBasedFSM fsm;

	public static int actionControl = 0;


	@Before
	public void setUp() throws Exception {
		fsm = new StringBasedFSM();
	}

	@Test
	public void testTransition1() throws Exception {
		assertTrue(fsm.readSymbol("symbol1", new IntegerContext(67)));
		assertEquals("Transition was not performed", MyStateType.RUNNING, fsm.getCurrentState());
	}

	@Test
	public void testFSMEnded() throws Exception {
		assertTrue(fsm.readSymbol("symbol1", new IntegerContext(67)));
		assertTrue(fsm.readSymbol("symbol2", new IntegerContext(67)));

		assertEquals("Transition was not performed", MyStateType.ENDED, fsm.getCurrentState());
		assertEquals("FSM is in wrong state, should be ended", true, fsm.isEnded());
	}

	@Test
	public void testTransitionAction() throws Exception {
		assertTrue(fsm.readSymbol("symbol1", new IntegerContext(actionControl)));

		assertEquals("Action was not performed", 67, actionControl);
	}
}
