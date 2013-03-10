package org.cytoscape.filter.internal.quickfind.view;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.filter.internal.filters.util.VisualPropertyUtil;
import org.cytoscape.filter.internal.quickfind.util.CyAttributesUtil;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;



/**
 * Quick Find Config Dialog Box.
 *
 * @author Ethan Cerami.
 */
public class QuickFindConfigDialog extends JDialog {
	/**
	 * Attribute ComboBox
	 */
	private JComboBox attributeComboBox;

	/**
	 * Table of Sample Attribute Values
	 */
	private JTable sampleAttributeValuesTable;

	/**
	 * Attribute description text area.
	 */
	private JTextArea attributeDescriptionBox;

	/**
	 * Current Network
	 */
	private CyNetwork currentNetwork;

	/**
	 * Current Index
	 */
	private GenericIndex currentIndex;

	/**
	 * Index Type.
	 */
	private int indexType;

	/**
	 * Apply Text.
	 */
	private static final String BUTTON_INDEX_TEXT = "Apply";

	/**
	 * Reindex Text.
	 */
	private static final String BUTTON_REINDEX_TEXT = "Apply";

	/**
	 * Apply Button.
	 */
	private JButton applyButton;

	/**
	 * Flag to indicate that we are currently adding new attributes.
	 */
	private boolean addingNewAttributeList = false;

	private final CyApplicationManager applicationManager;

	private final QuickFind quickFind;
	
	/**
	 * Constructor.
	 */
	public QuickFindConfigDialog(final QuickFind quickFind, CyApplicationManager applicationManager, CySwingApplication application) {
		this.applicationManager = applicationManager;
		this.quickFind = quickFind;
		
		//  Initialize, based on currently selected network
		currentNetwork = applicationManager.getCurrentNetwork();

		currentIndex = quickFind.getIndex(currentNetwork);
		indexType = currentIndex.getIndexType();

		Container container = getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		RenderingEngine<CyNetwork> engine = applicationManager.getCurrentRenderingEngine();
		VisualLexicon lexicon = engine.getVisualLexicon();
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		String title = VisualPropertyUtil.get(lexicon, view, "NETWORK_TITLE", BasicVisualLexicon.NETWORK, String.class);
		this.setTitle("Configure Search Options for:  " + title);

		//  Create Master Panel
		JPanel masterPanel = new JPanel();
		masterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));

		//  Add Node/Edge Selection Panel
		JPanel nodeEdgePanel = createNodeEdgePanel();
		masterPanel.add(nodeEdgePanel);

		//  Add Attribute ComboBox Panel
		JPanel attributePanel = createAttributeSelectionPanel();
		masterPanel.add(attributePanel);

		//  Add Attribute Description Panel
		JPanel attributeDescriptionPanel = createAttributeDescriptionPanel();
		masterPanel.add(attributeDescriptionPanel);

		//  Add Sample Attribute Values Panel
		JPanel attributeValuePanel = createAttributeValuePanel();
		masterPanel.add(attributeValuePanel);

		//  Add Button Panel
		masterPanel.add(Box.createVerticalGlue());

		JPanel buttonPanel = createButtonPanel();
		masterPanel.add(buttonPanel);
		container.add(masterPanel);

		//  Pack, set modality, and center on screen
		pack();
		setModal(true);
		setLocationRelativeTo(application.getJFrame());
		setVisible(true);
	}

	/**
	 * Gets Index Type.
	 *
	 * @return QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES.
	 */
	int getIndexType() {
		return this.indexType;
	}

	/**
	 * Enable / Disable Apply Button.
	 *
	 * @param enable Enable flag;
	 */
	void enableApplyButton(boolean enable) {
		if (applyButton != null) {
			applyButton.setEnabled(enable);
		}
	}

	/**
	 * Creates Button Panel.
	 *
	 * @return JPanel Object.
	 */
	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		// Cancel Button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					QuickFindConfigDialog.this.setVisible(false);
					QuickFindConfigDialog.this.dispose();
				}
			});

		//  Apply Button
		applyButton = new JButton(BUTTON_REINDEX_TEXT);
		applyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					QuickFindConfigDialog.this.setVisible(false);
					QuickFindConfigDialog.this.dispose();

					String newAttribute = (String) attributeComboBox.getSelectedItem();
					ReindexQuickFind task = new ReindexQuickFind(quickFind, currentNetwork, indexType,
					                                             newAttribute);
					
