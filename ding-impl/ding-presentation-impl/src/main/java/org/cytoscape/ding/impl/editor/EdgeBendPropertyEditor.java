package org.cytoscape.ding.impl.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.ding.Bend;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

public class EdgeBendPropertyEditor extends com.l2fprod.common.beans.editor.AbstractPropertyEditor {
	
	private JButton button;
	private Bend bend;
	
	private final ValueEditor<Bend> valueEditor;
		
	/**
	 * Creates a new CyLabelPositionLabelEditor object.
	 */
	public EdgeBendPropertyEditor(final ValueEditor<Bend> valueEditor) {
		this.valueEditor = valueEditor;
					
		editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
		//((JPanel) editor).add("*");
//		label.setOpaque(false);
		((JPanel) editor).add(button = ComponentFactory.Helper.getFactory()
				.createMiniButton());
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editBend();
			}
		});
		((JPanel) editor).add(button = ComponentFactory.Helper.getFactory()
				.createMiniButton());
		button.setText("X");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Bend old = bend;
				bend = null;
				firePropertyChange(old, null);
			}
		});
		((JPanel) editor).setOpaque(false);
	}

	
	@Override
	public Object getValue() {
		return bend;
	}

	
	@Override
	public void setValue(Object value) {
		bend = (Bend) value;
	}

	private void editBend() {
		final Bend newVal = valueEditor.showEditor(null, bend);

		if (newVal != null) {
			setValue(newVal);
			firePropertyChange(null, newVal);
		}
	}
}
