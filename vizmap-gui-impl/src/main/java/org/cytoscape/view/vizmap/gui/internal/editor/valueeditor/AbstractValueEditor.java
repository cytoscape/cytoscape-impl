package org.cytoscape.view.vizmap.gui.internal.editor.valueeditor;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public abstract class AbstractValueEditor<V> implements ValueEditor<V> {

	protected Class<V> type;
	
	protected final JOptionPane pane;
	protected JDialog editorDialog;
	
	public AbstractValueEditor(final Class<V> type) {
		this.type = type;
		
		pane = new JOptionPane();
		pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
	}

	@Override public Class<V> getValueType() {
		return type;
	}
}
