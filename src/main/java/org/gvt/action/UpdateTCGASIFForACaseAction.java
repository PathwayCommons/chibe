package org.gvt.action;

import org.cbio.causality.model.Alteration;
import org.cbio.causality.model.AlterationPack;
import org.eclipse.swt.custom.CTabItem;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * Action for highlighting the nodes using a given name.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class UpdateTCGASIFForACaseAction extends TCGASIFAction
{
	/**
	 * Constructor
	 */
	public UpdateTCGASIFForACaseAction(ChisioMain main)
	{
		super("Update Graph for a Case ...", main);
	}

	public void run()
	{
		if (!okToRun(main)) return;

		List<String> cases = Arrays.asList(
			ChisioMain.cBioPortalAccessor.getCurrentCaseList().getCases());

		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 200, "TCGA Cases",
			"Please select a TCGA case", cases, null, false, true, null);

		dialog.setDoSort(false);
		Object o = dialog.open();

		if (o == null) return;

		String aCase = o.toString();

		LoadTCGASpecificSIFAction action = new LoadTCGASpecificSIFAction(main);
		action.setCaseID(aCase);
		action.setStudy(getCurrentStudy());
		action.setNewView(false);
		action.run();
	}
}