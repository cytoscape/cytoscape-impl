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
package csapps.layout.algorithms.graphPartition;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;


public class AttributeCircleLayout extends AbstractLayoutAlgorithm<AttributeCircleLayoutContext> {
	private final boolean supportNodeAttributes;

	/**
	 * Creates a new AttributeCircleLayout object.
	 *
	 * @param supportAttributes  DOCUMENT ME!
	 */
	public AttributeCircleLayout(final boolean supportNodeAttributes)
	{
		super((supportNodeAttributes ? "attribute-circle": "circle"), 
		      (supportNodeAttributes ? "Attribute Circle Layout" : "Circle Layout"),
		      true);
		this.supportNodeAttributes = supportNodeAttributes;
	}

	/**
	 * Creates a new AttributeCircleLayout object.
	 */
	public AttributeCircleLayout() {
		this(true);
	}

	@Override
	public TaskIterator createTaskIterator(AttributeCircleLayoutContext context) {
		return new TaskIterator(new AttributeCircleLayoutTask(getName(), context));
	}

	// Required methods for AbstactLayout
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Set<Class<?>> supportsNodeAttributes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();
		if (!supportNodeAttributes)
			return ret;

		ret.add(Integer.class);
		ret.add(Double.class);
		ret.add(String.class);
		ret.add(Boolean.class);
		ret.add(List.class);
		ret.add(Map.class);

		return ret;
	}

	/**
	 *
	 * We don't have any special widgets
	 *
	 * @returns List of our "special" weights
	 */
	public List<String> getInitialAttributeList() {
		ArrayList<String> attList = new ArrayList<String>();
		attList.add("(none)");

		return attList;
	}
	
	@Override
	public AttributeCircleLayoutContext createLayoutContext() {
		return new AttributeCircleLayoutContext(supportsSelectedOnly(), supportsNodeAttributes(), supportsEdgeAttributes());
	}
}
