/**
 * @author ruschein
 */
package prefuse.util.force;

import java.util.Iterator;

//
// ************************   WARNING:  B R O K E N !!! ************************
//

/*
 * #%L
 * Cytoscape Prefuse Layout Impl (layout-prefuse-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
    
	private final StateMonitor monitor;
	
	public BackwardEulerIntegrator(StateMonitor monitor) {
		this.monitor = monitor;
	}
	
	@Override
	public void integrate(final ForceSimulator sim, final long timestep) {
		float speedLimit = sim.getSpeedLimit();
		Iterator<ForceItem> iter = sim.getItems();

		while (iter.hasNext()) {
			if (monitor.isCancelled())
				return;
			
			ForceItem item = iter.next();
			float coeff = timestep / item.mass;
			item.velocity[0] += coeff * item.force[0];
			item.velocity[1] += coeff * item.force[1];
			item.location[0] += timestep * item.velocity[0];
			item.location[1] += timestep * item.velocity[1];
			float vx = item.velocity[0];
			float vy = item.velocity[1];
			float v = (float) Math.sqrt(vx * vx + vy * vy);
			
			if (v > speedLimit) {
				item.velocity[0] = speedLimit * vx / v;
				item.velocity[1] = speedLimit * vy / v;
			}
		}
	}
}
