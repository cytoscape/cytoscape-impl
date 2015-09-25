package org.cytoscape.filter.internal.view.look;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

public interface FilterPanelStyle {

	JLabel createLabel(String text);
	
	<T> JComboBox<T> createCombo(ComboBoxModel<T> model);
	
	<T> JComboBox<T> createCombo(T[] items);
	
	<T> JComboBox<T> createCombo();
	
	JTextField createTextField();
	
	JCheckBox createCheckBox(String text);
	
	JFormattedTextField createFormattedTextField();
	
	JFormattedTextField createFormattedTextField(AbstractFormatter formatter);
	
	JFormattedTextField createFormattedTextField(AbstractFormatterFactory formatterFactory);
	
}
