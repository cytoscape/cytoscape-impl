/* File: NetworkMergeFrame.java

 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.network.merge.internal.ui;

import org.cytoscape.network.merge.internal.NetworkMerge;
import org.cytoscape.network.merge.internal.NetworkMergeParameter;
import org.cytoscape.network.merge.internal.NetworkMergeParameterImpl;
import org.cytoscape.network.merge.internal.AttributeBasedNetworkMerge;
import org.cytoscape.network.merge.internal.model.AttributeMappingImpl;
import org.cytoscape.network.merge.internal.model.MatchingAttributeImpl;
import org.cytoscape.network.merge.internal.model.AttributeMapping;
import org.cytoscape.network.merge.internal.model.MatchingAttribute;
import org.cytoscape.network.merge.internal.NetworkMerge.Operation;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictHandler;
//import org.cytoscape.network.merge.internal.conflict.IDMappingAttributeConflictHandler;
import org.cytoscape.network.merge.internal.conflict.DefaultAttributeConflictHandler;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictManager;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollector;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollectorImpl;
import org.cytoscape.network.merge.internal.util.AttributeValueMatcher;
import org.cytoscape.network.merge.internal.util.DefaultAttributeValueMatcher;
import org.cytoscape.network.merge.internal.util.AttributeMerger;
import org.cytoscape.network.merge.internal.util.DefaultAttributeMerger;

import cytoscape.Cytoscape;
import cytoscape.CyNetwork;

import cytoscape.cythesaurus.service.CyThesaurusServiceClient;
import cytoscape.cythesaurus.service.CyThesaurusServiceMessageBasedClient;

import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.data.attr.MultiHashMapDefinition;

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

import cytoscape.util.CyNetworkNaming;
import cytoscape.util.CytoscapeAction;
import cytoscape.util.GraphSetUtils;

import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListModel;
import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.BoxLayout;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.WindowConstants;

/**
 *
 * Main frame for advance network merge
 */
public class NetworkMergeFrame extends JFrame {
	private boolean altDifferenceIsChecked = false;

