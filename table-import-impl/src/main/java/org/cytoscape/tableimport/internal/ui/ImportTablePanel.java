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
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.DB_OBJECT_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.TAXON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIcons.ID_ICON;
import static org.cytoscape.tableimport.internal.util.ImportType.NETWORK_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.ONTOLOGY_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.TABLE_IMPORT;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.INTERACTION;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.KEY;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.NONE;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.SOURCE;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.SOURCE_ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.TARGET;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.TARGET_ATTR;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextFileDelimiters;
import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.FileType;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.tableimport.internal.util.URLUtil;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.JStatusBar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main panel for Table Import.
 */
public class ImportTablePanel extends JPanel implements PropertyChangeListener {
	
	private static final long serialVersionUID = 7356378931577386260L;

	private static final Logger logger = LoggerFactory.getLogger(ImportTablePanel.class);

	/*
	 * Default value for Interaction edge attribute.
	 */
	private static final String DEFAULT_INTERACTION = "pp";

	/*
	 * Signals used among Swing components in this dialog:
	 */
	public static final String LIST_DELIMITER_CHANGED = "listDelimiterChanged";
	public static final String ATTR_DATA_TYPE_CHANGED = "attrDataTypeChanged";
	public static final String ATTRIBUTE_NAME_CHANGED = "aliasTableChanged";
	public static final String SHEET_CHANGED = "sheetChanged";

	private static final String ID = CyNetwork.NAME;

	private JDialog advancedDialog;
	
	protected JCheckBox transferNameCheckBox;
	protected ButtonGroup attrTypeButtonGroup;
	protected JLabel attributeFileLabel;
	protected JButton browseAnnotationButton;
	protected JButton browseOntologyButton;

	protected JRadioButton edgeRadioButton;
	protected JRadioButton networkRadioButton;
	protected JComboBox<String> mappingAttributeComboBox;
	protected JRadioButton nodeRadioButton;
	protected JComboBox<String> ontologyComboBox;
	protected JButton selectAttributeFileButton;
	protected JComboBox<String> annotationComboBox;

	protected JTextField targetDataSourceTextField;
	
	private JPanel basicPanel;
	private JPanel dataSourcesPanel;
	private JPanel simpleAttributeImportPanel;
	private JPanel textImportOptionPanel;
	private JPanel annotationTblMappingPanel;
	private PreviewTablePanel previewPanel;
	
	private JButton advancedButton;
	
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
	protected JTable aliasTable;
	protected JCheckBox importAllCheckBox;
	
	// Key column index
	protected int keyInFile;

	// Data Type
	private ObjectType objType;
	private final ImportType dialogType;

	protected Map<String, String> annotationUrlMap;
	protected Map<String, String> annotationFormatMap;
	protected Map<String, Map<String, String>> annotationAttributesMap;
	protected Map<String, String> ontologyUrlMap;
	protected Map<String, String> ontologyTypeMap;
	protected Map<String, String> ontologyDescriptionMap;
	private List<AttributeDataType> attributeDataTypes;

	/*
	 * Tracking multiple sheets.
	 */
	private String[] columnHeaders;
	protected String listDelimiter;
	private CyTable selectedAttributes;
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	private CyNetwork network;

	private final String fileType;

	private Workbook workbook;

	private OntologyPanelBuilder panelBuilder;

	private final InputStreamTaskFactory factory;
	private final CyServiceRegistrar serviceRegistrar;
	private File tempFile;

	public ImportTablePanel(
			final ImportType dialogType,
			final InputStream is,
			final String fileType,
			final String inputName,
			final InputStreamTaskFactory factory,
			final CyServiceRegistrar serviceRegistrar
	) throws JAXBException, IOException {
		this(dialogType, is, fileType, factory, serviceRegistrar);
	}

