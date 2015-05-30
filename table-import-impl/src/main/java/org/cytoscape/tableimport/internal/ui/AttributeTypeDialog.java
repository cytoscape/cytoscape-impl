package org.cytoscape.tableimport.internal.ui;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.BACKSLASH;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.COLON;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.COMMA;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.SLASH;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.cytoscape.tableimport.internal.util.AttributeTypes;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 *
 * @author kono
 */
public class AttributeTypeDialog extends JDialog {
	
	private static final long serialVersionUID = -6155494263756012860L;
	
	private static final String STRING = "List of Strings";
	private static final String INTEGER = "List of Integers";
	private static final String FLOAT = "List of Floating Point Numbers";
	private static final String BOOLEAN = "List of Booleans";
	
	private static final String[] LIST_DATA_TYPES = { STRING, INTEGER, FLOAT, BOOLEAN };
	
	private final byte dataType;
	private final int index;
	private String name;
	
	private JLabel listDelimiterLabel;
	
	private JPanel mainPanel;
	private JTextField attributeNameTextField;
	private JRadioButton booleanRadioButton;
	private JButton cancelButton;
	private ButtonGroup delimiterButtonGroup;
	private JRadioButton floatingPointRadioButton;
	private JRadioButton integerRadioButton;
	private JComboBox<String> listDelimiterComboBox;
	private JRadioButton listDelimiterRadioButton;
	private JRadioButton listRadioButton;
	private JComboBox<String> listTypeComboBox;
	private JButton okButton;
	private JRadioButton otherRadioButton;
	private JTextField otherTextField;
	private JRadioButton stringRadioButton;
	private ButtonGroup listDelimiterButtonGroup;

	public AttributeTypeDialog(Dialog parent, boolean modal, final String name, final byte dataType,
	                           int index, String delimiter) {
		super(parent, true);
		this.name = name;
		this.dataType = dataType;
		this.index = index;

		initComponents();
		updateComponents(delimiter);
	}

	@SuppressWarnings("serial")
	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Edit Column");
		
		listDelimiterLabel = new JLabel("List Delimiter:");
		
		delimiterButtonGroup = new ButtonGroup();
		listDelimiterButtonGroup = new ButtonGroup();

