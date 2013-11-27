package org.gvt.util;

import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.model.custom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.gvt.model.custom.CustomGraph.*;

/**
 * @author Ozgun Babur
 */
public class CustomReader
{
	protected Map<String, NodeModel> nodeMap;
	protected Map<String, CustomEdge> edgeMap;
	protected Map<String, CustomGroup> compoundMap;

	public CustomReader()
	{
		nodeMap = new HashMap<String, NodeModel>();
		edgeMap = new HashMap<String, CustomEdge>();
	}

	public CompoundModel readFile(File file)
	{
		Map<String, List<Map<String, String>>> map = readIntoMaps(file);

		CustomGraph graph = new CustomGraph(
			map.containsKey(GRAPH) ? map.get(GRAPH).iterator().next() : null);

		if (map.containsKey(NODE))
		for (Map<String, String> nodeProps : map.get(NODE))
		{
			if (nodeMap.containsKey(nodeProps.get(ID))) continue;
			CustomNode node = new CustomNode(graph, nodeProps);
			nodeMap.put(nodeProps.get(ID), node);
		}

		if (map.containsKey(EDGE))
		for (Map<String, String> edgeProps : map.get(EDGE))
		{
			if (edgeMap.containsKey(edgeProps.get(ID))) continue;
			
			assert edgeProps.containsKey(SOURCE);
			assert edgeProps.containsKey(TARGET);
			assert nodeMap.containsKey(edgeProps.get(SOURCE));
			assert nodeMap.containsKey(edgeProps.get(TARGET)) :
				"Target node does not exist: " + edgeProps.get(TARGET);

			CustomEdge edge = new CustomEdge(nodeMap.get(edgeProps.get(SOURCE)),
				nodeMap.get(edgeProps.get(TARGET)), edgeProps);

			edgeMap.put(edgeProps.get(ID), edge);
		}

		if (map.containsKey(COMPOUND))
		for (Map<String, String> compoundProps : map.get(COMPOUND))
		{
			assert compoundProps.containsKey(ID);
			CustomGroup group = new CustomGroup(graph, compoundProps, nodeMap);
			nodeMap.put(compoundProps.get(ID), group);
		}
		
		return graph;
	}
	
	protected Map<String, List<Map<String, String>>> readIntoMaps(File file)
	{
		try
		{
			Map<String, List<Map<String, String>>> map = 
				new HashMap<String, List<Map<String, String>>>();

			BufferedReader reader = new BufferedReader(new FileReader(file));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				line = line.trim();
				if (line.equals(ENDOFFILE)) break;
				if (line.length() == 0) continue;
				if (line.startsWith(COMMENT_INDICATOR)) continue;
				
				Map<String, String> eleMap = new HashMap<String, String>();
				
				for (String token : line.split(ELEMENT_SEPARATOR))
				{
					int ind = token.indexOf(PROPERTY_SEPARATOR);
					
					if (ind < 0) continue;
					
					eleMap.put(token.substring(0, ind).trim().toUpperCase(),
						token.substring(ind + 1).trim());
				}
				
				String type = eleMap.get(TYPE);
				if (type == null) continue;

				type = type.toUpperCase();
				if (!map.containsKey(type)) map.put(type, new ArrayList<Map<String, String>>());
				
				map.get(type).add(eleMap);
			}

			reader.close();
			
			return map;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
}
