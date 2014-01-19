package org.gvt.util;

import org.biopax.paxtools.pattern.miner.SIFType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.model.sifl3.SIFEdge;
import org.gvt.model.basicsif.BasicSIFEdge;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.ChisioMain;
import org.patika.mada.util.XRef;

import java.io.*;
import java.util.*;

/**
 * This class is extended from XMLReader but it reads txt files. This is because the class XMLReader
 * is incorrectly named. It does nothing about xml files, it should have been named as FileReader.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SIFReader
{
	private List<SIFType> enumTypes;
	private Set<String> types;
	private Map<String, BasicSIFNode> nodeMap;
	private Map<String, BasicSIFEdge> edgeMap;
	private Map<String, String> idToName;
	private Set<String> relationsSet;
	private BasicSIFGraph root;
	private String delim;
	private boolean useGroups;

	private boolean duplicatesExist;

	public SIFReader()
	{
		nodeMap = new HashMap<String, BasicSIFNode>();
		edgeMap = new HashMap<String, BasicSIFEdge>();
		relationsSet = new HashSet<String>();
		useGroups = true;
//		prepareIdToNameMap();
	}

	public SIFReader(List<SIFType> enumTypes)
	{
		this();
		this.enumTypes = enumTypes;
	}

	public void setUseGroups(boolean useGroups)
	{
		this.useGroups = useGroups;
	}

	public CompoundModel readXMLFile(File sifFile)
	{
		if (enumTypes != null)
		{
			types = new HashSet<String>();

			for (SIFType enumType : enumTypes)
			{
				types.add(enumType.getTag());
			}
		}
		try
		{
			duplicatesExist = false;
			root = new BasicSIFGraph();

			String filename = sifFile.getName();
			root.setName(filename.substring(0, filename.indexOf(".sif")));
			
			root.setAsRoot();

			delim = fileContainsTab(sifFile) ? "\t\n\r\f" : " \n\r\f";

			BufferedReader reader = new BufferedReader(new FileReader(sifFile));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.trim().length() > 0)
				{
					processLine(line);
				}
			}

			reader.close();

			if (duplicatesExist)
			{
//				root.write(new FileOutputStream(sifFile));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageBox messageBox = new MessageBox(
				new Shell(),
				SWT.ERROR_UNSUPPORTED_FORMAT);
			messageBox.setMessage("File cannot be loaded!");
			messageBox.setText(ChisioMain.TOOL_NAME);
			messageBox.open();

			return null;
		}

		System.out.println("SIF view contains " + root.getNodes().size() + " nodes and " +
			root.getEdges().size() + " edges.");

		// Format the SIF view if an additional format file exists

		String formatFile = sifFile.getPath().substring(0, sifFile.getPath().lastIndexOf(".")) + ".format";

		boolean group = useGroups;
		if (new File(formatFile).exists())
		{
			group = formatView(formatFile) && useGroups;
		}

		if (group) root.groupSimilarNodes();

		return root;
	}

	private void processLine(String line)
	{
		StringTokenizer st = new StringTokenizer(line, delim);

		String first = st.nextToken();

		if (st.hasMoreTokens())
		{
			String relation = st.nextToken();

			if (st.hasMoreTokens())
			{
				String second = st.nextToken();

				String meds = st.hasMoreTokens() ? st.nextToken() : null;

				// commented out because we want to display non-paxtools sifs
				if (types == null || types.contains(relation))
				{
					createUnit(first, relation, second, meds);
				}
			}
		}
		else
		{
			getNode(first);
		}
	}

	private void createUnit(String first, String relation, String second, String medIDs)
	{
		// Stop if relation is unfamiliar
		if (!SIFEdge.typeMap.containsKey(relation)) return;

		String relStr = first + "\t" + relation + "\t" + second;
		String revStr = second + "\t" + relation + "\t" + first;

		if (relationsSet.contains(relStr) ||
			(!SIFEdge.typeMap.get(relation).isDirected() &&
				relationsSet.contains(revStr)))
		{
			duplicatesExist = true;
			return;
		}
		else
		{
			relationsSet.add(relStr);
		}

		BasicSIFNode node1 = getNode(first);
		BasicSIFNode node2 = getNode(second);
		BasicSIFEdge edge = new BasicSIFEdge(node1, node2, relation, medIDs);
		edgeMap.put(first + " " + relation + " " + second, edge);
	}

	private BasicSIFNode getNode(String id)
	{
		if (!nodeMap.containsKey(id))
		{
			BasicSIFNode node = new BasicSIFNode(root, id, id);
			nodeMap.put(id, node);
		}

		return nodeMap.get(id);
	}

	private BasicSIFNode getNode_bkp(String id)
	{
		if (!nodeMap.containsKey(id))
		{
			String line = idToName.get(id);
			String name;
			String[] terms;

			if (line == null)
			{
				name = id;
				terms = new String[0];
			}
			else
			{
				terms = line.split(OUTER_SEP);
				name = terms[0];
			}

			if (name.length() == 0)
			{
				if (terms.length > 1) name = terms[1].split(INNER_SEP)[0];
				if (name.length() == 0) name = "noname";
			}

			BasicSIFNode node = new BasicSIFNode(root, id, name);

			node.addReference(new XRef("name" + XRef.SEPARATOR + node.getName()));

			if (terms.length > 1)
			{
				for (String ref : terms[1].split(INNER_SEP))
				{
					if (ref.length() > 0)
					{
						if (!ref.contains(XRef.SEPARATOR))
						{
							System.out.println("defective line = " + line);
						}
						else
						{
							node.addReference(new XRef(ref));
						}
					}
				}
			}
			if (terms.length > 2)
			{
				for (String ref : terms[2].split(INNER_SEP))
				{
					if (ref.length() > 0)
					{
						if (!ref.contains(XRef.SEPARATOR))
						{
							System.out.println("defective line = " + line);
						}
						else
						{
							node.addReference(new XRef(ref));
						}
					}
				}
			}

			nodeMap.put(id, node);
		}

		return nodeMap.get(id);
	}

	private boolean fileContainsTab(File filename) throws IOException
	{
		boolean contains = false;

		BufferedReader reader = new BufferedReader(new FileReader(filename));

		int i;
		while ((i = reader.read()) != -1)
		{
			if (i == '\t')
			{
				contains = true;
			}
		}
		reader.close();
		return contains;
	}

	private void prepareIdToNameMap()
	{
		idToName = new HashMap<String, String>();

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(
				"sif_files/all.owl.names.txt"));

			String line;
			while((line = reader.readLine()) != null)
			{
				int i = line.indexOf(OUTER_SEP);
				String id = line.substring(0, i);
				line = line.substring(i+1);
				idToName.put(id, line);
			}

			reader.close();
		}
		catch (Exception e){e.printStackTrace();}
	}

	private boolean formatView(String formatFile)
	{
		boolean group = useGroups;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(formatFile));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");

				if (token.length < 2) continue;

				if (token[0].equals("node"))
				{
					NodeModel node = findNode(token[1]);

					if (node == null) continue;

					if (token[2].equals("color"))
					{
						node.setColor(getColor(token[3]));
					}
					else if (token[2].equals("highlight"))
					{
						node.setHighlight(token[3].equals("on"));
					}
				}
				else if (token[0].equals("edge"))
				{
					BasicSIFEdge edge = edgeMap.get(token[1]);

					if (token[2].equals("color"))
					{
						edge.setColor(getColor(token[3]));
					}
				}
				else if (token[0].equals("graph"))
				{
					if (token[1].equals("grouping"))
					{
						useGroups = token[2].equals("on");
					}
				}
			}

			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return group;
	}

	private NodeModel findNode(String name)
	{
		for (Object o : root.getNodes())
		{
			NodeModel nm = (NodeModel) o;
			if (nm.getText().equals(name)) return nm;
		}
		return null;
	}

	private Color getColor(String s)
	{
		String[] c = s.split(" ");
		return new Color(null,
			Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
	}

	private static final String OUTER_SEP = "~";
	private static final String INNER_SEP = "\\|";
}