//					// TODO: Port this later
//					JTaskConfig config = new JTaskConfig();
//					config.setAutoDispose(true);
//					config.displayStatus(true);
//					config.displayTimeElapsed(false);
//					config.displayCloseButton(true);
//					config.setOwner(application.getJFrame());
//					config.setModal(true);

					//  Execute Task via TaskManager
					//  This automatically pops-open a JTask Dialog Box.
					//  This method will block until the JTask Dialog Box
					//  is disposed.
//					TaskManager.executeTask(task, config);
				}
			});
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(cancelButton);
		buttonPanel.add(applyButton);

		return buttonPanel;
	}

	/**
	 * Creates a Panel to show the currently selected attribute description.
	 *
	 * @return JPanel Object.
	 */
	private JPanel createAttributeDescriptionPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Table Column Description:"));
		panel.setLayout(new BorderLayout());
		attributeDescriptionBox = new JTextArea(5, 40);
		attributeDescriptionBox.setEditable(false);
		attributeDescriptionBox.setLineWrap(true);
		attributeDescriptionBox.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(attributeDescriptionBox);
		panel.add(scrollPane, BorderLayout.CENTER);
		setAttributeDescription();

		return panel;
	}

	/**
	 * Creates a Panel of Sample Attribute Values.
	 *
	 * @return JPanel Object.
	 */
	private JPanel createAttributeValuePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Sample Column Values:"));
		panel.setLayout(new GridLayout(1, 0));

		//  Table Cells are not editable
		sampleAttributeValuesTable = new JTable() {
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
		addTableModel(sampleAttributeValuesTable);
		this.setVisibleRowCount(sampleAttributeValuesTable, 5);
		panel.add(sampleAttributeValuesTable);

		return panel;
	}

	/**
	 * Sets Text for Attribute Description Box.
	 */
	private void setAttributeDescription() {
		Object selectedAttribute = attributeComboBox.getSelectedItem();
//		CyAttributes attributes = getCyAttributes();
		String attributeKey;

		if (selectedAttribute != null) {
			attributeKey = selectedAttribute.toString();
		} else {
			attributeKey = currentIndex.getControllingAttribute();
		}

		String description;

		if (attributeKey.equals(QuickFind.UNIQUE_IDENTIFIER)) {
			description = "Each node and edge in Cytoscape is assigned a "
			              + "unique identifier.  This is an alphanumeric value.";
		} else if (attributeKey.equals(QuickFind.INDEX_ALL_ATTRIBUTES)) {
			description = "Index all columns.  Use this option for the "
			              + "widest search scope possible.  Note that indexing "
			              + "all columns on very large networks may take a few " + "seconds.";
		} else {
//			description = attributes.getAttributeDescription(attributeKey);
			description = null;
		}

		if (description == null) {
			description = "No description available.";
		}

		attributeDescriptionBox.setText(description);
		attributeDescriptionBox.setCaretPosition(0);
	}

	/**
	 * Creates TableModel consisting of Distinct Attribute Values.
	 */
	private void addTableModel(JTable table) {
		Object selectedAttribute = attributeComboBox.getSelectedItem();

		//  Determine current attribute key
		String attributeKey;

		if (selectedAttribute != null) {
			attributeKey = selectedAttribute.toString();
		} else {
			attributeKey = currentIndex.getControllingAttribute();
		}

		//  Create column names
		Vector columnNames = new Vector();
		columnNames.add(attributeKey);

		TableModel model = new DefaultTableModel(columnNames, 5);

		DetermineDistinctValuesTask task = new DetermineDistinctValuesTask(model, attributeKey, this, applicationManager);

//		// TODO: Port this later
//		JTaskConfig config = new JTaskConfig();
//		config.setAutoDispose(true);
//		config.displayStatus(true);
//		config.displayTimeElapsed(false);
//		config.displayCloseButton(true);
//		config.setOwner(application.getJFrame());
//		config.setModal(true);

		//  Execute Task via TaskManager
		//  This automatically pops-open a JTask Dialog Box.
		//  This method will block until the JTask Dialog Box
		//  is disposed.
//		table.setModel(model);
//		TaskManager.executeTask(task, config);
	}

	private JPanel createNodeEdgePanel() {
		JPanel nodeEdgePanel = new JPanel();
		nodeEdgePanel.setBorder(new TitledBorder("Search:"));
		nodeEdgePanel.setLayout(new BoxLayout(nodeEdgePanel, BoxLayout.X_AXIS));

		JRadioButton nodeButton = new JRadioButton("Nodes");
		nodeButton.setActionCommand(Integer.toString(QuickFind.INDEX_NODES));

		JRadioButton edgeButton = new JRadioButton("Edges");
		edgeButton.setActionCommand(Integer.toString(QuickFind.INDEX_EDGES));

		if (indexType == QuickFind.INDEX_NODES) {
			nodeButton.setSelected(true);
		} else {
			edgeButton.setSelected(true);
		}

		ButtonGroup group = new ButtonGroup();
		group.add(nodeButton);
		group.add(edgeButton);
		nodeEdgePanel.add(nodeButton);
		nodeEdgePanel.add(edgeButton);
		nodeEdgePanel.add(Box.createHorizontalGlue());

		//  User has switched index type.
		ActionListener indexTypeListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String actionCommand = actionEvent.getActionCommand();
				int type = Integer.parseInt(actionCommand);

				if (type != indexType) {
					indexType = type;
					addingNewAttributeList = true;

					Vector attributeList = createAttributeList();
					attributeComboBox.removeAllItems();

					for (int i = 0; i < attributeList.size(); i++) {
						attributeComboBox.addItem(attributeList.get(i));
					}

					addingNewAttributeList = false;

					//  Simulate attribute combo box selection.
					//  Invoke via SwingUtilities, so that radio button
					//  selection is not delayed.
					if (attributeList.size() > 0) {
						SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									attributeComboBox.setSelectedIndex(0);
								}
							});
					}
				}
			}
		};

		nodeButton.addActionListener(indexTypeListener);
		edgeButton.addActionListener(indexTypeListener);

		return nodeEdgePanel;
	}

	/**
	 * Creates the Attribute Selection Panel.
	 *
	 * @return JPanel Object.
	 */
	private JPanel createAttributeSelectionPanel() {
		JPanel attributePanel = new JPanel();

		attributePanel.setBorder(new TitledBorder("Select Column:"));
		attributePanel.setLayout(new BoxLayout(attributePanel, BoxLayout.X_AXIS));

		//  Create ComboBox
		Vector attributeList = createAttributeList();
		attributeComboBox = new JComboBox(attributeList);

		String currentAttribute = currentIndex.getControllingAttribute();

		if (currentAttribute != null) {
			attributeComboBox.setSelectedItem(currentAttribute);
		}

		attributePanel.add(attributeComboBox);
		attributePanel.add(Box.createHorizontalGlue());

		//  Add Action Listener
		attributeComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//  If we are adding new attributes to combox box, ignore the event
					if (addingNewAttributeList) {
						return;
					}

					//  First, set text of apply button
					String currentAttribute = currentIndex.getControllingAttribute();
					String newAttribute = (String) attributeComboBox.getSelectedItem();

					if (currentAttribute.equalsIgnoreCase(newAttribute)) {
						applyButton.setText(BUTTON_REINDEX_TEXT);
					} else {
						applyButton.setText(BUTTON_INDEX_TEXT);
					}

					addTableModel(sampleAttributeValuesTable);
					setAttributeDescription();
				}
			});

		return attributePanel;
	}

	private Vector createAttributeList() {
//		// TODO: Port this
		Vector attributeList = new Vector();
//		CyAttributes attributes = getCyAttributes();
//		String[] attributeNames = attributes.getAttributeNames();
//
//		if (attributeNames != null) {
//			//  Show all attributes, except those of TYPE_COMPLEX
//			for (int i = 0; i < attributeNames.length; i++) {
//				int type = attributes.getType(attributeNames[i]);
//
//				//  only show user visible attributes
//				if (attributes.getUserVisible(attributeNames[i])) {
//					if (type != CyAttributes.TYPE_COMPLEX) {
//						attributeList.add(attributeNames[i]);
//					}
//				}
//			}
//
//			//  Alphabetical sort
//			Collections.sort(attributeList);
//
//			//  Add default:  Unique Identifier
//			attributeList.insertElementAt(QuickFind.UNIQUE_IDENTIFIER, 0);
//
//			//  Add option to index by all attributes
//			//  Not yet sure if I want to add this yet.  Keep code below.
//			//  if (attributeList.size() > 1) {
//			//    attributeList.add(QuickFind.INDEX_ALL_ATTRIBUTES);
//			//  }
//		}

		return attributeList;
	}

