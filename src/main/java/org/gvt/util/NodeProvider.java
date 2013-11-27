package org.gvt.util;

import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;

/**
 * @author Ozgun Babur
 */
public interface NodeProvider
{
	/**
	 * Gets or creates the node to build the network.
	 * @param id ID of the node
	 * @return node
	 */
	public NodeModel getNode(String id, CompoundModel relatedRoot);

	/**
	 * Checks if the entitiy needs to be displayed on the graph. Sometimes we need to know this
	 * before attempting to create it.
	 */
	public boolean needsToBeDisplayed(String id);

	/**
	 * This method is needed because sometimes a node uses this provider in its constructor. Since
	 * it is still being constructed, it won't be yet registered by the provider. This method
	 * enables nodes to add themselves to the providers registry while they are constructed.
	 */
	public void register(String id, NodeModel node);
}
