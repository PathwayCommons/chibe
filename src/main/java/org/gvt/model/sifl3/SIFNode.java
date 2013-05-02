package org.gvt.model.sifl3;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.gvt.model.biopaxl3.Actor;
import org.gvt.model.biopaxl3.BioPAXNode;
import org.gvt.util.EntityHolder;

import java.util.Collection;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SIFNode extends BioPAXNode implements EntityAssociated
{
	private EntityReference entity;

	public SIFNode(CompoundModel root, EntityHolder entity)
	{
		this(root, entity.l3er);
		assert entity.l3er != null;
	}

	public SIFNode(CompoundModel root, EntityReference entity)
	{
		super(root);

		this.entity = entity;

		setShape("RoundRect");

//		extractReferences(entity);
		configFromModel();

		int width = suggestInitialWidth();

		if (!(entity instanceof SmallMoleculeReference) && width < Actor.MIN_INITIAL_WIDTH)
		{
			width = Actor.MIN_INITIAL_WIDTH;
		}

		int height = 20;
		setSize(new Dimension(width, height));
	}

	public void configFromModel()
	{
		String name = extractGeneSymbol(entity);
		if (name == null) name = entity.getDisplayName();
		if (name == null && !entity.getName().isEmpty()) name = entity.getName().iterator().next();
		if (name == null) name = "noname";

		setText(name);
		setTooltipText(getText());

		if (entity instanceof SmallMoleculeReference)
		{
			setColor(Actor.SMALL_MOL_BG_COLOR);
		}
		else
		{
			setColor(getEntitySpecificColor());
		}
	}

	/**
	 * Each RDF id is associated with a color.
	 *
	 * @return a color specific to physical entity
	 */
	public Color getEntitySpecificColor()
	{
		return getStringSpecificColor(entity.getRDFId());
	}

	public EntityHolder getEntity()
	{
		return new EntityHolder(entity);
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return null;
	}

	public boolean isEvent()
	{
		return false;
	}

	public boolean isBreadthNode()
	{
		return true;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

//		addNamesAndTypeAndID(list, entity);
//		addDataSourceAndXrefAndComments(list, entity);

		return list;
	}
}
