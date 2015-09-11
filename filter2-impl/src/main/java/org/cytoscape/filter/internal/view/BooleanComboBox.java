package org.cytoscape.filter.internal.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * A panel that contains a combo box with two values, the first value is true, second is false.
 */
@SuppressWarnings("serial")
public class BooleanComboBox extends JPanel {

	private final JComboBox<Element> combo;
	
	private class Element {
		final String name;
		final boolean value;
		
		Element(String name, boolean value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	
	public interface StateChangeListener {
		void stateChanged(boolean is);
	}
	
	public BooleanComboBox(String trueLabel, String falseLabel) {
		combo = new JComboBox<>(new Element[] {
			new Element(trueLabel, true),
			new Element(falseLabel, false)
		});
		setLayout(new BorderLayout());
		add(combo, BorderLayout.CENTER);
		setOpaque(false);
	}
	
	public boolean booleanValue() {
		return combo.getItemAt(combo.getSelectedIndex()).value;
	}
	
	public void setState(boolean state) {
		combo.setSelectedIndex(state ? 0 : 1);
	}
	
	
	public void addStateChangeListener(StateChangeListener listener) {
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listener.stateChanged(booleanValue());
			}
		});
	}
	
	
}
