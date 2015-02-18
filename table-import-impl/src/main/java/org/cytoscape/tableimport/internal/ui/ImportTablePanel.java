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
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.EDGE;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.NETWORK;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.NODE;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.DB_OBJECT_SYMBOL;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.DB_OBJECT_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.GO_ID;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.TAXON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.BOOLEAN_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.FLOAT_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.ID_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.INT_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.LIST_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.STRING_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.ALIAS;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.ONTOLOGY;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.PRIMARY_KEY;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
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
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.JStatusBar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskManager;
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

	private static final String[] keyTable = { "Alias", "Key", "Column Name", "Data Type" };
	private static final String ID = CyNetwork.NAME;

	private JDialog advancedDialog;
	
	protected JCheckBox caseSensitiveCheckBox;
	
	// protected JTable aliasTable;
	protected JScrollPane aliasScrollPane;
	protected JCheckBox transferNameCheckBox;
	protected ButtonGroup attrTypeButtonGroup;
	protected JLabel attributeFileLabel;
	protected JButton browseAnnotationButton;
	protected JButton browseOntologyButton;

	protected JRadioButton edgeRadioButton;
	protected JRadioButton networkRadioButton;
	protected JComboBox mappingAttributeComboBox;
	protected JRadioButton nodeRadioButton;
	protected JComboBox ontologyComboBox;
	protected JComboBox ontologyInAnnotationComboBox;
	protected JButton selectAttributeFileButton;
	protected JComboBox annotationComboBox;

	protected JTextField targetDataSourceTextField;
	protected JLabel targetOntologyLabel;
	protected JTextField ontologyTextField;
	
	private JPanel basicPanel;
	private JPanel dataSourcesPanel;
	private NetworkImportOptionsPanel networkImportPanel;
	private JPanel simpleAttributeImportPanel;
	private JPanel textImportOptionPanel;
	private JPanel annotationTblMappingPanel;
	private JPanel annotationOntologyMappingPanel;
	private PreviewTablePanel previewPanel;
	
	private JButton advancedButton;
	
	protected JComboBox primaryKeyComboBox;
	protected JTextField defaultInteractionTextField;
	protected JSpinner startRowSpinner;
	protected JTextField commentLineTextField;

	// Delimiter check boxes
	protected JCheckBox tabCheckBox;
	protected JCheckBox commaCheckBox;
	protected JCheckBox semicolonCheckBox;
	protected JCheckBox spaceCheckBox;
	protected JCheckBox otherCheckBox;
	protected JTextField otherDelimiterTextField;

	JStatusBar statusBar;
	

	// protected DefaultTableModel model;
	protected AliasTableModel aliasTableModel;
	protected JTable aliasTable;
	protected JCheckBox importAllCheckBox;
	
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

	private CyNetwork network;

	private final String fileType;

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
	private final IconManager iconManager;

	public ImportTablePanel(final int dialogType, final InputStream is, final String fileType, final String inputName,
			final CyProperty<Bookmarks> bookmarksProp, final BookmarksUtil bkUtil, final TaskManager taskManager,
			final InputStreamTaskFactory factory, final CyNetworkManager manager, final CyTableFactory tableFactory,
			final CyTableManager tableManager, final FileUtil fileUtil, final IconManager iconManager)
					throws JAXBException, IOException {
		this(dialogType, is, fileType, bookmarksProp, bkUtil, taskManager, factory, manager, tableFactory,
				tableManager, fileUtil, iconManager);
	}

	public ImportTablePanel(final int dialogType, final InputStream is, final String fileType,
			final CyProperty<Bookmarks> bookmarksProp, final BookmarksUtil bkUtil, final TaskManager taskManager,
			final InputStreamTaskFactory factory, final CyNetworkManager manager, final CyTableFactory tableFactory,
			final CyTableManager tableManager, final FileUtil fileUtil, final IconManager iconManager)
					throws JAXBException, IOException {
		this.bookmarksProp = null;
		this.bkUtil = null;
		this.taskManager = taskManager;
		this.factory = factory;
		this.manager = manager;
		this.tableFactory = tableFactory;
		this.tableManager = tableManager;
		this.fileUtil = fileUtil;
		this.fileType = fileType;
		this.iconManager = iconManager;

		if (dialogType != ONTOLOGY_AND_ANNOTATION_IMPORT) {
			// Before, this.fileType was always null.
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
		} else if (is == null) {
			if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
				if (bookmarksProp == null)
					throw new NullPointerException("Bookmark Property is null.");
				if (bkUtil == null)
					throw new NullPointerException("Bookmark Utility is null.");

				this.bookmarksProp = bookmarksProp;
				this.bkUtil = bkUtil;
			}
		}
		
		selectedAttributes = null;

		network = CytoscapeServices.cyApplicationManager.getCurrentNetwork();
		if (network != null) {
			selectedAttributes = network.getDefaultNodeTable();
		}
		
		this.objType = NODE;
		this.dialogType = dialogType;
		this.listDelimiter = PIPE.toString();

		this.aliasTableModelMap = new HashMap<>();
		this.aliasTableMap = new HashMap<>();
		this.primaryKeyMap = new HashMap<>();

		annotationUrlMap = new HashMap<>();
		annotationFormatMap = new HashMap<>();
		annotationAttributesMap = new HashMap<>();

		ontologyUrlMap = new HashMap<>();
		ontologyDescriptionMap = new HashMap<>();
		ontologyTypeMap = new HashMap<>();

		attributeDataTypes = new ArrayList<Byte>();

		initComponents();

		updateComponents();

		getPreviewPanel().addPropertyChangeListener(this);

		// Hide input file and use inputStream
		this.attributeFileLabel.setVisible(false);
		this.selectAttributeFileButton.setVisible(false);
		this.targetDataSourceTextField.setVisible(false);

		// Case import network
		if (this.dialogType == NETWORK_IMPORT) {
			this.edgeRadioButton.setVisible(false);
			this.nodeRadioButton.setVisible(false);
		}

		// Case import node/edge attribute
		if (this.dialogType == SIMPLE_ATTRIBUTE_IMPORT)
			this.networkRadioButton.setVisible(false);

		boolean useFirstRow = dialogType == NETWORK_IMPORT || dialogType == SIMPLE_ATTRIBUTE_IMPORT;
		
		try {
			setPreviewPanel(useFirstRow);
		} catch (Exception e) {
			logger.error("Failed to create preview.  (Invalid input file)", e);
			throw new IOException("Fialed to create preview.", e);
		}
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

	/**
	 * Listening to local signals used among Swing components in this dialog.
	 */
	@Override
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

				for (Byte type : getPreviewPanel().getCurrentDataTypes()) {
					attributeDataTypes.add(type);
				}
			}

			attributeDataTypes.set(key, newType);

			if (dialogType != NETWORK_IMPORT) {
				final JTable curTable = aliasTableMap.get(getPreviewPanel().getSelectedSheetName());
				curTable.setDefaultRenderer(Object.class,
						new AliasTableRenderer(attributeDataTypes, primaryKeyComboBox.getSelectedIndex(), iconManager));
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
			final int columnCount = getPreviewPanel().getPreviewTable().getColumnCount();
			aliasTableModelMap.put(getPreviewPanel().getSelectedSheetName(), new AliasTableModel(keyTable, columnCount));

			initializeAliasTable(columnCount, null);
			updatePrimaryKeyComboBox();
		} else if (evt.getPropertyName().equals(NETWORK_IMPORT_TEMPLATE_CHANGED)) {
			/*
			 * This is a signal from network import options panel.
			 */
			List<Integer> columnIdx = (List<Integer>) evt.getNewValue();

			final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) getPreviewPanel()
					.getPreviewTable().getCellRenderer(0, 0);
			rend.setSourceIndex(columnIdx.get(0));
			rend.setTargetIndex(columnIdx.get(1));
			rend.setInteractionIndex(columnIdx.get(2));

			getPreviewPanel().getPreviewTable().getTableHeader().resizeAndRepaint();
			getPreviewPanel().getPreviewTable().repaint();

			getPreviewPanel().repaint();
		}
	}

	private void initComponents() {
		statusBar = new JStatusBar();

		importAllCheckBox = new JCheckBox("Import everything (Key is always ID)");
		importAllCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				importAllCheckBoxActionPerformed(evt);
			}
		});

		caseSensitiveCheckBox = new JCheckBox("Case Sensitive");
		caseSensitiveCheckBox
				.setToolTipText("<html><font color=\"red\"><strong>Caution!</strong> If you uncheck this, import can be extremely slow.</font></html>");
		caseSensitiveCheckBox.setSelected(true);
		caseSensitiveCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ignoreCaseCheckBoxActionPerformed(evt);
			}

			private void ignoreCaseCheckBoxActionPerformed(ActionEvent evt) {
				caseSensitive = caseSensitiveCheckBox.isSelected();
			}
		});

		attrTypeButtonGroup = new ButtonGroup();
		nodeRadioButton = new JRadioButton("Node");
		edgeRadioButton = new JRadioButton("Edge");
		networkRadioButton = new JRadioButton("Network");
		ontologyComboBox = new JComboBox();
		browseOntologyButton = new JButton();
		annotationComboBox = new JComboBox();
		browseAnnotationButton = new JButton();

		targetDataSourceTextField = new JTextField();
		selectAttributeFileButton = new JButton();
		mappingAttributeComboBox = new JComboBox();
		aliasScrollPane = new JScrollPane();
		targetOntologyLabel = new JLabel("Ontology:");
		ontologyTextField = new JTextField();
		ontologyInAnnotationComboBox = new JComboBox();
		
		tabCheckBox = new JCheckBox();
		commaCheckBox = new JCheckBox();
		semicolonCheckBox = new JCheckBox();
		spaceCheckBox = new JCheckBox();
		otherCheckBox = new JCheckBox();
		otherDelimiterTextField = new JTextField();
		transferNameCheckBox = new JCheckBox("Transfer first line as column names");

		defaultInteractionTextField = new JTextField();

		attributeFileLabel = new JLabel();

		startRowSpinner = new JSpinner();

		commentLineTextField = new JTextField();
		commentLineTextField.setName("commentLineTextField");

		primaryKeyComboBox = new JComboBox();
		primaryKeyComboBox.setEnabled(false);
		primaryKeyComboBox.addActionListener(new ActionListener() {
			@Override
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

		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			/*
			 * Data Source Panel Layouts.
			 */
			nodeRadioButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			nodeRadioButton.setMargin(new Insets(0, 0, 0, 0));
			nodeRadioButton.setSelected(true);
			nodeRadioButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					attributeRadioButtonActionPerformed(evt);
				}
			});

			edgeRadioButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			edgeRadioButton.setMargin(new Insets(0, 0, 0, 0));
			edgeRadioButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					attributeRadioButtonActionPerformed(evt);
				}
			});

			networkRadioButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			networkRadioButton.setMargin(new Insets(0, 0, 0, 0));
			networkRadioButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					attributeRadioButtonActionPerformed(evt);
				}
			});
		}

		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			panelBuilder = new OntologyPanelBuilder(this, bookmarksProp, bkUtil, taskManager, factory, manager,
					tableFactory, tableManager, fileUtil);
			panelBuilder.buildPanel();
		}

		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
			// titleIconLabel.setIcon(SPREADSHEET_ICON_LARGE.getIcon());
			attributeFileLabel.setText("Input File");
			
			selectAttributeFileButton.setText("Select File(s)");
			selectAttributeFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						setPreviewPanel(false);
					} catch (Exception e) {
						JOptionPane
								.showMessageDialog(
										ImportTablePanel.this,
										"<html>Could not read selected file.<p>See <b>Help->Error Dialog</b> for further details.</html>",
										"ERROR", JOptionPane.ERROR_MESSAGE);
						logger.warn("Could not read selected file.", e);
					}
				}
			});
		}

		/*
		 * Layout data for advanced panel
		 */
		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)) {
			if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
				mappingAttributeComboBox.setEnabled(false);
				mappingAttributeComboBox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						nodeKeyComboBoxActionPerformed(evt);
					}
				});
			}
		}

		tabCheckBox.setText("<html><b><font size=+1>\u21b9 <font></b><font size=-2>(tab)</font><html>");
		tabCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		commaCheckBox.setText("<html><b><font size=+1>, <font></b><font size=-2>(comma)</font><html>");
		commaCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		semicolonCheckBox.setText("<html><b><font size=+1>; <font></b><font size=-2>(semicolon)</font><html>");
		semicolonCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		spaceCheckBox.setText("<html><b><font size=+1>\u2423 <font></b><font size=-2>(space)</font><html>");
		spaceCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		otherCheckBox.setText("Other:");
		otherCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					otherDelimiterTextField.requestFocus();
					delimiterCheckBoxActionPerformed(evt);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// TODO: VetoableChangeListener???

		otherDelimiterTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent evt) {

			}
			@Override
			public void keyReleased(KeyEvent evt) {
				try {
					if (otherCheckBox.isSelected())
						displayPreview();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void keyTyped(KeyEvent evt) {
			}
		});

		getPreviewPanel().getReloadButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					displayPreview();

					if (transferNameCheckBox.isSelected())
						transferNameCheckBoxActionPerformed(null);
				} catch (IOException e) {
					e.printStackTrace();
					throw new IllegalStateException("Could not reload target file.");
				}
			}
		});
		
		transferNameCheckBox.setEnabled(false);
		transferNameCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				transferNameCheckBoxActionPerformed(evt);
			}
		});

		startRowSpinner.setName("startRowSpinner");

		final SpinnerNumberModel startRowSpinnerModel = new SpinnerNumberModel(1, 1, 10000000, 1);
		startRowSpinner.setModel(startRowSpinnerModel);
		startRowSpinner.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent evt) {
				startRowSpinnerMouseWheelMoved(evt);
			}
		});
		startRowSpinner.setToolTipText("<html>Load entries from this line. <p>"
				+ "(Click on the <strong><i>Refresh Preview</i></strong> button to refresh preview.)</p></html>");

		commentLineTextField.setToolTipText("<html>Lines start with this string will be ignored. <br>"
				+ "(Click on the <strong><i>Refresh Preview</i></strong> button to refresh preview.)</html>");

		defaultInteractionTextField.setText(DEFAULT_INTERACTION);
		defaultInteractionTextField.setToolTipText("<html>If <font color=\"red\"><i>Default Interaction</i></font>"
				+ " is selected, this value will be used for <i>Interaction Type</i>.<br></html>");

		globalLayout();

		if (basicPanel != null)
			basicPanel.repaint();
	}
	
	private JPanel getBasicPanel() {
		if (basicPanel == null) {
			basicPanel = new JPanel();
			
			final GroupLayout layout = new GroupLayout(basicPanel);
			basicPanel.setLayout(layout);
			layout.setAutoCreateGaps(true);
	
			final ParallelGroup hGroup = layout.createParallelGroup(CENTER);
			final SequentialGroup vGroup = layout.createSequentialGroup();
			
			layout.setHorizontalGroup(hGroup);
			layout.setVerticalGroup(vGroup);
			
			if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
				hGroup.addComponent(getDataSourcesPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(getDataSourcesPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			} else if (dialogType == SIMPLE_ATTRIBUTE_IMPORT || dialogType == NETWORK_IMPORT) {
				hGroup.addComponent(getSimpleAttributeImportPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(getSimpleAttributeImportPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
				
				if (dialogType == NETWORK_IMPORT) {
					hGroup.addComponent(getNetworkImportPanel(), TRAILING, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
					vGroup.addComponent(getNetworkImportPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
				}
			}
			
			hGroup.addComponent(getAdvancedButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			vGroup.addComponent(getAdvancedButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		}
		
		return basicPanel;
	}
	
	private JPanel getDataSourcesPanel() {
		if (dataSourcesPanel == null) {
			dataSourcesPanel = new JPanel();
			dataSourcesPanel.setBorder(LookAndFeelUtil.createTitledBorder("Data Sources"));
			
			final JLabel dataTypeLabel = new JLabel("Data Type:");
			final JLabel sourceLabel = new JLabel("Annotation:");
			final JLabel ontologyLabel = new JLabel("Ontology:");
			
			final GroupLayout layout = new GroupLayout(dataSourcesPanel);
			dataSourcesPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(TRAILING)
							.addComponent(dataTypeLabel)
							.addComponent(sourceLabel)
							.addComponent(ontologyLabel)
					)
					.addGroup(layout.createParallelGroup(LEADING)
							.addGroup(layout.createSequentialGroup()
									.addComponent(nodeRadioButton)
									.addComponent(edgeRadioButton)
									.addComponent(networkRadioButton)
							)
							.addComponent(annotationComboBox, 0, 100, Short.MAX_VALUE)
							.addComponent(ontologyComboBox, 0, 100, Short.MAX_VALUE)
					)
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(browseAnnotationButton)
							.addComponent(browseOntologyButton)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER)
							.addComponent(dataTypeLabel)
							.addComponent(nodeRadioButton)
							.addComponent(edgeRadioButton)
							.addComponent(networkRadioButton)
					)
					.addGroup(layout.createParallelGroup(CENTER)
							.addComponent(sourceLabel)
							.addComponent(annotationComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(browseAnnotationButton)
					)
					.addGroup(layout.createParallelGroup(CENTER)
							.addComponent(ontologyLabel)
							.addComponent(ontologyComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(browseOntologyButton)
					)
			);
		}
		
		return dataSourcesPanel;
	}
	
	private JPanel getSimpleAttributeImportPanel() {
		if (simpleAttributeImportPanel == null) {
			simpleAttributeImportPanel = new JPanel();
			
			final GroupLayout layout = new GroupLayout(simpleAttributeImportPanel);
			simpleAttributeImportPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addGroup(layout.createSequentialGroup()
							.addComponent(attributeFileLabel)
							.addComponent(targetDataSourceTextField, DEFAULT_SIZE, 300, Short.MAX_VALUE)
							.addComponent(selectAttributeFileButton)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(LEADING)
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(selectAttributeFileButton)
							.addComponent(targetDataSourceTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(attributeFileLabel)
					)
			);
		}
		
		return simpleAttributeImportPanel;
	}
	
	private NetworkImportOptionsPanel getNetworkImportPanel() {
		if (networkImportPanel == null) {
			networkImportPanel = new NetworkImportOptionsPanel(iconManager);
			networkImportPanel.addPropertyChangeListener(this);
		}
		
		return networkImportPanel;
	}
	
	private JPanel getTextImportOptionPanel() {
		if (textImportOptionPanel == null) {
			textImportOptionPanel = new JPanel();
			textImportOptionPanel.setBorder(LookAndFeelUtil.createTitledBorder("Text File Import Options"));
			
			final JLabel delimiterLabel = new JLabel("Delimiter:");
			delimiterLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			final JLabel startRowLabel = new JLabel("Start Import Row:");
			startRowLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			final JLabel commentLineLabel = new JLabel("Ignore lines starting with:");
			commentLineLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			final JLabel defaultInteractionLabel = new JLabel("Default Interaction:");
			defaultInteractionLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			final JSeparator sep1 = new JSeparator();
			final JSeparator sep2 = new JSeparator();
			
			final GroupLayout layout = new GroupLayout(textImportOptionPanel);
			textImportOptionPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			// Get the width of the largest left and right components
			final int lw = commentLineLabel.getPreferredSize().width; // to align all left-side components
			final int rw = transferNameCheckBox.getPreferredSize().width; // to align all right-side components

			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
									.addComponent(delimiterLabel, PREFERRED_SIZE, lw, PREFERRED_SIZE)
									.addGap(lw)
									.addGap(lw)
									.addGap(lw)
									.addGap(lw)
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
									.addComponent(tabCheckBox)
									.addComponent(commaCheckBox)
									.addComponent(semicolonCheckBox)
									.addComponent(spaceCheckBox, rw, rw, Short.MAX_VALUE)
									.addGroup(layout.createSequentialGroup()
										.addComponent(otherCheckBox)
										.addComponent(otherDelimiterTextField, PREFERRED_SIZE, 80, PREFERRED_SIZE)
									)
							)
					)
					.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
									.addComponent(defaultInteractionLabel, PREFERRED_SIZE, lw, PREFERRED_SIZE)
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
									.addComponent(defaultInteractionTextField, rw, rw, Short.MAX_VALUE)
							)
					)
					.addComponent(sep2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
									.addGap(lw)
									.addComponent(startRowLabel, PREFERRED_SIZE, lw, PREFERRED_SIZE)
									.addComponent(commentLineLabel, PREFERRED_SIZE, lw, PREFERRED_SIZE)
									.addGap(lw)
									.addGap(lw)
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
									.addComponent(transferNameCheckBox, rw, rw, Short.MAX_VALUE)
									.addComponent(startRowSpinner, PREFERRED_SIZE, 54, PREFERRED_SIZE)
									.addComponent(commentLineTextField, PREFERRED_SIZE, 54, PREFERRED_SIZE)
									.addComponent(caseSensitiveCheckBox, rw, rw, Short.MAX_VALUE)
									.addComponent(importAllCheckBox, rw, rw, Short.MAX_VALUE)
							)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(delimiterLabel)
							.addComponent(tabCheckBox)
					)
					.addComponent(commaCheckBox)
					.addComponent(semicolonCheckBox)
					.addComponent(spaceCheckBox)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(otherCheckBox)
							.addComponent(otherDelimiterTextField)
					)
					.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(defaultInteractionLabel)
							.addComponent(defaultInteractionTextField)
					)
					.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(transferNameCheckBox)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(startRowLabel)
							.addComponent(startRowSpinner)
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(commentLineLabel)
							.addComponent(commentLineTextField)
					)
					.addComponent(caseSensitiveCheckBox)
					.addComponent(importAllCheckBox)
			);
			
			if (dialogType == NETWORK_IMPORT) {
				caseSensitiveCheckBox.setVisible(false);
			} else {
				sep1.setVisible(false);
				defaultInteractionLabel.setVisible(false);
				defaultInteractionTextField.setVisible(false);
			}
			
			if (dialogType != ONTOLOGY_AND_ANNOTATION_IMPORT)
				importAllCheckBox.setVisible(false);
		}
		
		return textImportOptionPanel;
	}
	
	private JPanel getAnnotationTblMappingPanel() {
		if (annotationTblMappingPanel == null) {
			annotationTblMappingPanel = new JPanel();
			annotationTblMappingPanel.setBorder(LookAndFeelUtil.createTitledBorder("Annotation File to Table Mapping"));
		
			final JLabel keyIconLabel = new JLabel(PRIMARY_KEY.getText());
			keyIconLabel.setFont(iconManager.getIconFont(14.0f));
			
			final JLabel aliasIconLabel = new JLabel(ALIAS.getText());
			aliasIconLabel.setFont(iconManager.getIconFont(14.0f));
			aliasIconLabel.setForeground(ALIAS.getForeground());
			
			final JLabel keyLabel = new JLabel("Key Column in Source Table:");
			final JLabel aliasLabel = new JLabel("Aliases in Source Table:");
			final JLabel netKeyLabel = new JLabel("Key Column for Network:");
			
			final JLabel arrowLabel = new JLabel(IconManager.ICON_ARROW_RIGHT);
			arrowLabel.setFont(iconManager.getIconFont(12.0f));
			
			final GroupLayout layout = new GroupLayout(annotationTblMappingPanel);
			annotationTblMappingPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING, true)
							.addGroup(layout.createSequentialGroup()
									.addComponent(keyIconLabel)
									.addComponent(keyLabel)
							)
							.addComponent(primaryKeyComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGroup(layout.createSequentialGroup()
									.addComponent(aliasIconLabel)
									.addComponent(aliasLabel)
							)
							.addComponent(aliasScrollPane, 320, 320, Short.MAX_VALUE)
					)
					.addComponent(arrowLabel)
					.addGroup(layout.createParallelGroup(LEADING, true)
							.addComponent(netKeyLabel)
							.addComponent(mappingAttributeComboBox, 320, 320, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(keyIconLabel)
							.addComponent(keyLabel)
							.addComponent(netKeyLabel)
					)
					.addGroup(layout.createParallelGroup(CENTER)
							.addComponent(primaryKeyComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(arrowLabel)
							.addComponent(mappingAttributeComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(aliasIconLabel)
							.addComponent(aliasLabel)
					)
					.addComponent(aliasScrollPane, DEFAULT_SIZE, 100, Short.MAX_VALUE)
			);
			
			if (dialogType != ONTOLOGY_AND_ANNOTATION_IMPORT) {
				netKeyLabel.setVisible(false);
				arrowLabel.setVisible(false);
				mappingAttributeComboBox.setVisible(false);
				aliasIconLabel.setVisible(false);
				aliasLabel.setVisible(false);
				aliasScrollPane.setVisible(false);
			}
		}
		
		return annotationTblMappingPanel;
	}
	
	protected JPanel getAnnotationOntologyMappingPanel() {
		if (annotationOntologyMappingPanel == null) {
			annotationOntologyMappingPanel = new JPanel();
			annotationOntologyMappingPanel.setBorder(
					LookAndFeelUtil.createTitledBorder("Annotation File to Ontology Mapping"));
			
			final JLabel ontologyIconLabel = new JLabel(ONTOLOGY.getText());
			ontologyIconLabel.setFont(iconManager.getIconFont(14.0f));
			ontologyIconLabel.setForeground(ONTOLOGY.getForeground());
			
			final JLabel ontologyInAnnotationLabel = new JLabel("Key Column in Annotation File:");
			
			final JLabel arrowLabel = new JLabel(IconManager.ICON_ARROW_RIGHT);
			arrowLabel.setFont(iconManager.getIconFont(12.0f));
			
			ontologyTextField.setEditable(false);
			ontologyTextField.setToolTipText("This ontology will be used for mapping.");

			ontologyInAnnotationComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					final int ontologyCol = ontologyInAnnotationComboBox.getSelectedIndex();
					final List<Integer> gaAlias = new ArrayList<Integer>();
					gaAlias.add(DB_OBJECT_SYNONYM.getPosition());
					
					getPreviewPanel().getPreviewTable().setDefaultRenderer(
							Object.class,
							new AttributePreviewTableCellRenderer(
									keyInFile, gaAlias, ontologyCol, TAXON.getPosition(), importFlag));
					getPreviewPanel().repaint();
				}
			});

			final GroupLayout layout = new GroupLayout(annotationOntologyMappingPanel);
			annotationOntologyMappingPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING)
							.addGroup(layout.createSequentialGroup()
									.addComponent(ontologyIconLabel)
									.addComponent(ontologyInAnnotationLabel)
							)
							.addComponent(ontologyInAnnotationComboBox, 280, 280, Short.MAX_VALUE)
					)
					.addComponent(arrowLabel)
					.addGroup(layout.createParallelGroup(TRAILING)
							.addComponent(targetOntologyLabel, LEADING)
							.addComponent(ontologyTextField, 280, 280, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(ontologyIconLabel)
							.addComponent(ontologyInAnnotationLabel)
							.addComponent(targetOntologyLabel)
					)
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(ontologyInAnnotationComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(ontologyTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(arrowLabel)
					)
			);
			
			// Disable unnecessary components
			importAllCheckBox.setSelected(true);
			importAllCheckBox.setEnabled(false);
		}
		
		return annotationOntologyMappingPanel;
	}
	
	protected PreviewTablePanel getPreviewPanel() {
		if (previewPanel == null) {
			if (dialogType == NETWORK_IMPORT) {
				previewPanel = new PreviewTablePanel(PreviewTablePanel.NETWORK_PREVIEW, iconManager);
			} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
				commentLineTextField.setText("!");
				importAllCheckBox.setEnabled(false);
				previewPanel = new PreviewTablePanel(PreviewTablePanel.ONTOLOGY_PREVIEW, iconManager);
			} else {
				previewPanel = new PreviewTablePanel(iconManager);
			}
		}
		
		return previewPanel;
	}
	
	protected JButton getAdvancedButton() {
		if (advancedButton == null) {
			advancedButton = new JButton("Advanced Options...");
			
			advancedButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showAdvancedDialog();
				}
			});
		}
		
		return advancedButton;
	}
	
	protected void showAdvancedDialog() {
		if (advancedDialog == null) {
			advancedDialog = new JDialog(SwingUtilities.getWindowAncestor(this), ModalityType.DOCUMENT_MODAL);
			advancedDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			advancedDialog.setResizable(false);
			advancedDialog.setTitle("Import - Advanced Options");
			
			final JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					advancedDialog.setVisible(false);
				}
			});
			
			final GroupLayout layout = new GroupLayout(advancedDialog.getContentPane());
			advancedDialog.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			final ParallelGroup hGroup = layout.createParallelGroup(LEADING, true);
			final SequentialGroup vGroup = layout.createSequentialGroup();
			
			layout.setHorizontalGroup(hGroup);
			layout.setVerticalGroup(vGroup);
			
			if (dialogType == SIMPLE_ATTRIBUTE_IMPORT || dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
				hGroup.addComponent(getAnnotationTblMappingPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(getAnnotationTblMappingPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
			
			if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
				hGroup.addComponent(getAnnotationOntologyMappingPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(getAnnotationOntologyMappingPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
			
			hGroup.addComponent(getTextImportOptionPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
			vGroup.addComponent(getTextImportOptionPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			
			hGroup.addComponent(okButton, TRAILING, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			vGroup.addComponent(okButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		}

		advancedDialog.pack();
		advancedDialog.setLocationRelativeTo(CytoscapeServices.cySwingApplication.getJFrame());
		advancedDialog.setVisible(true);
	}

	/**
	 * Update UI based on the primary key selection.
	 * 
	 * @param evt
	 */
	private void primaryKeyComboBoxActionPerformed(ActionEvent evt) {
		// Not necessary in Network Import.
		if (dialogType == NETWORK_IMPORT)
			return;

		// Update primary key index.
		keyInFile = primaryKeyComboBox.getSelectedIndex();

		// Update
		getPreviewPanel().getPreviewTable().setDefaultRenderer(Object.class, getRenderer(getPreviewPanel().getFileType()));

		try {
			if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
				// setStatusBar(new URL(targetDataSourceTextField.getText()));
			} else {
				setStatusBar(new URL(annotationUrlMap.get(annotationComboBox.getSelectedItem().toString())));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		getPreviewPanel().repaint();

		JTable curTable = aliasTableMap.get(getPreviewPanel().getSelectedSheetName());
		curTable.setModel(aliasTableModelMap.get(getPreviewPanel().getSelectedSheetName()));

		if (curTable.getCellRenderer(0, 1) != null) {
			((AliasTableRenderer) curTable.getCellRenderer(0, 1)).setPrimaryKey(keyInFile);
			aliasScrollPane.setViewportView(curTable);

			primaryKeyMap.put(getPreviewPanel().getSelectedSheetName(), primaryKeyComboBox.getSelectedIndex());

			aliasScrollPane.setViewportView(curTable);
			curTable.repaint();
		}

		// Update table view
		ColumnResizer.adjustColumnPreferredWidths(getPreviewPanel().getPreviewTable());
		getPreviewPanel().getPreviewTable().repaint();
	}

	private void attributeRadioButtonActionPerformed(ActionEvent evt) {
		CyNetwork network = CytoscapeServices.cyApplicationManager.getCurrentNetwork();

		if (nodeRadioButton.isSelected()) {
			// selectedAttributes = Cytoscape.getNodeAttributes();
			if (network != null) {
				selectedAttributes = network.getDefaultNodeTable();
			}

			objType = NODE;
		} else if (edgeRadioButton.isSelected()) {
			// selectedAttributes = Cytoscape.getEdgeAttributes();
			if (network != null) {
				selectedAttributes = network.getDefaultEdgeTable();
			}

			objType = EDGE;
		} else {
			// selectedAttributes = Cytoscape.getNetworkAttributes();
			logger.info("\nNote: ImportTextTableFDialog.attributeRadioButtonActionPerformed():Import network table not implemented yet!\n");
			objType = NETWORK;
		}

		updateMappingAttributeComboBox();
		setKeyList();
	}

	/**
	 * If Import All selected, ID combo box should be set to ID
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

	private void nodeKeyComboBoxActionPerformed(ActionEvent evt) {
		getPreviewPanel().getPreviewTable().setDefaultRenderer(Object.class, getRenderer(getPreviewPanel().getFileType()));

		setKeyList();
	}

	/*
	 * This method indicates whether the first row of a file that is being
	 * imported as a table should be used to populate column names.
	 */
	private void useFirstRow(boolean useFirstRow) {
		final DefaultTableModel model = (DefaultTableModel) getPreviewPanel().getPreviewTable().getModel();
		if (useFirstRow) {
			if ((getPreviewPanel().getPreviewTable() != null) && (model != null)) {
				columnHeaders = new String[getPreviewPanel().getPreviewTable().getColumnCount()];

				for (int i = 0; i < columnHeaders.length; i++) {
					// Save the header
					columnHeaders[i] = getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).getHeaderValue()
							.toString();
					getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i)
							.setHeaderValue((String) model.getValueAt(0, i));
				}

				model.removeRow(0);
				getPreviewPanel().getPreviewTable().getTableHeader().resizeAndRepaint();
			}

		} else {
			// Restore row
			String currentName = null;
			Object headerVal = null;

			for (int i = 0; i < columnHeaders.length; i++) {
				headerVal = getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

				if (headerVal == null) {
					currentName = "";
				} else {
					currentName = headerVal.toString();
				}

				getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).setHeaderValue(columnHeaders[i]);
				columnHeaders[i] = currentName;
			}

			model.insertRow(0, columnHeaders);
			getPreviewPanel().getPreviewTable().getTableHeader().resizeAndRepaint();
			// startRowSpinner.setEnabled(true);
		}

		updateAliasTable();
		updatePrimaryKeyComboBox();

	}

	private void transferNameCheckBoxActionPerformed(ActionEvent evt) {
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
		final int colCount = getPreviewPanel().getPreviewTable().getColumnModel().getColumnCount();
		importFlag = new boolean[colCount];

		for (int i = 0; i < colCount; i++) {
			importFlag[i] = ((AttributePreviewTableCellRenderer) getPreviewPanel().getPreviewTable().getCellRenderer(0, i))
					.isImportFlag(i);
		}

		/*
		 * Get Attribute Names
		 */
		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

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

		final Byte[] test = getPreviewPanel().getDataTypes(getPreviewPanel().getSelectedSheetName());

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
			JTable curTable = aliasTableMap.get(getPreviewPanel().getSelectedSheetName());

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

	private final void setPreviewPanel(final boolean useFirstRow) throws IOException {
		try {
			readAnnotationForPreview(checkDelimiter());
		} catch (Exception e) {
			throw new IOException("Could not read table file for preview.  The source file may contain invalid values.", e);
		}

		transferNameCheckBox.setEnabled(true);
		transferNameCheckBox.setSelected(true);
		if (useFirstRow)
			useFirstRow(true);

		if (getPreviewPanel().getPreviewTable() == null) {
			return;
		} else {
			ColumnResizer.adjustColumnPreferredWidths(getPreviewPanel().getPreviewTable());
			getPreviewPanel().getPreviewTable().repaint();
		}
	}

	private void delimiterCheckBoxActionPerformed(ActionEvent evt) throws IOException {
		transferNameCheckBox.setSelected(false);
		displayPreview();
	}

	/**
	 * Actions for selecting start line.
	 */
	private void startRowSpinnerMouseWheelMoved(MouseWheelEvent evt) {
		JSpinner source = (JSpinner) evt.getSource();

		SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
		Integer oldValue = (Integer) source.getValue();
		int intValue = oldValue.intValue() - (evt.getWheelRotation() * model.getStepSize().intValue());
		Integer newValue = new Integer(intValue);

		if ((model.getMaximum().compareTo(newValue) >= 0) && (model.getMinimum().compareTo(newValue) <= 0)) {
			source.setValue(newValue);
		}
	}

	private List<Integer> getAliasList() {
		final List<Integer> aliasList = new ArrayList<Integer>();
		AliasTableModel curModel = aliasTableModelMap.get(getPreviewPanel().getSelectedSheetName());

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

	private final void displayPreview() throws IOException {
		readAnnotationForPreview(checkDelimiter());
		getPreviewPanel().repaint();
	}

	private void updateComponents() throws JAXBException, IOException {
		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			// Update available file lists.
			panelBuilder.setOntologyComboBox();
			panelBuilder.setAnnotationComboBox();

			if (ontologyComboBox.getSelectedItem() != null)
				ontologyTextField.setText(ontologyComboBox.getSelectedItem().toString());
		}

		getPreviewPanel().getReloadButton().setEnabled(false);
		startRowSpinner.setEnabled(false);
//		startRowLabel.setEnabled(false);
		getPreviewPanel().getPreviewTable().getTableHeader().setReorderingAllowed(false);
		setRadioButtonGroup();

		if (dialogType == NETWORK_IMPORT) {
			// do nothing
		} else { // attribute import
			updateMappingAttributeComboBox();
		}

		setStatusBar("", "", "File Size: Unknown");

		Window parent = SwingUtilities.getWindowAncestor(this);
		if (parent != null)
			parent.pack();
	}

	private void updatePrimaryKeyComboBox() {
		final DefaultTableModel model = (DefaultTableModel) getPreviewPanel().getPreviewTable().getModel();
		String oldSelectedItem = "";

		primaryKeyComboBox.setRenderer(new ComboBoxRenderer(attributeDataTypes));

		if (primaryKeyComboBox.getSelectedItem() != null)
			oldSelectedItem = primaryKeyComboBox.getSelectedItem().toString();

		if ((model != null) && (model.getColumnCount() > 0)) {
			primaryKeyComboBox.removeAllItems();

			Object curValue = null;

			for (int i = 0; i < model.getColumnCount(); i++) {
				curValue = getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

				if (curValue != null) {
					primaryKeyComboBox.addItem(curValue.toString());
					if (curValue.toString().equals(oldSelectedItem))
						primaryKeyComboBox.setSelectedItem(curValue.toString());
				} else {
					primaryKeyComboBox.addItem("");
				}
			}
		}

		primaryKeyComboBox.setEnabled(true);

		Integer selectedIndex = primaryKeyMap.get(getPreviewPanel().getSelectedSheetName());

		if (selectedIndex == null) {
			// primaryKeyComboBox.setSelectedIndex(0);
		} else {
			if (primaryKeyComboBox.getSelectedItem() != null)
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

		if (fileType != null && fileType.equalsIgnoreCase(SupportedFileType.CSV.getExtension()))
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
		final DefaultTableModel model = (DefaultTableModel) getPreviewPanel().getPreviewTable().getModel();

		if ((model != null) && (model.getColumnCount() > 0)) {
			ontologyInAnnotationComboBox.removeAllItems();

			for (int i = 0; i < model.getColumnCount(); i++) {
				ontologyInAnnotationComboBox.addItem(getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i)
						.getHeaderValue().toString());
			}
		}

		ontologyInAnnotationComboBox.setEnabled(true);
	}

	protected void readAnnotationForPreviewOntology(URL sourceURL, List<String> delimiters) throws IOException {

		final int previewSize;

		if (getPreviewPanel().getShowAllRadioButton().isSelected())
			previewSize = -1;
		else
			previewSize = Integer.parseInt(getPreviewPanel().getCounterSpinner().getValue().toString());

		/*
		 * Load data from the given URL.
		 */
		final String commentChar = commentLineTextField.getText();
		int startLine = getStartLineNumber();
		InputStream tempIs = URLUtil.getInputStream(sourceURL);
		getPreviewPanel().setPreviewTable(workbook, this.fileType, sourceURL.toString(), tempIs, delimiters, null,
				previewSize, commentChar, startLine - 1);

		tempIs.close();

		if (getPreviewPanel().getPreviewTable() == null)
			return;

		// Initialize import flags.
		final int colSize = getPreviewPanel().getPreviewTable().getColumnCount();
		importFlag = new boolean[colSize];

		for (int i = 0; i < colSize; i++) {
			importFlag[i] = true;
		}

		listDataTypes = getPreviewPanel().getCurrentListDataTypes();

		for (int i = 0; i < getPreviewPanel().getTableCount(); i++) {
			final int columnCount = getPreviewPanel().getPreviewTable(i).getColumnCount();

			aliasTableModelMap.put(getPreviewPanel().getSheetName(i), new AliasTableModel(keyTable, columnCount));

			if (getPreviewPanel().getFileType() == FileTypes.GENE_ASSOCIATION_FILE) {
				TableModel previewModel = getPreviewPanel().getPreviewTable(i).getModel();
				String[] columnNames = new String[previewModel.getColumnCount()];

				for (int j = 0; j < columnNames.length; j++) {
					columnNames[j] = previewModel.getColumnName(j);
				}

				initializeAliasTable(columnCount, columnNames, i);

				AliasTableModel curModel = aliasTableModelMap.get(getPreviewPanel().getSheetName(i));
				curModel.setValueAt(true, DB_OBJECT_SYNONYM.getPosition(), 0);
				disableComponentsForGA();
			} else {
				initializeAliasTable(columnCount, null, i);
			}

//			advancedOptionCheckBox.setEnabled(true);
//			textImportCheckBox.setEnabled(true);
			updatePrimaryKeyComboBox();

			setOntologyInAnnotationComboBox();

			if (DB_OBJECT_SYMBOL.getPosition() < primaryKeyComboBox.getItemCount())
				primaryKeyComboBox.setSelectedIndex(DB_OBJECT_SYMBOL.getPosition());

			if (GO_ID.getPosition() < ontologyInAnnotationComboBox.getItemCount())
				ontologyInAnnotationComboBox.setSelectedIndex(GO_ID.getPosition());

			attributeRadioButtonActionPerformed(null);

			Window parent = SwingUtilities.getWindowAncestor(this);
			if (parent != null)
				parent.pack();
		}
	}

	/**
	 * Display preview table
	 * 
	 * @param delimiters
	 * @throws IOException
	 */
	protected void readAnnotationForPreview(List<String> delimiters) throws IOException {
		/*
		 * Check number of lines we should load. if -1, load everything in the
		 * file.
		 */
		final int previewSize;

		if (getPreviewPanel().getShowAllRadioButton().isSelected())
			previewSize = -1;
		else
			previewSize = Integer.parseInt(getPreviewPanel().getCounterSpinner().getValue().toString());

		/*
		 * Load data from the given URL.
		 */
		final String commentChar = commentLineTextField.getText();
		int startLine = getStartLineNumber();

		// creating the IS copy
		InputStream tempIs = null;
		if (tempFile != null) {
			tempIs = new FileInputStream(tempFile);
		}
		// Load Spreadsheet data for preview.
		if (fileType != null
				&& (fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) || fileType
						.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) && workbook == null) {
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
			tempIs2 = new FileInputStream(tempFile);

		getPreviewPanel().setPreviewTable(workbook, this.fileType, "", tempIs2, delimiters, null, previewSize, commentChar,
				startLine - 1);

		if (tempIs2 != null) {
			tempIs2.close();
		}

		if (getPreviewPanel().getPreviewTable() == null)
			return;

		// Initialize import flags.
		final int colSize = getPreviewPanel().getPreviewTable().getColumnCount();
		importFlag = new boolean[colSize];

		for (int i = 0; i < colSize; i++) {
			importFlag[i] = true;
		}

		listDataTypes = getPreviewPanel().getCurrentListDataTypes();

		if (dialogType == NETWORK_IMPORT) {

			final String[] columnNames = new String[getPreviewPanel().getPreviewTable().getColumnCount()];
			for (int i = 0; i < columnNames.length; i++)
				columnNames[i] = getPreviewPanel().getPreviewTable().getColumnName(i);

			getNetworkImportPanel().setComboBoxes(columnNames);

			if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension())
					|| this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {
				setDelimitersEnabled(false);
			} else {
				setDelimitersEnabled(true);
			}

			AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) getPreviewPanel().getPreviewTable()
					.getCellRenderer(0, 0);
			rend.setSourceIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
			rend.setTargetIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
			rend.setInteractionIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
		} else {
			for (int i = 0; i < getPreviewPanel().getTableCount(); i++) {
				final int columnCount = getPreviewPanel().getPreviewTable(i).getColumnCount();

				aliasTableModelMap.put(getPreviewPanel().getSheetName(i), new AliasTableModel(keyTable, columnCount));

				if (getPreviewPanel().getFileType() == FileTypes.GENE_ASSOCIATION_FILE) {
					TableModel previewModel = getPreviewPanel().getPreviewTable(i).getModel();
					String[] columnNames = new String[previewModel.getColumnCount()];

					for (int j = 0; j < columnNames.length; j++) {
						columnNames[j] = previewModel.getColumnName(j);
					}

					initializeAliasTable(columnCount, columnNames, i);

					AliasTableModel curModel = aliasTableModelMap.get(getPreviewPanel().getSheetName(i));
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
					setDelimitersEnabled(true);
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

		getPreviewPanel().getReloadButton().setEnabled(true);
		startRowSpinner.setEnabled(true);
//		startRowLabel.setEnabled(true);

		Window parent = SwingUtilities.getWindowAncestor(this);
		if (parent != null)
			parent.pack();
	}

	private void disableComponentsForGA() {
		primaryKeyComboBox.setEnabled(true);
		aliasTableMap.get(getPreviewPanel().getSelectedSheetName()).setEnabled(true);
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

	private void setDelimitersEnabled(final boolean enabled) {
		tabCheckBox.setEnabled(enabled);
		commaCheckBox.setEnabled(enabled);
		spaceCheckBox.setEnabled(enabled);
		semicolonCheckBox.setEnabled(enabled);
		otherCheckBox.setEnabled(enabled);
		otherDelimiterTextField.setEnabled(enabled);
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

		if (getPreviewPanel().getShowAllRadioButton().isSelected()) {
			centerMessage = "All entries are loaded for preview";
		} else {
			centerMessage = "First " + 
							getPreviewPanel().getCounterSpinner().getValue().toString() +
							" entries are loaded for preview";
		}

		if (sourceURL.toString().startsWith("file:")) {
			int fileSize = 0;

			BufferedInputStream fis = null;
			try {
				fis = (BufferedInputStream) sourceURL.openStream();
				fileSize = fis.available();
				fis.close();
			} catch (IOException e) {
				if (fis != null)
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
			rightMessage = "File Size: Unknown (remote data source)";
		}

		setStatusBar("Key-Value Matched: " + getPreviewPanel().checkKeyMatch(primaryKeyComboBox.getSelectedIndex()),
				centerMessage, rightMessage);
	}

	/**
	 * Update the list of mapping attributes.
	 */
	private void setKeyList() {
		if (mappingAttributeComboBox.getSelectedItem() == null) {
			return;
		}

		if (CytoscapeServices.cyApplicationManager.getCurrentNetwork() == null) {
			return;
		}

		String selectedKeyAttribute = mappingAttributeComboBox.getSelectedItem().toString();

		Iterator it;

		Set<Object> valueSet = new TreeSet<Object>();

		// TODO -- setKeyList
		if (selectedKeyAttribute.equals(ID)) {

			if (objType == NODE) {
				for (CyNode node : network.getNodeList()) {
					final String name = network.getRow(node).get(ID, String.class);
					if (name == null)
						continue;
					valueSet.add(name); // ID = "name"
				}
			} else if (objType == EDGE) {
				for (CyEdge edge : network.getEdgeList()) {
					final String name = network.getRow(edge).get(ID, String.class);
					if (name == null)
						continue;
					valueSet.add(name); // ID = "name"
				}
			}
		}
		getPreviewPanel().setKeyAttributeList(valueSet);

	}

	private void updateAliasTableCell(String name, int columnIndex) {
		JTable curTable = aliasTableMap.get(getPreviewPanel().getSelectedSheetName());
		curTable.setDefaultRenderer(Object.class,
				new AliasTableRenderer(attributeDataTypes, primaryKeyComboBox.getSelectedIndex(), iconManager));

		AliasTableModel curModel = aliasTableModelMap.get(getPreviewPanel().getSelectedSheetName());
		curModel.setValueAt(name, columnIndex, 1);
		curTable.setModel(curModel);
		curTable.repaint();
		aliasScrollPane.repaint();
		repaint();
	}

	private void updateAliasTable() {
		if (dialogType == NETWORK_IMPORT)
			return;

		JTable curTable = aliasTableMap.get(getPreviewPanel().getSelectedSheetName());

		curTable.setDefaultRenderer(Object.class,
				new AliasTableRenderer(attributeDataTypes, primaryKeyComboBox.getSelectedIndex(), iconManager));

		AliasTableModel curModel = aliasTableModelMap.get(getPreviewPanel().getSelectedSheetName());

		Object curValue = null;

		for (int i = 0; i < getPreviewPanel().getPreviewTable().getColumnCount(); i++) {
			curValue = getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

			if (curValue != null) {
				curModel.setValueAt(curValue.toString(), i, 1);
			} else {
				getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).setHeaderValue("");
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
		final Object[][] keyTableData = new Object[rowCount][keyTable.length];

		final String tabName;

		if (sheetIndex == -1)
			tabName = getPreviewPanel().getSelectedSheetName();
		else
			tabName = getPreviewPanel().getSheetName(sheetIndex);

		final Byte[] dataTypeArray = getPreviewPanel().getDataTypes(tabName);

		for (int i = 0; i < rowCount; i++) {
			keyTableData[i][0] = new Boolean(false);
			keyTableData[i][1] = "";

			if (columnNames == null) {
				keyTableData[i][2] = "Column " + (i + 1);
			} else {
				keyTableData[i][2] = columnNames[i];
			}

			if (dataTypeArray.length <= i) {
				attributeDataTypes.add(AttributeTypes.TYPE_STRING);
			} else {
				attributeDataTypes.add(dataTypeArray[i]);
			}

			keyTableData[i][3] = "String";
		}

		final AliasTableModel curModel = new AliasTableModel(keyTableData, keyTable);
		aliasTableModelMap.put(tabName, curModel);

		curModel.addTableModelListener(this);
		/*
		 * Set the list and combo box
		 */
		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)
			mappingAttributeComboBox.setEnabled(true);

		final JTable curTable = new JTable();
		curTable.setModel(curModel);
		aliasTableMap.put(tabName, curTable);

		curTable.setDefaultRenderer(Object.class,
				new AliasTableRenderer(attributeDataTypes, primaryKeyComboBox.getSelectedIndex(), iconManager));
		curTable.setEnabled(true);
		curTable.setCellSelectionEnabled(false);
		curTable.getTableHeader().setReorderingAllowed(false);

		curTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		curTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		curTable.getColumnModel().getColumn(2).setPreferredWidth(280);
		curTable.getColumnModel().getColumn(3).setPreferredWidth(100);

		aliasScrollPane.setViewportView(curTable);
		repaint();
	}

	private void updateMappingAttributeComboBox() {
		mappingAttributeComboBox.removeAllItems();

		final ListCellRenderer lcr = mappingAttributeComboBox.getRenderer();
		mappingAttributeComboBox.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel cmp = (JLabel) lcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value.equals(ID)) {
					cmp.setIcon(ID_ICON.getIcon());
				} else {
					// cmp.setIcon(getDataTypeIcon(selectedAttributes.getColumnTypeMap().get(value.toString())));
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

			AliasTableModel curModel = aliasTableModelMap.get(getPreviewPanel().getSelectedSheetName());

			for (int i = 0; i < curModel.getColumnCount(); i++) {
				if ((Boolean) curModel.getValueAt(i, 0)) {
					gaAlias.add(i);
				}
			}

			gaAlias.add(DB_OBJECT_SYNONYM.getPosition());

			rend = new AttributePreviewTableCellRenderer(keyInFile, gaAlias, ontologyCol, TAXON.getPosition(),
					importFlag);
		} else {
			rend = new AttributePreviewTableCellRenderer(keyInFile, new ArrayList<Integer>(),
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST, importFlag);
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
	@Override
	public void tableChanged(TableModelEvent evt) {
		final int row = evt.getFirstRow();
		final int col = evt.getColumn();
		AliasTableModel curModel = aliasTableModelMap.get(getPreviewPanel().getSelectedSheetName());

		if (col == 0) {
			getPreviewPanel().setAliasColumn(row, (Boolean) curModel.getValueAt(row, col));
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

		final JTable table = getPreviewPanel().getPreviewTable();

		if ((table == null) || (table.getModel() == null) || (table.getColumnCount() == 0)) {
			JOptionPane
					.showMessageDialog(this, "No table selected.", "Invalid Table.", JOptionPane.INFORMATION_MESSAGE);

			return false;
		} else if ((table.getColumnCount() < 2) && (dialogType != NETWORK_IMPORT)) {
			JOptionPane.showMessageDialog(this, "Table should contain at least 2 columns.", "Invalid Table.",
					JOptionPane.INFORMATION_MESSAGE);

			return false;
		}

		if (dialogType == NETWORK_IMPORT) {
			final int sIdx = getNetworkImportPanel().getSourceIndex();
			final int tIdx = getNetworkImportPanel().getTargetIndex();
			final int iIdx = getNetworkImportPanel().getInteractionIndex();

			if ((sIdx == tIdx) || (((iIdx == sIdx) || (iIdx == tIdx)) && (iIdx != -1))) {
				JOptionPane.showMessageDialog(this,
						"Columns for source, target, and interaction type must be distinct.", "Same column index.",
						JOptionPane.INFORMATION_MESSAGE);

				return false;
			}
		}

		return true;
	}

	/*
	 * Layout Information for the entire dialog.<br>
	 * 
	 * <p> This layout will be switched by dialog type parameter. </p>
	 */
	private void globalLayout() {
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		/*
		 * Case 1: Simple Attribute Import
		 */
		if (dialogType == SIMPLE_ATTRIBUTE_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(TRAILING, layout.createSequentialGroup()
							.addComponent(statusBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getBasicPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(statusBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addGroup(TRAILING, layout.createSequentialGroup()
							.addComponent(statusBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getBasicPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(statusBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		} else if (dialogType == NETWORK_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPreviewPanel(), TRAILING, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getBasicPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
	}

	public boolean isFirstRowTitle() {
		return transferNameCheckBox.isSelected();
	}

	public int getStartLineNumber() {
		if (isFirstRowTitle())
			return Integer.parseInt(startRowSpinner.getValue().toString());
		return Integer.parseInt(startRowSpinner.getValue().toString()) - 1;
	}

	public String getCommentlinePrefix() {
		return commentLineTextField.getText();
	}

	public int getPrimaryKeyColumnIndex() {
		return primaryKeyComboBox.getSelectedIndex();
	}

	public boolean isCaseSansitive() {
		return caseSensitive;
	}

	public AttributeMappingParameters getAttributeMappingParameters() throws Exception {
		/*
		 * Get import flags
		 */
		final int colCount = getPreviewPanel().getPreviewTable().getColumnModel().getColumnCount();
		importFlag = new boolean[colCount];

		for (int i = 0; i < colCount; i++) {
			importFlag[i] = ((AttributePreviewTableCellRenderer) getPreviewPanel().getPreviewTable().getCellRenderer(0, i))
					.isImportFlag(i);
		}

		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

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

		final Byte[] test = getPreviewPanel().getDataTypes(getPreviewPanel().getSelectedSheetName());

		final Byte[] attributeTypes = new Byte[test.length];

		for (int i = 0; i < test.length; i++) {
			attributeTypes[i] = test[i];
		}

		int startLineNumber = getStartLineNumber();
		String commentChar = null;
		if (!commentLineTextField.getText().isEmpty())
			commentChar = commentLineTextField.getText();
		keyInFile = primaryKeyComboBox.getSelectedIndex();

		// Build mapping parameter object.
		List<String> del = checkDelimiter();
		AttributeMappingParameters mapping = new AttributeMappingParameters(del, listDelimiter, keyInFile,
				attributeNames, attributeTypes, listDataTypes, importFlag, caseSensitive, startLineNumber, commentChar);

		return mapping;
	}

	public NetworkTableMappingParameters getNetworkTableMappingParameters() throws Exception {
		/*
		 * Get import flags
		 */
		final int colCount = getPreviewPanel().getPreviewTable().getColumnModel().getColumnCount();
		importFlag = new boolean[colCount];

		for (int i = 0; i < colCount; i++) {
			importFlag[i] = ((AttributePreviewTableCellRenderer) getPreviewPanel().getPreviewTable().getCellRenderer(0, i))
					.isImportFlag(i);
		}

		/*
		 * Get Attribute Names
		 */

		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = getPreviewPanel().getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

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

		// final byte[] attributeTypes = new byte[getPreviewPanel().getPreviewTable()
		// .getColumnCount()];
		final Byte[] test = getPreviewPanel().getDataTypes(getPreviewPanel().getSelectedSheetName());

		final Byte[] attributeTypes = new Byte[test.length];

		for (int i = 0; i < test.length; i++) {
			attributeTypes[i] = test[i];
		}

		int startLineNumber = getStartLineNumber();

		String commentChar = null;
		if (!commentLineTextField.getText().isEmpty())
			commentChar = commentLineTextField.getText();
		keyInFile = primaryKeyComboBox.getSelectedIndex();

		final int sourceColumnIndex = getNetworkImportPanel().getSourceIndex();
		final int targetColumnIndex = getNetworkImportPanel().getTargetIndex();

		final String defaultInteraction = defaultInteractionTextField.getText();

		final int interactionColumnIndex = getNetworkImportPanel().getInteractionIndex();

		// Build mapping parameter object.
		List<String> del = checkDelimiter();
		NetworkTableMappingParameters mapping;
		mapping = new NetworkTableMappingParameters(del, listDelimiter, attributeNames, attributeTypes, listDataTypes,
				importFlag, sourceColumnIndex, targetColumnIndex, interactionColumnIndex, defaultInteraction,
				startLineNumber, commentChar);

		return mapping;
	}
}
