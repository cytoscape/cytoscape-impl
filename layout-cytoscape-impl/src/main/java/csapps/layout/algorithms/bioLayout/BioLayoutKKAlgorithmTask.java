package csapps.layout.algorithms.bioLayout;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.layout.LayoutPoint;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import csapps.layout.Profile;


public class BioLayoutKKAlgorithmTask extends BioLayoutAlgorithmTask {

	/**
	 * A small value used to avoid division by zero
	   */
	protected static double EPSILON = 0.0000001D;

	/**
	 * The total number of layout passes
	 */
	private int m_numLayoutPasses = 10;

	/**
	 * The average number of iterations per Node
	 */
	private double m_averageIterationsPerNode = 40;
	
	/**
	 * Spring strength
	 */
	private double m_nodeDistanceStrengthConstant=15.0;
	
	/** 
	 * Spring rest length"
	 */
	private double m_nodeDistanceRestLengthConstant=45.0;
	
	private double[] m_nodeDistanceSpringScalars;
	
	/**
	 * Strength of a 'disconnected' spring
	 */
	private double m_disconnectedNodeDistanceSpringStrength=0.05;
	
	/**
	 * Rest length of a 'disconnected' spring"
	 */
	private double m_disconnectedNodeDistanceSpringRestLength=2000.0;
	
	/**
	 * Strength to apply to avoid collisions
	 */
	private double m_anticollisionSpringStrength;
	
	private double[] m_anticollisionSpringScalars;

	/**
	 * Data arrays
	 */
	private double[][] m_nodeDistanceSpringRestLengths;
	private double[][] m_nodeDistanceSpringStrengths;

	/**
	 * Current layout pass
	 */
	private int m_layoutPass = 2;

	/**
	 * The number of nodes
	 */
	private int m_nodeCount;

	/**
	 * The Partition
	 */
	private LayoutPartition partition;

	/**
	 * Profile data
	 */
	Profile calculationProfile;
	Profile distanceProfile;

	private BioLayoutKKContext context;

	/**
	 * This is the constructor for the bioLayout algorithm.
	 * @param supportEdgeWeights a boolean to indicate whether we should
	 *                                                  behave as if we support weights
	 */
	public BioLayoutKKAlgorithmTask(final String name, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut, Set<Class<?>> supportedNodeAttributeTypes, Set<Class<?>> supportedEdgeAttributeTypes, List<String> initialAttributes, final BioLayoutKKContext context, final boolean supportWeights) {
		super(name, networkView, nodesToLayOut, supportedEdgeAttributeTypes, supportedEdgeAttributeTypes, initialAttributes, context.singlePartition);
		this.context = context;
		this.supportWeights = supportWeights;
		this.m_layoutPass = context.m_layoutPass;
	}

