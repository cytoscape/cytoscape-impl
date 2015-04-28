package org.cytoscape.network.merge.internal.ui;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.cytoscape.model.CyNetwork;
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
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

/**
 * Main dialog for advance network merge
 */
public class NetworkMergeDialog extends JDialog {
	
	private static final long serialVersionUID = 1013626339762545400L;
	
	private final CyNetworkManager cnm;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming cnn;
	private final TaskManager<?, ?> taskManager;
	private final IconManager iconMgr;
	private CreateNetworkViewTaskFactory netViewCreator;
	
	private JPanel operationPnl;
	private ButtonGroup operationGroup;
	private JPanel differencePnl;
	private JRadioButton difference1Btn;
	private JRadioButton difference2Btn;
	private ButtonGroup differenceGroup;
	private JPanel selectNetPnl;
	private JPanel selectNetBtnPnl;
	private JButton moveRightBtn;
	private JButton moveLeftBtn;
	private JButton moveUpBtn;
	private JButton moveDownBtn;
	private JList<CyNetwork> unselectedNetLs;
	private JList<CyNetwork> selectedNetLs;
	private BasicCollapsiblePanel advancedOptionsPnl;
	private JScrollPane attrScr;
	private NetworkListModel selectedNetData;
	private SortedNetworkListModel unselectedNetData;
	private JTabbedPane mergeAttrTp;
	private JPanel mergeNodeAttrPnl;
	private JPanel mergeEdgeAttrPnl;
	private MatchNodeTable matchNodeTbl;
	private MergeAttributeTable mergeNodeAttrTbl;
	private MergeAttributeTable mergeEdgeAttrTbl;
	private JCheckBox idMappingCkb;
	private JCheckBox inNetMergeCkb;
	private JPanel buttonPnl;
	private JButton cancelBtn;
	private JButton okBtn;
	
	private final TreeMap<Operation, AbstractButton> operationButtons;
	private Map<String, Map<String, Set<String>>> selectedNetAttrIDType;
	
	private final AttributeMapping nodeAttrMapping;
	private final AttributeMapping edgeAttrMapping;
	private final MatchingAttribute matchingAttr;
//	boolean checkCyThesaurus;
	
	private String tgtType;
	private Operation selectedOperation = Operation.UNION;
	
	/** Creates new form NetworkMergeDialog */
	public NetworkMergeDialog(final CyNetworkManager cnm,
							  final CyNetworkFactory cnf,
							  final CyNetworkNaming cnn,
							  final TaskManager<?, ?> taskManager,
							  final IconManager iconMgr,
							  final CreateNetworkViewTaskFactory netViewCreator) {
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.cnm = cnm;
		this.cnf = cnf;
		this.cnn = cnn;
		this.taskManager = taskManager;
		this.iconMgr = iconMgr;
		this.netViewCreator = netViewCreator;

//		checkCyThesaurus = checkCyThesaurus();
		
		operationButtons = new TreeMap<>();
		matchingAttr = new MatchingAttributeImpl();
		nodeAttrMapping = new AttributeMappingImpl();
		edgeAttrMapping = new AttributeMappingImpl();

		initComponents();
		updateOKButton();
	}

	private void initComponents() {
		setTitle("Advanced Network Merge");
		setResizable(false);
		
		operationGroup = new ButtonGroup();
		
		differenceGroup = new ButtonGroup();
		differenceGroup.add(getDifference1Btn());
		differenceGroup.add(getDifference2Btn());
		
		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getOperationPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getDifferencePnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSelectNetPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAdvancedOptionsPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getButtonPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getOperationPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getDifferencePnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSelectNetPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAdvancedOptionsPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getButtonPnl(),  PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		updateDifferencePanel();
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkBtn().getAction(), getCancelBtn().getAction());
		getRootPane().setDefaultButton(getOkBtn());
		
