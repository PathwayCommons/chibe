package org.gvt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.model.EdgeModel;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;
import org.gvt.model.basicsif.BasicSIFEdge;
import org.gvt.model.basicsif.BasicSIFGroup;
import org.gvt.model.basicsif.BasicSIFNode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Action for highlighting the edges in a SIF view with their absence in a selected SIF file.
 *
 * @author Ozgun Babur
 */
public class HighlightSIFDifferenceAction extends ChiBEAction
{
	/**
	 * The SIF file that we are looking for difference from.
	 */
	String filename;

	/**
	 * Constructor
	 */
	public HighlightSIFDifferenceAction(ChisioMain main)
	{
		super("Highlight SIF Difference ...", null, main);
		addFilterExtension(FILE_KEY, new String[]{"*.sif"});
		addFilterName(FILE_KEY, new String[]{"SIF File (*.sif)"});
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void run()
	{
		if (main.getViewer() == null) return;

		// Ask user the name if it is null
		if (filename == null) filename = new FileChooser(this).choose(FILE_KEY);
		if (filename == null) return;

		// If we have an input file, highlight its contents

		Set<String> edgeNames = new HashSet<String>();

		loadElements(filename, edgeNames);

		int highlightedEdges = highlightAbsentEdges(edgeNames);

		if (highlightedEdges == 0)
		{
			MessageDialog.openInformation(main.getShell(),
				"Information:", "There is nothing to highlight.");
		}
		else
		{
			MessageDialog.openInformation(main.getShell(),
				"Information:", highlightedEdges + " edges are highlighted.");
		}

		filename = null;
	}

	protected int highlightAbsentEdges(Set<String> names)
	{
		// Iterate all edges in graph

		CompoundModel root = (CompoundModel) ((ChsRootEditPart) main.getViewer().
			getRootEditPart().getChildren().get(0)).getModel();

		Iterator<EdgeModel> iter = root.getEdges().iterator();

		int highlighted = 0;

		while (iter.hasNext())
		{
			EdgeModel em = iter.next();

			if (em instanceof BasicSIFEdge)
			{
				BasicSIFEdge edge = (BasicSIFEdge) em;

				NodeModel source = edge.getSource();
				NodeModel target = edge.getTarget();

				if (source instanceof BasicSIFGroup)
				{
					for (Object o : ((BasicSIFGroup) source).getChildren())
					{
						BasicSIFNode s = (BasicSIFNode) o;

						if (target instanceof BasicSIFGroup)
						{
							for (Object oo : ((BasicSIFGroup) target).getChildren())
							{
								BasicSIFNode t = (BasicSIFNode) oo;

								if (needsHighlight(edge, s, t, names))
								{
									highlight(edge, s, t);
									highlighted++;
								}
							}
						}
						else if (needsHighlight(edge, s, target, names))
						{
							highlight(edge, s);
							highlighted++;
						}
					}
				}
				else if (target instanceof BasicSIFGroup)
				{
					for (Object o : ((BasicSIFGroup) target).getChildren())
					{
						BasicSIFNode t = (BasicSIFNode) o;

						if (needsHighlight(edge, source, t, names))
						{
							highlight(edge, t);
							highlighted++;
						}
					}
				}
				else if (needsHighlight(edge, source, target, names))
				{
					highlight(edge);
					highlighted++;
				}
			}
		}
		return highlighted;
	}

	protected void loadElements(String filename, Set<String> edgeNames)
	{
		try
		{
			Scanner sc = new Scanner(new File(filename));

			while (sc.hasNextLine())
			{
				String[] token = sc.nextLine().split("\t");
				if (token.length < 3) continue;

				edgeNames.add(token[0] + " " + token[1] + " " + token[2]);
			}

			sc.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private boolean needsHighlight(BasicSIFEdge edge, NodeModel source, NodeModel target, Set<String> exclude)
	{
		String key = source.getText() + " " + edge.getTag() + " " + target.getText();
		if (exclude.contains(key)) return false;
		if (!edge.isDirected())
		{
			key = target.getText() + " " + edge.getTag() + " " + source.getText();
			if (exclude.contains(key)) return false;
		}
		return true;
	}

	private void highlight(GraphObject... gos)
	{
		for (GraphObject go : gos)
		{
			go.setHighlightColor(ChisioMain.higlightColor);
			go.setHighlight(true);
		}
	}
}