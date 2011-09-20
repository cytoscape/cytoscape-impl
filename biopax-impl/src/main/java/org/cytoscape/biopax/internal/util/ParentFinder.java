package org.cytoscape.biopax.internal.util;

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