		pack();
	}
	
	private JPanel getOperationPnl() {
		if (operationPnl == null) {
			operationPnl = new JPanel();
			operationPnl.setLayout(new BoxLayout(operationPnl, BoxLayout.LINE_AXIS));
			
			operationPnl.add(Box.createHorizontalGlue());
			
			int count = 0;
			final Operation[] values = Operation.values();
			
			for (Operation op : values) {
				final JToggleButton btn = new JToggleButton(op.toString(), op.getIcon());
				btn.setActionCommand(op.name());
				operationGroup.add(btn);
				operationButtons.put(op, btn);
				
				// Mac OS properties:
				btn.putClientProperty("JComponent.sizeVariant", "regular");
				btn.putClientProperty("JButton.buttonType", "segmented");
					
				if (count == 0)
					btn.putClientProperty("JButton.segmentPosition", "first");
				else if (count == values.length - 1)
					btn.putClientProperty("JButton.segmentPosition", "last");
				else
					btn.putClientProperty("JButton.segmentPosition", "middle");
					
				btn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						final String actionCommand = evt.getActionCommand();
						final Operation selectOp = Operation.valueOf(actionCommand);
						final int nSelectedNetwork = selectedNetData.getSize();
						
						if (selectOp == Operation.DIFFERENCE && nSelectedNetwork > 2) {
							final int ioption = JOptionPane
									.showConfirmDialog(
											NetworkMergeDialog.this,
											"Only the first two networks in the selected network list will be merged for difference operation. All the other selected networks will be removed. Are you sure?",
											"Warning: only two networks will be kept", JOptionPane.YES_NO_OPTION);
							if (ioption == JOptionPane.NO_OPTION && selectedOperation != null) {
								operationGroup.setSelected(operationButtons.get(selectedOperation).getModel(), true);
								return;
							}

							for (int is = nSelectedNetwork - 1; is >= 2; is--) {
								CyNetwork removed = selectedNetData.removeElement(is);
								unselectedNetData.add(removed);
								addRemoveAttributeMapping(removed, false);
							}
							
							getSelectedNetLs().repaint();
							getUnselectedNetLs().repaint();
							updateAttributeTable();
							updateMergeAttributeTable();
						}

						selectedOperation = selectOp;

						updateDifferencePanel();
						updateUpDownButtons();
						updateOKButton();
					}
				});
				
				operationPnl.add(btn);
				count++;
			}
			
			operationPnl.add(Box.createHorizontalGlue());
			
			operationGroup.setSelected(operationButtons.get(Operation.values()[0]).getModel(), true);
		}
		
		return operationPnl;
	}
	
	private JPanel getDifferencePnl() {
		if (differencePnl == null) {
			differencePnl = new JPanel();
			
			final GroupLayout layout = new GroupLayout(differencePnl);
			differencePnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(getDifference1Btn())
					.addComponent(getDifference2Btn())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getDifference1Btn())
					.addComponent(getDifference2Btn())
			);
		}
		
		return differencePnl;
	}
	
	private JRadioButton getDifference1Btn() {
		if (difference1Btn == null) {
			difference1Btn = new JRadioButton("Only remove nodes if all their edges are being subtracted, too");
			difference1Btn.setSelected(true);
		}
		
		return difference1Btn;
	}
	
	private JRadioButton getDifference2Btn() {
		if (difference2Btn == null) {
			difference2Btn = new JRadioButton("Remove all nodes that are in the 2\u207F\u1D48 network");
		}
		
		return difference2Btn;
	}
	
	private JPanel getSelectNetPnl() {
		if (selectNetPnl == null) {
			selectNetPnl = new JPanel();
			
			final JLabel allNetsLbl = new JLabel("Available Networks:");
			final JLabel selNetsLbl = new JLabel("Networks to Merge:");
			
			final JScrollPane listScr1 = new JScrollPane(getUnselectedNetLs());
			listScr1.setPreferredSize(new Dimension(260, 72));
			final JScrollPane listScr2 = new JScrollPane(getSelectedNetLs());
			listScr2.setPreferredSize(new Dimension(260, 72));
			
			final GroupLayout layout = new GroupLayout(selectNetPnl);
			selectNetPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(allNetsLbl)
						.addComponent(listScr1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addComponent(getSelectNetBtnPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addComponent(selNetsLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(getMoveUpBtn())
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getMoveDownBtn())
							)
						.addComponent(listScr2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE, true)
					.addGroup(layout.createSequentialGroup()
						.addComponent(allNetsLbl)
						.addComponent(listScr1)
					)
					.addComponent(getSelectNetBtnPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.BASELINE, true)
							.addComponent(selNetsLbl)
							.addComponent(getMoveUpBtn())
							.addComponent(getMoveDownBtn())
						)
						.addComponent(listScr2)
					)
			);
		}
		
		return selectNetPnl;
	}
	
	private JList<CyNetwork> getUnselectedNetLs() {
		if (unselectedNetLs == null) {
			unselectedNetData = new SortedNetworkListModel();
			unselectedNetLs = new JList<>(unselectedNetData);
			
			for (CyNetwork network : cnm.getNetworkSet()) {
				unselectedNetData.add(network);
			}
			
			unselectedNetLs.setCellRenderer(new ListCellRenderer<CyNetwork>() {
				private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

				public Component getListCellRendererComponent(JList<? extends CyNetwork> list, CyNetwork value,
						int index, boolean isSelected, boolean cellHasFocus) {
					JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
					renderer.setText(value.toString());
					
					return renderer;
				}
			});

			unselectedNetLs.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent evt) {
					int index = unselectedNetLs.getMinSelectionIndex();
					if (index > -1) {
						selectedNetLs.getSelectionModel().clearSelection();
						getMoveRightBtn().setEnabled(true);
					} else {
						getMoveRightBtn().setEnabled(false);
					}
				}
			});
		}
		
		return unselectedNetLs;
	}
	
	private JList<CyNetwork> getSelectedNetLs() {
		if (selectedNetLs == null) {
			selectedNetData = new NetworkListModel();
			selectedNetLs = new JList<>(selectedNetData);
			selectedNetLs.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			
			selectedNetLs.setCellRenderer(new ListCellRenderer<CyNetwork>() {
				private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

				@Override
				public Component getListCellRendererComponent(JList<? extends CyNetwork> list, CyNetwork value,
						int index, boolean isSelected, boolean cellHasFocus) {
					JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
					renderer.setText(value.toString());
					
					return renderer;
				}
			});
			selectedNetLs.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent evt) {
					int index = selectedNetLs.getMinSelectionIndex();
					if (index > -1) {
						getUnselectedNetLs().getSelectionModel().clearSelection();
						getMoveLeftBtn().setEnabled(true);
					} else {
						getMoveLeftBtn().setEnabled(false);
					}
					updateUpDownButtons();
				}
			});
		}
		
		return selectedNetLs;
	}
	
	private JPanel getSelectNetBtnPnl() {
		if (selectNetBtnPnl == null) {
			selectNetBtnPnl = new JPanel();
			selectNetBtnPnl.setLayout(new BoxLayout(selectNetBtnPnl, BoxLayout.Y_AXIS));
			
			selectNetBtnPnl.add(Box.createVerticalGlue());
			selectNetBtnPnl.add(getMoveRightBtn());
			selectNetBtnPnl.add(Box.createVerticalStrut(10));
			selectNetBtnPnl.add(getMoveLeftBtn());
		}
		
		return selectNetBtnPnl;
	}
	
	private JButton getMoveRightBtn() {
		if (moveRightBtn == null) {
			moveRightBtn = new JButton(IconManager.ICON_ANGLE_RIGHT);
			moveRightBtn.setFont(iconMgr.getIconFont(14.0f));
			moveRightBtn.setToolTipText("Add Selected");
			moveRightBtn.setEnabled(false);
			moveRightBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					int[] indices = getUnselectedNetLs().getSelectedIndices();
					if (indices == null || indices.length == 0) {
						return;
					}

					if (getOperation() == Operation.DIFFERENCE && selectedNetData.getSize() + indices.length > 2) {
						JOptionPane
								.showMessageDialog(
										NetworkMergeDialog.this,
										"Difference operation only supports two networks. If you need to replace the selected network, remove it first and select the new one.",
										"Warning", JOptionPane.WARNING_MESSAGE);
						return;
					}

					for (int i = indices.length - 1; i >= 0; i--) {
						CyNetwork removed = unselectedNetData.removeElement(indices[i]);
						selectedNetData.add(removed);
						addRemoveAttributeMapping(removed, true);
					}

					if (unselectedNetData.getSize() == 0) {
						getUnselectedNetLs().clearSelection();
						moveRightBtn.setEnabled(false);
					} else {
						int minindex = getUnselectedNetLs().getMinSelectionIndex();
						if (minindex >= unselectedNetData.getSize()) {
							minindex = 0;
						}
						getUnselectedNetLs().setSelectedIndex(minindex);
					}

					getSelectedNetLs().repaint();
					getUnselectedNetLs().repaint();
					updateOKButton();
					updateAttributeTable();
					updateMergeAttributeTable();
				}
			});
		}
		
		return moveRightBtn;
	}
	
	private JButton getMoveLeftBtn() {
		if (moveLeftBtn == null) {
			moveLeftBtn = new JButton(IconManager.ICON_ANGLE_LEFT);
			moveLeftBtn.setFont(iconMgr.getIconFont(14.0f));
			moveLeftBtn.setToolTipText("Remove Selected");
			moveLeftBtn.setEnabled(false);
			moveLeftBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					int[] indices = getSelectedNetLs().getSelectedIndices();
					if (indices == null || indices.length == 0) {
						return;
					}

					for (int i = indices.length - 1; i >= 0; i--) {
						CyNetwork removed = selectedNetData.removeElement(indices[i]);
						unselectedNetData.add(removed);
						addRemoveAttributeMapping(removed, false);
					}

					if (selectedNetData.getSize() == 0) {
						getSelectedNetLs().clearSelection();
						moveLeftBtn.setEnabled(false);
					} else {
						int minindex = getSelectedNetLs().getMinSelectionIndex();
						if (minindex >= selectedNetData.getSize()) {
							minindex = 0;
						}
						getSelectedNetLs().setSelectedIndex(minindex);
					}

					getSelectedNetLs().repaint();
					getUnselectedNetLs().repaint();
					updateOKButton();
					updateAttributeTable();
					updateMergeAttributeTable();
				}
			});
		}
		
		return moveLeftBtn;
	}
	
	private JButton getMoveUpBtn() {
		if (moveUpBtn == null) {
			moveUpBtn = new JButton(IconManager.ICON_CARET_UP);
			moveUpBtn.setFont(iconMgr.getIconFont(17.0f));
			moveUpBtn.setToolTipText("Move Selected Up");
			moveUpBtn.setBorderPainted(false);
			moveUpBtn.setContentAreaFilled(false);
			moveUpBtn.setOpaque(false);
			moveUpBtn.setFocusPainted(false);
			moveUpBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			moveUpBtn.setEnabled(false);
			moveUpBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					int[] indices = getSelectedNetLs().getSelectedIndices();
					
					if (indices == null || indices.length == 0)
						return;

					int imin = getSelectedNetLs().getMinSelectionIndex();
					int imax = getSelectedNetLs().getMaxSelectionIndex();

					if (imin < 1)
						return;

					CyNetwork removed = selectedNetData.removeElement(imin - 1);
					selectedNetData.add(imax, removed);

					for (int ii = 0; ii < indices.length; ii++) {
						indices[ii]--;
					}
					getSelectedNetLs().setSelectedIndices(indices);

					updateUpDownButtons();
					getSelectedNetLs().repaint();
					getUnselectedNetLs().repaint();
					updateOKButton();
					updateAttributeTable();
					updateMergeAttributeTable();
				}
			});
		}
		
		return moveUpBtn;
	}
	
	private JButton getMoveDownBtn() {
		if (moveDownBtn == null) {
			moveDownBtn = new JButton(IconManager.ICON_CARET_DOWN);
			moveDownBtn.setFont(iconMgr.getIconFont(17.0f));
			moveDownBtn.setToolTipText("Move Selected Down");
			moveDownBtn.setBorderPainted(false);
			moveDownBtn.setContentAreaFilled(false);
			moveDownBtn.setOpaque(false);
			moveDownBtn.setFocusPainted(false);
			moveDownBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			moveDownBtn.setEnabled(false);
			moveDownBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					int[] indices = getSelectedNetLs().getSelectedIndices();
					
					if (indices == null || indices.length == 0)
						return;

					int imin = getSelectedNetLs().getMinSelectionIndex();
					int imax = getSelectedNetLs().getMaxSelectionIndex();

					if (imax >= selectedNetData.getSize() - 1)
						return;

					CyNetwork removed = selectedNetData.removeElement(imax + 1);
					selectedNetData.add(imin, removed);

					for (int ii = 0; ii < indices.length; ii++) {
						indices[ii]++;
					}
					getSelectedNetLs().setSelectedIndices(indices);

					updateUpDownButtons();
					getSelectedNetLs().repaint();
					getUnselectedNetLs().repaint();
					updateOKButton();
					updateAttributeTable();
					updateMergeAttributeTable();
				}
			});
		}
		
		return moveDownBtn;
	}
	
	private BasicCollapsiblePanel getAdvancedOptionsPnl() {
		if (advancedOptionsPnl == null) {
			advancedOptionsPnl = new BasicCollapsiblePanel("Advanced Options");
			advancedOptionsPnl.addCollapseListener(new BasicCollapsiblePanel.CollapseListener() {
				@Override
				public void collapsed() {
					updateOKButton();
					pack();
				}
				@Override
				public void expanded() {
					updateOKButton();
					pack();
				}
			});
			
			final JLabel matchingColumnsLbl = new JLabel("Matching Columns (table columns to match nodes between networks):");
			
//			JLabel idmappingLabel = new JLabel();
//			idmappingLabel.setForeground(new Color(255, 0, 51));
//			idmappingLabel
//					.setText("If you want to map identifiers between the matching columns, please install CyThesaurus app version "
//							+ requiredCyThesaursServiceVersion + " or above.");
//			idmappingLabel.setVisible(false);
//			attrPnl.add(idmappingLabel);
			
			final JLabel howLbl = new JLabel("How to merge columns:");
			
			final GroupLayout layout = new GroupLayout(advancedOptionsPnl.getContentPane());
			advancedOptionsPnl.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(matchingColumnsLbl)
					.addComponent(getAttrScr(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getIdMappingCkb())
					.addComponent(howLbl)
					.addComponent(getMergeAttrTp(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getInNetMergeCkb())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(matchingColumnsLbl)
					.addComponent(getAttrScr(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getIdMappingCkb())
					.addComponent(howLbl)
					.addComponent(getMergeAttrTp(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getInNetMergeCkb())
			);
		}

		return advancedOptionsPnl;
	}
	
	private JScrollPane getAttrScr() {
		if (attrScr == null) {
			attrScr = new JScrollPane();
			attrScr.setMinimumSize(new Dimension(100, 50));
			attrScr.setPreferredSize(new Dimension(450, 50));
			attrScr.setViewportView(getMatchNodeTbl());
		}
		
		return attrScr;
	}
	
	private MatchNodeTable getMatchNodeTbl() {
		if (matchNodeTbl == null) {
			matchNodeTbl = new MatchNodeTable(matchingAttr);
			matchNodeTbl.getModel().addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					getMergeNodeAttrTbl().updateMatchingAttribute();
				}
			});
		}
		
		return matchNodeTbl;
	}
	
	private MergeAttributeTable getMergeNodeAttrTbl() {
		if (mergeNodeAttrTbl == null) {
			mergeNodeAttrTbl = new MergeAttributeTable(nodeAttrMapping, matchingAttr);
		}
		
		return mergeNodeAttrTbl;
	}
	
	private MergeAttributeTable getMergeEdgeAttrTbl() {
		if (mergeEdgeAttrTbl == null) {
			mergeEdgeAttrTbl = new MergeAttributeTable(edgeAttrMapping);
		}
		
		return mergeEdgeAttrTbl;
	}
	
	private JCheckBox getIdMappingCkb() {
		if (idMappingCkb == null) {
			idMappingCkb = new JCheckBox("Map IDs between the matching columns");
			idMappingCkb.setVisible(false);
		}
		
		return idMappingCkb;
	}
	
	private JTabbedPane getMergeAttrTp() {
		if (mergeAttrTp == null) {
			mergeAttrTp = new JTabbedPane();
			mergeAttrTp.setTabPlacement(JTabbedPane.BOTTOM);
			mergeAttrTp.setMinimumSize(new Dimension(450, 150));
			mergeAttrTp.setPreferredSize(new Dimension(450, 200));
			
			mergeAttrTp.addTab("Node", getMergeNodeAttrPnl());
			mergeAttrTp.addTab("Edge", getMergeEdgeAttrPnl());
		}
		
		return mergeAttrTp;
	}
	
	private JPanel getMergeNodeAttrPnl() {
		if (mergeNodeAttrPnl == null) {
			mergeNodeAttrPnl = new JPanel();
			mergeNodeAttrPnl.setLayout(new BoxLayout(mergeNodeAttrPnl, BoxLayout.LINE_AXIS));
			
			final JScrollPane mergeNodeAttributeScrollPane = new JScrollPane();
			mergeNodeAttributeScrollPane.setViewportView(getMergeNodeAttrTbl());
			
			mergeNodeAttrPnl.add(mergeNodeAttributeScrollPane);
		}
		
		return mergeNodeAttrPnl;
	}
	
	private JPanel getMergeEdgeAttrPnl() {
		if (mergeEdgeAttrPnl == null) {
			mergeEdgeAttrPnl = new JPanel();
			mergeEdgeAttrPnl.setLayout(new BoxLayout(mergeEdgeAttrPnl, BoxLayout.LINE_AXIS));
			
			final JScrollPane mergeEdgeAttributeScrollPane = new JScrollPane();
			mergeEdgeAttributeScrollPane.setViewportView(getMergeEdgeAttrTbl());
			
			mergeEdgeAttrPnl.add(mergeEdgeAttributeScrollPane);
		}
		
		return mergeEdgeAttrPnl;
	}
	
	private JCheckBox getInNetMergeCkb() {
		if (inNetMergeCkb == null) {
			inNetMergeCkb = new JCheckBox("Enable merging nodes/edges in the same network");
			inNetMergeCkb.setSelected(true);
			inNetMergeCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					updateOKButton();
				}
			});
		}
		
		return inNetMergeCkb;
	}
	
	private JPanel getButtonPnl() {
		if (buttonPnl == null) {
			buttonPnl = LookAndFeelUtil.createOkCancelPanel(getOkBtn(), getCancelBtn());
			buttonPnl.setDoubleBuffered(false);
		}
		
		return buttonPnl;
	}
	
	@SuppressWarnings("serial")
	private JButton getOkBtn() {
		if (okBtn == null) {
			okBtn = new JButton(new AbstractAction("Merge") {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (idMappingCkb.isSelected()) {
						// TODO: implement this option
					}

					final String netName = cnn.getSuggestedNetworkTitle(getMergeNodeAttrTbl().getMergedNetworkName());
					final AttributeConflictCollector conflictCollector = new AttributeConflictCollectorImpl();

					// Network merge task
					final NetworkMergeTask nmTask = new NetworkMergeTask(cnf, cnm, netName, matchingAttr,
							nodeAttrMapping, edgeAttrMapping, selectedNetData.getNetworkList(),
							getOperation(), conflictCollector, selectedNetAttrIDType, tgtType,
							getInNetMergeCkb().isSelected(), netViewCreator);

					final TaskIterator ti = new TaskIterator(nmTask);

					// Execute Task in New Thread; pop open JTask Dialog Box.
					taskManager.execute(ti);

					setVisible(false);
					dispose();
				}
			});
			okBtn.getAction().setEnabled(false);
		}
		
		return okBtn;
	}
	
	@SuppressWarnings("serial")
	private JButton getCancelBtn() {
		if (cancelBtn == null) {
			cancelBtn = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					setVisible(false);
					dispose();
				}
			});
		}
		
		return cancelBtn;
	}

