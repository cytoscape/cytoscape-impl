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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.network.merge.internal.NetworkMerge.Operation;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollector;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollectorImpl;
import org.cytoscape.network.merge.internal.model.AttributeMapping;
import org.cytoscape.network.merge.internal.model.AttributeMappingImpl;
import org.cytoscape.network.merge.internal.model.MatchingAttribute;
import org.cytoscape.network.merge.internal.model.MatchingAttributeImpl;
import org.cytoscape.network.merge.internal.task.NetworkMergeTask;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.task.creation.NetworkViewCreator;

/**
 *
 * Main frame for advance network merge
 */
public class NetworkMergeFrame extends JFrame {
	private boolean altDifferenceIsChecked = false;
        private CyNetworkManager cnm;
        private CyNetworkFactory cnf;
        private CyNetworkNaming cnn;
        private TaskManager taskManager;
        private NetworkViewCreator netViewCreator;

	/** Creates new form NetworkMergeFrame */
	public NetworkMergeFrame(CyNetworkManager cnm, 
                        CyNetworkFactory cnf,
                        CyNetworkNaming cnn,
                        TaskManager taskManager,
						NetworkViewCreator netViewCreator) {
		frame = this;
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                
                this.cnm = cnm;
                this.cnf = cnf;
                this.cnn = cnn;
                this.taskManager = taskManager;

		checkCyThesaurus = checkCyThesaurus();

		selectedNetworkAttributeIDType = null;
		tgtType = null;
		matchingAttribute = new MatchingAttributeImpl();
		nodeAttributeMapping = new AttributeMappingImpl();
		edgeAttributeMapping = new AttributeMappingImpl();

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
		GridBagConstraints gridBagConstraints;

		JPanel operationPanel = new JPanel();
		JLabel operationLabel = new JLabel();
		operationComboBox = new JComboBox();
		operationIcon = new JLabel();
		differenceButton = new JRadioButton("Only remove nodes if all their edges are being subtracted, too.");
		difference2Button = new JRadioButton("Remove all nodes that are in the 2nd network.");
		differenceGroup = new ButtonGroup();
		differenceGroup.add(differenceButton);
		differenceGroup.add(difference2Button);
		JSeparator jSeparator1 = new JSeparator();
		JPanel selectNetworkPanel = new JPanel();
		JScrollPane unselectedNetworkScrollPane = new JScrollPane();
		unselectedNetworkData = new SortedNetworkListModel();
		unselectedNetworkList = new JList(unselectedNetworkData);
		JPanel lrButtonPanel = new JPanel();
		rightButton = new JButton();
		leftButton = new JButton();
		JScrollPane selectedNetworkScrollPane = new JScrollPane();
		selectedNetworkData = new NetworkListModel();
		selectedNetworkList = new JList(selectedNetworkData);
		udButtonPanel = new JPanel();
		upButton = new JButton();
		downButton = new JButton();
		collapsiblePanelAgent = advancedNetworkMergeCollapsiblePanel;
		advancedPanel = new JPanel();
		attributePanel = new JPanel();
		matchNodeTable = new MatchNodeTable(matchingAttribute);
		attributeScrollPane = new JScrollPane();
		idmappingCheckBox = new JCheckBox();
		JLabel idmappingLabel = new JLabel();
		JSeparator jSeparator3 = new JSeparator();
		JPanel mergeAttributePanel = new JPanel();
		JTabbedPane mergeAttributeTabbedPane = new JTabbedPane();
		JPanel mergeNodeAttributePanel = new JPanel();
		JScrollPane mergeNodeAttributeScrollPane = new JScrollPane();
		JPanel mergeEdgeAttributePanel = new JPanel();
		JScrollPane mergeEdgeAttributeScrollPane = new JScrollPane();
		advancedOptionCollapsiblePanelAgent = advancedOptionCollapsiblePanel;
		optionPanel = new JPanel();
		inNetworkMergeCheckBox = new JCheckBox();
		JSeparator jSeparator4 = new JSeparator();
		JPanel okPanel = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		setTitle("Advanced Network Merge");
		getContentPane().setLayout(new GridBagLayout());

		operationPanel.setMinimumSize(new Dimension(211, 20));
		operationPanel.setLayout(new BoxLayout(operationPanel, BoxLayout.LINE_AXIS));

		operationLabel.setText("Operation:   ");
		operationPanel.add(operationLabel);

		operationComboBox.setModel(new DefaultComboBoxModel(new Operation[] { Operation.UNION,Operation.INTERSECTION,Operation.DIFFERENCE }));
		selectedOperation = (Operation) operationComboBox.getSelectedItem();
		operationComboBox.setPreferredSize(new Dimension(150, 20));
		operationComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
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
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(operationPanel, gridBagConstraints);

		operationIcon.setIcon(UNION_ICON);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(operationIcon, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(differenceButton, gridBagConstraints);
		differenceButton.setVisible(false);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(difference2Button, gridBagConstraints);
		difference2Button.setVisible(false);
		difference2Button.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					altDifferenceIsChecked = evt.getStateChange() == ItemEvent.SELECTED;
					if (altDifferenceIsChecked)
						operationIcon.setIcon(DIFFERENCE2_ICON);
					else
						operationIcon.setIcon(DIFFERENCE_ICON);
				}
			});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(jSeparator1, gridBagConstraints);

		selectNetworkPanel.setBorder(BorderFactory.createTitledBorder("Networks to merge"));
		selectNetworkPanel.setMinimumSize(new Dimension(490, 100));
		selectNetworkPanel.setPreferredSize(new Dimension(490, 130));
		selectNetworkPanel.setLayout(new GridBagLayout());

		unselectedNetworkScrollPane.setPreferredSize(new Dimension(200, 100));

		unselectedNetworkList.setBorder(BorderFactory.createTitledBorder("Available networks"));
		for (CyNetwork network : cnm.getNetworkSet()) {
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
					renderer.setText(((CyNetwork)value).toString()); //TODO: network title
					return renderer;
				}
			});

		unselectedNetworkList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent evt) {
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

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		selectNetworkPanel.add(unselectedNetworkScrollPane, gridBagConstraints);

		lrButtonPanel.setLayout(new GridLayout(0, 1, 0, 2));

		rightButton.setIcon(new ImageIcon(getClass().getResource("/images/right16.gif"))); // NOI18N
		rightButton.setEnabled(false);
		rightButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
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

		leftButton.setIcon(new ImageIcon(getClass().getResource("/images/left16.gif"))); // NOI18N
		leftButton.setEnabled(false);
		leftButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
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

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		selectNetworkPanel.add(lrButtonPanel, gridBagConstraints);

		selectedNetworkScrollPane.setPreferredSize(new Dimension(200, 100));

		selectedNetworkList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		selectedNetworkList.setBorder(BorderFactory.createTitledBorder("Selected networks"));
		selectedNetworkList.setCellRenderer(new ListCellRenderer() {
				private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
				public Component getListCellRendererComponent(
									      JList list,
									      Object value,
									      int index,
									      boolean isSelected,
									      boolean cellHasFocus) {
					JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					renderer.setText(((CyNetwork)value).toString()); //TODO: network title
					return renderer;
				}
			});
		selectedNetworkList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent evt) {
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

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		selectNetworkPanel.add(selectedNetworkScrollPane, gridBagConstraints);

		udButtonPanel.setLayout(new GridLayout(0, 1, 0, 2));
		udButtonPanel.setVisible(false);

		upButton.setIcon(new ImageIcon(getClass().getResource("/images/up16.gif"))); // NOI18N
		upButton.setEnabled(false);
		upButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
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

		downButton.setIcon(new ImageIcon(getClass().getResource("/images/down16.gif"))); // NOI18N
		downButton.setEnabled(false);
		downButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
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

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		selectNetworkPanel.add(udButtonPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(selectNetworkPanel, gridBagConstraints);

		collapsiblePanelAgent.setLayout(new BorderLayout());

		advancedPanel.setPreferredSize(new Dimension(690, 400));
		advancedPanel.setLayout(new GridBagLayout());

		attributePanel.setBorder(BorderFactory.createTitledBorder("Matching attributes (attributes to match nodes between networks)"));
		attributePanel.setLayout(new GridBagLayout());

		attributeScrollPane.setMinimumSize(new Dimension(100, 50));
		attributeScrollPane.setPreferredSize(new Dimension(450, 50));
		matchNodeTable.getModel().addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					mergeNodeAttributeTable.updateMatchingAttribute();
				}
			});

		attributeScrollPane.setViewportView(matchNodeTable);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		attributePanel.add(attributeScrollPane, gridBagConstraints);

		idmappingCheckBox.setText("Map IDs between the matching attributes");
		//idmappingCheckBox.setVisible(checkCyThesaurus);
                idmappingCheckBox.setVisible(false);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		attributePanel.add(idmappingCheckBox, gridBagConstraints);

		idmappingLabel.setForeground(new Color(255, 0, 51));
		idmappingLabel.setText("If you want to map identifiers between the matching attributes, please install CyThesaurus app version "+requiredCyThesaursServiceVersion+" or above.");
		//idmappingLabel.setVisible(!checkCyThesaurus);
                idmappingLabel.setVisible(false);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		attributePanel.add(idmappingLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		advancedPanel.add(attributePanel, gridBagConstraints);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		advancedPanel.add(jSeparator3, gridBagConstraints);

		mergeAttributePanel.setBorder(BorderFactory.createTitledBorder("How to merge attributes?"));
		mergeAttributePanel.setMinimumSize(new Dimension(400, 200));
		mergeAttributePanel.setPreferredSize(new Dimension(600, 200));
		mergeAttributePanel.setLayout(new BoxLayout(mergeAttributePanel, BoxLayout.LINE_AXIS));

		mergeAttributeTabbedPane.setMinimumSize(new Dimension(450, 150));
		mergeAttributeTabbedPane.setPreferredSize(new Dimension(450, 200));

		mergeNodeAttributePanel.setLayout(new BoxLayout(mergeNodeAttributePanel, BoxLayout.LINE_AXIS));

		mergeNodeAttributeTable = new MergeAttributeTable(nodeAttributeMapping,matchingAttribute);
		mergeNodeAttributeScrollPane.setViewportView(mergeNodeAttributeTable);

		mergeNodeAttributePanel.add(mergeNodeAttributeScrollPane);

		mergeAttributeTabbedPane.addTab("Node", mergeNodeAttributePanel);

		mergeEdgeAttributePanel.setLayout(new BoxLayout(mergeEdgeAttributePanel, BoxLayout.LINE_AXIS));

		mergeEdgeAttributeTable = new MergeAttributeTable(edgeAttributeMapping);
		mergeEdgeAttributeScrollPane.setViewportView(mergeEdgeAttributeTable);

		mergeEdgeAttributePanel.add(mergeEdgeAttributeScrollPane);

		mergeAttributeTabbedPane.addTab("Edge", mergeEdgeAttributePanel);

		mergeAttributePanel.add(mergeAttributeTabbedPane);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		advancedPanel.add(mergeAttributePanel, gridBagConstraints);

		advancedOptionCollapsiblePanelAgent.setLayout(new BorderLayout());

		optionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		inNetworkMergeCheckBox.setSelected(true);
		inNetworkMergeCheckBox.setText("Enable merging nodes/edges in the same network");
		inNetworkMergeCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					inNetworkMergeCheckBoxActionPerformed(evt);
				}
			});
		optionPanel.add(inNetworkMergeCheckBox);

		/*

		advancedOptionCollapsiblePanelAgent.add(optionPanel, BorderLayout.PAGE_START);
		*/
		advancedOptionCollapsiblePanel.getContentPane().add(optionPanel, BorderLayout.CENTER);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		advancedPanel.add(advancedOptionCollapsiblePanelAgent, gridBagConstraints);

		/*

		collapsiblePanelAgent.add(advancedPanel, BorderLayout.CENTER);
		*/
		advancedNetworkMergeCollapsiblePanel.getContentPane().add(advancedPanel, BorderLayout.CENTER);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(collapsiblePanelAgent, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(jSeparator4, gridBagConstraints);

		okPanel.setDoubleBuffered(false);
		okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.LINE_AXIS));

		okButton.setText(" Merge ");
		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					okButtonActionPerformed(evt);
				}
			});
		okPanel.add(okButton);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setVisible(false);
					dispose();
				}
			});
		okPanel.add(cancelButton);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = contentPaneGridY++;
		gridBagConstraints.anchor = GridBagConstraints.LINE_END;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(okPanel, gridBagConstraints);

		pack();
	}// </editor-fold>                        

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
		//this.setAlwaysOnTop(false);
