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
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.ALIAS;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.INTERACTION;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.ONTOLOGY;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.PRIMARY_KEY;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.SOURCE;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.TARGET;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.TAXON;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextFileDelimiters;
import org.cytoscape.tableimport.internal.ui.theme.IconManager;
import org.cytoscape.tableimport.internal.util.AttributeTypes;
import org.cytoscape.tableimport.internal.util.URLUtil;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * General purpose preview table panel.
 */
public class PreviewTablePanel extends JPanel {

	private static final float ICON_FONT_SIZE = 14.0f;

	private static final long serialVersionUID = -7779176856705540150L;

	public static final int ATTRIBUTE_PREVIEW = 1;
	public static final int ONTOLOGY_PREVIEW = 2;
	public static final int NETWORK_PREVIEW = 3;
	
	private static final String DEF_TAB_MESSAGE = "Data File Preview Window";

	// Lines start with this char will be ignored.
	private String commentChar;
	private boolean loadFlag = false;

	// Tracking attribute data type.
	// private Byte[] dataTypes;
	private Map<String, Byte[]> dataTypeMap;
	private Map<String, Byte[]> listDataTypeMap;

	/*
	 * GUI Components
	 */
	private JScrollPane previewScrollPane;
	private JTable previewTable;

	// Tables for each worksheet.
	private Map<String, JTable> previewTables;
	private JTabbedPane tableTabbedPane;
	private JScrollPane keyPreviewScrollPane;
	private JList keyPreviewList;
	private DefaultListModel keyListModel;
	
	private JRadioButton showAllRadioButton;
	private JRadioButton counterRadioButton;
	private JSpinner counterSpinner;
	private JButton reloadButton;
	
	private ButtonGroup importTypeButtonGroup;
	
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private int panelType;
	private String listDelimiter;

	private final IconManager iconManager;

	private static final Logger logger = LoggerFactory.getLogger(PreviewTablePanel.class);

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel(final IconManager iconManager) {
		this(ATTRIBUTE_PREVIEW, iconManager);
	}

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel(int panelType, final IconManager iconManager) {
		this.panelType = panelType;
		this.iconManager = iconManager;

		dataTypeMap = new HashMap<String, Byte[]>();
		listDataTypeMap = new HashMap<String, Byte[]>();

		initComponents();
	}

