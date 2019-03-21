package org.gvt.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import java.util.HashMap;
import java.util.Map;

/**
 * The specific information box.
 *
 * @author Ozgun Babur
 */
public class InfoFigure extends Figure
{
	/**
	 * 	Full info to be displayed.
 	 */
	private String info;

	/**
	 * Specifies whether this feature is a "NOT" feature defined in BiopAX level 3.
	 */
	private boolean not;

	/**
	 * Order number of the info box.
	 */
	private int i;

	/**
	 * Width and height of this box.
	 */
	private Dimension dim;

	public InfoFigure(String info, int i, Rectangle parentBounds, Dimension dim)
	{
		this.i = i;
		this.dim = dim;

		// Debug code -- Prints distinct info strings
//		if (!encountered.contains(info))
//		{
//			System.out.println("info = " + info);
//			encountered.add(info);
//		}
		//------------------------

		updateBounds(parentBounds);

		// Get rid of position information for accurate coloring using color maps

		if (info.indexOf("@") > 0) this.info = info.substring(0, info.indexOf("@")).trim();
		else if (info.indexOf("[") > 0) this.info = info.substring(0, info.indexOf("[")).trim();
		else this.info = info;

		not = false;
		if (info.startsWith("-")) 
		{
			this.info = info.substring(1);
			not = true;
		}
		
		Label label = new Label(getLetter(this.info));
		this.setToolTip(new Label(getTooltipText(info)));

		Rectangle r = getBounds().getCopy();
		r.y -= 1;
		r.x += 1;

		label.setBounds(r);
		label.setFont(FONT);

		label.setForegroundColor(getForeColor(this.info));
		this.add(label);
	}

	protected String getTooltipText(String info)
	{
		return info;
	}

	protected void paintFigure(Graphics g)
	{
		g.setAntialias(SWT.ON);
		Rectangle r = getParent().getBounds().getCopy();
		r.x += 2;
		r.width -= 4;
		updateBounds(r);

		String lett = getLetter(info);

		// Calculate the point to show the info
		Point p = getLoc(r);

		// Adjust background and foregound colors
		g.setBackgroundColor(getBackColor(info));
		g.setForegroundColor(getBordColor(info));

		// Draw the info base according to the corresponding shape

		int shp = getInfoShape(lett);

		int xoff = 1;
		int yoff = 1;
		
		switch(shp)
		{
			case OVAL:
				g.fillOval(p.x + xoff, p.y, dim.width-xoff, dim.height-yoff);
				g.drawOval(p.x + xoff, p.y, dim.width-1-xoff, dim.height-1-yoff);
				break;
			case RECT:
				g.fillRectangle(p.x + xoff, p.y, dim.width-xoff, dim.height-yoff);
				g.drawRectangle(p.x + xoff, p.y, dim.width-1-xoff, dim.height-1-yoff);
				break;
		}

		if (not)
		{
			g.setForegroundColor(DEFAULT_NOT_COLOR);
			g.drawLine(p.x,  p.y, p.x + dim.width, p.y + dim.height);
			g.drawLine(p.x + dim.width,  p.y, p.x, p.y + dim.height);
		}
	}

	private void updateBounds(Rectangle parentBounds)
	{
		setBounds(new Rectangle(getLoc(parentBounds), dim));
	}

	private Point getLoc(Rectangle rec)
	{
		int spannum = i / 4;
		int spansign = i % 2 == 0 ? 1 : -1;
		int y = (i % 4) < 2 ? rec.y : rec.height + rec.y - dim.height;
		int x = (i % 2 == 0 ? rec.x : rec.width + rec.x - dim.width) +
			(spannum * spansign * dim.width);
		return new Point(x, y);
	}

	protected Color getBackColor(String info)
	{
		Color c = backColorMap.get(info.toLowerCase());
		return c == null ? Character.isDigit(info.charAt(0)) ?
			DIGIT_BACK_COLOR : DEFAULT_BACK_COLOR : c;
	}

	protected Color getForeColor(String info)
	{
		Color c = foreColorMap.get(info.toLowerCase());
		return c == null ? Character.isDigit(info.charAt(0)) ?
			DIGIT_FORE_COLOR : DEFAULT_FORE_COLOR : c;
	}

	protected Color getBordColor(String info)
	{
		Color c = bordColorMap.get(info.toLowerCase());
		return c == null ? DEFAULT_BORD_COLOR : c;
	}

	protected int getInfoShape(String letter)
	{
		if (Character.isDigit(letter.charAt(0)) ||
			info.equals("active") || info.equals("active tf") ||
			info.equals("native") || info.equals("inactive") ||
			info.equals("residue modification, active") ||
			info.equals("residue modification, inactive"))
		{
			return RECT;
		}
		else
		{
			return OVAL;
		}
	}

