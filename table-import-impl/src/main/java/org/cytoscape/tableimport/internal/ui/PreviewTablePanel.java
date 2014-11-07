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

import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogColorTheme.ALIAS_COLOR;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogColorTheme.ONTOLOGY_COLOR;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogColorTheme.PRIMARY_KEY_COLOR;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogColorTheme.SPECIES_COLOR;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogFontTheme.LABEL_FONT;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.RIGHT_ARROW_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.SPREADSHEET_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.TEXT_FILE_ICON;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
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
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextFileDelimiters;
import org.cytoscape.tableimport.internal.util.AttributeTypes;
import org.cytoscape.tableimport.internal.util.URLUtil;
import org.cytoscape.util.swing.ColumnResizer;
import org.jdesktop.layout.GroupLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * General purpose preview table panel.
 */
public class PreviewTablePanel extends JPanel {

	private static final long serialVersionUID = -7779176856705540150L;

	/**
	 *
	 */
	public static final int ATTRIBUTE_PREVIEW = 1;

	/**
	 *
	 */
	public static final int ONTOLOGY_PREVIEW = 2;

	/**
	 *
	 */
	public static final int NETWORK_PREVIEW = 3;

	/*
	 * Default messages
	 */
	private static final String DEF_MESSAGE = "Legend:";
	private static final String DEF_TAB_MESSAGE = "Data File Preview Window";

	// Lines start with this char will be ignored.
	private String commentChar;
	private final String message;
	private boolean loadFlag = false;

	// Tracking attribute data type.
	// private Byte[] dataTypes;
	private Map<String, Byte[]> dataTypeMap;
	private Map<String, Byte[]> listDataTypeMap;

	/*
	 * GUI Components
	 */
	private JLabel legendLabel;
	private JLabel aliasLabel;
	private JLabel primaryKeyLabel;
	private JLabel ontologyTermLabel;
	private JLabel taxonomyLabel;
	private JLabel instructionLabel;
	private JLabel rightArrowLabel;
	private JLabel fileTypeLabel;
	private JScrollPane previewScrollPane;
	private JTable previewTable;

	// Tables for each worksheet.
	private Map<String, JTable> previewTables;
	private JTabbedPane tableTabbedPane;
	private JScrollPane keyPreviewScrollPane;
	private JList keyPreviewList;
	private DefaultListModel keyListModel;
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private int panelType;
	private String listDelimiter;
	private InputStream is = null;

	private static final Logger logger = LoggerFactory.getLogger(PreviewTablePanel.class);

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel() {
		this(DEF_MESSAGE, ATTRIBUTE_PREVIEW);
	}


	/**
	 * Creates a new PreviewTablePanel object.
	 * 
	 * @param message
	 *            DOCUMENT ME!
	 * @param panelType
	 *            DOCUMENT ME!
	 */
	public PreviewTablePanel(String message, int panelType) {
		if (message == null) {
			this.message = DEF_MESSAGE;
		} else {
			this.message = message;
		}

		this.panelType = panelType;

		dataTypeMap = new HashMap<String, Byte[]>();
		listDataTypeMap = new HashMap<String, Byte[]>();

		initComponents();
		hideUnnecessaryComponents();
	}


