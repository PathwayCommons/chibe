package org.gvt.layout;

import java.io.Serializable;

/**
 * This method gathers the user-customizable layout options in a package
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LayoutOptionsPack implements Serializable
{
	private static LayoutOptionsPack instance;

	private General general;
	private CoSE coSE;
	private Spring spring;

	public class General
	{
		private boolean animationDuringLayout; // T-F
		private boolean animationOnLayout; // T-F
		private int animationPeriod; // 0-100
		private int layoutQuality; // proof, default, draft
		private boolean incremental; // T-F
		private boolean createBendsAsNeeded; // T-F
		
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
		
		public int getLayoutQuality()
		{
			return layoutQuality;
		}

		public void setLayoutQuality(int quality)
		{
			this.layoutQuality = quality;
		}
		
		public boolean isIncremental()
		{
			return incremental;
		}

		public void setIncremental(boolean incremental)
		{
			this.incremental = incremental;
		}
		
		public boolean isCreateBendsAsNeeded() {
			return createBendsAsNeeded;
		}
		
		public void setCreateBendsAsNeeded(boolean createBendsAsNeeded) {
			this.createBendsAsNeeded = createBendsAsNeeded;
		}

	}

	public class CoSE
	{
		private int idealEdgeLength; // any positive int
		private int springStrength; // 0-100
		private int repulsionStrength; // 0-100
		private int gravityStrength; // 0-100
		private int compoundGravityStrength; // 0-100

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

	private LayoutOptionsPack()
	{
		this.general = new General();
		this.coSE = new CoSE();
		this.spring = new Spring();

		setDefaultLayoutProperties();
	}

	public void setDefaultLayoutProperties()
	{
		general.setAnimationPeriod(50);
		general.setAnimationDuringLayout(
			AbstractLayout.DEFAULT_ANIMATION_DURING_LAYOUT);
		general.setAnimationOnLayout(
			AbstractLayout.DEFAULT_ANIMATION_ON_LAYOUT);
		general.setLayoutQuality(AbstractLayout.DEFAULT_QUALITY);
		general.setIncremental(AbstractLayout.DEFAULT_INCREMENTAL);
		general.setCreateBendsAsNeeded(
				AbstractLayout.DEFAULT_CREATE_BENDS_AS_NEEDED);
		
		coSE.setIdealEdgeLength(CoSELayout.DEFAULT_EDGE_LENGTH);
		coSE.setSpringStrength(50);
		coSE.setRepulsionStrength(50);//35
		coSE.setGravityStrength(50);
		coSE.setCompoundGravityStrength(50);//10

		spring.setDisconnectedNodeDistanceSpringRestLength((int)
			SpringLayout.DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH);
		spring.setNodeDistanceRestLength((int)
			SpringLayout.DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT);
	}

	public static LayoutOptionsPack getInstance()
	{
		if (instance == null)
		{
			instance = new LayoutOptionsPack();
		}
		
		return instance;
	}

	public CoSE getCoSE()
	{
		return coSE;
	}

	public Spring getSpring()
	{
		return spring;
	}

	public General getGeneral()
	{
		return general;
	}
}