package org.gvt.util;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.xmlbeans.XmlString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.graphdrawing.graphml.xmlns.*;
import org.gvt.model.*;

/**
 * GraphML writer class for saving graphml files
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class GraphMLWriter
{
	HashMap hashMap = new HashMap();

	GraphmlType newGraphml;

	KeyType xKey;

	KeyType yKey;

	KeyType heightKey;

	KeyType widthKey;

	KeyType colorKey;

	KeyType borderColorKey;

	KeyType textKey;

	KeyType textFontKey;

	KeyType textColorKey;

	KeyType shapeKey;

	KeyType clusterIDKey;

	KeyType marginKey;

	KeyType arrowKey;

	KeyType styleKey;

	KeyType bendpointKey;

	KeyType highlightColorKey;

	public Object writeXMLFile(CompoundModel root)
	{
		// create a new graphml file
		GraphmlDocument newGraphmlDoc = GraphmlDocument.Factory.newInstance();
		newGraphml = newGraphmlDoc.addNewGraphml();

		// define the keys that will be used in xml file
		xKey = newGraphml.addNewKey();
		xKey.setId("x");
		xKey.setFor(KeyForType.NODE);
		xKey.setAttrName("x");
		xKey.setAttrType(KeyTypeType.INT);

		yKey = newGraphml.addNewKey();
		yKey.setId("y");
		yKey.setFor(KeyForType.NODE);
		yKey.setAttrName("y");
		yKey.setAttrType(KeyTypeType.INT);

		heightKey = newGraphml.addNewKey();
		heightKey.setId("height");
		heightKey.setFor(KeyForType.NODE);
		heightKey.setAttrName("height");
		heightKey.setAttrType(KeyTypeType.INT);

		widthKey = newGraphml.addNewKey();
		widthKey.setId("width");
		widthKey.setFor(KeyForType.NODE);
		widthKey.setAttrName("width");
		widthKey.setAttrType(KeyTypeType.INT);

		shapeKey = newGraphml.addNewKey();
		shapeKey.setId("shape");
		shapeKey.setFor(KeyForType.NODE);
		shapeKey.setAttrName("shape");
		shapeKey.setAttrType(KeyTypeType.STRING);

		clusterIDKey = newGraphml.addNewKey();
		clusterIDKey.setId("clusterID");
		clusterIDKey.setFor(KeyForType.NODE);
		clusterIDKey.setAttrName("clusterID");
		clusterIDKey.setAttrType(KeyTypeType.STRING);

		marginKey = newGraphml.addNewKey();
		marginKey.setId("margin");
		marginKey.setFor(KeyForType.GRAPH);
		marginKey.setAttrName("margin");
		marginKey.setAttrType(KeyTypeType.INT);

		styleKey = newGraphml.addNewKey();
		styleKey.setId("style");
		styleKey.setFor(KeyForType.EDGE);
		styleKey.setAttrName("style");
		styleKey.setAttrType(KeyTypeType.STRING);

		arrowKey = newGraphml.addNewKey();
		arrowKey.setId("arrow");
		arrowKey.setFor(KeyForType.EDGE);
		arrowKey.setAttrName("arrow");
		arrowKey.setAttrType(KeyTypeType.STRING);

		bendpointKey = newGraphml.addNewKey();
		bendpointKey.setId("bendpoint");
		bendpointKey.setFor(KeyForType.EDGE);
		bendpointKey.setAttrName("bendpoint");
		bendpointKey.setAttrType(KeyTypeType.STRING);

		colorKey = newGraphml.addNewKey();
		colorKey.setId("color");
		colorKey.setFor(KeyForType.ALL);
		colorKey.setAttrName("color");
		colorKey.setAttrType(KeyTypeType.STRING);

		borderColorKey = newGraphml.addNewKey();
		borderColorKey.setId("borderColor");
		borderColorKey.setFor(KeyForType.ALL);
		borderColorKey.setAttrName("borderColor");
		borderColorKey.setAttrType(KeyTypeType.STRING);

		textKey = newGraphml.addNewKey();
		textKey.setId("text");
		textKey.setFor(KeyForType.ALL);
		textKey.setAttrName("text");
		textKey.setAttrType(KeyTypeType.STRING);

		textFontKey = newGraphml.addNewKey();
		textFontKey.setId("textFont");
		textFontKey.setFor(KeyForType.ALL);
		textFontKey.setAttrName("textFont");
		textFontKey.setAttrType(KeyTypeType.STRING);

		textColorKey = newGraphml.addNewKey();
		textColorKey.setId("textColor");
		textColorKey.setFor(KeyForType.ALL);
		textColorKey.setAttrName("textColor");
		textColorKey.setAttrType(KeyTypeType.STRING);

		highlightColorKey = newGraphml.addNewKey();
		highlightColorKey.setId("highlightColor");
		highlightColorKey.setFor(KeyForType.ALL);
		highlightColorKey.setAttrName("highlightColor");
		highlightColorKey.setAttrType(KeyTypeType.STRING);

		// create a new graph with our root node recursively
		GraphType newGraph = newGraphml.addNewGraph();
		createTree(newGraph, root, "");

		return newGraphmlDoc;
	}

	/**
	 * create the graphml structure from chisio model recursively
	 *
	 * @param rootGraph
	 * @param root
	 * @param graphId
	 */
	public void createTree(
		GraphType rootGraph,
		CompoundModel root,
		String graphId
	)
	{
		// set root graph properties
		rootGraph.setId(graphId);
		rootGraph.setEdgedefault(GraphEdgedefaultType.UNDIRECTED);

		// create child nodes for this graph
		Iterator iter = root.getChildren().iterator();
		int i = 0;

		while (iter.hasNext())
		{
			NodeModel model = (NodeModel) iter.next();
			String id = graphId + "n" + i;
			hashMap.put(model, id);

			// create the node in graphml file
			NodeType newNode = rootGraph.addNewNode();
			newNode.setId(id);

			if (createNode(newNode, model))
			{
				// if node is a compound node than margin property must be added

				DataType marginData = rootGraph.addNewData();
				marginData.setKey(marginKey.getId());
				marginData.set(XmlString.Factory.newValue("0"));

				// Also the subgraph of this compound node must be created
				GraphType newGraph = newNode.addNewGraph();
				createTree(newGraph,
					(CompoundModel) model,
					newNode.getId() + ":");
			}

			i++;
		}

		if (graphId.equals(""))
		{
			// create edges in graphml file
			i = 0;
			Iterator edgeIter =
				root.getEdgeIterator(CompoundModel.ALL_EDGES, true, false);

			while (edgeIter.hasNext())
			{
				EdgeModel model = (EdgeModel) edgeIter.next();
				String id = graphId + "e" + i;

				// create an edge
				EdgeType newEdge = rootGraph.addNewEdge();
				newEdge.setId(id);
				newEdge.setSource((String) hashMap.get(model.getSource()));
				newEdge.setTarget((String) hashMap.get(model.getTarget()));

				createEdge(newEdge, model);

				i++;
			}
		}

		// also add margin property for this graph at the end
		DataType marginData = rootGraph.addNewData();
		marginData.setKey(marginKey.getId());
		marginData.set(XmlString.Factory.newValue("0"));
	}

	public boolean createNode(NodeType newNode, NodeModel model)
	{
		// write properties of this node into graphml file
		DataType xData = newNode.addNewData();
		xData.setKey(xKey.getId());
		XmlString xStr = XmlString.Factory.newValue("" + model.getConstraint().x);
		xData.set(xStr);

		DataType yData = newNode.addNewData();
		yData.setKey(yKey.getId());
		XmlString yStr = XmlString.Factory.newValue("" + model.getConstraint().y);
		yData.set(yStr);

		DataType heightData = newNode.addNewData();
		heightData.setKey(heightKey.getId());
		heightData.set(XmlString.Factory.newValue("" + model.getConstraint().height));

		DataType widthData = newNode.addNewData();
		widthData.setKey(widthKey.getId());
		XmlString widthStr = XmlString.Factory.newValue("" + model.getConstraint().width);
		widthData.set(widthStr);

		DataType colorData = newNode.addNewData();
		colorData.setKey(colorKey.getId());
		RGB rgb = model.getColor().getRGB();
		colorData.set(XmlString.Factory.newValue(rgb.red + " " + rgb.green + " " + rgb.blue));

		DataType borderColorData = newNode.addNewData();
		borderColorData.setKey(borderColorKey.getId());
		rgb = model.getBorderColor().getRGB();
		borderColorData.set(XmlString.Factory.newValue(rgb.red + " " +
			rgb.green + " " + rgb.blue));

		DataType textData = newNode.addNewData();
		textData.setKey(textKey.getId());
		textData.set(XmlString.Factory.newValue(model.getText()));

		DataType textFontData = newNode.addNewData();
		textFontData.setKey(textFontKey.getId());
		Font f = model.getTextFont();
		textFontData.set(XmlString.Factory.newValue(
			f.getFontData()[0].toString()));

		DataType textColorData = newNode.addNewData();
		textColorData.setKey(textColorKey.getId());
		rgb = model.getTextColor().getRGB();
		textColorData.set(XmlString.Factory.newValue(rgb.red + " " +
			rgb.green + " " + rgb.blue));

		if (model.isHighlight())
		{
			DataType highlightColorData = newNode.addNewData();
			highlightColorData.setKey(highlightColorKey.getId());

			String colorText;
			if (model.getHighlightColor() != null)
			{
				rgb = model.getHighlightColor().getRGB();
				colorText = rgb.red + " " + rgb.green + " " + rgb.blue;
			}
			else
			{
				colorText = "255 255 0";
			}

			highlightColorData.set(XmlString.Factory.newValue(colorText));
		}

		if (model instanceof CompoundModel)
		{
			return true;
		}
		else
		{
			// if node is a simple node, than write shape property
			DataType shapeData = newNode.addNewData();
			shapeData.setKey(shapeKey.getId());
			String shp = model.getShape();
			if (shp.startsWith("RoundRect")) shp = "Rectangle";
			else if (shp.startsWith("Diamond")) shp = "Triangle";
			shapeData.set(XmlString.Factory.newValue(shp));
		}

		return false;
	}

	public void createEdge(EdgeType newEdge, EdgeModel model)
	{
		// write edge's properties into file
		DataType colorData = newEdge.addNewData();
		colorData.setKey(colorKey.getId());
		RGB rgb = model.getColor().getRGB();
		colorData.set(XmlString.Factory.newValue(rgb.red + " " +
			rgb.green + " " + rgb.blue));

		DataType textData = newEdge.addNewData();
		textData.setKey(textKey.getId());
		textData.set(XmlString.Factory.newValue(model.getText()));

		DataType textFontData = newEdge.addNewData();
		textFontData.setKey(textFontKey.getId());
		Font f = model.getTextFont();
		textFontData.set(XmlString.Factory.newValue(
			f.getFontData()[0].toString()));

		DataType textColorData = newEdge.addNewData();
		textColorData.setKey(textColorKey.getId());
		rgb = model.getTextColor().getRGB();
		textColorData.set(XmlString.Factory.newValue(rgb.red + " " +
			rgb.green + " " + rgb.blue));

		DataType styleData = newEdge.addNewData();
		styleData.setKey(styleKey.getId());
		styleData.set(XmlString.Factory.
			newValue(model.getStyle()));

		DataType arrowData = newEdge.addNewData();
		arrowData.setKey(arrowKey.getId());
		arrowData.set(XmlString.Factory.
			newValue(model.getArrow()));

		Iterator<EdgeBendpoint> bendpointIter =
			model.getBendpoints().iterator();

		while (bendpointIter.hasNext())
		{
			DataType bendpointData = newEdge.addNewData();
			bendpointData.setKey(bendpointKey.getId());
			bendpointData.set(XmlString.Factory.
				newValue(bendpointIter.next().toString()));
		}

		DataType widthData = newEdge.addNewData();
		widthData.setKey(widthKey.getId());
		widthData.set(XmlString.Factory.
			newValue("" + model.getWidth()));

		if (model.isHighlight())
		{
			DataType highlightColorData = newEdge.addNewData();
			highlightColorData.setKey(highlightColorKey.getId());
			
			String colorText;
			if (model.getHighlightColor() != null)
			{
				rgb = model.getHighlightColor().getRGB();
				colorText = rgb.red + " " + rgb.green + " " + rgb.blue;
			}
			else
			{
				colorText = "255 255 0";
			}
			highlightColorData.set(XmlString.Factory.newValue(colorText));
		}
	}
}
