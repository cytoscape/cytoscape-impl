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
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.EDGE;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.NETWORK;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.NODE;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.DB_OBJECT_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.GO_ID;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.TAXON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIcons.ID_ICON;
import static org.cytoscape.tableimport.internal.util.ImportType.NETWORK_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.ONTOLOGY_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.TABLE_IMPORT;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.INTERACTION;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.KEY;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.NONE;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ONTOLOGY;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.IndexedPropertyChangeEvent;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import org.cytoscape.tableimport.internal.ui.PreviewTablePanel.PreviewTableModel;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.FileType;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.tableimport.internal.util.TypeUtil;
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
@SuppressWarnings("serial")
public class ImportTablePanel extends JPanel implements PropertyChangeListener, DataEvents {
	
	private static final Logger logger = LoggerFactory.getLogger(ImportTablePanel.class);

	/*
	 * Default value for Interaction edge attribute.
	 */
	private static final String DEFAULT_INTERACTION = "pp";

	private static final String ID = CyNetwork.NAME;

	private JDialog advancedDialog;
	
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
	private JLabel delimiterLabel;
	protected JCheckBox tabCheckBox;
	protected JCheckBox commaCheckBox;
	protected JCheckBox semicolonCheckBox;
	protected JCheckBox spaceCheckBox;
	protected JCheckBox otherCheckBox;
	protected JTextField otherDelimiterTextField;
	
	private JCheckBox transferNameCheckBox;

	JStatusBar statusBar;

	// protected DefaultTableModel model;
	protected JTable aliasTable;
	protected JCheckBox importAllCheckBox;
	
	// Data Type
	private ObjectType objType;
	private final ImportType importType;
	
	protected Map<String, String> annotationUrlMap;
	protected Map<String, String> annotationFormatMap;
	protected Map<String, Map<String, String>> annotationAttributesMap;
	protected Map<String, String> ontologyUrlMap;
	protected Map<String, String> ontologyTypeMap;
	protected Map<String, String> ontologyDescriptionMap;

	/*
	 * Tracking multiple sheets.
	 */
	private CyTable selectedAttributes;
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	private CyNetwork network;

	private final String fileType;

	private Workbook workbook;

	private OntologyPanelBuilder panelBuilder;

	private final InputStreamTaskFactory factory;
	private final CyServiceRegistrar serviceRegistrar;
	private File tempFile;
	
	private boolean updating;

	public ImportTablePanel(
			final ImportType importType,
			final InputStream is,
			final String fileType,
			final InputStreamTaskFactory factory,
			final CyServiceRegistrar serviceRegistrar
	) throws JAXBException, IOException {
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
		this.fileType = fileType;

		if (importType != ONTOLOGY_IMPORT) {
			// Before, this.fileType was always null.
			tempFile = File.createTempFile("temp", this.fileType);
			tempFile.deleteOnExit();
			FileOutputStream os = new FileOutputStream(tempFile);
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1)
				os.write(bytes, 0, read);
			
			os.flush();
			os.close();
		}
		
		selectedAttributes = null;

		network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		if (network != null)
			selectedAttributes = network.getDefaultNodeTable();
		
		this.objType = NODE;
		this.importType = importType;

		annotationUrlMap = new HashMap<>();
		annotationFormatMap = new HashMap<>();
		annotationAttributesMap = new HashMap<>();

		ontologyUrlMap = new HashMap<>();
		ontologyDescriptionMap = new HashMap<>();
		ontologyTypeMap = new HashMap<>();

		initComponents();
		updateComponents();

		getPreviewPanel().addPropertyChangeListener(this);

		// Hide input file and use inputStream
		this.attributeFileLabel.setVisible(false);
		this.selectAttributeFileButton.setVisible(false);
		this.targetDataSourceTextField.setVisible(false);

		// Case import network
		if (this.importType == NETWORK_IMPORT) {
			this.edgeRadioButton.setVisible(false);
			this.nodeRadioButton.setVisible(false);
		}

		// Case import node/edge attribute
		if (this.importType == TABLE_IMPORT)
			this.networkRadioButton.setVisible(false);

