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
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.INTERACTION;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.SOURCE;
import static org.cytoscape.tableimport.internal.ui.theme.SourceColumnSemantics.TARGET;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.tableimport.internal.ui.theme.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * GUI Component for specify options for network table import.<br>
 *
 * @since Cytoscape 2.4
 * @version 0.8
 * @author Keiichiro Ono
 */
public class NetworkImportOptionsPanel extends JPanel {
	
	private static final long serialVersionUID = 2708007951959440414L;
	
	private final PropertyChangeSupport changes;
	
	private JLabel edgeAttributesLabel;
	private JLabel iconLabel1;
	private JLabel iconLabel2;
	private JComboBox<String> interactionComboBox;
	private JLabel interactionLabel;
	private JLabel interactionIconLabel;
	private JComboBox<String> sourceComboBox;
	private JLabel sourceLabel;
	private JLabel sourceIconLabel;
	private JComboBox<String> targetComboBox;
	private JLabel targetLabel;
	private JLabel targetIconLabel;

	private final IconManager iconManager;

	private boolean ignoreColumnsComboBoxActionEvent;

	/**
	 * Creates a new NetworkImportOptionsPanel object.
	 */
	public NetworkImportOptionsPanel(final IconManager iconManager) {
		this.iconManager = iconManager;
		initComponents();

		initializeUIStates();
		changes = new PropertyChangeSupport(this);
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

	private void initComponents() {
		sourceLabel = new JLabel("Source Interaction:");
		sourceComboBox = new JComboBox<>();
		interactionLabel = new JLabel("Interaction Type:");
		interactionComboBox = new JComboBox<>();
		targetLabel = new JLabel("Target Interaction:");
		targetComboBox = new JComboBox<>();
		iconLabel1 = new JLabel(IconManager.ICON_DOUBLE_ANGLE_LEFT);
		iconLabel2 = new JLabel(IconManager.ICON_DOUBLE_ANGLE_RIGHT);
		edgeAttributesLabel = new JLabel();
		
		sourceIconLabel = new JLabel(SOURCE.getText());
		sourceIconLabel.setFont(iconManager.getIconFont(14.0f));
		sourceIconLabel.setForeground(SOURCE.getForeground());
		
		interactionIconLabel = new JLabel(INTERACTION.getText());
		interactionIconLabel.setFont(iconManager.getIconFont(14.0f));
		interactionIconLabel.setForeground(INTERACTION.getForeground());
		
		targetIconLabel = new JLabel(TARGET.getText());
		targetIconLabel.setFont(iconManager.getIconFont(14.0f));
		targetIconLabel.setForeground(TARGET.getForeground());

		setBorder(LookAndFeelUtil.createPanelBorder());

		sourceComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				networkColumnsComboBoxActionPerformed(evt);
			}
		});

		interactionComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				networkColumnsComboBoxActionPerformed(evt);
			}
		});

		targetComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				networkColumnsComboBoxActionPerformed(evt);
			}
		});

		iconLabel1.setFont(iconManager.getIconFont(14.0f));
		iconLabel2.setFont(iconManager.getIconFont(14.0f));
		
		edgeAttributesLabel.setFont(edgeAttributesLabel.getFont().deriveFont(11.0f));
		edgeAttributesLabel.setText("The other columns will be imported as EDGE ATTRIBUTES.");

		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
							.addGroup(layout.createSequentialGroup()
									.addComponent(sourceIconLabel)
									.addComponent(sourceLabel)
							)
							.addComponent(sourceComboBox, 0, 220, Short.MAX_VALUE)
					)
	                .addComponent(iconLabel1)
	                .addGroup(layout.createParallelGroup(Alignment.LEADING)
	                		.addGroup(layout.createSequentialGroup()
									.addComponent(interactionIconLabel)
									.addComponent(interactionLabel)
							)
	                		.addComponent(interactionComboBox, 0, 220, Short.MAX_VALUE)
	                )
	                .addComponent(iconLabel2)
	                .addGroup(layout.createParallelGroup(Alignment.LEADING)
	                		.addGroup(layout.createSequentialGroup()
									.addComponent(targetIconLabel)
									.addComponent(targetLabel)
							)
	                		.addComponent(targetComboBox, 0, 220, Short.MAX_VALUE)
	                )
				)
				.addComponent(edgeAttributesLabel, DEFAULT_SIZE, 100, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(sourceIconLabel)
						.addComponent(sourceLabel)
						.addComponent(interactionIconLabel)
						.addComponent(interactionLabel)
						.addComponent(targetIconLabel)
						.addComponent(targetLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(sourceComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(iconLabel1)
						.addComponent(interactionComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(iconLabel2)
						.addComponent(targetComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(edgeAttributesLabel)
		);
	}

	private void networkColumnsComboBoxActionPerformed(ActionEvent e) {
		if (ignoreColumnsComboBoxActionEvent)
			return;
		
		final int sIdx = sourceComboBox.getSelectedIndex() - 1;
		final int tIdx = targetComboBox.getSelectedIndex() - 1;
		final int iIdx = interactionComboBox.getSelectedIndex() - 1;

		List<Integer> colIdx = new ArrayList<Integer>();

		colIdx.add(sIdx);
		colIdx.add(tIdx);
		colIdx.add(iIdx);

		changes.firePropertyChange(ImportTablePanel.NETWORK_IMPORT_TEMPLATE_CHANGED, null, colIdx);
	}

	private void initializeUIStates() {
		sourceComboBox.setEnabled(false);
		targetComboBox.setEnabled(false);
		interactionComboBox.setEnabled(false);
	}

	public void setComboBoxes(String[] columnNames) {
		ignoreColumnsComboBoxActionEvent = true;
		
		try {
			sourceComboBox.removeAllItems();
			targetComboBox.removeAllItems();
			interactionComboBox.removeAllItems();
	
			interactionComboBox.addItem("-- Default Interaction --");
			sourceComboBox.addItem("-- Select Column --");
			targetComboBox.addItem("-- Select Column --");
			
			for (String item : columnNames) {
				sourceComboBox.addItem(item);
				targetComboBox.addItem(item);
				interactionComboBox.addItem(item);
			}
	
			sourceComboBox.setEnabled(true);
			targetComboBox.setEnabled(true);
			interactionComboBox.setEnabled(true);
	
			sourceComboBox.setSelectedIndex(0);
			targetComboBox.setSelectedIndex(0);
			interactionComboBox.setSelectedIndex(0);
		} finally {
			ignoreColumnsComboBoxActionEvent = false;
		}
	}

	/**
	 * Get index from combo boxes.
	 */
	public int getSourceIndex() {
		return sourceComboBox.getSelectedIndex() - 1;
	}

	public int getTargetIndex() {
		return targetComboBox.getSelectedIndex() - 1;
	}

	public int getInteractionIndex() {
		return interactionComboBox.getSelectedIndex() - 1;
	}
}
