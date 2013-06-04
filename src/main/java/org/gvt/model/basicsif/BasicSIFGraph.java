package org.gvt.model.basicsif;

import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl2.BioPAXL2Graph;
import org.patika.mada.graph.GraphObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BasicSIFGraph extends BioPAXL2Graph
{
	public BasicSIFGraph()
	{
		this.setGraphType(BASIC_SIF);
	}

	


	public void write(OutputStream os)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

			for (Object o : getEdges())
			{
				BasicSIFEdge edge = (BasicSIFEdge) o;
				BasicSIFNode source = (BasicSIFNode) edge.getSource();
				BasicSIFNode target = (BasicSIFNode) edge.getTarget();
				
				writer.write(source.getRdfid() + "\t");
				writer.write(edge.type.getTag() + "\t");
				writer.write(target.getRdfid() + "\n");
			}

			writer.close();
		}
		catch (IOException e){e.printStackTrace();}
	}

	public BioPAXL2Graph excise(Collection<GraphObject> objects, boolean keepHighlights)
	{
		BasicSIFGraph graph = new BasicSIFGraph();

		Map<NodeModel, NodeModel> map = new HashMap<NodeModel, NodeModel>();

		for (GraphObject object : objects)
		{
			if (object instanceof BasicSIFNode)
			{
				BasicSIFNode orig = (BasicSIFNode) object;
				BasicSIFNode ex = new BasicSIFNode(orig, graph);
				map.put(orig, ex);
			}
		}
		
		for (GraphObject object : objects)
		{
			if (object instanceof BasicSIFEdge)
			{
				BasicSIFEdge orig = (BasicSIFEdge) object;
				new BasicSIFEdge(orig, map);
			}
		}

		return graph;
	}
}