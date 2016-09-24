package org.gvt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Action for highlighting the nodes using a given name.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class HighlightUsingFileAction extends ChiBEAction
{
	/**
	 * Name to use in highlighting.
	 */
	String filename;

	/**
	 * Constructor
	 */
	public HighlightUsingFileAction(ChisioMain main)
	{
		super("Highlight Using File ...", null, main);
		addFilterExtension(FILE_KEY, new String[]{"*.highlight"});
		addFilterName(FILE_KEY, new String[]{"Highlight File (*.highlight)"});
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

		Set<String> nodeNames = new HashSet<String>();
		Set<String> edgeNames = new HashSet<String>();

		loadElementsToHighlight(filename, nodeNames, edgeNames);

		int highlightedNodes = highlightNodes(nodeNames);
		int highlightedEdges = highlightEdges(edgeNames);

		if (highlightedNodes + highlightedEdges == 0)
		{
			MessageDialog.openInformation(main.getShell(),
				"Information:", "There is nothing to highlight.");
		}
		else if (highlightedEdges == 0)
		{
			MessageDialog.openInformation(main.getShell(),
				"Information:", highlightedNodes + " nodes are highlighted.");
		}
		else if (highlightedNodes == 0)
		{
			MessageDialog.openInformation(main.getShell(),
				"Information:", highlightedEdges + " edges are highlighted.");
		}
		else
		{
			MessageDialog.openInformation(main.getShell(),
				"Information:", highlightedNodes + " nodes and " + highlightedEdges + " edges are highlighted.");
		}

		filename = null;
	}

	protected int highlightNodes(Set<String> names)
	{
		// Iterate all nodes in graph

		CompoundModel root = (CompoundModel) ((ChsRootEditPart) main.getViewer().
			getRootEditPart().getChildren().get(0)).getModel();

		Iterator<NodeModel> nodeIter = root.getNodes().iterator();

		int highlighted = 0;

		while (nodeIter.hasNext())
		{
			NodeModel node = nodeIter.next();
			if (names.contains(node.getText()))
			{
				node.setHighlightColor(ChisioMain.higlightColor);
				node.setHighlight(true);
				highlighted ++;
			}
		}
		return highlighted;
	}

	protected int highlightEdges(Set<String> names)
	{
		// Iterate all nodes in graph

		CompoundModel root = (CompoundModel) ((ChsRootEditPart) main.getViewer().
			getRootEditPart().getChildren().get(0)).getModel();

		Iterator<EdgeModel> nodeIter = root.getEdges().iterator();

		int highlighted = 0;

		while (nodeIter.hasNext())
		{
			EdgeModel edge = nodeIter.next();
			if (names.contains(edge.getSource().getText() + "\t" + edge.getTarget().getText()))
			{
				edge.setHighlightColor(ChisioMain.higlightColor);
				edge.setHighlight(true);
				highlighted++;
			}
		}
		return highlighted;
	}

	protected void loadElementsToHighlight(String filename, Set<String> nodeNames, Set<String> edgeNames)
	{
		try
		{
			Scanner sc = new Scanner(new File(filename));

			while (sc.hasNextLine())
			{
				String[] token = sc.nextLine().split("\t");
				if (token.length < 2) continue;

				if (token[0].equals("node")) nodeNames.add(token[1]);
				else if (token[0].equals("edge")) edgeNames.add(token[1] + "\t" + token[2]);
			}

			sc.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}