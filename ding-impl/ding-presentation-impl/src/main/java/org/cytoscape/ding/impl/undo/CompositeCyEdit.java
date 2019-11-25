package org.cytoscape.ding.impl.undo;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class CompositeCyEdit extends AbstractCyEdit {

	private final List<AbstractCyEdit> children = new ArrayList<>();
	private final CyServiceRegistrar registrar;
	
	private CompositeCyEdit(String name, CyServiceRegistrar registrar) {
		super(name);
		this.registrar = registrar;
	}
	
	public static CompositeCyEdit init(String name, CyServiceRegistrar registrar) {
		return new CompositeCyEdit(name, registrar);
	}
	
	public void post(AbstractCyEdit ... edits) {
		add(edits);
		post();
	}
	
	public CompositeCyEdit add(AbstractCyEdit ... edits) {
		for(AbstractCyEdit edit : edits) {
			children.add(edit);
		}
		return this;
	}
	
	@Override
	public void undo() {
		children.forEach(AbstractCyEdit::undo);
	}

	@Override
	public void redo() {
		children.forEach(AbstractCyEdit::redo);
	}
	
	public void post() {
		registrar.getService(UndoSupport.class).postEdit(this);
	}

}
