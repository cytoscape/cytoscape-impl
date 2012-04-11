/**
 * Copyright (c) 2006 The Regents of the University of California.
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
 * Lays out the nodes in a graph using a modification of the Kamada-Kawai
 * algorithm.
 * <p>
 * The basic layout algorithm follows from the paper:
 * <em>"An Algorithm for Drawing General Undirected Graphs"</em>
 * by Tomihisa Kamada and Satoru Kawai.
 * <p>
 * The algorithm has been modified to take into account edge weights, which
 * allows for its use for laying out similarity networks, which are useful
 * for biological problems.
 *
 * @see "Tomihisa Kamada and Satoru Kawai: An algorithm for drawing general indirect graphs. Information Processing Letters 31(1):7-15, 1989"
 * @see "Tomihisa Kamada: On visualization of abstract objects and relations. Ph.D. dissertation, Dept. of Information Science, Univ. of Tokyo, Dec. 1988."
 *
 * @author <a href="mailto:scooter@cgl.ucsf.edu">Scooter Morris</a>
 * @version 0.9
 */
public class BioLayoutKKAlgorithm  extends AbstractLayoutAlgorithm<BioLayoutKKContext> {
	private final boolean supportWeights; 

	public BioLayoutKKAlgorithm(boolean supportEdgeWeights) {
		super((supportEdgeWeights ?  "kamada-kawai" : "kamada-kawai-noweight"),
		      (supportEdgeWeights ?  "Edge-weighted Spring Embedded" : "Spring Embedded"),
		      true);
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

	public TaskIterator createTaskIterator(BioLayoutKKContext context) {
		return new TaskIterator(
			new BioLayoutKKAlgorithmTask(getName(), context, supportWeights));
	}
	
	@Override
	public BioLayoutKKContext createLayoutContext() {
		return new BioLayoutKKContext(supportsSelectedOnly(), supportsNodeAttributes(), supportsEdgeAttributes());
	}
}
