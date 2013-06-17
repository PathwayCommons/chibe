package org.patika.mada.util;

import org.eclipse.swt.graphics.Color;
import org.gvt.model.biopaxl2.Complex;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.Iterator;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class CausativePath extends Path
{
	//==============================================================================================
	// Section: Methods
	//==============================================================================================

	public void associateInferenceData()
	{
		Iterator<GraphObject> goIter = getObjects().iterator();

		Node start = (Node) goIter.next();

		assert start.hasSignificantExperimentalChange(ExperimentData.EXPRESSION_DATA);

		int sign = start.getExperimentData(ExperimentData.EXPRESSION_DATA).getSign();

		start.putLabel(INFERENCE_DATA_KEY, new InferenceData(true, sign > 0));


		while (goIter.hasNext())
		{
			GraphObject go = goIter.next();

			if (go instanceof Node)
			{
				Node node = (Node) go;

				boolean significant = node.hasSignificantExperimentalChange(
					ExperimentData.EXPRESSION_DATA);

				node.putLabel(INFERENCE_DATA_KEY, new InferenceData(significant, sign > 0));

				if (node instanceof Complex)
				{
					for (Node child : node.getChildren())
					{
						if (!this.contains(child))
						{
							child.putLabel(INFERENCE_DATA_KEY,
								new InferenceData(significant, sign > 0));
						}
					}
				}
			}
			else
			{
				Edge edge = (Edge) go;
				sign *= edge.getSign();
			}
		}
	}

	public void removeInferenceData()
	{
		for (Node node : getNodes())
		{
			node.removeLabel(INFERENCE_DATA_KEY);

			if (node instanceof Complex)
			{
				for (Node child : node.getChildren())
				{
					if (!this.contains(child))
					{
						child.removeLabel(INFERENCE_DATA_KEY);
					}
				}
			}
		}
	}

	private class InferenceData implements Representable
	{
		private boolean significant;
		private boolean upregulated;

		private InferenceData(boolean significant, boolean upregulated)
		{
			this.significant = significant;
			this.upregulated = upregulated;
		}

		public boolean alterNodeColor()
		{
			return true;
		}

		public boolean alterToolTipText()
		{
			return false;
		}

		public boolean alterTextColor()
		{
			return true;
		}

		public Color getNodeColor()
		{
			return significant ? 
				upregulated ? SIGNIFICANT_UPREGULATION_COLOR : SIGNIFICANT_DOWNREGULATION_COLOR :
				upregulated ? INFERRED_UPREGULATION_COLOR : INFERRED_DOWNREGULATION_COLOR;
		}

		public String getToolTipText()
		{
			return null;
		}

		public Color getTextColor()
		{
			return significant ? SIGNIFICANT_TEXT_COLOR : INFERRED_TEXT_COLOR;
		}
	}

	//==============================================================================================
	// Section: Class constants
	//==============================================================================================

	private static final Color SIGNIFICANT_TEXT_COLOR = new Color(null, 255, 255, 255);
	private static final Color SIGNIFICANT_UPREGULATION_COLOR = new Color(null, 200, 20, 20);
	private static final Color SIGNIFICANT_DOWNREGULATION_COLOR = new Color(null, 20, 20, 200);
	private static final Color INFERRED_TEXT_COLOR = new Color(null, 0, 0, 0);
	private static final Color INFERRED_UPREGULATION_COLOR = new Color(null, 255, 200, 200);
	private static final Color INFERRED_DOWNREGULATION_COLOR = new Color(null, 200, 200, 255);

	public static final String INFERENCE_DATA_KEY = "INFERENCE_DATA_KEY";
}