	protected String getLetter(String info)
	{
		String let = letterMap.get(info.toLowerCase());
		return let == null ? info.substring(0, 1).toLowerCase() : let;
	}

	protected static final Font FONT = new Font(null, "Segoe UI", 7, 0);
	protected static final Color DIGIT_BACK_COLOR = new Color(null, 250, 250, 250);
	protected static final Color DIGIT_FORE_COLOR = new Color(null, 0, 0, 0);
	protected static final Color DEFAULT_BACK_COLOR = new Color(null, 255, 255, 255);
	protected static final Color DEFAULT_FORE_COLOR = new Color(null, 50, 50, 50);
	protected static final Color DEFAULT_BORD_COLOR = new Color(null, 0, 0, 0);
	protected static final Color DEFAULT_NOT_COLOR = new Color(null, 200, 0, 0);

	protected static final Map<String, Color> backColorMap = new HashMap<String, Color>();
	protected static final Map<String, Color> foreColorMap = new HashMap<String, Color>();
	protected static final Map<String, Color> bordColorMap = new HashMap<String, Color>();
	protected static final Map<String, String> letterMap = new HashMap<String, String>();

	private static void put(String s, Color b, Color f, String l)
	{
		assert s != null;
		if (b != null) backColorMap.put(s, b);
		if (f != null) foreColorMap.put(s, f);
		if (l != null) letterMap.put(s, l);
	}
	
	static
	{
		final Color WHITE = new Color(null, 255, 255, 255);
		final Color PHOSPHO_BG = new Color(null, 230, 230, 100);
		final Color PHOSPHO_FORE = new Color(null, 0, 0, 50);
		final Color ACTIVE_BG = new Color(null, 50, 150, 50);
		final Color ACTIVE_FORE = WHITE;
		final Color INACTIVE_BG = new Color(null, 150, 50, 50);
		final Color INACTIVE_FORE = WHITE;
		final Color METHYL_FORE = new Color(null, 20, 20, 200);
		final Color GLYCOSYL_FORE = new Color(null, 20, 20, 100);
		final Color FUCOSYL_FORE = new Color(null, 20, 20, 100);
		final Color UBIQUITIN_FORE = new Color(null, 100, 20, 20);

		put("phosphorylation", PHOSPHO_BG, PHOSPHO_FORE, null);
		put("phosphorylation site", PHOSPHO_BG, PHOSPHO_FORE, null);
		put("phosphate group", PHOSPHO_BG, PHOSPHO_FORE, null);
		put("phosphorylated residue", PHOSPHO_BG, PHOSPHO_FORE, null);
		put("phosphorylated", PHOSPHO_BG, PHOSPHO_FORE, null);
		put("o-phospho-l-serine", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("o-phospho-l-threonine", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("o-phospho-l-tyrosine", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("o4'-phospho-l-tyrosine", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("mi:0170", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("phosres", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("optyr", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("opthr", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("opser", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("opser-6", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("opthr-24", PHOSPHO_BG, PHOSPHO_FORE, "p");
		put("opthr-10", PHOSPHO_BG, PHOSPHO_FORE, "p");

		put("active", ACTIVE_BG, ACTIVE_FORE, null);
		put("inactive", INACTIVE_BG, INACTIVE_FORE, null);
		put("residue modification, inactive", INACTIVE_BG, INACTIVE_FORE, "i");
		put("residue modification, active", ACTIVE_BG, ACTIVE_FORE, "a");

		put("native", new Color(null, 200, 200, 200), new Color(null, 100, 100, 100), null);
		put("ubiquitination site", null, UBIQUITIN_FORE, null);
		put("ubiquitination", null, UBIQUITIN_FORE, null);
		put("ubiquitinylated lysine", null, UBIQUITIN_FORE, null);
		put("chain coordinates", new Color(null, 150, 150, 150), WHITE, null);
		put("methylated lysine", null, METHYL_FORE, null);
		put("n6,n6-dimethyl-l-lysine", null, METHYL_FORE, "m");
		put("n6-methyl-l-lysine", null, METHYL_FORE, "m");
		put("n-acetylated l-lysine", null, new Color(null, 20, 80, 20), "a");
		put("n4-glycosyl-l-asparagine", null, GLYCOSYL_FORE, "g");
		put("o-glycosyl-l-threonine", null, GLYCOSYL_FORE, "g");
		put("o-glucosyl-l-serine", null, GLYCOSYL_FORE, "g");
		put("o-fucosyl-l-threonine", null, FUCOSYL_FORE, "f");
		put("o-fucosyl-l-serine", null, FUCOSYL_FORE, "f");
	}

	protected static final int OVAL = 0;
	protected static final int RECT = 1;
}