	/** Creates new form NetworkMergeFrame */
	public NetworkMergeFrame() {
		frame = this;
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		checkCyThesaurus = checkCyThesaurus();

		selectedNetworkAttributeIDType = null;
		tgtType = null;
		matchingAttribute = new MatchingAttributeImpl(Cytoscape.getNodeAttributes());
		nodeAttributeMapping = new AttributeMappingImpl(Cytoscape.getNodeAttributes());
		edgeAttributeMapping = new AttributeMappingImpl(Cytoscape.getEdgeAttributes());

		advancedNetworkMergeCollapsiblePanel = new CollapsiblePanel("Advanced Network Merge");
		advancedNetworkMergeCollapsiblePanel.addCollapeListener(new CollapsiblePanel.CollapeListener() {
				public void collaped() {
					updateOKButtonEnable();
					updateSize();
				}

				public void expanded() {
					updateOKButtonEnable();
					updateSize();
				}
			});

		advancedOptionCollapsiblePanel = new CollapsiblePanel("Advanced Option");

		initComponents();

		updateOKButtonEnable();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		javax.swing.JPanel operationPanel = new javax.swing.JPanel();
		javax.swing.JLabel operationLabel = new javax.swing.JLabel();
		operationComboBox = new javax.swing.JComboBox();
		operationIcon = new javax.swing.JLabel();
		differenceButton = new javax.swing.JRadioButton("Only remove nodes if all their edges are being subtracted, too.");
		difference2Button = new javax.swing.JRadioButton("Remove all nodes that are in the 2nd network.");
		differenceGroup = new javax.swing.ButtonGroup();
		differenceGroup.add(differenceButton);
		differenceGroup.add(difference2Button);
		javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
		javax.swing.JPanel selectNetworkPanel = new javax.swing.JPanel();
		javax.swing.JScrollPane unselectedNetworkScrollPane = new javax.swing.JScrollPane();
		unselectedNetworkData = new SortedNetworkListModel();
		unselectedNetworkList = new javax.swing.JList(unselectedNetworkData);
		javax.swing.JPanel lrButtonPanel = new javax.swing.JPanel();
		rightButton = new javax.swing.JButton();
		leftButton = new javax.swing.JButton();
		javax.swing.JScrollPane selectedNetworkScrollPane = new javax.swing.JScrollPane();
		selectedNetworkData = new NetworkListModel();
		selectedNetworkList = new javax.swing.JList(selectedNetworkData);
		udButtonPanel = new javax.swing.JPanel();
		upButton = new javax.swing.JButton();
		downButton = new javax.swing.JButton();
		collapsiblePanelAgent = advancedNetworkMergeCollapsiblePanel;
		advancedPanel = new javax.swing.JPanel();
		attributePanel = new javax.swing.JPanel();
		matchNodeTable = new MatchNodeTable(matchingAttribute);
		attributeScrollPane = new javax.swing.JScrollPane();
		idmappingCheckBox = new javax.swing.JCheckBox();
		javax.swing.JLabel idmappingLabel = new javax.swing.JLabel();
		javax.swing.JSeparator jSeparator3 = new javax.swing.JSeparator();
		javax.swing.JPanel mergeAttributePanel = new javax.swing.JPanel();
		javax.swing.JTabbedPane mergeAttributeTabbedPane = new javax.swing.JTabbedPane();
		javax.swing.JPanel mergeNodeAttributePanel = new javax.swing.JPanel();
		javax.swing.JScrollPane mergeNodeAttributeScrollPane = new javax.swing.JScrollPane();
		javax.swing.JPanel mergeEdgeAttributePanel = new javax.swing.JPanel();
		javax.swing.JScrollPane mergeEdgeAttributeScrollPane = new javax.swing.JScrollPane();
		advancedOptionCollapsiblePanelAgent = advancedOptionCollapsiblePanel;
		optionPanel = new javax.swing.JPanel();
		inNetworkMergeCheckBox = new javax.swing.JCheckBox();
		javax.swing.JSeparator jSeparator4 = new javax.swing.JSeparator();
		javax.swing.JPanel okPanel = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setTitle("Advanced Network Merge");
		getContentPane().setLayout(new java.awt.GridBagLayout());

		operationPanel.setMinimumSize(new java.awt.Dimension(211, 20));
		operationPanel.setLayout(new javax.swing.BoxLayout(operationPanel, javax.swing.BoxLayout.LINE_AXIS));

		operationLabel.setText("Operation:   ");
		operationPanel.add(operationLabel);

		operationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new Operation[] { Operation.UNION,Operation.INTERSECTION,Operation.DIFFERENCE }));
		selectedOperation = (Operation) operationComboBox.getSelectedItem();
		operationComboBox.setPreferredSize(new java.awt.Dimension(150, 20));
		operationComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					Operation selectOp = (Operation)operationComboBox.getSelectedItem();
					int nSelectedNetwork = selectedNetworkData.getSize();
					if (selectOp==Operation.DIFFERENCE && nSelectedNetwork>2) {
						final int ioption = JOptionPane.showConfirmDialog(frame,
												  "Only the first two networks in the selected network list will be merged for difference operation. All the other selected networks will be removed. Are you sure?",
												  "Warning: only two networks will be kept",
												  JOptionPane.YES_NO_OPTION );
						if (ioption==JOptionPane.NO_OPTION) {
							operationComboBox.setSelectedItem(selectedOperation);
							return;
						}

						for (int is=nSelectedNetwork-1; is>=2; is--) {
							CyNetwork removed = selectedNetworkData.removeElement(is);
							unselectedNetworkData.add(removed);
							addRemoveAttributeMapping(removed,false);
						}
						selectedNetworkList.repaint();
						unselectedNetworkList.repaint();
						updateAttributeTable();
						updateMergeAttributeTable();
					}

					selectedOperation = selectOp;
					operationIcon.setIcon(OPERATION_ICONS[operationComboBox.getSelectedIndex()]);

					if (selectOp == Operation.DIFFERENCE) {
						differenceButton.setVisible(true);
						difference2Button.setVisible(true);
						differenceButton.setSelected(true);
					} else {
						differenceButton.setVisible(false);
						difference2Button.setVisible(false);
					}

					updateUpDownButtonEnable();
					updateSize();
					udButtonPanel.setVisible(getOperation()==Operation.DIFFERENCE);

				}
			});
		operationPanel.add(operationComboBox);

		int contentPaneGridY = 0;
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(operationPanel, gridBagConstraints);

		operationIcon.setIcon(UNION_ICON);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(operationIcon, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(differenceButton, gridBagConstraints);
		differenceButton.setVisible(false);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(difference2Button, gridBagConstraints);
		difference2Button.setVisible(false);
		difference2Button.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent evt) {
					altDifferenceIsChecked = evt.getStateChange() == ItemEvent.SELECTED;
					if (altDifferenceIsChecked)
						operationIcon.setIcon(DIFFERENCE2_ICON);
					else
						operationIcon.setIcon(DIFFERENCE_ICON);
				}
			});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(jSeparator1, gridBagConstraints);

		selectNetworkPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Networks to merge"));
		selectNetworkPanel.setMinimumSize(new java.awt.Dimension(490, 100));
		selectNetworkPanel.setPreferredSize(new java.awt.Dimension(490, 130));
		selectNetworkPanel.setLayout(new java.awt.GridBagLayout());

		unselectedNetworkScrollPane.setPreferredSize(new java.awt.Dimension(200, 100));

		unselectedNetworkList.setBorder(javax.swing.BorderFactory.createTitledBorder("Available networks"));
		for (Iterator<CyNetwork> it = Cytoscape.getNetworkSet().iterator(); it.hasNext(); ) {
			CyNetwork network = it.next();
			unselectedNetworkData.add(network);
		}

		unselectedNetworkList.setCellRenderer(new ListCellRenderer() {
				private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
				public Component getListCellRendererComponent(
									      JList list,
									      Object value,
									      int index,
									      boolean isSelected,
									      boolean cellHasFocus) {
					JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					renderer.setText(((CyNetwork)value).getTitle());
					return renderer;
				}
			});

		unselectedNetworkList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
					int index = unselectedNetworkList.getMinSelectionIndex();
					if (index>-1) {
						selectedNetworkList.getSelectionModel().clearSelection();
						rightButton.setEnabled(true);
					} else {
						rightButton.setEnabled(false);
					}
				}
			});
		unselectedNetworkScrollPane.setViewportView(unselectedNetworkList);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		selectNetworkPanel.add(unselectedNetworkScrollPane, gridBagConstraints);

		lrButtonPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

		rightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/right16.gif"))); // NOI18N
		rightButton.setEnabled(false);
		rightButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					int [] indices = unselectedNetworkList.getSelectedIndices();
					if (indices == null || indices.length == 0) {
						return;
					}

					if (getOperation()==Operation.DIFFERENCE && selectedNetworkData.getSize()+indices.length>2) {
						JOptionPane.showMessageDialog(frame,"Difference operation only supports two networks! If you need to replace the selected network, remove it first and select the new one.", "Warning", JOptionPane.WARNING_MESSAGE );
						return;
					}

					for (int i= indices.length-1; i>=0; i--) {
						CyNetwork removed = unselectedNetworkData.removeElement(indices[i]);
						selectedNetworkData.add(removed);
						addRemoveAttributeMapping(removed,true);
					}

					if (unselectedNetworkData.getSize()==0) {
						unselectedNetworkList.clearSelection();
						rightButton.setEnabled(false);
					} else {
						int minindex = unselectedNetworkList.getMinSelectionIndex();
						if (minindex>= unselectedNetworkData.getSize()) {
							minindex = 0;
						}
						unselectedNetworkList.setSelectedIndex(minindex);
					}

					selectedNetworkList.repaint();
					unselectedNetworkList.repaint();
					updateOKButtonEnable();
					updateAttributeTable();
					updateMergeAttributeTable();
				}
			});
		lrButtonPanel.add(rightButton);

		leftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/left16.gif"))); // NOI18N
		leftButton.setEnabled(false);
		leftButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					int [] indices = selectedNetworkList.getSelectedIndices();
					if (indices == null || indices.length == 0) {
						return;
					}

					for (int i= indices.length-1; i>=0; i--) {
						CyNetwork removed = selectedNetworkData.removeElement(indices[i]);
						unselectedNetworkData.add(removed);
						addRemoveAttributeMapping(removed,false);
					}

					if (selectedNetworkData.getSize()==0) {
						selectedNetworkList.clearSelection();
						leftButton.setEnabled(false);
					} else {
						int minindex = selectedNetworkList.getMinSelectionIndex();
						if (minindex>= selectedNetworkData.getSize()) {
							minindex = 0;
						}
						selectedNetworkList.setSelectedIndex(minindex);
					}

					selectedNetworkList.repaint();
					unselectedNetworkList.repaint();
					updateOKButtonEnable();
					updateAttributeTable();
					updateMergeAttributeTable();
				}
			});
		lrButtonPanel.add(leftButton);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		selectNetworkPanel.add(lrButtonPanel, gridBagConstraints);

		selectedNetworkScrollPane.setPreferredSize(new java.awt.Dimension(200, 100));

		selectedNetworkList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		selectedNetworkList.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected networks"));
		selectedNetworkList.setCellRenderer(new ListCellRenderer() {
				private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
				public Component getListCellRendererComponent(
									      JList list,
									      Object value,
									      int index,
									      boolean isSelected,
									      boolean cellHasFocus) {
					JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					renderer.setText(((CyNetwork)value).getTitle());
					return renderer;
				}
			});
		selectedNetworkList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
					int index = selectedNetworkList.getMinSelectionIndex();
					if (index>-1) {
						unselectedNetworkList.getSelectionModel().clearSelection();
						leftButton.setEnabled(true);
					} else {
						leftButton.setEnabled(false);
					}
					updateUpDownButtonEnable();
				}
			});
		selectedNetworkScrollPane.setViewportView(selectedNetworkList);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		selectNetworkPanel.add(selectedNetworkScrollPane, gridBagConstraints);

		udButtonPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 2));
		udButtonPanel.setVisible(false);

		upButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/up16.gif"))); // NOI18N
		upButton.setEnabled(false);
		upButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					int [] indices = selectedNetworkList.getSelectedIndices();
					if (indices == null || indices.length == 0) {
						return;
					}

					int imin = selectedNetworkList.getMinSelectionIndex();
					int imax = selectedNetworkList.getMaxSelectionIndex();

					if (imin<1) return;

					CyNetwork removed = selectedNetworkData.removeElement(imin-1);
					selectedNetworkData.add(imax,removed);

					for (int ii=0; ii<indices.length; ii++) {
						indices[ii]--;
					}
					selectedNetworkList.setSelectedIndices(indices);

					updateUpDownButtonEnable();
					selectedNetworkList.repaint();
					unselectedNetworkList.repaint();
					updateOKButtonEnable();
					updateAttributeTable();
					updateMergeAttributeTable();
				}
			});
		udButtonPanel.add(upButton);

		downButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/down16.gif"))); // NOI18N
		downButton.setEnabled(false);
		downButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					int [] indices = selectedNetworkList.getSelectedIndices();
					if (indices == null || indices.length == 0) {
						return;
					}

					int imin = selectedNetworkList.getMinSelectionIndex();
					int imax = selectedNetworkList.getMaxSelectionIndex();

					if (imax>=selectedNetworkData.getSize()-1) return;

					CyNetwork removed = selectedNetworkData.removeElement(imax+1);
					selectedNetworkData.add(imin,removed);

					for (int ii=0; ii<indices.length; ii++) {
						indices[ii]++;
					}
					selectedNetworkList.setSelectedIndices(indices);

					updateUpDownButtonEnable();
					selectedNetworkList.repaint();
					unselectedNetworkList.repaint();
					updateOKButtonEnable();
					updateAttributeTable();
					updateMergeAttributeTable();
				}
			});
		udButtonPanel.add(downButton);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		selectNetworkPanel.add(udButtonPanel, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(selectNetworkPanel, gridBagConstraints);

		collapsiblePanelAgent.setLayout(new java.awt.BorderLayout());

		advancedPanel.setPreferredSize(new java.awt.Dimension(690, 400));
		advancedPanel.setLayout(new java.awt.GridBagLayout());

		attributePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Matching attributes (attributes to match nodes between networks)"));
		attributePanel.setLayout(new java.awt.GridBagLayout());

		attributeScrollPane.setMinimumSize(new java.awt.Dimension(100, 50));
		attributeScrollPane.setPreferredSize(new java.awt.Dimension(450, 50));
		matchNodeTable.getModel().addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					mergeNodeAttributeTable.updateMatchingAttribute();
				}
			});

		attributeScrollPane.setViewportView(matchNodeTable);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		attributePanel.add(attributeScrollPane, gridBagConstraints);

		idmappingCheckBox.setText("Map IDs between the matching attributes");
		idmappingCheckBox.setVisible(checkCyThesaurus);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		attributePanel.add(idmappingCheckBox, gridBagConstraints);

		idmappingLabel.setForeground(new java.awt.Color(255, 0, 51));
		idmappingLabel.setText("If you want to map identifiers between the matching attributes, please install CyThesaurus plugin version "+requiredCyThesaursServiceVersion+" or above.");
		idmappingLabel.setVisible(!checkCyThesaurus);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		attributePanel.add(idmappingLabel, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		advancedPanel.add(attributePanel, gridBagConstraints);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		advancedPanel.add(jSeparator3, gridBagConstraints);

		mergeAttributePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("How to merge attributes?"));
		mergeAttributePanel.setMinimumSize(new java.awt.Dimension(400, 200));
		mergeAttributePanel.setPreferredSize(new java.awt.Dimension(600, 200));
		mergeAttributePanel.setLayout(new javax.swing.BoxLayout(mergeAttributePanel, javax.swing.BoxLayout.LINE_AXIS));

		mergeAttributeTabbedPane.setMinimumSize(new java.awt.Dimension(450, 150));
		mergeAttributeTabbedPane.setPreferredSize(new java.awt.Dimension(450, 200));

		mergeNodeAttributePanel.setLayout(new javax.swing.BoxLayout(mergeNodeAttributePanel, javax.swing.BoxLayout.LINE_AXIS));

		mergeNodeAttributeTable = new MergeAttributeTable(nodeAttributeMapping,matchingAttribute);
		mergeNodeAttributeScrollPane.setViewportView(mergeNodeAttributeTable);

		mergeNodeAttributePanel.add(mergeNodeAttributeScrollPane);

		mergeAttributeTabbedPane.addTab("Node", mergeNodeAttributePanel);

		mergeEdgeAttributePanel.setLayout(new javax.swing.BoxLayout(mergeEdgeAttributePanel, javax.swing.BoxLayout.LINE_AXIS));

		mergeEdgeAttributeTable = new MergeAttributeTable(edgeAttributeMapping);
		mergeEdgeAttributeScrollPane.setViewportView(mergeEdgeAttributeTable);

		mergeEdgeAttributePanel.add(mergeEdgeAttributeScrollPane);

		mergeAttributeTabbedPane.addTab("Edge", mergeEdgeAttributePanel);

		mergeAttributePanel.add(mergeAttributeTabbedPane);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		advancedPanel.add(mergeAttributePanel, gridBagConstraints);

		advancedOptionCollapsiblePanelAgent.setLayout(new java.awt.BorderLayout());

		optionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

		inNetworkMergeCheckBox.setSelected(true);
		parameter = new NetworkMergeParameterImpl(inNetworkMergeCheckBox.isSelected());
		inNetworkMergeCheckBox.setText("Enable merging nodes/edges in the same network");
		inNetworkMergeCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					inNetworkMergeCheckBoxActionPerformed(evt);
				}
			});
		optionPanel.add(inNetworkMergeCheckBox);

		/*

		advancedOptionCollapsiblePanelAgent.add(optionPanel, java.awt.BorderLayout.PAGE_START);
		*/
		advancedOptionCollapsiblePanel.getContentPane().add(optionPanel, java.awt.BorderLayout.CENTER);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		advancedPanel.add(advancedOptionCollapsiblePanelAgent, gridBagConstraints);

		/*

		collapsiblePanelAgent.add(advancedPanel, java.awt.BorderLayout.CENTER);
		*/
		advancedNetworkMergeCollapsiblePanel.getContentPane().add(advancedPanel, java.awt.BorderLayout.CENTER);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(collapsiblePanelAgent, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(jSeparator4, gridBagConstraints);

		okPanel.setDoubleBuffered(false);
		okPanel.setLayout(new javax.swing.BoxLayout(okPanel, javax.swing.BoxLayout.LINE_AXIS));

		okButton.setText(" Merge ");
		okButton.setEnabled(false);
		okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					okButtonActionPerformed(evt);
				}
			});
		okPanel.add(okButton);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					setVisible(false);
					dispose();
				}
			});
		okPanel.add(cancelButton);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(okPanel, gridBagConstraints);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
		//this.setAlwaysOnTop(false);
		if (this.advancedNetworkMergeCollapsiblePanel.isCollapsed()) {
                        if (getOperation() == Operation.UNION) {
                                GraphSetUtils.createUnionGraph(this.selectedNetworkData.getNetworkList(), true,
							       CyNetworkNaming.getSuggestedNetworkTitle("Union"));
                        } else if (getOperation() == Operation.INTERSECTION) {
                                GraphSetUtils.createIntersectionGraph(this.selectedNetworkData.getNetworkList(), true,
								      CyNetworkNaming.getSuggestedNetworkTitle("Intersection"));
                        } else if (getOperation() == Operation.DIFFERENCE) {
				if (altDifferenceIsChecked)
					GraphSetUtils.createDifferenceGraph2(this.selectedNetworkData.getNetworkList(), true,
									     CyNetworkNaming.getSuggestedNetworkTitle("Difference"));
				else
					GraphSetUtils.createDifferenceGraph(this.selectedNetworkData.getNetworkList(), true,
									    CyNetworkNaming.getSuggestedNetworkTitle("Difference"));
                        }

                } else {
                        if (this.idmappingCheckBox.isSelected()) {
				Map<String,Set<String>> selectedNetworkAttribute = new HashMap<String,Set<String>>();
				Iterator<Map.Entry<String,String>> itEntry = matchingAttribute.getNetAttrMap().entrySet().iterator();
				while (itEntry.hasNext()) {
					Map.Entry<String,String> entry = itEntry.next();
					String netID = entry.getKey();
					String attr = entry.getValue();
					Set<String> attrs = new HashSet<String>(1);
					attrs.add(attr);
					selectedNetworkAttribute.put(netID,attrs);
				}

				boolean isFrameAlwaysOnTop = frame.isAlwaysOnTop();
				frame.setAlwaysOnTop(false);

				final boolean isNode = true;
				org.cytoscape.network.merge.internal.ui.IDMappingDialog dialog = new org.cytoscape.network.merge.internal.ui.IDMappingDialog(frame,true,selectedNetworkAttribute,isNode);
				dialog.setLocationRelativeTo(frame);
				dialog.setVisible(true);
				dialog.setTgtType(tgtType);
				if (!dialog.isCancelled()) {
					selectedNetworkAttributeIDType = dialog.getSrcTypes();
					tgtType = dialog.getTgtType();
				} else {
					int ret = JOptionPane.showConfirmDialog(this, "Error: you have not configured how to mapping the attributes." +
										"\nMerge network without mapping IDs?", "No ID mapping", JOptionPane.YES_NO_OPTION);
					if (ret==JOptionPane.NO_OPTION) {
						frame.setAlwaysOnTop(isFrameAlwaysOnTop);
						return;
					}
				}
				frame.setAlwaysOnTop(isFrameAlwaysOnTop);
                        }

                        AttributeConflictCollector conflictCollector = new AttributeConflictCollectorImpl();

                        // network merge task
                        NetworkMergeSessionTask nmTask = new NetworkMergeSessionTask(
										     this.parameter,
										     this.matchingAttribute,
										     this.nodeAttributeMapping,
										     this.edgeAttributeMapping,
										     this.selectedNetworkData.getNetworkList(),
										     getOperation(),
										     mergeNodeAttributeTable.getMergedNetworkName(),
										     conflictCollector,
										     selectedNetworkAttributeIDType,
										     tgtType);

                        // Configure JTask Dialog Pop-Up Box
                        final JTaskConfig jTaskConfig = new JTaskConfig();
                        jTaskConfig.setOwner(this);
                        jTaskConfig.displayCloseButton(true);
                        jTaskConfig.displayCancelButton(true);
                        jTaskConfig.displayStatus(true);
                        jTaskConfig.setAutoDispose(false);
                        jTaskConfig.displayTimeElapsed(true);                        
                        
                        // Execute Task in New Thread; pop open JTask Dialog Box.
                        TaskManager.executeTask(nmTask, jTaskConfig);
                        if (nmTask.isCancelled()) return;

                        // conflict handling task
                        if (!conflictCollector.isEmpty()) {
                                HandleConflictsTask hcTask = new HandleConflictsTask(conflictCollector);
                                TaskManager.executeTask(hcTask, jTaskConfig);
                        }

                }

		setVisible(false);
		dispose();
	}//GEN-LAST:event_okButtonActionPerformed

	private void inNetworkMergeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inNetworkMergeCheckBoxActionPerformed
		parameter.enableInNetworkMerge(inNetworkMergeCheckBox.isSelected());
		this.updateOKButtonEnable();
	}//GEN-LAST:event_inNetworkMergeCheckBoxActionPerformed

	private double requiredCyThesaursServiceVersion = 1.01;
	private boolean checkCyThesaurus() {
		CyThesaurusServiceClient client = new CyThesaurusServiceMessageBasedClient("AdvanceNetworkMerge");
		if (!client.isServiceAvailable())
			return false;

		double version = client.serviceVersion();
		return version >= requiredCyThesaursServiceVersion;
	}
	/*
	 * Call when adding or removing a network to/from selected network list
	 * 
	 */
	private void addRemoveAttributeMapping(CyNetwork network, boolean isAdd) {
		final String netID = network.getIdentifier();
    
		if (isAdd) {
			nodeAttributeMapping.addNetwork(netID);
			edgeAttributeMapping.addNetwork(netID);
			matchingAttribute.addNetwork(netID);
		} else {
			nodeAttributeMapping.removeNetwork(netID);
			edgeAttributeMapping.removeNetwork(netID);
			matchingAttribute.removeNetwork(netID);
		}
	}

	private void updateOKButtonEnable() {
		int n = !advancedNetworkMergeCollapsiblePanel.isCollapsed()&&parameter.inNetworkMergeEnabled()&&getOperation()==Operation.UNION?1:2;

		if (selectedNetworkData.getSize()<n) {
			okButton.setToolTipText("Select at least "+n+" networks to merge");
			okButton.setEnabled(false);
			return;
		}    
    
		okButton.setToolTipText(null);
		okButton.setEnabled(true);
	}

	private void updateUpDownButtonEnable() {
		int imin = selectedNetworkList.getMinSelectionIndex();
		int imax = selectedNetworkList.getMaxSelectionIndex();
		upButton.setEnabled(0<imin&&imax<=selectedNetworkData.getSize()-1);
		downButton.setEnabled(0<=imin&&imax<selectedNetworkData.getSize()-1);
	}

	private void updateAttributeTable() {
		matchNodeTable.fireTableStructureChanged();
	}


	private void updateMergeAttributeTable() {
		mergeNodeAttributeTable.fireTableStructureChanged();
		mergeEdgeAttributeTable.fireTableStructureChanged();
	}

	private void updateSize() {
		Dimension dim;
		if (advancedNetworkMergeCollapsiblePanel.isCollapsed()) {
			if (getOperation() != Operation.DIFFERENCE) {
				dim = new Dimension(500,380);
			} else {
				dim = new Dimension(500,450);
			}
		} else {
			if (frame.getExtendedState()==Frame.MAXIMIZED_BOTH) {
                                return;
                        }

                        Dimension dim_curr = frame.getSize(); // current dim
                        int width_curr = dim_curr.width;
                        int height_curr = dim_curr.height;

                        int width = 700;
                        int height = getOperation()==Operation.DIFFERENCE?800:720;

                        if (width < width_curr) {
                                width = width_curr;
                        }

                        if (height < height_curr) {
                                height = height_curr;
                        }

			dim = new Dimension(width,height);
		}

		frame.setSize(dim);
	}

	/*
	 * Get currently selected operation
	 * 
	 */
	private Operation getOperation() {
		return this.selectedOperation;
	}


	private MergeAttributeTable mergeNodeAttributeTable;
	private MergeAttributeTable mergeEdgeAttributeTable;
	private MatchNodeTable matchNodeTable;
	private AttributeMapping nodeAttributeMapping;
	private AttributeMapping edgeAttributeMapping;
	private MatchingAttribute matchingAttribute;
	private Map<String,Map<String,Set<String>>> selectedNetworkAttributeIDType;
	private String tgtType;

	private Frame frame;

	private final ImageIcon UNION_ICON = new ImageIcon(getClass().getResource("/images/union.png"));
	private final ImageIcon INTERSECTION_ICON = new ImageIcon(getClass().getResource("/images/intersection.png"));
	private final ImageIcon DIFFERENCE_ICON = new ImageIcon(getClass().getResource("/images/difference.png"));
	private final ImageIcon DIFFERENCE2_ICON = new ImageIcon(getClass().getResource("/images/difference2.png"));
	private final ImageIcon[] OPERATION_ICONS =  { UNION_ICON, INTERSECTION_ICON, DIFFERENCE_ICON };

	private CollapsiblePanel advancedNetworkMergeCollapsiblePanel;
	private CollapsiblePanel advancedOptionCollapsiblePanel;

	private NetworkMergeParameter parameter;
    
	boolean checkCyThesaurus;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel advancedOptionCollapsiblePanelAgent;
	private javax.swing.JPanel advancedPanel;
	private javax.swing.JPanel attributePanel;
	private javax.swing.JScrollPane attributeScrollPane;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel collapsiblePanelAgent;
	private javax.swing.JButton downButton;
	private javax.swing.JCheckBox idmappingCheckBox;
	private javax.swing.JCheckBox inNetworkMergeCheckBox;
	private javax.swing.JButton leftButton;
	private javax.swing.JButton okButton;
	private javax.swing.JComboBox operationComboBox;
	private Operation selectedOperation;
	private javax.swing.JLabel operationIcon;
	private javax.swing.JRadioButton differenceButton;
	private javax.swing.JRadioButton difference2Button;
	private javax.swing.ButtonGroup differenceGroup;
	private javax.swing.JPanel optionPanel;
	private javax.swing.JButton rightButton;
	private javax.swing.JList selectedNetworkList;
	private NetworkListModel selectedNetworkData;
	private javax.swing.JPanel udButtonPanel;
	private javax.swing.JList unselectedNetworkList;
	private SortedNetworkListModel unselectedNetworkData;
	private javax.swing.JButton upButton;
	// End of variables declaration//GEN-END:variables

}

