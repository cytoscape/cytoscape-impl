package org.cytoscape.view.vizmap.gui.internal.editor.valueeditor;

import java.awt.Component;

import javax.swing.JOptionPane;

public class StringValueEditor extends AbstractValueEditor<String> {

	private static final String MESSAGE = "Please enter new text value";

	public StringValueEditor(Class<String> type) {
		super(type);
	}

	
	@Override public String showEditor(Component parent, String initialValue) {
		return JOptionPane.showInputDialog(parent, MESSAGE, initialValue);
	}

}
