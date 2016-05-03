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
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.BreakIterator;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.EquationUtil;
import org.cytoscape.equations.Function;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.LookAndFeelUtil;


enum ApplicationDomain {
	CURRENT_CELL("Current cell only"),      // The currently selected cell in the browser.
	CURRENT_SELECTION("Current selection"), // All entries in the browser.
	ENTIRE_ATTRIBUTE("Entire column");   // All values of the current attribute.

	private final String asString;
	
	ApplicationDomain(final String asString) { this.asString = asString; }
	@Override public String toString() { return asString; }
}


@SuppressWarnings("serial")
public class FormulaBuilderDialog extends JDialog {
	
	private static final String FUNC_SELECTION_MESSAGE = "Please select a function...";
	
	private JPanel functionPanel;
	private JPanel argumentPanel;
	private JPanel applyToPanel;
	private JList<Function> functionList;
	private JLabel infoLabel;
	private JLabel usageLabel;
	private JTextField formulaTextField;
	private JButton addButton1;
	private JButton addButton2;
	private JButton undoButton;
	private JComboBox<String> attribNamesComboBox;
	private JTextField constantValuesTextField;
	private JLabel applyToLabel;
	private JComboBox<ApplicationDomain> applyToComboBox;
	private JButton okButton;
	private JButton cancelButton;
	private Function function;
	private ArrayList<Class<?>> leadingArgs;
	private ApplicationDomain applicationDomain;
	private Stack<Integer> undoStack;
	private final EquationCompiler compiler;
	private final BrowserTable table;
	private final BrowserTableModel tableModel;
	private final String targetAttrName;

	public FormulaBuilderDialog(final EquationCompiler compiler, final BrowserTable table, final Frame parent,
			final String targetAttrName) {
		super(parent, "Create Function For: " + targetAttrName, ModalityType.APPLICATION_MODAL);

		this.compiler = compiler;
		this.table = table;
		this.tableModel = (BrowserTableModel) table.getModel();
		this.targetAttrName = targetAttrName;
		this.leadingArgs = new ArrayList<>();
		this.applicationDomain = ApplicationDomain.CURRENT_CELL;
		this.undoStack = new Stack<>();

		initComponents();
	}

