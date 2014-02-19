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


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.*;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextFileDelimiters;
import org.cytoscape.tableimport.internal.util.AttributeTypes;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.tableimport.internal.util.URLUtil;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.JStatusBar;
import org.cytoscape.work.TaskManager;
import org.jdesktop.layout.GroupLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.*;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.*;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogFontTheme.TITLE_FONT;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.*;


/**
 * MainUI for Table Import.
 *
 * TODO: Refactor to make this more generic component.
 *
 */
public class ImportTablePanel extends JPanel implements PropertyChangeListener, TableModelListener {
	private static final long serialVersionUID = 7356378931577386260L;

	private static final Logger logger = LoggerFactory.getLogger(ImportTablePanel.class);

	/**
	 * This dialog GUI will be switched based on the following parameters:
	 *
	 * SIMPLE_ATTRIBUTE_IMPORT: Import attributes in text table.
	 * ONTOLOGY_AND_ANNOTATION_IMPORT: Load ontology and map attributes in text
	 * table.
	 *
	 * NETWORK_IMPORT: Import text table as a network.
	 */
	public static final int SIMPLE_ATTRIBUTE_IMPORT = 1;
	public static final int ONTOLOGY_AND_ANNOTATION_IMPORT = 2;
	public static final int NETWORK_IMPORT = 3;

	/*
	 * Default value for Interaction edge attribute.
	 */
	private static final String DEFAULT_INTERACTION = "pp";

	/*
	 * Signals used among Swing components in this dialog:
	 */
	public static final String LIST_DELIMITER_CHANGED = "listDelimiterChanged";
	public static final String LIST_DATA_TYPE_CHANGED = "listDataTypeChanged";
	public static final String ATTR_DATA_TYPE_CHANGED = "attrDataTypeChanged";
	public static final String ATTRIBUTE_NAME_CHANGED = "aliasTableChanged";
	public static final String SHEET_CHANGED = "sheetChanged";
	public static final String NETWORK_IMPORT_TEMPLATE_CHANGED = "networkImportTemplateChanged";

	private static final String[] keyTable = { "Alias?", "Column Name", "Data Type" };
	private static final String ID = CyNetwork.NAME; 

	// Key column index
	protected int keyInFile;

	// Case sensitivity
	private Boolean caseSensitive = true;

	// Data Type
	private org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType objType;
	private final int dialogType;

	protected Map<String, String> annotationUrlMap;
	protected Map<String, String> annotationFormatMap;
	protected Map<String, Map<String, String>> annotationAttributesMap;
	protected Map<String, String> ontologyUrlMap;
	protected Map<String, String> ontologyTypeMap;
	protected Map<String, String> ontologyDescriptionMap;
	private List<Byte> attributeDataTypes;

	/*
	 * This is for storing data type in the list object.
	 */
	private Byte[] listDataTypes;

	/*
	 * Tracking multiple sheets.
	 */
	private Map<String, AliasTableModel> aliasTableModelMap;
	private Map<String, JTable> aliasTableMap;
	private Map<String, Integer> primaryKeyMap;
	private String[] columnHeaders;
	protected String listDelimiter;
	boolean[] importFlag;
	private CyTable selectedAttributes;
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private File[] inputFiles;

	private CyNetwork network;

	private InputStream is;
	private final String fileType;
	private String inputName = null;

	private Workbook workbook = null;

	private OntologyPanelBuilder panelBuilder;

	private CyProperty<Bookmarks> bookmarksProp;
	private BookmarksUtil bkUtil;
	private final InputStreamTaskFactory factory;
	private final TaskManager taskManager;
	private final CyNetworkManager manager;
	private final CyTableFactory tableFactory;
	private final CyTableManager tableManager;
	private File tempFile;
	private final FileUtil fileUtil;

	public ImportTablePanel(final int dialogType, final InputStream is, final String fileType,
	                        final String inputName, final CyProperty<Bookmarks> bookmarksProp,
	                        final BookmarksUtil bkUtil, final TaskManager taskManager,
	                        final InputStreamTaskFactory factory, final CyNetworkManager manager,
	                        final CyTableFactory tableFactory, final CyTableManager tableManager, final FileUtil fileUtil)
	    throws JAXBException, IOException
	{
		this(dialogType, is, fileType, bookmarksProp, bkUtil, taskManager, factory, manager,
		     tableFactory, tableManager, fileUtil);
		this.inputName = inputName;
	}

	public ImportTablePanel(final int dialogType, final InputStream is, final String fileType,
				final CyProperty<Bookmarks> bookmarksProp,
				final BookmarksUtil bkUtil, final TaskManager taskManager,
				final InputStreamTaskFactory factory, final CyNetworkManager manager,
				final CyTableFactory tableFactory, final CyTableManager tableManager, final FileUtil fileUtil)
	    throws JAXBException, IOException
	{
		this.bookmarksProp = null;
		this.bkUtil        = null;
		this.taskManager   = taskManager;
		this.factory       = factory;
		this.manager       = manager;
		this.tableFactory  = tableFactory;
		this.tableManager  = tableManager;
        this.fileUtil = fileUtil;
		this.fileType = fileType;

		if (dialogType != ONTOLOGY_AND_ANNOTATION_IMPORT) {

			//Before, this.fileType was always null.
			tempFile = File.createTempFile("temp", this.fileType);
			tempFile.deleteOnExit();
			FileOutputStream os = new FileOutputStream(tempFile);
			int read = 0;
			byte[] bytes = new byte[1024];
		 
			while ((read = is.read(bytes)) != -1) {
				os.write(bytes, 0, read);
			}
			os.flush();
			os.close();
			
			this.is = new FileInputStream(tempFile);
		}else if (is == null)
			this.is =  null;
		
		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			if (bookmarksProp == null)
				throw new NullPointerException("Bookmark Property is null.");
			if (bkUtil == null)
				throw new NullPointerException("Bookmark Utility is null.");

			this.bookmarksProp = bookmarksProp;
			this.bkUtil = bkUtil;

		}
		

		selectedAttributes = null;

		network = CytoscapeServices.cyApplicationManager.getCurrentNetwork();
		if (network != null){
			selectedAttributes = network.getDefaultNodeTable();
		}
		this.objType = NODE;
		this.dialogType = dialogType;
		this.listDelimiter = PIPE.toString();

		this.aliasTableModelMap = new HashMap<String, AliasTableModel>();
		this.aliasTableMap = new HashMap<String, JTable>();
		this.primaryKeyMap = new HashMap<String, Integer>();

		annotationUrlMap = new HashMap<String, String>();
		annotationFormatMap = new HashMap<String, String>();
		annotationAttributesMap = new HashMap<String, Map<String, String>>();

		ontologyUrlMap = new HashMap<String, String>();
		ontologyDescriptionMap = new HashMap<String, String>();
		ontologyTypeMap = new HashMap<String, String>();

		attributeDataTypes = new ArrayList<Byte>();

		initComponents();

		// Hide two unwanted button
		this.importButton.setVisible(false);
		this.cancelButton.setVisible(false);

		updateComponents();

		previewPanel.addPropertyChangeListener(this);

		//Don't know why this panel is disabled at start up
		this.attributeNamePanel.setEnabled(true);

		// Hide input file and use inputStream
		this.attributeFileLabel.setVisible(false);
		this.selectAttributeFileButton.setVisible(false);
		this.targetDataSourceTextField.setVisible(false);

		//Case import network
		if (this.dialogType == NETWORK_IMPORT) {
			this.edgeRadioButton.setVisible(false);
			this.nodeRadioButton.setVisible(false);
		}

		// Case import node/edge attribute
		if (this.dialogType == SIMPLE_ATTRIBUTE_IMPORT)
			this.networkRadioButton.setVisible(false);

		this.helpButton.setVisible(false);

		boolean useFirstRow = dialogType == NETWORK_IMPORT || dialogType == SIMPLE_ATTRIBUTE_IMPORT;
		setPreviewPanel(null, useFirstRow);
		
