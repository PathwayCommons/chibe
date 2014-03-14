package org.gvt.action;

import org.cbio.causality.util.Summary;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.GraphAnimation;
import org.gvt.command.LayoutCommand;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.layout.BiPaLayout;
import org.gvt.model.CompoundModel;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for CoSE layout operation.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class AlignSelectedAction extends Action
{
	ChisioMain main = null;
	ScrollingGraphicalViewer viewer;
	Direction dir;

	/**
	 * Constructor
	 */
	public AlignSelectedAction(ChisioMain main, Direction dir)
	{
		this(main, null, dir);
	}

	public AlignSelectedAction(ChisioMain main, ScrollingGraphicalViewer viewer, Direction dir)
	{
		super("Align Selected: " +
			dir.toString().substring(0, 1) + dir.toString().substring(1).toLowerCase());

		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/layout-cose.gif"));
		this.main = main;
		this.viewer = viewer;
		this.dir = dir;
	}

	public void run()
	{
		if (viewer == null) viewer = main.getViewer();
		if (viewer == null) return;

		List<GraphObject> selected = main.getSelectedModel();

		int newLoc = selectNewLoc(getLocs(selected));
		setNewLoc(selected, newLoc);
		adjustParents(selected);

		main.makeDirty();
		viewer = null;
	}

	private List<Integer> getLocs(List<GraphObject> selected)
	{
		List<Integer> locs = new ArrayList<Integer>();

		for (GraphObject go : selected)
		{
			if (go instanceof NodeModel)
			{
				NodeModel node = (NodeModel) go;

				int v;

				switch (dir)
				{
					case TOP: v = node.getTopAbs(); break;
					case BOTTOM: v = node.getBottomAbs(); break;
					case LEFT: v = node.getLeftAbs(); break;
					case RIGHT: v = node.getRightAbs(); break;
					case X_CENTER: v = node.getCenterXAbs(); break;
					case Y_CENTER: v = node.getCenterYAbs(); break;
					default: throw new IllegalArgumentException("Invalid direction: " + dir);
				}

				locs.add(v);
			}
		}
		return locs;
	}

	private void setNewLoc(List<GraphObject> selected, int newLoc)
	{
		for (GraphObject go : selected)
		{
			if (go instanceof NodeModel)
			{
				NodeModel node = (NodeModel) go;

				switch (dir)
				{
					case TOP: node.setTopAbs(newLoc); break;
					case BOTTOM: node.setBottomAbs(newLoc); break;
					case LEFT: node.setLeftAbs(newLoc); break;
					case RIGHT: node.setRightAbs(newLoc); break;
					case X_CENTER: node.setCenterXAbs(newLoc); break;
					case Y_CENTER: node.setCenterYAbs(newLoc); break;
					default: throw new IllegalArgumentException("Invalid direction: " + dir);
				}
			}
		}
	}

	private int selectNewLoc(List<Integer> locs)
	{
		switch (dir)
		{
			case TOP:
			case LEFT: return Summary.min(locs);
			case BOTTOM:
			case RIGHT: return Summary.max(locs);
			case X_CENTER:
			case Y_CENTER: return (int) Summary.mean(locs);
			default: throw new IllegalArgumentException("Invalid direction: " + dir);
		}
	}

	private void adjustParents(List<GraphObject> selected)
	{
		for (GraphObject go : selected)
		{
			if (go instanceof NodeModel)
			{
				NodeModel node = (NodeModel) go;
				adjustParent(node);
			}
		}
	}

	private void adjustParent(NodeModel node)
	{
		CompoundModel parent = node.getParentModel();
		if (parent != null)
		{
			parent.calculateSizeUp();

			adjustParent(parent);
		}
	}

	public enum Direction
	{
		TOP,
		BOTTOM,
		LEFT,
		RIGHT,
		X_CENTER,
		Y_CENTER
	}
}