//	private double requiredCyThesaursServiceVersion = 1.01;
//
//	private boolean checkCyThesaurus() {
//		// CyThesaurusServiceClient client = new
//		// CyThesaurusServiceMessageBasedClient("AdvanceNetworkMerge");
//		// if (!client.isServiceAvailable())
//		// return false;
//		//
//		// double version = client.serviceVersion();
//		// return version >= requiredCyThesaursServiceVersion;
//		return false;
//	}

	/*
	 * Call when adding or removing a network to/from selected network list
	 */
	private void addRemoveAttributeMapping(CyNetwork network, boolean isAdd) {

		if (isAdd) {
			nodeAttrMapping.addNetwork(network, network.getDefaultNodeTable()); // TODO:
																						// make
																						// the
																						// table
																						// an
																						// user
																						// option?
			edgeAttrMapping.addNetwork(network, network.getDefaultEdgeTable());
			matchingAttr.addNetwork(network);
		} else {
			nodeAttrMapping.removeNetwork(network);
			edgeAttrMapping.removeNetwork(network);
			matchingAttr.removeNetwork(network);
		}
	}

	private void updateOKButton() {
		int n = !getAdvancedOptionsPnl().isCollapsed() && getInNetMergeCkb().isSelected()
				&& getOperation() == Operation.UNION ? 1 : 2;

		if (selectedNetData.getSize() < n) {
			getOkBtn().setToolTipText("Select at least " + n + " networks to merge");
			getOkBtn().getAction().setEnabled(false);
		} else {
			getOkBtn().setToolTipText(null);
			getOkBtn().getAction().setEnabled(true);
		}
	}

	private void updateUpDownButtons() {
		boolean diff = selectedOperation == Operation.DIFFERENCE;
		int imin = getSelectedNetLs().getMinSelectionIndex();
		int imax = getSelectedNetLs().getMaxSelectionIndex();
		getMoveUpBtn().setEnabled(diff && 0 < imin && imax <= selectedNetData.getSize() - 1);
		getMoveDownBtn().setEnabled(diff && 0 <= imin && imax < selectedNetData.getSize() - 1);
	}

	private void updateDifferencePanel() {
		getDifferencePnl().setVisible(selectedOperation == Operation.DIFFERENCE);
		pack();
	}
	
	private void updateAttributeTable() {
		getMatchNodeTbl().fireTableStructureChanged();
	}

	private void updateMergeAttributeTable() {
		getMergeNodeAttrTbl().fireTableStructureChanged();
		getMergeEdgeAttrTbl().fireTableStructureChanged();
	}

	/*
	 * Get currently selected operation
	 */
	private Operation getOperation() {
		return this.selectedOperation;
	}
}

