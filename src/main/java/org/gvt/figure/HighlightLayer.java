package org.gvt.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.gvt.ChisioMain;
import org.gvt.model.ECluster;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;
import org.ivis.layout.Cluster;
import org.ivis.layout.Clustered;
import org.ivis.util.PointD;

import java.util.HashMap;

/**
 * This is the highlight layer which draws the higlights for nodes, edges and
 * compound nodes. When an object is highlighted, a highlight figure is created
 * for it, and added to layer.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class HighlightLayer extends Layer
{
	public static final String HIGHLIGHT_LAYER = "Highlight Layer";

	public HashMap<Integer, HighlightClusterFigure> clusterHighlights =
		new HashMap<Integer, HighlightClusterFigure>();

	public HashMap highlighted = new HashMap();

	private int highlightBorder = 6;

	public HighlightLayer()
	{
		setOpaque(true);
		setLayoutManager(new XYLayout());
	}

	/**
	 * Adds highlight to cluster
	 */
	public void addHighlightToCluster(Cluster cluster)
	{
		if (clusterHighlights.containsKey(cluster.getClusterID()))
		{
			remove((IFigure) clusterHighlights.get(cluster.getClusterID()));
			clusterHighlights.remove(cluster.getClusterID());
		}

		HighlightClusterFigure highlight = new HighlightClusterFigure(this, cluster);
		clusterHighlights.put(cluster.getClusterID(), highlight);
		add(highlight, ((ECluster)cluster).getPointList().getBounds().getCopy().expand(1, 1));
	}

	/**
	 * Remove highlight to cluster
	 */
	public void removeHighlightFromCluster(Cluster cluster)
	{
		if (clusterHighlights.containsKey(cluster.getClusterID()))
		{
			remove((IFigure) clusterHighlights.get(cluster.getClusterID()));
			clusterHighlights.remove(cluster.getClusterID());
		}
	}

	/**
	 * Adds highlight to given node/compound
	 */
	public void addHighlightToNode(NodeFigure object)
	{
		removeHighlight(object);
		HighlightNodeFigure highlight =	new HighlightNodeFigure(object);
		highlighted.put(object, highlight);
		add(highlight, object.getBounds().getCopy());
	}

	/**
	 * Adds highlight to given edge
	 */
	public void addHighlightToEdge(EdgeFigure object)
	{
		removeHighlight(object);
		HighlightEdgeFigure highlight = new HighlightEdgeFigure(object);
		highlighted.put(object, highlight);
		add(highlight, object.getBounds().getCopy());
	}

	/**
	 * Removes the given highlight from given object
	 */
	public void removeHighlight(IFigure object)
	{
		if (highlighted.containsKey(object))
		{
			remove((IFigure) highlighted.get(object));
			highlighted.remove(object);
		}
	}

	/**
	 * Highlight polygon for clusters
	 */
	class HighlightClusterFigure extends Figure
	{
		ECluster cluster;
		HighlightLayer layer;

		public HighlightClusterFigure(HighlightLayer layer, Cluster cluster)
		{
			this.cluster = (ECluster) cluster;
			this.layer = layer;
		}

		protected void paintFigure(Graphics graphics)
		{
			if (ChisioMain.runningOnWindows)
			{
				graphics.setAlpha(100);
			}

			PointList pl = ((ECluster)cluster).getPointList();
			Rectangle rect = pl.getBounds().getCopy();
			setBounds(rect);

			graphics.setBackgroundColor(this.cluster.getHighlightColor());
			graphics.setForegroundColor(this.cluster.getHighlightColor());
			graphics.fillPolygon(pl);

//			System.out.println(pl.getBounds().x + " " + pl.getBounds().y);
			//TODO: a counter method is used for preventing
			// infinite recursive calls. Another solution should be found
			layer.repaint(rect);
//			if(counter % 2 == 0)
//			{
//				layer.repaintHelper(rect);
//			}
//			else
//			{
//				counter++;
//			}
		}
	}
//	public void repaintHelper(Rectangle rect)
//	{
//		counter++;
//		repaint(rect);
//	}

	public static int counter = 0;

	/**
	 * Highlight for nodes/compounds
	 */
	class HighlightNodeFigure extends Figure
	{
		NodeFigure figure;

		public HighlightNodeFigure(IFigure figure)
		{
			this.figure = (NodeFigure) figure;

			if (this.figure.highlightColor == null)
			{
				this.figure.highlightColor = ChisioMain.higlightColor;
			}
		}

		protected void paintFigure(Graphics graphics)
		{
			Rectangle r = figure.getBounds().getCopy();
			r.expand(highlightBorder - 1, highlightBorder - 1);
			setBounds(r);

			if (ChisioMain.runningOnWindows)
			{
				graphics.setAlpha(150);
			}

			graphics.setBackgroundColor(figure.highlightColor);

			Rectangle rect = r.getCopy();
			rect.width = highlightBorder;
			graphics.fillRectangle(rect);

			rect.x = r.x + r.width - highlightBorder;
			graphics.fillRectangle(rect);

			rect = r.getCopy();
			rect.width -= 2 * (highlightBorder - 1);
			rect.height = highlightBorder;
			rect.x += highlightBorder - 1;
			graphics.fillRectangle(rect);

			rect.y = r.y + r.height - highlightBorder;
			graphics.fillRectangle(rect);
		}
	}

	/**
	 * Highlights for edges
	 */
	class HighlightEdgeFigure extends Figure
	{
		EdgeFigure figure;

		public HighlightEdgeFigure(IFigure figure)
		{
			this.figure = (EdgeFigure) figure;

			if (this.figure.highlightColor == null)
			{
				this.figure.highlightColor = ChisioMain.higlightColor;
			}
		}

		protected void paintFigure(Graphics graphics)
		{
			setBounds(figure.getBounds());
			graphics.setForegroundColor(figure.highlightColor);
			graphics.setLineWidth(highlightBorder);
			
			if (ChisioMain.runningOnWindows)
			{
				graphics.setAlpha(150);
			}
			
			graphics.drawPolyline(figure.getPoints().getCopy());
		}
	}

	/**
	 * Rehighlights all the highlighted objects because of the refresh need.
	 * This method is written to handle the inconsistency bug in highlights
	 * when zoom level is changed or window is scrolled.
	 */
	public void refreshHighlights()
	{
		Object[] keys = highlighted.keySet().toArray();

		for (int i = 0; i < keys.length; i++)
		{
			Object obj = keys[i];

			if (obj instanceof EdgeFigure)
			{
				EdgeFigure fig = (EdgeFigure) obj;
				fig.updateHighlight(this, fig.highlight);
			}
			else
			{
				NodeFigure fig = (NodeFigure) obj;
				fig.updateHighlight(this, fig.highlight);
			}
		}
	}
}