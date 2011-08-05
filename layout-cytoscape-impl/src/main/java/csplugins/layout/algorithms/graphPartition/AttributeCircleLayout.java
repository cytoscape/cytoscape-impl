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
package csplugins.layout.algorithms.graphPartition;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.ListSingleSelection;


public class AttributeCircleLayout extends AbstractLayoutAlgorithm implements TunableValidator {
	
	@Tunable(description = "The attribute to use for the layout")
	public String attribute = CyTableEntry.NAME;
	
//	@Tunable(description="Node attribute to be use")
//	public ListSingleSelection<Integer> attrName;
	
	@Tunable(description = "Circle size")
	public double spacing = 100.0;
	@Tunable(description = "Don't partition graph before layout", groups = "Standard settings")
	public boolean singlePartition;

	private final boolean supportNodeAttributes;

	/**
	 * Creates a new AttributeCircleLayout object.
	 *
	 * @param supportAttributes  DOCUMENT ME!
	 */
	public AttributeCircleLayout(final UndoSupport undoSupport, final boolean supportNodeAttributes)
	{
		super(undoSupport, (supportNodeAttributes ? "attribute-circle": "circle"), 
		                   (supportNodeAttributes ? "Attribute Circle Layout" : "Circle Layout"),
		                   true);
		this.supportNodeAttributes = supportNodeAttributes;
	}

	/**
	 * Creates a new AttributeCircleLayout object.
	 */
	public AttributeCircleLayout(final UndoSupport undoSupport) {
		this(undoSupport,true);
	}

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		return attribute.length() > 0 && spacing > 0.0 ? ValidationState.OK : ValidationState.INVALID;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(
			new AttributeCircleLayoutTask(networkView, getName(), selectedOnly,
						      staticNodes, attribute, spacing,
						      supportNodeAttributes, singlePartition));
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
	 * Sets the attribute to use for the weights
	 *
	 * @param value the name of the attribute
	 */
	public void setLayoutAttribute(String value) {
		if (value.equals("(none)"))
			this.attribute = null;
		else
			this.attribute = value;
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
}
