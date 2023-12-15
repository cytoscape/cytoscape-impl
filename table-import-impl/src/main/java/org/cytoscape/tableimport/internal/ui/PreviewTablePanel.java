package org.cytoscape.tableimport.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING;
import static org.cytoscape.tableimport.internal.util.ImportType.NETWORK_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.TABLE_IMPORT;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ALIAS;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.KEY;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.NONE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextDelimiter;
import org.cytoscape.tableimport.internal.task.TableImportContext;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.FileType;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.tableimport.internal.util.TypeUtil;
import org.cytoscape.tableimport.internal.util.URLUtil;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * General purpose preview table panel.
 */
@SuppressWarnings("serial")
public class PreviewTablePanel extends JPanel {

	private static final float ICON_FONT_SIZE = 14.0f;
	
	// Lines start with this char will be ignored.
	private String commentChar;
	private int startLine;

	// Tracking attribute data type.
	private String[] namespaces;
	private SourceColumnSemantic[] types;
	private AttributeDataType[] dataTypes;
	private String[] listDelimiters;
	private Character decimalSeparator;
	
	private Set<?> keySet;

	/*
	 * GUI Components
	 */
	private JLabel sheetLabel;
	private JComboBox<Sheet> sheetComboBox;
//	private JButton pasteColumnSettingsButton; // TODO: suggested feature (copy/paste column settings)
	private JTable previewTable;
	private JButton selectAllButton;
	private JButton selectNoneButton;
	private JScrollPane tableScrollPane;
	
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private ImportType importType;
	
	private final TableImportContext tableImportContext;
	private final IconManager iconManager;
	
	private EditDialog editDialog;
	private int lastDialogIndex = -1;
	private long lastDialogTime;
	private boolean updating;
	
//	private AttributeSettings copiedColumnSettings; TODO: suggested feature
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel(
			ImportType importType,
			TableImportContext tableImportContext,
			IconManager iconManager
	) {
		this.importType = importType;
		this.tableImportContext = tableImportContext;
		this.iconManager = iconManager;

		initComponents();
	}

	public void setKeyAttributeList(Set<?> keySet) {
		this.keySet = keySet;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (changes == null)
			return;

		changes.addPropertyChangeListener(l);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	private void initComponents() {
		setBorder(LookAndFeelUtil.createTitledBorder("Preview"));
		
		sheetLabel = new JLabel("Sheet:");
		sheetLabel.setVisible(false);
		
		var instructionLabel = new JLabel("Click on a column to edit it.");
		instructionLabel.setFont(instructionLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		
		LookAndFeelUtil.equalizeSize(getSelectAllButton(), getSelectNoneButton());
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(sheetLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(RELATED)
						.addComponent(getSheetComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(UNRELATED)
						.addComponent(instructionLabel)
						.addGap(20, 20, Short.MAX_VALUE)
						.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(RELATED)
						.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
//						.addPreferredGap(UNRELATED)
//						.addComponent(getPasteColumnSettingsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getTableScrollPane(), DEFAULT_SIZE, 320, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(sheetLabel)
						.addComponent(getSheetComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(instructionLabel)
						.addComponent(getSelectAllButton())
						.addComponent(getSelectNoneButton())
//						.addComponent(getPasteColumnSettingsButton())
				)
				.addPreferredGap(RELATED)
				.addComponent(getTableScrollPane(), 120, 180, Short.MAX_VALUE)
				.addPreferredGap(RELATED)
		);
		
		ColumnResizer.adjustColumnPreferredWidths(getPreviewTable());
		update();
	}

// TODO: suggested feature (copy/paste column settings)
//	private JButton getPasteColumnSettingsButton() {
//		if (pasteColumnSettingsButton == null) {
//			pasteColumnSettingsButton = new JButton(IconManager.ICON_PASTE);
//			pasteColumnSettingsButton.setToolTipText("Paste Column Settings...");
//			pasteColumnSettingsButton.setBorderPainted(false);
//			pasteColumnSettingsButton.setContentAreaFilled(false);
//			pasteColumnSettingsButton.setFocusPainted(false);
//			pasteColumnSettingsButton.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
//			pasteColumnSettingsButton.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
//			pasteColumnSettingsButton.setEnabled(false);
//			
//			pasteColumnSettingsButton.addActionListener(evt -> {
//				if (copiedColumnSettings != null) {
//					var parent = SwingUtilities.getWindowAncestor(PreviewTablePanel.this);
//					var dialog = new ColumnSelectorDialog(parent);
//					dialog.pack();
//					dialog.setLocationRelativeTo(parent);
//					dialog.setVisible(true);
//				}
//			});
//		}
//		
//		return pasteColumnSettingsButton;
//	}
	
	public JTable getPreviewTable() {
		if (previewTable == null) {
			previewTable = new JTable(new PreviewTableModel(new Vector<Vector<String>>(), new Vector<String>(), false));
			previewTable.setShowGrid(false);
			previewTable.setCellSelectionEnabled(false);
			previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			previewTable.setDefaultEditor(Object.class, null);

			if (importType == NETWORK_IMPORT) {
				var netRenderer = new PreviewTableCellRenderer();
				previewTable.setDefaultRenderer(Object.class, netRenderer);
			} else {
				previewTable.setDefaultRenderer(Object.class, new PreviewTableCellRenderer());
			}

			var hd = previewTable.getTableHeader();
			hd.setReorderingAllowed(false);
			hd.setDefaultRenderer(new PreviewTableHeaderRenderer());
			
			var colModelListener = new TableColumnModelListener() {
				@Override
				public void columnMoved(TableColumnModelEvent e) {
					disposeEditDialog();
				}
				@Override
				public void columnMarginChanged(ChangeEvent e) {
					disposeEditDialog();
				}
				@Override
				public void columnSelectionChanged(ListSelectionEvent e) {
					// Ignored...
				}
				@Override
				public void columnRemoved(TableColumnModelEvent e) {
					// Ignored...
				}
				@Override
				public void columnAdded(TableColumnModelEvent e) {
					// Ignored...
				}
			};
			
			hd.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					var columnModel = previewTable.getColumnModel();
					int newColIdx = columnModel.getColumnIndexAtX(e.getX());
					int idx = editDialog != null ? editDialog.index : -1;
					
					disposeEditDialog();
						
					if (idx != newColIdx)
						showEditDialog(newColIdx);
					
					// Do not show editor dialog when the user is resizing the columns
					previewTable.getColumnModel().removeColumnModelListener(colModelListener);
					previewTable.getColumnModel().addColumnModelListener(colModelListener);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					previewTable.getColumnModel().removeColumnModelListener(colModelListener);
				}
			});
			
			// Also close the editor dialog when the table changes
			previewTable.getModel().addTableModelListener(evt -> disposeEditDialog());
		}
		
		return previewTable;
	}

