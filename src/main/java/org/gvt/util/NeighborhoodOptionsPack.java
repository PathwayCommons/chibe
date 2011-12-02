package org.gvt.util;

import org.gvt.gui.AbstractQueryParamDialog;

/**
 * This class is used for storing data from Neighborhood Query Dialog.
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class NeighborhoodOptionsPack extends AbstractOptionsPack
{
	/**
	 * whether downstream or upstream is requested, both is also acceptable
	 */
	private boolean downstream;
	private boolean upstream;
	
	/**
	 * Constructor
	 */
	public NeighborhoodOptionsPack()
	{
		setDefaultValues();
	}
	
	/**
	 * Getters and Setters
	 */
	public boolean isDownstream()
	{
		return downstream;
	}

	public void setDownstream(boolean downstream)
	{
		this.downstream = downstream;
	}

	public boolean isUpstream()
	{
		return upstream;
	}

	public void setUpstream(boolean upstream)
	{
		this.upstream = upstream;
	}

	/**
	 * Set Default Values
	 */
	public void setDefaultValues()
	{
		super.setDefaultValues();
		
		//set default values of upstream and downstream
		
		setUpstream(AbstractQueryParamDialog.UPSTREAM);
		setDownstream(AbstractQueryParamDialog.DOWNSTREAM);
	}
}