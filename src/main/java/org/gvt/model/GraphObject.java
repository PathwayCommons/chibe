package org.gvt.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class implements the basis for all graph objects.
 * 
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public abstract class GraphObject
{
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	protected String text;

	protected String tooltipText;

	protected Font textFont;

	protected Color textColor;

	protected Color color;

	protected boolean highlight;

	protected Color highlightColor;

	protected Map<Object, Object> dataMap;

	public static final String P_TOOLTIP_TEXT = "_tooltipText";

	public static final String P_TEXT = "_text";

	public static final String P_TEXTFONT = "_textFont";

	public static final String P_TEXTCOLOR = "_textColor";

	public static final String P_COLOR = "_color";

	public static final String P_HIGHLIGHT = "_highlight";

	public static final String P_HIGHLIGHTCOLOR = "_highlightColor";

	protected GraphObject()
	{
		this.dataMap = new HashMap<Object, Object>(1);
	}

	public void setText(String text)
	{
		this.text = text;
		firePropertyChange(P_TEXT, null, this.text);
	}

	public String getText()
	{
		return this.text;
	}

	public void setTextFont(Font font)
	{
		this.textFont = font;
		firePropertyChange(P_TEXTFONT, null, this.textFont);
	}

	public Font getTextFont()
	{
		return this.textFont;
	}
	public void setTextColor(Color color)
	{
		this.textColor = color;
		firePropertyChange(P_TEXTCOLOR, null, this.textColor);
	}

	public Color getTextColor()
	{
		return this.textColor;
	}

	public String getTooltipText()
	{
		return tooltipText;
	}

	public void setTooltipText(String tooltipText)
	{
		this.tooltipText = tooltipText;
		firePropertyChange(P_TOOLTIP_TEXT, null, this.tooltipText);
	}

	public void setColor(Color c)
	{
		this.color = c;
		firePropertyChange(P_COLOR, null, this.color);
	}

	public Color getColor()
	{
		return color;
	}

	public boolean isHighlight()
	{
		return this.highlight;
	}

	public void setHighlight(boolean highlight)
	{
		this.highlight = highlight;
		firePropertyChange(P_HIGHLIGHT, null, this.highlight);
	}

	public Color getHighlightColor()
	{
		return this.highlightColor;
	}

	public void setHighlightColor(Color highlightColor)
	{
		this.highlightColor = highlightColor;
		firePropertyChange(P_HIGHLIGHTCOLOR, null, this.highlightColor);
	}

	public void addPropertyChangeListener(PropertyChangeListener l)
	{
		listeners.addPropertyChangeListener(l);
	}

	public void firePropertyChange(String propName, Object oldValue,
			Object newValue)
	{
		listeners.firePropertyChange(propName, oldValue, newValue);
	}

	public void removePropertyChangeListener(PropertyChangeListener l)
	{
		listeners.removePropertyChangeListener(l);
	}


	//----------------------------------------------------------------------------------------------
	// Section: User data related operations.
	//----------------------------------------------------------------------------------------------

	public void putLabel(Object label, Object value)
	{
		this.dataMap.put(label, value);
	}

	public void putLabel(Object label)
	{
		this.dataMap.put(label, null);
	}

	public boolean hasLabel(Object label)
	{
		return this.dataMap.containsKey(label);
	}

	public boolean hasLabel(Object label, Object value)
	{
		return this.dataMap.containsKey(label) && this.dataMap.get(label).equals(value);
	}

	public void removeLabel(Object label)
	{
		this.dataMap.remove(label);
	}

	public Object getLabel(Object label)
	{
		return this.dataMap.get(label);
	}
	
	public Set<Object> getAllLabels()
	{
		return this.dataMap.keySet();
	}
}