	private void hideUnnecessaryComponents() {
		fileTypeLabel.setVisible(false);

		if (panelType == NETWORK_PREVIEW || panelType == ATTRIBUTE_PREVIEW) {
			keyPreviewScrollPane.setVisible(false);
			rightArrowLabel.setVisible(false);
			legendLabel.setVisible(false);
			primaryKeyLabel.setVisible(false);
			aliasLabel.setVisible(false);
			ontologyTermLabel.setVisible(false);
			taxonomyLabel.setVisible(false);
		}
		repaint();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param data
	 *            DOCUMENT ME!
	 */
	public void setKeyAttributeList(Set data) {
		keyPreviewScrollPane.setBackground(Color.white);
		keyListModel.clear();

		for (Object item : data) {
			keyListModel.addElement(item);
		}

		keyPreviewList.repaint();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param l
	 *            DOCUMENT ME!
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (changes == null)
			return;

		changes.addPropertyChangeListener(l);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param l
	 *            DOCUMENT ME!
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	private void initComponents() {
		legendLabel = new JLabel();
		instructionLabel = new javax.swing.JLabel();

		primaryKeyLabel = new javax.swing.JLabel();
		aliasLabel = new javax.swing.JLabel();
		ontologyTermLabel = new JLabel();
		taxonomyLabel = new JLabel();
		previewScrollPane = new JScrollPane();
		rightArrowLabel = new JLabel();
		tableTabbedPane = new JTabbedPane();
		keyListModel = new DefaultListModel();
		keyPreviewList = new JList(keyListModel);
		keyPreviewScrollPane = new JScrollPane();

		previewTables = new HashMap<String, JTable>();
		previewTable = new JTable();
		previewTable.setName("previewTable");
		previewTable.setOpaque(false);
		previewTable.setBackground(Color.white);

		fileTypeLabel = new JLabel();
		fileTypeLabel.setFont(new Font("Sans-Serif", Font.BOLD, 14));

		keyPreviewScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Key Columns"));

		keyPreviewList.setOpaque(false);
		keyPreviewList.setCellRenderer(new KeyAttributeListRenderer());
		keyPreviewScrollPane.setViewportView(keyPreviewList);

		previewScrollPane.setOpaque(false);
		previewScrollPane.setViewportView(previewTable);
		previewScrollPane.setBackground(Color.WHITE);

		final BufferedImage datasourceImage = getBufferedImage(getClass().getResource(
				"/images/ximian/data_sources_trans.png"));

		final BufferedImage bi = getBufferedImage(getClass().getResource("/images/icon100_trans.png"));

		tableTabbedPane.setBackground(Color.white);
		tableTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				tableTabbedPaneStateChanged(evt);
			}
		});

		previewScrollPane.getViewport().setOpaque(false);
		previewScrollPane.setViewportBorder(new CentredBackgroundBorder(datasourceImage));
		keyPreviewScrollPane.getViewport().setOpaque(false);
		keyPreviewScrollPane.setViewportBorder(new CentredBackgroundBorder(bi));

		tableTabbedPane.addTab(DEF_TAB_MESSAGE, previewScrollPane);

		rightArrowLabel.setIcon(RIGHT_ARROW_ICON.getIcon());

		JTableHeader hd = previewTable.getTableHeader();
		hd.setReorderingAllowed(false);
		hd.setDefaultRenderer(new HeaderRenderer(hd.getDefaultRenderer(), null));

		/*
		 * Setting table properties
		 */
		previewTable.setCellSelectionEnabled(false);
		previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		previewTable.setDefaultEditor(Object.class, null);

