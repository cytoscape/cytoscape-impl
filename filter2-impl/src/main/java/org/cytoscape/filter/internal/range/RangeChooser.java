package org.cytoscape.filter.internal.range;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.filter.internal.view.look.FilterPanelStyle;

@SuppressWarnings("serial")
public class RangeChooser<N extends Number & Comparable<N>> extends JPanel {
	
	private JRangeSlider slider;
	private JFormattedTextField lowField;
	private JFormattedTextField highField;
	private JLabel label1;
	private JLabel label2;
	private JPanel spacerPanel;
	private JLabel label3;
	
	private PropertyChangeListener textListener;
	private ChangeListener sliderListener;

	RangeChooser(FilterPanelStyle style, RangeChooserController<N> controller) {
		slider = new JRangeSlider(controller.getSliderModel().getBoundedRangeModel(), JRangeSlider.HORIZONTAL);
		
		lowField = style.createFormattedTextField(controller.getFormatterFactory());
		lowField.setHorizontalAlignment(JTextField.TRAILING);
		lowField.setColumns(6);

		highField = style.createFormattedTextField(controller.getFormatterFactory());
		highField.setHorizontalAlignment(JTextField.TRAILING);
		highField.setColumns(6);
		
		
		sliderListener = new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				controller.sliderChanged();
			}
		};
		textListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				controller.textFieldChanged();
			}
		};
		
		addListeners(controller);
		
		
		label1 = style.createLabel("between ");
		label2 = style.createLabel(" and ");
		label3 = style.createLabel(" inclusive.");

		spacerPanel = new JPanel();
		spacerPanel.setOpaque(false);
		
		setLayout(new GridBagLayout());
		setOpaque(false);
	}
	
	
	void addListeners(RangeChooserController<N> controller) {
		slider.addChangeListener(sliderListener);
		lowField.addPropertyChangeListener("value", textListener);
		highField.addPropertyChangeListener("value", textListener);
	}
	
	void removeListeners() {
		slider.removeChangeListener(sliderListener);
		lowField.removePropertyChangeListener("value", textListener);
		highField.removePropertyChangeListener("value", textListener);
	}

	public JFormattedTextField getLowField() {
		return lowField;
	}

	public JFormattedTextField getHighField() {
		return highField;
	}
	
	public void setInteractive(boolean isInteractive) {
		removeAll();
		int row = 0;
		add(label1, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(lowField, new GridBagConstraints(1, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(label2, new GridBagConstraints(2, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(highField, new GridBagConstraints(3, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(label3, new GridBagConstraints(4, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(spacerPanel, new GridBagConstraints(5, row, 1, 1, 1, 0, GridBagConstraints.LINE_START,  GridBagConstraints.HORIZONTAL,  new Insets(0, 0, 0, 0), 0, 0));
		row++;
		
		if (isInteractive) {
			add(slider, new GridBagConstraints(0, row, 6, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 3), 0, 0));
			slider.invalidate();
		}
		revalidate();
	}
}
