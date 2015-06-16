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
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.ImportType.NETWORK_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.ONTOLOGY_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.TABLE_IMPORT;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ALIAS;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.KEY;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.NONE;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ONTOLOGY;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.TAXON;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.accessibility.AccessibleComponent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextFileDelimiters;
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

/**
 * General purpose preview table panel.
 */
@SuppressWarnings("serial")
public class PreviewTablePanel extends JPanel {

	private static final float ICON_FONT_SIZE = 14.0f;
	private static final String DEF_TAB_MESSAGE = "Data File Preview Window";
	
	// Lines start with this char will be ignored.
	private String commentChar;
	private boolean loadFlag;

	// Tracking attribute data type.
	private Map<String, SourceColumnSemantic[]> typeMap;
	private Map<String, AttributeDataType[]> dataTypeMap;
	
	private Set<?> keySet;

	/*
	 * GUI Components
	 */
	private JTable previewTable;

	// Tables for each worksheet.
	private Map<String, JTable> previewTables;
	private JTabbedPane previewTabbedPane;
	
	private JRadioButton showAllRadioButton;
	private JRadioButton counterRadioButton;
	private JSpinner counterSpinner;
	private JButton reloadButton;
	
	private ButtonGroup importTypeButtonGroup;
	
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private ImportType importType;
	private String listDelimiter;
	
	private VetoableChangeListener vetoableChangeListener;
	private int editingColumnIndex = -1;

