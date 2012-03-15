package org.gvt.command;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.gvt.model.EdgeBendpoint;
import org.gvt.model.EdgeModel;

/**
 * This class maintains the base bendpoint command for bendpoints. Other
 * bendpoint commands (create, delete, move) are derived from this class.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class BendpointCommand extends Command
{
	protected int index;

	protected Point location;

	protected EdgeModel wire;

	private Dimension d1, d2;

	protected Dimension getFirstRelativeDimension()
	{
		return d1;
	}

	protected Dimension getSecondRelativeDimension()
	{
		return d2;
	}

	protected int getIndex()
	{
		return index;
	}

	protected Point getLocation()
	{
		return location;
	}

	protected EdgeModel getWire()
	{
		return wire;
	}

	public void redo()
	{
		execute();
	}

	public void setRelativeDimensions(Dimension dim1, Dimension dim2)
	{
		d1 = dim1;
		d2 = dim2;
	}

	public void setIndex(int i)
	{
		index = i;
	}

	public void setLocation(Point p)
	{
		location = p;
	}

	public void setWire(EdgeModel w)
	{
		wire = w;
	}
	
	public void execute()
	{
		super.execute();
		
		getWire().getSource().getParentModel().calculateSize();
	}
}