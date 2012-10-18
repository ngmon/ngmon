package cz.muni.fi.xtovarn.fsm.action;

public abstract class AbstractAction implements Action {
    private boolean success = false;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess() {
        this.success = true;
    }
}