	private final IconManager iconManager;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(PreviewTablePanel.class);

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel(final IconManager iconManager) {
		this(TABLE_IMPORT, iconManager);
	}

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel(final ImportType importType, final IconManager iconManager) {
		this.importType = importType;
		this.iconManager = iconManager;

		typeMap = new HashMap<>();
		dataTypeMap = new HashMap<>();

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
		
		final JLabel legendLabel = new JLabel("Legend:");
		final JLabel pkLabel = new JLabel("Key");
		final JLabel aliasLabel = new JLabel("Alias");
		final JLabel ontologyLabel = new JLabel("Ontology");
		final JLabel taxonLabel = new JLabel("Taxon");
		
		final JLabel pkIconLabel = new JLabel(KEY.getText());
		pkIconLabel.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
		
		final JLabel aliasIconLabel = new JLabel(ALIAS.getText());
		aliasIconLabel.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
		aliasIconLabel.setForeground(ALIAS.getForeground());
		
		final JLabel ontologyIconLabel = new JLabel(ONTOLOGY.getText());
		ontologyIconLabel.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
		ontologyIconLabel.setForeground(ONTOLOGY.getForeground());
		
		final JLabel taxonIconLabel = new JLabel(TAXON.getText());
		taxonIconLabel.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
		taxonIconLabel.setForeground(TAXON.getForeground());
		
		final JLabel counterLabel = new JLabel("entries");
		counterLabel.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
		
		previewTabbedPane = new JTabbedPane();

		previewTables = new HashMap<String, JTable>();
		previewTable = new JTable();
		previewTable.setName(DEF_TAB_MESSAGE);
		previewTable.setOpaque(false);
		
		final JTableHeader hd = previewTable.getTableHeader();
		hd.setReorderingAllowed(false);
		hd.setDefaultRenderer(new PreviewTableHeaderRenderer());

		previewTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				tableTabbedPaneStateChanged(evt);
			}
		});

		previewTabbedPane.addTab(DEF_TAB_MESSAGE, new PreviewTab(previewTable, null));
		
		/*
		 * Setting table properties
		 */
		previewTable.setCellSelectionEnabled(false);
		previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		previewTable.setDefaultEditor(Object.class, null);

		importTypeButtonGroup = new ButtonGroup();
		importTypeButtonGroup.add(getShowAllRadioButton());
		importTypeButtonGroup.add(getCounterRadioButton());
		importTypeButtonGroup.setSelected(getCounterRadioButton().getModel(), true);
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		final JSeparator sep = new JSeparator();

		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(previewTabbedPane, DEFAULT_SIZE, 250, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
					.addComponent(getShowAllRadioButton())
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getCounterRadioButton())
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getCounterSpinner(), PREFERRED_SIZE, 60, PREFERRED_SIZE)
					.addComponent(counterLabel)
					.addGap(20, 20, Short.MAX_VALUE)
					.addComponent(getReloadButton())
				)
				.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(legendLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(UNRELATED)
						.addComponent(pkIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(2)
						.addComponent(pkLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(UNRELATED)
						.addComponent(aliasIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(2)
						.addComponent(aliasLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(UNRELATED)
						.addComponent(ontologyIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(2)
						.addComponent(ontologyLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(UNRELATED)
						.addComponent(taxonIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(2)
						.addComponent(taxonLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(previewTabbedPane, DEFAULT_SIZE, 200, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(CENTER)
						.addComponent(getShowAllRadioButton())
						.addComponent(getCounterRadioButton())
						.addComponent(getCounterSpinner(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(counterLabel)
						.addComponent(getReloadButton())
				)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(CENTER)
						.addComponent(legendLabel)
						.addComponent(pkIconLabel)
						.addComponent(pkLabel)
						.addComponent(aliasIconLabel)
						.addComponent(aliasLabel)
						.addComponent(ontologyIconLabel)
						.addComponent(ontologyLabel)
						.addComponent(taxonIconLabel)
						.addComponent(taxonLabel)
				)
		);
		
		if (importType != ONTOLOGY_IMPORT) {
			sep.setVisible(false);
			legendLabel.setVisible(false);
			pkIconLabel.setVisible(false);
			pkLabel.setVisible(false);
			aliasIconLabel.setVisible(false);
			aliasLabel.setVisible(false);
			ontologyIconLabel.setVisible(false);
			ontologyLabel.setVisible(false);
			taxonIconLabel.setVisible(false);
			taxonLabel.setVisible(false);
		}
	}
	
	public PreviewTab getSelectedPreviewTab() {
		return (PreviewTab) previewTabbedPane.getSelectedComponent();
	}
	
	public JTable getSelectedPreviewTable() {
		final PreviewTab tab = getSelectedPreviewTab();

		return tab == null ? null : tab.table;
	}
	
	/**
	 * Get selected tab name.
	 * @return name of the selected tab (i.e., sheet name)
	 */
	public String getSelectedTabName() {
		final int index = previewTabbedPane.getSelectedIndex();
		
		return index >= 0 ? previewTabbedPane.getTitleAt(index) : null;
	}

	public JTable getPreviewTable(int index) {
		return ((PreviewTab) previewTabbedPane.getComponentAt(index)).table;
	}

	public int getTableCount() {
		return previewTabbedPane.getTabCount();
	}

	public String getSheetName(final int index) {
		return previewTabbedPane.getTitleAt(index);
	}

	public SourceColumnSemantic[] getTypes(final String tabName) {
		return typeMap.get(tabName);
	}
	
	public SourceColumnSemantic[] getCurrentTypes() {
		return getTypes(getSelectedTabName());
	}
	
	public void setType(final String tabName, final int index, SourceColumnSemantic type) {
		if (index < 0)
			return;
		
		final SourceColumnSemantic[] types = getTypes(tabName);
		
		if (types != null && types.length > index) {
			// First replace the index that currently has this unique type by the default type
			if (type.isUnique())
				replaceType(tabName, type, TypeUtil.getDefaultType(importType));
			
			types[index] = type;
		}
	}
	
	protected void fillTypes(final String tabName, final SourceColumnSemantic newValue) {
		final SourceColumnSemantic[] types = getTypes(tabName);
		
		if (types != null)
			Arrays.fill(types, newValue);
	}
	
	protected void replaceType(final String tabName, final SourceColumnSemantic type1, final SourceColumnSemantic type2) {
		final SourceColumnSemantic[] types = getTypes(tabName);
		
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				if (types[i] == type1)
					types[i] = type2;
			}
		}
	}
	
	public AttributeDataType[] getDataTypes(final String tabName) {
		return dataTypeMap.get(tabName);
	}
	
	public AttributeDataType[] getCurrentDataTypes() {
		return getDataTypes(getSelectedTabName());
	}

	public AttributeDataType getDataType(final String tabName, final int index) {
		final AttributeDataType[] dataTypes = getDataTypes(tabName);
		
		if (dataTypes != null && dataTypes.length > index)
			return dataTypes[index];
		
		return null;
	}
	
	public void setDataType(final String tabName, final int index, final AttributeDataType newValue) {
		if (index < 0)
			return;
		
		final AttributeDataType[] dataTypes = getDataTypes(tabName);
		
		if (dataTypes != null && dataTypes.length > index && dataTypes[index] != newValue) {
			dataTypes[index] = newValue;
			changes.firePropertyChange(ImportTablePanel.ATTR_DATA_TYPE_CHANGED, null, dataTypes);
		}
	}
	
	public FileType getFileType() {
		final String sheetName = getSheetName(previewTabbedPane.getSelectedIndex());

		if (sheetName.startsWith("gene_association"))
			return FileType.GENE_ASSOCIATION_FILE;

		return FileType.ATTRIBUTE_FILE;
	}

	private void tableTabbedPaneStateChanged(ChangeEvent evt) {
		if ((previewTabbedPane.getSelectedComponent() != null)
				&& getSelectedPreviewTable() != null
				&& (loadFlag == true)) {
			changes.firePropertyChange(ImportTablePanel.SHEET_CHANGED, null, null);
		}
	}

	/**
	 * Load file and show preview.
	 */
	public void setPreviewTable(
			final Workbook wb,
			String fileType,
			String fileFullName,
			InputStream tempIs,
			List<String> delimiters,
			TableCellRenderer renderer,
			int size,
			final String commentLineChar,
			final int startLine
	) throws IOException {
		if (tempIs == null)
			return;

		TableCellRenderer curRenderer = renderer;

		if ((commentLineChar != null) && (commentLineChar.trim().length() != 0))
			this.commentChar = commentLineChar;
		else
			this.commentChar = null;

		if (curRenderer == null)
			curRenderer = new PreviewTableCellRenderer();

		for (int i = 0; i < previewTabbedPane.getTabCount(); i++)
			previewTabbedPane.removeTabAt(i);

		previewTables = new HashMap<String, JTable>();

		TableModel newModel;
		boolean isTable = false;
		
		if (fileType != null) {
			if (fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension())
					|| fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {
				isTable = true;

				if (wb.getNumberOfSheets() == 0)
					throw new IllegalStateException("No sheet found in the workbook.");

				/*
				 * Load each sheet in the workbook.
				 */
				final Sheet sheet = wb.getSheetAt(0);
				newModel = parseExcel(size, curRenderer, sheet, startLine);

				if (newModel.getRowCount() == 0)
					throw new IllegalStateException("No data found in the Excel sheet.");

				final String sheetName = wb.getSheetName(0);
				
				typeMap.put(sheetName, TypeUtil.guessTypes(importType, newModel));
				dataTypeMap.put(sheetName, TypeUtil.guessDataTypes(newModel));
				addTableTab(newModel, sheetName, curRenderer);
			}
		}

		if (!isTable) {
			newModel = parseText(tempIs, size, curRenderer, delimiters, startLine);

			String tabName;
			String[] urlParts = fileFullName.split("/");
			
			if (urlParts.length > 0 && !fileFullName.isEmpty())
				tabName = urlParts[urlParts.length - 1];
			else
				tabName = "Source Table";
			
			typeMap.put(tabName, TypeUtil.guessTypes(importType, newModel));
			dataTypeMap.put(tabName, TypeUtil.guessDataTypes(newModel));
			addTableTab(newModel, tabName, curRenderer);
		}

		loadFlag = true;
	}
	
	public void setFirstRowAsColumnNames() {
		final PreviewTab tab = getSelectedPreviewTab();
		final JTable table = tab != null ? tab.table : null;
		
		if (table != null) {
			final DefaultTableModel model = (DefaultTableModel) table.getModel();
			final Vector<String> colNames = new Vector<>();
			
			for (int i = 0; i < table.getColumnCount(); i++) {
				String name = model.getValueAt(0, i).toString();
				colNames.add(name);
				table.getColumnModel().getColumn(i).setHeaderValue(name);
			}
	
			model.removeRow(0);
			model.setColumnIdentifiers(colNames);
			typeMap.put(table.getName(), TypeUtil.guessTypes(importType, model));
			tab.update();
		}
	}

	protected boolean isCytoscapeAttributeFile(final URL sourceURL) throws IOException {
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

	private void addTableTab(final TableModel newModel, final String tabName, final TableCellRenderer renderer) {
		final JTable table = new JTable(newModel);
		table.setName(tabName);
		previewTables.put(tabName, table);

		final PreviewTab tab = new PreviewTab(table, renderer);
		previewTabbedPane.addTab(tabName, tab);
	}

	/**
	 * Based on the file type, setup the initial column names.
	 */
	private Vector<String> getDefaultColumnNames(final int colCount) {
		final Vector<String> colNames = new Vector<>();

		for (int i = 0; i < colCount; i++)
			colNames.add("Column " + (i + 1));

		return colNames;
	}

	public int checkKeyMatch(final int targetColumn) {
		int matched = 0;

		if (keySet != null && !keySet.isEmpty()) {
			final TableModel curModel = getSelectedPreviewTable().getModel();
	
			try {
				curModel.getValueAt(0, targetColumn);
			} catch (ArrayIndexOutOfBoundsException e) {
				return 0;
			}
	
			final int rowCount = curModel.getRowCount();
			
			for (int i = 0; i < rowCount; i++) {
				final Object val = curModel.getValueAt(i, targetColumn);
				
				if (val != null && keySet.contains(val))
					matched++;
			}
		}
		
		return matched;
	}
	
	/**
	 * Returns the first index of the table column that has the passed type.
	 */
	protected int getColumnIndex(final String tabName, final SourceColumnSemantic type) {
		final SourceColumnSemantic[] types = getTypes(tabName);
		
		if (types != null)
			return Arrays.asList(types).indexOf(type);
		
		return -1;
	}
	
	protected void setAliasColumn(final int index, final boolean flag) {
		final SourceColumnSemantic[] types = getCurrentTypes();

		if (types != null && types.length > index) {
			types[index] = flag ? ALIAS : ATTR;
			
			if (getSelectedPreviewTab() != null)
				getSelectedPreviewTab().update();
		}
	}
	
	protected boolean isImported(final int index) {
		final SourceColumnSemantic[] types = getCurrentTypes();
		
		if (types != null && types.length > index)
			return types[index] != NONE;

		return false;
	}

	protected JRadioButton getShowAllRadioButton() {
		if (showAllRadioButton == null) {
			showAllRadioButton = new JRadioButton("Show all entries in the file");
			showAllRadioButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
		}
		
		return showAllRadioButton;
	}
	
	protected JRadioButton getCounterRadioButton() {
		if (counterRadioButton == null) {
			counterRadioButton = new JRadioButton("Show first");
			counterRadioButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
		}
		
		return counterRadioButton;
	}
	
	protected JSpinner getCounterSpinner() {
		if (counterSpinner == null) {
			final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 1, 10000000, 10);
			counterSpinner = new JSpinner(spinnerModel);
			counterSpinner.setToolTipText(
					"<html><body>Click <strong><i>Refresh Preview</i></strong> button to update the table</body></html>");
			counterSpinner.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
			
			counterSpinner.addMouseWheelListener(new MouseWheelListener() {
				@Override
				@SuppressWarnings("unchecked")
				public void mouseWheelMoved(final MouseWheelEvent evt) {
					final JSpinner source = (JSpinner) evt.getSource();

					final SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
					final Integer oldValue = (Integer) source.getValue();
					final int intValue = oldValue.intValue() - (evt.getWheelRotation() * model.getStepSize().intValue());
					final Integer newValue = new Integer(intValue);

					if (model.getMaximum().compareTo(newValue) >= 0 && model.getMinimum().compareTo(newValue) <= 0)
						source.setValue(newValue);
				}
			});
		}
		
		return counterSpinner;
	}
	
	protected JButton getReloadButton() {
		if (reloadButton == null) {
			reloadButton = new JButton(IconManager.ICON_REFRESH);
			reloadButton.setFont(iconManager.getIconFont(12.0f));
			reloadButton.setToolTipText("Refresh Preview");
			reloadButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
		}
		
		return reloadButton;
	}
	
	private TableModel parseExcel(int size, TableCellRenderer renderer, final Sheet sheet, int startLine)
			throws IOException {
		if (size == -1)
			size = Integer.MAX_VALUE;

		int maxCol = 0;
		final Vector<Object> data = new Vector<>();

		int rowCount = 0;
		Row row;

		while (((row = sheet.getRow(rowCount)) != null) && (rowCount < size)) {
			if (rowCount >= startLine) {
				final Vector<Object> rowVector = new Vector<>();

				if (maxCol < row.getPhysicalNumberOfCells())
					maxCol = row.getPhysicalNumberOfCells();

				for (short j = 0; j < maxCol; j++) {
					Cell cell = row.getCell(j);

					if (cell == null) {
						rowVector.add(null);
					} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						rowVector.add(cell.getRichStringCellValue().getString());
					} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						final Double dblValue = cell.getNumericCellValue();
						final Integer intValue = dblValue.intValue();

						if (intValue.doubleValue() == dblValue) {
							rowVector.add(intValue.toString());
						} else {
							rowVector.add(dblValue.toString());
						}
					} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
						rowVector.add(Boolean.toString(cell.getBooleanCellValue()));
					} else if ((cell.getCellType() == Cell.CELL_TYPE_BLANK)
							|| (cell.getCellType() == Cell.CELL_TYPE_ERROR)) {
						rowVector.add(null);
					} else {
						rowVector.add(null);
					}
				}

				data.add(rowVector);
			}

			rowCount++;
		}

		return new DefaultTableModel(data, getDefaultColumnNames(maxCol));
	}
	
	private TableModel parseText(InputStream tempIs, int size, TableCellRenderer renderer, List<String> delimiters,
			int startLine) throws IOException {
		String line;
		String attrName = "Attr1";
		Vector<Vector<String>> data;
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

				for (String delimiter : delimiters) {
					delimiterBuffer.append(delimiter);
				}

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
		boolean importAll = false;

		if (size == -1)
			importAll = true;

		// Distinguish between CSV files and everything else.
		// TODO: Since the CSV parser allows for other delimiters, consider exploring using it for everything.

		// The variables are modified by both the new method and the old method.
		int counter = 0;
		maxColumn = 0;
		data = new Vector<>();
		
		if (delimiters != null && delimiters.contains(TextFileDelimiters.COMMA.toString()) && delimiters.size() == 1) {
			// Only if there is exactly one delimiter and that delimiter is a
			// comma should you read the file using OpenCSV
			// New method... Using OpenCSV
			final CSVReader reader = new CSVReader(bufRd);
			String[] rowData; // Note that rowData is roughly equivalent to
								// "parts" in the old code.
			while ((rowData = reader.readNext()) != null) {
				final Vector<String> row = new Vector<>();
				
				for (String field : rowData)
					row.add(field);
				
				if (rowData.length > maxColumn)
					maxColumn = rowData.length;
				
				data.add(row);
				counter++;

				if (importAll == false && counter >= size)
					break;
			}
			
			try {
				reader.close();
			} catch (Exception e) { }
		} else {
			// Old method... Using naive splitting.
			String[] parts;
			
			while ((line = bufRd.readLine()) != null) {
				if (((commentChar != null) && line.startsWith(commentChar)) || (line.trim().length() == 0)
						|| (counter < startLine)) {
					// ignore
				} else {
					final Vector<String> row = new Vector<>();

					if (delimiterRegEx.length() == 0) {
						parts = new String[1];
						parts[0] = line;
					} else {
						parts = line.split(delimiterRegEx);
					}

					for (String entry : parts) {
						row.add(entry);
					}

					if (parts.length > maxColumn)
						maxColumn = parts.length;

					data.add(row);
				}

				counter++;

				if (importAll == false && counter >= size)
					break;
			}
		}

		// If the inputStream is passed in from parameter, do not close it
		if (tempIs != null)
			tempIs.close();

		if (delimiters == null) {
			// Cytoscape attr file.
			final Vector<String> columnNames = new Vector<>();
			columnNames.add("Key");
			columnNames.add(attrName);
			
			return new DefaultTableModel(data, columnNames);
		} else {
			return new DefaultTableModel(data, getDefaultColumnNames(maxColumn));
		}
	}
	
	private final class TableHeaderListener extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			final JTable table = getSelectedPreviewTable();
			final JTableHeader hd = table.getTableHeader();
			final TableColumnModel columnModel = table.getColumnModel();
			final int colIdx = columnModel.getColumnIndexAtX(e.getX());
			final TableColumn column = columnModel.getColumn(colIdx);

			final Window parent = SwingUtilities.getWindowAncestor(PreviewTablePanel.this);
			
			final SourceColumnSemantic[] types = getCurrentTypes();
			final AttributeDataType[] dataTypes = getCurrentDataTypes();

			/*
			 * Right click: This action pops up an dialog to edit the attribute type and name.
			 */
			final AttributeEditorPanel attrEditorPanel = new AttributeEditorPanel(
					parent,
					column.getHeaderValue().toString(),
					TypeUtil.getAvailableTypes(importType),
					types[colIdx],
					dataTypes[colIdx],
					listDelimiter,
					iconManager
			);
			
			final JDialog dialog = new JDialog(parent, ModalityType.MODELESS);
			dialog.setUndecorated(true);
			dialog.add(attrEditorPanel);
			
			final ActionMap actionMap = attrEditorPanel.getActionMap();
			final InputMap inputMap = attrEditorPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "VK_ESCAPE");
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "VK_ENTER");
			
			actionMap.put("VK_ESCAPE", new AbstractAction("VK_ESCAPE") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
					editingColumnIndex = -1;
				}
			});
			actionMap.put("VK_ENTER", new AbstractAction("VK_ENTER") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
					editingColumnIndex = -1;
					updateTable(colIdx, attrEditorPanel);
				}
			});
			
			// This allows to switch focus between the different text fields in the dialog
			// and only dispose it if the user clicks outside the dialog
			vetoableChangeListener = new VetoableChangeListener() {
				private boolean gained;
				
				@Override
				public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
					if (evt.getNewValue() == dialog)
						gained = true;
					
					if (gained && evt.getNewValue() != dialog) {
						if (vetoableChangeListener != null)
							KeyboardFocusManager.getCurrentKeyboardFocusManager().removeVetoableChangeListener(
									"focusedWindow", vetoableChangeListener);
						
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dialog.dispose();
								editingColumnIndex = -1;
								updateTable(colIdx, attrEditorPanel);
							}
						});
						vetoableChangeListener = null;
					}
				}
			};
			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.addVetoableChangeListener("focusedWindow", vetoableChangeListener);
			
			// Get the column header location
			// (see: https://bugs.openjdk.java.net/browse/JDK-4408424)
			final AccessibleComponent ac = hd.getAccessibleContext().getAccessibleChild(colIdx)
					.getAccessibleContext().getAccessibleComponent();
			
			final Point screenPt = ac.getLocationOnScreen();
			final Point compPt = ac.getLocation();
			int xOffset = screenPt.x - compPt.x;
			int yOffset = screenPt.y - compPt.y + hd.getBounds().height;

		    final Point pt = ac.getBounds().getLocation();
		    pt.translate(xOffset, yOffset);
			
		    // Show the dialog right below the column header
		    dialog.setLocation(pt);
			dialog.pack();
			
			editingColumnIndex = colIdx;
			
			dialog.setVisible(true);
			dialog.requestFocus();
		}

		private void updateTable(final int colIdx, final AttributeEditorPanel attrEditorPanel) {
			final JTable table = getSelectedPreviewTable();
			final JTableHeader hd = table.getTableHeader();
			final TableColumnModel columnModel = table.getColumnModel();
			final TableColumn column = columnModel.getColumn(colIdx);

			final String tabName = getSelectedTabName();
			
			final String name = attrEditorPanel.getName();
			final SourceColumnSemantic newType = attrEditorPanel.getType();
			final AttributeDataType newDataType = attrEditorPanel.getDataType();

			if (name != null) {
				column.setHeaderValue(name);
				hd.resizeAndRepaint();

				if (newDataType.isList()) {
					listDelimiter = attrEditorPanel.getListDelimiterType();

					changes.firePropertyChange(ImportTablePanel.LIST_DELIMITER_CHANGED, null,
							attrEditorPanel.getListDelimiterType());
				}

				final Vector<Object> colNamePair = new Vector<>();
				colNamePair.add(colIdx);
				colNamePair.add(name);
				changes.firePropertyChange(ImportTablePanel.ATTRIBUTE_NAME_CHANGED, null, colNamePair);

				setType(tabName, colIdx, newType);
				setDataType(tabName, colIdx, newDataType);

				if (getSelectedPreviewTab() != null)
					getSelectedPreviewTab().update();
			}
		}
	}
	
	protected class PreviewTab extends JPanel {
		
		private final JTable table;
		private final JButton selectAllButton;
		private final JButton selectNoneButton;

		private PreviewTab(final JTable table, final TableCellRenderer renderer) {
			this.table = table;
			
			if (LookAndFeelUtil.isAquaLAF())
				this.setOpaque(false);
			
			final JLabel instructionLabel = new JLabel("Click on a column to edit it.");
			instructionLabel.setFont(instructionLabel.getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));
			
			selectAllButton = new JButton("Select All");
			selectAllButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					replaceType(table.getName(), NONE, TypeUtil.getDefaultType(importType));
					update();
				}
			});
			selectAllButton.putClientProperty("JButton.buttonType", "gradient"); // Mac OS X only
			selectAllButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
			selectAllButton.setEnabled(false);
			
			selectNoneButton = new JButton("Select None");
			selectNoneButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					fillTypes(table.getName(), NONE);
					update();
				}
			});
			selectNoneButton.putClientProperty("JButton.buttonType", "gradient"); // Mac OS X only
			selectNoneButton.putClientProperty("JComponent.sizeVariant", "small"); // Mac OS X only
			selectNoneButton.setEnabled(false);
			
			LookAndFeelUtil.equalizeSize(selectAllButton, selectNoneButton);
			
			final JScrollPane scrollPane = new JScrollPane(table);
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addContainerGap()
							.addComponent(instructionLabel)
							.addGap(20, 20, Short.MAX_VALUE)
							.addComponent(selectAllButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(selectNoneButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addContainerGap()
					)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(instructionLabel)
							.addComponent(selectAllButton)
							.addComponent(selectNoneButton)
					)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			
			table.setCellSelectionEnabled(false);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setDefaultEditor(Object.class, null);

			if (importType == NETWORK_IMPORT) {
				final TableCellRenderer netRenderer = new PreviewTableCellRenderer();
				table.setDefaultRenderer(Object.class, netRenderer);
			} else if (renderer != null) {
				table.setDefaultRenderer(Object.class, renderer);
			}

			final JTableHeader hd = table.getTableHeader();
			hd.setReorderingAllowed(false);
			hd.setDefaultRenderer(new PreviewTableHeaderRenderer());

			table.getTableHeader().addMouseListener(new TableHeaderListener());

			ColumnResizer.adjustColumnPreferredWidths(table);

			table.revalidate();
			table.repaint();
			table.getTableHeader().repaint();
		}
		
		public JTable getTable() {
			return table;
		}
		
		protected void update() {
			table.revalidate();
			table.repaint();
			table.getTableHeader().resizeAndRepaint();
			
			selectAllButton.setEnabled(false);
			selectNoneButton.setEnabled(false);
			
			final SourceColumnSemantic[] types = getTypes(table.getName());
			
			if (types != null) {
				selectAllButton.setEnabled(Arrays.asList(types).contains(NONE));
				
				for (SourceColumnSemantic t : types) {
					if (t != NONE) {
						selectNoneButton.setEnabled(true);
						break;
					}
				}
			}
		}
	}
	
	private class PreviewTableHeaderRenderer extends JPanel implements TableCellRenderer {
		
		private final Border BORDER = BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground"));
		
		private final JLabel typeLabel;
		private final JLabel nameLabel;
		private final JLabel editLabel;
		
		PreviewTableHeaderRenderer() {
			nameLabel = new JLabel();
			nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));
			
			typeLabel = new JLabel();
			typeLabel.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
			
			editLabel = new JLabel(IconManager.ICON_CARET_LEFT);
			editLabel.setFont(iconManager.getIconFont(12.0f));
			editLabel.setHorizontalAlignment(JLabel.CENTER);
			
			// Forces the edit label to always have the same size, no matter its state
			final JLabel tempLabel = new JLabel(IconManager.ICON_CARET_DOWN);
			tempLabel.setFont(iconManager.getIconFont(12.0f));
			LookAndFeelUtil.equalizeSize(editLabel, tempLabel);
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(typeLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(nameLabel)
					.addGap(2, 4, Short.MAX_VALUE)
					.addComponent(editLabel)
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
					.addComponent(typeLabel)
					.addComponent(nameLabel)
					.addComponent(editLabel)
			);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS,
		                                               boolean hasF, int row, int col) {
			nameLabel.setText(val != null ? val.toString() : "");
			
			final SourceColumnSemantic[] types = getTypes(tbl.getName());
			Color fgColor = UIManager.getColor("Label.foreground");
			
			// Set type icon
			if (types != null && types.length > col) {
				SourceColumnSemantic type = types[col];
				
				if (type == null)
					type = NONE;
				
				typeLabel.setForeground(type.getForeground());
				typeLabel.setText(type.getText());
				
				setToolTipText(type.getDescription());
				
				if (type == NONE)
					fgColor = UIManager.getColor("Label.disabledForeground");
			} else {
				fgColor = UIManager.getColor("Label.disabledForeground");
			}
			
			if (editingColumnIndex == col)
				editLabel.setText(IconManager.ICON_CARET_DOWN);
			else
				editLabel.setText(IconManager.ICON_CARET_LEFT);
			
			nameLabel.setForeground(fgColor);
			setBorder(col == 0 ? null : BORDER);
			this.invalidate();

			return this;
		}
	}
	
	public class PreviewTableCellRenderer extends DefaultTableCellRenderer {
		
		public PreviewTableCellRenderer() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		                                               boolean hasFocus, int row, int column) {
			setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
			
			setFont(getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));
			setFont(getFont().deriveFont(getColumnIndex(table.getName(), KEY) == column ? Font.BOLD : Font.PLAIN));
			
			setText(value == null ? "" : value.toString());

			if (isImported(column))
				setForeground(table.getForeground());
			else
				setForeground(UIManager.getColor("Label.disabledForeground"));
			
			final AttributeDataType dataType = getDataType(table.getName(), column);
			
			if (dataType == TYPE_INTEGER || dataType == TYPE_FLOATING)
				setHorizontalAlignment(JLabel.RIGHT);
			else if (dataType == TYPE_BOOLEAN)
				setHorizontalAlignment(JLabel.CENTER);
			else
				setHorizontalAlignment(JLabel.LEFT);
			
			return this;
		}
	}
}
