/* %% Ignore-License */
/*
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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class AttributeGridLayout extends AbstractLayoutAlgorithm {
	
	private final boolean supportNodeAttributes;

	public AttributeGridLayout(final boolean supportNodeAttributes, final UndoSupport undoSupport) {
		super((supportNodeAttributes ? "attribute-grid" : "grid"),
				(supportNodeAttributes ? "Attribute Grid Layout" : "Grid Layout"),
				undoSupport);
		this.supportNodeAttributes = supportNodeAttributes;
	}

	public AttributeGridLayout(final UndoSupport undoSupport) {
		this(true, undoSupport);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut,
			String attrName) {
		return new TaskIterator(new AttributeGridLayoutTask(toString(), networkView, nodesToLayOut,
				(AttributeGridLayoutContext) context, attrName, undoSupport));
	}

	@Override
	public Set<Class<?>> getSupportedNodeAttributeTypes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();
		if (!supportNodeAttributes)
			return ret;

		ret.add(Integer.class);
		ret.add(Double.class);
		ret.add(String.class);
		ret.add(Boolean.class);
		ret.add(List.class);

		return ret;
	}

	@Override
	public AttributeGridLayoutContext createLayoutContext() {
		return new AttributeGridLayoutContext();
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
