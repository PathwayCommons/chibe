package org.gvt.model;

import org.patika.mada.graph.Node;
import org.gvt.util.EntityHolder;

/**
 * @author Ozgun Babur
 */
public interface EntityAssociated extends Node
{
	public EntityHolder getEntity();
}
