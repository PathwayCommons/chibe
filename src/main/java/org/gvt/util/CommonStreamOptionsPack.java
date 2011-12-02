package org.gvt.util;

import org.gvt.gui.AbstractQueryParamDialog;

/**
 * This class is used for storing data from Common Stream Query Dialog.
 *
 * @author Shatlyk Ashyralyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CommonStreamOptionsPack extends AbstractOptionsPack
{
	/**
	 * True for downstream, false for upstream.
	 */
	private boolean downstream;
	
	/**
	 * Constructor
	 */
	public CommonStreamOptionsPack()
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

	/**
	 * Set Default Values
	 */
	public void setDefaultValues()
	{
		super.setDefaultValues();
		
		//set default value of downstream
		setDownstream(AbstractQueryParamDialog.DOWNSTREAM);
	}
}