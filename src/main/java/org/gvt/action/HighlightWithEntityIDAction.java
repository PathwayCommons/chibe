package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.EntityAssociated;

import java.util.Collection;

/**
 * Highlights object in the given view if their associated entity's id is in the given set.
 *
 * @author Ozgun Babur
 */
public class HighlightWithEntityIDAction extends Action
{
	ChisioMain main;
	BioPAXGraph pgraph;
	Collection<String> ids;

	public HighlightWithEntityIDAction(ChisioMain main, BioPAXGraph pgraph, Collection<String> ids)
	{
		this.main = main;
		this.pgraph = pgraph;
		this.ids = ids;
	}

	public void run()
	{
		for (Object o : pgraph.getNodes())
		{
			if (o instanceof EntityAssociated)
			{
				EntityAssociated ea = (EntityAssociated) o;

				if (ids.contains(ea.getEntity().getID()))
				ea.setHighlight(true);
			}
		}
	}
}
