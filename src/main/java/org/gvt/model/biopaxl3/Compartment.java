package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.Level3Element;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.patika.mada.graph.GraphObject;

import java.util.*;

/**
 * A compartment in BioPAX file.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Compartment extends BioPAXCompoundNode
{
	public Compartment(CompoundModel root, String name)
	{
		super(root);

		setText(name);
		setColor(COLOR);
		setBorderColor(BORDER_COLOR);
	}

	public Compartment(Compartment toexcise, CompoundModel root)
	{
		super(toexcise, root);
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		// There is no first class entity in biopax that is associatable with compartment
		return new ArrayList<Level3Element>(0);
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

		list.add(new String[]{"Type", "Compartment"});
		list.add(new String[]{"Name", getText()});

		return list;
	}

	public Set<GraphObject> getRequisites()
	{
		return new HashSet<GraphObject>();
	}

	private static final Color COLOR = new Color(null, 0, 50, 100);
	private static final Color BORDER_COLOR = new Color(null, 0, 0, 255);
}
