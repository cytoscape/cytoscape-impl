package csapps.layout.algorithms.bioLayout;


import java.util.ArrayList;

import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.layout.LayoutPoint;


public class BioLayoutFRAlgorithmTask extends BioLayoutAlgorithmTask {
	private double attraction_constant;
	private double repulsion_constant;
	private double gravity_constant;

	/**
	 * maxDistance is the actual calculated distance
	 * beyond which repulsive forces will not operate.
	 * This value takes into account max_distance_factor,
	 * but also the size of the nodes in comparison to
	 * the size of the graph.
	 */
	private double maxDistance;

	/**
	 * This limits the velocity to no more than 1/maxVelocity_divisor
	 * of the width or height per iteration
	 */
	private double maxVelocity_divisor = 25;
	private double maxVelocity;

	/**
	 * This ArrayList is used to calculate the slope of the magnitude
	 * of the displacement.  When the slope is (approximately) 0, we're
	 * done.
	 */
	private ArrayList<Double> displacementArray;

	/**
	 * The partition we're laying out
	 */
	private LayoutPartition partition;

	/**
	 * The width and height of the layout
	 */
	private double width = 0;
	private double height = 0;

	private BioLayoutFRContext context;

	/**
	 * Profile data -- not used, for now
	Profile initProfile;
	Profile iterProfile;
	Profile repulseProfile;
	Profile attractProfile;
	Profile updateProfile;
	 */

	public BioLayoutFRAlgorithmTask(final String name, final BioLayoutFRContext context, final boolean supportWeights) {		
		super(name, context, context.singlePartition);
		this.context = context;

		this.supportWeights =supportWeights;
		
		displacementArray = new ArrayList<Double>(100);
	}

	/**
	 * Required methods (and overrides) for AbstractLayoutAlgorithm
	 */

	/**
	 * Return the "name" of this algorithm.  This is meant
	 * to be used by programs for deciding which algorithm to
	 * use.  toString() should be used for the human-readable
	 * name.
	 *
	 * @return the algorithm name
	 */
	public String getName() {
		return "fruchterman-rheingold";
	}

	/**
	 * Return the "title" of this algorithm.  This is meant
	 * to be used for titles and labels that represent this
	 * algorithm.
	 *
	 * @return the human-readable algorithm name
	 */
	public String toString() {
		if (supportWeights)
			return "Edge-weighted Force directed (BioLayout)";
		else

			return "Force directed (BioLayout)";
	}