	public String[] getAttributeNames() {
		String[] names = null;
		var model = (PreviewTableModel) getPreviewTable().getModel();
		int columnCount = model.getColumnCount();
		names = new String[columnCount];
		
		for (int i = 0; i < columnCount; i++)
			names[i] = model.getColumnName(i);
		
		return names;
	}
	
	public String[] getNamespaces() {
		return namespaces;
	}
	
	protected void setNamespace(int index, String newValue) {
		if (index < 0)
			return;
		
		if (namespaces != null && namespaces.length > index)
			namespaces[index] = newValue;
	}
	
	public SourceColumnSemantic[] getTypes() {
		return types;
	}
	
	protected void setType(int index, SourceColumnSemantic newType) {
		if (index < 0)
			return;
		
		if (types != null && types.length > index) {
			// First replace the index that currently has this unique type by the default type
			if (newType.isUnique())
				replaceType(newType, TypeUtil.getDefaultType(importType));
			
			var oldType = types[index];
			types[index] = newType;
			
			if (newType != oldType)
				changes.fireIndexedPropertyChange(DataEvents.ATTR_TYPE_CHANGED, index, oldType, newType);
		}
	}
	
	protected void fillTypes(SourceColumnSemantic newValue) {
		if (types != null)
			Arrays.fill(types, newValue);
	}
	
