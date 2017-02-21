package org.gvt.layout;

import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl2.Actor;
import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class EdgeFixLayout extends org.ivis.layout.cose.CoSELayout
{
	public EdgeFixLayout()
	{
	}

	@Override
	public LNode newNode(Object vNode)
	{
		return new BiPaNode(this.graphManager, vNode);
	}

	public LNode newNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		return new BiPaNode(gm, loc, size, vNode);
	}

	public LNode createNewLNode(LGraphManager gm, NodeModel model)
	{
		return new BiPaNode(gm, model);
	}

	public boolean layout()
	{
		return true;
	}
}