	/**
	 * Perform a layout
	 */
	public void layoutPartion(LayoutPartition partition) {
		this.partition = partition;

		LayoutPoint initialLocation = null;

		/* Get all of our profiles */
		/*
		        initProfile = new Profile();
		        iterProfile = new Profile();
		        repulseProfile = new Profile();
		        attractProfile = new Profile();
		        updateProfile = new Profile();

		        initProfile.start();
		*/

		// Calculate a bounded rectangle for our
		// layout.  This is roughly the area of all
		// nodes * 2
		calculateSize();

		System.out.println("BioLayoutFR Algorithm.  Laying out " + partition.nodeCount()
		                   + " nodes and " + partition.edgeCount() + " edges: ");

		// Initialize our temperature
		double temp;

		if (context.temperature == 0) {
			temp = Math.sqrt(this.width*this.height)/2;
		} else {
			temp = Math.sqrt(this.width*this.height) * this.context.temperature/100;
		}

		// Figure out our starting point
		if (selectedOnly)
			initialLocation = partition.getAverageLocation();

		// Randomize our points, if any points lie
		// outside of our bounds
		if (context.randomize)
			partition.randomizeLocations();

		// Calculate our force constant
		calculateForces();

		// Calculate our edge weights
		partition.calculateEdgeWeights();
		// initProfile.done("Initialization completed in ");
		taskMonitor.setStatusMessage("Calculating new node positions");
		taskMonitor.setProgress(0.01);

		// Main algorithm
		// iterProfile.start();
		int iteration = 0;

		for (iteration = 0; (iteration < context.nIterations) && !cancelled; iteration++) {
			if ((temp = doOneIteration(iteration, temp)) == 0)
				break;

			if (debug || ((context.update_iterations > 0) && ((iteration % context.update_iterations) == 0))) {
				if (iteration > 0) {
					// Actually move the pieces around
					for (LayoutNode v: partition.getNodeList()) {
						// if this is locked, the move just resets X and Y
						v.moveToLocation();

					}
					// This fires events to presentation layer.
					networkView.updateView();
				}

				if (debug) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}

			taskMonitor.setStatusMessage("Calculating new node positions - " + iteration);
			taskMonitor.setProgress(iteration / context.nIterations);
		}

		// iterProfile.done("Iterations complete in ");
		// System.out.println("Attraction calculation portion of iterations took "+attractProfile.getTotalTime()+"ms");
		// System.out.println("Repulsion calculation portion of iterations took "+repulseProfile.getTotalTime()+"ms");
		// System.out.println("Update portion of iterations took "+updateProfile.getTotalTime()+"ms");
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
		if (selectedOnly) {
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

		System.out.println("Layout complete after " + iteration + " iterations");
	}

	/**
	 * This executes a single iteration of the FR algorithm.
	 *
	 * @param iteration The current interation.
	 * @param temp The current temperature factor.
	 * @return an updated temperature factor.
	 */
	public double doOneIteration(int iteration, double temp) {
		double xAverage = 0;
		double yAverage = 0;

		// repulseProfile.start();
		// Calculate repulsive forces
		for (LayoutNode v: partition.getNodeList()) {
			if (!v.isLocked()) {
				xAverage += v.getX()/partition.nodeCount();
				yAverage += v.getY()/partition.nodeCount();
			}
		}

		for (LayoutNode v: partition.getNodeList()) {
			if (!v.isLocked()) {
				calculateRepulsion(v);
				if (gravity_constant != 0)
					calculateGravity(v,xAverage,yAverage);
			}
		}

		// repulseProfile.checkpoint();

		// Dump the current displacements
		// print_disp();

		// attractProfile.start();
		// Calculate attractive forces

/// for e in E do begin
		for (LayoutEdge e: partition.getEdgeList()) {
			calculateAttraction(e);
		}
/// end

		// attractProfile.checkpoint();

		// Dump the current displacements
		// print_disp();

		// Dampen & update
		double xDispTotal = 0;
		double yDispTotal = 0;
		// updateProfile.start();

/// for v in V do begin
		for (LayoutNode v: partition.getNodeList()) {
			if (v.isLocked())
				continue;

			calculatePosition(v, temp);

			xDispTotal += Math.abs(v.getXDisp());
			yDispTotal += Math.abs(v.getYDisp());
		}
/// end

		// Translate back to the middle (or to the starting point,
		// if we're dealing with a selected group
		if (!selectedOnly) {
			for (LayoutNode v: partition.getNodeList()) {
				v.decrement(xAverage - (width / 2), yAverage - (height / 2));
			}
		}

		// updateProfile.checkpoint();

		// Test our total x and y displacement to see if we've
		// hit our completion criteria
		if (complete(xDispTotal, yDispTotal))
			return 0;

		// cool
// t := cool(t)
		return cool(temp, iteration);
	}

	/**
	 * calculate the slope of the total displacement over the last 10 iterations.  If its positive or 0
	 * we're done.
	 *
	 */
	private boolean complete(double xDisp, double yDisp) {
		Double disp = new Double(Math.sqrt((xDisp * xDisp) + (yDisp * yDisp)));

		displacementArray.add(disp);

		Object[] dispArray = displacementArray.toArray();

		if (dispArray.length < 99)
			return false;

		double averageSlope = 0;
		double averageValue = ((Double) dispArray[0]).doubleValue() / dispArray.length;

		for (int i = 1; i < dispArray.length; i++) {
			averageSlope += ((((Double) dispArray[i]).doubleValue()
			                 - ((Double) dispArray[i - 1]).doubleValue()) / dispArray.length);
			averageValue += (((Double) dispArray[i]).doubleValue() / dispArray.length);
		}

		// System.out.println("Total displacement = "+disp.doubleValue()+" Average slope = "+averageSlope);
		// 5% a reasonable criteria?
		// if (Math.abs(averageSlope) < Math.abs(averageValue)*.001) return true;
		if (Math.abs(averageSlope) < .001)
			return true;

		if (displacementArray.size() > 99)
			displacementArray.remove(0);

		return false;
	}

	/**
	 * calculate the repulsive forces and offsets for
	 * each vertex.
	 *
	 * @param v LayoutNode we're calculating repulsive forces for
	 */
	private void calculateRepulsion(LayoutNode v) {
/// v.disp := 0;
		v.setDisp(0, 0);

		double radius = v.getWidth() / 2;

/// for u in V do
		for (LayoutNode u: partition.getNodeList()) {
			double dx = v.getX() - u.getX();
			double dy = v.getY() - u.getY();

/// if (u # v) then begin
			if (v == u)
				continue;

			// Get the 
			// double xSign = Math.signum(v.getX() - u.getX());
			// double ySign = Math.signum(v.getY() - u.getY());

/// delta := v.pos - u.pos
			// Get our euclidean distance
			double deltaDistance = v.distance(u);

			if (deltaDistance == 0.0)
				deltaDistance = EPSILON;

			double fr = forceR(repulsion_constant, deltaDistance);

			// If its too close, increase the force by a constant
			if (deltaDistance < (radius + (u.getWidth() / 2))) {
				// System.out.println("Applying conflict_avoidance force: "+conflict_avoidance);
				fr += context.conflict_avoidance;
			}

			if (Double.isNaN(fr)) {
				fr = 500;
			}

			/*
			            System.out.println("Repulsive force between "+v.getIdentifier()
			                             +" and "+u.getIdentifier()+" is "+fr);
			            System.out.println("   distance = "+deltaDistance);
			            System.out.println("   incrementing "+v.getIdentifier()+" by ("+
		                                     fr+", "+fr+")");
			*/

			// Adjust the displacement.  In the case of doing selectedOnly,
			// we increase the force to enhance the discrimination power.
			// Also note that we only update the displacement of the movable
			// node since the other node won't move anyways.
/// v.disp := v.disp + (delta/abs(delta)) * fr(abs(delta))
			double xVector = dx*fr/deltaDistance;
			double yVector = dy*fr/deltaDistance;
			if (v.isLocked()) {
				return; // shouldn't happen
			} else if (u.isLocked()) {
				v.incrementDisp(xVector * 2, yVector * 2);
			} else {
				v.incrementDisp(xVector, yVector);
			}
		}
	}

	/**
	 * calculate the attractive forces and offsets for
	 * each vertex based on their connecting edges and the
	 * corresponding edge weights.
	 *
	 * @param e Edge we're calculating attractive forces for
	 */
	private void calculateAttraction(LayoutEdge e) {
		LayoutNode v = e.getSource();
		LayoutNode u = e.getTarget();
		double dx = v.getX() - u.getX();
		double dy = v.getY() - u.getY();

/// delta := e.v.pos - e.u.pos
		double deltaDistance = v.distance(u);

		double fa = forceA(attraction_constant, deltaDistance, e.getWeight());

		if (Double.isNaN(fa)) {
			fa = EPSILON;
		}

		// Adjust the displacement.  In the case of doing selectedOnly,
		// we increase the force to enhance the discrimination power.
		// Also note that we only update the displacement of the movable
		// node since the other node won't move anyways.

/// e.v.disp := e.v.disp - (delta/abs(delta)) * fa(abs(delta))
/// e.u.disp := e.u.disp + (delta/abs(delta)) * fa(abs(delta))
		double xVector = dx*fa;
		double yVector = dy*fa;
		if (u.isLocked() && v.isLocked()) {
			return; // shouldn't happen
		} else if (u.isLocked()) {
			v.decrementDisp(xVector * 2, yVector * 2);
		} else if (v.isLocked()) {
			u.incrementDisp(xVector * 2, yVector * 2);
		} else {
			v.decrementDisp(xVector, yVector);
			u.incrementDisp(xVector, yVector);
		}
	}

	/**
	 * Calculate the gravity (pull towards the center) force.
	 *
	 * @param v the node we're pulling
	 * @param xAverage the X portion of the location that's pulling us
	 * @param yAverage the Y portion of the location that's pulling us
	 */
	private void calculateGravity(LayoutNode v,double xAverage, double yAverage)
	{
		double dx = v.getX() - xAverage;
		double dy = v.getY() - yAverage;
		double distance = Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));
		//double theta = Math.atan(dy/dx);
		//double xSign = Math.signum(dx);
		//double ySign = Math.signum(dy);
		if(distance == 0) distance = EPSILON;
		double phi = (1 +v.getDegree())/3;
		double force = gravity_constant*distance*phi;
		double xVector = dx*force;
		double yVector = dy*force;
		if (v.isLocked()) {
			return; 
		}// shouldn't happen
		
