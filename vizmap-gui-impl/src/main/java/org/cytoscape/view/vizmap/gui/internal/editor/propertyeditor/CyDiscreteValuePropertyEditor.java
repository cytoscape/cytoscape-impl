package org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPanel;

import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.DiscreteValueEditor;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

public class CyDiscreteValuePropertyEditor<T> extends AbstractPropertyEditor {

	private final DiscreteValueEditor<T> valEditor;
	private T currentValue;
	private Component parent;

	public CyDiscreteValuePropertyEditor(final DiscreteValueEditor<T> valEditor) {
		this.valEditor = valEditor;
		this.editor = new JPanel();
		editor.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				selectValue();
			}

			public void focusLost(FocusEvent arg0) {
			}
		});
	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

	@Override
	public void setValue(Object value) {
		if (value == null)
			this.currentValue = null;
		else
			this.currentValue = (T) value;
	}

	@Override
	public Object getValue() {
		return currentValue;
	}

	private final void selectValue() {
		final T val = (T) super.getValue();
		final T selectedVal = valEditor.showEditor(parent, val);

		if (selectedVal != null) {
			final T oldVal = val;
			final T newVal = selectedVal;

			super.setValue(newVal);
			this.currentValue = newVal;
			firePropertyChange(oldVal, newVal);
		}
	}
}
