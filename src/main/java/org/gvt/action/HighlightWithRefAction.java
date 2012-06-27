package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.patika.mada.graph.Node;
import org.patika.mada.util.XRef;

import java.util.Collection;

/**
 * Highlights object in the given view if they contain the given xref.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class HighlightWithRefAction extends Action
{
	ChisioMain main;
	BioPAXGraph pgraph;
	Collection<XRef> refs;

	public HighlightWithRefAction(ChisioMain main, BioPAXGraph pgraph, Collection<XRef> refs)
	{
		this.main = main;
		this.pgraph = pgraph;
		this.refs = refs;
	}

	public void run()
	{
		for (Object o : pgraph.getNodes())
		{
			if (o instanceof Node)
			{
				Node node = (Node) o;

				for (XRef ref : refs)
				{
					if (node.getReferences().contains(ref))
					{
						node.setHighlight(true);
					}
				}
			}
		}
	}
}
