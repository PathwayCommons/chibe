package org.gvt;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.gvt.action.*;

/**
 * This class implements the Chisio toolbar.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ToolbarManager extends ToolBarManager
{
	public ToolbarManager(int style, ChisioMain main)
	{
		super(style);
		add(new LoadBioPaxModelAction(main));
		add(new MergeAction(main));
		add(new SaveBioPAXFileAction(main));
		add(new Separator());
		add(new OpenPathwaysAction(main));
		add(new ClosePathwayAction(main, false));
		add(new CropAction(main, CropAction.CropTo.HIGHLIGHTED));
		add(new CropAction(main, CropAction.CropTo.SELECTED));
		add(new DeletePathwayAction(main, false));
		add(new UpdatePathwayAction(main, true));
		add(new Separator());
		add(new ExportToSIFAction(main, true));
		add(new Separator());
		add(new SelectionToolAction("Select Tool", main));
		add(new MarqueeZoomToolAction("Marquee Zoom Tool", main));
		add(new ZoomAction(main, -1 , null));
		add(new ZoomAction(main, +1 , null));
		add(new ZoomAction(main, 0 , null));
		add(new Separator());
		add(new HighlightSelectedAction(main));
		add(new RemoveHighlightFromSelectedAction(main));
		add(new DeleteAction(main));
		add(new RemoveCompoundAction(main));
		add(new InspectorAction(main, false));
		add(new Separator());
		add(new CoSELayoutAction(main));
		add(new SpringLayoutAction(main));
		add(new LayoutInspectorAction(main));
		add(new Separator());
		add(new DebugButtonAction(main));

		update(true);
	}
}