class NetworkListModel extends AbstractListModel {
        Vector<CyNetwork> model;
        
        public NetworkListModel() {
		model= new Vector<CyNetwork>();
        }

        //@Override
        public int getSize() {
		return model.size();
        }

        //@Override
        public CyNetwork getElementAt(int index) {
		return model.get(index);
        }

        public void add(CyNetwork network) {
		model.add(network);
		fireContentsChanged(this, 0, getSize());
        }
        
        public void add(int index, CyNetwork network) {
		model.add(index,network);
		fireContentsChanged(this, 0, getSize());
        }

        public CyNetwork removeElement(int index) {
		CyNetwork removed = model.remove(index);
		if (removed!=null) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;   
        }
        
        
        public List<CyNetwork> getNetworkList() {
		return model;
        }
}

class SortedNetworkListModel extends AbstractListModel {
        // Using a SortedMap from String to network
        TreeMap<String,CyNetwork> model;
        
        public SortedNetworkListModel() {
		model= new TreeMap<String,CyNetwork>();
        }

        //@Override
        public int getSize() {
		return model.size();
        }

        //@Override
        public CyNetwork getElementAt(int index) {
		return (CyNetwork) model.values().toArray()[index];
        }

        public void add(CyNetwork network) {
		String title = network.getTitle();
		model.put(title.toUpperCase(),network);
		fireContentsChanged(this, 0, getSize());
        }

