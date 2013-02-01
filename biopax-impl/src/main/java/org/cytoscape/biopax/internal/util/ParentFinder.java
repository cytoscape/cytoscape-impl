package org.cytoscape.biopax.internal.util;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.util.Filter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

/**
 * 
 * 
 * @author rodch
 *
 */
public final class ParentFinder extends AbstractTraverser {
	private BioPAXElement query;
	private boolean found;
	
	public ParentFinder(EditorMap editorMap) {
		super(editorMap, new Filter<PropertyEditor>() {
			public boolean filter(PropertyEditor editor) {
				return !editor.getProperty().equals("nextStep");
			}
		});
	}

	@Override
	protected void visit(Object value, BioPAXElement parent, Model model,
			PropertyEditor editor) {
		// skip if already found or it's not a object property
		if(!found && value instanceof BioPAXElement) {
			// TODO: Verify this change
			if(getVisited().contains(query)) { // it is added there right before the visit method call
				found = true;
			} else {
				// continue into the value's values:
				traverse((BioPAXElement)value, model);
			}
		}
	}

	/**
	 * True if the 'parent' element is in fact contains 'child'
	 * (recursively) 
	 * 
	 * @param parent
	 * @return
	 */
	public boolean isParentChild(BioPAXElement parent, BioPAXElement child) {
		query = child;
		found = false;
		traverse(parent, null);
		return found;
	}
	
}