	public ImportTablePanel(
			final ImportType dialogType,
			final InputStream is,
			final String fileType,
			final InputStreamTaskFactory factory,
			final CyServiceRegistrar serviceRegistrar
	) throws JAXBException, IOException {
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
		this.fileType = fileType;

		if (dialogType != ONTOLOGY_IMPORT) {
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
		}
		
		selectedAttributes = null;

		network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		if (network != null) {
			selectedAttributes = network.getDefaultNodeTable();
		}
		
		this.objType = NODE;
		this.dialogType = dialogType;
		this.listDelimiter = PIPE.toString();

		annotationUrlMap = new HashMap<>();
		annotationFormatMap = new HashMap<>();
		annotationAttributesMap = new HashMap<>();

		ontologyUrlMap = new HashMap<>();
		ontologyDescriptionMap = new HashMap<>();
		ontologyTypeMap = new HashMap<>();

		attributeDataTypes = new ArrayList<>();

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
		if (this.dialogType == TABLE_IMPORT)
			this.networkRadioButton.setVisible(false);

		boolean useFirstRow = dialogType == NETWORK_IMPORT || dialogType == TABLE_IMPORT;
		
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
		} else if (evt.getPropertyName().equals(ATTR_DATA_TYPE_CHANGED)) {
			/*
			 * Data type of an attribute has been changed.
			 */
			final AttributeDataType[] dataTypes = (AttributeDataType[]) evt.getNewValue();

			if (dataTypes != null && dataTypes.length > attributeDataTypes.size())
				attributeDataTypes = Arrays.asList(dataTypes);
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

		attrTypeButtonGroup = new ButtonGroup();
		nodeRadioButton = new JRadioButton("Node");
		edgeRadioButton = new JRadioButton("Edge");
		networkRadioButton = new JRadioButton("Network");
		ontologyComboBox = new JComboBox<>();
		browseOntologyButton = new JButton("Browse...");
		annotationComboBox = new JComboBox<>();
		browseAnnotationButton = new JButton("Browse...");

		targetDataSourceTextField = new JTextField();
		selectAttributeFileButton = new JButton();
		mappingAttributeComboBox = new JComboBox<>();
		
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

		/*
		 * Set tooltips options.
		 */
		ToolTipManager tp = ToolTipManager.sharedInstance();
		tp.setInitialDelay(40);
		tp.setDismissDelay(50000);

		if (dialogType == ONTOLOGY_IMPORT) {
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

		if (dialogType == ONTOLOGY_IMPORT) {
			panelBuilder = new OntologyPanelBuilder(this, factory, serviceRegistrar);
			panelBuilder.buildPanel();
		}

		if ((dialogType == TABLE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
			// titleIconLabel.setIcon(SPREADSHEET_ICON_LARGE.getIcon());
			attributeFileLabel.setText("Input File");
			
			selectAttributeFileButton.setText("Select File(s)");
			selectAttributeFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						setPreviewPanel(false);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(
								serviceRegistrar.getService(CySwingApplication.class).getJFrame(),
								"<html>Could not read selected file.<p>See <b>Help->Error Dialog</b> for further details.</html>",
								"Error",
								JOptionPane.ERROR_MESSAGE
						);
						logger.warn("Could not read selected file.", e);
					}
				}
			});
		}

		/*
		 * Layout data for advanced panel
		 */
		if ((dialogType == TABLE_IMPORT) || (dialogType == ONTOLOGY_IMPORT)) {
			if (dialogType == ONTOLOGY_IMPORT) {
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
					logger.error("Error on tabCheckBox.actionPerformed", e);
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
					logger.error("Error on commaCheckBox.actionPerformed", e);
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
					logger.error("Error on semicolonCheckBox.actionPerformed", e);
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
					logger.error("Error on spaceCheckBox.actionPerformed", e);
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
					logger.error("Error on otherCheckBox.actionPerformed", e);
				}
			}
		});

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
					logger.error("Error on otherDelimiterTextField.keyReleased", e);
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
					logger.error("Error on reloadButton.actionPerformed", e);
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
			
			if (dialogType == ONTOLOGY_IMPORT) {
				hGroup.addComponent(getDataSourcesPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(getDataSourcesPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			} else if (dialogType == TABLE_IMPORT || dialogType == NETWORK_IMPORT) {
				hGroup.addComponent(getSimpleAttributeImportPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(getSimpleAttributeImportPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
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
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
									.addComponent(transferNameCheckBox, rw, rw, Short.MAX_VALUE)
									.addComponent(startRowSpinner, PREFERRED_SIZE, 54, PREFERRED_SIZE)
									.addComponent(commentLineTextField, PREFERRED_SIZE, 54, PREFERRED_SIZE)
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
					.addComponent(importAllCheckBox)
			);
			
			if (dialogType != NETWORK_IMPORT) {
				sep1.setVisible(false);
				defaultInteractionLabel.setVisible(false);
				defaultInteractionTextField.setVisible(false);
			}
			
			if (dialogType != ONTOLOGY_IMPORT)
				importAllCheckBox.setVisible(false);
		}
		
		return textImportOptionPanel;
	}
	
	private JPanel getAnnotationTblMappingPanel() {
		if (annotationTblMappingPanel == null) {
			annotationTblMappingPanel = new JPanel();
			annotationTblMappingPanel.setBorder(LookAndFeelUtil.createTitledBorder("Annotation File to Table Mapping"));
		
			final JLabel netKeyLabel = new JLabel("Key Column for Network:");
			
			final GroupLayout layout = new GroupLayout(annotationTblMappingPanel);
			annotationTblMappingPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addComponent(netKeyLabel)
					.addComponent(mappingAttributeComboBox, 320, 320, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(netKeyLabel)
					.addComponent(mappingAttributeComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			annotationTblMappingPanel.setVisible(dialogType == ONTOLOGY_IMPORT);
		}
		
		return annotationTblMappingPanel;
	}
	
	protected PreviewTablePanel getPreviewPanel() {
		if (previewPanel == null) {
			if (dialogType == ONTOLOGY_IMPORT) {
				commentLineTextField.setText("!");
				importAllCheckBox.setEnabled(false);
			}
			
			previewPanel = new PreviewTablePanel(dialogType, serviceRegistrar.getService(IconManager.class));
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
	
	@SuppressWarnings("serial")
	protected void showAdvancedDialog() {
		if (advancedDialog == null) {
			advancedDialog = new JDialog(SwingUtilities.getWindowAncestor(this), ModalityType.DOCUMENT_MODAL);
			advancedDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			advancedDialog.setResizable(false);
			advancedDialog.setTitle("Import - Advanced Options");
			
			final JButton okButton = new JButton(new AbstractAction("OK") {
				@Override
				public void actionPerformed(ActionEvent e) {
					advancedDialog.setVisible(false);
				}
			});
			
			final JPanel contentPane = new JPanel();
			final GroupLayout layout = new GroupLayout(contentPane);
			contentPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			final ParallelGroup hGroup = layout.createParallelGroup(LEADING, true);
			final SequentialGroup vGroup = layout.createSequentialGroup();
			
			layout.setHorizontalGroup(hGroup);
			layout.setVerticalGroup(vGroup);
			
			if (dialogType == TABLE_IMPORT || dialogType == ONTOLOGY_IMPORT) {
				hGroup.addComponent(getAnnotationTblMappingPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(getAnnotationTblMappingPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
			
			hGroup.addComponent(getTextImportOptionPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
			vGroup.addComponent(getTextImportOptionPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			
			hGroup.addComponent(okButton, TRAILING, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			vGroup.addComponent(okButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			
			advancedDialog.setContentPane(contentPane);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(advancedDialog.getRootPane(),
					okButton.getAction(), okButton.getAction());
			advancedDialog.getRootPane().setDefaultButton(okButton);
			
			if (dialogType == ONTOLOGY_IMPORT) {
				// Disable unnecessary components
				importAllCheckBox.setSelected(true);
				importAllCheckBox.setEnabled(false);
			}
		}

		advancedDialog.pack();
		advancedDialog.setLocationRelativeTo(serviceRegistrar.getService(CySwingApplication.class).getJFrame());
		advancedDialog.setVisible(true);
	}

	private void attributeRadioButtonActionPerformed(ActionEvent evt) {
		final CyNetwork network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();

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
		updateTypes(getPreviewPanel().getFileType());
		setKeyList();
	}

	/*
	 * This method indicates whether the first row of a file that is being
	 * imported as a table should be used to populate column names.
	 */
	private void useFirstRow(boolean useFirstRow) {
		final JTable table = getPreviewPanel().getSelectedPreviewTable();
		
		if (table == null)
			return;
		
		final DefaultTableModel model = (DefaultTableModel) table.getModel();
		
		if (useFirstRow) {
			// Save the current header first
			columnHeaders = new String[table.getColumnCount()];

			for (int i = 0; i < columnHeaders.length; i++)
				columnHeaders[i] = table.getColumnModel().getColumn(i).getHeaderValue().toString();

			getPreviewPanel().setFirstRowAsColumnNames();
		} else {
			// Restore row
			String currentName = null;
			Object headerVal = null;

			for (int i = 0; i < columnHeaders.length; i++) {
				headerVal = table.getColumnModel().getColumn(i).getHeaderValue();
				currentName = headerVal == null ? "" : headerVal.toString();
				table.getColumnModel().getColumn(i).setHeaderValue(columnHeaders[i]);
				columnHeaders[i] = currentName;
			}

			model.insertRow(0, columnHeaders);
			table.getTableHeader().resizeAndRepaint();
		}
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
		final int colCount = getPreviewPanel().getSelectedPreviewTable().getColumnModel().getColumnCount();

		/*
		 * Get Attribute Names
		 */
		final List<String> attrNameList = new ArrayList<String>();

		Object curName = null;

		final String tabName = getPreviewPanel().getSelectedTabName();
		final SourceColumnSemantic[] types = getPreviewPanel().getTypes(tabName);
		
		for (int i = 0; i < colCount; i++) {
			curName = getPreviewPanel().getSelectedPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

			if (attrNameList.contains(curName)) {
				int dupIndex = 0;

				for (int idx = 0; idx < attrNameList.size(); idx++) {
					if (curName.equals(attrNameList.get(idx))) {
						dupIndex = idx;

						break;
					}
				}

				if (types[i] != SourceColumnSemantic.NONE && types[dupIndex] != SourceColumnSemantic.NONE) {
					JOptionPane.showMessageDialog(
							serviceRegistrar.getService(CySwingApplication.class).getJFrame(), 
							"Duplicate Column Name Found: " + curName,
							"Import Error",
							JOptionPane.ERROR_MESSAGE
					);

					return;
				}
			}

			if (curName == null) {
				attrNameList.add("Column " + i);
			} else {
				attrNameList.add(curName.toString());
			}
		}

		if (dialogType == ONTOLOGY_IMPORT)
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

		if (getPreviewPanel().getSelectedPreviewTable() == null) {
			return;
		} else {
			ColumnResizer.adjustColumnPreferredWidths(getPreviewPanel().getSelectedPreviewTable());
			getPreviewPanel().getSelectedPreviewTable().repaint();
		}
	}

	private void delimiterCheckBoxActionPerformed(ActionEvent evt) throws IOException {
		transferNameCheckBox.setSelected(false);
		displayPreview();
	}

	/**
	 * Actions for selecting start line.
	 */
	@SuppressWarnings("unchecked")
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

	private final void displayPreview() throws IOException {
		readAnnotationForPreview(checkDelimiter());
		getPreviewPanel().repaint();
	}

	private void updateComponents() throws JAXBException, IOException {
		if (dialogType == ONTOLOGY_IMPORT) {
			// Update available file lists.
			panelBuilder.setOntologyComboBox();
			panelBuilder.setAnnotationComboBox();
		}

		getPreviewPanel().getReloadButton().setEnabled(false);
		startRowSpinner.setEnabled(false);
		getPreviewPanel().getSelectedPreviewTable().getTableHeader().setReorderingAllowed(false);
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

		if (getPreviewPanel().getSelectedPreviewTable() == null)
			return;

		for (int i = 0; i < getPreviewPanel().getTableCount(); i++) {
			if (getPreviewPanel().getFileType() == FileType.GENE_ASSOCIATION_FILE) {
				TableModel previewModel = getPreviewPanel().getPreviewTable(i).getModel();
				String[] columnNames = new String[previewModel.getColumnCount()];

				for (int j = 0; j < columnNames.length; j++) {
					columnNames[j] = previewModel.getColumnName(j);
				}

				disableComponentsForGA();
			}

// TODO
//			if (GO_ID.getPosition() < ontologyInAnnotationComboBox.getItemCount())
//				ontologyInAnnotationComboBox.setSelectedIndex(GO_ID.getPosition());

			attributeRadioButtonActionPerformed(null);
			Window parent = SwingUtilities.getWindowAncestor(this);
			
			if (parent != null)
				parent.pack();
		}
	}

	/**
	 * Display preview table
	 */
	protected void readAnnotationForPreview(List<String> delimiters) throws IOException {
		/*
		 * Check number of lines we should load. if -1, load everything in the file.
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
		
		if (tempFile != null)
			tempIs = new FileInputStream(tempFile);
		
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

		if (tempIs != null)
			tempIs.close();

		InputStream tempIs2 = null;
		
		if (tempFile != null)
			tempIs2 = new FileInputStream(tempFile);

		getPreviewPanel().setPreviewTable(workbook, fileType, "", tempIs2, delimiters, null, previewSize, commentChar,
				startLine - 1);

		if (tempIs2 != null)
			tempIs2.close();

		if (getPreviewPanel().getSelectedPreviewTable() == null)
			return;

		if (dialogType == NETWORK_IMPORT) {
			if (fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension())
					|| fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {
				setDelimitersEnabled(false);
			} else {
				setDelimitersEnabled(true);
			}
		} else {
			for (int i = 0; i < getPreviewPanel().getTableCount(); i++) {
				if (getPreviewPanel().getFileType() == FileType.GENE_ASSOCIATION_FILE) {
					TableModel previewModel = getPreviewPanel().getPreviewTable(i).getModel();
					String[] columnNames = new String[previewModel.getColumnCount()];

					for (int j = 0; j < columnNames.length; j++) {
						columnNames[j] = previewModel.getColumnName(j);
					}

					disableComponentsForGA();
				}
			}

			/*
			 * If this is not an Excel file, enable delimiter checkboxes.
			 */
			FileType type = checkFileType();

			if (fileType != null) {
				if (type == FileType.GENE_ASSOCIATION_FILE) {
// TODO					
//					ontologyInAnnotationComboBox.setSelectedIndex(GO_ID.getPosition());
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

		Window parent = SwingUtilities.getWindowAncestor(this);
		
		if (parent != null)
			parent.pack();
	}

	private void disableComponentsForGA() {
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

	private FileType checkFileType() {
		if (dialogType == ONTOLOGY_IMPORT)
			return FileType.CUSTOM_ANNOTATION_FILE;
		
		if (dialogType == NETWORK_IMPORT)
			return FileType.NETWORK_FILE;

		return FileType.ATTRIBUTE_FILE;
	}

	// TODO
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
					} catch (IOException e1) { }
			}

			if ((fileSize / 1000) == 0) {
				rightMessage = "File Size: " + fileSize + " Bytes";
			} else {
				rightMessage = "File Size: " + (fileSize / 1000) + " KBytes";
			}
		} else {
			rightMessage = "File Size: Unknown (remote data source)";
		}

		setStatusBar("Key-Value Matched: " + getPreviewPanel().checkKeyMatch(keyInFile), centerMessage, rightMessage);
	}

	/**
	 * Update the list of mapping attributes.
	 */
	private void setKeyList() {
		if (mappingAttributeComboBox.getSelectedItem() == null)
			return;
		if (serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork() == null)
			return;

		final String selectedKeyAttribute = mappingAttributeComboBox.getSelectedItem().toString();
		final Set<Object> valueSet = new TreeSet<Object>();

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

	private void updateMappingAttributeComboBox() {
		mappingAttributeComboBox.removeAllItems();
		final ListCellRenderer<? super String> lcr = mappingAttributeComboBox.getRenderer();
		
		mappingAttributeComboBox.setRenderer(new ListCellRenderer<String>() {
			public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
					boolean isSelected, boolean cellHasFocus) {
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

		if (serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork() == null)
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

	private void updateTypes(final FileType type) {
		final String tabName = getPreviewPanel().getSelectedTabName();
		final SourceColumnSemantic[] types = getPreviewPanel().getTypes(tabName);
		
		if (types != null) {
			for (int i = 0; i < types.length; i++)
				getPreviewPanel().setType(tabName, i, (types[i] != NONE ? ATTR : NONE));
		}

		if (type == FileType.GENE_ASSOCIATION_FILE) {
			getPreviewPanel().setAliasColumn(DB_OBJECT_SYNONYM.getPosition(), true);
			getPreviewPanel().setType(tabName, TAXON.getPosition(), SourceColumnSemantic.TAXON);
			getPreviewPanel().setType(tabName, keyInFile, KEY);
		}
		
		getPreviewPanel().getSelectedPreviewTab().update();
	}

	private void setStatusBar(String message1, String message2, String message3) {
		statusBar.setLeftLabel(message1);
		statusBar.setCenterLabel(message2);
		statusBar.setRightLabel(message3);
	}

	public List<String> checkDelimiter() {
		final List<String> delList = new ArrayList<String>();

		if (tabCheckBox.isSelected())
			delList.add(TextFileDelimiters.TAB.toString());

		if (commaCheckBox.isSelected())
			delList.add(TextFileDelimiters.COMMA.toString());

		if (spaceCheckBox.isSelected())
			delList.add(TextFileDelimiters.SPACE.toString());

		if (semicolonCheckBox.isSelected())
			delList.add(TextFileDelimiters.SEMICOLON.toString());

		if (otherCheckBox.isSelected() && otherDelimiterTextField.getText().trim().length() > 0)
			delList.add(otherDelimiterTextField.getText());

		return delList;
	}

	/**
	 * Error checker for input table.
	 * @return true if table looks OK.
	 */
	private boolean checkDataSourceError() {
		final JTable table = getPreviewPanel().getSelectedPreviewTable();
		final JFrame parent = serviceRegistrar.getService(CySwingApplication.class).getJFrame();

		if ((table == null) || (table.getModel() == null) || (table.getColumnCount() == 0)) {
			JOptionPane.showMessageDialog(parent, "No table selected.", "Invalid Table", JOptionPane.WARNING_MESSAGE);

			return false;
		} else if ((table.getColumnCount() < 2) && (dialogType != NETWORK_IMPORT)) {
			JOptionPane.showMessageDialog(parent, "Table should contain at least 2 columns.", "Invalid Table",
					JOptionPane.INFORMATION_MESSAGE);

			return false;
		}

		if (dialogType == NETWORK_IMPORT) {
			final String tabName = getPreviewPanel().getSelectedTabName();
			
			final int sIdx = getPreviewPanel().getColumnIndex(tabName, SOURCE_ATTR);
			final int tIdx = getPreviewPanel().getColumnIndex(tabName, TARGET_ATTR);
			final int iIdx = getPreviewPanel().getColumnIndex(tabName, INTERACTION);

			if ((sIdx == tIdx) || (((iIdx == sIdx) || (iIdx == tIdx)) && (iIdx != -1))) {
				JOptionPane.showMessageDialog(
						parent,
						"Columns for source, target, and interaction type must be distinct.",
						"Same Column Index",
						JOptionPane.WARNING_MESSAGE
				);

				return false;
			}
		}

		return true;
	}

	/**
	 * Layout Information for the entire dialog.
	 * This layout will be switched by dialog type parameter.
	 */
	private void globalLayout() {
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		/*
		 * Case 1: Simple Attribute Import
		 */
		if (dialogType == TABLE_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 680, Short.MAX_VALUE)
					.addGroup(TRAILING, layout.createSequentialGroup()
							.addComponent(statusBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addComponent(getAdvancedButton())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getBasicPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 360, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(statusBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getAdvancedButton())
			);
		} else if (dialogType == ONTOLOGY_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 680, Short.MAX_VALUE)
					.addGroup(TRAILING, layout.createSequentialGroup()
							.addComponent(statusBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addComponent(getAdvancedButton())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getBasicPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 360, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(statusBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getAdvancedButton())
			);
		} else if (dialogType == NETWORK_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 680, Short.MAX_VALUE)
					.addComponent(getAdvancedButton())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getBasicPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 360, Short.MAX_VALUE)
					.addComponent(getAdvancedButton())
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

	public AttributeMappingParameters getAttributeMappingParameters() throws Exception {
		/*
		 * Get import flags
		 */
		final int colCount = getPreviewPanel().getSelectedPreviewTable().getColumnModel().getColumnCount();

		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		final String tabName = getPreviewPanel().getSelectedTabName();
		final SourceColumnSemantic[] types = getPreviewPanel().getTypes(tabName);
		
		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = getPreviewPanel().getSelectedPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

			if (attrNameList.contains(curName)) {
				int dupIndex = 0;

				for (int idx = 0; idx < attrNameList.size(); idx++) {
					if (curName.equals(attrNameList.get(idx))) {
						dupIndex = idx;

						break;
					}
				}

				if (types[i] != SourceColumnSemantic.NONE && types[dupIndex] != SourceColumnSemantic.NONE) {
					JOptionPane.showMessageDialog(
							serviceRegistrar.getService(CySwingApplication.class).getJFrame(), 
							"Duplicate Column Name Found: " + curName,
							"Import Error",
							JOptionPane.ERROR_MESSAGE
					);

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

		final AttributeDataType[] dataTypes = getPreviewPanel().getDataTypes(tabName);
		final AttributeDataType[] dataTypesCopy = Arrays.copyOf(dataTypes, dataTypes.length);
		final SourceColumnSemantic[] typesCopy = Arrays.copyOf(types, types.length);

		int startLineNumber = getStartLineNumber();
		String commentChar = null;
		
		if (!commentLineTextField.getText().isEmpty())
			commentChar = commentLineTextField.getText();

		// Build mapping parameter object.
		final List<String> del = checkDelimiter();
		final AttributeMappingParameters mapping = new AttributeMappingParameters(del, listDelimiter, keyInFile,
				attributeNames, dataTypesCopy, typesCopy, startLineNumber, commentChar);

		return mapping;
	}

	public NetworkTableMappingParameters getNetworkTableMappingParameters() throws Exception {
		final int colCount = getPreviewPanel().getSelectedPreviewTable().getColumnModel().getColumnCount();

		/*
		 * Get Attribute Names
		 */

		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		final String tabName = getPreviewPanel().getSelectedTabName();
		final SourceColumnSemantic[] types = getPreviewPanel().getTypes(tabName);
		
		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = getPreviewPanel().getSelectedPreviewTable().getColumnModel().getColumn(i).getHeaderValue();

			if (attrNameList.contains(curName)) {
				int dupIndex = 0;

				for (int idx = 0; idx < attrNameList.size(); idx++) {
					if (curName.equals(attrNameList.get(idx))) {
						dupIndex = idx;

						break;
					}
				}

				if (types[i] != SourceColumnSemantic.NONE && types[dupIndex] != SourceColumnSemantic.NONE) {
					JOptionPane.showMessageDialog(
							serviceRegistrar.getService(CySwingApplication.class).getJFrame(), 
							"Duplicate Column Name Found: " + curName,
							"Import Error",
							JOptionPane.ERROR_MESSAGE
					);

					return null;
				}
			}

			if (curName == null)
				attrNameList.add("Column " + i);
			else
				attrNameList.add(curName.toString());
		}

		attributeNames = attrNameList.toArray(new String[0]);

		final AttributeDataType[] dataTypes = getPreviewPanel().getDataTypes(tabName);
		final AttributeDataType[] dataTypesCopy = Arrays.copyOf(dataTypes, dataTypes.length);
		final SourceColumnSemantic[] typesCopy = Arrays.copyOf(types, types.length);

		int startLineNumber = getStartLineNumber();

		String commentChar = null;
		
		if (!commentLineTextField.getText().isEmpty())
			commentChar = commentLineTextField.getText();
		
		keyInFile = getPreviewPanel().getColumnIndex(tabName, KEY);
		final int sourceColumnIndex = getPreviewPanel().getColumnIndex(tabName, SOURCE);
		final int targetColumnIndex = getPreviewPanel().getColumnIndex(tabName, TARGET);
		final int interactionColumnIndex = getPreviewPanel().getColumnIndex(tabName, INTERACTION);

		final String defaultInteraction = defaultInteractionTextField.getText();

		// Build mapping parameter object.
		final List<String> del = checkDelimiter();
		NetworkTableMappingParameters mapping = new NetworkTableMappingParameters(del, listDelimiter, attributeNames,
				dataTypesCopy, typesCopy, sourceColumnIndex, targetColumnIndex, interactionColumnIndex,
				defaultInteraction, startLineNumber, commentChar);

		return mapping;
	}
}
