package org.cytoscape.filter.internal.view.look;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;


public class FlatStyle implements FilterPanelStyle {
	
	private <T extends JComponent> T style(T component) {
		component.putClientProperty("JComponent.sizeVariant", "small");
		return component;
	}
	
	private <T extends JTextField> T styleTextField(T textField) {
		textField.putClientProperty("JComponent.sizeVariant", "small");
		Color color = UIManager.getColor("Separator.foreground");
		textField.setBorder(BorderFactory.createLineBorder(color, 1));
		return textField;
	}
	
	
	
	@Override
	public JLabel createLabel(String text) {
		return style(new JLabel(text));
	}

	@Override
	public <T> JComboBox<T> createCombo() {
		return style(new JComboBox<>());
	}
	
	@Override
	public <T> JComboBox<T> createCombo(ComboBoxModel<T> model) {
		return style(new JComboBox<>(model));
	}
	
	@Override
	public <T> JComboBox<T> createCombo(T[] items) {
		return style(new JComboBox<>(items));
	}
	
	@Override
	public JCheckBox createCheckBox(String text) {
		return style(new JCheckBox(text));
	}
	
	@Override
	public JTextField createTextField() {
		return styleTextField(new JTextField());
	}
	
	@Override
	public JFormattedTextField createFormattedTextField() {
		return styleTextField(new JFormattedTextField());
	}
	
	@Override
	public JFormattedTextField createFormattedTextField(AbstractFormatter formatter) {
		return styleTextField(new JFormattedTextField(formatter));
	}
	
	@Override
	public JFormattedTextField createFormattedTextField(AbstractFormatterFactory formatterFactory) {
		return styleTextField(new JFormattedTextField(formatterFactory));
	}

}
