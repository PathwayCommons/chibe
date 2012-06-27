package org.ivis.layout.spring;

/**
 * This class maintains the constants used by Spring layout.
 *
 * @author: Selcuk Onur Sumer (modified by)
 * 
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SpringConstants
{
		public static final int DEFAULT_NUM_LAYOUT_PASSES = 4;

		public static final double DEFAULT_AVERAGE_ITERATIONS_PER_NODE = 40D;

		public static final double
			DEFAULT_NODE_DISTANCE_SPRING_SCALARS[] =
				{1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D};

		public static final double DEFAULT_NODE_DISTANCE_STRENGTH_CONSTANT = 15D;

		public static final double DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT = 60D;

		public static final double
			DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_STRENGTH = 0.05D;

		public static final double
			DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH = 250D;

		public static final double
			DEFAULT_ANTICOLLISION_SPRING_SCALARS[] =
				{0.0D, 1.0D, 2.0D, 3.0D, 4.0D, 5.0D};

		public static final double DEFAULT_ANTICOLLISION_SPRING_STRENGTH = 100D;
}