		// Hide the alias Panel, we will do the table join somewhere else, not in this GUI
		aliasScrollPane.setVisible(false);

	}


	public void addPropertyChangeListener(PropertyChangeListener l) {
		if(changes == null) return;
		changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	/**
	 * Listening to local signals used among Swing components in this dialog.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(LIST_DELIMITER_CHANGED)) {
			/*
			 * List delimiter has been changed by preview table GUI.
			 */
			listDelimiter = evt.getNewValue().toString();
		} else if (evt.getPropertyName().equals(LIST_DATA_TYPE_CHANGED)) {
			listDataTypes = (Byte[]) evt.getNewValue();
		} else if (evt.getPropertyName().equals(ATTR_DATA_TYPE_CHANGED)) {
			/*
			 * Data type of an attribute has been chabged.
			 */
			final Vector vec = (Vector) evt.getNewValue();
			final Integer key = (Integer) vec.get(0);
			final Byte newType = (Byte) vec.get(1);

			if (key > attributeDataTypes.size()) {
				attributeDataTypes = new ArrayList<Byte>();

				for (Byte type : previewPanel.getCurrentDataTypes()) {
					attributeDataTypes.add(type);
				}
			}

			attributeDataTypes.set(key, newType);

			if (dialogType != NETWORK_IMPORT) {
				final JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());
				curTable.setDefaultRenderer(Object.class,
				                            new AliasTableRenderer(attributeDataTypes,
				                                                   primaryKeyComboBox.getSelectedIndex()));
				curTable.repaint();
			}
		} else if (evt.getPropertyName().equals(ATTRIBUTE_NAME_CHANGED)) {
			/*
			 * Update Alias Table
			 */
			if (dialogType != NETWORK_IMPORT) {
				final Vector vec = (Vector) evt.getNewValue();
				final String name = (String) vec.get(1);
				final Integer column = (Integer) vec.get(0);

				// Update cell in the attribute table
				updateAliasTableCell(name, column);

				// Update Primary Key combo box
				updatePrimaryKeyComboBox();
			}
		} else if (evt.getPropertyName().equals(SHEET_CHANGED)) {
			/*
			 * Only when the file is in Excel format.
			 */
			final int columnCount = previewPanel.getPreviewTable().getColumnCount();
			aliasTableModelMap.put(previewPanel.getSelectedSheetName(),
			                       new AliasTableModel(keyTable, columnCount));

			initializeAliasTable(columnCount, null);
			updatePrimaryKeyComboBox();
		} else if (evt.getPropertyName().equals(NETWORK_IMPORT_TEMPLATE_CHANGED)) {
			/*
			 * This is a signal from network import options panel.
			 */
			List<Integer> columnIdx = (List<Integer>) evt.getNewValue();

			final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) previewPanel.getPreviewTable()
			                                                                                               .getCellRenderer(0,
			                                                                                                                0);
			rend.setSourceIndex(columnIdx.get(0));
			rend.setTargetIndex(columnIdx.get(1));
			rend.setInteractionIndex(columnIdx.get(2));

			previewPanel.getPreviewTable().getTableHeader().resizeAndRepaint();
			previewPanel.getPreviewTable().repaint();

			previewPanel.repaint();
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc=" Generated Code">
	private void initComponents() {
		statusBar = new JStatusBar();

		importAllCheckBox = new JCheckBox("Import everything (Key is always ID)");
		importAllCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					importAllCheckBoxActionPerformed(evt);
				}
			});

		caseSensitiveCheckBox = new JCheckBox("Case Sensitive");
		caseSensitiveCheckBox.setToolTipText("<html><strong><font color=\"red\">Caution. If you uncheck this, import can be extremely slow.</font></strong></html>");
		caseSensitiveCheckBox.setSelected(true);
		caseSensitiveCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					ignoreCaseCheckBoxActionPerformed(evt);
				}

				private void ignoreCaseCheckBoxActionPerformed(ActionEvent evt) {
					caseSensitive = caseSensitiveCheckBox.isSelected();
				}
			});

		importTypeButtonGroup = new ButtonGroup();

		attrTypePanel = new JPanel();

		counterSpinner = new javax.swing.JSpinner();
		counterLabel = new javax.swing.JLabel();
		reloadButton = new javax.swing.JButton();
		showAllRadioButton = new javax.swing.JRadioButton();
		counterRadioButton = new javax.swing.JRadioButton();

		attrTypeButtonGroup = new javax.swing.ButtonGroup();
		titleIconLabel1 = new javax.swing.JLabel();
		titleIconLabel2 = new javax.swing.JLabel();
		titleIconLabel3 = new javax.swing.JLabel();
		titleLabel = new javax.swing.JLabel();
		titleSeparator = new javax.swing.JSeparator();
		importButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		helpButton = new javax.swing.JButton();
		basicPanel = new javax.swing.JPanel();
		attribuiteLabel = new javax.swing.JLabel();
		nodeRadioButton = new javax.swing.JRadioButton();
		edgeRadioButton = new javax.swing.JRadioButton();
		networkRadioButton = new javax.swing.JRadioButton();
		annotationAndOntologyImportPanel = new javax.swing.JPanel();
		ontologyLabel = new javax.swing.JLabel();
		ontologyComboBox = new javax.swing.JComboBox();
		browseOntologyButton = new javax.swing.JButton();
		sourceLabel = new javax.swing.JLabel();
		annotationComboBox = new javax.swing.JComboBox();
		browseAnnotationButton = new javax.swing.JButton();

		targetDataSourceTextField = new javax.swing.JTextField();
		selectAttributeFileButton = new javax.swing.JButton();
		advancedPanel = new javax.swing.JPanel();
		advancedOptionCheckBox = new javax.swing.JCheckBox();
		textImportCheckBox = new javax.swing.JCheckBox();
		attr2annotationPanel = new javax.swing.JPanel();
		primaryKeyLabel = new javax.swing.JLabel();
		nodeKeyLabel = new javax.swing.JLabel();
		mappingAttributeComboBox = new javax.swing.JComboBox();
		aliasScrollPane = new javax.swing.JScrollPane();
		arrowButton1 = new javax.swing.JButton();
		ontology2annotationPanel = new javax.swing.JPanel();
		targetOntologyLabel = new javax.swing.JLabel();
		ontologyTextField = new javax.swing.JTextField();
		ontologyInAnnotationLabel = new javax.swing.JLabel();
		ontologyInAnnotationComboBox = new javax.swing.JComboBox();
		arrowButton2 = new javax.swing.JButton();
		textImportOptionPanel = new javax.swing.JPanel();
		delimiterPanel = new javax.swing.JPanel();
		tabCheckBox = new JCheckBox();
		commaCheckBox = new JCheckBox();
		semicolonCheckBox = new JCheckBox();
		spaceCheckBox = new JCheckBox();
		otherCheckBox = new JCheckBox();
		otherDelimiterTextField = new javax.swing.JTextField();
		transferNameCheckBox = new javax.swing.JCheckBox();

		attributeNamePanel = new JPanel();
		previewOptionPanel = new JPanel();
		networkImportOptionPanel = new JPanel();

		defaultInteractionLabel = new JLabel();
		defaultInteractionTextField = new JTextField();

		simpleAttributeImportPanel = new javax.swing.JPanel();
		attributeFileLabel = new javax.swing.JLabel();

		startRowSpinner = new JSpinner();
		startRowLabel = new JLabel();

		commentLineLabel = new JLabel();
		commentLineTextField = new JTextField();
		commentLineTextField.setName("commentLineTextField");

		titleLabel.setFont(TITLE_FONT.getFont());

		if (dialogType == NETWORK_IMPORT) {
			previewPanel = new PreviewTablePanel(null, PreviewTablePanel.NETWORK_PREVIEW);
		} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			defaultInteractionLabel.setEnabled(false);
			defaultInteractionTextField.setEnabled(false);
			commentLineTextField.setText("!");
			importAllCheckBox.setEnabled(false);
			previewPanel = new PreviewTablePanel(null, PreviewTablePanel.ONTOLOGY_PREVIEW);
		} else {
			defaultInteractionLabel.setEnabled(false);
			defaultInteractionTextField.setEnabled(false);

			previewPanel = new PreviewTablePanel();
		}

		primaryLabel = new JLabel("");
		primaryKeyComboBox = new JComboBox();
		primaryKeyComboBox.setEnabled(false);
		primaryKeyComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					primaryKeyComboBoxActionPerformed(evt);
				}
			});

		/*
		 * Set tooltips options.
		 */
		ToolTipManager tp = ToolTipManager.sharedInstance();
		tp.setInitialDelay(40);
		tp.setDismissDelay(50000);

		//setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		//titleIconLabel2.setIcon(RIGHT_ARROW_ICON.getIcon());

		//titleIconLabel3.setIcon(new ImageIcon(getClass().getResource("/images/icon48.png")));

		titleSeparator.setForeground(java.awt.Color.blue);

		importButton.setText("Import");
		//importButton.addActionListener(new java.awt.event.ActionListener() {
		//		public void actionPerformed(java.awt.event.ActionEvent evt) {
		//			try {
		//				importButtonActionPerformed(evt);
		//			} catch (IOException e) {
		//				e.printStackTrace();
		//			} catch (Exception e) {
						// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		}
		//	});

		cancelButton.setText("Cancel");
		//cancelButton.addActionListener(new java.awt.event.ActionListener() {
		//		public void actionPerformed(java.awt.event.ActionEvent evt) {
		//			cancelButtonActionPerformed(evt);
		//		}
		//	});

		helpButton.setBackground(new java.awt.Color(255, 255, 255));
		helpButton.setText("?");
		helpButton.setToolTipText("Display help page...");
		helpButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		helpButton.setPreferredSize(new java.awt.Dimension(14, 14));
		helpButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					helpButtonActionPerformed(arg0);
				}
			});

		
		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
		/*
		 * Data Source Panel Layouts.
		 */
			basicPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Sources",
		                                                                  javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
		                                                                  javax.swing.border.TitledBorder.DEFAULT_POSITION,
		                                                                  new java.awt.Font("Dialog",
		                                                                                    1, 11)));

	
			attribuiteLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
			attribuiteLabel.setText("Data Type");

			nodeRadioButton.setText("Node");
			nodeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			nodeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			nodeRadioButton.setSelected(true);
			nodeRadioButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						attributeRadioButtonActionPerformed(evt);
					}
				});

			edgeRadioButton.setText("Edge");
			edgeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			edgeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			edgeRadioButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						attributeRadioButtonActionPerformed(evt);
					}
				});

			networkRadioButton.setText("Network");
			networkRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			networkRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			networkRadioButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						attributeRadioButtonActionPerformed(evt);
					}
				});

			org.jdesktop.layout.GroupLayout attrTypePanelLayout = new org.jdesktop.layout.GroupLayout(attrTypePanel);
			attrTypePanel.setLayout(attrTypePanelLayout);
			attrTypePanelLayout.setHorizontalGroup(attrTypePanelLayout.createParallelGroup(
					org.jdesktop.layout.GroupLayout.LEADING).add(
					attrTypePanelLayout.createSequentialGroup().add(attribuiteLabel).add(24, 24, 24)
							.add(nodeRadioButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(edgeRadioButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(networkRadioButton)
							.addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
			attrTypePanelLayout.setVerticalGroup(attrTypePanelLayout.createParallelGroup(
					org.jdesktop.layout.GroupLayout.LEADING).add(
					attrTypePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
							.add(attribuiteLabel).add(nodeRadioButton).add(edgeRadioButton).add(networkRadioButton)));
		}


		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			panelBuilder = new OntologyPanelBuilder(this, bookmarksProp, bkUtil,
			                                        taskManager, factory, manager,
			                                        tableFactory, tableManager, fileUtil);
			panelBuilder.buildPanel();
		}

		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
			//titleIconLabel1.setIcon(SPREADSHEET_ICON_LARGE.getIcon());

			attributeFileLabel.setText("Input File");
			attributeFileLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
			selectAttributeFileButton.setText("Select File(s)");
			selectAttributeFileButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							setPreviewPanel(evt,false);
						} catch (IOException e) {

							JOptionPane.showMessageDialog(ImportTablePanel.this, "<html>Could not read selected file.<p>See <b>Help->Error Dialog</b> for further details.</html>", "ERROR", JOptionPane.ERROR_MESSAGE);
							logger.warn("Could not read selected file.", e);
						}
					}
				});

			GroupLayout simpleAttributeImportPanelLayout = new GroupLayout(simpleAttributeImportPanel);
			simpleAttributeImportPanel.setLayout(simpleAttributeImportPanelLayout);
			simpleAttributeImportPanelLayout.setHorizontalGroup(simpleAttributeImportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
			                                                                                    .add(simpleAttributeImportPanelLayout.createSequentialGroup()
			                                                                                                                         .add(attributeFileLabel)
			                                                                                                                         .add(24,
			                                                                                                                              24,
			                                                                                                                              24)
			                                                                                                                         .add(targetDataSourceTextField,
			                                                                                                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                                                                                              300,
			                                                                                                                              Short.MAX_VALUE)
			                                                                                                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                                                                                                         .add(selectAttributeFileButton)
			                                                                                                                         .addContainerGap()));
			simpleAttributeImportPanelLayout.setVerticalGroup(simpleAttributeImportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
			                                                                                  .add(simpleAttributeImportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
			                                                                                                                       .add(selectAttributeFileButton)
			                                                                                                                       .add(targetDataSourceTextField,
			                                                                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			                                                                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
			                                                                                                                       .add(attributeFileLabel)));
		}

		GroupLayout basicPanelLayout = new GroupLayout(basicPanel);
		basicPanel.setLayout(basicPanelLayout);

		basicPanelLayout.setHorizontalGroup(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(basicPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(basicPanelLayout.createSequentialGroup()
										.add(simpleAttributeImportPanel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap())
								.add(basicPanelLayout.createSequentialGroup()
										.add(annotationAndOntologyImportPanel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap())
								.add(basicPanelLayout.createSequentialGroup()
										.add(attrTypePanel,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(50,
												Short.MAX_VALUE)))));
		basicPanelLayout.setVerticalGroup(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
		                                                  .add(basicPanelLayout.createSequentialGroup()
		                                                                       .add(attrTypePanel,
		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
		                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
		                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
		                                                                       .add(simpleAttributeImportPanel,
		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
		                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
		                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
		                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                                                                        Short.MAX_VALUE)
		                                                                       .add(annotationAndOntologyImportPanel,
		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
		                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		/*
		 * Layout data for advanced panel
		 */
		advancedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Advanced",
		                                                                     javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
		                                                                     javax.swing.border.TitledBorder.DEFAULT_POSITION,
		                                                                     new java.awt.Font("Dialog",
		                                                                                       1, 11)));

		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT)
		    || (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)) {
			advancedOptionCheckBox.setText("Show Mapping Options");
			advancedOptionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			advancedOptionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
			advancedOptionCheckBox.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						advancedOptionCheckBoxActionPerformed(evt);
					}
				});

			attr2annotationPanel.setBackground(new java.awt.Color(250, 250, 250));
			attr2annotationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
					new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED),
					"Annotation File to Table Mapping", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11)));
			primaryKeyLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
			primaryKeyLabel.setForeground(new java.awt.Color(51, 51, 255));
			primaryKeyLabel.setText("Select the primary key column in table:");

			if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			nodeKeyLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
			nodeKeyLabel.setForeground(new java.awt.Color(255, 0, 51));
			nodeKeyLabel.setText("Key Column for Network");

			mappingAttributeComboBox.setForeground(new java.awt.Color(255, 0, 51));
			mappingAttributeComboBox.setEnabled(false);
			mappingAttributeComboBox.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						nodeKeyComboBoxActionPerformed(evt);
					}
				}
			);
			}

			arrowButton1.setBackground(new java.awt.Color(250, 250, 250));
			arrowButton1.setOpaque(false);
			arrowButton1.setIcon(RIGHT_ARROW_ICON.getIcon());
			arrowButton1.setBorder(null);
			arrowButton1.setBorderPainted(false);

			GroupLayout attr2annotationPanelLayout = new GroupLayout(attr2annotationPanel);
			attr2annotationPanel.setLayout(attr2annotationPanelLayout);
			attr2annotationPanelLayout.setHorizontalGroup(attr2annotationPanelLayout.createParallelGroup(
					GroupLayout.LEADING).add(
					attr2annotationPanelLayout
							.createSequentialGroup()
							.addContainerGap()
							.add(attr2annotationPanelLayout
									.createParallelGroup(GroupLayout.LEADING)
									.add(attr2annotationPanelLayout.createSequentialGroup().add(primaryKeyLabel)
											.add(100, 100, 100))
									.add(attr2annotationPanelLayout.createSequentialGroup().add(primaryLabel)
											.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
											.add(primaryKeyComboBox, 0, 0, Short.MAX_VALUE))
									.add(aliasScrollPane, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(arrowButton1)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(attr2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
									.add(mappingAttributeComboBox, 0, 100, Short.MAX_VALUE).add(nodeKeyLabel))
							.addContainerGap()));
			attr2annotationPanelLayout.setVerticalGroup(attr2annotationPanelLayout.createParallelGroup(
					GroupLayout.LEADING).add(
					attr2annotationPanelLayout
							.createSequentialGroup()
							.add(attr2annotationPanelLayout.createParallelGroup(GroupLayout.BASELINE)
									.add(primaryKeyLabel).add(nodeKeyLabel))
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(attr2annotationPanelLayout
									.createParallelGroup(GroupLayout.BASELINE)
									.add(primaryLabel)
									.add(primaryKeyComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
											GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(attr2annotationPanelLayout
									.createParallelGroup(GroupLayout.LEADING)
									.add(aliasScrollPane, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
									.add(attr2annotationPanelLayout
											.createSequentialGroup()
											.add(attr2annotationPanelLayout
													.createParallelGroup(GroupLayout.TRAILING)
													.add(mappingAttributeComboBox, GroupLayout.PREFERRED_SIZE,
															GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
													.add(arrowButton1)).addContainerGap()))));
		}

		textImportCheckBox.setText("Show Text File Import Options");
		textImportCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		textImportCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		textImportCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					textImportCheckBoxActionPerformed(evt);
				}
			});

		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)
			panelBuilder.buildAnnotationPanel();

		/*
		 * For Network Import
		 */
		if (dialogType == NETWORK_IMPORT) {
			networkImportPanel = new NetworkImportOptionsPanel();
			networkImportPanel.addPropertyChangeListener(this);
			caseSensitiveCheckBox.setVisible(false);
		}

		
		
		textImportOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
				new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED),
				"Text File Import Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11)));
		delimiterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Delimiter"));
		tabCheckBox.setText("Tab");
		tabCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tabCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		tabCheckBox.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				try
				{
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		commaCheckBox.setText("Comma");
		commaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		commaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		commaCheckBox.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				try
				{
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		semicolonCheckBox.setText("Semicolon");
		semicolonCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		semicolonCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		semicolonCheckBox.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				try
				{
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		spaceCheckBox.setText("Space");
		spaceCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		spaceCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		spaceCheckBox.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				try
				{
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		otherCheckBox.setText("Other");
		otherCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		otherCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		otherCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					otherDelimiterTextField.requestFocus();
					delimiterCheckBoxActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		//TODO: VetoableChangeListener???

		otherDelimiterTextField.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent evt)
			{

			}

			public void keyReleased(KeyEvent evt)
			{
				try
				{
					if (otherCheckBox.isSelected())
						displayPreview();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			public void keyTyped(KeyEvent evt)
			{
			}
		});

		GroupLayout delimiterPanelLayout = new GroupLayout(delimiterPanel);
		delimiterPanel.setLayout(delimiterPanelLayout);
		delimiterPanelLayout.setHorizontalGroup(delimiterPanelLayout.createParallelGroup(GroupLayout.LEADING).add(
				delimiterPanelLayout.createSequentialGroup().add(tabCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(commaCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(semicolonCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(spaceCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(otherCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(otherDelimiterTextField, GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)));
		delimiterPanelLayout.setVerticalGroup(delimiterPanelLayout.createParallelGroup(GroupLayout.LEADING).add(
				delimiterPanelLayout
						.createSequentialGroup()
						.add(delimiterPanelLayout
								.createParallelGroup(GroupLayout.BASELINE)
								.add(tabCheckBox)
								.add(commaCheckBox)
								.add(semicolonCheckBox)
								.add(spaceCheckBox)
								.add(otherCheckBox)
								.add(otherDelimiterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		transferNameCheckBox.setEnabled(false);

		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 1, 10000000, 10);
		counterSpinner.setModel(spinnerModel);
		counterSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener()
		{
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt)
			{
				counterSpinnerMouseWheelMoved(evt);
			}
		});
		counterSpinner.setToolTipText("<html><body>Click <strong text=\"red\"><i>Refresh Preview</i></strong> button to update the table.</body></html>");

		counterLabel.setText("entries.");

		previewOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview Options"));
		reloadButton.setText("Refresh Preview");
		reloadButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		reloadButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				try
				{
					reloadButtonActionPerformed(evt);
				} catch (IOException e)
				{
					e.printStackTrace();
					throw new IllegalStateException("Could not reload target file.");
				}
			}
		});

		showAllRadioButton.setText("Show all entries in the file");
		showAllRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		showAllRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

		counterRadioButton.setText("Show first ");
		counterRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		counterRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

		org.jdesktop.layout.GroupLayout previewOptionPanelLayout = new org.jdesktop.layout.GroupLayout(previewOptionPanel);
		previewOptionPanel.setLayout(previewOptionPanelLayout);

		previewOptionPanelLayout.setHorizontalGroup(previewOptionPanelLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				previewOptionPanelLayout
						.createSequentialGroup()
						.addContainerGap()
						.add(showAllRadioButton)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(counterRadioButton)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(counterSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(counterLabel)
						.addContainerGap(76, Short.MAX_VALUE)));
		previewOptionPanelLayout.setVerticalGroup(previewOptionPanelLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
				.add(counterRadioButton)
				.add(counterSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
				.add(counterLabel).add(showAllRadioButton));

		attributeNamePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Column Names"));

		transferNameCheckBox.setText("Transfer first line as column names");

		transferNameCheckBox.setBorder(null);
		transferNameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		transferNameCheckBox.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				transferNameCheckBoxActionPerformed(evt);
			}
		});

		startRowLabel.setText("Start Import Row: ");
		startRowLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		startRowSpinner.setName("startRowSpinner");

		SpinnerNumberModel startRowSpinnerModel = new SpinnerNumberModel(1, 1, 10000000, 1);
		startRowSpinner.setModel(startRowSpinnerModel);
		startRowSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener()
		{
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt)
			{
				startRowSpinnerMouseWheelMoved(evt);
			}
		});
		startRowSpinner.setToolTipText("<html>Load entries from this line. <p>"
				+ "(Click on the <strong><i>Refresh Preview</i></strong> button to refresh preview.)</p></html>");

		commentLineLabel.setText("Comment Line:");

		commentLineTextField.setToolTipText("<html>Lines start with this string will be ignored. <br>"
				+ "(Click on the <strong><i>Refresh Preview</i></strong> button to refresh preview.)</html>");

		GroupLayout attributeNamePanelLayout = new org.jdesktop.layout.GroupLayout(attributeNamePanel);
		attributeNamePanel.setLayout(attributeNamePanelLayout);

		attributeNamePanelLayout.setHorizontalGroup(attributeNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(attributeNamePanelLayout.createSequentialGroup()
						.add(transferNameCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(startRowLabel)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(startRowSpinner,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								51,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(commentLineLabel)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(commentLineTextField,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								24,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(12,
								Short.MAX_VALUE)));
		attributeNamePanelLayout.setVerticalGroup(attributeNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(attributeNamePanelLayout.createSequentialGroup()
						.add(attributeNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
								.add(transferNameCheckBox,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										21,
										Short.MAX_VALUE)
								.add(startRowLabel)
								.add(startRowSpinner,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(commentLineLabel)
								.add(commentLineTextField,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
						.addContainerGap()));

		
		
	
		networkImportOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Network Import Options"));
		defaultInteractionLabel.setText("Default Interaction:");
		defaultInteractionTextField.setText(DEFAULT_INTERACTION);
		defaultInteractionTextField.setToolTipText("<html>If <font color=\"red\"><i>Default Interaction</i></font>"
				+ " is selected, this value will be used for <i>Interaction Type</i>.<br></html>");

		org.jdesktop.layout.GroupLayout networkImportOptionPanelLayout = new org.jdesktop.layout.GroupLayout(networkImportOptionPanel);
		
		networkImportOptionPanel.setLayout(networkImportOptionPanelLayout);

		networkImportOptionPanelLayout.setHorizontalGroup(networkImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(networkImportOptionPanelLayout.createSequentialGroup()
						.addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.add(defaultInteractionLabel)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(defaultInteractionTextField,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								58,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
		networkImportOptionPanelLayout.setVerticalGroup(networkImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(defaultInteractionLabel)
				.add(defaultInteractionTextField,
						org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));

		org.jdesktop.layout.GroupLayout textImportOptionPanelLayout = new org.jdesktop.layout.GroupLayout(textImportOptionPanel);
		textImportOptionPanel.setLayout(textImportOptionPanelLayout);

		textImportOptionPanelLayout.setHorizontalGroup(textImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING,
						textImportOptionPanelLayout.createSequentialGroup()
								.add(attributeNamePanel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
								.add(networkImportOptionPanel,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(29,
										29,
										29)
								.add(reloadButton))
				.add(org.jdesktop.layout.GroupLayout.TRAILING,
						textImportOptionPanelLayout.createSequentialGroup()
								.add(delimiterPanel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
								.add(previewOptionPanel,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
		textImportOptionPanelLayout.setVerticalGroup(textImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(textImportOptionPanelLayout.createSequentialGroup()
						.add(textImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(delimiterPanel,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										71,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(previewOptionPanel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										50,
										Short.MAX_VALUE))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(textImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(networkImportOptionPanel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										45,
										Short.MAX_VALUE)
								.add(attributeNamePanel,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										71,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(org.jdesktop.layout.GroupLayout.TRAILING,
										reloadButton))));

		org.jdesktop.layout.GroupLayout advancedPanelLayout = new org.jdesktop.layout.GroupLayout(advancedPanel);
		advancedPanel.setLayout(advancedPanelLayout);

		advancedPanelLayout.setHorizontalGroup(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(advancedPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(advancedOptionCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(textImportCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(importAllCheckBox)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(caseSensitiveCheckBox))
				.add(attr2annotationPanel,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE)
				.add(ontology2annotationPanel,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE)
				.add(textImportOptionPanel,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE));
		advancedPanelLayout.setVerticalGroup(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(advancedPanelLayout.createSequentialGroup()
						.add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
								.add(advancedOptionCheckBox)
								.add(textImportCheckBox)
								.add(importAllCheckBox)
								.add(caseSensitiveCheckBox))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(attr2annotationPanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(ontology2annotationPanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(textImportOptionPanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		globalLayout();

		attr2annotationPanel.setVisible(false);
		basicPanel.repaint();
		ontology2annotationPanel.setVisible(false);
		textImportOptionPanel.setVisible(false);

		
		if (dialogType == SIMPLE_ATTRIBUTE_IMPORT){
			mappingAttributeComboBox.setVisible(false);
			arrowButton1.setVisible(false);
			networkImportOptionPanel.setVisible(false);
		}
		//pack();
	} // </editor-fold>

	/**
	 * Update UI based on the primary key selection.
	 * @param evt
	 */
	private void primaryKeyComboBoxActionPerformed(ActionEvent evt) {
		// Not necessary in Network Import.
		if (dialogType == NETWORK_IMPORT)
			return;

		// Update primary key index.
		keyInFile = primaryKeyComboBox.getSelectedIndex();

		// Update
		previewPanel.getPreviewTable()
		            .setDefaultRenderer(Object.class, getRenderer(previewPanel.getFileType()));

		try {
			if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
				//setStatusBar(new URL(targetDataSourceTextField.getText()));
			} else {
				setStatusBar(new URL(annotationUrlMap.get(annotationComboBox.getSelectedItem()
				                                                            .toString())));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		previewPanel.repaint();

		JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());
		curTable.setModel(aliasTableModelMap.get(previewPanel.getSelectedSheetName()));

		if (curTable.getCellRenderer(0, 1) != null) {
			((AliasTableRenderer) curTable.getCellRenderer(0, 1)).setPrimaryKey(keyInFile);
			aliasScrollPane.setViewportView(curTable);

			primaryKeyMap.put(previewPanel.getSelectedSheetName(),
			                  primaryKeyComboBox.getSelectedIndex());

			aliasScrollPane.setViewportView(curTable);
			curTable.repaint();
		}

		// Update table view
		ColumnResizer.adjustColumnPreferredWidths(previewPanel.getPreviewTable());
		previewPanel.getPreviewTable().repaint();
	}

	private void helpButtonActionPerformed(ActionEvent evt) {
		// TODO: Quick help should be implemented!
	}

	private void attributeRadioButtonActionPerformed(ActionEvent evt) {
		CyNetwork network = CytoscapeServices.cyApplicationManager.getCurrentNetwork();

		if (nodeRadioButton.isSelected()) {
			//selectedAttributes = Cytoscape.getNodeAttributes();
			if (network != null){
				selectedAttributes = network.getDefaultNodeTable();
			}

			objType = NODE;
		} else if (edgeRadioButton.isSelected()) {
			//selectedAttributes = Cytoscape.getEdgeAttributes();
			if (network != null){
				selectedAttributes = network.getDefaultEdgeTable();
			}

			objType = EDGE;
		} else {
			//selectedAttributes = Cytoscape.getNetworkAttributes();
			logger.info("\nNote: ImportTextTableFDialog.attributeRadioButtonActionPerformed():Import network table not implemented yet!\n");
			objType = NETWORK;
		}

		updateMappingAttributeComboBox();
		setKeyList();
	}

	private void advancedOptionCheckBoxActionPerformed(ActionEvent evt) {
		if (advancedOptionCheckBox.isSelected()) {
			attr2annotationPanel.setVisible(true);
			ontology2annotationPanel.setVisible(true);
		} else {
			attr2annotationPanel.setVisible(false);
			ontology2annotationPanel.setVisible(false);
		}

		if (dialogType == ImportTablePanel.SIMPLE_ATTRIBUTE_IMPORT) {
			ontology2annotationPanel.setVisible(false);
		}
		JDialog dlg = (JDialog)SwingUtilities.getWindowAncestor(this);
		dlg.pack();
	}

	/**
	 * If Import All selected, ID combo box should be set to ID
	 * @param evt
	 */
	private void importAllCheckBoxActionPerformed(ActionEvent evt) {
		if (importAllCheckBox.isSelected()) {
			// Lock key to ID
			mappingAttributeComboBox.setSelectedItem(ID);
			mappingAttributeComboBox.setEnabled(false);
		} else {
			mappingAttributeComboBox.setEnabled(true);
		}
	}

	private void nodeKeyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
		previewPanel.getPreviewTable()
		            .setDefaultRenderer(Object.class, getRenderer(previewPanel.getFileType()));

		setKeyList();
	}

    /*
     * This method indicates whether the first row of a file that is being imported as a table should be used to
     * populate column names.
     */
    private void useFirstRow(boolean useFirstRow)
    {
        final DefaultTableModel model = (DefaultTableModel) previewPanel.getPreviewTable().getModel();
        if( useFirstRow ) {
            if ((previewPanel.getPreviewTable() != null) && (model != null)) {
                columnHeaders = new String[previewPanel.getPreviewTable().getColumnCount()];

                for (int i = 0; i < columnHeaders.length; i++) {
                    // Save the header
                    columnHeaders[i] = previewPanel.getPreviewTable().getColumnModel().getColumn(i)
                            .getHeaderValue().toString();
                    previewPanel.getPreviewTable().getColumnModel().getColumn(i)
                            .setHeaderValue((String) model.getValueAt(0, i));
                }

                model.removeRow(0);
                previewPanel.getPreviewTable().getTableHeader().resizeAndRepaint();
            }

        } else {
            // Restore row
            String currentName = null;
            Object headerVal = null;

            for (int i = 0; i < columnHeaders.length; i++) {
                headerVal = previewPanel.getPreviewTable().getColumnModel().getColumn(i)
                        .getHeaderValue();

                if (headerVal == null) {
                    currentName = "";
                } else {
                    currentName = headerVal.toString();
                }

                previewPanel.getPreviewTable().getColumnModel().getColumn(i)
                        .setHeaderValue(columnHeaders[i]);
                columnHeaders[i] = currentName;
            }

            model.insertRow(0, columnHeaders);
            previewPanel.getPreviewTable().getTableHeader().resizeAndRepaint();
            //startRowSpinner.setEnabled(true);
        }

        updateAliasTable();
        updatePrimaryKeyComboBox();

    }


	private void transferNameCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
		useFirstRow(transferNameCheckBox.isSelected());
		repaint();
	}


	/**
	 * Load from the data source.<br>
	 *
	 * @throws Exception
	 */
	public void importTable() throws Exception {
		if (checkDataSourceError() == false)
			return;
		/*
		 * Get import flags
		 */
		final int colCount = previewPanel.getPreviewTable().getColumnModel().getColumnCount();
		importFlag = new boolean[colCount];

		for (int i = 0; i < colCount; i++) {
			importFlag[i] = ((AttributePreviewTableCellRenderer) previewPanel.getPreviewTable()
			                                                                 .getCellRenderer(0, i))
			                .getImportFlag(i);
		}

		/*
		 * Get Attribute Names
		 */
		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = previewPanel.getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

			if (attrNameList.contains(curName)) {
				int dupIndex = 0;

				for (int idx = 0; idx < attrNameList.size(); idx++) {
					if (curName.equals(attrNameList.get(idx))) {
						dupIndex = idx;

						break;
					}
				}

				if (importFlag[i] && importFlag[dupIndex]) {
					final JLabel label = new JLabel("Duplicate Column Name Found: " + curName);
					label.setForeground(Color.RED);
					JOptionPane.showMessageDialog(this, label);

					return;
				}
			}

			if (curName == null) {
				attrNameList.add("Column " + i);
			} else {
				attrNameList.add(curName.toString());
			}
		}
		attributeNames = attrNameList.toArray(new String[0]);
		
		
		final Byte[] test = previewPanel.getDataTypes(previewPanel.getSelectedSheetName());

		final Byte[] attributeTypes = new Byte[test.length];

		for (int i = 0; i < test.length; i++) {
			attributeTypes[i] = test[i];
		}

		final List<Integer> aliasList = new ArrayList<Integer>();
		String mappingAttribute = ID;

		if (dialogType != NETWORK_IMPORT) {
			/*
			 * Get column indecies for alias
			 */
			JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());

			if (curTable != null) {
				for (int i = 0; i < curTable.getModel().getRowCount(); i++) {
					if ((Boolean) curTable.getModel().getValueAt(i, 0) == true) {
						aliasList.add(i);
					}
				}
			}

			/*
			 * Get mapping attribute
			 */
			mappingAttribute = mappingAttributeComboBox.getSelectedItem().toString();
		}

		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)
			panelBuilder.importOntologyAndAnnotation();
				
	}

	private void setPreviewPanel(ActionEvent evt, boolean useFirstRow) throws IOException {

		readAnnotationForPreview( checkDelimiter());
		transferNameCheckBox.setEnabled(true);
		transferNameCheckBox.setSelected(true);
		if( useFirstRow )
			useFirstRow(true);

		if (previewPanel.getPreviewTable() == null) {
			return;
		} else {
			ColumnResizer.adjustColumnPreferredWidths(previewPanel.getPreviewTable());
			previewPanel.getPreviewTable().repaint();
		}
	}

	private void delimiterCheckBoxActionPerformed(ActionEvent evt) throws IOException {
        transferNameCheckBox.setSelected(false);
		displayPreview();
	}

	private void textImportCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
		if (textImportCheckBox.isSelected()) {
			textImportOptionPanel.setVisible(true);
		} else {
			textImportOptionPanel.setVisible(false);
		}

		JDialog dlg = (JDialog)SwingUtilities.getWindowAncestor(this);
		dlg.pack();
		
	}


	private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt)
	    throws IOException {
		displayPreview();

		if (transferNameCheckBox.isSelected())
			this.transferNameCheckBoxActionPerformed(null);
	}

	/**
	 * Actions for mouse wheel movement
	 *
	 * @param evt
	 */
	private void counterSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
		JSpinner source = (JSpinner) evt.getSource();

		SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
		Integer oldValue = (Integer) source.getValue();
		int intValue = oldValue.intValue()
		               - (evt.getWheelRotation() * model.getStepSize().intValue());
		Integer newValue = new Integer(intValue);

		if ((model.getMaximum().compareTo(newValue) >= 0)
		    && (model.getMinimum().compareTo(newValue) <= 0)) {
			source.setValue(newValue);
		}
	}

	/**
	 * Actions for selecting start line.
	 *
	 * @param evt
	 */
	private void startRowSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
		JSpinner source = (JSpinner) evt.getSource();

		SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
		Integer oldValue = (Integer) source.getValue();
		int intValue = oldValue.intValue()
		               - (evt.getWheelRotation() * model.getStepSize().intValue());
		Integer newValue = new Integer(intValue);

		if ((model.getMaximum().compareTo(newValue) >= 0)
		    && (model.getMinimum().compareTo(newValue) <= 0)) {
			source.setValue(newValue);
		}
	}

	/* =============================================================================================== */
	private List<Integer> getAliasList() {
		final List<Integer> aliasList = new ArrayList<Integer>();
		AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());

		if (curModel == null) {
			return aliasList;
		}

		for (int i = 0; i < curModel.getRowCount(); i++) {
			if ((Boolean) curModel.getValueAt(i, 0)) {
				aliasList.add(i);
			}
		}

		return aliasList;
	}


	private void displayPreview() throws IOException {
		final String selectedSourceName;
		//final URL sourceURL;

		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			selectedSourceName = annotationComboBox.getSelectedItem().toString();
			//sourceURL = new URL(annotationUrlMap.get(selectedSourceName));
		} else {
			selectedSourceName = targetDataSourceTextField.getText();
			//sourceURL = new URL(selectedSourceName);
		}

		readAnnotationForPreview(checkDelimiter());
		previewPanel.repaint();
	}

	private void updateComponents() throws JAXBException, IOException {

		if (dialogType == SIMPLE_ATTRIBUTE_IMPORT) {
			//setTitle("Import Annotation File");
			//titleLabel.setText("Import Attribute from Table");
			annotationAndOntologyImportPanel.setVisible(false);
			importAllCheckBox.setVisible(false);
		} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			//setTitle("Import Ontology Data and Annotations");
			titleLabel.setText("Import Ontology and Annotation");
			ontology2annotationPanel.setVisible(false);

			// Update available file lists.
			panelBuilder.setOntologyComboBox();
			panelBuilder.setAnnotationComboBox();

			if(ontologyComboBox.getSelectedItem() != null)
				ontologyTextField.setText(ontologyComboBox.getSelectedItem().toString());
		} else if (dialogType == NETWORK_IMPORT) {
			//setTitle("Import Network and Edge Attributes from Table");
			//titleLabel.setText("Import Network from Table");
			annotationAndOntologyImportPanel.setVisible(false);

			importAllCheckBox.setVisible(false);
		}

		reloadButton.setEnabled(false);
		startRowSpinner.setEnabled(false);
		startRowLabel.setEnabled(false);
		previewPanel.getPreviewTable().getTableHeader().setReorderingAllowed(false);
		setRadioButtonGroup();
		//pack();

		if (dialogType == NETWORK_IMPORT){
			// do nothing
		}
		else { // attribute import
			updateMappingAttributeComboBox();
		}

		setStatusBar("-", "-", "File Size: Unknown");
	}

	private void updatePrimaryKeyComboBox() {
		final DefaultTableModel model = (DefaultTableModel) previewPanel.getPreviewTable().getModel();

		primaryKeyComboBox.setRenderer(new ComboBoxRenderer(attributeDataTypes));

		if ((model != null) && (model.getColumnCount() > 0)) {
			primaryKeyComboBox.removeAllItems();

			Object curValue = null;

			for (int i = 0; i < model.getColumnCount(); i++) {
				curValue = previewPanel.getPreviewTable().getColumnModel().getColumn(i)
				                       .getHeaderValue();

				if (curValue != null) {
					primaryKeyComboBox.addItem(curValue.toString());
				} else {
					primaryKeyComboBox.addItem("");
				}
			}
		}

		primaryKeyComboBox.setEnabled(true);

		Integer selectedIndex = primaryKeyMap.get(previewPanel.getSelectedSheetName());

		if (selectedIndex == null) {
			//primaryKeyComboBox.setSelectedIndex(0);
		} else {
			primaryKeyComboBox.setSelectedIndex(selectedIndex);
		}
	}

	protected static ImageIcon getDataTypeIcon(Byte dataType) {
		ImageIcon dataTypeIcon = null;

		if (dataType == AttributeTypes.TYPE_STRING) {
			dataTypeIcon = STRING_ICON.getIcon();
		} else if (dataType == AttributeTypes.TYPE_INTEGER) {
			dataTypeIcon = INT_ICON.getIcon();
		} else if (dataType == AttributeTypes.TYPE_FLOATING) {
			dataTypeIcon = FLOAT_ICON.getIcon();
		} else if (dataType == AttributeTypes.TYPE_BOOLEAN) {
			dataTypeIcon = BOOLEAN_ICON.getIcon();
		} else if (dataType == AttributeTypes.TYPE_SIMPLE_LIST) {
			dataTypeIcon = LIST_ICON.getIcon();
		}

		return dataTypeIcon;
	}

	private void setRadioButtonGroup() {
		attrTypeButtonGroup.add(nodeRadioButton);
		attrTypeButtonGroup.add(edgeRadioButton);
		attrTypeButtonGroup.add(networkRadioButton);
		attrTypeButtonGroup.setSelected(nodeRadioButton.getModel(), true);

		importTypeButtonGroup.add(showAllRadioButton);
		importTypeButtonGroup.add(counterRadioButton);
		importTypeButtonGroup.setSelected(counterRadioButton.getModel(), true);

		if( fileType != null && fileType.equalsIgnoreCase(SupportedFileType.CSV.getExtension()) )
			commaCheckBox.setSelected(true);
		else
			tabCheckBox.setSelected(true);

		tabCheckBox.setEnabled(false);
		commaCheckBox.setEnabled(false);
		spaceCheckBox.setEnabled(false);

		if (dialogType == NETWORK_IMPORT) {
			spaceCheckBox.setSelected(true);
		} else {
			spaceCheckBox.setSelected(false);
		}

		semicolonCheckBox.setEnabled(false);
		otherCheckBox.setEnabled(false);
		otherDelimiterTextField.setEnabled(false);
	}

	private void setOntologyInAnnotationComboBox() {
		final DefaultTableModel model = (DefaultTableModel) previewPanel.getPreviewTable().getModel();

		if ((model != null) && (model.getColumnCount() > 0)) {
			ontologyInAnnotationComboBox.removeAllItems();

			for (int i = 0; i < model.getColumnCount(); i++) {
				ontologyInAnnotationComboBox.addItem(previewPanel.getPreviewTable().getColumnModel()
				                                                 .getColumn(i).getHeaderValue()
				                                                 .toString());
			}
		}

		ontologyInAnnotationComboBox.setEnabled(true);
	}


	protected void readAnnotationForPreviewOntology(URL sourceURL,  List<String> delimiters) throws IOException {
		
		final int previewSize;

		if (showAllRadioButton.isSelected())
			previewSize = -1;
		else
			previewSize = Integer.parseInt(counterSpinner.getValue().toString());

		/*
		 * Load data from the given URL.
		 */
		final String commentChar = commentLineTextField.getText();
		int startLine = getStartLineNumber();
		InputStream tempIs = URLUtil.getInputStream(sourceURL);
		previewPanel.setPreviewTable( workbook, this.fileType,  tempIs, delimiters, null, previewSize,
				commentChar, startLine - 1);
		
		tempIs.close();
		
		if (previewPanel.getPreviewTable() == null)
			return;

		// Initialize import flags.
		final int colSize = previewPanel.getPreviewTable().getColumnCount();
		importFlag = new boolean[colSize];

		for (int i = 0; i < colSize; i++) {
			importFlag[i] = true;
		}

		listDataTypes = previewPanel.getCurrentListDataTypes();

		for (int i = 0; i < previewPanel.getTableCount(); i++) {
			final int columnCount = previewPanel.getPreviewTable(i).getColumnCount();

			aliasTableModelMap.put(previewPanel.getSheetName(i),
			                       new AliasTableModel(keyTable, columnCount));

			if (previewPanel.getFileType() == FileTypes.GENE_ASSOCIATION_FILE) {
				TableModel previewModel = previewPanel.getPreviewTable(i).getModel();
				String[] columnNames = new String[previewModel.getColumnCount()];

				for (int j = 0; j < columnNames.length; j++) {
					columnNames[j] = previewModel.getColumnName(j);
				}

				initializeAliasTable(columnCount, columnNames, i);

				AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSheetName(i));
				curModel.setValueAt(true, DB_OBJECT_SYNONYM.getPosition(), 0);
				disableComponentsForGA();
			} else {
				initializeAliasTable(columnCount, null, i);
			}

			updatePrimaryKeyComboBox();
			
			setOntologyInAnnotationComboBox();

			attributeRadioButtonActionPerformed(null);
		}
		
	}


	/**
	 * Display preview table
	 *
	 * @param delimiters
	 * @throws IOException
	 */
	protected void readAnnotationForPreview ( List<String> delimiters) throws IOException {

		/*
		 * Check number of lines we should load. if -1, load everything in the
		 * file.
		 */
		final int previewSize;

		if (showAllRadioButton.isSelected())
			previewSize = -1;
		else
			previewSize = Integer.parseInt(counterSpinner.getValue().toString());

		/*
		 * Load data from the given URL.
		 */
		final String commentChar = commentLineTextField.getText();
		int startLine = getStartLineNumber();
	
		//creating the IS copy	
		InputStream tempIs = null;
		if (tempFile != null){
			tempIs = new FileInputStream(tempFile);
		}
		// Load Spreadsheet data for preview.
		if(fileType != null && (fileType.equalsIgnoreCase(
				SupportedFileType.EXCEL.getExtension())
				|| fileType.equalsIgnoreCase(
						SupportedFileType.OOXML.getExtension())) && workbook == null) {
			try {
				workbook = WorkbookFactory.create(tempIs);
			} catch (InvalidFormatException e) {
				tempIs.close();
				throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?", e);
			}
			
		}
		
		if (tempIs != null) {
			tempIs.close();
		}
		
		InputStream tempIs2 = null;
		if (tempFile != null)
			 tempIs2 =  new FileInputStream(tempFile);


		previewPanel.setPreviewTable( workbook, this.fileType,  tempIs2, delimiters, null, previewSize,
				commentChar, startLine - 1);

		if (tempIs2 != null){
			tempIs2.close();
		}

		if (previewPanel.getPreviewTable() == null)
			return;

		// Initialize import flags.
		final int colSize = previewPanel.getPreviewTable().getColumnCount();
		importFlag = new boolean[colSize];

		for (int i = 0; i < colSize; i++) {
			importFlag[i] = true;
		}

		listDataTypes = previewPanel.getCurrentListDataTypes();


		if (dialogType == NETWORK_IMPORT) {

			final String[] columnNames = new String[previewPanel.getPreviewTable().getColumnCount()];
			for (int i = 0; i < columnNames.length; i++)
				columnNames[i] = previewPanel.getPreviewTable().getColumnName(i);

			networkImportPanel.setComboBoxes(columnNames);

			if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
					this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {
				switchDelimiterCheckBoxes(false);
			} else {
				switchDelimiterCheckBoxes(true);
			}

			AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) previewPanel.getPreviewTable()
			                                                                                         .getCellRenderer(0,
			                                                                                                          0);
			rend.setSourceIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
			rend.setTargetIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
			rend.setInteractionIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
		} else {
			for (int i = 0; i < previewPanel.getTableCount(); i++) {
				final int columnCount = previewPanel.getPreviewTable(i).getColumnCount();

				aliasTableModelMap.put(previewPanel.getSheetName(i),
				                       new AliasTableModel(keyTable, columnCount));

				if (previewPanel.getFileType() == FileTypes.GENE_ASSOCIATION_FILE) {
					TableModel previewModel = previewPanel.getPreviewTable(i).getModel();
					String[] columnNames = new String[previewModel.getColumnCount()];

					for (int j = 0; j < columnNames.length; j++) {
						columnNames[j] = previewModel.getColumnName(j);
					}

					initializeAliasTable(columnCount, columnNames, i);

					AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSheetName(i));
					curModel.setValueAt(true, DB_OBJECT_SYNONYM.getPosition(), 0);
					disableComponentsForGA();
				} else {
					initializeAliasTable(columnCount, null, i);
				}

				updatePrimaryKeyComboBox();
			}

			setOntologyInAnnotationComboBox();
			/*
			 * If this is not an Excel file, enable delimiter checkboxes.
			 */
			FileTypes type = checkFileType();

			if (fileType != null) {
				if (type == FileTypes.GENE_ASSOCIATION_FILE) {
					primaryKeyComboBox.setSelectedIndex(DB_OBJECT_SYMBOL.getPosition());
					ontologyInAnnotationComboBox.setSelectedIndex(GO_ID.getPosition());
					disableComponentsForGA();
				} else if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) == false
						|| this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension()) == false) {
					switchDelimiterCheckBoxes(true);
					nodeRadioButton.setEnabled(true);
					edgeRadioButton.setEnabled(true);
					networkRadioButton.setEnabled(true);
					importAllCheckBox.setEnabled(false);
				} else {
					importAllCheckBox.setEnabled(false);
				}
			}

			attributeRadioButtonActionPerformed(null);
		}

		reloadButton.setEnabled(true);
		startRowSpinner.setEnabled(true);
		startRowLabel.setEnabled(true);
	}

	private void disableComponentsForGA() {
		primaryKeyComboBox.setEnabled(true);
		aliasTableMap.get(previewPanel.getSelectedSheetName()).setEnabled(true);
		ontologyInAnnotationComboBox.setEnabled(false);

		nodeRadioButton.setSelected(true);
		nodeRadioButton.setEnabled(false);
		edgeRadioButton.setEnabled(false);
		networkRadioButton.setEnabled(false);

		tabCheckBox.setEnabled(false);
		tabCheckBox.setSelected(true);
		commaCheckBox.setEnabled(false);
		commaCheckBox.setSelected(false);
		spaceCheckBox.setEnabled(false);
		spaceCheckBox.setSelected(false);
		semicolonCheckBox.setEnabled(false);
		semicolonCheckBox.setSelected(false);
		otherCheckBox.setEnabled(false);
		otherCheckBox.setSelected(false);
		otherDelimiterTextField.setEnabled(false);

		importAllCheckBox.setEnabled(false);
	}

	private void switchDelimiterCheckBoxes(Boolean state) {
		tabCheckBox.setEnabled(state);
		commaCheckBox.setEnabled(state);
		spaceCheckBox.setEnabled(state);
		semicolonCheckBox.setEnabled(state);
		otherCheckBox.setEnabled(state);
		otherDelimiterTextField.setEnabled(state);
	}

	private FileTypes checkFileType() {
		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			return FileTypes.CUSTOM_ANNOTATION_FILE;
		} else if (dialogType == NETWORK_IMPORT) {
			return FileTypes.NETWORK_FILE;
		}

		return FileTypes.ATTRIBUTE_FILE;
	}

	private void setStatusBar(URL sourceURL) {
		final String centerMessage;
		final String rightMessage;

		if (showAllRadioButton.isSelected()) {
			centerMessage = "All entries are loaded for preview.";
		} else {
			centerMessage = "First " + counterSpinner.getValue().toString()
			                + " entries are loaded for preview.";
		}

		if (sourceURL.toString().startsWith("file:")) {
			int fileSize = 0;

			BufferedInputStream fis = null;
			try {
				fis = (BufferedInputStream) sourceURL.openStream();
				fileSize = fis.available();
				fis.close();
			} catch (IOException e) {
				if(fis!= null)
					try {
						fis.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}

			if ((fileSize / 1000) == 0) {
				rightMessage = "File Size: " + fileSize + " Bytes";
			} else {
				rightMessage = "File Size: " + (fileSize / 1000) + " KBytes";
			}
		} else {
			rightMessage = "File Size Unknown (Remote Data Source)";
		}

		setStatusBar("Key-Value Matched: "
		             + previewPanel.checkKeyMatch(primaryKeyComboBox.getSelectedIndex()),
		             centerMessage, rightMessage);
	}

	/**
	 * Update the list of mapping attributes.
	 *
	 */
	private void setKeyList() {

		if (mappingAttributeComboBox.getSelectedItem() == null) {
			return;
		}

		if (CytoscapeServices.cyApplicationManager.getCurrentNetwork() == null){
			return;
		}

		String selectedKeyAttribute = mappingAttributeComboBox.getSelectedItem().toString();

		Iterator it;

		Set<Object> valueSet = new TreeSet<Object>();

		//TODO -- setKeyList
		if (selectedKeyAttribute.equals(ID)) {

			if (objType == NODE) {
				for ( CyNode node : network.getNodeList() ) {
					valueSet.add(network.getRow(node).get(ID, String.class)); // ID = "name"
				}
			} else if (objType == EDGE) {
				for ( CyEdge edge : network.getEdgeList() ) {
					valueSet.add(network.getRow(edge).get(ID, String.class)); // ID = "name"
				}
			} 
		} 
		previewPanel.setKeyAttributeList(valueSet);

	}

	private void updateAliasTableCell(String name, int columnIndex) {
		JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());
		curTable.setDefaultRenderer(Object.class,
		                            new AliasTableRenderer(attributeDataTypes,
		                                                   primaryKeyComboBox.getSelectedIndex()));

		AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());
		curModel.setValueAt(name, columnIndex, 1);
		curTable.setModel(curModel);
		curTable.repaint();
		aliasScrollPane.repaint();
		repaint();
	}

	private void updateAliasTable() {
		if (dialogType == NETWORK_IMPORT) {
			return;
		}

		JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());

		curTable.setDefaultRenderer(Object.class,
		                            new AliasTableRenderer(attributeDataTypes,
		                                                   primaryKeyComboBox.getSelectedIndex()));

		AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());

		Object curValue = null;

		for (int i = 0; i < previewPanel.getPreviewTable().getColumnCount(); i++) {
			curValue = previewPanel.getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

			if (curValue != null) {
				curModel.setValueAt(curValue.toString(), i, 1);
			} else {
				previewPanel.getPreviewTable().getColumnModel().getColumn(i).setHeaderValue("");
				curModel.setValueAt("", i, 1);
			}
		}

		curTable.setModel(curModel);
		aliasScrollPane.setViewportView(curTable);
		aliasScrollPane.repaint();
	}

	private void initializeAliasTable(int rowCount, String[] columnNames) {
		initializeAliasTable(rowCount, columnNames, -1);
	}

	private void initializeAliasTable(int rowCount, String[] columnNames, int sheetIndex) {
		Object[][] keyTableData = new Object[rowCount][keyTable.length];

		AliasTableModel curModel = null;
		String tabName;

		if (sheetIndex == -1) {
			tabName = previewPanel.getSelectedSheetName();
		} else {
			tabName = previewPanel.getSheetName(sheetIndex);
		}

		curModel = aliasTableModelMap.get(tabName);

		curModel = new AliasTableModel();

		Byte[] dataTypeArray = previewPanel.getDataTypes(tabName);

		for (int i = 0; i < rowCount; i++) {
			keyTableData[i][0] = new Boolean(false);

			if (columnNames == null) {
				keyTableData[i][1] = "Column " + (i + 1);
			} else {
				keyTableData[i][1] = columnNames[i];
			}

			if (dataTypeArray.length <= i) {
				attributeDataTypes.add(AttributeTypes.TYPE_STRING);
			} else {
				attributeDataTypes.add(dataTypeArray[i]);
			}

			keyTableData[i][2] = "String";
		}

		curModel = new AliasTableModel(keyTableData, keyTable);

		aliasTableModelMap.put(tabName, curModel);

		curModel.addTableModelListener(this);
		/*
		 * Set the list and combo box
		 */
		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) 
			mappingAttributeComboBox.setEnabled(true);

		JTable curTable = new JTable();
		curTable.setModel(curModel);
		aliasTableMap.put(tabName, curTable);

		curTable.setDefaultRenderer(Object.class,
		                            new AliasTableRenderer(attributeDataTypes,
		                                                   primaryKeyComboBox.getSelectedIndex()));
		curTable.setEnabled(true);
		curTable.setSelectionBackground(Color.white);
		curTable.getTableHeader().setReorderingAllowed(false);

		curTable.getColumnModel().getColumn(0).setPreferredWidth(55);
		curTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		curTable.getColumnModel().getColumn(2).setPreferredWidth(100);

		aliasScrollPane.setViewportView(curTable);
		repaint();
	}

	private void updateMappingAttributeComboBox() {

		mappingAttributeComboBox.removeAllItems();

		final ListCellRenderer lcr = mappingAttributeComboBox.getRenderer();
		mappingAttributeComboBox.setRenderer(new ListCellRenderer() {
				public Component getListCellRendererComponent(JList list, Object value, int index,
				                                              boolean isSelected,
				                                              boolean cellHasFocus) {
					JLabel cmp = (JLabel) lcr.getListCellRendererComponent(list, value, index,
					                                                       isSelected, cellHasFocus);

					if (value.equals(ID)) {
						cmp.setIcon(ID_ICON.getIcon());
					} else {
						//cmp.setIcon(getDataTypeIcon(selectedAttributes.getColumnTypeMap().get(value.toString())));
					}

					return cmp;
				}
			});

		mappingAttributeComboBox.addItem(ID);

		if (CytoscapeServices.cyApplicationManager.getCurrentNetwork() == null)
			return;

		for (final CyColumn column : selectedAttributes.getColumns()) {
			final String columnName = column.getName();
			if (columnName.equalsIgnoreCase(ID))
				continue;

			final Class<?> type = column.getType();
			if (type == String.class || type == Integer.class || type == Double.class || type == List.class)
				mappingAttributeComboBox.addItem(columnName);
		}
	}

	private TableCellRenderer getRenderer(FileTypes type) {
		final TableCellRenderer rend;

		if (type == FileTypes.GENE_ASSOCIATION_FILE) {
			keyInFile = this.primaryKeyComboBox.getSelectedIndex();

			int ontologyCol = this.ontologyInAnnotationComboBox.getSelectedIndex();
			List<Integer> gaAlias = new ArrayList<Integer>();

			AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());

			for (int i = 0; i < curModel.getColumnCount(); i++) {
				if ((Boolean) curModel.getValueAt(i, 0)) {
					gaAlias.add(i);
				}
			}

			gaAlias.add(DB_OBJECT_SYNONYM.getPosition());

			rend = new AttributePreviewTableCellRenderer(keyInFile, gaAlias, ontologyCol,
			                                             TAXON.getPosition(), importFlag,
			                                             listDelimiter);
		} else {
			rend = new AttributePreviewTableCellRenderer(keyInFile, new ArrayList<Integer>(),
			                                             AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
			                                             AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
			                                             importFlag, listDelimiter);
		}

		return rend;
	}

	private void setStatusBar(String message1, String message2, String message3) {
		statusBar.setLeftLabel(message1);
		statusBar.setCenterLabel(message2);
		statusBar.setRightLabel(message3);
	}

	/**
	 * Alias table changed.
	 */
	public void tableChanged(TableModelEvent evt) {
		final int row = evt.getFirstRow();
		final int col = evt.getColumn();
		AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());

		if (col == 0) {
			previewPanel.setAliasColumn(row, (Boolean) curModel.getValueAt(row, col));
		}

		aliasScrollPane.repaint();
	}

	public List<String> checkDelimiter() {
		final List<String> delList = new ArrayList<String>();

		if (tabCheckBox.isSelected()) {
			delList.add(TextFileDelimiters.TAB.toString());
		}

		if (commaCheckBox.isSelected()) {
			delList.add(TextFileDelimiters.COMMA.toString());
		}

		if (spaceCheckBox.isSelected()) {
			delList.add(TextFileDelimiters.SPACE.toString());
		}

		if (semicolonCheckBox.isSelected()) {
			delList.add(TextFileDelimiters.SEMICOLON.toString());
		}

		if (otherCheckBox.isSelected() && otherDelimiterTextField.getText().trim().length() > 0) {
			delList.add(otherDelimiterTextField.getText());
		}

		return delList;
	}

	/**
	 * Error checker for imput table.<br>
	 *
	 * @return true if table looks OK.
	 */
	private boolean checkDataSourceError() {
		
		final JTable table = previewPanel.getPreviewTable();

		if ((table == null) || (table.getModel() == null) || (table.getColumnCount() == 0)) {
			JOptionPane.showMessageDialog(this, "No table selected.", "Invalid Table.",
			                              JOptionPane.INFORMATION_MESSAGE);

			return false;
		} else if ((table.getColumnCount() < 2) && (dialogType != NETWORK_IMPORT)) {
			JOptionPane.showMessageDialog(this, "Table should contain at least 2 columns.",
			                              "Invalid Table.", JOptionPane.INFORMATION_MESSAGE);

			return false;
		}

		if (dialogType == NETWORK_IMPORT) {
			final int sIdx = networkImportPanel.getSourceIndex();
			final int tIdx = networkImportPanel.getTargetIndex();
			final int iIdx = networkImportPanel.getInteractionIndex();

			if ((sIdx == tIdx) || (((iIdx == sIdx) || (iIdx == tIdx)) && (iIdx != -1))) {
				JOptionPane.showMessageDialog(this,
				                              "Columns for source, target, and interaction type must be distinct.",
				                              "Same column index.", JOptionPane.INFORMATION_MESSAGE);

				return false;
			}
		}

		return true;
	}

	/*
	 * Layout Information for the entire dialog.<br>
	 *
	 * <p> This layout will be switched by dialog type parameter. </p>
	 *
	 */
	private void globalLayout() {
		GroupLayout layout = new GroupLayout(this); //getContentPane());
		//getContentPane().setLayout(layout);
		this.setLayout(layout);
		/*
		 * Case 1: Simple Attribute Import
		 */
		if (dialogType == SIMPLE_ATTRIBUTE_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
					layout.createSequentialGroup()
							.addContainerGap()
							.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
									.add(org.jdesktop.layout.GroupLayout.TRAILING,
											layout.createSequentialGroup()
													.add(statusBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
															org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
															Short.MAX_VALUE)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
													.add(importButton)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
													.add(cancelButton))
									.add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
											org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.add(org.jdesktop.layout.GroupLayout.TRAILING, advancedPanel,
											org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
											org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.add(basicPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
											org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									/*.add(org.jdesktop.layout.GroupLayout.TRAILING,
											layout.createSequentialGroup()
													.add(titleIconLabel1)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
													.add(titleIconLabel2)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
													.add(titleIconLabel3)
													.add(20, 20, 20)
													.add(titleLabel)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 350,
															Short.MAX_VALUE)
													.add(helpButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
															org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
															org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))*/
									//.add(titleSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 700,
									//		Short.MAX_VALUE)
											).addContainerGap()));
			layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
					layout.createSequentialGroup()
							.addContainerGap()
							/*.add(layout
									.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
									.add(layout
											.createSequentialGroup()
											.add(layout
													.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
													.add(helpButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
															20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
													.add(titleIconLabel1).add(titleIconLabel2).add(titleIconLabel3))
											.add(2, 2, 2))
									.add(layout.createSequentialGroup().add(titleLabel)
											.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))*/
							//.add(titleSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10,
							//		org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(basicPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
									org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(advancedPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
									org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
									org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(layout
									.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
									.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
											.add(cancelButton).add(importButton))
									.add(statusBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
											org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
											org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap()));
			// pack();
		} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
			                                .add(layout.createSequentialGroup().addContainerGap()
			                                           .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
			                                                           layout.createSequentialGroup()
			                                                                 .add(statusBar,
			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                                      Short.MAX_VALUE)
			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                                                 .add(importButton)
			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                                                 .add(cancelButton))
			                                                      .add(previewPanel,
			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                           Short.MAX_VALUE)
			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
			                                                           advancedPanel,
			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                           Short.MAX_VALUE)
			                                                      .add(basicPanel,
			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                           Short.MAX_VALUE)
			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
			                                                           layout.createSequentialGroup()
			                                                                 .add(titleIconLabel1)
			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                                                 .add(titleIconLabel2)
			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                                                 .add(titleIconLabel3)
			                                                                 .add(20, 20, 20)
			                                                                 .add(titleLabel)
			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
			                                                                                  250,
			                                                                                  Short.MAX_VALUE)
			                                                                 .add(helpButton,
			                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
			                                                      .add(titleSeparator,
			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                           300, Short.MAX_VALUE))
			                                           .addContainerGap()));
			layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
			                              .add(layout.createSequentialGroup().addContainerGap()
			                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
			                                                    .add(layout.createSequentialGroup()
			                                                               .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
			                                                                          .add(helpButton,
			                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			                                                                               20,
			                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
			                                                                          .add(titleIconLabel1)
			                                                                          .add(titleIconLabel2)
			                                                                          .add(titleIconLabel3))
			                                                               .add(2, 2, 2))
			                                                    .add(layout.createSequentialGroup()
			                                                               .add(titleLabel)
			                                                               .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
			                                         .add(titleSeparator,
			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			                                              10,
			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                         .add(basicPanel,
			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                         .add(advancedPanel,
			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                         .add(previewPanel,
			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                              Short.MAX_VALUE)
			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
			                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING,
			                                                                         false)
			                                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
			                                                               .add(cancelButton)
			                                                               .add(importButton))
			                                                    .add(statusBar,
			                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
			                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
			                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
			                                         .addContainerGap()));

			annotationAndOntologyImportPanel.setVisible(true);
			attrTypePanel.setVisible(true);
		} else if (dialogType == NETWORK_IMPORT) {

			layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.LEADING).add(
					layout.createSequentialGroup()
							.addContainerGap()
							.add(layout
									.createParallelGroup(GroupLayout.LEADING)
									.add(GroupLayout.TRAILING, previewPanel, GroupLayout.DEFAULT_SIZE,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.add(GroupLayout.TRAILING, advancedPanel, GroupLayout.DEFAULT_SIZE,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.add(GroupLayout.TRAILING, networkImportPanel, GroupLayout.DEFAULT_SIZE,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.add(basicPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)
									.add(GroupLayout.TRAILING,
											layout.createSequentialGroup()
													.add(titleIconLabel1)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
													.add(titleIconLabel2)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
													.add(titleIconLabel3)
													.add(20, 20, 20)
													.add(titleLabel)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 350,
															Short.MAX_VALUE)
													.add(helpButton, GroupLayout.PREFERRED_SIZE,
															GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.add(titleSeparator, GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
									.add(GroupLayout.TRAILING,
											layout.createSequentialGroup().add(importButton)
													.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
													.add(cancelButton))).addContainerGap()));
			layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING).add(
					layout.createSequentialGroup()
							.addContainerGap()
							.add(layout
									.createParallelGroup(GroupLayout.TRAILING)
									.add(layout
											.createSequentialGroup()
											.add(layout
													.createParallelGroup(GroupLayout.BASELINE)
													.add(helpButton, GroupLayout.PREFERRED_SIZE, 20,
															GroupLayout.PREFERRED_SIZE).add(titleIconLabel1)
													.add(titleIconLabel2).add(titleIconLabel3)).add(2, 2, 2))
									.add(layout.createSequentialGroup().add(titleLabel)
											.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
							.add(titleSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(basicPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(networkImportPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(advancedPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(previewPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							.add(layout.createParallelGroup(GroupLayout.BASELINE).add(cancelButton).add(importButton))
							.addContainerGap()));

			// annotationAndOntologyImportPanel.setVisible(false);
			networkImportPanel.setVisible(true);
			attrTypePanel.setVisible(false);
			advancedOptionCheckBox.setVisible(false);
			importAllCheckBox.setVisible(false);
			// advancedOptionPanel.setVisible(false);
		}
	}
	
	
	
	public boolean isFirstRowTitle(){
		return transferNameCheckBox.isSelected();
	}
	public int getStartLineNumber(){
		if (isFirstRowTitle())
			return Integer.parseInt(startRowSpinner.getValue().toString())  ;
		return Integer.parseInt(startRowSpinner.getValue().toString())-1;
	}
	public String getCommentlinePrefix(){
		return commentLineTextField.getText();
	}
	public int getPrimaryKeyColumnIndex(){
		return primaryKeyComboBox.getSelectedIndex();
	}
	public boolean isCaseSansitive(){
		return caseSensitive;
	}
	
	public AttributeMappingParameters getAttributeMappingParameters () throws Exception{
		
		/*
		 * Get import flags
		 */
		final int colCount = previewPanel.getPreviewTable().getColumnModel().getColumnCount();
		importFlag = new boolean[colCount];

		for (int i = 0; i < colCount; i++) {
			importFlag[i] = ((AttributePreviewTableCellRenderer) previewPanel.getPreviewTable()
			                                                                 .getCellRenderer(0, i))
			                .getImportFlag(i);
		}

		
		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = previewPanel.getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

			if (attrNameList.contains(curName)) {
				int dupIndex = 0;

				for (int idx = 0; idx < attrNameList.size(); idx++) {
					if (curName.equals(attrNameList.get(idx))) {
						dupIndex = idx;

						break;
					}
				}

				if (importFlag[i] && importFlag[dupIndex]) {
					final JLabel label = new JLabel("Duplicate Column Name Found: " + curName);
					label.setForeground(Color.RED);
					JOptionPane.showMessageDialog(this, label);

					return null;
				}
			}

			if (curName == null) {
				attrNameList.add("Column " + i);
			} else {
				attrNameList.add(curName.toString());
			}
		}

		attributeNames = attrNameList.toArray(new String[0]);

		final Byte[] test = previewPanel.getDataTypes(previewPanel.getSelectedSheetName());

		final Byte[] attributeTypes = new Byte[test.length];

		for (int i = 0; i < test.length; i++) {
			attributeTypes[i] = test[i];
		}

		
		

		int startLineNumber = getStartLineNumber();
		String commentChar  = null;
		if(!commentLineTextField.getText().isEmpty())
			commentChar = commentLineTextField.getText();
		keyInFile = primaryKeyComboBox.getSelectedIndex();

			// Build mapping parameter object.
		List<String> del = checkDelimiter();
		 AttributeMappingParameters mapping = new AttributeMappingParameters( del,
			 								listDelimiter, keyInFile,
		 									attributeNames, attributeTypes,
		 									listDataTypes, importFlag,
		 									caseSensitive, startLineNumber, commentChar );

		 return mapping;
	}

	public NetworkTableMappingParameters getNetworkTableMappingParameters() throws Exception{
		/*
		 * Get import flags
		 */
		final int colCount = previewPanel.getPreviewTable().getColumnModel().getColumnCount();
		importFlag = new boolean[colCount];

		for (int i = 0; i < colCount; i++) {
			importFlag[i] = ((AttributePreviewTableCellRenderer) previewPanel.getPreviewTable()
			                                                                 .getCellRenderer(0, i))
			                .getImportFlag(i);
		}

		/*
		 * Get Attribute Names
		 */

		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = previewPanel.getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

			if (attrNameList.contains(curName)) {
				int dupIndex = 0;

				for (int idx = 0; idx < attrNameList.size(); idx++) {
					if (curName.equals(attrNameList.get(idx))) {
						dupIndex = idx;

						break;
					}
				}

				if (importFlag[i] && importFlag[dupIndex]) {
					final JLabel label = new JLabel("Duplicate Column Name Found: " + curName);
					label.setForeground(Color.RED);
					JOptionPane.showMessageDialog(this, label);

					return null;
				}
			}

			if (curName == null) {
				attrNameList.add("Column " + i);
			} else {
				attrNameList.add(curName.toString());
			}
		}

		attributeNames = attrNameList.toArray(new String[0]);

		//final byte[] attributeTypes = new byte[previewPanel.getPreviewTable()
		// .getColumnCount()];
		final Byte[] test = previewPanel.getDataTypes(previewPanel.getSelectedSheetName());

		final Byte[] attributeTypes = new Byte[test.length];

		for (int i = 0; i < test.length; i++) {
			attributeTypes[i] = test[i];
		}

		int startLineNumber = getStartLineNumber();
		
		String commentChar = null;
		if (!commentLineTextField.getText().isEmpty())
			commentChar = commentLineTextField.getText();
		keyInFile = primaryKeyComboBox.getSelectedIndex();

		
		final int sourceColumnIndex = networkImportPanel.getSourceIndex();
		final int targetColumnIndex = networkImportPanel.getTargetIndex();

		final String defaultInteraction = defaultInteractionTextField.getText();

		final int interactionColumnIndex = networkImportPanel.getInteractionIndex();
		
			// Build mapping parameter object.
		List<String> del = checkDelimiter();
		 NetworkTableMappingParameters mapping;
			mapping = new NetworkTableMappingParameters (del,
									                    listDelimiter,
									                    attributeNames,
									                    attributeTypes,
									                    listDataTypes,
									                    importFlag,
									                    sourceColumnIndex,
									                    targetColumnIndex,
									                    interactionColumnIndex,
									                    defaultInteraction, startLineNumber, commentChar);
		
		 return mapping;
	}
	
	
	
	// Variables declaration - do not modify
	protected javax.swing.JCheckBox advancedOptionCheckBox;
	protected javax.swing.JCheckBox caseSensitiveCheckBox;
	protected javax.swing.JPanel advancedPanel;

	// protected JTable aliasTable;
	protected javax.swing.JScrollPane aliasScrollPane;
	protected javax.swing.JPanel annotationAndOntologyImportPanel;
	protected javax.swing.JButton arrowButton1;
	protected javax.swing.JButton arrowButton2;
	protected javax.swing.JPanel attr2annotationPanel;
	protected javax.swing.JCheckBox transferNameCheckBox;
	protected javax.swing.ButtonGroup attrTypeButtonGroup;
	protected javax.swing.JLabel attribuiteLabel;
	protected javax.swing.JLabel attributeFileLabel;
	protected javax.swing.JPanel basicPanel;
	protected javax.swing.JButton browseAnnotationButton;
	protected javax.swing.JButton browseOntologyButton;
	protected javax.swing.JButton cancelButton;

	protected javax.swing.JPanel delimiterPanel;
	protected javax.swing.JRadioButton edgeRadioButton;
	protected javax.swing.JButton helpButton;
	protected javax.swing.JButton importButton;
	protected javax.swing.JRadioButton networkRadioButton;
	protected javax.swing.JComboBox mappingAttributeComboBox;
	protected javax.swing.JLabel nodeKeyLabel;
	protected javax.swing.JRadioButton nodeRadioButton;
	protected javax.swing.JPanel ontology2annotationPanel;
	protected javax.swing.JComboBox ontologyComboBox;
	protected javax.swing.JComboBox ontologyInAnnotationComboBox;
	protected javax.swing.JLabel ontologyInAnnotationLabel;
	protected javax.swing.JLabel ontologyLabel;
	protected PreviewTablePanel previewPanel;
	protected javax.swing.JLabel primaryKeyLabel;
	protected javax.swing.JButton selectAttributeFileButton;
	protected javax.swing.JPanel simpleAttributeImportPanel;
	protected javax.swing.JComboBox annotationComboBox;
	protected javax.swing.JLabel sourceLabel;

	protected javax.swing.JTextField targetDataSourceTextField;
	protected javax.swing.JLabel targetOntologyLabel;
	protected javax.swing.JTextField ontologyTextField;
	protected javax.swing.JCheckBox textImportCheckBox;
	protected javax.swing.JPanel textImportOptionPanel;
	protected javax.swing.JLabel titleIconLabel1;
	protected javax.swing.JLabel titleIconLabel2;
	protected javax.swing.JLabel titleIconLabel3;
	protected javax.swing.JLabel titleLabel;
	protected javax.swing.JSeparator titleSeparator;
	protected JComboBox primaryKeyComboBox;
	protected JLabel primaryLabel;
	protected JPanel attrTypePanel;
	protected javax.swing.JRadioButton showAllRadioButton;
	protected javax.swing.JLabel counterLabel;
	protected javax.swing.JRadioButton counterRadioButton;
	protected javax.swing.JButton reloadButton;
	protected javax.swing.JSpinner counterSpinner;
	protected javax.swing.ButtonGroup importTypeButtonGroup;
	protected JPanel attributeNamePanel;
	protected JPanel previewOptionPanel;
	protected JPanel networkImportOptionPanel;
	protected JLabel defaultInteractionLabel;
	protected JTextField defaultInteractionTextField;
	protected JLabel startRowLabel;
	protected JSpinner startRowSpinner;
	protected JLabel commentLineLabel;
	protected JTextField commentLineTextField;

	//Delimiter check boxes
	protected JCheckBox tabCheckBox;
	protected JCheckBox commaCheckBox;
	protected JCheckBox semicolonCheckBox;
	protected JCheckBox spaceCheckBox;
	protected JCheckBox otherCheckBox;
	protected JTextField otherDelimiterTextField;


	// End of variables declaration
	JStatusBar statusBar;
	protected NetworkImportOptionsPanel networkImportPanel;

	// protected DefaultTableModel model;
	protected AliasTableModel aliasTableModel;
	protected JTable aliasTable;
	protected JCheckBox importAllCheckBox;
}
