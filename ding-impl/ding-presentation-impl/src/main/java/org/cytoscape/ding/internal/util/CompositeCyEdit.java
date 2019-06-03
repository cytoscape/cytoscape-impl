package org.cytoscape.ding.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.work.undo.AbstractCyEdit;

public class CompositeCyEdit extends AbstractCyEdit {

	private List<AbstractCyEdit> children = new ArrayList<>();
	
	public CompositeCyEdit(String presentationName) {
		super(presentationName);
	}
	
	public CompositeCyEdit add(AbstractCyEdit child) {
		children.add(child);
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

}
