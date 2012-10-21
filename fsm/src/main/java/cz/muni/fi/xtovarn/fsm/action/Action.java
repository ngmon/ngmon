package cz.muni.fi.xtovarn.fsm.action;

public interface Action<T3 extends ActionContext> {
    public boolean perform(T3 context);
}