        public CyNetwork removeElement(int index) {
		CyNetwork removed = model.remove(getElementAt(index).getTitle().toUpperCase());
		if (removed!=null) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;   
        }
        
        public List<CyNetwork> getNetworkList() {
		return new Vector<CyNetwork>(model.values());
        }
}

class NetworkMergeSessionTask implements Task {
	final NetworkMergeParameter parameter;
	private MatchingAttribute matchingAttribute;
	private AttributeMapping nodeAttributeMapping;
	private AttributeMapping edgeAttributeMapping;
	private List<CyNetwork> selectedNetworkList;
	private Operation operation;
	private String mergedNetworkName;
	private AttributeConflictCollector conflictCollector;
	private Map<String,Map<String,Set<String>>> selectedNetworkAttributeIDType;
	private final String tgtType;
	final private AttributeBasedNetworkMerge networkMerge ;
	private TaskMonitor taskMonitor;    
	private boolean cancelled;

	/**
	 * Constructor.<br>
	 *
	 */
	NetworkMergeSessionTask( final NetworkMergeParameter parameter,
				 final MatchingAttribute matchingAttribute,
				 final AttributeMapping nodeAttributeMapping,
				 final AttributeMapping edgeAttributeMapping,
				 final List<CyNetwork> selectedNetworkList,
				 final Operation operation,
				 final String mergedNetworkName,
				 final AttributeConflictCollector conflictCollector,
				 final Map<String,Map<String,Set<String>>> selectedNetworkAttributeIDType,
				 final String tgtType) {
		this.parameter = parameter;
		this.matchingAttribute = matchingAttribute;
		this.nodeAttributeMapping = nodeAttributeMapping;
		this.edgeAttributeMapping = edgeAttributeMapping;
		this.selectedNetworkList = selectedNetworkList;
		this.operation = operation;
		this.mergedNetworkName = mergedNetworkName;
		this.conflictCollector = conflictCollector;
		this.selectedNetworkAttributeIDType = selectedNetworkAttributeIDType;
		this.tgtType = tgtType;
		cancelled = false;        
        
		final AttributeValueMatcher attributeValueMatcher;
		final AttributeMerger attributeMerger;
		//if (idMapping==null) {
                attributeValueMatcher = new DefaultAttributeValueMatcher();
                attributeMerger = new DefaultAttributeMerger(conflictCollector);
		//        } else {
		//                attributeValueMatcher = new IDMappingAttributeValueMatcher(idMapping);
		//                attributeMerger = new IDMappingAttributeMerger(conflictCollector,idMapping,tgtType);
		//        }

		networkMerge = new AttributeBasedNetworkMerge(
							      parameter,
							      matchingAttribute,
							      nodeAttributeMapping,
							      edgeAttributeMapping,
							      attributeMerger,
							      attributeValueMatcher);
                
	}
    
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Executes Task
	 *
	 * @throws
	 * @throws Exception
	 */
	//@Override
	public void run() {

		try {  
			networkMerge.setTaskMonitor(taskMonitor);

			if (selectedNetworkAttributeIDType!=null) {
				taskMonitor.setStatus("Mapping IDs...");
				taskMonitor.setPercentCompleted(-1);
				CyThesaurusServiceClient client = new CyThesaurusServiceMessageBasedClient("AdvanceNetworkMerge");
				if (!client.isServiceAvailable()) {
					taskMonitor.setStatus("CyThesaurs service is not available.");
					taskMonitor.setPercentCompleted(100);
					return;
				}

				defineTgtAttributes();

				String mergedAttr = nodeAttributeMapping.getMergedAttribute(0);
				for (String net : selectedNetworkAttributeIDType.keySet()) {
					final Set<String> nets = new HashSet<String>(1);
					nets.add(net);
					Map<String,Set<String>> mapAttrTypes = selectedNetworkAttributeIDType.get(net);
					for (String attr : mapAttrTypes.keySet()) {
						Set<String> types = mapAttrTypes.get(attr);
						if (!client.mapID(nets, attr, mergedAttr, types, tgtType)) {
							taskMonitor.setStatus("Failed to map IDs.");
							taskMonitor.setPercentCompleted(100);
							return;
						}
					}
				}

				matchingAttribute.clear();
				for (String net : selectedNetworkAttributeIDType.keySet()) {
					matchingAttribute.putAttributeForMatching(net, mergedAttr);
					nodeAttributeMapping.setOriginalAttribute(net, mergedAttr, 0);
				}

			}

			CyNetwork mergedNetwork = networkMerge.mergeNetwork(
									    selectedNetworkList,
									    operation,
									    mergedNetworkName);            

			/*
			  cytoscape.view.CyNetworkView networkView = Cytoscape.getNetworkView(mergedNetworkName);

			  // get the VisualMappingManager and CalculatorCatalog
			  cytoscape.visual.VisualMappingManager manager = Cytoscape.getVisualMappingManager();
			  cytoscape.visual.CalculatorCatalog catalog = manager.getCalculatorCatalog();

			  cytoscape.visual.VisualStyle vs = catalog.getVisualStyle(mergedNetworkName+" Visual Style");
			  if (vs == null) {
			  // if not, create it and add it to the catalog
			  //vs = createVisualStyle(networkMerge);
			  cytoscape.visual.NodeAppearanceCalculator nodeAppCalc = new cytoscape.visual.NodeAppearanceCalculator();
			  cytoscape.visual.mappings.PassThroughMapping pm = new cytoscape.visual.mappings.PassThroughMapping(new String(), cytoscape.data.Semantics.CANONICAL_NAME);

			  cytoscape.visual.calculators.Calculator nlc = new cytoscape.visual.calculators.BasicCalculator(null,
			  pm, cytoscape.visual.VisualPropertyType.NODE_LABEL);
			  nodeAppCalc.setCalculator(nlc);

			  vs.setNodeAppearanceCalculator(nodeAppCalc);

			  catalog.addVisualStyle(vs);
			  }
			  // actually apply the visual style
			  manager.setVisualStyle(vs);
			  networkView.redrawGraph(true,true);
			*/

			//            taskMonitor.setPercentCompleted(100);
			//            taskMonitor.setStatus("The selected networks were successfully merged into network '"
			//                                  + mergedNetwork.getTitle()
			//                                  + "' with "
			//                                  + conflictCollector.getMapToIDAttr().size()
			//                                  + " attribute conflicts.");

		} catch(Exception e) {
			taskMonitor.setException(e, "Network Merge Failed!");
			e.printStackTrace();
		}
        
	}

