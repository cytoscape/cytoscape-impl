package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.IllegalFormatException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicIconFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class SetColumnFormatDialog extends JDialog {

	public static final String FLOAT_FORMAT_PROPERTY = "floatingPointColumnFormat";
	private static final double FORMAT_EXAMPLE_NUM = 123.4567890987654321;
	private JPanel samplePanel;
	private JPanel formatPanel;
	private JPanel advancedPanel;
	private JButton decimalDecreaseButton;
	private JLabel formatExampleLabel;
	private JButton decimalIncreaseButton;
	private JCheckBox scientificNotationCheckBox;
	private JButton removeFormatButton;
	private JTextField formatEntry;
	private JButton okButton;
	private JButton cancelButton;
	private JButton setDefaultButton;
	private JButton clearDefaultButton;
	private int decimalPlaces = 4;
	private boolean scientificNotation = false;

	private final BrowserTableModel tableModel;
	private final BrowserTableColumnModel tableColumnModel;
	private final String targetAttrName;
	private final CyProperty<Properties> props;

	@SuppressWarnings("unchecked")
	public SetColumnFormatDialog(final BrowserTable table, final Frame parent, final String targetAttrName,
			CyServiceRegistrar serviceRegistrar) {
		super(parent, "Set Column Format for: " + targetAttrName, ModalityType.APPLICATION_MODAL);
		this.props = serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		this.targetAttrName = targetAttrName;
		this.tableModel = (BrowserTableModel) table.getModel();
		tableColumnModel = (BrowserTableColumnModel) table.getColumnModel();

		loadFormat(tableColumnModel.getColumnFormat(targetAttrName));
		initComponents();
	}

	private void loadFormat(String format) {
		if (format == null) {
			format = props.getProperties().getProperty(FLOAT_FORMAT_PROPERTY);
		}
		if (format != null) {
			Pattern p = Pattern.compile("\\.(\\d*)(e|f)$");
			Matcher m = p.matcher(format);
			if (m.find()) {
				decimalPlaces = Integer.parseInt(m.group(1));
				scientificNotation = m.group(2).equals("e");
				getScientificNotationCheckBox().setSelected(scientificNotation);
			}
			getFormatEntry().setText(format);
		}
	}

	private void initComponents() {

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(getOkButton(), getCancelButton(),
				getRemoveFormatButton());

		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(getSamplePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(getFormatPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(getAdvancedPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getSamplePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getFormatPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAdvancedPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));

		getContentPane().add(contents);

		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkButton().getAction(),
				getCancelButton().getAction());
		getRootPane().setDefaultButton(getOkButton());

		pack();
		setResizable(false);
	}

	private JPanel getSamplePanel() {
		if (samplePanel == null) {
			samplePanel = new JPanel();
			samplePanel.setBorder(LookAndFeelUtil.createTitledBorder("Sample"));
			samplePanel.add(getFormatExampleLabel());
		}
		return samplePanel;
	}

	private JPanel getFormatPanel() {
		if (formatPanel == null) {
			formatPanel = new JPanel();

			final GroupLayout layout = new GroupLayout(formatPanel);
			formatPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			layout.setHorizontalGroup(
					layout.createSequentialGroup().addComponent(getDecimalDecreaseButton(), DEFAULT_SIZE, 32, 32)
							.addComponent(getDecimalIncreaseButton(), DEFAULT_SIZE, 32, 32)
							.addComponent(getScientificNotationCheckBox()));

			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getDecimalDecreaseButton(), DEFAULT_SIZE, 32, 32)
					.addComponent(getDecimalIncreaseButton(), DEFAULT_SIZE, 32, 32)
					.addComponent(getScientificNotationCheckBox()));
		}

		return formatPanel;
	}

	private class ExpandedArrow extends ImageIcon {
		@Override
		public synchronized void paintIcon(Component c, Graphics g, int x, int y) {

			Graphics2D g2 = (Graphics2D) g;
			Icon icon = BasicIconFactory.getMenuArrowIcon();
			AffineTransform init = g2.getTransform();
			g2.translate(c.getHeight() / 3, c.getHeight() / 2);
			g2.rotate(Math.PI / 2, 0, 0);
			g2.translate(-c.getHeight() / 3, -c.getHeight() / 2);

			icon.paintIcon(c, g, x, y);

			g2.setTransform(init);

		}
	}

	private JPanel getAdvancedPanel() {
		if (advancedPanel == null) {
			advancedPanel = new JPanel();
			advancedPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			JCheckBox advancedToggle = new JCheckBox("Advanced");
			Icon right = BasicIconFactory.getMenuArrowIcon();
			advancedToggle.setIcon(right);
			Icon down = new ExpandedArrow();
			advancedToggle.setSelectedIcon(down);

			advancedPanel.add(advancedToggle, c);
			c.gridx = 1;
			c.weightx = 1;
			advancedPanel.add(new JSeparator(JSeparator.HORIZONTAL), c);
			JPanel advancedSubpanel = new JPanel();
			
			advancedToggle.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					advancedSubpanel.setVisible(((JCheckBox) e.getSource()).isSelected());
					pack();

				}

			});
			
			advancedSubpanel.setLayout(new GridBagLayout());
			advancedSubpanel.setBorder(LookAndFeelUtil.createPanelBorder());
			c.insets = new Insets(3,3,3,3);
			JLabel formatLabel = new JLabel("Format Spec: ");
			c.gridx = 0;
			c.gridy = 0;
			advancedSubpanel.add(formatLabel, c);
			c.gridx = 1;
			c.gridwidth = 2;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			advancedSubpanel.add(getFormatEntry(), c);
			// c.gridx = 3;
			c.gridwidth = 1;
			// advancedSubpanel.add(getUseDefaultButton(), c);
			c.gridx = 1;
			c.gridy = 1;
			advancedSubpanel.add(getClearDefaultButton(), c);
			c.gridx = 2;
			advancedSubpanel.add(getSetDefaultButton(), c);
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 2;
			c.gridheight = 2;
			advancedPanel.add(advancedSubpanel, c);

			advancedSubpanel.setVisible(false);
		}
		return advancedPanel;
	}

	private JCheckBox getScientificNotationCheckBox() {
		if (scientificNotationCheckBox == null) {
			scientificNotationCheckBox = new JCheckBox("Scientific Notation", scientificNotation);
			scientificNotationCheckBox.setToolTipText("Toggle scientific notation formatting");
			scientificNotationCheckBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					scientificNotation = scientificNotationCheckBox.isSelected();
					updateFormatEntry();
				}

			});
		}
		return scientificNotationCheckBox;
	}

	private JButton getRemoveFormatButton() {
		if (removeFormatButton == null) {
			removeFormatButton = new JButton("Remove Format");
			removeFormatButton.setToolTipText("Use the default floating point format spec from Cytoscape properties");
			removeFormatButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					tableColumnModel.setColumnFormat(targetAttrName, null);
					dispose();
				}
			});
		}
		return removeFormatButton;
	}

	private JButton getSetDefaultButton() {
		if (setDefaultButton == null) {
			setDefaultButton = new JButton("Save As Default");
			setDefaultButton.setToolTipText("Store as the default format spec for all floating point columns");
			setDefaultButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (getFormatExampleLabel() != null) {
						props.getProperties().setProperty(FLOAT_FORMAT_PROPERTY, getFormatEntry().getText());
						tableModel.fireTableDataChanged();
					}
				}
			});
		}
		return setDefaultButton;
	}

	private JButton getClearDefaultButton() {
		if (clearDefaultButton == null) {
			clearDefaultButton = new JButton("Clear Default");
			clearDefaultButton.setToolTipText("Remove formatting floating point columns by default");
			clearDefaultButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					props.getProperties().remove(FLOAT_FORMAT_PROPERTY);
					tableModel.fireTableDataChanged();
				}
			});
		}
		return clearDefaultButton;
	}

	private JButton getDecimalIncreaseButton() {
		if (decimalIncreaseButton == null) {
			ImageIcon ico = new ImageIcon(getClass().getResource("/images/decimalIncrease.png"));
			decimalIncreaseButton = new JButton(ico);
			decimalIncreaseButton.setToolTipText("Increase decimal precision");
			decimalIncreaseButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					decimalPlaces = Math.min(16, decimalPlaces + 1);
					updateFormatEntry();
				}
			});
		}

		return decimalIncreaseButton;
	}

	private JTextField getFormatEntry() {
		if (formatEntry == null) {

			formatEntry = new JTextField("%.4f");

			formatEntry.setToolTipText("The formatting rule that is applied to all values in the column");
			formatEntry.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					formatFieldChanged();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					formatFieldChanged();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					formatFieldChanged();

				}
			});
		}
		formatEntry.setBorder(BorderFactory.createEmptyBorder());
		return formatEntry;
	}

	private JButton getDecimalDecreaseButton() {
		if (decimalDecreaseButton == null) {
			ImageIcon ico = new ImageIcon(getClass().getResource("/images/decimalDecrease.png"));
			decimalDecreaseButton = new JButton(ico);

			decimalDecreaseButton.setToolTipText("Decrease decimal precision");
			decimalDecreaseButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					decimalPlaces = Math.max(0, decimalPlaces - 1);
					updateFormatEntry();
				}
			});
		}

		return decimalDecreaseButton;
	}

	private JLabel getFormatExampleLabel() {
		if (formatExampleLabel == null) {
			formatExampleLabel = new JLabel(
					String.format(getFormatEntry().getText(), FORMAT_EXAMPLE_NUM));
			formatExampleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}

		return formatExampleLabel;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(new AbstractAction("Apply") {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (updateCells())
						dispose();
				}
			});
		}

		return okButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}

		return cancelButton;
	}

	private void formatFieldChanged() {
		boolean valid = false;
		if (getFormattedExample() != null) {
			getFormatEntry().setBorder(BorderFactory.createEmptyBorder());
			updateFormatExampleLabel();
			valid = true;
		} else {
			getFormatEntry().setBorder(BorderFactory.createLineBorder(Color.RED, 2));
		}
		getOkButton().setEnabled(valid);
		getSetDefaultButton().setEnabled(valid);
	}

	private String getFormattedExample() {
		String formatted = null;
		String newStr = formatEntry.getText();
		if (!newStr.isEmpty()) {
			try {
				formatted = String.format(newStr, FORMAT_EXAMPLE_NUM);
			} catch (IllegalFormatException e) {

			}
		}
		return formatted;
	}

	private void updateFormatEntry() {
		final String formatStr = String.format("%%.%d%c", decimalPlaces,
				getScientificNotationCheckBox().isSelected() ? 'e' : 'f');
		getFormatEntry().setText(formatStr);
	}

	private void updateFormatExampleLabel() {
		final String format = getFormattedExample();
		getFormatExampleLabel().setText(format);
		pack();
	}

	private boolean updateCells() {
		String format = getFormatEntry().getText();
		boolean complete = tableColumnModel.setColumnFormat(targetAttrName, format);
		if (complete)
			tableModel.fireTableDataChanged();
		return complete;
	}

	public SetColumnFormatDialog() {
		targetAttrName = "";
		tableColumnModel = null;
		tableModel = null;
		props = null;
		initComponents();
	}

	public static void main(String[] args) {
		SetColumnFormatDialog d = new SetColumnFormatDialog();
		d.setVisible(true);
	}

}
