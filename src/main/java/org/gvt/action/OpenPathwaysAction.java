package org.gvt.action;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.CTabItem;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.PathwaySelectionDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.util.PathwayHolder;
import org.patika.mada.util.XRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class OpenPathwaysAction extends Action
{
	private ChisioMain main;

	/**
	 * Pathways to open. If not supplied in constructor, then user is prompted with a dialog for
	 * selecting pathways to open.
	 */
	private List<String> pathways;

	/**
	 * Objects in the opened view will be highlighted if this list is not null any of their
	 * references is in this list.
	 */
	private Collection<XRef> refsToHighlight;
	
	/**
	 * Constructor
	 */
	public OpenPathwaysAction(ChisioMain main)
	{
		super("Open Pathway ...");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/pathway-open.png"));
		this.main = main;
	}

	public OpenPathwaysAction(ChisioMain main, List<String> pathways)
	{
		this(main);
		this.pathways = pathways;
	}

	public void run()
	{
		Model model = main.getOwlModel();

		if (model == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first.");

			return;
		}

		List<String> allNames = new ArrayList<String>(main.getAllPathwayNames());
		Map<String, PathwayHolder> nameToPathwayMap = main.getRootGraph().getNameToPathwayMap();

		assert allNames.size() == nameToPathwayMap.size() : "Sizes do not match!";

		if (allNames.isEmpty())
		{
			String message = model.getObjects().isEmpty() ? "BioPAX model is empty!" :
				"There is no pathway defined in this BioPAX model.\n" +
					"You can create one using \"Pathway > Create\" menu.";

			MessageDialog.openInformation(main.getShell(), "No Pathway!", message);

			return;
		}

		if (pathways != null)
		{
			pathways.retainAll(allNames);
		}
		else
		{
			pathways = new ArrayList<String>();
			pathways.addAll(main.getOpenTabNames());

            if (model.getLevel() == BioPAXLevel.L2)
            {
                ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(),
                    500,
                    "Pathway Selection Dialog",
                    "Select pathways to view",
                    allNames, pathways, true, true, null);

                dialog.open();

                if (dialog.isCancelled())
                {
                    pathways = null;
                    return;
                }
            }
            else
            {
                PathwaySelectionDialog dialog = new PathwaySelectionDialog(
                        main.getShell(), model, pathways);

                dialog.open();

                if (dialog.isCancelled())
                {
                    pathways = null;
                    return;
                }
            }
		}

		if (pathways != null)
		{
			for (String name : new ArrayList<String>(main.getOpenTabNames()))
			{
				// Close pathways that user deselect
				if (!pathways.contains(name))
				{
					main.closeTabIfNotBasicSIF(name, true);
				}
				// Remove names of already open pathways from selection
				else
				{
					pathways.remove(name);
				}
			}
		}

		if (!pathways.isEmpty())
		{
			for (String name : pathways)
			{
				openPathwayInTab(nameToPathwayMap.get(name));
			}
		}

		// Do not remmeber pathways the next time
		pathways = null;
	}

	private void openPathwayInTab(PathwayHolder p)
	{
		assert p != null;

		BioPAXGraph graph = main.getRootGraph().excise(p);
		boolean layedout = graph.fetchLayout();
		CTabItem tab = main.createNewTab(graph);

		if (refsToHighlight != null && !refsToHighlight.isEmpty())
		{
			ScrollingGraphicalViewer viewer = main.getTabToViewerMap().get(tab);
			CompoundModel root = (CompoundModel) viewer.getContents().getModel();

			if (root instanceof BioPAXGraph)
			{
				new HighlightWithRefAction(main, (BioPAXGraph) root, refsToHighlight).run();
			}
		}

		if (!layedout)
		{
			new CoSELayoutAction(main).run();
		}
	}

	public Collection<XRef> getRefsToHighlight()
	{
		return refsToHighlight;
	}

	public void setRefsToHighlight(Collection<XRef> refsToHighlight)
	{
		this.refsToHighlight = refsToHighlight;
	}
}