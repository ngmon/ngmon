package cz.muni.fi.xtovarn.fsm.action;

public interface Action<T1> {
    public boolean perform(T1 context);
}
