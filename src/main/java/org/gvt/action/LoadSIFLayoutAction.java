package org.gvt.action;

import org.eclipse.draw2d.geometry.Point;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.sifl3.SIFGraph;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class LoadSIFLayoutAction extends ChiBEAction
{
	private String layoutFilename;
	private BioPAXGraph graph;

	/**
	 * Constructor
	 * @param main main application
	 */
	public LoadSIFLayoutAction(ChisioMain main)
	{
		super("Load Layout...", null, main);
		addFilterExtension(FILE_KEY, new String[]{"*.layout"});
		addFilterName(FILE_KEY, new String[]{"SIF Layout (*.layout)"});
	}

	public void setLayoutFilename(String layoutFilename)
	{
		this.layoutFilename = layoutFilename;
	}

	public void setGraph(BioPAXGraph graph)
	{
		this.graph = graph;
	}

	public void run()
	{
		if (graph == null) graph = main.getPathwayGraph();
		if (!(graph instanceof BasicSIFGraph || graph instanceof SIFGraph)) return;

		if (layoutFilename == null) layoutFilename = new FileChooser(this).choose(FILE_KEY);
		if (layoutFilename == null) return;

		Map<String, Point> map = loadLayout(layoutFilename);

		if (graph instanceof SIFGraph)
		{
			((SIFGraph) graph).loadLayout(map);
		}
		else if (graph instanceof BasicSIFGraph)
		{
			((BasicSIFGraph) graph).loadLayout(map);
		}

		EdgeFixLayoutAction action = new EdgeFixLayoutAction(main);
		action.run();

		graph = null;
		layoutFilename = null;
	}

	private Map<String, Point> loadLayout(String filename)
	{
		try
		{
			Map<String, Point> map = new HashMap<String, Point>();
			Scanner sc = new Scanner(new File(filename));

			while (sc.hasNextLine())
			{
				String[] token = sc.nextLine().split("\t");
				if (token.length >= 3)
				{
					map.put(token[0], new Point(Integer.valueOf(token[1]), Integer.valueOf(token[2])));
				}
			}

			return map;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}