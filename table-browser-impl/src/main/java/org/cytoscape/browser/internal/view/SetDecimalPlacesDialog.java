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

import java.awt.BorderLayout;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class SetDecimalPlacesDialog extends JDialog {

	private static final float FORMAT_EXAMPLE_NUM = 123.4567890987654321f;
	private JPanel formatPanel;
	private JPanel customPanel;
	private JButton decimalDecreaseButton;
	private JLabel formatExampleLabel;
	private JLabel decimalCountLabel;
	private JButton decimalIncreaseButton;
	private JCheckBox scientificNotationCheckBox;
	private JTextField formatEntry;
	private JLabel formatLabel;
	private JTabbedPane tabPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton useDefaultButton;
	private JButton setDefaultButton;
	
	private JButton removeFormatButton;

	private String formatStr = "%.4f";
	private int decimalPlaces = 4;
	private boolean scientificNotation = false;

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
	
	private void loadFormat(String format){
		if (format != null){
			formatStr = format;
			Pattern p = Pattern.compile("\\.(\\d*)(e|f)$");
			Matcher m = p.matcher(formatStr);
			if (m.find()){
				decimalPlaces = Integer.parseInt(m.group(1));
				getDecimalCountLabel().setText(String.valueOf(decimalPlaces));
				scientificNotation = m.group(2).equals("e");
				getScientificNotationCheckBox().setSelected(scientificNotation);
			}else{
				getTabPane().setSelectedComponent(getCustomPanel());
			}
			getFormatEntry().setText(formatStr);
		}
	}

	private void initComponents() {

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(getOkButton(), getCancelButton(),
				getSetDefaultButton(), getUseDefaultButton(), getRemoveFormatButton());

		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(getFormatExampleLabel(), 300, 300, Short.MAX_VALUE)
						.addComponent(getTabPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getFormatExampleLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getTabPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));

		getContentPane().add(contents);

		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkButton().getAction(),
				getCancelButton().getAction());
		getRootPane().setDefaultButton(getOkButton());

		pack();
		setResizable(false);
	}

	private JTabbedPane getTabPane() {
		if (tabPane == null) {
			tabPane = new JTabbedPane();

			tabPane.addTab("Decimal Places", getFormatPanel());
			tabPane.addTab("Custom", getCustomPanel());
			tabPane.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					if (tabPane.getSelectedComponent() == getFormatPanel()) {
						updateFormatEntry();
					} else {
						updateFormatExampleLabel();
					}
					pack();
				}
			});
		}
		return tabPane;
	}

	private JButton getUseDefaultButton() {
		if (useDefaultButton == null) {
			useDefaultButton = new JButton("Use Default");
			useDefaultButton.addActionListener(new ActionListener() {
			
				@Override
				public void actionPerformed(ActionEvent e) {
					String format = props.getProperties().getProperty("columnFormat");
					if (format != null){
						loadFormat(format);
					}
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
					props.getProperties().setProperty("columnFormat", formatStr);
					
				}
			});
		}
		return setDefaultButton;
	}

	private JLabel getDecimalCountLabel() {
		if (decimalCountLabel == null) {
			decimalCountLabel = new JLabel(String.valueOf(decimalPlaces));
			decimalCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return decimalCountLabel;
	}

	private JButton getRemoveFormatButton() {
		if (removeFormatButton == null) {
			removeFormatButton = new JButton("Unformat");
			removeFormatButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					tableColumnModel.setColumnFormat(targetAttrName, null);
					tableModel.fireTableDataChanged();
					dispose();
				}
			});
		}
		return removeFormatButton;
	}

	private JPanel getCustomPanel() {
		if (customPanel == null) {

			formatLabel = new JLabel("Format: ");
			customPanel = new JPanel();
			SpringLayout layout  = new SpringLayout();
			layout.putConstraint(SpringLayout.WEST, formatLabel, 15, SpringLayout.WEST, customPanel);
			layout.putConstraint(SpringLayout.NORTH, formatLabel, 5, SpringLayout.NORTH, customPanel);
			
			layout.putConstraint(SpringLayout.WEST, getFormatEntry(), 5, SpringLayout.EAST, formatLabel);
			layout.putConstraint(SpringLayout.NORTH, getFormatEntry(), 5, SpringLayout.NORTH, customPanel);
			
			layout.putConstraint(SpringLayout.EAST, customPanel,
                    5,
                    SpringLayout.EAST, getFormatEntry());

			
			customPanel.setLayout(layout);
			customPanel.add(formatLabel, BorderLayout.WEST);
			customPanel.add(getFormatEntry(), BorderLayout.CENTER);
			customPanel.add(new JPanel(), BorderLayout.NORTH);
			customPanel.add(new JPanel(), BorderLayout.SOUTH);
		}

		customPanel.setVisible(false);
		return customPanel;
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
							.addComponent(getDecimalCountLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getDecimalIncreaseButton(), DEFAULT_SIZE, 32, 32).addComponent(
									getScientificNotationCheckBox(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));

			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getDecimalDecreaseButton(), DEFAULT_SIZE, 32, 32)
					.addComponent(getDecimalCountLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getDecimalIncreaseButton(), DEFAULT_SIZE, 32, 32)
					.addComponent(getScientificNotationCheckBox(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));
		}

		return formatPanel;
	}

	private JCheckBox getScientificNotationCheckBox() {
		if (scientificNotationCheckBox == null) {
			scientificNotationCheckBox = new JCheckBox("Scientific Notation", scientificNotation);
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
	
	private JTextField getFormatEntry(){
		if (formatEntry == null){
			formatEntry = new JTextField(formatStr);
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
			formatExampleLabel = new JLabel(String.format("Example: " + getFormatEntry().getText(), FORMAT_EXAMPLE_NUM));
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
		try {
			String newStr = formatEntry.getText();
			String.format(newStr, FORMAT_EXAMPLE_NUM);
			getFormatEntry().setBorder(BorderFactory.createEmptyBorder());
			formatStr = newStr;
			updateFormatExampleLabel();
			getOkButton().setEnabled(true);
		} catch (IllegalFormatException ex) {
			getFormatEntry().setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			getOkButton().setEnabled(false);
			return;
		}
	}
	
	private void updateFormatEntry() {
		final String formatStr = String.format("%%.%d%c", decimalPlaces,
				getScientificNotationCheckBox().isSelected() ? 'e' : 'f');
		getFormatEntry().setText(formatStr);
		getDecimalCountLabel().setText(String.valueOf(decimalPlaces));
	}

	private void updateFormatExampleLabel() {
		final String format = String.format(formatEntry.getText(), FORMAT_EXAMPLE_NUM);
		if (format.isEmpty()) {
			return;
		}
		getFormatExampleLabel().setText("Example: " + format);
		pack();
	}

	private boolean updateCells() {
		boolean complete = tableColumnModel.setColumnFormat(targetAttrName, getFormatEntry().getText());
		if (complete)
			tableModel.fireTableDataChanged();
		return complete;
	}

	public static void main(String[] args) {
		SetDecimalPlacesDialog d = new SetDecimalPlacesDialog(null, null, "name", null);
		d.setVisible(true);
	}

}
