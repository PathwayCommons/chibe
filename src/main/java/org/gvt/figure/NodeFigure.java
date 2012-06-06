package org.gvt.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.gvt.ChsXYLayout;
import org.gvt.model.NodeModel;

import java.util.List;
import java.util.ArrayList;

/**
 * This class maintains the node figure which is the UI of nodes. Each node has
 * its own color, label and shape.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class NodeFigure extends Figure
{
	Label label;

	String shape;

	boolean highlight;

	Color highlightColor;
	
	boolean smallMolecule;
	boolean drawCloneMarker;

	public PointList triangle = new PointList(3);

	/**
	 * Constructors
	 */
	public NodeFigure()
	{
		super();
	}

	public NodeFigure(Point absLocation,
		Dimension size,
		String text,
		String tooltipText,
		Font textFont,
		Color textColor,
		Color color,
		Color borderColor,
		String shape,
		Color highlightColor,
		boolean highlight)
	{
		super();

		this.label = new Label();

		if (tooltipText != null) label.setToolTip(new Label(tooltipText));

		add(label);
		
		this.highlight = highlight;
		
		setBackgroundColor(color);
		setForegroundColor(borderColor);
		setLayoutManager(new ChsXYLayout());
		Rectangle r = new Rectangle(absLocation.getCopy(), size.getCopy());
		setBounds(r);

		updateText(text);
		updateTextFont(textFont);
		updateTextColor(textColor);
		updateHighlightColor(highlightColor);
		updateShape(shape);

		smallMolecule = false;
		drawCloneMarker = false;
	}