//		if (this.advancedNetworkMergeCollapsiblePanel.isCollapsed()) {
//                        if (getOperation() == Operation.UNION) {
//                                GraphSetUtils.createUnionGraph(this.selectedNetworkData.getNetworkList(), true,
//							       CyNetworkNaming.getSuggestedNetworkTitle("Union"));
//                        } else if (getOperation() == Operation.INTERSECTION) {
//                                GraphSetUtils.createIntersectionGraph(this.selectedNetworkData.getNetworkList(), true,
//								      CyNetworkNaming.getSuggestedNetworkTitle("Intersection"));
//                        } else if (getOperation() == Operation.DIFFERENCE) {
//				if (altDifferenceIsChecked)
//					GraphSetUtils.createDifferenceGraph2(this.selectedNetworkData.getNetworkList(), true,
//									     CyNetworkNaming.getSuggestedNetworkTitle("Difference"));
//				else
//					GraphSetUtils.createDifferenceGraph(this.selectedNetworkData.getNetworkList(), true,
//									    CyNetworkNaming.getSuggestedNetworkTitle("Difference"));
//                        }
//
//                } else {
                        if (this.idmappingCheckBox.isSelected()) {
//				Map<String,Set<String>> selectedNetworkAttribute = new HashMap<String,Set<String>>();
//				for (Map.Entry<String,String> entry : matchingAttribute.getNetAttrMap().entrySet()) {
//					String netID = entry.getKey();
//					String attr = entry.getValue();
//					Set<String> attrs = new HashSet<String>(1);
//					attrs.add(attr);
//					selectedNetworkAttribute.put(netID,attrs);
//				}
//
//				boolean isFrameAlwaysOnTop = frame.isAlwaysOnTop();
//				frame.setAlwaysOnTop(false);
//
//				final boolean isNode = true;
//				IDMappingDialog dialog = new IDMappingDialog(frame,true,selectedNetworkAttribute,isNode);
//				dialog.setLocationRelativeTo(frame);
//				dialog.setVisible(true);
//				dialog.setTgtType(tgtType);
//				if (!dialog.isCancelled()) {
//					selectedNetworkAttributeIDType = dialog.getSrcTypes();
//					tgtType = dialog.getTgtType();
//				} else {
//					int ret = JOptionPane.showConfirmDialog(this, "Error: you have not configured how to mapping the attributes." +
//										"\nMerge network without mapping IDs?", "No ID mapping", JOptionPane.YES_NO_OPTION);
//					if (ret==JOptionPane.NO_OPTION) {
//						frame.setAlwaysOnTop(isFrameAlwaysOnTop);
//						return;
//					}
//				}
//				frame.setAlwaysOnTop(isFrameAlwaysOnTop);
                        }
                        
                        CyNetwork net = cnf.createNetwork();
                        String netName = cnn.getSuggestedNetworkTitle(mergeNodeAttributeTable.getMergedNetworkName());
						net.getRow(net).set(CyTableEntry.NAME,netName);
                        cnm.addNetwork(net);
                        
                        AttributeConflictCollector conflictCollector = new AttributeConflictCollectorImpl();

                        // network merge task
                        NetworkMergeTask nmTask = new NetworkMergeTask(net,
                                 this.matchingAttribute,
                                 this.nodeAttributeMapping,
                                 this.edgeAttributeMapping,
                                 this.selectedNetworkData.getNetworkList(),
                                 getOperation(),
                                 conflictCollector,
                                 selectedNetworkAttributeIDType,
                                 tgtType,
                                 this.inNetworkMergeCheckBox.isSelected(),
								 netViewCreator);

						TaskIterator ti = new TaskIterator(2,nmTask);

                        
                        // Execute Task in New Thread; pop open JTask Dialog Box.
                        taskManager.execute(ti);

		setVisible(false);
		dispose();
	}//GEN-LAST:event_okButtonActionPerformed

	private void inNetworkMergeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inNetworkMergeCheckBoxActionPerformed
		this.updateOKButtonEnable();
	}//GEN-LAST:event_inNetworkMergeCheckBoxActionPerformed

	private double requiredCyThesaursServiceVersion = 1.01;
	private boolean checkCyThesaurus() {
//		CyThesaurusServiceClient client = new CyThesaurusServiceMessageBasedClient("AdvanceNetworkMerge");
//		if (!client.isServiceAvailable())
//			return false;
//
//		double version = client.serviceVersion();
//		return version >= requiredCyThesaursServiceVersion;
            return false;
	}
	/*
	 * Call when adding or removing a network to/from selected network list
	 * 
	 */
	private void addRemoveAttributeMapping(CyNetwork network, boolean isAdd) {
    
		if (isAdd) {
			nodeAttributeMapping.addNetwork(network, network.getDefaultNodeTable()); //TODO: make the table an user option?
			edgeAttributeMapping.addNetwork(network, network.getDefaultEdgeTable());
			matchingAttribute.addNetwork(network);
		} else {
			nodeAttributeMapping.removeNetwork(network);
			edgeAttributeMapping.removeNetwork(network);
			matchingAttribute.removeNetwork(network);
		}
	}

	private void updateOKButtonEnable() {
		int n = !advancedNetworkMergeCollapsiblePanel.isCollapsed()&&inNetworkMergeCheckBox.isSelected()&&getOperation()==Operation.UNION?1:2;

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
		String title = network.toString(); // TODO: TITLE
		model.put(title.toUpperCase(),network);
		fireContentsChanged(this, 0, getSize());
        }

        public CyNetwork removeElement(int index) {
		CyNetwork removed = model.remove(getElementAt(index).toString().toUpperCase());
		if (removed!=null) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;   
        }
        
        public List<CyNetwork> getNetworkList() {
		return new Vector<CyNetwork>(model.values());
        }
}


