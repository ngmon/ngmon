package cz.muni.fi.xtovarn.fsm;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

// TODO Rewrite test
public class FSMTest {

    enum MyStateType {STARTED, RUNNING, FAILED, ENDED}

    private class StringBasedFSM<T1 extends Enum<T1>> extends AbstractFiniteStateMachine<T1, String> {

        public StringBasedFSM(T1 startState, T1[] endStates, Class<T1> statesEnumClass) {
            super(startState, endStates, statesEnumClass);
        }
    }

    private StringBasedFSM<MyStateType> fsm;

    @Before
    public void setUp() throws Exception {
        fsm = new StringBasedFSM<MyStateType>(MyStateType.STARTED, new MyStateType[]{MyStateType.ENDED, MyStateType.FAILED}, MyStateType.class);

        fsm.addTransition(MyStateType.STARTED, "symbol1", MyStateType.RUNNING);
        fsm.addTransition(MyStateType.STARTED, null, MyStateType.FAILED);
        fsm.addTransition(MyStateType.STARTED, "a", MyStateType.FAILED);
        fsm.addTransition(MyStateType.RUNNING, "symbol2", MyStateType.ENDED);
    }

    @Test
    public void testTransition1() throws Exception {
        fsm.transition("symbol1");

        assertEquals("Transition was not performed", MyStateType.RUNNING, fsm.getCurrentState());
    }

    @Test
    public void testFSMEnded1() throws Exception {
        fsm.transition("symbol1");
        fsm.transition("symbol2");

        assertEquals("Transition was not performed", MyStateType.ENDED, fsm.getCurrentState());
        assertEquals("FSM is in wrong state, should be ended", true, fsm.isEnded());
    }

    @Test
    public void testFSMEnded2() throws Exception {
        fsm.transition("a");

        assertEquals("Transition was not performed", MyStateType.FAILED, fsm.getCurrentState());
        assertEquals("FSM is in wrong state, should be ended", true, fsm.isEnded());
    }

    @Test
    public void testNullTransition() throws Exception {
        fsm.transition(null);
        assertEquals("Transition was not performed", MyStateType.FAILED, fsm.getCurrentState());
    }
}