	private void initComponents() {
		final JLabel fnIconLabel = new JLabel("f(x)");
		Font iconFont = null;
		
		try {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/jsMath-cmti10.ttf"));
			iconFont = iconFont.deriveFont(36.0f);
			fnIconLabel.setFont(iconFont);
		} catch (Exception e) {
			throw new RuntimeException("Error loading font", e);
		}
		
		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(getOkButton(), getCancelButton());
		
		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getFunctionPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(fnIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getFormulaTextField(), 380, 520, Short.MAX_VALUE)
				)
				.addComponent(getApplyToPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getFunctionPanel())
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(fnIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getFormulaTextField(), 60, 60, Short.MAX_VALUE)
				)
				.addComponent(getApplyToPanel())
				.addComponent(buttonPanel)
		);

		getContentPane().add(contents);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkButton().getAction(),
				getCancelButton().getAction());
		getRootPane().setDefaultButton(getOkButton());
		
		pack();
		setResizable(false);
		
		if (((DefaultListModel<Function>)getFunctionList().getModel()).size() > 0)
			getFunctionList().setSelectedIndex(0);
	}
	
	private JPanel getFunctionPanel() {
		if (functionPanel == null) {
			functionPanel = new JPanel();
			functionPanel.setBorder(LookAndFeelUtil.createTitledBorder("Functions"));
			
			final JScrollPane scrollPane = new JScrollPane(getFunctionList());
			
			final GroupLayout layout = new GroupLayout(functionPanel);
			functionPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(scrollPane, 160, 180, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getInfoLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getUsageLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getArgumentPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getInfoLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getUsageLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(10, 80, Short.MAX_VALUE)
							.addComponent(getArgumentPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		}
		
		return functionPanel;
	}
	
	private JPanel getArgumentPanel() {
		if (argumentPanel == null) {
			argumentPanel = new JPanel();
			
			if (LookAndFeelUtil.isAquaLAF())
				argumentPanel.setOpaque(false);
			
			final JLabel nextArgLabel = new JLabel("Next Argument:");
			
			final GroupLayout layout = new GroupLayout(argumentPanel);
			argumentPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(nextArgLabel)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getAttribNamesComboBox(), 200, 320, Short.MAX_VALUE)
							.addComponent(getAddButton1(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getConstantValuesTextField(), 200, 320, Short.MAX_VALUE)
							.addComponent(getAddButton2(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
							.addGap(1, 1, Short.MAX_VALUE)
							.addComponent(getUndoButton())
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(nextArgLabel)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(getAttribNamesComboBox())
							.addComponent(getAddButton1())
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(getConstantValuesTextField())
							.addComponent(getAddButton2())
					)
					.addComponent(getUndoButton())
			);
		}
		
		return argumentPanel;
	}
	
	private JPanel getApplyToPanel() {
		if (applyToPanel == null) {
			applyToPanel = new JPanel();
			applyToPanel.setBorder(LookAndFeelUtil.createPanelBorder());
			
			final GroupLayout layout = new GroupLayout(applyToPanel);
			applyToPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(getApplyToLabel())
					.addComponent(getApplyToComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(getApplyToLabel())
					.addComponent(getApplyToComboBox())
			);
		}
		
		return applyToPanel;
	}
	
	private JList<Function> getFunctionList() {
		if (functionList == null) {
			DefaultListModel<Function> model = new DefaultListModel<>();
			
			functionList = new JList<>(model);
			functionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			functionList.addListSelectionListener(e -> functionSelected());
			
			functionList.setCellRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value,
						int index, boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					final Function fn = (Function) value;
					this.setText(fn.getName());
					this.setToolTipText(fn.getFunctionSummary());
					
					return this;
				}
			});

			final EquationParser parser = compiler.getParser();
			final List<Function> functions = new ArrayList<>(parser.getRegisteredFunctions());
			final Collator collator = Collator.getInstance(Locale.getDefault());
			
			Collections.sort(functions, (f1, f2) -> collator.compare(f1.getName(), f2.getName()));
			
			final Class<?> requestedReturnType = getAttributeType(targetAttrName);
			int index = 0;
			
			for (final Function fn : functions) {
				if (returnTypeIsCompatible(requestedReturnType, fn.getReturnType()))
					model.add(index++, fn);
			}
		}
		
		return functionList;
	}
	
	private JComboBox<String> getAttribNamesComboBox() {
		if (attribNamesComboBox == null) {
			attribNamesComboBox = new JComboBox<>();
			attribNamesComboBox.setToolTipText("Select a columm...");
			attribNamesComboBox.setEnabled(false);
		}
		
		return attribNamesComboBox;
	}
	
	private JTextField getConstantValuesTextField() {
		if (constantValuesTextField == null) {
			constantValuesTextField = new JTextField();
			constantValuesTextField.setToolTipText("...or enter a constant value");
			constantValuesTextField.setEnabled(false);
			constantValuesTextField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateAddButton2();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateAddButton2();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					updateAddButton2();
				}
			});
		}
		
		return constantValuesTextField;
	}
	
	private JButton getAddButton1() {
		if (addButton1 == null) {
			addButton1 = new JButton("Add");
			addButton1.setToolTipText("Add the column reference");
			addButton1.addActionListener(e -> updateButtonsAndArgumentDropdown(null, (String) getAttribNamesComboBox().getSelectedItem()));
			addButton1.setEnabled(false);
		}
		
		return addButton1;
	}
	
	private JButton getAddButton2() {
		if (addButton2 == null) {
			addButton2 = new JButton("Add");
			addButton2.setToolTipText("Add the constant value");
			addButton2.addActionListener(e -> updateButtonsAndArgumentDropdown(getConstantValuesTextField().getText(), null));
			addButton2.setEnabled(false);
		}
		
		return addButton2;
	}
	
	private JButton getUndoButton() {
		if (undoButton == null) {
			undoButton = new JButton("Undo");
			undoButton.addActionListener(e -> {
                if (!undoStack.isEmpty()) {
                    final String formula = getFormulaTextField().getText();
                    final int previousLength = undoStack.pop();
                    getFormulaTextField().setText(formula.substring(0, previousLength));
                    
                    if (!leadingArgs.isEmpty())
                        leadingArgs.remove(leadingArgs.size() - 1);
                    
                    updateButtonsAndArgumentDropdown(null, null);
                }
            });
			undoButton.setEnabled(false);
		}
		
		return undoButton;
	}
	
	private JLabel getInfoLabel() {
		if (infoLabel == null) {
			infoLabel = new JLabel(FUNC_SELECTION_MESSAGE);
			infoLabel.setFont(infoLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			infoLabel.setPreferredSize(new Dimension(320, infoLabel.getPreferredSize().height));
			
			if (function != null)
				wrapLabelText(infoLabel, function.getFunctionSummary());
		}
		
		return infoLabel;
	}
	
	private JLabel getUsageLabel() {
		if (usageLabel == null) {
			usageLabel = new JLabel(" ");
			usageLabel.setFont(usageLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			usageLabel.setPreferredSize(new Dimension(320, usageLabel.getPreferredSize().height));
			
			if (function != null)
				wrapLabelText(usageLabel, function.getUsageDescription());
		}
		
		return usageLabel;
	}
	
	private JLabel getApplyToLabel() {
		if (applyToLabel == null) {
			applyToLabel = new JLabel("Apply to:");
		}
		
		return applyToLabel;
	}
	
	private JComboBox<ApplicationDomain> getApplyToComboBox() {
		if (applyToComboBox == null) {
			applyToComboBox = new JComboBox<>();
			final int selectedCellRow = table.getSelectedRow();
			
			if (selectedCellRow >= 0)
				applyToComboBox.addItem(ApplicationDomain.CURRENT_CELL);
			
			if (attributesContainBooleanSelected())
				applyToComboBox.addItem(ApplicationDomain.CURRENT_SELECTION);
			
			applyToComboBox.addItem(ApplicationDomain.ENTIRE_ATTRIBUTE);

			applyToComboBox.addActionListener(e -> applicationDomain = (ApplicationDomain) applyToComboBox.getSelectedItem());
			applyToComboBox.setEditable(false);
			applyToComboBox.setEnabled(false);
		}
		
		return applyToComboBox;
	}
	
	private JTextField getFormulaTextField() {
		if (formulaTextField == null) {
			formulaTextField = new JTextField();
			formulaTextField.setEditable(false);
			
			if (function != null)
				formulaTextField.setText("=" + function.getName() + "(");
		}
		
		return formulaTextField;
	}
	
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(new AbstractAction("OK") {
				@Override
				public void actionPerformed(ActionEvent e) {
					final StringBuilder errorMessage = new StringBuilder(30);
					
					if (updateCells(errorMessage))
						dispose();
					else
						displayErrorMessage(errorMessage.toString());
				}
			});
			okButton.setEnabled(false);
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
	
	/**
	 *  @returns the type of the attribute "attribName" translated into the language of attribute equations or null
	 */
	private Class<?> getAttributeType(final String attribName) {
		return tableModel.getDataTable().getColumn(attribName).getType();
	}

	private boolean returnTypeIsCompatible(final Class<?> requiredType, final Class<?> returnType) {
		if (returnType == requiredType)
			return true;

		if (requiredType == String.class)
			return true;

		if (requiredType == Boolean.class
		    && (returnType == Double.class || returnType == Long.class || returnType == Object.class))
			return true;

		if (requiredType == Double.class
		    && (returnType == Long.class || returnType == Boolean.class
			|| returnType == String.class || returnType == Object.class))
			return true;

		if (requiredType == Integer.class
			    && (returnType == Double.class || returnType == Boolean.class
				|| returnType == String.class || returnType == Object.class))
				return true;
		
		if (requiredType == Long.class
		    && (returnType == Double.class || returnType == Boolean.class
			|| returnType == String.class || returnType == Object.class))
			return true;

		if (requiredType == List.class
		    && List.class.isAssignableFrom(returnType))
			return true;

		return false;
	}

	private boolean attributesContainBooleanSelected() {
		final CyColumn selectedColumn =
			tableModel.getDataTable().getColumn(CyNetwork.SELECTED);
		return selectedColumn != null
		       && selectedColumn.getType() == Boolean.class;
	}

	/**
	 *  Tests whether "expression" is valid given the possible argument types are "validArgTypes".
	 *  @returns null, if "expression" is invalid, or, the type of "expression" if it was valid
	 */
	private Class<?> expressionIsValid(final List<Class<?>> validArgTypes, final String expression) {
		final Map<String, Class<?>> attribNamesAndTypes = new HashMap<String, Class<?>>();
		for (final CyColumn column : tableModel.getDataTable().getColumns())
			attribNamesAndTypes.put(column.getName(), column.getType());

		final EquationParser parser = compiler.getParser();
		if (!parser.parse("=" + expression, attribNamesAndTypes)) {
			displayErrorMessage(parser.getErrorMsg());
			return null;
		}
			
		final Class<?> expressionType = parser.getType();
		if (validArgTypes.contains(expressionType))
			return expressionType;

		final StringBuilder errorMessage = new StringBuilder("Expression is of an incompatible data type (");
		errorMessage.append(getLastDotComponent(expressionType.toString()));
		errorMessage.append(") valid types are: ");
		for (int i = 0; i < validArgTypes.size(); ++i) {
			if (validArgTypes.get(i) == null)
				continue;

			errorMessage.append(getLastDotComponent(validArgTypes.get(i).toString()));
			if (i < validArgTypes.size() - 1)
				errorMessage.append(',');
		}
		displayErrorMessage(errorMessage.toString());

		return null;
	}

	/**
	 *  Assumes that "s" consists of components separated by dots.
	 *  @returns the last component of "s" or all of "s" if there are no dots
	 */
	private static String getLastDotComponent(final String s) {
		final int lastDotPos = s.lastIndexOf('.');
		if (lastDotPos == -1)
			return s;

		return s.substring(lastDotPos + 1);
	}

	/**
	 *  Fills the attribute names combobox box with the subset of valid (as in potential current function
	 *  arguments) attribute names.
	 */
	private void updateAttribNamesComboBox() {
		getAttribNamesComboBox().setEnabled(getPossibleNextArgumentTypes() != null);
		
		if (function == null)
			return;

		getAttribNamesComboBox().removeAllItems();
		final List<Class<?>> possibleArgTypes = getPossibleNextArgumentTypes();
		final ArrayList<String> possibleAttribNames = new ArrayList<String>(20);
		final Collection<CyColumn> columns = tableModel.getDataTable().getColumns();
		
		for (final CyColumn column : columns) {
			if (isTypeCompatible(possibleArgTypes, column.getType()))
				possibleAttribNames.add(column.getName());
		}

		final Collator collator = Collator.getInstance(Locale.getDefault());
		Collections.sort(possibleAttribNames, (s1, s2) -> collator.compare(s1, s2));

		for (final String attribName : possibleAttribNames)
			getAttribNamesComboBox().addItem(attribName);
	}

	/**
	 *  @returns the set of allowed types for the next argument or null if no additional argument is valid
	 */
	private List<Class<?>> getPossibleNextArgumentTypes() {
		final Class<?>[] leadingArgsAsArray = new Class<?>[leadingArgs.size()];
		leadingArgs.toArray(leadingArgsAsArray);
		
		return function.getPossibleArgTypes(leadingArgsAsArray);
	}

	private boolean isTypeCompatible(final List<Class<?>> allowedArgumentTypes, final Class<?> attribType) {
		if (allowedArgumentTypes == null)
			return false;
		if (allowedArgumentTypes.contains(Object.class))
			return true;
		return allowedArgumentTypes.contains(attribType);
	}
			
	/**
	 *  Updates the appearance and status of various GUI components based on what is currently in the formula field.
	 */
	private void updateButtonsAndArgumentDropdown(final String constExpr, final String columnName) {
		final StringBuilder formula = new StringBuilder(getFormulaTextField().getText());
		
		if ((constExpr != null && constExpr.length() > 0) || (columnName != null && columnName.length() > 0)) {
			undoStack.push(formula.length());
			undoButton.setEnabled(true);
		}
		
		if (!leadingArgs.isEmpty()) // Not the first argument => we need a comma!
			formula.append(',');
		
		if (constExpr != null && constExpr.length() > 0) {
			final List<Class<?>> possibleArgTypes = getPossibleNextArgumentTypes();
			final Class<?> exprType;
			
			if ((exprType = expressionIsValid(possibleArgTypes, constExpr)) != null) {
				formula.append(constExpr);
				getConstantValuesTextField().setText("");
				leadingArgs.add(exprType);
			}
		} else if (columnName != null) {
			formula.append(EquationUtil.attribNameAsReference(columnName));
			final CyColumn column = tableModel.getDataTable().getColumn(columnName);
			leadingArgs.add(column.getType());
		}

		getFormulaTextField().setText(formula.toString());
		final List<Class<?>> possibleNextArgTypes = getPossibleNextArgumentTypes();
		
		if (possibleNextArgTypes == null) {
			final String currentFormula = getFormulaTextField().getText();
			getFormulaTextField().setText(currentFormula + ")");
		}

		updateAttribNamesComboBox();
		updateConstantValuesTextField();
		updateAddButton1();
		updateAddButton2();
		updateUndoButton();
		updateOkButton();
	}
	
	private void updateConstantValuesTextField() {
		getConstantValuesTextField().setEnabled(getPossibleNextArgumentTypes() != null);
	}
	
	private void updateAddButton1() {
		final boolean zeroArgument = getPossibleNextArgumentTypes() == null;
		getAddButton1().setEnabled(!zeroArgument && getAttribNamesComboBox().getSelectedItem() != null);
	}
	
	private void updateAddButton2() {
		final boolean zeroArgument = getPossibleNextArgumentTypes() == null;
		final String text = getConstantValuesTextField().getText();
		getAddButton2().setEnabled(!zeroArgument && text != null && !text.isEmpty());
	}
	
	private void updateUndoButton() {
		getUndoButton().setEnabled(!undoStack.empty());
	}
	
	private void updateOkButton() {
		final List<Class<?>> possibleNextArgTypes = getPossibleNextArgumentTypes();
		getOkButton().setEnabled(possibleNextArgTypes == null || possibleNextArgTypes.contains(null));
	}

	private boolean updateCells(final StringBuilder errorMessage) {
		String formula = getFormulaTextField().getText();
		
		if (formula.charAt(formula.length() - 1) != ')')
			formula = formula + ")";

		final int cellColum = table.convertColumnIndexToModel( table.getSelectedColumn());
		
		final String attribName = tableModel.getColumnName(cellColum);
		final CyTable attribs = tableModel.getDataTable();

		final Equation equation = compileEquation(attribs, attribName, formula, errorMessage);
		
		if (equation == null)
			return false;
		
		switch (applicationDomain) {
		case CURRENT_CELL:
			final int cellRow = table.convertRowIndexToModel( table.getSelectedRow());
			tableModel.setValueAt(formula, cellRow, cellColum);
			break;
		case CURRENT_SELECTION:
			final Collection<CyRow> selectedRows =
				tableModel.getDataTable().getMatchingRows(CyNetwork.SELECTED, true);
			for (final CyRow selectedRow : selectedRows) {
				if (!setAttribute(selectedRow, attribName, equation, errorMessage))
					return false;
			}
			break;
		case ENTIRE_ATTRIBUTE:
			final List<CyRow> rows = tableModel.getDataTable().getAllRows();
			for (final CyRow row : rows) {
				if (!setAttribute(row, attribName, equation, errorMessage))
					return false;
			}
			break;
		default:
			throw new IllegalStateException("unknown application domain: " + applicationDomain + ".");
		}

		return true;
	}

	/**
	 *  @returns the compiled equation upon success or null if an error occurred
	 */
	private Equation compileEquation(final CyTable attribs, final String attribName,
	                                 final String formula, final StringBuilder errorMessage) {
		final Map<String, Class<?>> attrNameToTypeMap = TableBrowserUtil.getAttNameToTypeMap(attribs, attribName);
		
		if (compiler.compile(formula, attrNameToTypeMap))
			return compiler.getEquation();

		errorMessage.append(compiler.getLastErrorMsg());
		
		return null;
	}

	/**
	 *  @returns true if the attribute value has been successfully updated, else false
	 */
	private boolean setAttribute(final CyRow row, final String attribName,
				     final Equation newValue, final StringBuilder errorMessage) {
		try {
			row.set(attribName, newValue);
			return true;
		} catch (final Exception e) {
			errorMessage.append(e.getMessage());
			return false;
		}
	}

	private void functionSelected() {
		function = getFunctionList().getSelectedValue();
		
		if (function == null)
			return;

		getApplyToComboBox().setEnabled(true);

		leadingArgs.clear();
		final boolean zeroArgumentFunction = getPossibleNextArgumentTypes() == null;
		getFormulaTextField().setText("=" + function.getName() + (zeroArgumentFunction ? "()" : "("));
		
		wrapLabelText(infoLabel, function.getFunctionSummary());
		wrapLabelText(getUsageLabel(), function.getUsageDescription());
		
		updateAttribNamesComboBox();
		updateConstantValuesTextField();
		updateAddButton1();
		updateAddButton2();
		updateOkButton();
	}

	private static void displayErrorMessage(final String errorMessage) {
		JOptionPane.showMessageDialog(new JFrame(), errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void wrapLabelText(final JLabel label, final String text) {
		if (label.getPreferredSize() == null)
			return;
		
		final int maxWidth = label.getPreferredSize().width;
		final BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);

		final FontMetrics fm = label.getFontMetrics(label.getFont());
		
		final StringBuilder trial = new StringBuilder();
		final StringBuilder real = new StringBuilder("<html>");
		int start = boundary.first();
		
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
			final String word = text.substring(start, end);
			trial.append(word);
			int trialWidth = SwingUtilities.computeStringWidth(fm, trial.toString());
			
			if (trialWidth > maxWidth) {
				trial.setLength(0);
				trial.append(word);
				real.append("<br>");
			}
			
			real.append(word);
		}

		real.append("</html>");
		label.setText(real.toString());
	}
}