	/**
	 * Overrides for LayoutAlgorithm support
	 */
	public String getName() {
		if (supportWeights)
			return "kamada-kawai-noweight";
		else

			return "kamada-kawai";
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String toString() {
		if (supportWeights)
			return "Edge-weighted Spring Embedded";
		else

			return "Spring Embedded";
	}

	/**
	 * Sets the number of iterations
	 *
	 * @param value the number of iterations
	 */
	public void setNumberOfIterationsPerNode(int value) {
		m_averageIterationsPerNode = value;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 */
	public void setNumberOfIterationsPerNode(String value) {
		Integer val = Integer.valueOf(value);
		m_averageIterationsPerNode = val.intValue();
	}

	/**
	 * Sets the number of layout passes
	 *
	 * @param value the number of layout passes
	 */
	public void setNumberOfLayoutPasses(int value) {
		m_numLayoutPasses = value;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 */
	public void setNumberOfLayoutPasses(String value) {
		Integer val = Integer.valueOf(value);
		m_numLayoutPasses = val.intValue();
	}

	/**
	 * Sets the distance spring strength contant
	 *
	 * @param value the distance spring strength contant
	 */
	public void setDistanceSpringStrength(double value) {
		m_nodeDistanceStrengthConstant = value;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 */
	public void setDistanceSpringStrength(String value) {
		Double val = new Double(value);
		m_nodeDistanceStrengthConstant = val.doubleValue();
	}

	/**
	 * Sets the rest length constant
	 *
	 * @param value the rest length constant
	 */
	public void setDistanceRestLength(double value) {
		m_nodeDistanceRestLengthConstant = value;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 */
	public void setDistanceRestLength(String value) {
		Double val = new Double(value);
		m_nodeDistanceRestLengthConstant = val.doubleValue();
	}

	/**
	 * Sets the disconnected node distance spring strength
	 *
	 * @param value the disconnected node distance spring strength
	 */
	public void setDisconnectedSpringStrength(double value) {
		m_disconnectedNodeDistanceSpringStrength = value;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 */
	public void setDisconnectedSpringStrength(String value) {
		Double val = new Double(value);
		m_disconnectedNodeDistanceSpringStrength = val.doubleValue();
	}

	/**
	 * Sets the disconnected node sprint rest length
	 *
	 * @param value the disconnected node sprint rest length
	 */
	public void setDisconnectedRestLength(double value) {
		m_disconnectedNodeDistanceSpringRestLength = value;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 */
	public void setDisconnectedRestLength(String value) {
		Double val = new Double(value);
		m_disconnectedNodeDistanceSpringRestLength = val.doubleValue();
	}

	/**
	 * Sets the anticollision spring strength
	 *
	 * @param value the anticollision spring strength
	 */
	public void setAnticollisionSpringStrength(double value) {
		m_anticollisionSpringStrength = value;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 */
	public void setAnticollisionSpringStrength(String value) {
		Double val = new Double(value);
		m_anticollisionSpringStrength = val.doubleValue();
	}

	/**
	 * Perform a layout
	 */
	public void layoutPartion(LayoutPartition partition) {
		LayoutPoint initialLocation = null;
		this.partition = partition;
		// Initialize all of our values.  This will create
		// our internal objects and initialize them
		// local_initialize();
		m_nodeCount = partition.nodeCount();

		// Set defaults -- this is done here insted of in the constructor
		// to allow users to change m_numLayoutPasses
		m_nodeDistanceSpringScalars = new double[m_numLayoutPasses];

		for (int i = 0; i < m_numLayoutPasses; i++)
			m_nodeDistanceSpringScalars[i] = 1.0;

		m_anticollisionSpringScalars = new double[m_numLayoutPasses];
		m_anticollisionSpringScalars[0] = 0.0;

		for (int i = 1; i < m_numLayoutPasses; i++)
			m_anticollisionSpringScalars[i] = 1.0;

		System.out.println("BioLayoutKK Algorithm.  Laying out " + m_nodeCount + " nodes and "
		                   + partition.edgeCount() + " edges: ");

		/*
		        for (Iterator diter = partition.nodeIterator(); diter.hasNext(); ) {
		            System.out.println("\t"+(LayoutNode)diter.next());
		        }
		        for (Iterator diter = partition.edgeIterator(); diter.hasNext(); ) {
		            System.out.println("\t"+(LayoutEdge)diter.next());
		        }
		*/

		// Calculate a distance threshold
		double euclideanDistanceThreshold = (m_nodeCount + partition.edgeCount()) / 10;

		int numIterations = (int) ((m_nodeCount * m_averageIterationsPerNode) / m_numLayoutPasses);

		List <PartialDerivatives>partialsList = new ArrayList<PartialDerivatives>();
		double[] potentialEnergy = new double[1];

		if (potentialEnergy[0] != 0.0)
			throw new RuntimeException();

		PartialDerivatives partials;
		PartialDerivatives furthestNodePartials = null;

		m_nodeDistanceSpringRestLengths = new double[m_nodeCount][m_nodeCount];
		m_nodeDistanceSpringStrengths = new double[m_nodeCount][m_nodeCount];

		// Figure out our starting point
		initialLocation = partition.getAverageLocation();

		// Randomize our points, if any points lie
		// outside of our bounds
		if (context.randomize)
			partition.randomizeLocations();

		// Calculate our edge weights
		partition.calculateEdgeWeights();

		// Compute our distances
		if (cancelled)
			return;

		taskMonitor.setProgress(0.02);
		taskMonitor.setStatusMessage("Calculating node distances");

		int[][] nodeDistances = calculateNodeDistances();

		if (cancelled)
			return;

		taskMonitor.setProgress(0.04);
		taskMonitor.setStatusMessage("Calculating spring constants");

		calculateSpringData(nodeDistances);

		final double percentCompletedBeforePasses = 5.0d;
		final double percentCompletedAfterPass1 = 60.0d;
		final double percentCompletedAfterFinalPass = 95.0d;
		double currentProgress = percentCompletedBeforePasses;

		// Profile partialProfile = new Profile();
		// Profile springProfile = new Profile();

		// Compute our optimal lengths
		for (m_layoutPass = 0; m_layoutPass < m_numLayoutPasses; m_layoutPass++) {
			final double percentProgressPerIter;
			Profile passTimer = new Profile();
			passTimer.start();

			if (m_layoutPass == 0) {
				percentProgressPerIter = (percentCompletedAfterPass1 - percentCompletedBeforePasses) / (double) (m_nodeCount
				                         + numIterations);
			} else {
				percentProgressPerIter = (percentCompletedAfterFinalPass
				                         - percentCompletedAfterPass1) / (double) ((m_nodeCount
				                                                                   + numIterations) * (m_numLayoutPasses
				                                                                                      - 1));
			}

			// Initialize this layout pass.
			potentialEnergy[0] = 0.0;
			partialsList.clear();
			furthestNodePartials = null;

			taskMonitor.setStatusMessage("Calculating partial derivatives -- pass " + (m_layoutPass + 1)
			                      + " of " + m_numLayoutPasses);

			// partialProfile.start();

			// Calculate all node distances.  Keep track of the furthest.
			for (LayoutNode v: partition.getNodeList()) {

				if (cancelled)
					return;

				taskMonitor.setProgress(currentProgress/100.0);

				if (v.isLocked())
					continue;

				partials = new PartialDerivatives(v);
				calculatePartials(partials, null, potentialEnergy, false);
				// System.out.println(partials.printPartial()+" potentialEnergy = "+potentialEnergy[0]);
				partialsList.add(partials);

				if ((furthestNodePartials == null)
				    || (partials.euclideanDistance > furthestNodePartials.euclideanDistance)) {
					furthestNodePartials = partials;
				}

				currentProgress += percentProgressPerIter;
			}

			// partialProfile.done("Partial time for pass "+(m_layoutPass+1)+" is ");
			taskMonitor.setStatusMessage("Executing spring logic -- pass " + (m_layoutPass + 1) + " of "
			                      + m_numLayoutPasses);

			// springProfile.start();
			for (int iterations_i = 0;
			     (iterations_i < numIterations)
			     && (furthestNodePartials.euclideanDistance >= euclideanDistanceThreshold);
			     iterations_i++) {
				if (cancelled)
					return;

				taskMonitor.setProgress(currentProgress/100.0);

				furthestNodePartials = moveNode(furthestNodePartials, partialsList, potentialEnergy);
				//    		System.out.println(furthestNodePartials.printPartial()+" (furthest) potentialEnergy = "+potentialEnergy[0]);
				currentProgress += percentProgressPerIter;
			}

			// springProfile.done("Spring time for pass "+(m_layoutPass+1)+" is ");
		}

		taskMonitor.setProgress(percentCompletedAfterFinalPass/100.0);
		taskMonitor.setStatusMessage("Updating display");

		// Actually move the pieces around
		// Note that we reset our min/max values before we start this
		// so we can get an accurate min/max for paritioning
		partition.resetNodes();
		for (LayoutNode v: partition.getNodeList()) {
			partition.moveNodeToLocation(v);
		}

		// Not quite done, yet.  If we're only laying out selected nodes, we need
		// to migrate the selected nodes back to their starting position
		double xDelta = 0.0;
		double yDelta = 0.0;
		final LayoutPoint finalLocation = partition.getAverageLocation();
		xDelta = finalLocation.getX() - initialLocation.getX();
		yDelta = finalLocation.getY() - initialLocation.getY();
		for (LayoutNode v: partition.getNodeList()) {
			if (!v.isLocked()) {
				v.decrement(xDelta, yDelta);
				partition.moveNodeToLocation(v);
			}
		}
	}

	private int[][] calculateNodeDistances() {
		int[][] distances = new int[m_nodeCount][];
		LinkedList<Integer> queue = new LinkedList<Integer>();
		boolean[] completedNodes = new boolean[m_nodeCount];
		int toNode;
		int fromNode;
		int neighbor;
		int toNodeDistance;
		int neighborDistance;

		for (LayoutNode v: partition.getNodeList()) {
			fromNode = v.getIndex();

			if (distances[fromNode] == null)
				distances[fromNode] = new int[m_nodeCount];

			Arrays.fill(distances[fromNode], Integer.MAX_VALUE);
			distances[fromNode][fromNode] = 0;
			Arrays.fill(completedNodes, false);
			queue.add(Integer.valueOf(fromNode));

			while (!(queue.isEmpty())) {
				int index = ((Integer) queue.removeFirst()).intValue();

				if (completedNodes[index])
					continue;

				completedNodes[index] = true;
				toNode = index;
				toNodeDistance = distances[fromNode][index];

				if (index < fromNode) {
					// Oh boy.  We've already got every distance from/to this node.
					int distanceThroughToNode;

					for (int i = 0; i < m_nodeCount; i++) {
						if (distances[index][i] == Integer.MAX_VALUE)
							continue;

						distanceThroughToNode = toNodeDistance + distances[index][i];

						if (distanceThroughToNode <= distances[fromNode][i]) {
							// Any immediate neighbor of a node that's already been
							// calculated for that does not already have a shorter path
							// calculated from fromNode never will, and is thus complete.
							if (distances[index][i] == 1)
								completedNodes[i] = true;

							distances[fromNode][i] = distanceThroughToNode;
						}
					}

					// End for every node, update the distance using the distance
					// from toNode.  So now we don't need to put any neighbors on the
					// queue or anything, since they've already been taken care of by
					// the previous calculation.
					continue;
				} // End if toNode has already had all of its distances calculated.

				List<LayoutNode> neighborList = v.getNeighbors();
				for (LayoutNode neighbor_v: neighborList) {
					neighbor = neighbor_v.getIndex();

					// We've already done everything we can here.
					if (completedNodes[neighbor])
						continue;

					neighborDistance = distances[fromNode][neighbor];

					if ((toNodeDistance != Integer.MAX_VALUE)
					    && (neighborDistance > (toNodeDistance + 1))) {
						distances[fromNode][neighbor] = toNodeDistance + 1;
						queue.addLast(Integer.valueOf(neighbor));
					}
				}
			}
		}

		return distances;
	}

	private void calculateSpringData(int[][] nodeDistances) {
		// Set all springs to the default
		for (int node_i = 0; node_i < m_nodeCount; node_i++) {
			Arrays.fill(m_nodeDistanceSpringRestLengths[node_i],
			            m_disconnectedNodeDistanceSpringRestLength);
			Arrays.fill(m_nodeDistanceSpringStrengths[node_i],
			            m_disconnectedNodeDistanceSpringStrength);
		}

		// Calculate rest lengths and strengths based on node distance data.
		for (LayoutEdge edge: partition.getEdgeList()) {
			int node_i = edge.getSource().getIndex();
			int node_j = edge.getTarget().getIndex();
			double weight = edge.getWeight();

			// System.out.println(edge);
			if (nodeDistances[node_i][node_j] != Integer.MAX_VALUE) {
				// Compute spring rest lengths.
				m_nodeDistanceSpringRestLengths[node_i][node_j] = (m_nodeDistanceRestLengthConstant * nodeDistances[node_i][node_j]) / (weight);
				m_nodeDistanceSpringRestLengths[node_j][node_i] = m_nodeDistanceSpringRestLengths[node_i][node_j];
				// System.out.println("Setting spring ("+node_i+","+node_j+") ["+weight+"] length to "+m_nodeDistanceSpringRestLengths[node_j][node_i]);
				// Compute spring strengths.
				m_nodeDistanceSpringStrengths[node_i][node_j] = m_nodeDistanceStrengthConstant / (nodeDistances[node_i][node_j] * nodeDistances[node_i][node_j]);
				m_nodeDistanceSpringStrengths[node_j][node_i] = m_nodeDistanceSpringStrengths[node_i][node_j];

				// System.out.println("Setting spring ("+node_i+","+node_j+") strength to "+m_nodeDistanceSpringStrengths[node_j][node_i]);
			}
		}
	}

	/**
	 * Here is the code for the partial derivative solver.  Note that for clarity,
	 * it has been devided into four parts:
	 *    calculatePartials -- main algorithm, calls the other three parts
	 *    calculateSpringPartial -- computes the first part of the spring partial (partial.x, partial.y)
	 *    calculateSpringPartial3 -- computes the second part of the partial (partial.xx, partial.yy)
	 *    calculateSpringPartialCross -- computes the final part of the partial (partial.xy)
	 *    calculatePE -- computes the potential energy
	 */

	// used to calculate the x and y portions of the partial
	private double calculateSpringPartial(int pass, double distToTouch, int nodeIndex,
	                                      int otherNodeIndex, double eucDist, double value,
	                                      double radius) {
		double incrementalChange = (m_nodeDistanceSpringScalars[pass] * (m_nodeDistanceSpringStrengths[nodeIndex][otherNodeIndex] * (value
		                                                                                                                            - ((m_nodeDistanceSpringRestLengths[nodeIndex][otherNodeIndex] * value) / eucDist))));

		if (distToTouch < 0.0) {
			incrementalChange += (m_anticollisionSpringScalars[pass] * (m_anticollisionSpringStrength * (value
			                                                                                            - ((radius * value) / eucDist))));
		}

		return incrementalChange;
	}

	// used to calculate the xx and yy portions of the partial
	private double calculateSpringPartial3(int pass, double distToTouch, int nodeIndex,
	                                       int otherNodeIndex, double eucDist3, double value,
	                                       double radius) {
		double incrementalChange = (m_nodeDistanceSpringScalars[pass] * (m_nodeDistanceSpringStrengths[nodeIndex][otherNodeIndex] * (1.0
		                                                                                                                            - ((m_nodeDistanceSpringRestLengths[nodeIndex][otherNodeIndex] * value) / eucDist3))));

		if (distToTouch < 0.0) {
			incrementalChange += (m_anticollisionSpringScalars[m_layoutPass] * (m_anticollisionSpringStrength * (1.0
			                                                                                                    - ((radius * value) / eucDist3))));
		}

		return incrementalChange;
	}

	// used to calculate the xy portion of the partial
	private double calculateSpringPartialCross(int pass, double distToTouch, int nodeIndex,
	                                           int otherNodeIndex, double eucDist3, double value,
	                                           double radius) {
		double incrementalChange = (m_nodeDistanceSpringScalars[pass] * (m_nodeDistanceSpringStrengths[nodeIndex][otherNodeIndex] * ((m_nodeDistanceSpringRestLengths[nodeIndex][otherNodeIndex] * value) / eucDist3)));

		if (distToTouch < 0.0) {
			incrementalChange += ((m_anticollisionSpringScalars[m_layoutPass] * (m_anticollisionSpringStrength * radius * value)) / eucDist3);
		}

		return incrementalChange;
	}

	// Calculate the potential energy
	private double calculatePE(int pass, double distToRest, double distToTouch, int nodeIndex,
	                           int otherNodeIndex) {
		double incrementalChange = (m_nodeDistanceSpringScalars[pass] * ((m_nodeDistanceSpringStrengths[nodeIndex][otherNodeIndex] * (distToRest * distToRest)) / 2));

		if (distToTouch < 0.0) {
			incrementalChange += (m_anticollisionSpringScalars[pass] * ((m_anticollisionSpringStrength * (distToTouch * distToTouch)) / 2));
		}

		return incrementalChange;
	}

	private PartialDerivatives calculatePartials(PartialDerivatives partials, List partialsList,
	                                             double[] potentialEnergy, boolean reversed) {
		partials.reset();

		LayoutNode node = partials.node;

		// How does this ever get to be > 0?
		// Get the node size from the nodeView?
		double nodeRadius = node.getWidth() / 2;
		double nodeX = node.getX();
		double nodeY = node.getY();
		PartialDerivatives otherPartials = null;
		LayoutNode otherNode;
		double otherNodeRadius;
		PartialDerivatives furthestPartials = null;
		Iterator iterator;

		if (partialsList == null)
			iterator = partition.nodeIterator();
		else
			iterator = partialsList.iterator();

		double deltaX;
		double deltaY;
		double otherNodeX;
		double otherNodeY;
		double euclideanDistance;
		double euclideanDistanceCubed;
		double distanceFromRest;
		double distanceFromTouching;
		double incrementalChange;
		double[] xTable = { .01, .01, -.01, -.01 };
		double[] yTable = { .01, -.01, .01, -.01 };
		int offsetTable = 0;
		int nodeIndex = node.getIndex();

		while (iterator.hasNext()) {
			if (partialsList == null) {
				otherNode = (LayoutNode) iterator.next();
			} else {
				otherPartials = (PartialDerivatives) iterator.next();
				otherNode = otherPartials.node;
			}

			if (node == otherNode)
				continue;

			// How does this every get to be > 0?
			// Get the node size from the nodeView?
			otherNodeRadius = otherNode.getWidth() / 2;
			otherNodeX = otherNode.getX();
			otherNodeY = otherNode.getY();

			deltaX = nodeX - otherNodeX;
			deltaY = nodeY - otherNodeY;
			euclideanDistance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

			if (((float) euclideanDistance) < 0.0001) {
				otherNodeX = otherNodeX + xTable[offsetTable];
				otherNodeY = otherNodeY + yTable[offsetTable++];

				if (offsetTable > 3)
					offsetTable = 0;

				otherNode.setX(otherNodeX);
				otherNode.setY(otherNodeY);
				euclideanDistance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
			}

			/*
			            System.out.println("nodeX = "+nodeX);
			      System.out.println("nodeY = "+nodeY);
			      System.out.println("otherNodeX = "+otherNode.getX());
			      System.out.println("otherNodeY = "+otherNode.getY());
			*/
			int otherNodeIndex = otherNode.getIndex();
			double radius = nodeRadius + otherNodeRadius;

			euclideanDistanceCubed = euclideanDistance * euclideanDistance * euclideanDistance;
			distanceFromTouching = euclideanDistance - (nodeRadius + otherNodeRadius);
			distanceFromRest = (euclideanDistance
			                   - m_nodeDistanceSpringRestLengths[nodeIndex][otherNodeIndex]);

			// calculationProfile.start();
			if (!reversed) {
				partials.x += calculateSpringPartial(m_layoutPass, distanceFromTouching, nodeIndex,
				                                     otherNodeIndex, euclideanDistance, deltaX,
				                                     radius);
				partials.y += calculateSpringPartial(m_layoutPass, distanceFromTouching, nodeIndex,
				                                     otherNodeIndex, euclideanDistance, deltaY,
				                                     radius);
				partials.xx += calculateSpringPartial3(m_layoutPass, distanceFromTouching,
				                                       nodeIndex, otherNodeIndex,
				                                       euclideanDistanceCubed, deltaY * deltaY,
				                                       radius);
				partials.yy += calculateSpringPartial3(m_layoutPass, distanceFromTouching,
				                                       nodeIndex, otherNodeIndex,
				                                       euclideanDistanceCubed, deltaX * deltaX,
				                                       radius);
				partials.xy += calculateSpringPartialCross(m_layoutPass, distanceFromTouching,
				                                           nodeIndex, otherNodeIndex,
				                                           euclideanDistanceCubed, deltaX * deltaY,
				                                           radius);
				potentialEnergy[0] += calculatePE(m_layoutPass, distanceFromRest,
				                                  distanceFromTouching, nodeIndex, otherNodeIndex);
			}

			if (otherPartials != null) {
				if (!reversed) {
					otherPartials.x += calculateSpringPartial(m_layoutPass, distanceFromTouching,
					                                          otherNodeIndex, nodeIndex,
					                                          euclideanDistance, -deltaX, radius);
					otherPartials.y += calculateSpringPartial(m_layoutPass, distanceFromTouching,
					                                          otherNodeIndex, nodeIndex,
					                                          euclideanDistance, -deltaY, radius);
					otherPartials.xx += calculateSpringPartial3(m_layoutPass, distanceFromTouching,
					                                            otherNodeIndex, nodeIndex,
					                                            euclideanDistanceCubed,
					                                            deltaY * deltaY, radius);
					otherPartials.yy += calculateSpringPartial3(m_layoutPass, distanceFromTouching,
					                                            otherNodeIndex, nodeIndex,
					                                            euclideanDistanceCubed,
					                                            deltaX * deltaX, radius);
					otherPartials.xy += calculateSpringPartialCross(m_layoutPass,
					                                                distanceFromTouching,
					                                                nodeIndex, otherNodeIndex,
					                                                euclideanDistanceCubed,
					                                                deltaX * deltaY, radius);
					potentialEnergy[0] += calculatePE(m_layoutPass, distanceFromRest,
					                                  distanceFromTouching, nodeIndex,
					                                  otherNodeIndex);
				} else {
					otherPartials.x -= calculateSpringPartial(m_layoutPass, distanceFromTouching,
					                                          otherNodeIndex, nodeIndex,
					                                          euclideanDistance, -deltaX, radius);
					otherPartials.y -= calculateSpringPartial(m_layoutPass, distanceFromTouching,
					                                          otherNodeIndex, nodeIndex,
					                                          euclideanDistance, -deltaY, radius);
					otherPartials.xx -= calculateSpringPartial3(m_layoutPass, distanceFromTouching,
					                                            nodeIndex, otherNodeIndex,
					                                            euclideanDistanceCubed,
					                                            deltaY * deltaY, radius);
					otherPartials.yy -= calculateSpringPartial3(m_layoutPass, distanceFromTouching,
					                                            nodeIndex, otherNodeIndex,
					                                            euclideanDistanceCubed,
					                                            deltaX * deltaX, radius);
					otherPartials.xy -= calculateSpringPartialCross(m_layoutPass,
					                                                distanceFromTouching,
					                                                nodeIndex, otherNodeIndex,
					                                                euclideanDistanceCubed,
					                                                deltaX * deltaY, radius);
					potentialEnergy[0] -= calculatePE(m_layoutPass, distanceFromRest,
					                                  distanceFromTouching, nodeIndex,
					                                  otherNodeIndex);
				}

				// Update the euclidean distance
				otherPartials.euclideanDistance = Math.sqrt((otherPartials.x * otherPartials.x)
				                                            + (otherPartials.y * otherPartials.y));

				if ((furthestPartials == null)
				    || (otherPartials.euclideanDistance > furthestPartials.euclideanDistance))
					furthestPartials = otherPartials;
			}

			// calculationProfile.checkpoint();
		} // end of while loop

		if (!reversed)
			partials.euclideanDistance = Math.sqrt((partials.x * partials.x)
			                                       + (partials.y * partials.y));

		if ((furthestPartials == null)
		    || (partials.euclideanDistance > furthestPartials.euclideanDistance))
			furthestPartials = partials;

		return furthestPartials;
	}

	/**
	 * The PartialDerivatives class maintains the values for the partial derivatives
	 * as they are computed.
	 */
	private class PartialDerivatives {
		final LayoutNode node;
		double x;
		double y;
		double xx;
		double yy;
		double xy;
		double euclideanDistance;

		PartialDerivatives(LayoutNode node) {
			this.node = node;
		}

		PartialDerivatives(PartialDerivatives copyFrom) {
			this.node = copyFrom.node;
			copyFrom(copyFrom);
		}

		String printPartial() {
			String retVal = "Partials for node " + node.getIndex() + " are: " + x + "," + y + ","
			                + xx + "," + yy + "," + xy + " dist = " + euclideanDistance;

			return retVal;
		}

		void reset() {
			x = 0.0;
			y = 0.0;
			xx = 0.0;
			yy = 0.0;
			xy = 0.0;
			euclideanDistance = 0.0;
		}

		void copyFrom(PartialDerivatives otherPartialDerivatives) {
			x = otherPartialDerivatives.x;
			y = otherPartialDerivatives.y;
			xx = otherPartialDerivatives.xx;
			yy = otherPartialDerivatives.yy;
			xy = otherPartialDerivatives.xy;
			euclideanDistance = otherPartialDerivatives.euclideanDistance;
		}
	}

	private PartialDerivatives moveNode(PartialDerivatives partials, List partialsList,
	                                    double[] potentialEnergy) {
		PartialDerivatives startingPartials = new PartialDerivatives(partials);
		calculatePartials(partials, partialsList, potentialEnergy, true);

		// System.out.println(partials.printPartial()+" potentialEnergy = "+potentialEnergy[0]);
		try {
			simpleMoveNode(startingPartials);
		} catch (Exception e) {
			System.out.println(e);
		}

		return calculatePartials(partials, partialsList, potentialEnergy, false);
	}

	private void simpleMoveNode(PartialDerivatives partials) {
		LayoutNode node = partials.node;

		if (node.isLocked()) {
			return;
		}

		double denominator = ((partials.xx * partials.yy) - (partials.xy * partials.xy));

		if (((float) denominator) == 0.0) {
			return;

			// throw new RuntimeException("denominator too close to 0 for node "+node);
		}

		// System.out.println(partials.printPartial());
		double deltaX = (((-partials.x * partials.yy) - (-partials.y * partials.xy)) / denominator);
		double deltaY = (((-partials.y * partials.xx) - (-partials.x * partials.xy)) / denominator);
		/* System.out.println("Moving node "+node.getIdentifier()+" from "+node.getX()+", "+node.getY()+
		                    " to "+(node.getX()+deltaX)+", "+(node.getY()+deltaY)); */
		node.setLocation(node.getX() + deltaX, node.getY() + deltaY);
	}

	// Debugging version of inner loop for calculatePartials

}
