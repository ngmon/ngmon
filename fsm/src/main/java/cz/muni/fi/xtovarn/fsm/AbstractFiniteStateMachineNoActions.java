package cz.muni.fi.xtovarn.fsm;

import java.util.*;

public abstract class AbstractFiniteStateMachineNoActions<T1 extends Enum<T1>, T2> {
	private final Class<T1> statesEnumClass; // Generics Workaround

	private Map<T1, Map<T2, T1>> stateTransitionFunction;

	private final T1 startState;
	private final Set<T1> endStates;
	private T1 currentState;

	private final boolean DEBUG;

	public AbstractFiniteStateMachineNoActions(T1 startState, T1[] endStates, Class<T1> statesEnumClass, boolean debug) {
		this.statesEnumClass = statesEnumClass;

		this.stateTransitionFunction = new EnumMap<>(statesEnumClass);
		this.startState = startState;
		this.endStates = new HashSet<>(Arrays.asList(endStates));
		this.currentState = startState;
		this.DEBUG = debug;
	}

	public AbstractFiniteStateMachineNoActions(T1 startState, T1[] endStates, Class<T1> statesEnumClass) {
		this(startState, endStates, statesEnumClass, false);
	}

	public void addTransition(T1 sourceState, T2 symbol, T1 targetState) {
		if (endStates.contains(sourceState)) {
			throw new IllegalArgumentException("You cannot create transition from an end state");
		}

		Map<T2, T1> transitions = stateTransitionFunction.get(sourceState);

		if (transitions == null) {
			transitions = new HashMap<>();
			transitions.put(symbol, targetState);
			stateTransitionFunction.put(sourceState, transitions);
		} else {
			transitions.put(symbol, targetState);
		}

	}

	// is transition allowed?
	public synchronized boolean isAllowed(T2 symbol) {
		return getNextState(symbol) != null;

	}

	public synchronized void readSymbol(T2 symbol) {
		T1 nextState = getNextState(symbol);

		if (nextState == null) {
			throw new IllegalArgumentException(String.format(
					"No such transition under given symbol(%s) from state %s!", symbol.toString(), this.currentState));
		}

		if (DEBUG) {
			System.out.println(String.format("[%s] %s: %s -> %s", this.toString(), symbol, currentState, nextState));
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

	public synchronized Set<T1> getEndStates() {
		return endStates;
	}

}