	private void defineTgtAttributes() {
		CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
		List<String> attrNames = java.util.Arrays.asList(nodeAttributes.getAttributeNames());

		MultiHashMapDefinition mmapDef = nodeAttributes.getMultiHashMapDefinition();

		String mergedAttr = nodeAttributeMapping.getMergedAttribute(0);
		if (attrNames.contains(mergedAttr)) {
			// for existing attribute, check if its type is String or List
			byte attrType = nodeAttributes.getType(mergedAttr);
			if (attrType!=CyAttributes.TYPE_STRING && attrType!=CyAttributes.TYPE_SIMPLE_LIST) {
				throw new java.lang.UnsupportedOperationException("Only String and List target attributes are supported.");
			}
		} else {
			byte attrType = nodeAttributeMapping.getMergedAttributeType(0);

			if (attrType==CyAttributes.TYPE_SIMPLE_LIST) {
				// define the new attribute as List
				byte[] keyTypes = new byte[] { MultiHashMapDefinition.TYPE_INTEGER };
				mmapDef.defineAttribute(mergedAttr,
							MultiHashMapDefinition.TYPE_STRING,
							keyTypes);
			} else {
				mmapDef.defineAttribute(mergedAttr,
							MultiHashMapDefinition.TYPE_STRING,
							null);
			}
		}
	}