	protected void replaceType(SourceColumnSemantic type1, SourceColumnSemantic type2) {
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				if (types[i] == type1)
					setType(i, type2);
			}
		}
	}
	
	public AttributeDataType[] getDataTypes() {
		return dataTypes;
	}
	
	public AttributeDataType getDataType(int index) {
		if (dataTypes != null && dataTypes.length > index)
			return dataTypes[index];
		
		return null;
	}
	
	protected void setDataType(int index, AttributeDataType newValue) {
		if (index < 0)
			return;
		
		if (dataTypes != null && dataTypes.length > index) {
			var oldValue = dataTypes[index];
			dataTypes[index] = newValue;
			
			if (newValue != oldValue)
				changes.fireIndexedPropertyChange(DataEvents.ATTR_DATA_TYPE_CHANGED, index, oldValue, newValue);
		}
	}
	
	public String[] getListDelimiters() {
		return listDelimiters;
	}
	
	public void setListDelimiter(int index, String newValue) {
		if (index < 0)
			return;
		
		if (listDelimiters != null && listDelimiters.length > index)
			listDelimiters[index] = newValue;
	}
	
	public FileType getFileType() {
		var name = getSourceName();

		if (name != null && name.startsWith("gene_association"))
			return FileType.GENE_ASSOCIATION_FILE;

		return FileType.ATTRIBUTE_FILE;
	}

	public String getSourceName() {
		return getPreviewTable().getName();
	}
	
	/**
	 * Load file and show preview.
	 */
	public void update(
			Workbook workbook,
			String fileType,
			String fileFullName,
			InputStream tempIs,
			List<String> delimiters,
			String commentLineChar,
			int startLine,
			Character decimalSeparator
	) throws IOException {
		if (tempIs == null)
			return;

		if ((commentLineChar != null) && (commentLineChar.trim().length() != 0))
			this.commentChar = commentLineChar;
		else
			this.commentChar = null;

		this.startLine = startLine;
		this.decimalSeparator = decimalSeparator;

		updating = true;
		
		try {
			getSheetComboBox().removeAllItems();
			getSheetComboBox().setVisible(false);
			sheetLabel.setVisible(false);
			
			PreviewTableModel newModel = null;
			
			if (SupportedFileType.EXCEL.getExtension().equalsIgnoreCase(fileType)
					|| SupportedFileType.OOXML.getExtension().equalsIgnoreCase(fileType)) {
				int numberOfSheets = workbook.getNumberOfSheets();
				
				if (numberOfSheets == 0)
					throw new IllegalStateException("No sheet found in the workbook.");
	
				for (int i = 0; i < numberOfSheets; i++) {
					var sheet = workbook.getSheetAt(i);
					
					if (sheet.getPhysicalNumberOfRows() > 0)
						getSheetComboBox().addItem(sheet);
				}
				
				if (getSheetComboBox().getItemCount() > 0)
					getSheetComboBox().setSelectedIndex(0);
				
				if (getSheetComboBox().getItemCount() > 1) {
					sheetLabel.setVisible(true);
					getSheetComboBox().setVisible(true);
				}
				
				/*
				 * Load each sheet in the workbook.
				 */
				if (getSheetComboBox().getItemCount() > 0) {
					var sheet = workbook.getSheetAt(0);
					update(sheet);
				} else {
					throw new RuntimeException("No data found in the Excel sheets.");
				}
			} else {
				newModel = parseText(tempIs, delimiters, startLine);
	
				String sourceName;
				String[] urlParts = fileFullName.split("/");
				
				if (urlParts.length > 0 && !fileFullName.isEmpty())
					sourceName = urlParts[urlParts.length - 1];
				else
					sourceName = "Source Table";
				
				dataTypes = TypeUtil.guessDataTypes(newModel, decimalSeparator);
				types = TypeUtil.guessTypes(importType, newModel, dataTypes, getIgnoredTypes());
				listDelimiters = new String[newModel.getColumnCount()];
				namespaces = TypeUtil.getPreferredNamespaces(types);
				
				update(newModel, sourceName);
			}
		} finally {
			updating = false;
		}
	}

	public void setFirstRowAsColumnNames() {
		var model = (PreviewTableModel) getPreviewTable().getModel();
		model.setFirstRowNames(true);

		types = TypeUtil.guessTypes(importType, model, dataTypes, getIgnoredTypes());
		update();
		
		ColumnResizer.adjustColumnPreferredWidths(getPreviewTable());
	}

	protected boolean isCytoscapeAttributeFile(URL sourceURL) throws IOException {
		InputStream is = null;
		boolean testResult = true;

		try {
			BufferedReader bufRd = null;
			is = URLUtil.getInputStream(sourceURL);
			
			try {
				String line = null;
				int i = 0;
				bufRd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8").newDecoder()));
				
				// Test first two lines to check the file type.
				while ((line = bufRd.readLine()) != null) {
					if (i == 0) {
						String[] elements = line.split(" +");

						if (elements.length == 1) {
							// True so far.
						} else {
							elements = line.split("[(]");

							if ((elements.length == 2) && elements[1].startsWith("class=")) {
								// true so far.
							} else {
								testResult = false;

								break;
							}
						}
					} else if (i == 1) {
						String[] elements = line.split(" += +");

						if (elements.length != 2)
							testResult = false;
					} else if (i >= 2) {
						break;
					}

					i++;
				}
			} finally {
				if (bufRd != null) bufRd.close();
			}
		} finally {
			if (is != null) is.close();
		}

		return testResult;
	}

	public int checkKeyMatch(int targetColumn) {
		int matched = 0;

		if (keySet != null && !keySet.isEmpty()) {
			var curModel = getPreviewTable().getModel();
	
			try {
				curModel.getValueAt(0, targetColumn);
			} catch (ArrayIndexOutOfBoundsException e) {
				return 0;
			}
	
			int rowCount = curModel.getRowCount();
			
			for (int i = 0; i < rowCount; i++) {
				Object val = curModel.getValueAt(i, targetColumn);
				
				if (val != null && keySet.contains(val))
					matched++;
			}
		}
		
		return matched;
	}
	
	public int getPreviewSize() {
		return 500;
	}
	
	/**
	 * Returns the first index of the table column that has the passed type.
	 */
	protected int getColumnIndex(SourceColumnSemantic type) {
		if (types != null)
			return Arrays.asList(types).indexOf(type);
		
		return -1;
	}
	
	protected void setAliasColumn(int index, boolean flag) {
		if (types != null && types.length > index) {
			types[index] = flag ? ALIAS : ATTR;
			update();
		}
	}
	
	protected boolean isImported(int index) {
		if (types != null && types.length > index)
			return types[index] != NONE;

		return false;
	}
	private Class<?> getNumericClass(double val) {
		// We use BigDecimal to know if the numerical value is int, long or double
		BigDecimal bd = BigDecimal.valueOf(val);
		try {
			bd.intValueExact();
			return Integer.class;
		} catch (ArithmeticException eInt) {
			try {
				bd.longValueExact();
				return Long.class;
			} catch (ArithmeticException eLong) {
				return Double.class;
			}
		}
	}
	
	private String formatCell(Cell cell, Class<?> cellClass, DataFormatter formatter, FormulaEvaluator evaluator) {
		if (cell == null) {
			return "";
		}
		
		CellType cellType = cell.getCellType();
		if (cellType == CellType.FORMULA) {
			if (evaluator == null) {
				return cell.getCellFormula();
			}
			cellType = evaluator.evaluateFormulaCell(cell);
		}
		switch (cellType) {
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return formatter.formatCellValue(cell, evaluator);
			}
			BigDecimal val = BigDecimal.valueOf(cell.getNumericCellValue());

			if (cellClass == Integer.class) {
				return String.valueOf(val.intValue());
			}
			if (cellClass == Long.class) {
				return String.valueOf(val.longValue());
			}
			if (cellClass == Double.class) {
				return String.valueOf(val.doubleValue());
			}
			return val.toPlainString();

		case STRING:
			return cell.getRichStringCellValue().getString();
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case BLANK:
			return "";
		}

		return "";
	}

	private PreviewTableModel parseExcel(Sheet sheet, int startLine) throws IOException {
		int size = getPreviewSize();
		
		if (size == -1)
			size = Integer.MAX_VALUE;

		int maxCol = 0;
		var data = new Vector<Vector<String>>();
		boolean firstRowNames = importType == NETWORK_IMPORT || importType == TABLE_IMPORT;

		int rowCount = 0;
		int validRowCount = 0;
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		DataFormatter formatter = new DataFormatter();
		Row row;

		var colTypes = new Vector<Class<?>>();
		var previewRows = new Vector<Row>();

		while (((row = sheet.getRow(rowCount)) != null) && (validRowCount < size)) {
			if (rowCount >= startLine) {
				if (maxCol < row.getLastCellNum())
					maxCol = row.getLastCellNum();
				
				// We initialize colTypes
				if(colTypes.size() != maxCol) {
					for(int c=colTypes.size(); c<maxCol; ++c) {
						colTypes.add(null);
					}
				}

				for (short j = 0; j < maxCol; j++) {
					Cell cell = row.getCell(j);
					if (cell != null && cell.getCellType() != CellType.ERROR && 
							(cell.getCellType() != CellType.FORMULA || cell.getCachedFormulaResultType() != CellType.ERROR)) {
						// We check types only if it's not the names of columns
						if(!firstRowNames || validRowCount > 0) {
							Class<?> colType = colTypes.get(j);
							
							CellType cellType = cell.getCellType();
							if (cellType == CellType.FORMULA) {
								if (evaluator != null) {
									cellType = evaluator.evaluateFormulaCell(cell);
								}
							}
							
							if(colType == null) {
								switch (cellType) {
								case NUMERIC :
									if (DateUtil.isCellDateFormatted(cell)) {
										colType = String.class;
									} else {
										colType = getNumericClass(cell.getNumericCellValue());
									}
									break;
								case STRING :
									colType = String.class;
									break;
								case BOOLEAN :
									colType = Boolean.class;
									break;
								case FORMULA :
								case BLANK :
									colType = null; // don't know yet
									break;
								}
							} else {
								// colType is not null
								// We check if the current type fits with the one of the cell

								// Previously detected as boolean?
								if (colType == Boolean.class) {
									// Just make sure the other rows are also compatible with boolean values...
									if (cellType != CellType.BOOLEAN) {
										// This row does not contain a boolean, so the column has to be a String
										colType = String.class;
									}
								} else if (colType == Integer.class) {
									// Make sure the other rows are also integers...
									if (cellType == CellType.NUMERIC) {
										Class<?> cellNumericType = getNumericClass(cell.getNumericCellValue());
										if (cellNumericType == Long.class)
											colType = Long.class;
										else if (cellNumericType == Double.class)
											colType = Double.class;
										// else it is Integer, we don't change
									} else {
										// Previously numeric, not numeric anymore: String
										colType = String.class;
									}
								} else if (colType == Long.class) {
									// Make sure the other rows are also longs (no need to check for integers anymore)...
									if (cellType == CellType.NUMERIC) {
										Class<?> cellNumericType = getNumericClass(cell.getNumericCellValue());
										if (cellNumericType == Double.class)
											colType = Double.class;
									} else {
										// Previously numeric, not numeric anymore: String
										colType = String.class;
									}
								} else if (colType == Double.class) {
									// Make sure the other rows are also doubles (no need to check for other numeric types)...
									if (cellType != CellType.NUMERIC) {
										colType = String.class;
									}
								}
							}

							colTypes.set(j, colType);
						}
					}
				}

				previewRows.add(row);
				validRowCount++;
			}

			rowCount++;
		}
		
		// Now that we know the type of each column, we can read the rows again for the preview
		for (Row r : previewRows) {
			var rowVector = new Vector<String>();
			
			for (short col = 0; col < maxCol; col++) {
				Cell cell = r.getCell(col);

				if (cell == null || cell.getCellType() == CellType.ERROR || 
						(cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.ERROR)) {
					rowVector.add(null);
				} else {
					rowVector.add(formatCell(cell, colTypes.get(col), formatter, evaluator));
				}
			}
			
			data.add(rowVector);
		}
		
		return new PreviewTableModel(data, new Vector<String>(), colTypes, firstRowNames);
	}
	
	private PreviewTableModel parseText(InputStream tempIs, List<String> delimiters, int startLine) throws IOException {
		String line;
		String attrName = "Attr1";
		Vector<Vector<String>> data = null;
		int maxColumn;

		BufferedReader bufRd = new BufferedReader(new InputStreamReader(tempIs, Charset.forName("UTF-8").newDecoder()));
		/*
		 * Generate reg. exp. for delimiter.
		 */
		final String delimiterRegEx;

		if (delimiters != null) {
			StringBuffer delimiterBuffer = new StringBuffer();

			if (delimiters.size() != 0) {
				delimiterBuffer.append("[");

				for (String delimiter : delimiters)
					delimiterBuffer.append(delimiter);

				delimiterBuffer.append("]");
			}

			delimiterRegEx = delimiterBuffer.toString();
		} else {
			// treat as cytoscape attribute files.
			delimiterRegEx = " += +";
			// Extract first column for attr name.
			line = bufRd.readLine();
			String[] line1 = line.split(" +");
			attrName = line1[0];
		}

		/*
		 * Read & extract one line at a time. The line can be Tab delimited,
		 */
		int size = getPreviewSize();
		boolean importAll = false;

		if (size == -1)
			importAll = true;

		// Distinguish between CSV files and everything else.
		// TODO: Since the CSV parser allows for other delimiters, consider exploring using it for everything.

		// The variables are modified by both the new method and the old method.
		int rowCount = 0;
		int validRowCount = 0;
		maxColumn = 0;
		data = new Vector<>();
		
		if (delimiters != null && delimiters.contains(TextDelimiter.COMMA.getDelimiter()) && delimiters.size() == 1) {
			// Only if there is exactly one delimiter and that delimiter is a
			// comma should you read the file using OpenCSV
			// New method... Using OpenCSV
			var reader = new CSVReader(bufRd);
			String[] rowData; // Note that rowData is roughly equivalent to "parts" in the old code.
			
			while ((rowData = reader.readNext()) != null) {
				var list = Arrays.asList(rowData);
				line = list.isEmpty() ? "" : String.join(TextDelimiter.COMMA.getDelimiter(), list);
				
				if (!ignoreLine(line, rowCount)) {
					var row = new Vector<String>();
					
					for (var field : rowData)
						row.add(field);
					
					if (rowData.length > maxColumn)
						maxColumn = rowData.length;
					
					data.add(row);
					validRowCount++;
				}
				
				rowCount++;

				if (importAll == false && validRowCount >= size)
					break;
			}
			
			try {
				reader.close();
			} catch (Exception e) { }
		} else {
			// Old method... Using naive splitting.
			String[] parts;
			
			while ((line = bufRd.readLine()) != null) {
				if (!ignoreLine(line, rowCount)) {
					var row = new Vector<String>();

					if (delimiterRegEx.length() == 0) {
						parts = new String[1];
						parts[0] = line;
					} else {
						parts = line.split(delimiterRegEx);
					}

					for (var entry : parts) {
						row.add(entry);
					}

					if (parts.length > maxColumn)
						maxColumn = parts.length;

					data.add(row);
					validRowCount++;
				}

				rowCount++;

				if (importAll == false && validRowCount >= size)
					break;
			}
		}

		// If the inputStream is passed in from parameter, do not close it
		if (tempIs != null)
			tempIs.close();

		boolean firstRowNames = importType == NETWORK_IMPORT || importType == TABLE_IMPORT;
		
		if (delimiters == null) {
			// Cytoscape attr file.
			var columnNames = new Vector<String>();
			columnNames.add("Key");
			columnNames.add(attrName);
			
			return new PreviewTableModel(data, columnNames, firstRowNames);
		} else {
			return new PreviewTableModel(data, new Vector<String>(), firstRowNames);
		}
	}

	private boolean ignoreLine(String line, int index) {
		return ((commentChar != null) && line.startsWith(commentChar)) || (line.trim().length() == 0)
				|| (index < startLine);
	}
	
	private void showEditDialog(int colIdx) {
		if (colIdx == lastDialogIndex && System.currentTimeMillis() - lastDialogTime < 100)
			return;
		
		lastDialogIndex = -1;
		lastDialogTime = 0;
		
		var parent = SwingUtilities.getWindowAncestor(PreviewTablePanel.this);
		
		var model = (PreviewTableModel) getPreviewTable().getModel();
		var attrName = model.getColumnName(colIdx);
		var availableNamespaces = TypeUtil.getAvailableNamespaces(importType);
		var availableTypes = TypeUtil.getAvailableTypes(importType);
		
		if (!tableImportContext.isKeyRequired() && availableTypes.contains(KEY)) {
			availableTypes = new ArrayList<>(availableTypes); // The original list cannot be modified!
			availableTypes.remove(KEY);
		}
		
		var attrEditorPanel = new AttributeEditorPanel(
				parent,
				attrName,
				availableTypes,
				availableNamespaces,
				types[colIdx],
				namespaces[colIdx],
				dataTypes[colIdx],
				listDelimiters[colIdx],
				iconManager
		);
// TODO: suggested feature (copy/paste column settings)
//		attrEditorPanel.getCopyButton().addActionListener(evt -> copyColumnSettings());
//		attrEditorPanel.getPasteButton().addActionListener(evt -> pasteColumnSettings());
//		attrEditorPanel.getPasteButton().setEnabled(copiedColumnSettings != null);
		
		if (LookAndFeelUtil.isWinLAF()) {
			attrEditorPanel.setBorder(
					BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("activeCaptionBorder")));
			attrEditorPanel.setBackground(UIManager.getColor("TableHeader.background"));
		}
		
		editDialog = new EditDialog(attrEditorPanel, parent, colIdx);
		
		var actionMap = attrEditorPanel.getActionMap();
		var inputMap = attrEditorPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "VK_ESCAPE");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "VK_ENTER");
		
		actionMap.put("VK_ESCAPE", new AbstractAction("VK_ESCAPE") {
			@Override
			public void actionPerformed(ActionEvent e) {
				disposeEditDialog();
			}
		});
		actionMap.put("VK_ENTER", new AbstractAction("VK_ENTER") {
			@Override
			public void actionPerformed(ActionEvent e) {
				disposeEditDialog();
			}
		});
		
		attrEditorPanel.addPropertyChangeListener("attributeName", evt -> {
			var name = (String) evt.getNewValue();
			
			if (name != null) {
				((PreviewTableModel) getPreviewTable().getModel()).setColumnName(colIdx, name);
				getPreviewTable().getColumnModel().getColumn(colIdx).setHeaderValue(name);
				update();
			}
		});
		attrEditorPanel.addPropertyChangeListener("namespace", evt -> {
			setNamespace(colIdx, (String) evt.getNewValue());
			update();
		});
		attrEditorPanel.addPropertyChangeListener("attributeType", evt -> {
			setType(colIdx, (SourceColumnSemantic) evt.getNewValue());
			update();
		});
		attrEditorPanel.addPropertyChangeListener("attributeDataType", evt -> {
			var newDataType = (AttributeDataType) evt.getNewValue();

			if (newDataType.isList())
				setListDelimiter(colIdx, attrEditorPanel.getListDelimiter());

			setDataType(colIdx, newDataType);
			update();
		});
		attrEditorPanel.addPropertyChangeListener("listDelimiter", evt -> {
			setListDelimiter(colIdx, (String) evt.getNewValue());
			update();
		});
		
		positionEditDialog();
		
		editDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				getPreviewTable().getTableHeader().repaint();
				attrEditorPanel.getAttributeNameTextField().requestFocusInWindow();
			}
			@Override
			public void windowClosed(WindowEvent e) {
				// Columns with empty names should not be imported!
				var name = ((PreviewTableModel) getPreviewTable().getModel()).getColumnName(colIdx);
				
				if (name == null || name.isBlank()) {
					setType(colIdx, NONE);
					update();
				}
				
				getPreviewTable().getTableHeader().repaint();
			}
		});
		
		editDialog.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				if (editDialog != null) {
					lastDialogIndex = editDialog.index;
					lastDialogTime = System.currentTimeMillis();
				}
				disposeEditDialog();
			}
			@Override
			public void windowGainedFocus(WindowEvent e) {
			}
		});
		
	    editDialog.pack();
		editDialog.setVisible(true);
	}

