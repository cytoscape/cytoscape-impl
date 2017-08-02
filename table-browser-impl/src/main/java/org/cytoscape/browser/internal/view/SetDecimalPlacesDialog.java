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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.IllegalFormatException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class SetDecimalPlacesDialog extends JDialog {

	private static final float FORMAT_EXAMPLE_NUM = 123.4567890987654321f;
	private JPanel formatPanel;
	private JButton decimalDecreaseButton;
	private JLabel formatExampleLabel;
	private JButton decimalIncreaseButton;
	private JToggleButton scientificNotationToggleButton;
	private JTextField formatEntry;
	private JButton okButton;
	private JButton cancelButton;
	private JButton useDefaultButton;
	private JButton setDefaultButton;
	private JButton clearDefaultButton;

	private int decimalPlaces = 4;
	private boolean scientificNotation = false;
	private boolean defaultFormat = false;

	private final BrowserTableModel tableModel;
	private final BrowserTableColumnModel tableColumnModel;
	private final String targetAttrName;
	private final CyProperty<Properties> props;

	@SuppressWarnings("unchecked")
	public SetDecimalPlacesDialog(final BrowserTable table, final Frame parent, final String targetAttrName,
			CyServiceRegistrar serviceRegistrar) {
		super(parent, "Set Decimal Places For: " + targetAttrName, ModalityType.APPLICATION_MODAL);
		this.props = serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		this.targetAttrName = targetAttrName;
		this.tableModel = (BrowserTableModel) table.getModel();
		tableColumnModel = (BrowserTableColumnModel) table.getColumnModel();
		
		loadFormat(tableColumnModel.getColumnFormat(targetAttrName));
		initComponents();
	}

	public SetDecimalPlacesDialog() {
		this.props = null;
		this.targetAttrName = null;
		this.tableModel = null;
		tableColumnModel = null;
		
		initComponents();
	}

	private void loadFormat(String format) {
		if (format == null){
			defaultFormat = true;
			format = props.getProperties().getProperty("columnFormat");
		}
		if (format != null) {
			Pattern p = Pattern.compile("\\.(\\d*)(e|f)$");
			Matcher m = p.matcher(format);
			if (m.find()) {
				decimalPlaces = Integer.parseInt(m.group(1));
				scientificNotation = m.group(2).equals("e");
				getScientificNotationToggleButton().setSelected(scientificNotation);
			}
			getFormatEntry().setText(format);
		}
	}

	private void initComponents() {

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(getOkButton(), getCancelButton(),
				getClearDefaultButton());

		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(getFormatPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getFormatPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));

		getContentPane().add(contents);

		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkButton().getAction(),
				getCancelButton().getAction());
		getRootPane().setDefaultButton(getOkButton());

		pack();
		setResizable(false);
	}

	private JButton getUseDefaultButton() {
		if (useDefaultButton == null) {
			useDefaultButton = new JButton("Use Default");
			useDefaultButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					tableColumnModel.setColumnFormat(targetAttrName, null);
					if (props.getProperties().containsKey("columnFormat")){
						loadFormat(props.getProperties().getProperty("columnFormat"));
					}
					defaultFormat = true;
				}
			});
		}
		return useDefaultButton;
	}

	private JButton getSetDefaultButton() {
		if (setDefaultButton == null) {
			setDefaultButton = new JButton("Set Default");
			setDefaultButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (getFormatExampleLabel() != null){
						props.getProperties().setProperty("columnFormat", getFormatEntry().getText());
						tableModel.fireTableDataChanged();
						defaultFormat = true;
					}
				}
			});
		}
		return setDefaultButton;
	}

	private JButton getClearDefaultButton() {
		if (clearDefaultButton == null) {
			clearDefaultButton = new JButton("Clear Default");
			clearDefaultButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (tableColumnModel.getColumnFormat(targetAttrName) == null){
						defaultFormat = true;
					}
					props.getProperties().remove("columnFormat");
					tableModel.fireTableDataChanged();
				}
			});
		}
		return clearDefaultButton;
	}

	private JPanel getFormatPanel() {
		if (formatPanel == null) {
			formatPanel = new JPanel();

			final GroupLayout layout = new GroupLayout(formatPanel);
			formatPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			JLabel formatLabel = new JLabel("Custom format:");

			layout.setHorizontalGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addComponent(getDecimalDecreaseButton(), DEFAULT_SIZE, 32, 32)
							.addComponent(getDecimalIncreaseButton(), DEFAULT_SIZE, 32, 32)
							.addComponent(getScientificNotationToggleButton(), 32, 32, 32)
							.addComponent(getFormatExampleLabel(), 250, 250, 250).addComponent(getUseDefaultButton()))
					.addGroup(layout.createSequentialGroup().addComponent(formatLabel).addComponent(getFormatEntry())
							.addComponent(getSetDefaultButton())));

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
							.addComponent(getDecimalDecreaseButton(), DEFAULT_SIZE, 32, 32)
							.addComponent(getDecimalIncreaseButton(), DEFAULT_SIZE, 32, 32)
							.addComponent(getScientificNotationToggleButton(), 32, 32, 32)
							.addComponent(getFormatExampleLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getUseDefaultButton()))
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true).addComponent(formatLabel)
							.addComponent(getFormatEntry(), 20, 20, 20).addComponent(getSetDefaultButton())));
		}

		return formatPanel;
	}

	private JToggleButton getScientificNotationToggleButton() {
		if (scientificNotationToggleButton == null) {
			scientificNotationToggleButton = new JToggleButton("E", scientificNotation);
			scientificNotationToggleButton.setToolTipText("Scientific Notation");
			scientificNotationToggleButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					scientificNotation = scientificNotationToggleButton.isSelected();
					updateFormatEntry();
				}

			});
		}
		return scientificNotationToggleButton;
	}

	private JButton getDecimalIncreaseButton() {
		if (decimalIncreaseButton == null) {
			ImageIcon ico = new ImageIcon(getClass().getResource("/images/decimalIncrease.png"));
			decimalIncreaseButton = new JButton(ico);
			decimalIncreaseButton.setToolTipText("Add a decimal place");
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

			decimalDecreaseButton.setToolTipText("Remove a decimal place");
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
					String.format("Example: " + getFormatEntry().getText(), FORMAT_EXAMPLE_NUM));
			formatExampleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}

		return formatExampleLabel;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(new AbstractAction("OK") {
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
		defaultFormat = false;
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
				getScientificNotationToggleButton().isSelected() ? 'e' : 'f');
		getFormatEntry().setText(formatStr);
		defaultFormat = false;
	}

	private void updateFormatExampleLabel() {
		final String format = getFormattedExample();
		getFormatExampleLabel().setText("Example: " + format);
		pack();
	}

	private boolean updateCells() {
		String format = defaultFormat ? null : getFormatEntry().getText();
		boolean complete = tableColumnModel.setColumnFormat(targetAttrName, format);
		if (complete)
			tableModel.fireTableDataChanged();
		return complete;
	}
	
	public static void main(String[] args){
		SetDecimalPlacesDialog d = new SetDecimalPlacesDialog();
		d.setVisible(true);
	}

}