		try {
			setPreviewPanel();
		} catch (Exception e) {
			logger.error("Failed to create preview.  (Invalid input file)", e);
			throw new IOException("Fialed to create preview.", e);
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (changes != null)
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
	public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ATTR_TYPE_CHANGED)) {
			SourceColumnSemantic type = null;
			int index = -1;
			
			if (evt instanceof IndexedPropertyChangeEvent) {
				type = (SourceColumnSemantic) ((IndexedPropertyChangeEvent)evt).getNewValue();
				index = ((IndexedPropertyChangeEvent)evt).getIndex();
			}
			
			// Update UI based on the primary key selection
			if (type == KEY && index >= 0 && importType != NETWORK_IMPORT) {
				// Update
				try {
					if (importType == ONTOLOGY_IMPORT)
						setStatusBar(new URL(annotationUrlMap.get(annotationComboBox.getSelectedItem().toString())));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
	
				getPreviewPanel().repaint();
	
				final JTable table = getPreviewPanel().getPreviewTable();
	
				// Update table view
				ColumnResizer.adjustColumnPreferredWidths(table);
				table.repaint();
			}
		}
	}

	private void initComponents() {
		statusBar = new JStatusBar();

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
		
		delimiterLabel = new JLabel("Delimiter:");
		delimiterLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		tabCheckBox = new JCheckBox();
		commaCheckBox = new JCheckBox();
		semicolonCheckBox = new JCheckBox();
		spaceCheckBox = new JCheckBox();
		otherCheckBox = new JCheckBox();
		otherDelimiterTextField = new JTextField();

		defaultInteractionTextField = new JTextField();

		attributeFileLabel = new JLabel();

		startRowSpinner = new JSpinner();

		commentLineTextField = new JTextField();
		commentLineTextField.setName("commentLineTextField");

		attrTypeButtonGroup.add(nodeRadioButton);
		attrTypeButtonGroup.add(edgeRadioButton);
		attrTypeButtonGroup.add(networkRadioButton);
		
		/*
		 * Set tooltips options.
		 */
		ToolTipManager tp = ToolTipManager.sharedInstance();
		tp.setInitialDelay(40);
		tp.setDismissDelay(50000);

		if (importType == ONTOLOGY_IMPORT) {
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

		if (importType == ONTOLOGY_IMPORT) {
			panelBuilder = new OntologyPanelBuilder(this, factory, serviceRegistrar);
			panelBuilder.buildPanel();
		}

		if ((importType == TABLE_IMPORT) || (importType == NETWORK_IMPORT)) {
			// titleIconLabel.setIcon(SPREADSHEET_ICON_LARGE.getIcon());
			attributeFileLabel.setText("Input File");
			
			selectAttributeFileButton.setText("Select File(s)");
			selectAttributeFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						setPreviewPanel();
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
		if (importType == ONTOLOGY_IMPORT) {
			mappingAttributeComboBox.setEnabled(true);
			mappingAttributeComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					updateTypes(getPreviewPanel().getFileType());
					setKeyList();
				}
			});
		}

		final ChangeListener delimitersChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				otherDelimiterTextField.setEnabled(otherCheckBox.isSelected());
				
				try {
					if (!updating)
						displayPreview();
				} catch (IOException e) {
					logger.error("Error on ChangeEvent of checkbox " + ((JCheckBox)evt.getSource()).getText(), e);
				}
			}
		};
		
		tabCheckBox.setText("<html><b><font size=+1>\u21b9 <font></b><font size=-2>(tab)</font><html>");
		tabCheckBox.addChangeListener(delimitersChangeListener);

		commaCheckBox.setText("<html><b><font size=+1>, <font></b><font size=-2>(comma)</font><html>");
		commaCheckBox.addChangeListener(delimitersChangeListener);

		semicolonCheckBox.setText("<html><b><font size=+1>; <font></b><font size=-2>(semicolon)</font><html>");
		semicolonCheckBox.addChangeListener(delimitersChangeListener);

		spaceCheckBox.setText("<html><b><font size=+1>\u2423 <font></b><font size=-2>(space)</font><html>");
		spaceCheckBox.addChangeListener(delimitersChangeListener);

		otherCheckBox.setText("Other:");
		otherCheckBox.addChangeListener(delimitersChangeListener);

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
					if (!updating)
						displayPreview();
				} catch (IOException e) {
					logger.error("Error on reloadButton.actionPerformed", e);
					throw new IllegalStateException("Could not reload target file.");
				}
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
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				getPreviewPanel().disposeEditDialog(true);
			}
		});
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
			
			if (importType == ONTOLOGY_IMPORT) {
				hGroup.addComponent(getDataSourcesPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(getDataSourcesPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			} else if (importType == TABLE_IMPORT || importType == NETWORK_IMPORT) {
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
			textImportOptionPanel.setBorder(LookAndFeelUtil.createTitledBorder("File Import Options"));
			
			final JLabel startRowLabel = new JLabel("Start Import Row:");
			startRowLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			final JLabel commentLineLabel = new JLabel("Ignore lines starting with:");
			commentLineLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			final JLabel defaultInteractionLabel = new JLabel("Default Interaction:");
			defaultInteractionLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			final GroupLayout layout = new GroupLayout(textImportOptionPanel);
			textImportOptionPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			// Get the width of the largest left and right components
			final int lw = commentLineLabel.getPreferredSize().width; // to align all left-side components
			final int rw = getTransferNameCheckBox().getPreferredSize().width; // to align all right-side components

			final ParallelGroup hGroup = layout.createParallelGroup(Alignment.LEADING, true);
			final SequentialGroup vGroup = layout.createSequentialGroup();

			if (!isSpreadsheetFile()) {
				// These fields cannot be used with Excel files
				final JSeparator sep = new JSeparator();
				
				hGroup
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
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				
				vGroup
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
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
			
			if (importType == NETWORK_IMPORT) {
				final JSeparator sep = new JSeparator();
				
				hGroup
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
									.addComponent(defaultInteractionLabel, PREFERRED_SIZE, lw, PREFERRED_SIZE)
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
									.addComponent(defaultInteractionTextField, rw, rw, Short.MAX_VALUE)
							)
					)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				
				vGroup
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(defaultInteractionLabel)
							.addComponent(defaultInteractionTextField)
					)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
			
			layout.setHorizontalGroup(hGroup
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
									.addGap(lw)
									.addComponent(startRowLabel, PREFERRED_SIZE, lw, PREFERRED_SIZE)
									.addComponent(commentLineLabel, PREFERRED_SIZE, lw, PREFERRED_SIZE)
									.addGap(lw)
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
									.addComponent(getTransferNameCheckBox(), rw, rw, Short.MAX_VALUE)
									.addComponent(startRowSpinner, PREFERRED_SIZE, 54, PREFERRED_SIZE)
									.addComponent(commentLineTextField, PREFERRED_SIZE, 54, PREFERRED_SIZE)
									.addComponent(getImportAllCheckBox(), rw, rw, Short.MAX_VALUE)
							)
					)
			);
			layout.setVerticalGroup(vGroup
					.addComponent(getTransferNameCheckBox())
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(startRowLabel)
							.addComponent(startRowSpinner)
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(commentLineLabel)
							.addComponent(commentLineTextField)
					)
					.addComponent(getImportAllCheckBox())
			);
			
			if (importType != ONTOLOGY_IMPORT)
				getImportAllCheckBox().setVisible(false);
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
			
			annotationTblMappingPanel.setVisible(importType == ONTOLOGY_IMPORT);
		}
		
		return annotationTblMappingPanel;
	}
	
	protected PreviewTablePanel getPreviewPanel() {
		if (previewPanel == null) {
			if (importType == ONTOLOGY_IMPORT) {
				commentLineTextField.setText("!");
				getImportAllCheckBox().setEnabled(false);
			}
			
			previewPanel = new PreviewTablePanel(importType, serviceRegistrar.getService(IconManager.class));
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
			
			if (importType == TABLE_IMPORT || importType == ONTOLOGY_IMPORT) {
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
			
			if (importType == ONTOLOGY_IMPORT) {
				// Disable unnecessary components
				getImportAllCheckBox().setSelected(true);
				getImportAllCheckBox().setEnabled(false);
			}
		}

		advancedDialog.pack();
		advancedDialog.setLocationRelativeTo(serviceRegistrar.getService(CySwingApplication.class).getJFrame());
		advancedDialog.setVisible(true);
	}

	private JCheckBox getImportAllCheckBox() {
		if (importAllCheckBox == null) {
			importAllCheckBox = new JCheckBox("Import everything (Key is always ID)");
			importAllCheckBox.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					// If Import All selected, ID combo box should be set to ID
					if (importAllCheckBox.isSelected()) {
						// Lock key to ID
						mappingAttributeComboBox.setSelectedItem(ID);
						mappingAttributeComboBox.setEnabled(false);
					} else {
						mappingAttributeComboBox.setEnabled(true);
					}
				}
			});
		}
		
		return importAllCheckBox;
	}
	
	private JCheckBox getTransferNameCheckBox() {
		if (transferNameCheckBox == null) {
			transferNameCheckBox = new JCheckBox("Use first line as column names");
			transferNameCheckBox.setSelected(isFirstRowNames());
			transferNameCheckBox.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					useFirstRowAsNames(transferNameCheckBox.isSelected());
					repaint();
				}
			});
		}
		
		return transferNameCheckBox;
	}
	
	private void attributeRadioButtonActionPerformed(ActionEvent evt) {
		final CyNetwork network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();

		if (nodeRadioButton.isSelected()) {
			if (network != null)
				selectedAttributes = network.getDefaultNodeTable();

			objType = NODE;
		} else if (edgeRadioButton.isSelected()) {
			if (network != null)
				selectedAttributes = network.getDefaultEdgeTable();

			objType = EDGE;
		} else {
			logger.info("\nNote: ImportTextTableFDialog.attributeRadioButtonActionPerformed():Import network table not implemented yet!\n");
			objType = NETWORK;
		}

		updateMappingAttributeComboBox();
		setKeyList();
	}

	/**
	 * This method indicates whether the first row of a file that is being
	 * imported as a table should be used to populate column names.
	 */
	private void useFirstRowAsNames(final boolean b) {
		final JTable table = getPreviewPanel().getPreviewTable();
		
		if (table != null) {
			final PreviewTableModel model = (PreviewTableModel) table.getModel();
			model.setFirstRowNames(b);
			ColumnResizer.adjustColumnPreferredWidths(table);
		}
	}

	/**
	 * Load from the data source.<br>
	 */
	public void importTable() throws Exception {
		if (!isInputTableValid())
			return;
		
		final String[] attrNames = getPreviewPanel().getAttributeNames();
		final SourceColumnSemantic[] types = getPreviewPanel().getTypes();
		
		if (!isAttributeNamesValid(attrNames, types))
			return;

		if (importType == ONTOLOGY_IMPORT)
			panelBuilder.importOntologyAndAnnotation();
	}

	private final void setPreviewPanel() throws IOException {
		try {
			readAnnotationForPreview(checkDelimiter());
		} catch (Exception e) {
			throw new IOException("Could not read table file for preview.  The source file may contain invalid values.", e);
		}
		
		if (getPreviewPanel().getPreviewTable() != null) {
			ColumnResizer.adjustColumnPreferredWidths(getPreviewPanel().getPreviewTable());
			getPreviewPanel().getPreviewTable().repaint();
		}
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
		updating = true;
		
		try {
			if (importType == ONTOLOGY_IMPORT) {
				// Update available file lists.
				panelBuilder.setOntologyComboBox();
				panelBuilder.setAnnotationComboBox();
			}
	
			getPreviewPanel().getReloadButton().setEnabled(false);
			startRowSpinner.setEnabled(false);
			getPreviewPanel().getPreviewTable().getTableHeader().setReorderingAllowed(false);
			
			attrTypeButtonGroup.setSelected(nodeRadioButton.getModel(), true);

			if (fileType != null && fileType.equalsIgnoreCase(SupportedFileType.CSV.getExtension())) {
				commaCheckBox.setSelected(true);
			} else {
				tabCheckBox.setSelected(true);
				spaceCheckBox.setSelected(importType == NETWORK_IMPORT);
			}

			otherDelimiterTextField.setEnabled(false);
			
			if (importType != NETWORK_IMPORT)
				updateMappingAttributeComboBox();
			
			if (importType == ONTOLOGY_IMPORT)
				disableComponentsForGA();
		} finally {
			updating = false;
		}

		setStatusBar("", "", "File Size: Unknown");
		Window parent = SwingUtilities.getWindowAncestor(this);
		
		if (parent != null)
			parent.pack();
	}

	protected void readAnnotationForPreviewOntology(final URL sourceURL, List<String> delimiters) throws IOException {
		/*
		 * Load data from the given URL.
		 */
		final String commentChar = getCommentLinePrefix();
		final int startLine = getStartLineNumber();
		final InputStream tempIs = URLUtil.getInputStream(sourceURL);
		getPreviewPanel().updatePreviewTable(workbook, this.fileType, sourceURL.toString(), tempIs, delimiters,
				commentChar, startLine - 1);

		tempIs.close();

		if (getPreviewPanel().getPreviewTable() == null)
			return;

		final JTable table = getPreviewPanel().getPreviewTable();
		
		if (getPreviewPanel().getFileType() == FileType.GENE_ASSOCIATION_FILE) {
			final TableModel previewModel = table.getModel();
			final String[] columnNames = new String[previewModel.getColumnCount()];

			for (int j = 0; j < columnNames.length; j++)
				columnNames[j] = previewModel.getColumnName(j);
		}

		getPreviewPanel().setType(GO_ID.getPosition(), ONTOLOGY);

		attributeRadioButtonActionPerformed(null);
		final Window parent = SwingUtilities.getWindowAncestor(this);
		
		if (parent != null)
			parent.pack();
	}

	/**
	 * Display preview table
	 */
	protected void readAnnotationForPreview(List<String> delimiters) throws IOException {
		/*
		 * Load data from the given URL.
		 */
		final String commentChar = getCommentLinePrefix();
		int startLine = getStartLineNumber();

		// creating the IS copy
		InputStream tempIs = null;
		
		if (tempFile != null)
			tempIs = new FileInputStream(tempFile);
		
		// Load Spreadsheet data for preview.
		if (isSpreadsheetFile() && workbook == null) {
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

		getPreviewPanel().updatePreviewTable(workbook, fileType, "", tempIs2, delimiters, commentChar, startLine - 1);

		if (tempIs2 != null)
			tempIs2.close();

		if (getPreviewPanel().getPreviewTable() == null)
			return;

		if (importType != NETWORK_IMPORT) {
			if (getPreviewPanel().getFileType() == FileType.GENE_ASSOCIATION_FILE) {
				final JTable table = getPreviewPanel().getPreviewTable();
				final TableModel previewModel = table.getModel();
				final String[] columnNames = new String[previewModel.getColumnCount()];

				for (int j = 0; j < columnNames.length; j++)
					columnNames[j] = previewModel.getColumnName(j);
				
				disableComponentsForGA();
			}

			/*
			 * If this is not an Excel file, enable delimiter checkboxes.
			 */
			if (fileType != null) {
				final FileType type = checkFileType();
				
				if (type == FileType.GENE_ASSOCIATION_FILE) {
					getPreviewPanel().setType(GO_ID.getPosition(), ONTOLOGY);
					disableComponentsForGA();
				} else if (!isSpreadsheetFile()) {
					nodeRadioButton.setEnabled(true);
					edgeRadioButton.setEnabled(true);
					networkRadioButton.setEnabled(true);
					getImportAllCheckBox().setEnabled(false);
				} else {
					getImportAllCheckBox().setEnabled(false);
				}
			}

			attributeRadioButtonActionPerformed(null);
		}

		getPreviewPanel().getReloadButton().setEnabled(true);
		startRowSpinner.setEnabled(true);

		final Window parent = SwingUtilities.getWindowAncestor(this);
		
		if (parent != null)
			parent.pack();
	}

	private void disableComponentsForGA() {
		nodeRadioButton.setSelected(true);
		nodeRadioButton.setEnabled(false);
		edgeRadioButton.setEnabled(false);
		networkRadioButton.setEnabled(false);

		delimiterLabel.setEnabled(false);
		
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

		getImportAllCheckBox().setEnabled(false);
	}

	private FileType checkFileType() {
		if (importType == ONTOLOGY_IMPORT)
			return FileType.CUSTOM_ANNOTATION_FILE;
		
		if (importType == NETWORK_IMPORT)
			return FileType.NETWORK_FILE;

		return FileType.ATTRIBUTE_FILE;
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
			@Override
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
		final SourceColumnSemantic[] types = getPreviewPanel().getTypes();
		
		if (types != null) {
			for (int i = 0; i < types.length; i++)
				getPreviewPanel().setType(i, (types[i] != NONE ? ATTR : NONE));
		}

		if (type == FileType.GENE_ASSOCIATION_FILE) {
			final int keyInFile = getPreviewPanel().getColumnIndex(KEY);
			
			getPreviewPanel().setAliasColumn(DB_OBJECT_SYNONYM.getPosition(), true);
			getPreviewPanel().setType(TAXON.getPosition(), SourceColumnSemantic.TAXON);
			getPreviewPanel().setType(keyInFile, KEY);
		}
		
		getPreviewPanel().updatePreviewTable();
	}

	private void setStatusBar(String message1, String message2, String message3) {
		statusBar.setLeftLabel(message1);
		statusBar.setCenterLabel(message2);
		statusBar.setRightLabel(message3);
	}
	
	private void setStatusBar(final URL sourceURL) {
		final String centerMessage;
		final String rightMessage;

		if (getPreviewPanel().getShowAllRadioButton().isSelected())
			centerMessage = "All entries are loaded for preview";
		else
			centerMessage = "First " + getPreviewPanel().getCounterSpinner().getValue() + " entries are loaded for preview";

		if (sourceURL.toString().startsWith("file:")) {
			int fileSize = 0;
			BufferedInputStream fis = null;
			
			try {
				fis = (BufferedInputStream) sourceURL.openStream();
				fileSize = fis.available();
				fis.close();
			} catch (IOException e) {
				try {
					if (fis != null) fis.close();
				} catch (IOException e1) {
				}
			}

			if ((fileSize / 1000) == 0)
				rightMessage = "File Size: " + fileSize + " Bytes";
			else
				rightMessage = "File Size: " + (fileSize / 1000) + " KBytes";
		} else {
			rightMessage = "File Size: Unknown (remote data source)";
		}

		final int keyInFile = getPreviewPanel().getColumnIndex(KEY);
		setStatusBar("Key-Value Matched: " + getPreviewPanel().checkKeyMatch(keyInFile), centerMessage, rightMessage);
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
	private boolean isInputTableValid() {
		final JTable table = getPreviewPanel().getPreviewTable();
		final JFrame parent = serviceRegistrar.getService(CySwingApplication.class).getJFrame();

		if ((table == null) || (table.getModel() == null) || (table.getColumnCount() == 0)) {
			JOptionPane.showMessageDialog(parent, "No table selected.", "Invalid Table", JOptionPane.WARNING_MESSAGE);

			return false;
		} else if ((table.getColumnCount() < 2) && (importType != NETWORK_IMPORT)) {
			JOptionPane.showMessageDialog(parent, "Table should contain at least 2 columns.", "Invalid Table",
					JOptionPane.INFORMATION_MESSAGE);

			return false;
		}

		if (importType == NETWORK_IMPORT) {
			final int sIdx = getPreviewPanel().getColumnIndex(SOURCE_ATTR);
			final int tIdx = getPreviewPanel().getColumnIndex(TARGET_ATTR);
			final int iIdx = getPreviewPanel().getColumnIndex(INTERACTION);

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
		if (importType == TABLE_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 680, Short.MAX_VALUE)
					.addComponent(statusBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getAdvancedButton())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getBasicPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 340, Short.MAX_VALUE)
					.addComponent(statusBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getAdvancedButton())
			);
		} else if (importType == ONTOLOGY_IMPORT) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
					.addComponent(getBasicPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 680, Short.MAX_VALUE)
					.addComponent(statusBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getAdvancedButton())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getBasicPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviewPanel(), DEFAULT_SIZE, 340, Short.MAX_VALUE)
					.addComponent(statusBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getAdvancedButton())
			);
		} else if (importType == NETWORK_IMPORT) {
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

	private boolean isFirstRowNames() {
		final JTable table = getPreviewPanel().getPreviewTable();
		
		if (table != null && table.getModel() instanceof PreviewTableModel)	
			return ((PreviewTableModel) table.getModel()).isFirstRowNames();
				
		return false;
	}

	private int getStartLineNumber() {
		if (isFirstRowNames())
			return Integer.parseInt(startRowSpinner.getValue().toString());
		
		return Integer.parseInt(startRowSpinner.getValue().toString()) - 1;
	}

	private String getCommentLinePrefix() {
		return commentLineTextField.getText();
	}

	public AttributeMappingParameters getAttributeMappingParameters() throws Exception {
		final String sourceName = getPreviewPanel().getSourceName();
		final String[] attrNames = getPreviewPanel().getAttributeNames();
		final SourceColumnSemantic[] types = getPreviewPanel().getTypes();
		
		if (!isAttributeNamesValid(attrNames, types))
			return null;

		final SourceColumnSemantic[] typesCopy = Arrays.copyOf(types, types.length);
		
		final AttributeDataType[] dataTypes = getPreviewPanel().getDataTypes();
		final AttributeDataType[] dataTypesCopy = Arrays.copyOf(dataTypes, dataTypes.length);
		
		final String[] listDelimiters = getPreviewPanel().getListDelimiters();
		final String[] listDelimitersCopy = Arrays.copyOf(listDelimiters, listDelimiters.length);

		int startLineNumber = getStartLineNumber();
		String commentChar = null;
		
		if (!getCommentLinePrefix().isEmpty())
			commentChar = getCommentLinePrefix();

		// Build mapping parameter object.
		final List<String> del = checkDelimiter();
		final int keyInFile = getPreviewPanel().getColumnIndex(KEY);
		
		final AttributeMappingParameters mapping = new AttributeMappingParameters(sourceName, del, listDelimitersCopy,
				keyInFile, attrNames, dataTypesCopy, typesCopy, startLineNumber, commentChar);

		return mapping;
	}

	private boolean isAttributeNamesValid(final String[] attrNames, final SourceColumnSemantic[] types) {
		for (int i = 0; i < attrNames.length; i++) {
			final String name = attrNames[i];

			for (int j = 0; j < attrNames.length; j++) {
				if (i != j && name.equals(attrNames[j]) &&
						!TypeUtil.allowsDuplicateName(importType, types[i], types[j])) {
					JOptionPane.showMessageDialog(
							serviceRegistrar.getService(CySwingApplication.class).getJFrame(), 
							"Duplicate Column Name Found: " + name,
							"Import Error",
							JOptionPane.ERROR_MESSAGE
					);
	
					return false;
				}
			}
		}
		
		return true;
	}

	public NetworkTableMappingParameters getNetworkTableMappingParameters() throws Exception {
		final String sourceName = getPreviewPanel().getSourceName();
		final String[] attrNames = getPreviewPanel().getAttributeNames();
		final SourceColumnSemantic[] types = getPreviewPanel().getTypes();
		
		if (!isAttributeNamesValid(attrNames, types))
			return null;

		final SourceColumnSemantic[] typesCopy = Arrays.copyOf(types, types.length);
		
		final AttributeDataType[] dataTypes = getPreviewPanel().getDataTypes();
		final AttributeDataType[] dataTypesCopy = Arrays.copyOf(dataTypes, dataTypes.length);
		
		final String[] listDelimiters = getPreviewPanel().getListDelimiters();
		final String[] listDelimitersCopy = Arrays.copyOf(listDelimiters, listDelimiters.length);

		int startLineNumber = getStartLineNumber();

		String commentChar = null;
		
		if (!getCommentLinePrefix().isEmpty())
			commentChar = getCommentLinePrefix();
		
		final int sourceColumnIndex = getPreviewPanel().getColumnIndex(SOURCE);
		final int targetColumnIndex = getPreviewPanel().getColumnIndex(TARGET);
		final int interactionColumnIndex = getPreviewPanel().getColumnIndex(INTERACTION);

		final String defaultInteraction = defaultInteractionTextField.getText();

		// Build mapping parameter object.
		final List<String> del = checkDelimiter();
		final NetworkTableMappingParameters mapping = new NetworkTableMappingParameters(sourceName, del,
				listDelimitersCopy, attrNames, dataTypesCopy, typesCopy, sourceColumnIndex, targetColumnIndex,
				interactionColumnIndex, defaultInteraction, startLineNumber, commentChar);

		return mapping;
	}
	
	private boolean isSpreadsheetFile() {
		return fileType != null &&
				(fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
				 fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension()));
	}
}
