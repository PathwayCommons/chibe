package org.gvt.action;

import org.biopax.paxtools.model.BioPAXElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.PathwayHolder;

import java.util.Set;

/**
 * This class is an abstract class for Local Query Actions.
 *
 * @author Shatlyk Ashyralyev
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public abstract class AbstractLocalQueryAction extends Action
{
	ChisioMain main;
	
	/**
	 * Constructor
	 */
	public AbstractLocalQueryAction(ChisioMain main, String arg)
	{
		super(arg);
		this.main = main;
	}

	/**
	 * Views result of Query in current view or new view
	 * Highlights resultant objects
	 */
	protected void viewAndHighlightResult(Set<BioPAXElement> result, boolean isCurrentView,
		String pathwayName)
	{
		//if no result can be found, open dialog to warn.
		if (result.isEmpty())
		{
			MessageDialog.openWarning(main.getShell(), "No result!",
				"Query result is empty with the specified parameters.");

			return;
		}

		Set<String> resultIDs = BioPAXUtil.collectIDs(result);

		if (!isCurrentView)
		{
			PathwayHolder ph = BioPAXUtil.createPathway(
				main.getBioPAXModel(), pathwayName, resultIDs);

			new OpenPathwaysAction(main, ph.getName()).run();
		}

		main.highlightIDs(resultIDs);
	}
}
