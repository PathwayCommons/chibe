package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.gui.LegendDialog;

/**
 * @author Merve Cakir
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class LegendAction extends Action
{
    ChisioMain main;

    public LegendAction(ChisioMain main)
    {
        super("Legends");

        this.main = main;
    }

    public void run()
    {
        new LegendDialog(main.getShell()).open();
    }
}
