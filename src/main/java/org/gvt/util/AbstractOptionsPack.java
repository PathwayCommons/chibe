package org.gvt.util;

import java.io.Serializable;

import org.gvt.gui.AbstractQueryParamDialog;

/**
 * This abstract class is for storing data from Local Query Dialogs.
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public abstract class AbstractOptionsPack  implements Serializable
{
	/**
	 * Length Limit of querying
	 */
	private int lengthLimit;
	
	/**
	 * Boolean to check whether result is going to be in 
	 * Current View or New View
	 */
	private boolean currentView; 

	/**
	 * Boolean to check whether dialog is closed by cancel
	 */
	private boolean cancel = true; 
	
	/**
	 * Getters and Setters
	 */
	public boolean isCancel()
	{
		return cancel;
	}

	public void setCancel(boolean cancel)
	{
		this.cancel = cancel;
	}

	public int getLengthLimit() 
	{
		return lengthLimit;
	}

	public void setLengthLimit(int lengthLimit) 
	{
		this.lengthLimit = lengthLimit;
	}

	public boolean isCurrentView() 
	{
		return currentView;
	}

	public void setCurrentView(boolean currentView) 
	{
		this.currentView = currentView;
	}

	/**
	 * This method sets default values of length limit and current view fields
	 */
	public void setDefaultValues()
	{
		setLengthLimit(AbstractQueryParamDialog.DEFAULT_LENGTH_LIMIT);
		setCurrentView(AbstractQueryParamDialog.CURRENT_VIEW);
	}
}
