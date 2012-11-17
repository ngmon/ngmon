package cz.muni.fi.xtovarn.fsm;

import cz.muni.fi.xtovarn.fsm.action.Action;
import cz.muni.fi.xtovarn.heimdall.commons.util.Pair;

import java.util.*;

/**
 *
 * @param <T1> State space type
 * @param <T2> Symbol
 * @param <T3> Action Context
 */
public abstract class AbstractFiniteStateMachine<T1 extends Enum<T1>, T2, T3> {
	final Class<T1> statesEnumClass; // Generics Workaround

	private Map<T1, Map<T2, Pair<Action<T3>, T1>>> stateTransitionFunction;

	private final T1 startState;
	private final List<T1> endStates;
	private T1 currentState;

	private Queue<T1> history;

	public AbstractFiniteStateMachine(T1 startState, T1[] endStates, Class<T1> statesEnumClass) { // Generics Workaround (Class<T1> statesEnumClass)
		this.statesEnumClass = statesEnumClass;

		this.stateTransitionFunction = new EnumMap<>(statesEnumClass);
		this.startState = startState;
		this.endStates = Arrays.asList(endStates);
		this.currentState = startState;
		this.history = new LinkedList<>();
	}

	public void addTransition(T1 sourceState, T2 symbol, T1 targetState, Action<T3> action) {
		if (endStates.contains(sourceState)) {
			throw new IllegalArgumentException("You cannot create transition from an end state");
		}

		Map<T2, Pair<Action<T3>, T1>> transition = stateTransitionFunction.get(sourceState);
		Pair<Action<T3>, T1> targetPair = new Pair<>(action, targetState);

		if (transition == null) {
			transition = new HashMap<>();
			transition.put(symbol, targetPair);
			this.stateTransitionFunction.put(sourceState, transition);
		} else {
			transition.put(symbol, targetPair);
		}

	}

	synchronized public boolean readSymbol(T2 symbol, T3 actionContext) {
		T1 nextState = getNextState(symbol);

		if (nextState == null) {
			throw new IllegalArgumentException("No such transition under given symbol!");
		}

		Action<T3> actionToPerform = getNextAction(symbol);

		if (actionToPerform != null) {

			if (actionContext == null) {
				throw new IllegalStateException("There is an Action associated with this transition, please provide valid ActionContext");
			}

			if (!actionToPerform.perform(actionContext)) {
				return false;
			}

		} else if (actionContext != null) {
			throw new IllegalStateException("You have provided an ActionContext for transition without Action");

		}

		this.history.add(currentState);
		this.currentState = nextState;

		return true;
	}

	protected synchronized boolean readSymbol(T2 symbol) {
		return this.readSymbol(symbol, null);
	}

	synchronized protected void rollback() {
		this.currentState = history.remove();
	}

	public Action<T3> getNextAction(T2 symbol) {
		return this.getNextPair(symbol).getFirst();
	}

	public T1 getNextState(T2 symbol) {
		return this.getNextPair(symbol).getSecond();
	}

	private Pair<Action<T3>, T1> getNextPair(T2 symbol) {

		return this.stateTransitionFunction.get(currentState).get(symbol);
	}

	synchronized public boolean isEnded() {
		return this.endStates.contains(this.currentState);
	}

	public T1 getCurrentState() {
		return this.currentState;
	}

	public T1 getStartState() {
		return this.startState;
	}

	public List<T1> getEndStates() {
		return this.endStates;
	}
}
