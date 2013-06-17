package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;

/**
 * Action for refreshing a graph with showing compartments.
 */
public class ShowCompartmentsAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public ShowCompartmentsAction(ChisioMain main)
	{
		super("Show Compartments");
		this.setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		UpdatePathwayAction ac = new UpdatePathwayAction(main, false);
		ac.setShowCompartments(true);
		ac.run();
	}
}