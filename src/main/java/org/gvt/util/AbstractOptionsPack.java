package org.gvt.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	 * Source symbols
	 */
	private List<String> sourceList;

	/**
	 * Target symbols
	 */
	private List<String> targetList;

	/**
	 * A prefix for querying PC with gene symbols
	 */
	final String SYMBOL_PREFIX = "urn:biopax:RelationshipXref:HGNC_";

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

	public void addSource(String s)
	{
		if (sourceList == null) sourceList = new ArrayList<String>();
		sourceList.add(s);
	}

	public void addTarget(String s)
	{
		if (targetList == null) targetList = new ArrayList<String>();
		targetList.add(s);
	}

	public List<String> getSourceList()
	{
		return sourceList;
	}

	public List<String> getTargetList()
	{
		return targetList;
	}

	public void setSourceList(List<String> sourceList)
	{
		this.sourceList = sourceList;
	}

	public void setTargetList(List<String> targetList)
	{
		this.targetList = targetList;
	}

	public void clearLists()
	{
		if (sourceList != null) sourceList.clear();
		if (targetList != null) targetList.clear();
	}

	protected List<String> getFormattedSymbols(List<String> symbols)
	{
		List<String> list = new ArrayList<String>();

		for (String s : symbols)
		{
			list.add(SYMBOL_PREFIX + s);
		}
		return list;
	}

	public List<String> getFormattedSourceList()
	{
		return getFormattedSymbols(sourceList);
	}

	public List<String> getFormattedTargetList()
	{
		return getFormattedSymbols(targetList);
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
