package org.ivis.layout;

/**
 * This class maintains the constants used by the layout package.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LayoutConstants
{
// -----------------------------------------------------------------------------
// Section: General user options
// -----------------------------------------------------------------------------
	/**
	 * Layout Quality
	 */
	public static final int PROOF_QUALITY = 0;
	public static final int DEFAULT_QUALITY = 1;
	public static final int DRAFT_QUALITY = 2;

	/**
	 * Default parameters
	 */
	public static final boolean DEFAULT_CREATE_BENDS_AS_NEEDED = false;
	public static final boolean DEFAULT_INCREMENTAL = false;
	public static final boolean DEFAULT_ANIMATION_ON_LAYOUT = true;
	public static final boolean DEFAULT_ANIMATION_DURING_LAYOUT = false;
	public static final int DEFAULT_ANIMATION_PERIOD = 50;
	public static final boolean DEFAULT_UNIFORM_LEAF_NODE_SIZES = false;
	/**
	 * Testing parameters
	 */
	public static final boolean TESTS_ACTIVE = false;
// -----------------------------------------------------------------------------
// Section: General other constants
// -----------------------------------------------------------------------------
	/*
	 * Margins of a graph to be applied on bouding rectangle of its contents. We
	 * assume margins on all four sides to be uniform.
	 */
	public static int DEFAULT_GRAPH_MARGIN = 10;

	/*
	 * The height of the label of a compound. We assume the label of a compound
	 * node is placed at the bottom with a dynamic width same as the compound
	 * itself.
	 */
	public static final int LABEL_HEIGHT = 20;

	/*
	 * Additional margins that we maintain as safety buffer for node-node
	 * overlaps. Compound node labels as well as graph margins are handled
	 * separately!
	 */
	public static final int COMPOUND_NODE_MARGIN  = 5;

	/*
	 * Default dimension of a non-compound node.
	 */
	public static final int SIMPLE_NODE_SIZE = 40;	

	/*
	 * Default dimension of a non-compound node.
	 */
	public static final int SIMPLE_NODE_HALF_SIZE = SIMPLE_NODE_SIZE / 2;	

	/*
	 * Empty compound node size. When a compound node is empty, its both
	 * dimensions should be of this value.
	 */
	public static final int EMPTY_COMPOUND_NODE_SIZE = 40;	

	/*
	 * Minimum length that an edge should take during layout
	 */
	public static final int MIN_EDGE_LENGTH = 1;

	/*
	 * World boundaries that layout operates on
	 */
	public static final int WORLD_BOUNDARY = 1000000;

	/*
	 * World boundaries that random positioning can be performed with
	 */
	public static final int INITIAL_WORLD_BOUNDARY = WORLD_BOUNDARY / 1000;

	/*
	 * Coordinates of the world center
	 */
	public static final int WORLD_CENTER_X = 1200;
	public static final int WORLD_CENTER_Y = 900;

	/*
	 * Margins for cluster boundaries
	 */
	public static final int CLUSTER_BOUNDARY_MARGIN = 4;
	
	// Test variables
	public static long time = 0;
	public static int iterations = 0;
}