//	// TODO: Port this
//	CyAttributes getCyAttributes() {
//		CyAttributes attributes;
//
//		if (indexType == QuickFind.INDEX_NODES) {
//			attributes = Cytoscape.getNodeAttributes();
//		} else {
//			attributes = Cytoscape.getEdgeAttributes();
//		}
//
//		return attributes;
//	}

	/**
	 * Sets the Visible Row Count.
	 *
	 * @param table JTable Object.
	 * @param rows  Number of Visible Rows.
	 */
	private void setVisibleRowCount(JTable table, int rows) {
		int height = 0;

		for (int row = 0; row < rows; row++) {
			height += table.getRowHeight(row);
		}

		table.setPreferredScrollableViewportSize(new Dimension(table
		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    .getPreferredScrollableViewportSize().width,
		                                                       height));
	}
}


/**
 * Long-term task to Reindex QuickFind.
 *
 * @author Ethan Cerami.
 */
class ReindexQuickFind implements Task {
	
	private final QuickFind quickFind;
	
	private String newAttributeKey;
	private CyNetwork cyNetwork;
	private int indexType;

	/**
	 * Constructor.
	 *
	 * @param indexType       Index Type.
	 * @param newAttributeKey New Attribute Key for Indexing.
	 */
	ReindexQuickFind(final QuickFind quickFind, CyNetwork cyNetwork, int indexType, String newAttributeKey) {
		this.cyNetwork = cyNetwork;
		this.indexType = indexType;
		this.newAttributeKey = newAttributeKey;
		this.quickFind = quickFind;
	}

