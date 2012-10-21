package cz.muni.fi.xtovarn.fsm;

import cz.muni.fi.xtovarn.fsm.action.Action;
import cz.muni.fi.xtovarn.fsm.action.ActionContext;
import cz.muni.fi.xtovarn.heimdall.commons.util.Pair;

import java.util.*;

public abstract class AbstractFiniteStateMachine<T_State extends Enum<T_State>, T_Symbol, T_ActionContext extends ActionContext> {
	final Class<T_State> statesEnumClass; // Generics Workaround

	private Map<T_State, Map<T_Symbol, Pair<Action<T_ActionContext>, T_State>>> stateTransitionFunction;

	private final T_State startState;
	private final List<T_State> endStates;
	private T_State currentState;

	private Queue<T_State> history;

	public AbstractFiniteStateMachine(T_State startState, T_State[] endStates, Class<T_State> statesEnumClass) { // Generics Workaround (Class<T_State> statesEnumClass)
		this.statesEnumClass = statesEnumClass;

		this.stateTransitionFunction = new EnumMap<T_State, Map<T_Symbol, Pair<Action<T_ActionContext>, T_State>>>(statesEnumClass);
		this.startState = startState;
		this.endStates = Arrays.asList(endStates);
		this.currentState = startState;
		this.history = new LinkedList<T_State>();
	}

	public void addTransition(T_State sourceState, T_Symbol symbol, T_State targetState, Action<T_ActionContext> action) {
		if (endStates.contains(sourceState)) {
			throw new IllegalArgumentException("You cannot create transition from an end state");
		}

		Map<T_Symbol, Pair<Action<T_ActionContext>, T_State>> transition = stateTransitionFunction.get(sourceState);
		Pair<Action<T_ActionContext>, T_State> targetPair = new Pair<Action<T_ActionContext>, T_State>(action, targetState);

		if (transition == null) {
			transition = new HashMap<T_Symbol, Pair<Action<T_ActionContext>, T_State>>();
			transition.put(symbol, targetPair);
			this.stateTransitionFunction.put(sourceState, transition);
		} else {
			transition.put(symbol, targetPair);
		}

	}

	synchronized public boolean readSymbol(T_Symbol symbol, T_ActionContext actionContext) {
		T_State nextState = getNextState(symbol);

		if (nextState == null) {
			throw new IllegalArgumentException("No such transition under given symbol!");
		}

		Action<T_ActionContext> actionToPerform = getNextAction(symbol);

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

	protected synchronized boolean readSymbol(T_Symbol symbol) {
		return this.readSymbol(symbol, null);
	}

	synchronized protected void rollback() {
		this.currentState = history.remove();
	}

	public Action<T_ActionContext> getNextAction(T_Symbol symbol) {
		return this.getNextPair(symbol).getFirst();
	}

	public T_State getNextState(T_Symbol symbol) {
		return this.getNextPair(symbol).getSecond();
	}

	private Pair<Action<T_ActionContext>, T_State> getNextPair(T_Symbol symbol) {

		return this.stateTransitionFunction.get(currentState).get(symbol);
	}

	synchronized public boolean isEnded() {
		return this.endStates.contains(this.currentState);
	}

	public T_State getCurrentState() {
		return this.currentState;
	}

	public T_State getStartState() {
		return this.startState;
	}

	public List<T_State> getEndStates() {
		return this.endStates;
	}
}
