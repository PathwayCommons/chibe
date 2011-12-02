package org.gvt.model.sif;

import org.biopax.paxtools.model.level2.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl2.BioPAXL2Graph;
import org.gvt.model.biopaxl2.BioPAXNode;
import org.gvt.model.biopaxl2.Actor;
import org.gvt.model.EntityAssociated;
import org.gvt.util.EntityHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SIFNode extends BioPAXNode implements EntityAssociated
{
	private EntityHolder entity;

	public SIFNode(CompoundModel root, EntityHolder entity)
	{
		super(root);

		this.entity = entity;

		setShape("RoundRect");

//		extractReferences(entity);
		configFromModel();

		int width = suggestInitialWidth();

		if (!(entity instanceof smallMolecule) && width < Actor.MIN_INITIAL_WIDTH)
		{
			width = Actor.MIN_INITIAL_WIDTH;
		}

		int height = 20;
		setSize(new Dimension(width, height));
	}

	public void configFromModel()
	{
		setText(entity.getName());
		setTooltipText(getText());

		if (entity instanceof smallMolecule)
		{
			setColor(Actor.SMALL_MOL_BG_COLOR);
		}
		else
		{
			setColor(getStringSpecificColor(getText()));
		}
	}

	public EntityHolder getEntity()
	{
		return entity;
	}

	/**
	 * Each RDF id is associated with a color.
	 * @return a color specific to physical entity
	 */
	public Color getEntitySpecificColor()
	{
		if (entity == null) return null;

		return super.getStringSpecificColor(entity.getID());
	}

	public Collection<? extends Level2Element> getRelatedModelElements()
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