@SuppressWarnings("serial")
class NetworkListModel extends AbstractListModel<CyNetwork> {
	
	Vector<CyNetwork> model;

	public NetworkListModel() {
		model = new Vector<CyNetwork>();
	}

	@Override
	public int getSize() {
		return model.size();
	}

	@Override
	public CyNetwork getElementAt(int index) {
		return model.get(index);
	}

	public void add(CyNetwork network) {
		model.add(network);
		fireContentsChanged(this, 0, getSize());
	}

	public void add(int index, CyNetwork network) {
		model.add(index, network);
		fireContentsChanged(this, 0, getSize());
	}

	public CyNetwork removeElement(int index) {
		CyNetwork removed = model.remove(index);
		if (removed != null) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;
	}

	public List<CyNetwork> getNetworkList() {
		return model;
	}
}

@SuppressWarnings("serial")
class SortedNetworkListModel extends AbstractListModel<CyNetwork> {
	
	TreeMap<String, CyNetwork> model;

	public SortedNetworkListModel() {
		model = new TreeMap<String, CyNetwork>();
	}

	@Override
	public int getSize() {
		return model.size();
	}

	@Override
	public CyNetwork getElementAt(int index) {
		return (CyNetwork) model.values().toArray()[index];
	}

	public void add(CyNetwork network) {
		String title = network.toString(); // TODO: TITLE
		model.put(title.toUpperCase(), network);
		fireContentsChanged(this, 0, getSize());
	}

	public CyNetwork removeElement(int index) {
		CyNetwork removed = model.remove(getElementAt(index).toString().toUpperCase());
		if (removed != null) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;
	}

	public List<CyNetwork> getNetworkList() {
		return new Vector<CyNetwork>(model.values());
	}
}
