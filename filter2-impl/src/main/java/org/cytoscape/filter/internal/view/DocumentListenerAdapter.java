package org.cytoscape.filter.internal.view;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface DocumentListenerAdapter extends DocumentListener {

	@Override
	default void insertUpdate(DocumentEvent e) {
		update(e);
	}

	@Override
	default void removeUpdate(DocumentEvent e) {
		update(e);
	}

	@Override
	default void changedUpdate(DocumentEvent e) {
		update(e);
	}
	
	
	void update(DocumentEvent e);

}