		this.setBorder(BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Preview",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11)));

		instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		instructionLabel.setText("Left Click: Enable/Disable Column, Right Click: Edit Column");
		instructionLabel.setFont(LABEL_FONT.getFont());
		instructionLabel.setForeground(Color.red);

		legendLabel.setFont(LABEL_FONT.getFont());
		legendLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		legendLabel.setText(message);

		primaryKeyLabel.setFont(LABEL_FONT.getFont());
		primaryKeyLabel.setForeground(Color.WHITE);
		primaryKeyLabel.setBackground(PRIMARY_KEY_COLOR.getColor());
		primaryKeyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		primaryKeyLabel.setText("Key");
		primaryKeyLabel.setToolTipText("Column in this color is the Primary Key.");
		primaryKeyLabel.setOpaque(true);

		aliasLabel.setFont(LABEL_FONT.getFont());
		aliasLabel.setForeground(Color.WHITE);
		aliasLabel.setBackground(ALIAS_COLOR.getColor());
		aliasLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		aliasLabel.setText("Alias");
		aliasLabel.setToolTipText("Columns in this color are Aliases.");
		aliasLabel.setOpaque(true);

		ontologyTermLabel.setFont(LABEL_FONT.getFont());
		ontologyTermLabel.setForeground(Color.WHITE);
		ontologyTermLabel.setBackground(ONTOLOGY_COLOR.getColor());
		ontologyTermLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		ontologyTermLabel.setText("Ontology");
		ontologyTermLabel.setToolTipText("Column in this color is Ontology Term.");
		ontologyTermLabel.setOpaque(true);

		taxonomyLabel.setFont(LABEL_FONT.getFont());
		taxonomyLabel.setForeground(Color.WHITE);
		taxonomyLabel.setBackground(SPECIES_COLOR.getColor());
		taxonomyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		taxonomyLabel.setText("Taxon");
		taxonomyLabel.setToolTipText("Columns in this color is Taxon (for Gene Association files only).");
		taxonomyLabel.setOpaque(true);

		GroupLayout previewPanelLayout = new GroupLayout(this);
		this.setLayout(previewPanelLayout);

		previewPanelLayout.setHorizontalGroup(previewPanelLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING,
						previewPanelLayout
								.createSequentialGroup()
								.add(tableTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250,
										Short.MAX_VALUE)
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
								.add(rightArrowLabel)
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
								.add(keyPreviewScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
				.add(previewPanelLayout
						.createSequentialGroup()
						.add(fileTypeLabel)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(instructionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(legendLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(primaryKeyLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(aliasLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(ontologyTermLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(taxonomyLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));
		previewPanelLayout.setVerticalGroup(previewPanelLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				previewPanelLayout
						.createSequentialGroup()
						.add(previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
								.add(fileTypeLabel).add(primaryKeyLabel).add(aliasLabel).add(ontologyTermLabel)
								.add(taxonomyLabel).add(legendLabel).add(instructionLabel))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(previewPanelLayout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
								.add(tableTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200,
										Short.MAX_VALUE)
								.add(keyPreviewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200,
										Short.MAX_VALUE)
								.add(org.jdesktop.layout.GroupLayout.LEADING, rightArrowLabel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))));
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

	/**
	 * DOCUMENT ME!
	 * 
	 * @param index
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getSheetName(int index) {
		return tableTabbedPane.getTitleAt(index);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param selectedTabName
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Byte[] getDataTypes(final String selectedTabName) {
		return dataTypeMap.get(selectedTabName);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Byte[] getCurrentDataTypes() {
		return dataTypeMap.get(getSelectedSheetName());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Byte[] getCurrentListDataTypes() {
		return listDataTypeMap.get(getSelectedSheetName());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
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

	/**
	 * DOCUMENT ME!
	 * 
	 * @param index
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public JTable getPreviewTable(int index) {
		JScrollPane selected = (JScrollPane) tableTabbedPane.getComponentAt(index);

		return (JTable) selected.getViewport().getComponent(0);
	}

	private void tableTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {
		if ((tableTabbedPane.getSelectedComponent() != null)
				&& (((JScrollPane) tableTabbedPane.getSelectedComponent()).getViewport().getComponent(0) != null)
				&& (loadFlag == true)) {
			changes.firePropertyChange(ImportTablePanel.SHEET_CHANGED, null, null);
		}
	}

	/**
	 * Get backgroung images for table & list.
	 * 
	 * @param url
	 * @return
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
	 * 
	 * @param wb
	 * @param fileType
	 * @param fileFullName
	 * @param tempIs
	 * @param delimiters
	 * @param renderer
	 *            renderer for this table. Can be null.
	 * @param size
	 * @param commentLineChar
	 *            TODO
	 * @param startLine
	 *            TODO
	 * @throws IOException
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
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST, null, TextFileDelimiters.PIPE.toString());
		}

		for (int i = 0; i < tableTabbedPane.getTabCount(); i++)
			tableTabbedPane.removeTabAt(i);

		previewTables = new HashMap<String, JTable>();

		TableModel newModel;

		fileTypeLabel.setVisible(true);

		boolean isTable = false;
		if (fileType != null) {
			if (fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension())
					|| fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {

				isTable = true;
				fileTypeLabel.setIcon(SPREADSHEET_ICON.getIcon());
				fileTypeLabel.setText("Excel" + '\u2122' + " Workbook");

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
			fileTypeLabel.setText("Text File");
			fileTypeLabel.setIcon(TEXT_FILE_ICON.getIcon());
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

		if (getFileType() == FileTypes.GENE_ASSOCIATION_FILE) {
			fileTypeLabel.setText("Gene Association");
			fileTypeLabel.setToolTipText("This is a fixed-format Gene Association file.");
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
		newScrollPane.setBackground(Color.WHITE);

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
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST, importFlag,
					TextFileDelimiters.PIPE.toString());

			newTable.setDefaultRenderer(Object.class, netRenderer);
		} else {
			newTable.setDefaultRenderer(Object.class, renderer);
		}

		JTableHeader hd = newTable.getTableHeader();
		hd.setReorderingAllowed(false);
		hd.setDefaultRenderer(new HeaderRenderer(hd.getDefaultRenderer(), dataTypeMap.get(tabName)));

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
	 * <p>
	 * </p>
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

	/**
	 * Not yet implemented.
	 * <p>
	 * </p>
	 * 
	 * @param targetColumn
	 * @return
	 */
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

	/**
	 * DOCUMENT ME!
	 * 
	 * @param column
	 *            DOCUMENT ME!
	 * @param flag
	 *            DOCUMENT ME!
	 */
	public void setAliasColumn(int column, boolean flag) {
		AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) getPreviewTable().getCellRenderer(
				0, column);
		rend.setAliasFlag(column, flag);
		// rend.setImportFlag(column, !rend.getImportFlag(column));
		getPreviewTable().getTableHeader().resizeAndRepaint();
		getPreviewTable().repaint();
	}

	private final class TableHeaderListener implements MouseListener {
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

					targetTable.getTableHeader().setDefaultRenderer(
							new HeaderRenderer(targetTable.getTableHeader().getDefaultRenderer(), dataTypes));
					dataTypeMap.put(selectedTabName, dataTypes);
				}
			} else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1)) {
				final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) targetTable
						.getCellRenderer(0, column);
				rend.setImportFlag(column, !rend.getImportFlag(column));
				targetTable.getTableHeader().resizeAndRepaint();
				targetTable.repaint();
			}
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
		}
	}
}

