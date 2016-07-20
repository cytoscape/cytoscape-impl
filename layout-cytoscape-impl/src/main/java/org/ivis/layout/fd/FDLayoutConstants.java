package org.ivis.layout.fd;

/**
 * This class maintains the constants used by force-directed layouts.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class FDLayoutConstants
{
// -----------------------------------------------------------------------------
// Section: user options
// -----------------------------------------------------------------------------
	/*
	 * Options potentially exposed to the user 
	 */
	public static final int DEFAULT_EDGE_LENGTH = 50;
	public static final double DEFAULT_SPRING_STRENGTH = 0.45;
	public static final double DEFAULT_REPULSION_STRENGTH = 4500.0;
	public static final double DEFAULT_GRAVITY_STRENGTH = 0.4;
	public static final double DEFAULT_COMPOUND_GRAVITY_STRENGTH = 1.0;
	public static final double DEFAULT_GRAVITY_RANGE_FACTOR = 2.0;
	public static final double DEFAULT_COMPOUND_GRAVITY_RANGE_FACTOR = 1.5;
	public static final boolean DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION = true;
	public static final boolean DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION = true;
	
// -----------------------------------------------------------------------------
// Section: remaining constants
// -----------------------------------------------------------------------------
	
	/*
	 * Maximum amount by which a node can be moved per iteration
	 */
	public static final double MAX_NODE_DISPLACEMENT_INCREMENTAL = 100.0;
	public static final double MAX_NODE_DISPLACEMENT = 
		MAX_NODE_DISPLACEMENT_INCREMENTAL * 3;

	/*
	 * Used to determine node pairs that are too close during repulsion calcs
	 */
	public static final double MIN_REPULSION_DIST = DEFAULT_EDGE_LENGTH / 10.0;

	/**
	 * Number of iterations that should be done in between convergence checks
	 */
	public static final int CONVERGENCE_CHECK_PERIOD = 100;

	/**
	 * Ideal edge length coefficient per level for intergraph edges
	 */
	public static final double PER_LEVEL_IDEAL_EDGE_LENGTH_FACTOR = 0.1;

	/**
	 * Minimum legth of an edge
	 */
	public static final int MIN_EDGE_LENGTH = 1;
	
	/**
	 * Number of iterations that should be done in between grid calculations
	 */
	public static final int GRID_CALCULATION_CHECK_PERIOD = 10;
}