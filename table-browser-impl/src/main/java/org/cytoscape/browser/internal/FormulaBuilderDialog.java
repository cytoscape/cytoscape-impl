/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.browser.internal;


import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.SwingUtilities;

import java.text.BreakIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.cytoscape.browser.internal.BrowserTableModel;

import org.cytoscape.equations.BooleanList;
import org.cytoscape.equations.DoubleList;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationUtil;
import org.cytoscape.equations.Function;
import org.cytoscape.equations.LongList;
import org.cytoscape.equations.StringList;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;


enum ApplicationDomain {
	CURRENT_CELL("Current cell only"),      // The currently selected cell in the browser.
	CURRENT_SELECTION("Current selection"), // All entries in the browser.
	ENTIRE_ATTRIBUTE("Entire attribute");   // All values of the current attribute.

	private final String asString;
	ApplicationDomain(final String asString) { this.asString = asString; }
	@Override public String toString() { return asString; }
}


public class FormulaBuilderDialog extends JDialog {
	private static final String FUNC_SELECTION_MESSAGE = "Please select a function...";
	private JComboBox functionComboBox = null;
	private JLabel usageLabel = null;
	private JTextField formulaTextField = null;
	private JPanel argumentPanel = null;
	private JButton addButton = null;
	private JButton undoButton = null;
	private JComboBox attribNamesComboBox = null;
	private JTextField constantValuesTextField = null;
	private JLabel applyToLabel = null;
	private JComboBox applyToComboBox = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private Function function = null;
	private Map<String, Function> stringToFunctionMap;
	private ArrayList<Class> leadingArgs;
	private ApplicationDomain applicationDomain;
	private Stack<Integer> undoStack;
	private final EquationCompiler compiler;
	private final BrowserTableModel tableModel;
	private final String targetAttrName;

	public FormulaBuilderDialog(final EquationCompiler compiler,
				    final BrowserTableModel tableModel, final Frame parent,
	                            final String targetAttrName)
	{
		super(parent);
		this.setTitle("Creating a formula for: " + targetAttrName);

		this.compiler = compiler;
		this.tableModel = tableModel;
		this.targetAttrName = targetAttrName;
		this.stringToFunctionMap = new HashMap<String, Function>();
		this.leadingArgs = new ArrayList<Class>();
		this.applicationDomain = ApplicationDomain.CURRENT_CELL;
		this.undoStack = new Stack<Integer>();

		final Container contentPane = getContentPane();
		final GroupLayout groupLayout = new GroupLayout(contentPane);
		contentPane.setLayout(groupLayout);

		initFunctionComboBox(contentPane);
		initUsageLabel(contentPane);
		initArgumentPanel(contentPane);
		initApplyToLabel(contentPane);
		initApplyToComboBox(contentPane);
		initFormulaTextField(contentPane);
		initOkButton(contentPane);
		initCancelButton(contentPane);

		setSize(614, 342);

		initLayout(groupLayout);
		setResizable(false);
		
		this.setAlwaysOnTop(true);
	}