// -----------------------------------------------------------------------------
// Section: Update methods.
// -----------------------------------------------------------------------------


	public boolean isSmallMolecule()
	{
		return smallMolecule;
	}

	public void setSmallMolecule(boolean smallMolecule)
	{
		this.smallMolecule = smallMolecule;
	}

	public boolean isDrawCloneMarker()
	{
		return drawCloneMarker;
	}

	public void setDrawCloneMarker(boolean drawCloneMarker)
	{
		this.drawCloneMarker = drawCloneMarker;
	}

	public void updateText(String str)
	{
		this.label.setText(str);
	}

	public void updateTooltipText(String str)
	{
		this.label.setToolTip(new Label(str));
	}

	public void updateTextFont(Font f)
	{
		this.label.setFont(f);
	}

	public void updateTextColor(Color c)
	{
		this.label.setForegroundColor(c);
	}

	public void updateColor(Color color)
	{
		setBackgroundColor(color);
	}

	public void updateBorderColor(Color color)
	{
		setForegroundColor(color);
	}

	public void updateShape(String s)
	{
		this.shape = s;
		this.removeAll();
		
		if (shape.equals(NodeModel.shapes[0]))
		{
			add(new RectangleFigure(getBounds()));
		}
		else if (shape.equals(NodeModel.shapes[1]))
		{
			add(new RoundRectFigure(getBounds()));
		}
		else if (shape.equals(NodeModel.shapes[2]))
		{
			add(new EllipseFigure(getBounds()));
		}
		else if (shape.equals(NodeModel.shapes[3]))
		{
			add(new TriangleFigure(getBounds()));	
		}
		else if (shape.equals(NodeModel.shapes[4]))
		{
			add(new DiamondFigure(getBounds()));	
		}
		else if (shape.startsWith(NodeModel.shapes[5]))
		{
//			assert shape.indexOf(";") > 0 && shape.length() >= 19 : "shape = " + shape;

			if (shape.indexOf(";") > 0)
			{
				List<String> infos = new ArrayList<String>();

				String line = shape.substring(shape.indexOf(";")+1);
				for (String info : line.split(";"))
				{
					if (info.length() > 0)
					{
						infos.add(info);
					}
				}
	
				add(new RoundRectWithInfo(getBounds(), infos, label));
			}
			else
			{
				add(new RectangleFigure(getBounds()));
			}
		}

		if (!shape.startsWith(NodeModel.shapes[5]))
		{
			add(label);
		}
	}

	public void updateHighlight(Layer highlight, boolean isHighlight)
	{
		this.highlight = isHighlight;

		if (this.highlight)
		{
			((HighlightLayer) highlight).addHighlightToNode(this);
		}
		else
		{
			((HighlightLayer) highlight).removeHighlight(this);
		}

		repaint();
	}

	public void updateHighlightColor(Color color)
	{
		this.highlightColor = color;
		repaint();
	}

	protected void paintFigure(Graphics g)
	{
		label.setSize(getSize());
	}

	public class RectangleFigure extends Figure
	{
		public RectangleFigure()
		{
			super();
		}
		public RectangleFigure(Rectangle rect)
		{
			
			setBounds(rect);
		}
		
		protected void paintFigure(Graphics g)
		{
			Rectangle r = getParent().getBounds().getCopy();
			setBounds(r);
			g.fillRectangle(r);
			r.height--;
			r.width--;
			g.drawRectangle(r);
		}
	}
	
	public class RoundRectFigure extends Figure
	{
		public RoundRectFigure()
		{
			super();
		}
		public RoundRectFigure(Rectangle rect)
		{			
			setBounds(rect);
		}
		
		protected void paintFigure(Graphics g)
		{
			g.setAntialias(SWT.ON);
			Rectangle r = getParent().getBounds().getCopy();
			setBounds(r);
			label.setBounds(r);
			int rounding = smallMolecule ? 15 : 10;

			g.fillRoundRectangle(r, rounding, rounding);

			if (drawCloneMarker)
			{
				Color old = g.getBackgroundColor();
				g.setBackgroundColor(new Color(null, 220, 220, 220));
				
				double ratio = 0.3;
				
				g.setClip(new Rectangle(r.x, (int) Math.round(r.y + (r.height * (1-ratio))), r.width, (int) Math.round(r.height * ratio)));
				g.fillRoundRectangle(r, rounding, rounding);
				g.setBackgroundColor(old);
				g.setClip(r);
			}

			r.height--;
			r.width--;
			g.drawRoundRectangle(r, rounding, rounding);
		}
	}

	public class EllipseFigure extends Figure
	{
		public EllipseFigure(Rectangle rect)
		{
			setBounds(rect);
		}
		
		protected void paintFigure(Graphics g)
		{
			g.setAntialias(SWT.ON);
			Rectangle r = getParent().getBounds().getCopy();
			setBounds(r);
			g.fillOval(r);
			r.height--;
			r.width--;
			g.drawOval(r);
		}
	}
	
	public class TriangleFigure extends Figure
	{
		public TriangleFigure(Rectangle rect)
		{
			setBounds(rect);
		}
		
		protected void paintFigure(Graphics g)
		{
			g.setAntialias(SWT.ON);
			Rectangle r = getParent().getBounds().getCopy();
			setBounds(r);
			PointList points = calculateTrianglePoints(r);
			g.fillPolygon(points);
			g.drawPolygon(points);
		}
	}
	// This is outside of the inner class because it is referenced at somewhere else.
	public PointList calculateTrianglePoints(Rectangle r)
	{
		PointList points = new PointList(3);
		
		points.addPoint(new Point(r.x + (r.width-1) / 2, r.y));
		points.addPoint(new Point(r.x, r.y + r.height-1));
		points.addPoint(new Point(r.x + r.width-1, r.y + r.height-1));

		return points;
	}
	
	public class DiamondFigure extends Figure
	{
		public DiamondFigure(Rectangle rect)
		{
			setBounds(rect);
		}

		protected void paintFigure(Graphics g)
		{
			g.setAntialias(SWT.ON);
			Rectangle r = getParent().getBounds().getCopy();
			setBounds(r);
			PointList points = calculateDiamondPoints(r);
			g.fillPolygon(points);
			g.drawPolygon(points);
		}

		protected PointList calculateDiamondPoints(Rectangle r)
		{
			PointList points = new PointList(4);
			
			points.addPoint(new Point(r.x + (r.width-1)/2, r.y));
			points.addPoint(new Point(r.x + (r.width-1), r.y + (r.height-1)/2));
			points.addPoint(new Point(r.x + (r.width-1)/2, r.y + (r.height-1)));
			points.addPoint(new Point(r.x, r.y + (r.height-1)/2));

			return points;
		}
	}
}