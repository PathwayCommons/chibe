package org.gvt.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cbio.causality.idmapping.HGNC;
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
public class QueryOptionsPack implements Serializable
{
	private boolean useID;

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
	private boolean cancel;

	/**
	 * Source symbols
	 */
	private List<String> sourceList;

	/**
	 * Target symbols
	 */
	private List<String> targetList;

	/**
	 * Symbols whose HGNC ID is unknown to ChiBE.
	 */
	private List<String> unknownSymbols;

	// These are used when bothstream is an option, like in neighborhood
	private boolean downstream;
	private boolean upstream;

	/**
	 * A value that will be summed with shortest path's length and the result
	 * will be the length limit.
	 */
	private int shortestPlusKLimit;

	/**
	 * True if user chooses length limit, false if user chooses shortest+k.
	 */
	private boolean limitType;

	/**
	 * To control the stopping behaviour of BFS. If true, search finishes at
	 * the first time of reaching target.
	 */
	private boolean strict;

	/**
	 * A prefix for querying PC with gene symbols
	 */
	final String SYMBOL_PREFIX = "urn:biopax:RelationshipXref:HGNC_HGNC%3A";

	public QueryOptionsPack()
	{
		this.lengthLimit = 1;
		this.currentView = false;
		this.cancel = true;
		this.sourceList = null;
		this.targetList = null;
		this.downstream = true;
		this.upstream = false;
		this.shortestPlusKLimit = 0;
		this.limitType = true;
		this.strict = false;
	}

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

	public List<String> getUnknownSymbols()
	{
		return unknownSymbols;
	}

	public void clearUnknownSymbols()
	{
		if (unknownSymbols != null) unknownSymbols.clear();
	}

	protected List<String> getConvertedSymbols(List<String> symbols)
	{
		if (unknownSymbols == null) unknownSymbols = new ArrayList<String>();

		List<String> list = new ArrayList<String>();

		for (String s : symbols)
		{
			String official = HGNC.getSymbol(s);

			if (official == null)
			{
				unknownSymbols.add(s);
			}
			else
			{
				list.add(official);
			}
		}
		return list;
	}

	protected String getOneStringSymbols(List<String> symbols)
	{
		String text = "";

		for (String s : symbols)
		{
			text += s + "\n";
		}
		return text.trim();
	}

	public List<String> getConvertedSourceList()
	{
		if (useID) return sourceList;
		return sourceList == null ? Collections.<String>emptyList() :
			getConvertedSymbols(sourceList);
	}

	public List<String> getConvertedTargetList()
	{
		if (useID) return targetList;
		return targetList == null ? Collections.<String>emptyList() :
			getConvertedSymbols(targetList);
	}

	public String getOneStringSources()
	{
		return getOneStringSymbols(sourceList);
	}

	public String getOneStringTargets()
	{
		return getOneStringSymbols(targetList);
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

	public int getShortestPlusKLimit()
	{
		return shortestPlusKLimit;
	}

	public void setShortestPlusKLimit(int shortestPlusKLimit)
	{
		this.shortestPlusKLimit = shortestPlusKLimit;
	}

	public boolean isStrict()
	{
		return strict;
	}

	public void setStrict(boolean strict)
	{
		this.strict = strict;
	}

	public boolean getLimitType()
	{
		return limitType;
	}

	public void setLimitType(boolean limitType)
	{
		this.limitType = limitType;
	}

	public boolean isUseID()
	{
		return useID;
	}

	public void setUseID(boolean useID)
	{
		this.useID = useID;
	}

	/**
	 * This method sets default values of length limit and current view fields
	 */
	public void setDefaultValues()
	{
		setLengthLimit(AbstractQueryParamDialog.DEFAULT_LENGTH_LIMIT);
		setCurrentView(AbstractQueryParamDialog.CURRENT_VIEW);

		setUpstream(true);
		setDownstream(true);

		setShortestPlusKLimit(AbstractQueryParamDialog.DEFAULT_SHORTEST_PLUS_K);
		setLimitType(AbstractQueryParamDialog.LIMIT_TYPE);

		setStrict(AbstractQueryParamDialog.STRICT);
	}
}
