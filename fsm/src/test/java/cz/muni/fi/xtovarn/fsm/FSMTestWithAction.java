package cz.muni.fi.xtovarn.fsm;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

// TODO Rewrite test
public class FSMTestWithAction {

    enum MyStateType {STARTED, RUNNING, ENDED}

    private StringBasedFSM<MyStateType> fsm;

    public static int actionControl = 0;

    @Before
    public void setUp() throws Exception {
        fsm = new StringBasedFSM<MyStateType>(MyStateType.STARTED, MyStateType.ENDED, MyStateType.class);

        fsm.addTransition(MyStateType.STARTED, "symbol1", MyStateType.RUNNING, new IntegerAction());
        fsm.addTransition(MyStateType.RUNNING, "symbol2", MyStateType.ENDED, new IntegerAction());
    }

    @Test
    public void testTransition1() throws Exception {
        fsm.readSymbol("symbol1", 67);

        assertEquals("Transition was not performed", MyStateType.RUNNING, fsm.getCurrentState());
    }

    @Test
    public void testFSMEnded() throws Exception {
        fsm.readSymbol("symbol1", 67);
        fsm.readSymbol("symbol2", 67);

        assertEquals("Transition was not performed", MyStateType.ENDED, fsm.getCurrentState());
        assertEquals("FSM is in wrong state, should be ended", true, fsm.isEnded());
    }

    @Test
    public void testTransitionAction() throws Exception {
        fsm.readSymbol("symbol1", actionControl);

        assertEquals("Action was not performed", 67, actionControl);
    }
}