		else {
			// System.out.println("Gravity adjustment = "+xVector+", "+yVector);
			v.decrementDisp( xVector, yVector);
		}
	}

	/**
	 * Calculate and update the position to move a vertex.
	 * This routine also handles limiting the velocity and
	 * doing the bounds checking to keep the vertices within
	 * the graphics area.
	 *
	 * @param v LayoutNode we're moving
	 * @param temp double representing the current temperature
	 */
/// v.pos := v.pos + (v.disp/|v.disp|) * min (v.disp, t);
	private void calculatePosition(LayoutNode v, double temp) {
		double deltaDistance = v.distance(v.getXDisp(), v.getYDisp());

		double newXDisp = v.getXDisp() / deltaDistance * Math.min(deltaDistance, temp);

		if (Double.isNaN(newXDisp)) {
			newXDisp = 0;
		}

		double newYDisp = v.getYDisp() / deltaDistance * Math.min(deltaDistance, temp);

		if (Double.isNaN(newYDisp)) {
			newYDisp = 0;
		}
		v.increment(newXDisp, newYDisp);

/// v.pos.x := min(W/2, max(-W/2, v.pos.x));
/// v.pos.y := min(L/2, max(-L/2, v.pos.y));
	}

	/**
	 * Cools the current temperature
	 *
	 * @param temp the current temperature
	 * @param iteration the iteration number
	 * @return the new temperature
	 */
	private double cool(double temp, int iteration) {
		temp *= (1.0 - ((double)iteration / (double)context.nIterations));

		return temp;
	}

	/**
	 * Calculate the width and height of the new graph.  If the graph already has been laid
	 * out, then the width and height should be resonable, so use those.  Otherwise, calculate
	 * a width and height based on the area covered by the existing graph.
	 */
	private void calculateSize() {
		// double spreadFactor = Math.max(spread_factor, edgeList.length/nodeList.length);
		// LayoutNode v0 = (LayoutNode)nodeList.get(0); // Get the first vertex to get to the class variables
		double spreadFactor = context.spread_factor;
		double averageWidth = partition.getWidth() / partition.nodeCount();
		double averageHeight = partition.getHeight() / partition.nodeCount();
		double current_area = (partition.getMaxX() - partition.getMinX()) * (partition.getMaxY()
		                                                                    - partition.getMinY());
		double node_area = partition.getWidth() * partition.getHeight();

		if (selectedOnly || (current_area > node_area)) {
			this.width = (partition.getMaxX() - partition.getMinX()) * spreadFactor;
			this.height = (partition.getMaxY() - partition.getMinY()) * spreadFactor;
			// make it square
			this.width = Math.max(this.width, this.height);
			this.height = this.width;
		} else {
			this.width = Math.sqrt(node_area) * spreadFactor;
			this.height = Math.sqrt(node_area) * spreadFactor;

			// System.out.println("spreadFactor = "+spreadFactor);
		}

		this.maxVelocity = Math.max(Math.max(averageWidth * 2, averageHeight * 2),
		                            Math.max(width, height) / maxVelocity_divisor);
		this.maxDistance = Math.max(Math.max(averageWidth * 10, averageHeight * 10),
		                            Math.min(width, height) * context.max_distance_factor / 100);

		        System.out.println("Size: "+width+" x "+height);
		        System.out.println("maxDistance = "+maxDistance);
		        System.out.println("maxVelocity = "+maxVelocity);
		/*
		*/
	}

	/**
	 * Calculate the attraction and repulsion constants.
	 */
	private void calculateForces() {
		double force = Math.sqrt((this.height * this.width) / partition.nodeCount());
		attraction_constant = force * context.attraction_multiplier;
		repulsion_constant = force * context.repulsion_multiplier;
		gravity_constant = context.gravity_multiplier;

/*
		        System.out.println("attraction_constant = "+attraction_constant
		                        +", repulsion_constant = "+repulsion_constant
						                +", gravity_constant = "+gravity_constant);
*/
	}

	/**
	 * Calculate the repulsive force
	 *
	 * @param k the repulsion constant
	 * @param distance the distance between the vertices
	 * @return the repulsive force
	 */
/// fr(z) := begin return k*k/z end;
	private double forceR(double k, double distance) {
		// We want to bound the distance over which
		// the repulsive force acts
		// Should we do this??
		if (distance > maxDistance)
			return 0;

		return ((k * k) / distance);
	}

	/**
	 * Calculate the attractive force
	 *
	 * @param k the attraction constant
	 * @param distance the distance between the vertices
	 * @param weight the edge weight
	 * @return the attractive force
	 */
/// fa(z) := begin return z*z/k end;
	private double forceA(double k, double distance, double weight) {
		return ((distance / k) * weight);
	}

}