		attributeNameTextField = new JTextField();
		cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});
		okButton = new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});
		stringRadioButton = new JRadioButton("String");
		integerRadioButton = new JRadioButton("Integer");
		floatingPointRadioButton = new JRadioButton("Floating Point");
		booleanRadioButton = new JRadioButton("Boolean");
		listRadioButton = new JRadioButton("List");
		otherRadioButton = new JRadioButton("Other:");
		listTypeComboBox = new JComboBox<>();
		listDelimiterComboBox = new JComboBox<>();
		otherTextField = new JTextField();
		listDelimiterRadioButton = new JRadioButton();

		stringRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dataTypeRadioButtonActionPerformed(evt);
			}
		});

		integerRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dataTypeRadioButtonActionPerformed(evt);
			}
		});

		floatingPointRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dataTypeRadioButtonActionPerformed(evt);
			}
		});

		booleanRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dataTypeRadioButtonActionPerformed(evt);
			}
		});

		listRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				listRadioButtonActionPerformed(evt);
			}
		});

		listTypeComboBox.setModel(new DefaultComboBoxModel<String>(LIST_DATA_TYPES));

		listDelimiterComboBox.setModel(
				new DefaultComboBoxModel<String>(new String[] {
						"|",
                        COLON.toString(),
                        SLASH.toString(),
                        BACKSLASH.toString(),
                        COMMA.toString()
                    }));
		otherRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				otherRadioButtonActionPerformed(evt);
			}
		});

		listDelimiterRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				listDelimiterRadioButtonActionPerformed(evt);
			}
		});
		listDelimiterRadioButton.setSelected(true);

		listDelimiterButtonGroup.add(listDelimiterRadioButton);
		listDelimiterButtonGroup.add(otherRadioButton);

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
		
		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup(CENTER)
				.addComponent(getMainPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getMainPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		getContentPane().add(contents);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(okButton);
		
		pack();
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setBorder(LookAndFeelUtil.createTitledBorder("Column " + (index + 1)));
			
			final JLabel attributeNameLabel = new JLabel("Column Name:");
			final JLabel colummTypeLabel = new JLabel("Column Type:");
			
			final JSeparator sep = new JSeparator(JSeparator.VERTICAL);
			
			final GroupLayout layout = new GroupLayout(mainPanel);
			mainPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(TRAILING)
							.addComponent(attributeNameLabel)
							.addComponent(colummTypeLabel)
							.addGap(10)
							.addGap(10)
							.addGap(10)
							.addGap(10)
							.addGap(10)
							.addGap(10)
					)
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(attributeNameTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(stringRadioButton)
							.addComponent(integerRadioButton)
							.addComponent(floatingPointRadioButton)
							.addComponent(booleanRadioButton)
							.addGroup(layout.createSequentialGroup()
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(listRadioButton)
											.addGap(1)
											.addGap(1)
									)
									.addComponent(sep)
									.addGroup(layout.createParallelGroup(TRAILING)
											.addComponent(listTypeComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
											.addGroup(layout.createSequentialGroup()
													.addGroup(layout.createParallelGroup(LEADING)
															.addComponent(listDelimiterLabel)
															.addGap(1)
													)
													.addGroup(layout.createParallelGroup(LEADING)
															.addGroup(layout.createSequentialGroup()
																	.addComponent(listDelimiterRadioButton)
																	.addComponent(listDelimiterComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
															)
															.addGroup(layout.createSequentialGroup()
																	.addComponent(otherRadioButton) // TODO make "other" part of the combobox
																	.addComponent(otherTextField, PREFERRED_SIZE, 60, PREFERRED_SIZE)
															)
													)
											)
									)
							)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER)
							.addComponent(attributeNameLabel)
							.addComponent(attributeNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER)
							.addComponent(colummTypeLabel)
							.addComponent(stringRadioButton)
					)
					.addComponent(integerRadioButton)
					.addComponent(floatingPointRadioButton)
					.addComponent(booleanRadioButton)
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(listRadioButton)
							.addComponent(sep)
							.addGroup(layout.createSequentialGroup()
									.addComponent(listTypeComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
											.addGroup(layout.createParallelGroup(CENTER)
											.addComponent(listDelimiterLabel)
											.addComponent(listDelimiterRadioButton)
											.addComponent(listDelimiterComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									)
									.addGroup(layout.createParallelGroup(CENTER)
											.addComponent(otherRadioButton)
											.addComponent(otherTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									)
							)
					)
			);
		}
		
		return mainPanel;
	}

	private void cancelButtonActionPerformed(ActionEvent evt) {
		name = null;
		dispose();
	}

	private void okButtonActionPerformed(ActionEvent evt) {
		name = attributeNameTextField.getText();
		dispose();
	}

	private void listRadioButtonActionPerformed(ActionEvent evt) {
		if (listRadioButton.isSelected()) {
			if (otherRadioButton.isSelected()) {
				otherTextField.setEnabled(true);
			} else {
				listDelimiterComboBox.setEnabled(true);
				listTypeComboBox.setEnabled(true);
			}

			listDelimiterLabel.setEnabled(true);
			listDelimiterRadioButton.setEnabled(true);
			otherRadioButton.setEnabled(true);
		}
	}

	private void dataTypeRadioButtonActionPerformed(ActionEvent evt) {
		listDelimiterLabel.setEnabled(false);
		listDelimiterComboBox.setEnabled(false);
		listDelimiterRadioButton.setEnabled(false);
		listTypeComboBox.setEnabled(false);
		otherRadioButton.setEnabled(false);
		otherTextField.setEnabled(false);
	}

	private void otherRadioButtonActionPerformed(ActionEvent evt) {
		if (otherRadioButton.isSelected()) {
			otherTextField.setEnabled(true);
			listDelimiterComboBox.setEnabled(false);
		}
	}

	private void listDelimiterRadioButtonActionPerformed(ActionEvent evt) {
		if (listDelimiterRadioButton.isSelected()) {
			listDelimiterComboBox.setEnabled(true);
			otherTextField.setEnabled(false);
		}
	}

	private void updateComponents(String delimiter) {
		attributeNameTextField.setText(name);
		listDelimiterComboBox.setEnabled(false);

		if (delimiter == null) {
			listDelimiterComboBox.setSelectedIndex(0);
		} else {
			for (int i = 0; i < listDelimiterComboBox.getItemCount(); i++) {
				if (delimiter.equals(listDelimiterComboBox.getItemAt(i))) {
					listDelimiterComboBox.setSelectedIndex(i);

					break;
				}
			}
		}

		listDelimiterRadioButton.setEnabled(false);
		listTypeComboBox.setEnabled(false);
		listDelimiterLabel.setEnabled(false);
		otherRadioButton.setEnabled(false);
		otherTextField.setEnabled(false);
		setButtonGroup();
	}

	private void setButtonGroup() {
		delimiterButtonGroup.add(stringRadioButton);
		delimiterButtonGroup.add(integerRadioButton);
		delimiterButtonGroup.add(floatingPointRadioButton);
		delimiterButtonGroup.add(booleanRadioButton);
		delimiterButtonGroup.add(listRadioButton);

		if (dataType == AttributeTypes.TYPE_STRING) {
			delimiterButtonGroup.setSelected(stringRadioButton.getModel(), true);
		} else if (dataType == AttributeTypes.TYPE_INTEGER) {
			delimiterButtonGroup.setSelected(integerRadioButton.getModel(), true);
		} else if (dataType == AttributeTypes.TYPE_FLOATING) {
			delimiterButtonGroup.setSelected(floatingPointRadioButton.getModel(), true);
		} else if (dataType == AttributeTypes.TYPE_BOOLEAN) {
			delimiterButtonGroup.setSelected(booleanRadioButton.getModel(), true);
		} else if (dataType == AttributeTypes.TYPE_SIMPLE_LIST) {
			delimiterButtonGroup.setSelected(listRadioButton.getModel(), true);
			listDelimiterComboBox.setEnabled(true);
			listTypeComboBox.setEnabled(true);
			otherTextField.setEnabled(false);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	byte getAttributeType() {
		if (delimiterButtonGroup.getSelection().equals(stringRadioButton.getModel())) {
			return AttributeTypes.TYPE_STRING;
		} else if (delimiterButtonGroup.getSelection().equals(integerRadioButton.getModel())) {
			return AttributeTypes.TYPE_INTEGER;
		} else if (delimiterButtonGroup.getSelection().equals(floatingPointRadioButton.getModel())) {
			return AttributeTypes.TYPE_FLOATING;
		} else if (delimiterButtonGroup.getSelection().equals(booleanRadioButton.getModel())) {
			return AttributeTypes.TYPE_BOOLEAN;
		} else if (delimiterButtonGroup.getSelection().equals(listRadioButton.getModel())) {
			return AttributeTypes.TYPE_SIMPLE_LIST;
		}

		return AttributeTypes.TYPE_STRING;
	}

	public String getListDelimiterType() {
		if (listDelimiterRadioButton.isSelected()) {
			if(listDelimiterComboBox.getSelectedItem().toString().equals("|")) {
				return PIPE.toString();
			} else
				return listDelimiterComboBox.getSelectedItem().toString();
		} else {
			return otherTextField.getText().trim();
		}
	}

	/**
	 * Returns data type of the entries in the list object.
	 * Complex type is not supported.
	 *
	 * @return
	 */
	public byte getListDataType() {
		if (listTypeComboBox.getSelectedItem().equals(STRING)) {
			return AttributeTypes.TYPE_STRING;
		} else if (listTypeComboBox.getSelectedItem().equals(INTEGER)) {
			return AttributeTypes.TYPE_INTEGER;
		} else if (listTypeComboBox.getSelectedItem().equals(FLOAT)) {
			return AttributeTypes.TYPE_FLOATING;
		} else if (listTypeComboBox.getSelectedItem().equals(BOOLEAN)) {
			return AttributeTypes.TYPE_BOOLEAN;
		}

		return AttributeTypes.TYPE_STRING;
	}
}
