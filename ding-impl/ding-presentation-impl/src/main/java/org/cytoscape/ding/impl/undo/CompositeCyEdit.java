package org.cytoscape.ding.impl.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class CompositeCyEdit<T extends AbstractCyEdit> {

	private final String compositeName;
	private final List<T> children;
	private final CyServiceRegistrar registrar;
	
	public CompositeCyEdit(String name, CyServiceRegistrar registrar, int initialCapacity) {
		this.compositeName = name;
		this.registrar = registrar;
		this.children = new ArrayList<>(initialCapacity);
	}
	
	public CompositeCyEdit<T> add(T edit) {
		if(edit != null) {
			children.add(edit);
		}
		return this;
	}
	
	public Collection<T> getChildren() {
		return children;
	}
	
	public void post() {
		String name = children.size() == 1 ? children.get(0).getPresentationName() : compositeName;
		
		var edit = new AbstractCyEdit(name) {
			@Override
			public void undo() {
				children.forEach(AbstractCyEdit::undo);
				
			}
			@Override
			public void redo() {
				children.forEach(AbstractCyEdit::redo);
			}
		};
		
		registrar.getService(UndoSupport.class).postEdit(edit);
	}

}
