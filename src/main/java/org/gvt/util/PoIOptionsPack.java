package org.gvt.util;

import org.gvt.gui.AbstractQueryParamDialog;

/**
 * This class is used for storing data from PoI Query Dialog.
 *
 * @author Merve Cakir
 */
public class PoIOptionsPack extends AbstractOptionsPack 
{
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
     * Constructor
     */
    public PoIOptionsPack()
	{
		setDefaultValues();
	}

    /**
     * Getters and setters
     */
    
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

    /**
     * To set the default values of fields.
     */
    public void setDefaultValues()
    {
        super.setDefaultValues();

        setShortestPlusKLimit(AbstractQueryParamDialog.DEFAULT_SHORTEST_PLUS_K);
        setLimitType(AbstractQueryParamDialog.LIMIT_TYPE);

        setStrict(AbstractQueryParamDialog.STRICT);
    }

}