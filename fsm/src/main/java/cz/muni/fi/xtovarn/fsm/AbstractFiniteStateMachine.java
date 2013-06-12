package cz.muni.fi.xtovarn.fsm;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.muni.fi.xtovarn.fsm.action.Action;
import cz.muni.fi.xtovarn.heimdall.commons.util.Pair;

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

	private final boolean DEBUG;
	
	private static Logger logger = LogManager.getLogger(AbstractFiniteStateMachine.class);

	public AbstractFiniteStateMachine(T1 startState, T1[] endStates, Class<T1> statesEnumClass, boolean debug) { // Generics Workaround (Class<T1> statesEnumClass)
		this.statesEnumClass = statesEnumClass;

		this.stateTransitionFunction = new EnumMap<>(statesEnumClass);
		this.startState = startState;
		this.endStates = Arrays.asList(endStates);
		this.currentState = startState;
		this.history = new LinkedList<>();
		this.DEBUG = debug;
	}

	public AbstractFiniteStateMachine(T1 startState, T1[] endStates, Class<T1> statesEnumClass) { // Generics Workaround (Class<T1> statesEnumClass)
		this(startState, endStates, statesEnumClass, false);
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

		if (DEBUG) {
			logger.debug(String.format("[%s] %s: %s -> %s", this.toString(), symbol, currentState, nextState));
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
		Pair<Action<T3>, T1> nextPair = this.getNextPair(symbol);
		if (nextPair == null)
			return null;
		return nextPair.getFirst();
	}

	public T1 getNextState(T2 symbol) {
		Pair<Action<T3>, T1> nextPair = this.getNextPair(symbol);
		if (nextPair == null)
			return null;
		return nextPair.getSecond();
	}

	private Pair<Action<T3>, T1> getNextPair(T2 symbol) {

		Map<T2, Pair<Action<T3>, T1>> map = this.stateTransitionFunction.get(currentState);
		if (map == null)
			return null;
		return map.get(symbol);
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
