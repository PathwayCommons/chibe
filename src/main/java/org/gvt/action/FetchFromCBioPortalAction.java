package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.gvt.ChisioMain;
import org.gvt.gui.FetchFromCBioPortalDialog;
import org.patika.mada.gui.FetchFromGEODialog;

public class FetchFromCBioPortalAction extends Action {
    ChisioMain main;

   	public FetchFromCBioPortalAction (ChisioMain main) {
   		super("Fetch from cBio Portal...");
   		this.main = main;
   	}

    public void run(){
        FetchFromCBioPortalDialog dialog = new FetchFromCBioPortalDialog(main);
        dialog.open();
    }

}
