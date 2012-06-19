/**
 * @author ruschein
 */
package prefuse.util.force;

import java.util.Iterator;

//
// ****************************************************   WARNING:  B R O K E N !!!
//

/**
 * Updates velocity and position data using the Backward Euler Method. This is the
 * simple and fast method, but is somewhat inaccurate and less smooth
 * than more costly approaches.
 *
 * @author Johannes Ruscheinski
 * @see RungeKuttaIntegrator
 * @see EulerIntegrator
 */
public class BackwardEulerIntegrator implements Integrator {
    
	/**
	 * @see prefuse.util.force.Integrator#integrate(prefuse.util.force.ForceSimulator, long)
	 */
	public void integrate(final ForceSimulator sim, final long timestep) {
		float speedLimit = sim.getSpeedLimit();
		Iterator iter = sim.getItems();
		while ( iter.hasNext() ) {
			ForceItem item = (ForceItem)iter.next();
			float coeff = timestep / item.mass;
			item.velocity[0] += coeff * item.force[0];
			item.velocity[1] += coeff * item.force[1];
			item.location[0] += timestep * item.velocity[0];
			item.location[1] += timestep * item.velocity[1];
			float vx = item.velocity[0];
			float vy = item.velocity[1];
			float v = (float)Math.sqrt(vx*vx + vy*vy);
			if ( v > speedLimit ) {
				item.velocity[0] = speedLimit * vx / v;
				item.velocity[1] = speedLimit * vy / v;
			}
		}
	}

} // end of class BackwardEulerIntegrator
