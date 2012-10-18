package cz.muni.fi.xtovarn.fsm;

import java.util.*;

public abstract class AbstractFiniteStateMachine<T1 extends Enum<T1>, T2> {
    final Class<T1> statesEnumClass; // Generics Workaround

    private Map<T1, Map<T2, T1>> stateTransitionFunction;

    private final T1 startState;
    private final Set<T1> endStates;
    private T1 currentState;

    public AbstractFiniteStateMachine(T1 startState, T1[] endStates, Class<T1> statesEnumClass) {
        this.statesEnumClass = statesEnumClass;

        this.stateTransitionFunction = new EnumMap<T1, Map<T2, T1>>(statesEnumClass);
        this.startState = startState;
        this.endStates = new HashSet<T1>(Arrays.asList(endStates));
        this.currentState = startState;
    }

    public void addTransition(T1 sourceState, T2 symbol, T1 targetState) {
        if (endStates.contains(sourceState)) {
            throw new IllegalArgumentException("You cannot create transition from an end state");
        }

        Map<T2, T1> transition = stateTransitionFunction.get(sourceState);

        if (transition == null) {
            transition = new HashMap<T2, T1>();
            transition.put(symbol, targetState);
            stateTransitionFunction.put(sourceState, transition);
        } else {
            transition.put(symbol, targetState);
        }

    }

    public synchronized void transition(T2 symbol) {
        T1 nextState = getNextState(symbol);

        if (nextState == null) {
            throw new IllegalArgumentException("No such transition under given symbol!");
        }
        this.currentState = nextState;

    }

    public synchronized T1 getNextState(T2 symbol) {
        return this.stateTransitionFunction.get(this.currentState).get(symbol);
    }

    public synchronized boolean isEnded() {
        return this.endStates.contains(this.currentState);
    }

    public synchronized T1 getCurrentState() {
        return this.currentState;
    }

    public synchronized T1 getStartState() {
        return this.startState;
    }

}