	private void initFunctionComboBox(final Container contentPane) {
		functionComboBox = new JComboBox();
		final Dimension desiredWidthAndHeight = new Dimension(600, 30);
		functionComboBox.setPreferredSize(desiredWidthAndHeight);
		contentPane.add(functionComboBox);
		functionComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					functionSelected();
				}
			});

		final EquationParser parser = compiler.getParser();
		final Set<Function> functions = parser.getRegisteredFunctions();
		final String[] functionNames = new String[functions.size()];
		int index = 0;
		for (final Function function : functions) {
			functionNames[index] = function.getName() + ": " + function.getFunctionSummary();
			stringToFunctionMap.put(functionNames[index], function);
			++index;
		}

		Arrays.sort(functionNames);

		final Class requestedReturnType = getAttributeType(targetAttrName);
		functionComboBox.addItem(FUNC_SELECTION_MESSAGE);
		for (final String functionName : functionNames) {
			if (returnTypeIsCompatible(requestedReturnType, stringToFunctionMap.get(functionName).getReturnType()))
				functionComboBox.addItem(functionName);
		}

		functionComboBox.setEditable(false);
		functionComboBox.setSelectedIndex(0);
	}

	/**
	 *  @returns the type of the attribute "attribName" translated into the language of attribute equations or null
	 */
	private Class<?> getAttributeType(final String attribName) {
		return tableModel.getAttributes().getColumn(attribName).getType();
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
		    && (returnType == DoubleList.class || returnType == BooleanList.class
			|| returnType == LongList.class || returnType == StringList.class))
			return true;

		return false;
	}

	private void initUsageLabel(final Container contentPane) {
		usageLabel = new JLabel();
		contentPane.add(usageLabel);
		if (function != null)
			wrapLabelText(usageLabel, function.getUsageDescription());
	}

	private void initFormulaTextField(final Container contentPane) {
		formulaTextField = new JTextField(50);
		formulaTextField.setPreferredSize(new Dimension(600, 120));
		contentPane.add(formulaTextField);
		formulaTextField.setEditable(false);
		if (function != null)
			formulaTextField.setText("=" + function.getName() + "(");
	}

	private void initArgumentPanel(final Container contentPane) {
		argumentPanel = new JPanel();
		contentPane.add(argumentPanel);
		argumentPanel.setBorder(BorderFactory.createTitledBorder("Next Argument"));

		attribNamesComboBox = new JComboBox();
		attribNamesComboBox.setPreferredSize(new Dimension(160, 30));
		argumentPanel.add(attribNamesComboBox);
		attribNamesComboBox.setEnabled(false);

		final JLabel orLabel = new JLabel();
		orLabel.setText("or");
		argumentPanel.add(orLabel);

		constantValuesTextField = new JTextField(15);
		argumentPanel.add(constantValuesTextField);
		constantValuesTextField.setEnabled(false);

		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final StringBuilder formula = new StringBuilder(formulaTextField.getText());
					undoStack.push(formula.length());
					undoButton.setEnabled(true);

					updateButtonsAndArgumentDropdown();
				}
			});
		argumentPanel.add(addButton);
		addButton.setEnabled(false);

		undoButton = new JButton("Undo");
		undoButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final String formula = formulaTextField.getText();
					final int previousLength = undoStack.pop();
					formulaTextField.setText(formula.substring(0, previousLength));
					addButton.setEnabled(true);
					leadingArgs.remove(leadingArgs.size() - 1);
					if (undoStack.empty())
						undoButton.setEnabled(false);
					updateButtonsAndArgumentDropdown();
				}
			});
		argumentPanel.add(undoButton);
		undoButton.setEnabled(false);
	}

	private void initApplyToLabel(final Container contentPane) {
		applyToLabel = new JLabel("Apply to: ");
		contentPane.add(applyToLabel);
	}

	private boolean attributesContainBooleanSelected() {
		final CyColumn selectedColumn =
			tableModel.getAttributes().getColumn(CyNetwork.SELECTED);
		return selectedColumn != null
		       && selectedColumn.getType() == Boolean.class;
	}

	private void initApplyToComboBox(final Container contentPane) {
		applyToComboBox = new JComboBox();

		final int selectedCellRow = tableModel.getTable().getSelectedRow();
		if (selectedCellRow >= 0)
			applyToComboBox.addItem(ApplicationDomain.CURRENT_CELL);
		if (attributesContainBooleanSelected())
			applyToComboBox.addItem(ApplicationDomain.CURRENT_SELECTION);
		applyToComboBox.addItem(ApplicationDomain.ENTIRE_ATTRIBUTE);

		final Dimension widthAndHeight = applyToComboBox.getPreferredSize();
		final Dimension desiredWidthAndHeight = new Dimension(180, 30);
		applyToComboBox.setPreferredSize(desiredWidthAndHeight);
		applyToComboBox.setMinimumSize(desiredWidthAndHeight);
		applyToComboBox.setSize(desiredWidthAndHeight);
		applyToComboBox.setMaximumSize(desiredWidthAndHeight);

		contentPane.add(applyToComboBox);
		applyToComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					applicationDomain = (ApplicationDomain)applyToComboBox.getSelectedItem();
				}
			});
		applyToComboBox.setEditable(false);
		applyToComboBox.setEnabled(false);
	}

	/**
	 *  Tests whether "expression" is valid given the possible argument types are "validArgTypes".
	 *  @returns null, if "expression" is invalid, or, the type of "expression" if it was valid
	 */
	private Class<?> expressionIsValid(final List<Class<?>> validArgTypes, final String expression) {
		final Map<String, Class<?>> attribNamesAndTypes = new HashMap<String, Class<?>>();
		for (final CyColumn column : tableModel.getAttributes().getColumns())
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
	 *  Fills the attribute names combox box with the subset of valid (as in potential current function
	 *  arguments) attribute names.
	 */
	private void updateAttribNamesComboBox() {
		if (function == null)
			return;

		attribNamesComboBox.removeAllItems();
		final List<Class<?>> possibleArgTypes = getPossibleNextArgumentTypes();
		final ArrayList<String> possibleAttribNames = new ArrayList<String>(20);
		final Collection<CyColumn> columns =
			tableModel.getAttributes().getColumns();
		for (final CyColumn column : columns) {
			if (isTypeCompatible(possibleArgTypes, column.getType()))
				possibleAttribNames.add(column.getName());
		}

		Collections.sort(possibleAttribNames);

		for (final String attribName : possibleAttribNames)
			attribNamesComboBox.addItem(attribName);
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
			
	private void initOkButton(final Container contentPane) {
		okButton = new JButton("Ok");
		contentPane.add(okButton);
		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final StringBuilder errorMessage = new StringBuilder(30);
					if (updateCells(errorMessage))
						dispose();
					else
						displayErrorMessage(errorMessage.toString());
				}
			});
	}

	/**
	 *  Updates the appearance and status of various GUI components based on what is currently in the formula field.
	 */
	private void updateButtonsAndArgumentDropdown() {
		final StringBuilder formula = new StringBuilder(formulaTextField.getText());

		if (!leadingArgs.isEmpty()) // Not the first argument => we need a comma!
			formula.append(',');
		final String constExpr = constantValuesTextField.getText();
		if (constExpr != null && constExpr.length() > 0) {
			final List<Class<?>> possibleArgTypes = getPossibleNextArgumentTypes();
			final Class<?> exprType;
			if ((exprType = expressionIsValid(possibleArgTypes, constExpr)) == null)
				return;

			formula.append(constExpr);
			constantValuesTextField.setText("");
			leadingArgs.add(exprType);
		}
		else {
			final String attribName = (String)attribNamesComboBox.getSelectedItem();
			if (attribName != null) {
				formula.append(EquationUtil.attribNameAsReference(attribName));
				final CyColumn column = tableModel.getAttributes().getColumn(attribName);
				leadingArgs.add(column.getType());
			}
		}
		formulaTextField.setText(formula.toString());

		final List<Class<?>> possibleNextArgTypes = getPossibleNextArgumentTypes();
		if (possibleNextArgTypes == null) {
			final String currentFormula = formulaTextField.getText();
			formulaTextField.setText(currentFormula + ")");

			addButton.setEnabled(false);
			okButton.setEnabled(true);
		}
		else if (possibleNextArgTypes.contains(null)) {
			okButton.setEnabled(true);
		}
		else {
			addButton.setEnabled(true);
			okButton.setEnabled(false);
		}

		updateAttribNamesComboBox();
	}

	private boolean updateCells(final StringBuilder errorMessage) {
		String formula = formulaTextField.getText();
		if (formula.charAt(formula.length() - 1) != ')')
			formula = formula + ")";

		final JTable table = tableModel.getTable();
		final int cellColum = table.convertColumnIndexToModel( table.getSelectedColumn());
		
		final String attribName = tableModel.getColumnName(cellColum);
		final CyTable attribs = tableModel.getAttributes();

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
				tableModel.getAttributes().getMatchingRows(CyNetwork.SELECTED, true);
			for (final CyRow selectedRow : selectedRows) {
				if (!setAttribute(selectedRow, attribName, equation, errorMessage))
					return false;
			}
			break;
		case ENTIRE_ATTRIBUTE:
			final List<CyRow> rows = tableModel.getAttributes().getAllRows();
			for (final CyRow row : rows) {
				if (!setAttribute(row, attribName, equation, errorMessage))
					return false;
			}
			break;
		default:
			throw new IllegalStateException("unknown application domain: "
			                                + applicationDomain + ".");
		}

		return true;
	}

	/**
	 *  @returns the compiled equation upon success or null if an error occurred
	 */
	private Equation compileEquation(final CyTable attribs, final String attribName,
	                                 final String formula, final StringBuilder errorMessage) 
	{
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		initAttribNameToTypeMap(attribs, attribName, attribNameToTypeMap);
		if (compiler.compile(formula, attribNameToTypeMap))
			return compiler.getEquation();

		errorMessage.append(compiler.getLastErrorMsg());
		return null;
	}

	/**
	 *  @returns true if the attribute value has been successfully updated, else false
	 */
	private boolean setAttribute(final CyRow row, final String attribName,
				     final Equation newValue, final StringBuilder errorMessage)
	{
		try {
			row.set(attribName, newValue);
			return true;
		} catch (final Exception e) {
			errorMessage.append(e.getMessage());
			return false;
		}
	}

	private void initCancelButton(final Container contentPane) {
		cancelButton = new JButton("Cancel");
		contentPane.add(okButton);
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
	}

	private void functionSelected() {
		final String funcName = (String)functionComboBox.getSelectedItem();
		if (funcName == null || formulaTextField == null || usageLabel == null
		    || funcName.equals(FUNC_SELECTION_MESSAGE))
			return;

		functionComboBox.removeItem(FUNC_SELECTION_MESSAGE);

		attribNamesComboBox.setEnabled(true);
		constantValuesTextField.setEnabled(true);
		applyToComboBox.setEnabled(true);

		leadingArgs.clear();
		function = stringToFunctionMap.get(funcName);
		final boolean zeroArgumentFunction = getPossibleNextArgumentTypes() == null;
		formulaTextField.setText("=" + function.getName() + (zeroArgumentFunction ? "()" : "("));
		wrapLabelText(usageLabel, function.getUsageDescription());
		updateAttribNamesComboBox();
		addButton.setEnabled(zeroArgumentFunction ? false : true);
		okButton.setEnabled(zeroArgumentFunction);
	}

	private void initLayout(final GroupLayout groupLayout) {
		// 1. vertical layout
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
					     .addComponent(functionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					     .addComponent(usageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					     .addComponent(argumentPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					     .addComponent(formulaTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					     .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						       .addComponent(applyToLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						       .addComponent(applyToComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					     .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						       .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						       .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));

		// 2. horizontal layout
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.CENTER)
					       .addComponent(functionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					       .addComponent(usageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					       .addComponent(argumentPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					       .addGroup(groupLayout.createSequentialGroup()
							 .addComponent(applyToLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							 .addComponent(applyToComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					       .addComponent(formulaTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					       .addGroup(groupLayout.createSequentialGroup()
							 .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							 .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
	}

	private static void displayErrorMessage(final String errorMessage) {
		JOptionPane.showMessageDialog(new JFrame(), errorMessage, "Error",
		                              JOptionPane.ERROR_MESSAGE);
	}

	/**
	 *  Populates "attribNameToTypeMap" with the names from "cyAttribs" and their types as mapped
	 *  to the types used by attribute equations.  Types (and associated names) not used by
	 *  attribute equations are ommitted.
	 *
	 *  @param cyAttribs            the attributes to map
	 *  @param ignore               if not null, skip the attribute with this name
	 *  @param attribNameToTypeMap  the result of the translation from attribute types to
	 *                              attribute equation types
	 */
	private static void initAttribNameToTypeMap(final CyTable attribs, final String ignore,
	                                            final Map<String, Class<?>> attribNameToTypeMap)
	{
		for (final CyColumn column : attribs.getColumns())
			attribNameToTypeMap.put(column.getName(), column.getType());
		if (ignore != null)
			attribNameToTypeMap.remove(ignore);
	}

	private void wrapLabelText(final JLabel label, final String text) {
		final FontMetrics fm = label.getFontMetrics(label.getFont());
		final Container container = label.getParent();
		final int containerWidth = container.getWidth();

		final BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);

		final StringBuilder trial = new StringBuilder();
		final StringBuilder real = new StringBuilder("<html>");

		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
			final String word = text.substring(start, end);
			trial.append(word);
			int trialWidth = SwingUtilities.computeStringWidth(fm, trial.toString());
			if (trialWidth > containerWidth) {
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
