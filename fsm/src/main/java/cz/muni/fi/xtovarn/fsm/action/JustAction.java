package cz.muni.fi.xtovarn.fsm.action;

public interface JustAction<T1> extends Action {

    public boolean perform(T1 entity);
}
