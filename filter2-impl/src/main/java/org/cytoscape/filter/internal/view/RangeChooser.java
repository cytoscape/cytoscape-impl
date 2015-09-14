package org.cytoscape.filter.internal.view;

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

import org.cytoscape.filter.internal.prefuse.JRangeSlider;

@SuppressWarnings("serial")
public class RangeChooser extends JPanel {
	
	private JRangeSlider slider;
	private JFormattedTextField minimumField;
	private JFormattedTextField maximumField;
	private JLabel label1;
	private JLabel label2;
	private JPanel spacerPanel;
	private JLabel label3;

	public RangeChooser(final RangeChooserController controller) {
		slider = new JRangeSlider(controller.getSliderModel(), JRangeSlider.HORIZONTAL);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				controller.sliderChanged(RangeChooser.this);
			}
		});
		
		minimumField = new JFormattedTextField(ViewUtil.createNumberFormatterFactory());
		minimumField.setHorizontalAlignment(JTextField.TRAILING);
		minimumField.setColumns(6);
		minimumField.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				controller.minimumChanged(RangeChooser.this);
			}
		});

		maximumField = new JFormattedTextField(ViewUtil.createNumberFormatterFactory());
		maximumField.setHorizontalAlignment(JTextField.TRAILING);
		maximumField.setColumns(6);
		maximumField.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				controller.maximumChanged(RangeChooser.this);
			}
		});

		label1 = new JLabel("between ");
		label2 = new JLabel(" and ");
		label3 = new JLabel(" inclusive.");

		spacerPanel = new JPanel();
		spacerPanel.setOpaque(false);
		
		setLayout(new GridBagLayout());
		setOpaque(false);
	}

	public JFormattedTextField getMinimumField() {
		return minimumField;
	}

	public JFormattedTextField getMaximumField() {
		return maximumField;
	}
	
	void setInteractive(boolean isInteractive) {
		removeAll();
		int row = 0;
		add(label1, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(minimumField, new GridBagConstraints(1, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(label2, new GridBagConstraints(2, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(maximumField, new GridBagConstraints(3, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
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
