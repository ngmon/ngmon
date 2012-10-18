package cz.muni.fi.xtovarn.fsm;

import cz.muni.fi.xtovarn.fsm.action.AbstractAction;

public class IntegerAction extends AbstractAction {

    public boolean perform(int number) {
        FSMTestWithAction.actionControl=number;

        return true;
    }

}
