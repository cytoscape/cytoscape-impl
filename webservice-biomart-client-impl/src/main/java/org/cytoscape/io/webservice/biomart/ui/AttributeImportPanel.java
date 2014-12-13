package org.cytoscape.io.webservice.biomart.ui;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * General GUI component for importing attributes.<br>
 * Maybe used by Web Service Clients to import attributes.
 * 
 * This UI accepts title and icon. Usually, those are from source database.
 * 
 * TODO: is this the right place for this class?
 * 
 */
public abstract class AttributeImportPanel extends JPanel implements ColumnCreatedListener, ColumnDeletedListener,
		SetCurrentNetworkListener {

	private static final long serialVersionUID = 8665197023334496167L;

	// Swing components. Maybe accessed from child classes.
	protected JComboBox columnNameComboBox;
	protected JLabel attributeLabel;
	protected JPanel attributePanel;
	protected JComboBox attributeTypeComboBox;
	protected JLabel attributeTypeLabel;
	protected JButton cancelButton;
	protected JComboBox databaseComboBox;
	protected JPanel databasePanel;
	protected JPanel attrListPanel;
	protected JPanel availableAttrPanel;
	protected JScrollPane availableAttrScrollPane;
	protected JButton importButton;
	protected JLabel titleLabel;
	protected JButton selectAllButton;
	protected JButton selectNoneButton;
	protected JButton refreshButton;
	protected CheckBoxJList attrCheckboxList;

	protected DefaultListModel attrCheckboxListModel;

	// Title of the panel.
	protected String panelTitle;

	// Icon for this panel title.
	protected Icon logo;

	// Attribute panel border title
	protected String attributePanelTitle;

	protected final CyTableManager tblManager;
	private final CyNetworkManager netManager;

	protected AttributeImportPanel(final CyTableManager tblManager, final CyNetworkManager netManager, Icon logo,
			String title, String attrPanelTitle) {
		this.logo = logo;
		this.panelTitle = title;
		this.attributePanelTitle = attrPanelTitle;
		this.tblManager = tblManager;
		this.netManager = netManager;

		initComponents();

		setAttributeComboBox();
	}

	private void initComponents() {
		attrCheckboxList = new CheckBoxJList();
		attrCheckboxListModel = new DefaultListModel();
		attrCheckboxList.setModel(attrCheckboxListModel);

		titleLabel = new JLabel(panelTitle);
		titleLabel.setIcon(logo);
		
		databasePanel = new JPanel();
		databaseComboBox = new JComboBox();
		attributePanel = new JPanel();
		attributeLabel = new JLabel("Column:");
		columnNameComboBox = new JComboBox();
		attributeTypeLabel = new JLabel("Data Type:");
		attributeTypeComboBox = new JComboBox();
		availableAttrPanel = new JPanel();
		availableAttrScrollPane = new JScrollPane();
		attrListPanel = new JPanel();
		importButton = new JButton("Import");
		cancelButton = new JButton("Cancel");
		selectAllButton = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
		
		refreshButton = new JButton("...");
		refreshButton.setToolTipText("Select Services...");

		databasePanel.setBorder(LookAndFeelUtil.createTitledBorder("Service"));

		databaseComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				databaseComboBoxActionPerformed(evt);
			}
		});
		
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				refreshButtonActionPerformed(evt);
			}
		});

		GroupLayout databasePanelLayout = new GroupLayout(databasePanel);
		databasePanel.setLayout(databasePanelLayout);
		databasePanelLayout.setAutoCreateContainerGaps(true);
		databasePanelLayout.setAutoCreateGaps(true);
		
		databasePanelLayout.setHorizontalGroup(databasePanelLayout.createSequentialGroup()
				.addComponent(databaseComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(refreshButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		databasePanelLayout.setVerticalGroup(databasePanelLayout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(databaseComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(refreshButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		attributePanel.setBorder(LookAndFeelUtil.createTitledBorder("Key Column in Cytoscape"));

		attributeTypeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				attributeTypeComboBoxActionPerformed(evt);
			}
		});

		GroupLayout attributePanelLayout = new GroupLayout(attributePanel);
		attributePanel.setLayout(attributePanelLayout);
		attributePanelLayout.setAutoCreateContainerGaps(true);
		attributePanelLayout.setAutoCreateGaps(true);
		
		attributePanelLayout.setHorizontalGroup(attributePanelLayout.createSequentialGroup()
				.addGroup(attributePanelLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(attributeLabel)
						.addComponent(attributeTypeLabel)
				)
				.addGroup(attributePanelLayout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(attributeTypeComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(columnNameComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
		attributePanelLayout.setVerticalGroup(attributePanelLayout.createSequentialGroup()
				.addGroup(attributePanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(attributeLabel)
						.addComponent(columnNameComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(attributePanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(attributeTypeLabel)
						.addComponent(attributeTypeComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		availableAttrPanel.setBorder(LookAndFeelUtil.createTitledBorder(attributePanelTitle));
		availableAttrScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		availableAttrScrollPane.setViewportView(attrCheckboxList);

		selectAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				selectAllButtonActionPerformed(evt);
			}
		});
		selectNoneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				selectNoneButtonActionPerformed(evt);
			}
		});
		
		GroupLayout availableAttrPanelLayout = new GroupLayout(availableAttrPanel);
		availableAttrPanel.setLayout(availableAttrPanelLayout);
		availableAttrPanelLayout.setAutoCreateContainerGaps(true);
		availableAttrPanelLayout.setAutoCreateGaps(true);
		
		availableAttrPanelLayout.setHorizontalGroup(availableAttrPanelLayout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(availableAttrScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(availableAttrPanelLayout.createSequentialGroup()
						.addComponent(selectAllButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectNoneButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				
		);
		availableAttrPanelLayout.setVerticalGroup(availableAttrPanelLayout.createSequentialGroup()
				.addComponent(availableAttrScrollPane, DEFAULT_SIZE, 280, Short.MAX_VALUE)
				.addGroup(availableAttrPanelLayout.createParallelGroup(Alignment.CENTER)
						.addComponent(selectAllButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectNoneButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				importButtonActionPerformed(evt);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});
		
		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(importButton, cancelButton);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(titleLabel)
				.addComponent(databasePanel, DEFAULT_SIZE, 800, Short.MAX_VALUE)
				.addComponent(attributePanel, DEFAULT_SIZE, 800, Short.MAX_VALUE)
				.addComponent(availableAttrPanel, DEFAULT_SIZE, 800, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(titleLabel)
				.addComponent(databasePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(attributePanel, PREFERRED_SIZE, DEFAULT_SIZE,	PREFERRED_SIZE)
				.addComponent(availableAttrPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE,	PREFERRED_SIZE)
		);
	}

	protected abstract void selectNoneButtonActionPerformed(ActionEvent evt);
	protected abstract void selectAllButtonActionPerformed(ActionEvent evt);
	protected abstract void refreshButtonActionPerformed(ActionEvent evt);

	protected void importButtonActionPerformed(ActionEvent evt) {
		importAttributes();
	}

	protected void cancelButtonActionPerformed(ActionEvent evt) {
		// Close parent
		final JDialog container = (JDialog) this.getRootPane().getParent();
		// System.out.println("parent = " + container);
		container.setVisible(false);
	}

	private void attributeTypeComboBoxActionPerformed(ActionEvent evt) {
		// TODO add your handling code here:
	}

	abstract protected void databaseComboBoxActionPerformed(ActionEvent evt);

	protected abstract void importAttributes();

	private void setAttributeComboBox() {
		final Set<CyNetwork> networks = this.netManager.getNetworkSet();

		for (CyNetwork network : networks) {
			final CyTable nodeTable = network.getDefaultNodeTable();
			final Collection<CyColumn> columns = nodeTable.getColumns();
			for (CyColumn col : columns)
				columnNameComboBox.addItem(col.getName());
		}
		columnNameComboBox.setSelectedItem(CyRootNetwork.SHARED_NAME);
	}

	protected void addAttribute(final String attributeName) {
		if (attributeName == null)
			return;

		final Object currentSelection = columnNameComboBox.getSelectedItem();

		final SortedSet<String> attrNameSet = new TreeSet<String>();
		attrNameSet.add(attributeName);
		for (int i = 0; i < columnNameComboBox.getItemCount(); i++)
			attrNameSet.add(columnNameComboBox.getItemAt(i).toString());

		columnNameComboBox.removeAllItems();

		for (String name : attrNameSet)
			columnNameComboBox.addItem(name);

		if (currentSelection != null)
			columnNameComboBox.setSelectedItem(currentSelection.toString());
	}

	protected void removeAttribute(final String attributeName) {
		if (attributeName != null) {
			columnNameComboBox.removeItem(attributeName);
			return;
		}
	}

	private boolean validTable(CyTable t) {
		for (CyNetwork network : netManager.getNetworkSet()) {
			if (t.equals(network.getDefaultNodeTable()))
				return true;
		}
		return false;
	}

	@Override
	public void handleEvent(final ColumnCreatedEvent e) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (validTable(e.getSource()))
					addAttribute(e.getColumnName());
			}
		});
	}

	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (validTable(e.getSource()))
					removeAttribute(e.getColumnName());
			}
		});
	}
	
	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateColumnList(e.getNetwork() != null ? e.getNetwork().getDefaultNodeTable() : null);
			}
		});
	}
	
	private void updateColumnList(final CyTable currentNodeTable) {
		final SortedSet<String> attrNameSet = new TreeSet<String>();
		
		if (currentNodeTable != null) {
			final Collection<CyColumn> columns = currentNodeTable.getColumns();
			
			for (CyColumn col: columns)
				attrNameSet.add(col.getName());
		}

		columnNameComboBox.removeAllItems();

		for (String name : attrNameSet)
			columnNameComboBox.addItem(name);
		
		columnNameComboBox.setSelectedItem(CyRootNetwork.SHARED_NAME);
	}

}