//	private void defineTgtAttributes() {
//		CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
//		List<String> attrNames = java.util.Arrays.asList(nodeAttributes.getAttributeNames());
//
//		MultiHashMapDefinition mmapDef = nodeAttributes.getMultiHashMapDefinition();
//
//		String mergedAttr = nodeAttributeMapping.getMergedAttribute(0);
//		if (attrNames.contains(mergedAttr)) {
//			// for existing attribute, check if its type is String or List
//			byte attrType = nodeAttributes.getType(mergedAttr);
//			if (attrType!=CyAttributes.TYPE_STRING && attrType!=CyAttributes.TYPE_SIMPLE_LIST) {
//				throw new java.lang.UnsupportedOperationException("Only String and List target attributes are supported.");
//			}
//		} else {
//			byte attrType = nodeAttributeMapping.getMergedAttributeType(0);
//
//			if (attrType==CyAttributes.TYPE_SIMPLE_LIST) {
//				// define the new attribute as List
//				byte[] keyTypes = new byte[] { MultiHashMapDefinition.TYPE_INTEGER };
//				mmapDef.defineAttribute(mergedAttr,
//							MultiHashMapDefinition.TYPE_STRING,
//							keyTypes);
//			} else {
//				mmapDef.defineAttribute(mergedAttr,
//							MultiHashMapDefinition.TYPE_STRING,
//							null);
//			}
//		}
//	}



