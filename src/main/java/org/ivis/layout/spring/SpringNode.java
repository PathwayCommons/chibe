package org.ivis.layout.spring;

import java.awt.Dimension;
import java.awt.Point;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;

/**
 * This class implements l-level nodes corresponding to Spring Layout.
 *
 * @author Class is taken from GINY library
 * @author Cihan Kucukkececi (modified by)
 * @author Selcuk Onur Sumer (modified by)
 */
public class SpringNode extends LNode
{
	public double x, y, xx, yy;

	public double xy, euclideanDistance;

	public void reset()
	{
		x = 0.0D;
		y = 0.0D;
		xx = 0.0D;
		yy = 0.0D;
		xy = 0.0D;
		euclideanDistance = 0.0D;
	}

	public void copyFrom(SpringNode other_partial_derivatives)
	{
		x = other_partial_derivatives.x;
		y = other_partial_derivatives.y;
		xx = other_partial_derivatives.xx;
		yy = other_partial_derivatives.yy;
		xy = other_partial_derivatives.xy;
		euclideanDistance = other_partial_derivatives.euclideanDistance;
	}

	public SpringNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
	}

	public SpringNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
	}

	public SpringNode(SpringNode node)
	{
		this(node.graphManager, node.vGraphObject);
		/* LNode information isn't copied. JUST PARTIAL DATA. */
		this.copyFrom(node);
	}
}