class KeyAttributeListRenderer extends JLabel implements ListCellRenderer {
	private static final Font KEY_LIST_FONT = new Font("Sans-Serif", Font.BOLD, 16);
	private static final Color FONT_COLOR = Color.BLACK;

	/**
	 * Creates a new KeyAttributeListRenderer object.
	 */
	public KeyAttributeListRenderer() {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param list
	 *            DOCUMENT ME!
	 * @param value
	 *            DOCUMENT ME!
	 * @param index
	 *            DOCUMENT ME!
	 * @param isSelected
	 *            DOCUMENT ME!
	 * @param cellHasFocus
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		setFont(KEY_LIST_FONT);
		setForeground(FONT_COLOR);
		setText(value.toString());

		this.setOpaque(false);

		return this;
	}
}

class CentredBackgroundBorder implements Border {
	private final BufferedImage image;

	/**
	 * Creates a new CentredBackgroundBorder object.
	 * 
	 * @param image
	 *            DOCUMENT ME!
	 */
	public CentredBackgroundBorder(BufferedImage image) {
		this.image = image;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param c
	 *            DOCUMENT ME!
	 * @param g
	 *            DOCUMENT ME!
	 * @param x
	 *            DOCUMENT ME!
	 * @param y
	 *            DOCUMENT ME!
	 * @param width
	 *            DOCUMENT ME!
	 * @param height
	 *            DOCUMENT ME!
	 */
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		x += ((width - image.getWidth()) / 2);
		y += ((height - image.getHeight()) / 2);
		((Graphics2D) g).drawRenderedImage(image, AffineTransform.getTranslateInstance(x, y));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param c
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Insets getBorderInsets(Component c) {
		return new Insets(0, 0, 0, 0);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isBorderOpaque() {
		return true;
	}
}
