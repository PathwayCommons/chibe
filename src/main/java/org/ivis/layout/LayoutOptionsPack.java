package org.ivis.layout;

import java.io.Serializable;

import org.ivis.layout.avsdf.AVSDFConstants;
import org.ivis.layout.cise.CiSEConstants;
import org.ivis.layout.cose.CoSEConstants;
import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.layout.sgym.SgymConstants;
import org.ivis.layout.spring.SpringConstants;

/**
 * This method gathers the user-customizable layout options in a package
 *
 * @author Cihan Kucukkececi
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LayoutOptionsPack implements Serializable
{
	private static LayoutOptionsPack instance;

	private General general;
	private CoSE coSE;
	private Cluster cluster;
	private CiSE ciSE;
	private AVSDF avsdf;
	private Spring spring;
	private Sgym sgym;

	public class General
	{
		private int layoutQuality; // proof, default, draft
		private boolean animationDuringLayout; // T-F
		private boolean animationOnLayout; // T-F
		private int animationPeriod; // 0-100
		private boolean incremental; // T-F
		private boolean createBendsAsNeeded; // T-F
		private boolean uniformLeafNodeSizes; // T-F

		public int getLayoutQuality()
		{
			return layoutQuality;
		}

		public void setLayoutQuality(int quality)
		{
			this.layoutQuality = quality;
		}

		public boolean isAnimationDuringLayout()
		{
			return animationDuringLayout;
		}

		public void setAnimationDuringLayout(boolean animationDuringLayout)
		{
			this.animationDuringLayout = animationDuringLayout;
		}

		public boolean isAnimationOnLayout()
		{
			return animationOnLayout;
		}

		public void setAnimationOnLayout(boolean animationOnLayout)
		{
			this.animationOnLayout = animationOnLayout;
		}

		public int getAnimationPeriod()
		{
			return animationPeriod;
		}

		public void setAnimationPeriod(int animationPeriod)
		{
			this.animationPeriod = animationPeriod;
		}

		public boolean isIncremental()
		{
			return incremental;
		}

		public void setIncremental(boolean incremental)
		{
			this.incremental = incremental;
		}

		public boolean isCreateBendsAsNeeded()
		{
			return createBendsAsNeeded;
		}

		public void setCreateBendsAsNeeded(boolean createBendsAsNeeded)
		{
			this.createBendsAsNeeded = createBendsAsNeeded;
		}

		public boolean isUniformLeafNodeSizes()
		{
			return uniformLeafNodeSizes;
		}
		
		public void setUniformLeafNodeSizes(boolean uniformLeafNodeSizes)
		{
			this.uniformLeafNodeSizes = uniformLeafNodeSizes;
		}
	}

	public class CoSE
	{
		private int idealEdgeLength; // any positive int
		private int springStrength; // 0-100
		private int repulsionStrength; // 0-100
		private boolean smartRepulsionRangeCalc; // T-F
		private int gravityStrength; // 0-100
		private int compoundGravityStrength; // 0-100
		private int gravityRange; // 0-100
		private int compoundGravityRange; // 0-100
		private boolean smartEdgeLengthCalc; // T-F
		private boolean multiLevelScaling; // T-F
		
		public int getIdealEdgeLength()
		{
			return idealEdgeLength;
		}

		public void setIdealEdgeLength(int idealEdgeLength)
		{
			this.idealEdgeLength = idealEdgeLength;
		}

		public int getSpringStrength()
		{
			return springStrength;
		}

		public void setSpringStrength(int springStrength)
		{
			this.springStrength = springStrength;
		}

		public int getRepulsionStrength()
		{
			return repulsionStrength;
		}

		public void setRepulsionStrength(int repulsionStrength)
		{
			this.repulsionStrength = repulsionStrength;
		}

		public int getGravityStrength()
		{
			return gravityStrength;
		}

		public void setGravityStrength(int gravityStrength)
		{
			this.gravityStrength = gravityStrength;
		}

		public int getCompoundGravityStrength()
		{
			return compoundGravityStrength;
		}

		public void setCompoundGravityStrength(int compoundGravityStrength)
		{
			this.compoundGravityStrength = compoundGravityStrength;
		}
		
		public int getGravityRange()
		{
			return gravityRange;
		}

		public void setGravityRange(int gravityRange)
		{
			this.gravityRange = gravityRange;
		}

		public int getCompoundGravityRange()
		{
			return compoundGravityRange;
		}

		public void setCompoundGravityRange(int compoundGravityRange)
		{
			this.compoundGravityRange = compoundGravityRange;
		}

		public boolean isSmartEdgeLengthCalc()
		{
			return smartEdgeLengthCalc;
		}

		public void setSmartEdgeLengthCalc(boolean smartEdgeLengthCalc)
		{
			this.smartEdgeLengthCalc = smartEdgeLengthCalc;
		}

		public boolean isMultiLevelScaling()
		{
			return multiLevelScaling;
		}

		public void setMultiLevelScaling(boolean multiLevelScaling)
		{
			this.multiLevelScaling = multiLevelScaling;
		}

		public void setSmartRepulsionRangeCalc(boolean smartRepulsionRangeCalc)
		{
			this.smartRepulsionRangeCalc = smartRepulsionRangeCalc;
		}

		public boolean isSmartRepulsionRangeCalc()
		{
			return smartRepulsionRangeCalc;
		}
	}

	public class Cluster
	{
		private int idealEdgeLength; // any positive int
		private int clusterSeperation; // 0-100
		private int clusterGravityStrength; // 0-100

		public int getClusterSeperation()
		{
			return clusterSeperation;
		}

		public void setClusterSeperation(int clusterSeperation)
		{
			this.clusterSeperation = clusterSeperation;
		}

		public int getIdealEdgeLength()
		{
			return idealEdgeLength;
		}

		public void setIdealEdgeLength(int idealEdgeLength)
		{
			this.idealEdgeLength = idealEdgeLength;
		}

		public int getClusterGravityStrength()
		{
			return clusterGravityStrength;
		}

		public void setClusterGravityStrength(int clusterGravityStrength)
		{
			this.clusterGravityStrength = clusterGravityStrength;
		}
	}

	public class CiSE
	{
		int nodeSeparation;
		int desiredEdgeLength;
		int interClusterEdgeLengthFactor;
		boolean allowNodesInsideCircle;
		double maxRatioOfNodesInsideCircle;

		public int getNodeSeparation()
		{
			return nodeSeparation;
		}

		public void setNodeSeparation(int nodeSeparation)
		{
			this.nodeSeparation = nodeSeparation;
		}

		public int getDesiredEdgeLength()
		{
			return desiredEdgeLength;
		}

		public void setDesiredEdgeLength(int desiredEdgeLength)
		{
			this.desiredEdgeLength = desiredEdgeLength;
		}

		public int getInterClusterEdgeLengthFactor()
		{
			return interClusterEdgeLengthFactor;
		}

		public void setInterClusterEdgeLengthFactor(int icelf)
		{
			this.interClusterEdgeLengthFactor = icelf;
		}
		
		public boolean isAllowNodesInsideCircle()
		{
			return allowNodesInsideCircle;
		}

		public void setAllowNodesInsideCircle(boolean allowNodesInsideCircle)
		{
			this.allowNodesInsideCircle = allowNodesInsideCircle;
		}

		public double getMaxRatioOfNodesInsideCircle()
		{
			return maxRatioOfNodesInsideCircle;
		}

		public void setMaxRatioOfNodesInsideCircle(double ratio)
		{
			this.maxRatioOfNodesInsideCircle = ratio;
		}
	}

	public class AVSDF
	{
		int nodeSeparation;

		public int getNodeSeparation()
		{
			return nodeSeparation;
		}

		public void setNodeSeparation(int nodeSeparation)
		{
			this.nodeSeparation = nodeSeparation;
		}
	}

	public class Spring
	{
		int nodeDistanceRestLength;
		int disconnectedNodeDistanceSpringRestLength;

		public int getNodeDistanceRestLength()
		{
			return nodeDistanceRestLength;
		}

		public void setNodeDistanceRestLength(int nodeDistanceRestLength)
		{
			this.nodeDistanceRestLength = nodeDistanceRestLength;
		}

		public int getDisconnectedNodeDistanceSpringRestLength()
		{
			return disconnectedNodeDistanceSpringRestLength;
		}

		public void setDisconnectedNodeDistanceSpringRestLength(
			int disconnectedNodeDistanceSpringRestLength)
		{
			this.disconnectedNodeDistanceSpringRestLength
				= disconnectedNodeDistanceSpringRestLength;
		}
	}

	public class Sgym
	{
		int horizontalSpacing;
		int verticalSpacing;
		boolean vertical;

		public int getHorizontalSpacing()
		{
			return horizontalSpacing;
		}

		public void setHorizontalSpacing(int horizontalSpacing)
		{
			this.horizontalSpacing = horizontalSpacing;
		}

		public int getVerticalSpacing()
		{
			return verticalSpacing;
		}

		public void setVerticalSpacing(int verticalSpacing)
		{
			this.verticalSpacing = verticalSpacing;
		}

		public boolean isVertical()
		{
			return vertical;
		}

		public void setVertical(boolean vertical)
		{
			this.vertical = vertical;
		}
	}

	private LayoutOptionsPack()
	{
		this.general = new General();
		this.coSE = new CoSE();
		this.cluster = new Cluster();
		this.ciSE = new CiSE();
		this.avsdf = new AVSDF();
		this.spring = new Spring();
		this.sgym = new Sgym();

		setDefaultLayoutProperties();
	}

	public void setDefaultLayoutProperties()
	{
		general.setAnimationPeriod(50);
		general.setAnimationDuringLayout(
			LayoutConstants.DEFAULT_ANIMATION_DURING_LAYOUT);
		general.setAnimationOnLayout(
			LayoutConstants.DEFAULT_ANIMATION_ON_LAYOUT);
		general.setLayoutQuality(LayoutConstants.DEFAULT_QUALITY);
		general.setIncremental(LayoutConstants.DEFAULT_INCREMENTAL);
		general.setCreateBendsAsNeeded(
			LayoutConstants.DEFAULT_CREATE_BENDS_AS_NEEDED);
		general.setUniformLeafNodeSizes(
			LayoutConstants.DEFAULT_UNIFORM_LEAF_NODE_SIZES);

		coSE.setIdealEdgeLength(CoSEConstants.DEFAULT_EDGE_LENGTH);
		coSE.setSmartEdgeLengthCalc(
			CoSEConstants.DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION);
		coSE.setMultiLevelScaling(CoSEConstants.DEFAULT_USE_MULTI_LEVEL_SCALING);
		coSE.setSmartRepulsionRangeCalc(
			FDLayoutConstants.DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION);
		coSE.setSpringStrength(50);
		coSE.setRepulsionStrength(50);
		coSE.setGravityStrength(50);
		coSE.setCompoundGravityStrength(50);
		coSE.setGravityRange(50);
		coSE.setCompoundGravityRange(50);

		ciSE.setNodeSeparation(CiSEConstants.DEFAULT_NODE_SEPARATION);
		ciSE.setDesiredEdgeLength(CiSEConstants.DEFAULT_EDGE_LENGTH);
		ciSE.setInterClusterEdgeLengthFactor(50);
		ciSE.setAllowNodesInsideCircle(
			CiSEConstants.DEFAULT_ALLOW_NODES_INSIDE_CIRCLE);
		ciSE.setMaxRatioOfNodesInsideCircle(
			CiSEConstants.DEFAULT_MAX_RATIO_OF_NODES_INSIDE_CIRCLE);

		avsdf.setNodeSeparation(AVSDFConstants.DEFAULT_NODE_SEPARATION);

		cluster.setIdealEdgeLength(CoSEConstants.DEFAULT_EDGE_LENGTH);
		cluster.setClusterSeperation(50);
		cluster.setClusterGravityStrength(50);

		spring.setDisconnectedNodeDistanceSpringRestLength((int)
			SpringConstants.DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH);
		spring.setNodeDistanceRestLength((int)
			SpringConstants.DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT);

		sgym.setHorizontalSpacing(SgymConstants.DEFAULT_HORIZONTAL_SPACING);
		sgym.setVerticalSpacing(SgymConstants.DEFAULT_VERTICAL_SPACING);
		sgym.setVertical(SgymConstants.DEFAULT_VERTICAL);
	}

	public static LayoutOptionsPack getInstance()
	{
		if (instance == null)
		{
			instance = new LayoutOptionsPack();
		}

		return instance;
	}

	public Sgym getSgym()
	{
		return sgym;
	}

	public CoSE getCoSE()
	{
		return coSE;
	}

	public Spring getSpring()
	{
		return spring;
	}

	public Cluster getCluster()
	{
		return cluster;
	}

	public CiSE getCiSE()
	{
		return ciSE;
	}

	public AVSDF getAVSDF()
	{
		return avsdf;
	}

	public General getGeneral()
	{
		return general;
	}
}