	/**
	 * Halts the Task: Not Currently Implemented.
	 */
	//@Override
	public void halt() {
		cancelled = true;
		networkMerge.interrupt();            
	}

	/**
	 * Sets the Task Monitor.
	 *
	 * @param taskMonitor
	 *            TaskMonitor Object.
	 */
	//@Override
	public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Gets the Task Title.
	 *
	 * @return Task Title.
	 */
	//@Override
	public String getTitle() {
		return "Merging networks";
	}
}

class HandleConflictsTask implements Task {
	private AttributeConflictCollector conflictCollector;

	private TaskMonitor taskMonitor;

	/**
	 * Constructor.<br>
	 *
	 */
	HandleConflictsTask(final AttributeConflictCollector conflictCollector) {
		this.conflictCollector = conflictCollector;
	}

	/**
	 * Executes Task
	 *
	 * @throws
	 * @throws Exception
	 */
	//@Override
	public void run() {
		taskMonitor.setStatus("Handle conflicts.\n\nIt may take a while.\nPlease wait...");
		taskMonitor.setPercentCompleted(0);

		try {
			int nBefore = conflictCollector.getMapToIDAttr().size();

			List<AttributeConflictHandler> conflictHandlers = new Vector<AttributeConflictHandler>();

			AttributeConflictHandler conflictHandler;

			//             if (idMapping!=null) {
			//                conflictHandler = new IDMappingAttributeConflictHandler(idMapping);
			//                conflictHandlers.add(conflictHandler);
			//             }

			conflictHandler = new DefaultAttributeConflictHandler();
			conflictHandlers.add(conflictHandler);

			AttributeConflictManager conflictManager = new AttributeConflictManager(conflictCollector,conflictHandlers);
			conflictManager.handleConflicts();

			int nAfter = conflictCollector.getMapToIDAttr().size();

			taskMonitor.setPercentCompleted(100);
			taskMonitor.setStatus("Successfully handled " + (nBefore-nAfter) + " attribute conflicts. "
					      + nAfter+" conflicts remains.");
		} catch(Exception e) {
			//taskMonitor.setPercentCompleted(100);
			//taskMonitor.setStatus("Conflict handle Failed!");
			taskMonitor.setException(e, "Conflict handle Failed!");
			e.printStackTrace();
		}

	}

	/**
	 * Halts the Task: Not Currently Implemented.
	 */
	//@Override
	public void halt() {
		// Task can not currently be halted.
		taskMonitor.setPercentCompleted(100);
		taskMonitor.setStatus("Failed!!!");
	}

	/**
	 * Sets the Task Monitor.
	 *
	 * @param taskMonitor
	 *            TaskMonitor Object.
	 */
	//@Override
	public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Gets the Task Title.
	 *
	 * @return Task Title.
	 */
	//@Override
	public String getTitle() {
		return "Merging networks";
	}
}
