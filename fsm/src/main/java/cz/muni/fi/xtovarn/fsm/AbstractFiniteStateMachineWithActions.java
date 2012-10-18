package cz.muni.fi.xtovarn.fsm;

import cz.muni.fi.xtovarn.fsm.action.Action;

import java.util.*;

public abstract class AbstractFiniteStateMachineWithActions<T1 extends Enum<T1>, T2> {
    final Class<T1> statesEnumClass; // Generics Workaround

    private Map<T1, Map<T2, Pair<Action, T1>>> stateTransitionFunction;

    private final T1 startState;
    private final T1 endState;
    private T1 currentState;

    private Queue<T1> history;

    public AbstractFiniteStateMachineWithActions(T1 startState, T1 endState, Class<T1> statesEnumClass) {
        this.statesEnumClass = statesEnumClass;

        this.stateTransitionFunction = new EnumMap<T1, Map<T2, Pair<Action, T1>>>(statesEnumClass);
        this.startState = startState;
        this.endState = endState;
        this.currentState = startState;
        this.history = new LinkedList<T1>();
    }

    public void addTransition(T1 sourceState, T2 symbol, T1 targetState, Action action) {
        if (sourceState.equals(endState)) {
            throw new IllegalArgumentException("You cannot create transition from an end state");
        }

        Map<T2, Pair<Action, T1>> transition = stateTransitionFunction.get(sourceState);
        Pair<Action, T1>  targetPair = new Pair<Action, T1>(action, targetState);

        if (transition == null) {
            transition = new HashMap<T2, Pair<Action, T1>>();
            transition.put(symbol, targetPair);
            stateTransitionFunction.put(sourceState, transition);
        } else {
            transition.put(symbol, targetPair);
        }

    }

    synchronized protected boolean transition(T2 symbol) {
        T1 nextState = getNextState(symbol);

        if (nextState == null) {
            throw new IllegalArgumentException("No such transition under given symbol!");
        } else {
            if (getNextAction(symbol).isSuccess()) {
                history.add(currentState);
                currentState = nextState;

                return true;
            }

            return false;

        }
    }

    protected synchronized void readSymbol(T2 symbol) {
        transition(symbol);
    }

    synchronized protected void rollback() {
        currentState = history.remove();
    }

    public Action getNextAction(T2 symbol) {
        return getNextPair(symbol).getFirst();
    }

    public T1 getNextState(T2 symbol) {
        return getNextPair(symbol).getSecond();
    }

    private Pair<Action, T1> getNextPair(T2 symbol) {
        Pair<Action, T1> pair = stateTransitionFunction.get(currentState).get(symbol);

        return pair;
    }

    synchronized public boolean isEnded() {
        return currentState.equals(endState);
    }

    public T1 getCurrentState() {
        return currentState;
    }

    public T1 getStartState() {
        return startState;
    }

    public T1 getEndState() {
        return endState;
    }
}