// TODO: suggested feature (copy/paste column settings)
//	private void copyColumnSettings() {
//		if (editDialog != null) {
//			copiedColumnSettings = editDialog.attrEditorPanel.getSettings();
//			getPasteColumnSettingsButton().setEnabled(true);
//		}
//	}
//	
//	private void pasteColumnSettings() {
//		if (editDialog != null && copiedColumnSettings != null)
//			editDialog.attrEditorPanel.setSettings(copiedColumnSettings);
//	}
//	
//	private void pasteColumnSettings(int[] selectedColumns) {
//		if (copiedColumnSettings != null) {
//			for (var colIdx : selectedColumns) {
//				setNamespace(colIdx, copiedColumnSettings.getNamespace());
//				setType(colIdx, copiedColumnSettings.getAttrType());
//				setDataType(colIdx, copiedColumnSettings.getAttrDataType());
//				setListDelimiter(colIdx, copiedColumnSettings.getListDelimiter());
//				update();
//			}
//		}
//	}

	private void positionEditDialog() {
		if (editDialog != null) {
			var hd = getPreviewTable().getTableHeader();
			
			// Get the column header location
			// (see: https://bugs.openjdk.java.net/browse/JDK-4408424)
			var ac = hd.getAccessibleContext().getAccessibleChild(editDialog.index).getAccessibleContext()
					.getAccessibleComponent();
			
			var screenPt = ac.getLocationOnScreen();
			var compPt = ac.getLocation();
			int xOffset = screenPt.x - compPt.x;
			int yOffset = screenPt.y - compPt.y + hd.getBounds().height;

			var pt = ac.getBounds().getLocation();
		    pt.translate(xOffset, yOffset);
		    
		    // This prevent the dialog from being positioned completely outside the parent panel
		    pt.x = Math.max(pt.x, getTableScrollPane().getLocationOnScreen().x - editDialog.getBounds().width);
		    pt.x = Math.min(pt.x, getTableScrollPane().getLocationOnScreen().x + getTableScrollPane().getBounds().width);
			
		    // Show the dialog right below the column header
		    editDialog.setLocation(pt);
		}
	}

	protected void disposeEditDialog() {
		if (editDialog != null) {
			editDialog.getContentPane().removeAll();
			editDialog.dispose();
			editDialog = null;
		}
	}

	private void update(Sheet sheet) throws IOException {
		var newModel = parseExcel(sheet, startLine);
		
		if (newModel.getRowCount() > 0) {
			var sheetName = sheet.getSheetName();
			
			dataTypes = TypeUtil.guessSheetDataTypes(newModel, decimalSeparator);
			types = TypeUtil.guessTypes(importType, newModel, dataTypes, getIgnoredTypes());
			listDelimiters = new String[newModel.getColumnCount()];
			namespaces = TypeUtil.getPreferredNamespaces(types);
			
			update(newModel, sheetName);
		}
		
		if (getPreviewTable() == null)
			throw new IllegalStateException("No data found in the Excel sheets.");
	}
	
	private void update(PreviewTableModel newModel, String name) {
		getPreviewTable().setName(name);
		getPreviewTable().setModel(newModel);
		
		ColumnResizer.adjustColumnPreferredWidths(getPreviewTable());
		update();
	}
	
	protected void update() {
		getPreviewTable().revalidate();
		getPreviewTable().repaint();
		getPreviewTable().getTableHeader().resizeAndRepaint();
		
		getSelectAllButton().setEnabled(false);
		getSelectNoneButton().setEnabled(false);
		
		if (types != null) {
			getSelectAllButton().setEnabled(Arrays.asList(types).contains(NONE));
			
			for (var t : types) {
				if (t != NONE) {
					getSelectNoneButton().setEnabled(true);
					break;
				}
			}
		}
	}
	
	/**
	 * Updates the current sheet's semantic types. It keeps previously set types unchanged, unless a type
	 * has been removed.
	 */
	protected void updateKeyType() {
		var name = getPreviewTable().getName();
		var model = (PreviewTableModel) getPreviewTable().getModel();
		
		if (name != null) {
			var currentTypes = types != null ? Arrays.asList(types) : Collections.emptyList();
			var ignoredTypes = getIgnoredTypes();
			var newTypes = TypeUtil.guessTypes(importType, model, dataTypes, ignoredTypes);
			
			if (tableImportContext.isKeyRequired() && !currentTypes.contains(KEY)) {
				for (int i = 0; i < newTypes.length; i++) {
					if (newTypes[i] == KEY) {
						setType(i, KEY);
						update();
						break;
					}
				}
			} else if (!tableImportContext.isKeyRequired() && currentTypes.contains(KEY)) {
				for (int i = 0; i < types.length; i++) {
					if (types[i] == KEY) {
						setType(i, newTypes.length > i ? newTypes[i] : NONE);
						update();
						break;
					}
				}
			}
		}
	}
	
	private Set<SourceColumnSemantic> getIgnoredTypes() {
		var set = new HashSet<SourceColumnSemantic>();
		
		if (tableImportContext != null && !tableImportContext.isKeyRequired())
			set.add(KEY);
		
		return set;
	}

	private JComboBox<Sheet> getSheetComboBox() {
		if (sheetComboBox == null) {
			sheetComboBox = new JComboBox<>();
			sheetComboBox.setVisible(false);
			sheetComboBox.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					setText(((Sheet)value).getSheetName());
					
					return this;
				}
			});
			sheetComboBox.addActionListener(evt -> {
				if (!updating) {
					disposeEditDialog();
					var sheet = (Sheet) sheetComboBox.getSelectedItem();
					
					try {
						if (sheet != null)
							update(sheet);
					} catch (IOException e) {
						logger.error("Cannot preview Excel sheet '" + sheet.getSheetName() + "'.", e);
					}
				}
			});
		}
		
		return sheetComboBox;
	}
	
	private JButton getSelectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton("Select All");
			selectAllButton.addActionListener(evt -> {
				disposeEditDialog();
				
				// Replace types "NONE" with new guessed types.
				// NOTE: This must not change the current data types!
				var ignoredTypes = getIgnoredTypes();
				ignoredTypes.addAll(Arrays.asList(types));
				
				var newTypes = TypeUtil.guessTypes(importType, getPreviewTable().getModel(), dataTypes, ignoredTypes);
				
				for (int i = 0; i < newTypes.length; i++) {
					if (types.length > i && types[i] == NONE)
						setType(i, newTypes[i]);
				}
				
				update();
			});
			
			if (isAquaLAF()) {
				selectAllButton.putClientProperty("JButton.buttonType", "gradient");
				selectAllButton.putClientProperty("JComponent.sizeVariant", "small");
			}
			
			selectAllButton.setEnabled(false);
		}
		
		return selectAllButton;
	}
	
	private JButton getSelectNoneButton() {
		if (selectNoneButton == null) {
			selectNoneButton = new JButton("Select None");
			selectNoneButton.addActionListener(evt -> {
				disposeEditDialog();
				fillTypes(NONE);
				update();
			});
			
			if (isAquaLAF()) {
				selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
				selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
			}
			
			selectNoneButton.setEnabled(false);
		}
		
		return selectNoneButton;
	}
	
	private JScrollPane getTableScrollPane() {
		if (tableScrollPane == null) {
			tableScrollPane = new JScrollPane(getPreviewTable());
			tableScrollPane.getHorizontalScrollBar().addAdjustmentListener(evt -> {
				// Realign the Attribute Editor Dialog if it is open
				if (!evt.getValueIsAdjusting())
					positionEditDialog();
			});
		}
		
		return tableScrollPane;
	}
	
	public class PreviewTableModel extends DefaultTableModel {
		
		private boolean firstRowNames;
		private Vector<Class<?>> predefinedClasses;
		
		public PreviewTableModel(
				Vector<Vector<String>> data,
				Vector<String> columnNames,
				Vector<Class<?>> columnTypes,
				boolean firstRowNames
		) {
			super(data, columnNames);
			this.firstRowNames = firstRowNames;
			this.predefinedClasses = columnTypes;
		}

		public PreviewTableModel(Vector<Vector<String>> data, Vector<String> columnNames, boolean firstRowNames) {
			this(data, columnNames, null, firstRowNames);
		}

		public boolean hasPredefinedTypes() {
			return predefinedClasses != null;
		}

		public Class<?> getPredefinedColumnClass(int column) {
			if (predefinedClasses != null && column < predefinedClasses.size()) {
				return predefinedClasses.get(column);
			}

			return String.class;
		}

		@SuppressWarnings("unchecked")
		public void setColumnName(int column, String name) {
			if (columnIdentifiers.isEmpty())
				columnIdentifiers.setSize(getColumnCount());
			
			if (columnIdentifiers.size() > column) {
				columnIdentifiers.set(column, name);
				this.fireTableChanged(new TableModelEvent(this));
			}
		}

		public void setFirstRowNames(boolean firstRowNames) {
			this.firstRowNames = firstRowNames;
			this.fireTableStructureChanged();
		}
		
		public boolean isFirstRowNames() {
			return firstRowNames;
		}
		
		@Override
		public int getRowCount() {
			return firstRowNames ? dataVector.size() - 1 : dataVector.size();
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public int getColumnCount() {
			return dataVector.size() > 0 ? ((Vector<String>) dataVector.get(0)).size() : 0;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public String getColumnName(int column) {
			String colName = null;
			
			// First check is the name has been overwritten by the user
			if (columnIdentifiers.size() > column)
				colName = (String) columnIdentifiers.get(column);
			
			if (colName == null) {
				if (firstRowNames && dataVector.size() > 0) {
					// No overwritten name and should use the first data row as column names
					Vector<String> firstRow = dataVector.get(0);
					
					if (firstRow != null && firstRow.size() > column) {
						colName = firstRow.get(column);
					}
				}
			}
			
			if(colName == null) {
				// Just return a default name
				colName = "Column " + (column + 1);
			}
			
			return colName;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (firstRowNames)
				row++;
			
			return super.getValueAt(row, column);
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (firstRowNames)
				row++;
			
			super.setValueAt(aValue, row, column);
		}
	}
	
	private class PreviewTableHeaderRenderer extends JPanel implements TableCellRenderer {
		
		private final JLabel typeLabel;
		private final JLabel nameLabel;
		private final JLabel editLabel;
		
		PreviewTableHeaderRenderer() {
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setBackground(UIManager.getColor("TableHeader.background"));
			
			nameLabel = new JLabel();
			nameLabel.setFont(UIManager.getFont("TableHeader.font"));
			
			typeLabel = new JLabel();
			typeLabel.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
			
			editLabel = new JLabel(IconManager.ICON_CARET_LEFT);
			editLabel.setFont(iconManager.getIconFont(12.0f));
			editLabel.setHorizontalAlignment(JLabel.CENTER);
			
			// Forces the edit label to always have the same size, no matter its state
			var tempLabel = new JLabel(IconManager.ICON_CARET_DOWN);
			tempLabel.setFont(iconManager.getIconFont(12.0f));
			LookAndFeelUtil.equalizeSize(editLabel, tempLabel);
			
			var layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(6)
					.addComponent(typeLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(nameLabel)
					.addGap(5, 5, Short.MAX_VALUE)
					.addComponent(editLabel)
					.addGap(6)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(4)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(typeLabel)
							.addComponent(nameLabel)
							.addComponent(editLabel)
					)
					.addGap(4)
			);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS,
		                                               boolean hasF, int row, int col) {
			nameLabel.setText(val != null && !val.toString().isBlank() ? val.toString() : "N/A");
			
			var fgColor = UIManager.getColor("TableHeader.foreground");
			
			// Set type icon
			if (types != null && types.length > col) {
				SourceColumnSemantic type = types[col];
				
				if (type == null)
					type = NONE;
				
				var dataType = dataTypes != null && dataTypes.length > col ? dataTypes[col] : TYPE_STRING;
				
				typeLabel.setForeground(type.getForeground());
				typeLabel.setText(type.getText());
				
				setToolTipText("<html>" + type.getDescription() + " - <i>" + dataType.getDescription() + "</i></html>");
				
				if (type == NONE)
					fgColor = UIManager.getColor("TextField.inactiveForeground");
			} else {
				fgColor = UIManager.getColor("TextField.inactiveForeground");
			}
			
			if (editDialog != null && editDialog.index == col && editDialog.isVisible())
				editLabel.setText(IconManager.ICON_CARET_DOWN);
			else
				editLabel.setText(IconManager.ICON_CARET_LEFT);
			
			nameLabel.setForeground(fgColor);
			
			this.invalidate();

			return this;
		}
	}
	
	private class PreviewTableCellRenderer extends DefaultTableCellRenderer {
		
		public PreviewTableCellRenderer() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		                                               boolean hasFocus, int row, int column) {
			setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
			
			setFont(getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			setFont(getFont().deriveFont(getColumnIndex(KEY) == column ? Font.BOLD : Font.PLAIN));
			
			setText(value == null ? "" : value.toString());

			if (isImported(column))
				setForeground(table.getForeground());
			else
				setForeground(UIManager.getColor("TextField.inactiveForeground"));
			
			var dataType = getDataType(column);
			
			if (dataType == TYPE_INTEGER || dataType == TYPE_LONG || dataType == TYPE_FLOATING)
				setHorizontalAlignment(JLabel.RIGHT);
			else if (dataType == TYPE_BOOLEAN)
				setHorizontalAlignment(JLabel.CENTER);
			else
				setHorizontalAlignment(JLabel.LEFT);
			
			return this;
		}
	}
	
	private class EditDialog extends JDialog {
		
		final int index;
		final AttributeEditorPanel attrEditorPanel;
		
		EditDialog(AttributeEditorPanel attrEditorPanel, Window parent, int index) {
			super(parent, ModalityType.MODELESS);
			this.index = index;
			this.attrEditorPanel = attrEditorPanel;
			
			setUndecorated(true);
			
			getContentPane().add(attrEditorPanel);
		}
	}

// TODO: suggested feature (copy/paste column settings)
//	private class ColumnSelectorDialog extends JDialog {
//		
//		ColumnSelectorDialog(Window parent) {
//			super(parent, "Paste Column Settings", ModalityType.MODELESS);
//			
//			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			setResizable(false);
//			
//			var columnList = new JList<String>(getAttributeNames());
//			columnList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//			
//			var scrollPane = new JScrollPane(
//					columnList,
//					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
//			);
//			
//			var okButton = new JButton(new AbstractAction("OK") {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					pasteColumnSettings(columnList.getSelectedIndices());
//					dispose();
//				}
//			});
//			var cancelButton = new JButton(new AbstractAction("Cancel") {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					dispose();
//				}
//			});
//			
//			var buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
//			
//			var layout = new GroupLayout(getContentPane());
//			getContentPane().setLayout(layout);
//			layout.setAutoCreateContainerGaps(true);
//			layout.setAutoCreateGaps(false);
//			
//			layout.setHorizontalGroup(layout.createParallelGroup(TRAILING, true)
//					.addComponent(scrollPane, DEFAULT_SIZE, 360, Short.MAX_VALUE)
//					.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
//			);
//			layout.setVerticalGroup(layout.createSequentialGroup()
//					.addComponent(scrollPane, DEFAULT_SIZE, 240, Short.MAX_VALUE)
//					.addPreferredGap(RELATED)
//					.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
//			);
//			
//			getRootPane().setDefaultButton(okButton);
//			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okButton.getAction(), cancelButton.getAction());
//		}
//	}
}
