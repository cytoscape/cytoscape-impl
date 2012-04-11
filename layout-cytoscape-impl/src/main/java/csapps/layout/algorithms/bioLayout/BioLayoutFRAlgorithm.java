/**
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package csapps.layout.algorithms.bioLayout;


import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;


/**
 * Lays out the nodes in a graph using a modification of the Fruchterman-Rheingold
 * algorithm.
 * <p>
 * The basic layout algorithm follows from the paper:
 * <em>"Graph Drawing by Force-directed Placement"</em>
 * by Thomas M.J. Fruchterman and Edward M. Reingold.
 * <p>
 * The algorithm has been modified to take into account edge weights, which
 * allows for its use for laying out similarity networks, which are useful
 * for biological problems.
 *
 * @author <a href="mailto:scooter@cgl.ucsf.edu">Scooter Morris</a>
 * @version 0.9
 */
public class BioLayoutFRAlgorithm extends AbstractLayoutAlgorithm<BioLayoutFRContext> {
	final boolean supportWeights; 
	
	/**
	 * This is the constructor for the bioLayout algorithm.
	 */
	public BioLayoutFRAlgorithm(boolean supportEdgeWeights) {
		
		super("fruchterman-rheingold", (supportEdgeWeights ?  "Edge-weighted Force directed (BioLayout)" : "Force directed (BioLayout)"), true);

		supportWeights = supportEdgeWeights;

	}

	@Override
	public Set<Class<?>> supportsEdgeAttributes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();
		if (!supportWeights)
			return ret;

		ret.add( Integer.class );
		ret.add( Double.class );

		return ret;
	}
	
	public TaskIterator createTaskIterator(BioLayoutFRContext context) {
		return new TaskIterator(
			new BioLayoutFRAlgorithmTask(getName(), context, supportWeights));
	}
	
	@Override
	public BioLayoutFRContext createLayoutContext() {
		return new BioLayoutFRContext(supportsSelectedOnly(), supportsNodeAttributes(), supportsEdgeAttributes());
	}
}