	public void setKeyAttributeList(Set data) {
		keyListModel.clear();

		for (Object item : data) {
			keyListModel.addElement(item);
		}

		keyPreviewList.repaint();
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
		final JLabel legendLabel = new JLabel("Legend:");
		final JLabel pkLabel = new JLabel("Key");
		final JLabel aliasLabel = new JLabel("Alias");
		final JLabel ontologyLabel = new JLabel("Ontology");
		final JLabel taxonLabel = new JLabel("Taxon");
		final JLabel keyColumnsLabel = new JLabel("Key Columns:");
		
		final JLabel instructionLabel = new JLabel("<html><b>Right-click</b> to edit column.</html>");
		instructionLabel.setFont(instructionLabel.getFont().deriveFont(11.0f));
		
		final JLabel rightArrowLabel = new JLabel(IconManager.ICON_ARROW_RIGHT);
		rightArrowLabel.setFont(iconManager.getIconFont(16.0f));
		
		final JLabel pkIconLabel = new JLabel(PRIMARY_KEY.getText());
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
		
		previewScrollPane = new JScrollPane();
		tableTabbedPane = new JTabbedPane();
		keyListModel = new DefaultListModel();
		keyPreviewList = new JList(keyListModel);
		keyPreviewScrollPane = new JScrollPane();

		previewTables = new HashMap<String, JTable>();
		previewTable = new JTable();
		previewTable.setOpaque(false);

		keyPreviewScrollPane.setViewportView(keyPreviewList);
		previewScrollPane.setViewportView(previewTable);

		tableTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				tableTabbedPaneStateChanged(evt);
			}
		});

		tableTabbedPane.addTab(DEF_TAB_MESSAGE, previewScrollPane);
		
		JTableHeader hd = previewTable.getTableHeader();
		hd.setReorderingAllowed(false);
		hd.setDefaultRenderer(new HeaderRenderer());

		/*
		 * Setting table properties
		 */
		previewTable.setCellSelectionEnabled(false);
		previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		previewTable.setDefaultEditor(Object.class, null);

		this.setBorder(LookAndFeelUtil.createTitledBorder("Preview"));

		importTypeButtonGroup = new ButtonGroup();
		importTypeButtonGroup.add(getShowAllRadioButton());
		importTypeButtonGroup.add(getCounterRadioButton());
		importTypeButtonGroup.setSelected(getCounterRadioButton().getModel(), true);
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		final JSeparator sep = new JSeparator();

		layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
				.addComponent(instructionLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(CENTER, layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING, true)
								.addComponent(tableTabbedPane, DEFAULT_SIZE, 250, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
									.addComponent(getShowAllRadioButton())
									.addComponent(getCounterRadioButton())
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
										.addComponent(pkLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addPreferredGap(UNRELATED)
										.addComponent(aliasIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(aliasLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addPreferredGap(UNRELATED)
										.addComponent(ontologyIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(ontologyLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addPreferredGap(UNRELATED)
										.addComponent(taxonIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(taxonLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
						)
						.addComponent(rightArrowLabel)
						.addGroup(layout.createParallelGroup(LEADING, true)
								.addComponent(keyColumnsLabel)
								.addComponent(keyPreviewScrollPane, PREFERRED_SIZE, 180, PREFERRED_SIZE)
						)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(instructionLabel)
				.addGroup(layout.createParallelGroup(TRAILING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(tableTabbedPane, DEFAULT_SIZE, 200, Short.MAX_VALUE)
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
						)
						.addComponent(rightArrowLabel, LEADING, DEFAULT_SIZE, 200, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(keyColumnsLabel)
								.addComponent(keyPreviewScrollPane, DEFAULT_SIZE, 200, Short.MAX_VALUE)
						)
				)
		);
		
		if (panelType != ONTOLOGY_PREVIEW) {
			sep.setVisible(false);
			keyColumnsLabel.setVisible(false);
			keyPreviewScrollPane.setVisible(false);
			rightArrowLabel.setVisible(false);
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

	public JTable getPreviewTable() {
		JScrollPane selected = (JScrollPane) tableTabbedPane.getSelectedComponent();

		if (selected == null) {
			return null;
		}

		return (JTable) selected.getViewport().getComponent(0);
	}

	public int getTableCount() {
		return tableTabbedPane.getTabCount();
	}

	public String getSheetName(int index) {
		return tableTabbedPane.getTitleAt(index);
	}

	public Byte[] getDataTypes(final String selectedTabName) {
		return dataTypeMap.get(selectedTabName);
	}

	public Byte[] getCurrentDataTypes() {
		return dataTypeMap.get(getSelectedSheetName());
	}

	public Byte[] getCurrentListDataTypes() {
		return listDataTypeMap.get(getSelectedSheetName());
	}

	public FileTypes getFileType() {
		final String sheetName = getSheetName(tableTabbedPane.getSelectedIndex());

		if (sheetName.startsWith("gene_association")) {
			return FileTypes.GENE_ASSOCIATION_FILE;
		}

		return FileTypes.ATTRIBUTE_FILE;
	}

	/**
	 * Get selected tab name.
	 * 
	 * @return name of the selected tab (i.e., sheet name)
	 */
	public String getSelectedSheetName() {
		return tableTabbedPane.getTitleAt(tableTabbedPane.getSelectedIndex());
	}

	public JTable getPreviewTable(int index) {
		JScrollPane selected = (JScrollPane) tableTabbedPane.getComponentAt(index);

		return (JTable) selected.getViewport().getComponent(0);
	}

	private void tableTabbedPaneStateChanged(ChangeEvent evt) {
		if ((tableTabbedPane.getSelectedComponent() != null)
				&& (((JScrollPane) tableTabbedPane.getSelectedComponent()).getViewport().getComponent(0) != null)
				&& (loadFlag == true)) {
			changes.firePropertyChange(ImportTablePanel.SHEET_CHANGED, null, null);
		}
	}

	/**
	 * Get background images for table & list.
	 */
	private BufferedImage getBufferedImage(URL url) {
		BufferedImage image;

		try {
			image = ImageIO.read(url);
		} catch (IOException ioe) {
			logger.warn("could't create image from: " + url.toString(), ioe);

			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}

		return image;
	}

	/**
	 * Load file and show preview.
	 */
	public void setPreviewTable(final Workbook wb, String fileType, String fileFullName, InputStream tempIs,
			List<String> delimiters, TableCellRenderer renderer, int size, final String commentLineChar,
			final int startLine) throws IOException {

		if (tempIs == null)
			return;

		TableCellRenderer curRenderer = renderer;

		if ((commentLineChar != null) && (commentLineChar.trim().length() != 0))
			this.commentChar = commentLineChar;
		else
			this.commentChar = null;
		/*
		 * If rendrer is null, create default one.
		 */
		if (curRenderer == null) {
			curRenderer = new AttributePreviewTableCellRenderer(0, new ArrayList<Integer>(),
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST, null);
		}

		for (int i = 0; i < tableTabbedPane.getTabCount(); i++)
			tableTabbedPane.removeTabAt(i);

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
				logger.debug("# of Sheets = " + wb.getNumberOfSheets());

				Sheet sheet = wb.getSheetAt(0);
				logger.debug("Sheet name = " + wb.getSheetName(0) + ", ROW = " + sheet.rowIterator().hasNext());

				newModel = parseExcel(size, curRenderer, sheet, startLine);

				if (newModel.getRowCount() == 0)
					throw new IllegalStateException("No data found in the Excel sheet.");

				DataTypeUtil.guessTypes(newModel, wb.getSheetName(0), dataTypeMap);
				listDataTypeMap.put(wb.getSheetName(0), initListDataTypes(newModel));
				addTableTab(newModel, wb.getSheetName(0), curRenderer);
			}
		}

		if (!isTable) {
			newModel = parseText(tempIs, size, curRenderer, delimiters, startLine);

			String tabName;
			String[] urlParts = fileFullName.split("/");
			if (urlParts.length > 0 && !fileFullName.isEmpty())
				tabName = urlParts[urlParts.length - 1];
			else
				tabName = "newTable";
			DataTypeUtil.guessTypes(newModel, tabName, dataTypeMap);
			listDataTypeMap.put(tabName, initListDataTypes(newModel));
			addTableTab(newModel, tabName, curRenderer);
		}

		loadFlag = true;
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
				if (bufRd != null) {
					bufRd.close();
				}
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}

		return testResult;
	}

	private void addTableTab(TableModel newModel, final String tabName, TableCellRenderer renderer) {
		JTable newTable = new JTable(newModel);
		previewTables.put(tabName, newTable);

		JScrollPane newScrollPane = new JScrollPane();
		newScrollPane.setViewportView(newTable);

		tableTabbedPane.add(tabName, newScrollPane);

		/*
		 * Initialize data type atrray. By default, everything is a String.
		 */

		// dataTypes = new Byte[newModel.getColumnCount()];
		// dataTypeMap.put(tabName, new Byte[newModel.getColumnCount()]);
		// for (int j = 0; j < newModel.getColumnCount(); j++) {
		// dataTypes[j] = CyAttributes.TYPE_STRING;
		// }

		// Setting table properties
		newTable.setCellSelectionEnabled(false);
		newTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		newTable.setDefaultEditor(Object.class, null);

		if (panelType == NETWORK_PREVIEW) {
			final int colCount = newTable.getColumnCount();
			final boolean[] importFlag = new boolean[colCount];

			for (int i = 0; i < colCount; i++) {
				importFlag[i] = false;
			}

			TableCellRenderer netRenderer = new AttributePreviewTableCellRenderer(
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST, new ArrayList<Integer>(),
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST, importFlag);

			newTable.setDefaultRenderer(Object.class, netRenderer);
		} else {
			newTable.setDefaultRenderer(Object.class, renderer);
		}

		JTableHeader hd = newTable.getTableHeader();
		hd.setReorderingAllowed(false);
		hd.setDefaultRenderer(new HeaderRenderer());

		newTable.getTableHeader().addMouseListener(new TableHeaderListener());

		ColumnResizer.adjustColumnPreferredWidths(newTable);

		newTable.revalidate();
		newTable.repaint();
		newTable.getTableHeader().repaint();
	}

	private Byte[] initListDataTypes(final TableModel model) {
		final Byte[] listTypes = new Byte[model.getColumnCount()];

		for (int i = 0; i < listTypes.length; i++) {
			listTypes[i] = null;
		}

		return listTypes;
	}

	/**
	 * Based on the file type, setup the initial column names.
	 * 
	 * @param colCount
	 * @return
	 */
	private Vector<String> getDefaultColumnNames(final int colCount) {

		final Vector<String> colNames = new Vector<String>();

		for (int i = 0; i < colCount; i++) {
			colNames.add("Column " + (i + 1));
		}

		return colNames;
	}

	private TableModel parseText(InputStream tempIs, int size, TableCellRenderer renderer, List<String> delimiters,
			int startLine) throws IOException {

		String line;
		String attrName = "Attr1";
		Vector data;
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

		if (size == -1) {
			importAll = true;
		}

		// Distinguish between CSV files and everything else.
		// TODO: Since the CSV parser allows for other delimiters, consider
		// exploring using it for everything.

		// The variables are modified by both the new method and the old method.
		int counter = 0;
		maxColumn = 0;
		data = new Vector();
		if (delimiters.contains(TextFileDelimiters.COMMA.toString()) && delimiters.size() == 1) {
			// Only if there is exactly one delimiter and that delimiter is a
			// comma should you read the file
			// using OpenCSV
			// New method... Using OpenCSV
			CSVReader reader = new CSVReader(bufRd);
			String[] rowData; // Note that rowData is roughly equivalent to
								// "parts" in the old code.
			while ((rowData = reader.readNext()) != null) {
				Vector row = new Vector();
				for (String field : rowData)
					row.add(field);
				if (rowData.length > maxColumn)
					maxColumn = rowData.length;
				data.add(row);
				counter++;

				if ((importAll == false) && (counter >= size)) {
					break;
				}
			}
		} else {
			// Old method... Using naive splitting.
			String[] parts;
			while ((line = bufRd.readLine()) != null) {
				if (((commentChar != null) && line.startsWith(commentChar)) || (line.trim().length() == 0)
						|| (counter < startLine)) {
					// ignore
				} else {
					Vector row = new Vector();

					if (delimiterRegEx.length() == 0) {
						parts = new String[1];
						parts[0] = line;
					} else {
						parts = line.split(delimiterRegEx);
					}

					for (String entry : parts) {
						row.add(entry);
					}

					if (parts.length > maxColumn) {
						maxColumn = parts.length;
					}

					data.add(row);
				}

				counter++;

				if ((importAll == false) && (counter >= size)) {
					break;
				}
			}
		}

		// If the inputStream is passed in from parameter, do not close it
		if (tempIs != null)
			tempIs.close();

		if (delimiters == null) {
			// Cytoscape attr file.
			Vector<String> columnNames = new Vector<String>();
			columnNames.add("Key");
			columnNames.add(attrName);
			return new DefaultTableModel(data, columnNames);
		} else
			return new DefaultTableModel(data, getDefaultColumnNames(maxColumn));
	}

	private TableModel parseExcel(int size, TableCellRenderer renderer, final Sheet sheet, int startLine)
			throws IOException {

		if (size == -1)
			size = Integer.MAX_VALUE;

		int maxCol = 0;
		final Vector<Object> data = new Vector<Object>();

		int rowCount = 0;
		Row row;

		while (((row = sheet.getRow(rowCount)) != null) && (rowCount < size)) {
			if (rowCount >= startLine) {
				Vector<Object> rowVector = new Vector<Object>();

				if (maxCol < row.getPhysicalNumberOfCells()) {
					maxCol = row.getPhysicalNumberOfCells();
				}

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

		return new DefaultTableModel(data, this.getDefaultColumnNames(maxCol));
	}

	public int checkKeyMatch(final int targetColumn) {
		final DefaultListModel listModel = (DefaultListModel) keyPreviewList.getModel();
		final Object[] data = listModel.toArray();

		final List<Object> fileKeyList = Arrays.asList(data);

		int matched = 0;

		final TableModel curModel = getPreviewTable().getModel();

		try {
			curModel.getValueAt(0, targetColumn);
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}

		final int rowCount = curModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			final Object val = curModel.getValueAt(i, targetColumn);
			if (val != null && fileKeyList.contains(val))
				matched++;
		}
		return matched;
	}

	public void setAliasColumn(int column, boolean flag) {
		AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) getPreviewTable().getCellRenderer(
				0, column);
		rend.setAliasFlag(column, flag);
		// rend.setImportFlag(column, !rend.getImportFlag(column));
		getPreviewTable().getTableHeader().resizeAndRepaint();
		getPreviewTable().repaint();
	}

	protected JRadioButton getShowAllRadioButton() {
		if (showAllRadioButton == null) {
			showAllRadioButton = new JRadioButton("Show all entries in the file");
		}
		
		return showAllRadioButton;
	}
	
	protected JRadioButton getCounterRadioButton() {
		if (counterRadioButton == null) {
			counterRadioButton = new JRadioButton("Show first");
		}
		
		return counterRadioButton;
	}
	
	protected JSpinner getCounterSpinner() {
		if (counterSpinner == null) {
			final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 1, 10000000, 10);
			counterSpinner = new JSpinner(spinnerModel);
			counterSpinner.setToolTipText(
					"<html><body>Click <strong><i>Refresh Preview</i></strong> button to update the table</body></html>");
			
			counterSpinner.addMouseWheelListener(new MouseWheelListener() {
				@Override
				public void mouseWheelMoved(final MouseWheelEvent evt) {
					final JSpinner source = (JSpinner) evt.getSource();

					final SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
					final Integer oldValue = (Integer) source.getValue();
					final int intValue = oldValue.intValue() - (evt.getWheelRotation() * model.getStepSize().intValue());
					final Integer newValue = new Integer(intValue);

					if (model.getMaximum().compareTo(newValue) >= 0
							&& model.getMinimum().compareTo(newValue) <= 0)
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
		}
		
		return reloadButton;
	}
	
	private final class TableHeaderListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			final JTable targetTable = getPreviewTable();
			final String selectedTabName = getSelectedSheetName();
			final Byte[] dataTypes = dataTypeMap.get(selectedTabName);
			final Byte[] listDataTypes = listDataTypeMap.get(selectedTabName);

			final int column = targetTable.getColumnModel().getColumnIndexAtX(e.getX());

			if (SwingUtilities.isRightMouseButton(e)) {
				// determine the parent of AttributeTypeDialog, should be a
				// TunableDialog
				Object parent = null;

				Container cnt = PreviewTablePanel.this.getParent();
				while (true) {
					cnt = cnt.getParent();
					if (cnt instanceof JDialog) {
						parent = cnt;
						break;
					}
				}

				/*
				 * Right click: This action pops up an dialog to edit the
				 * attribute type and name.
				 */
				AttributeTypeDialog atd = new AttributeTypeDialog((java.awt.Dialog) parent, true, targetTable
						.getColumnModel().getColumn(column).getHeaderValue().toString(), dataTypes[column], column,
						listDelimiter);

				atd.setLocationRelativeTo(targetTable.getParent());
				atd.setVisible(true);

				final String name = atd.getName();
				final byte newType = atd.getAttributeType();
				final byte newListType = atd.getListDataType();

				if (name != null) {
					targetTable.getColumnModel().getColumn(column).setHeaderValue(name);
					targetTable.getTableHeader().resizeAndRepaint();

					if (newType == AttributeTypes.TYPE_SIMPLE_LIST) {
						// listDelimiter = atd.getListDelimiterType();
						listDelimiter = atd.getListDelimiterType();

						changes.firePropertyChange(ImportTablePanel.LIST_DELIMITER_CHANGED, null,
								atd.getListDelimiterType());

						listDataTypes[column] = newListType;
						changes.firePropertyChange(ImportTablePanel.LIST_DATA_TYPE_CHANGED, null, listDataTypes);
						listDataTypeMap.put(selectedTabName, listDataTypes);
					}

					final Vector keyValPair = new Vector();
					keyValPair.add(column);
					keyValPair.add(newType);
					changes.firePropertyChange(ImportTablePanel.ATTR_DATA_TYPE_CHANGED, null, keyValPair);

					final Vector colNamePair = new Vector();
					colNamePair.add(column);
					colNamePair.add(name);
					changes.firePropertyChange(ImportTablePanel.ATTRIBUTE_NAME_CHANGED, null, colNamePair);

					dataTypes[column] = newType;

					targetTable.getTableHeader().setDefaultRenderer(new HeaderRenderer());
					dataTypeMap.put(selectedTabName, dataTypes);
				}
			} else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
				final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) targetTable
						.getCellRenderer(0, column);
				
				if (column == rend.getSourceIndex() || column == rend.getTargetIndex()
						|| column == rend.getInteractionIndex() || column == rend.getPrimaryKeyIndex()
						|| column == rend.getTaxonIndex() || column == rend.getOntologyIndex()
						|| rend.isAlias(column))
					return;
				
				rend.setImportFlag(column, !rend.isImportFlag(column));
				targetTable.getTableHeader().resizeAndRepaint();
				targetTable.repaint();
			}
		}
	}
	
	class HeaderRenderer extends JPanel implements TableCellRenderer {
		
		private static final long serialVersionUID = 3380290649336997187L;
		
		private final Border BORDER = BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Label.disabledForeground"));
		
		private final JCheckBox checkBox;
		private final JLabel iconLabel;
		private final Color defForeground;

		HeaderRenderer() {
			checkBox = new JCheckBox();
			
			iconLabel = new JLabel();
			iconLabel.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
			
			defForeground = checkBox.getForeground();
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(iconLabel)
					.addComponent(checkBox)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(iconLabel)
					.addComponent(checkBox)
			);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS,
		                                               boolean hasF, int row, int col) {
			final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) tbl.getCellRenderer(0, col);
			
			final int source = rend.getSourceIndex();
			final int interaction = rend.getInteractionIndex();
			final int target = rend.getTargetIndex();
			
			final boolean importFlag = rend.isImportFlag(col) || source == col || target == col || interaction == col;
			checkBox.setSelected(importFlag);
			checkBox.setText(val != null ? val.toString() : "");
			
			// Set icon
			if (col == rend.getPrimaryKeyIndex()) {
				iconLabel.setForeground(defForeground);
				iconLabel.setText(PRIMARY_KEY.getText());
			} else if (col == source) {
				iconLabel.setForeground(SOURCE.getForeground());
				iconLabel.setText(SOURCE.getText());
			} else if (col == target) {
				iconLabel.setForeground(TARGET.getForeground());
				iconLabel.setText(TARGET.getText());
			} else if (col == interaction) {
				iconLabel.setForeground(INTERACTION.getForeground());
				iconLabel.setText(INTERACTION.getText());
			} else if (col == rend.getOntologyIndex()) {
				iconLabel.setForeground(ONTOLOGY.getForeground());
				iconLabel.setText(ONTOLOGY.getText());
			} else if (rend.isAlias(col)) {
				iconLabel.setForeground(ALIAS.getForeground());
				iconLabel.setText(ALIAS.getText());
			} else if (col == rend.getTaxonIndex()) {
				iconLabel.setForeground(TAXON.getForeground());
				iconLabel.setText(TAXON.getText());
			} else {
				iconLabel.setForeground(defForeground);
				iconLabel.setText("");
			}
			
			setBorder(col == 0 ? null : BORDER);
			this.invalidate();

			return this;
		}

		/*
		private static ImageIcon getDataTypeIcon(byte dataType) {
			ImageIcon dataTypeIcon = null;

			if (dataType == String.class) { //CyAttributes.TYPE_STRING) {
				dataTypeIcon = STRING_ICON.getIcon();
			} else if (dataType == CyAttributes.TYPE_INTEGER) {
				dataTypeIcon = INTEGER_ICON.getIcon();
			} else if (dataType == CyAttributes.TYPE_FLOATING) {
				dataTypeIcon = FLOAT_ICON.getIcon();
			} else if (dataType == CyAttributes.TYPE_BOOLEAN) {
				dataTypeIcon = BOOLEAN_ICON.getIcon();
			} else if (dataType == CyAttributes.TYPE_SIMPLE_LIST) {
				dataTypeIcon = LIST_ICON.getIcon();
			}

			return dataTypeIcon;
		}
		*/
	}
}
