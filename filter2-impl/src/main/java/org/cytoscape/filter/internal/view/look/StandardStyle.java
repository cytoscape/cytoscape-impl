package org.cytoscape.filter.internal.view.look;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class StandardStyle implements FilterPanelStyle {


	@Override
	public JLabel createLabel(String text) {
		return new JLabel(text);
	}

	@Override
	public <T> JComboBox<T> createCombo() {
		return new JComboBox<>();
	}
	
	@Override
	public <T> JComboBox<T> createCombo(ComboBoxModel<T> model) {
		return new JComboBox<>(model);
	}
	
	@Override
	public <T> JComboBox<T> createCombo(T[] items) {
		return new JComboBox<>(items);
	}

	@Override
	public JCheckBox createCheckBox(String text) {
		return new JCheckBox(text);
	}
	
	@Override
	public JTextField createTextField() {
		return new JTextField();
	}
	
	@Override
	public JFormattedTextField createFormattedTextField() {
		return new JFormattedTextField();
	}
	
	@Override
	public JFormattedTextField createFormattedTextField(AbstractFormatter formatter) {
		return new JFormattedTextField(formatter);
	}
	
	@Override
	public JFormattedTextField createFormattedTextField(AbstractFormatterFactory formatterFactory) {
		return new JFormattedTextField(formatterFactory);
	}

}
