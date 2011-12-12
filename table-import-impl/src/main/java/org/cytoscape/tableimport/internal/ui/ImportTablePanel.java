/*
 Copyright (c) 2006, 2007, 2009, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.tableimport.internal.ui;


import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.EDGE;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.NETWORK;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.NODE;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.DB_OBJECT_SYMBOL;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.DB_OBJECT_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.GO_ID;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.TAXON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogFontTheme.TITLE_FONT;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.BOOLEAN_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.FLOAT_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.ID_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.INT_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.LIST_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.RIGHT_ARROW_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.SPREADSHEET_ICON_LARGE;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.STRING_ICON;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.reader.DefaultAttributeTableReader;
import org.cytoscape.tableimport.internal.reader.ExcelAttributeSheetReader;
import org.cytoscape.tableimport.internal.reader.ExcelNetworkSheetReader;
import org.cytoscape.tableimport.internal.reader.GraphReader;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.tableimport.internal.reader.NetworkTableReader;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextFileDelimiters;
import org.cytoscape.tableimport.internal.reader.TextTableReader;
import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;
import org.cytoscape.tableimport.internal.util.AttributeTypes;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.util.swing.JStatusBar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.jdesktop.layout.GroupLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

	private static final String[] keyTable = { "Alias?", "Column (Attribute Name)", "Data Type" };
	private static final String ID = CyTableEntry.NAME; 

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

	public ImportTablePanel(final int dialogType, final InputStream is, final String fileType,
	                        final String inputName, final CyProperty<Bookmarks> bookmarksProp,
	                        final BookmarksUtil bkUtil, final TaskManager taskManager,
	                        final InputStreamTaskFactory factory, final CyNetworkManager manager,
	                        final CyTableFactory tableFactory, final CyTableManager tableManager)
	    throws JAXBException, IOException
	{
		this(dialogType, is, fileType, bookmarksProp, bkUtil, taskManager, factory, manager,
		     tableFactory, tableManager);
		this.inputName = inputName;
	}

	public ImportTablePanel(final int dialogType, final InputStream is, final String fileType,
				final CyProperty<Bookmarks> bookmarksProp,
				final BookmarksUtil bkUtil, final TaskManager taskManager,
				final InputStreamTaskFactory factory, final CyNetworkManager manager,
				final CyTableFactory tableFactory, final CyTableManager tableManager)
	    throws JAXBException, IOException
	{
		this.bookmarksProp = null;
		this.bkUtil        = null;
		this.taskManager   = taskManager;
		this.factory       = factory;
		this.manager       = manager;
		this.tableFactory  = tableFactory;
		this.tableManager  = tableManager;

		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			if (bookmarksProp == null)
				throw new NullPointerException("Bookmark Property is null.");
			if (bkUtil == null)
				throw new NullPointerException("Bookmark Utility is null.");

			this.bookmarksProp = bookmarksProp;
			this.bkUtil = bkUtil;

		}
		this.is = is;
		this.fileType = fileType;
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

		setPreviewPanel(null);
		
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
		caseSensitiveCheckBox.setToolTipText("<html><strong><font color=\"red\">Caution! If you uncheck this, import can be extrelely slow.</font></strong></html>");
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
		tabCheckBox = new javax.swing.JCheckBox();
		commaCheckBox = new javax.swing.JCheckBox();
		semicolonCheckBox = new javax.swing.JCheckBox();
		spaceCheckBox = new javax.swing.JCheckBox();
		otherCheckBox = new javax.swing.JCheckBox();
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

		primaryLabel = new JLabel("Primary Key: ");
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

		titleIconLabel2.setIcon(RIGHT_ARROW_ICON.getIcon());

		titleIconLabel3.setIcon(new ImageIcon(getClass().getResource("/images/icon48.png")));

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

		/*
		 * Data Source Panel Layouts.
		 */
		basicPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Sources",
		                                                                  javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
		                                                                  javax.swing.border.TitledBorder.DEFAULT_POSITION,
		                                                                  new java.awt.Font("Dialog",
		                                                                                    1, 11)));

		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT)
		    || (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)) {
			attribuiteLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
			attribuiteLabel.setText("Attributes");

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
			                                        tableFactory, tableManager);
			panelBuilder.buildPanel();
		}

		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
			titleIconLabel1.setIcon(SPREADSHEET_ICON_LARGE.getIcon());

			attributeFileLabel.setText("Input File");
			attributeFileLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
			selectAttributeFileButton.setText("Select File(s)");
			selectAttributeFileButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							setPreviewPanel(evt);
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
					"Annotation File to Attribute Mapping", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11)));
			primaryKeyLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
			primaryKeyLabel.setForeground(new java.awt.Color(51, 51, 255));
			primaryKeyLabel.setText("Key Column in Annotation File");

			nodeKeyLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
			nodeKeyLabel.setForeground(new java.awt.Color(255, 0, 51));
			nodeKeyLabel.setText("Key Attribute for Network");

			mappingAttributeComboBox.setForeground(new java.awt.Color(255, 0, 51));
			mappingAttributeComboBox.setEnabled(false);
			mappingAttributeComboBox.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						nodeKeyComboBoxActionPerformed(evt);
					}
				});

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
		tabCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					try {
						delimiterCheckBoxActionPerformed(evt);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		commaCheckBox.setText("Comma");
		commaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		commaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		commaCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					try {
						delimiterCheckBoxActionPerformed(evt);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		semicolonCheckBox.setText("Semicolon");
		semicolonCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		semicolonCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		semicolonCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					try {
						delimiterCheckBoxActionPerformed(evt);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		spaceCheckBox.setText("Space");
		spaceCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		spaceCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		spaceCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					try {
						delimiterCheckBoxActionPerformed(evt);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		otherCheckBox.setText("Other");
		otherCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		otherCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		otherCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					try {
						delimiterCheckBoxActionPerformed(evt);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		otherDelimiterTextField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent evt) {
					try {
						otherTextFieldActionPerformed(evt);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				public void keyReleased(KeyEvent arg0) {
				}

				public void keyTyped(KeyEvent evt) {
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
		counterSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
				public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
					counterSpinnerMouseWheelMoved(evt);
				}
			});
		counterSpinner.setToolTipText("<html><body>Click <strong text=\"red\"><i>Refresh Preview</i></strong> button to update the table.</body></html>");

		counterLabel.setText("entries.");

		previewOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview Options"));
		reloadButton.setText("Refresh Preview");
		reloadButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		reloadButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					try {
						reloadButtonActionPerformed(evt);
					} catch (IOException e) {
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

		attributeNamePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Attribute Names"));

		transferNameCheckBox.setText("Transfer first line as attribute names");

		transferNameCheckBox.setBorder(null);
		transferNameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		transferNameCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					transferNameCheckBoxActionPerformed(evt);
				}
			});

		startRowLabel.setText("Start Import Row: ");
		startRowLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		startRowSpinner.setName("startRowSpinner");

		SpinnerNumberModel startRowSpinnerModel = new SpinnerNumberModel(1, 1, 10000000, 1);
		startRowSpinner.setModel(startRowSpinnerModel);
		startRowSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
				public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
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
		// Quick help should be implemented!
	}

	private void otherTextFieldActionPerformed(KeyEvent evt) throws IOException {
		if (otherCheckBox.isSelected()) {
			displayPreview();
		}
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
			logger.info("\nNote: ImportTextTableFDialog.attributeRadioButtonActionPerformed():Import network attribute not implemented yet!\n");
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

		JDialog dlg = (JDialog)this.getParent().getParent().getParent().getParent().getParent().getParent();
		dlg.pack();

		//pack();
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



	private void transferNameCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
		final DefaultTableModel model = (DefaultTableModel) previewPanel.getPreviewTable().getModel();

		if (transferNameCheckBox.isSelected()) {
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

			startRowSpinner.setEnabled(false);
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
			startRowSpinner.setEnabled(true);
		}

		updateAliasTable();
		updatePrimaryKeyComboBox();
		repaint();
	}


	/**
	 * Load from the data source.<br>
	 *
	 * @param evt
	 * @throws Exception
	 */
	public void importTable() throws Exception {
		if (checkDataSourceError() == false)
			return;

		boolean importAll = importAllCheckBox.isSelected();

		/*
		 * Get start line number. If "transfer" check box is true, then start
		 * reading from the second line.
		 */
		int startLineNumber;
		final int spinnerNumber = Integer.parseInt(startRowSpinner.getValue().toString());

		if (transferNameCheckBox.isSelected()) {
			startLineNumber = spinnerNumber;
		} else {
			startLineNumber = spinnerNumber - 1;
		}

		final String commentChar = commentLineTextField.getText();

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
					final JLabel label = new JLabel("Duplicate Attribute Name Found: " + curName);
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

		//final byte[] attributeTypes = new byte[previewPanel.getPreviewTable()
		// .getColumnCount()];
		final Byte[] test = previewPanel.getDataTypes(previewPanel.getSelectedSheetName());

		final Byte[] attributeTypes = new Byte[test.length];

		for (int i = 0; i < test.length; i++) {
			attributeTypes[i] = test[i];
		}

		// for (int i = 0; i < attributeTypes.length; i++) {
		//	 attributeTypes[i] = attributeDataTypes.get(i);
		// }
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

		ObjectType objType = null;

		if (dialogType != NETWORK_IMPORT) {
			if (nodeRadioButton.isSelected()) {
				objType = NODE;
			} else if (edgeRadioButton.isSelected()) {
				objType = EDGE;
			} else {
				objType = NETWORK;
			}
		}

		switch (dialogType) {
			case SIMPLE_ATTRIBUTE_IMPORT:
				// Case 1: Attribute table import.

				// Extract URL from the text table.
				//final URL source = new URL(targetDataSourceTextField.getText());

				// Make sure primary key index is up-to-date.
				keyInFile = primaryKeyComboBox.getSelectedIndex();

				// Build mapping parameter object.
				final AttributeMappingParameters mapping;
				final List<String> del;

				del = checkDelimiter();

				//TODO -- Need to fix problem in AttributeMappingParameters
				mapping = new AttributeMappingParameters(objType, del,
									 listDelimiter, keyInFile,
									 mappingAttribute, aliasList,
									 attributeNames, attributeTypes,
									 listDataTypes, importFlag,
									 caseSensitive);

				if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
				    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension()))
				{
					if (workbook == null)
						throw new NullPointerException("Wookbook object is null!");

					// Load all sheets in the table
					for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
						final Sheet sheet = workbook.getSheetAt(i);

						loadAnnotation(new ExcelAttributeSheetReader(sheet, mapping,
						                                             startLineNumber,
											     importAll),
							       null);
						//source.toString());
					}
				} else {
					loadAnnotation(new DefaultAttributeTableReader(null, mapping,
										       startLineNumber,
										       null, importAll, this.is),
						       null);
				}
				break;

			case ONTOLOGY_AND_ANNOTATION_IMPORT:
				panelBuilder.importOntologyAndAnnotation();
				break;

			case NETWORK_IMPORT:
				/*
				 * Case 3: read as network table (Network + Edge Attributes)
				 */

				// Extract URL from the text table.
				/*
				 * Now multiple files are supported.
				 */
				//URL[] sources = new URL[inputFiles.length];

				//for (int i = 0; i < sources.length; i++) {
				//	sources[i] = inputFiles[i].toURI().toURL();
				//}

				//final URL networkSource = new URL(targetDataSourceTextField.getText());

				final int sourceColumnIndex = networkImportPanel.getSourceIndex();
				final int targetColumnIndex = networkImportPanel.getTargetIndex();

				final String defaultInteraction = defaultInteractionTextField.getText();

				final int interactionColumnIndex = networkImportPanel.getInteractionIndex();

				final NetworkTableMappingParameters nmp = new NetworkTableMappingParameters(checkDelimiter(),
				                                                                            listDelimiter,
				                                                                            attributeNames,
				                                                                            attributeTypes,
				                                                                            null,
				                                                                            importFlag,
				                                                                            sourceColumnIndex,
				                                                                            targetColumnIndex,
				                                                                            interactionColumnIndex,
				                                                                            defaultInteraction);
				NetworkTableReader reader;
				String networkName;

				if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
				    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension()))
				{
					Sheet sheet = workbook.getSheetAt(0);
					networkName = workbook.getSheetName(0);
					
					reader = new ExcelNetworkSheetReader(networkName, sheet, nmp,
						                                     startLineNumber);
				} else {
					// Get name from URL.
					if ((commentChar != null) && (commentChar.length() != 0)
					    && transferNameCheckBox.isSelected())
						startLineNumber++;

					networkName = this.inputName;
					reader = new NetworkTableReader(networkName, this.is, nmp,
									startLineNumber, commentChar);
				}
				loadNetwork(reader);
				break;

			default:
				return;
		}
	}

	private void setPreviewPanel(ActionEvent evt) throws IOException {

		/*
		final File[] multiSource = CytoscapeServices.fileUtil.getFiles(this,
				"Select local file",
				CytoscapeServices.fileUtil.LOAD);

		if ((multiSource == null) || (multiSource[0] == null))
			return;

		// Pick the first one and show preview.
		this.inputFiles = multiSource;

		final File sourceFile = multiSource[0];

		targetDataSourceTextField.setText(sourceFile.toURI().toURL().toString());

		// Set tooltip as HTML
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body><strong text=\"red\">File(s) to be imported</strong><ul>");

		for (int i = 0; i < multiSource.length; i++) {
			builder.append("<li>" + multiSource[i].getName() + "</li>");
		}

		builder.append("</ul></body></html>");
		targetDataSourceTextField.setToolTipText(builder.toString());

		final URL sourceURL = new URL(targetDataSourceTextField.getText());
		*/

		readAnnotationForPreview(null, checkDelimiter());
		transferNameCheckBox.setEnabled(true);
		transferNameCheckBox.setSelected(false);

		if (previewPanel.getPreviewTable() == null) {
//			JLabel label = new JLabel("File is broken or empty!");
//			label.setForeground(Color.RED);
//			JOptionPane.showMessageDialog(this, label);
			return;
		} else {
			columnHeaders = new String[previewPanel.getPreviewTable().getColumnCount()];
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

		//((JDialog)this.getParent().getParent().getParent().getParent().getParent().getParent()).pack();
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

		readAnnotationForPreview(null, checkDelimiter());
		previewPanel.repaint();
	}

	private void updateComponents() throws JAXBException, IOException {

		if (dialogType == SIMPLE_ATTRIBUTE_IMPORT) {
			//setTitle("Import Annotation File");
			titleLabel.setText("Import Attribute from Table");
			annotationAndOntologyImportPanel.setVisible(false);
			importAllCheckBox.setVisible(true);
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
			titleLabel.setText("Import Network from Table");
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




	/**
	 * Display preview table
	 *
	 * @param sourceURL
	 * @param delimiters
	 * @throws IOException
	 */
	protected void readAnnotationForPreview(URL sourceURL, List<String> delimiters) throws IOException {

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
		final int startLine = Integer.parseInt(startRowSpinner.getValue().toString());

		// Load Spreadsheet data for preview.
		if(fileType != null && (fileType.equalsIgnoreCase(
				SupportedFileType.EXCEL.getExtension())
				|| fileType.equalsIgnoreCase(
						SupportedFileType.OOXML.getExtension())) && workbook == null) {
			try {
				workbook = WorkbookFactory.create(is);
			} catch (InvalidFormatException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?");
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}

		boolean is_isClosed = false;
		try {
			this.is.available();
		}
		catch (Exception e){
			is_isClosed = true;
		}

		// Mark the inputStream before preview
		if (!is_isClosed){
			this.is.mark(Integer.MAX_VALUE);
		}


		previewPanel.setPreviewTable(workbook, this.is, this.fileType, sourceURL, delimiters, null, previewSize,
				commentChar, startLine - 1);

		// Reset the inputStream to the mark after preview
		if (!is_isClosed){
				this.is.reset();
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
			FileTypes type = checkFileType(sourceURL);

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
					importAllCheckBox.setEnabled(true);
				} else {
					importAllCheckBox.setEnabled(true);
				}
			}

			attributeRadioButtonActionPerformed(null);
			/*
			 * Set Status bar
			 */
			//setStatusBar(sourceURL);
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

		importAllCheckBox.setEnabled(true);
	}

	private void switchDelimiterCheckBoxes(Boolean state) {
		tabCheckBox.setEnabled(state);
		commaCheckBox.setEnabled(state);
		spaceCheckBox.setEnabled(state);
		semicolonCheckBox.setEnabled(state);
		otherCheckBox.setEnabled(state);
		otherDelimiterTextField.setEnabled(state);
	}

	private FileTypes checkFileType(URL source) {
		//String[] parts = source.toString().split("/");

		//final String fileName = parts[parts.length - 1];

		//if (fileName.startsWith("gene_association")
		//    && (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)) {
		//	return FileTypes.GENE_ASSOCIATION_FILE;
		//} else
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

			try {
				BufferedInputStream fis = (BufferedInputStream) sourceURL.openStream();
				fileSize = fis.available();
			} catch (IOException e) {
				e.printStackTrace();
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
			} else {
				//it = Cytoscape.getNetworkSet().iterator();

				//while (it.hasNext()) {
				//	valueSet.add(((CyNetwork) it.next()).getTitle());
				//}
			}
		} else {

			//TODO
			/*
			final byte attrType = selectedAttributes.getType(selectedKeyAttribute);

			Object value = null;

			Iterator attrIt = selectedAttributes.getMultiHashMap()
			                                    .getObjectKeys(selectedKeyAttribute);

			String stringValue = null;
			Double dblValue = 0.;
			Integer intValue = 0;
			Boolean boolValue = false;
			List listValue = null;


			while (attrIt.hasNext()) {
				value = attrIt.next();


				switch (attrType) {
					case AttributeTypes.TYPE_STRING:
						//stringValue = selectedAttributes.getStringAttribute((String) value,
						//                                                    selectedKeyAttribute);
						stringValue = selectedAttributes.getColumnValues(selectedKeyAttribute, String.class);

						valueSet.add(stringValue);

						break;

					case AttributeTypes.TYPE_FLOATING:
						dblValue = selectedAttributes.getDoubleAttribute((String) value,
						                                                 selectedKeyAttribute);
						valueSet.add(dblValue);

						break;

					case AttributeTypes.TYPE_INTEGER:
						intValue = selectedAttributes.getIntegerAttribute((String) value,
						                                                  selectedKeyAttribute);
						valueSet.add(intValue);

						break;

					case AttributeTypes.TYPE_BOOLEAN:
						boolValue = selectedAttributes.getBooleanAttribute((String) value,
						                                                   selectedKeyAttribute);
						valueSet.add(boolValue);

						break;

					case AttributeTypes.TYPE_SIMPLE_LIST:
						listValue = selectedAttributes.getListAttribute((String) value,
						                                                selectedKeyAttribute);
						valueSet.addAll(listValue);

						break;

					default:
						break;
				}

			}
			*/
		}


		previewPanel.setKeyAttributeList(valueSet);

		//nodeKeyList.setListData(valueSet.toArray());

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

	private Task loadTask = null;

	public Task getLoadTask(){
		return loadTask;
	}

	private void loadAnnotation(TextTableReader reader, String source) {
		final ImportAttributeTableTask task = new ImportAttributeTableTask(reader, tableManager);
		this.loadTask = task;
	}

	private void loadNetwork(final GraphReader reader) {
		ImportNetworkTask task = new ImportNetworkTask(reader);
		this.loadTask = task;
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

	protected List<String> checkDelimiter() {
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

		if (otherCheckBox.isSelected()) {
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
		/*
		 * Number of ENABLED columns should be 2 or more.
		 *
		 */
		final JTable table = previewPanel.getPreviewTable();

		if ((table == null) || (table.getModel() == null) || (table.getColumnCount() == 0)) {
			JOptionPane.showMessageDialog(this, "No table selected.", "Invalid Table!",
			                              JOptionPane.INFORMATION_MESSAGE);

			return false;
		} else if ((table.getColumnCount() < 2) && (dialogType != NETWORK_IMPORT)) {
			JOptionPane.showMessageDialog(this, "Table should contain at least 2 columns.",
			                              "Invalid Table!", JOptionPane.INFORMATION_MESSAGE);

			return false;
		}

		if (dialogType == NETWORK_IMPORT) {
			final int sIdx = networkImportPanel.getSourceIndex();
			final int tIdx = networkImportPanel.getTargetIndex();
			final int iIdx = networkImportPanel.getInteractionIndex();

			//			if ((sIdx == -1) || (tIdx == -1)) {
			//				JOptionPane.showMessageDialog(this, "Source or Target index not selected.",
			//				                              "Please select both source & target index.",
			//				                              JOptionPane.INFORMATION_MESSAGE);
			//
			//				return false;
			//			}
			if ((sIdx == tIdx) || (((iIdx == sIdx) || (iIdx == tIdx)) && (iIdx != -1))) {
				JOptionPane.showMessageDialog(this,
				                              "Columns for source, target, and interaction type must be distinct.",
				                              "Same column index!", JOptionPane.INFORMATION_MESSAGE);

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
									.add(org.jdesktop.layout.GroupLayout.TRAILING,
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
															org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
									.add(titleSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 700,
											Short.MAX_VALUE)).addContainerGap()));
			layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
					layout.createSequentialGroup()
							.addContainerGap()
							.add(layout
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
											.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
							.add(titleSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10,
									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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

			// advancedOptionPanel.setVisible(false);
		}
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
	protected javax.swing.JCheckBox commaCheckBox;
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
	protected javax.swing.JTextField otherDelimiterTextField;
	protected javax.swing.JCheckBox otherCheckBox;
	protected PreviewTablePanel previewPanel;
	protected javax.swing.JLabel primaryKeyLabel;
	protected javax.swing.JButton selectAttributeFileButton;
	protected javax.swing.JCheckBox semicolonCheckBox;
	protected javax.swing.JPanel simpleAttributeImportPanel;
	protected javax.swing.JComboBox annotationComboBox;
	protected javax.swing.JLabel sourceLabel;
	protected javax.swing.JCheckBox spaceCheckBox;
	protected javax.swing.JCheckBox tabCheckBox;
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

	// End of variables declaration
	JStatusBar statusBar;
	protected NetworkImportOptionsPanel networkImportPanel;

	// protected DefaultTableModel model;
	protected AliasTableModel aliasTableModel;
	protected JTable aliasTable;
	protected JCheckBox importAllCheckBox;
}
