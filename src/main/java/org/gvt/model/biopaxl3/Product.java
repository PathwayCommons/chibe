package org.gvt.model.biopaxl3;

import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;

import java.util.Map;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Product extends BioPAXEdge
{
	public Product(ChbConversion source, NodeModel target)
	{
		super(source, target);
		
		assert target instanceof Actor || target instanceof ChbComplex;
		
		setArrow("Target");
		setColor(COLOR);
	}
	
	public Product(Product excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
	}

	private static final Color COLOR = new Color(null, 0, 0, 0);

	public int getSign()
	{
		return Edge.POSITIVE;
	}
}
