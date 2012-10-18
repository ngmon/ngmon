package cz.muni.fi.xtovarn.fsm;

public class StringBasedFSM<T1 extends Enum<T1>> extends AbstractFiniteStateMachineWithActions<T1, String> {
    public StringBasedFSM(T1 startState, T1 endState, Class<T1> statesEnumClass) {
        super(startState, endState, statesEnumClass);
    }

    protected synchronized void readSymbol(String symbol, int number) {
        IntegerAction action = (IntegerAction) super.getNextAction(symbol);

        if (action.perform(number)) {
            action.setSuccess();
        }

        super.readSymbol(symbol);
    }
}