	/**
	 * Executes Task:  Reindex.
	 */
	public void run(TaskMonitor taskMonitor) {
		quickFind.reindexNetwork(cyNetwork, indexType, newAttributeKey, taskMonitor);
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void cancel() {
		// No-op
	}

	/**
	 * Gets Title of Task.
	 *
	 * @return Title of Task.
	 */
	public String getTitle() {
		return "ReIndexing";
	}
}


/**
 * Long-term task to determine distinct attribute values.
 *
 * @author Ethan Cerami.
 */
class DetermineDistinctValuesTask implements Task {
	private TableModel tableModel;
	private String attributeKey;
	private QuickFindConfigDialog parentDialog;
	private final CyApplicationManager applicationManager;

	/**
	 * Creates a new DetermineDistinctValuesTask object.
	 *
	 * @param tableModel  DOCUMENT ME!
	 * @param attributeKey  DOCUMENT ME!
	 * @param parentDialog  DOCUMENT ME!
	 */
	public DetermineDistinctValuesTask(TableModel tableModel, String attributeKey,
	                                   QuickFindConfigDialog parentDialog, CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
		this.tableModel = tableModel;

		if (attributeKey.equals(QuickFind.INDEX_ALL_ATTRIBUTES)) {
			attributeKey = QuickFind.UNIQUE_IDENTIFIER;
		}

		this.attributeKey = attributeKey;

		//  Disable apply button, while task is in progress.
		parentDialog.enableApplyButton(false);
		this.parentDialog = parentDialog;
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setProgress(-1);

		//  Obtain distinct attribute values
		CyNetwork network = applicationManager.getCurrentNetwork();

		Iterator<? extends CyIdentifiable> iterator;

		if (parentDialog.getIndexType() == QuickFind.INDEX_NODES) {
			iterator = network.getNodeList().iterator();
		} else {
			iterator = network.getEdgeList().iterator();
		}

		String[] values = CyAttributesUtil.getDistinctAttributeValues(network,iterator,
		                                                              attributeKey, 5);

		if ((values != null) && (values.length > 0)) {
			for (int i = 0; i < values.length; i++) {
				tableModel.setValueAt(values[i], i, 0);
			}

			parentDialog.enableApplyButton(true);
		} else {
			VisualLexicon lexicon = applicationManager.getCurrentRenderingEngine().getVisualLexicon();
			CyNetworkView view = applicationManager.getCurrentNetworkView();
			String title = VisualPropertyUtil.get(lexicon, view, "NETWORK_TITLE", BasicVisualLexicon.NETWORK, String.class);
			tableModel.setValueAt("No values found in network:  " + title
			                      + ".  Cannot create index.", 0, 0);
		}
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void cancel() {
		//  No-op
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getTitle() {
		return "Accessing sample column data";
	}
}
