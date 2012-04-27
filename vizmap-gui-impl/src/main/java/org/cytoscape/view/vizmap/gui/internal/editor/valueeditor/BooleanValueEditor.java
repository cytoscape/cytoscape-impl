package org.cytoscape.view.vizmap.gui.internal.editor.valueeditor;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public final class BooleanValueEditor implements ValueEditor<Boolean> {

	@Override
	public Boolean showEditor(final Component parent, final Boolean initialValue) {
		
		String message = "Please select new value:";
		String title = "Select True or False";
		int optionType = JOptionPane.DEFAULT_OPTION;
		int messageType = JOptionPane.QUESTION_MESSAGE;
		Icon icon = null;
		Boolean[] options = new Boolean[] {true, false};
		int result = JOptionPane.showOptionDialog(parent, message, title, optionType, messageType, icon, options, initialValue);
		if(result == JOptionPane.CLOSED_OPTION)
			return false;
		else
			return options[result];
	}

	@Override
	public Class<Boolean> getType() {
		return Boolean.class;
	}
}
