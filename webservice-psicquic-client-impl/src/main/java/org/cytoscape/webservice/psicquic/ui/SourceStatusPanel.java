package org.cytoscape.webservice.psicquic.ui;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultRowSorter;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.PSIMI25VisualStyleBuilder;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.task.ImportNetworkFromPSICQUICTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.FinishStatus.Type;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

public class SourceStatusPanel extends JPanel implements TaskObserver {

	private static final long serialVersionUID = 6996385373168492882L;

	private static final Color SELECTED_ROW = new Color(0xaa, 0xaa, 0xaa, 200);
	private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);

	private static final int IMPORT_COLUMN_INDEX = 0;
	private static final int STATUS_COLUMN_INDEX = 1;
	private static final int DB_NAME_COLUMN_INDEX = 2;
	private static final int RECORD_COUNT_COLUMN_INDEX = 3;
	private static final int TAG_COLUMN_INDEX = 4;

	private boolean cancelFlag = false;

	private final RegistryManager manager;
	private final PSICQUICRestClient client;
	private String query;
	private final CyNetworkManager networkManager;

	private final TaskManager<?, ?> taskManager;

	private final SearchMode mode;

	private final CreateNetworkViewTaskFactory createViewTaskFactory;

	private final PSIMI25VisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;

	private final PSIMITagManager tagManager;

	private final CyProperty<Properties> props;

	private final CyServiceRegistrar registrar;

	private int interactionsFound = 0;

	/**
	 * Creates new form PSICQUICResultDialog
	 * 
	 */
	public SourceStatusPanel(final String query, final PSICQUICRestClient client, final RegistryManager manager,
			final CyNetworkManager networkManager, final Map<String, Long> result, final TaskManager taskManager,
			final SearchMode mode, final CreateNetworkViewTaskFactory createViewTaskFactory,
			final PSIMI25VisualStyleBuilder vsBuilder, final VisualMappingManager vmm,
			final PSIMITagManager tagManager, final CyProperty<Properties> props, final CyServiceRegistrar registrar) {
		this.manager = manager;
		this.client = client;
		this.query = query;
		this.networkManager = networkManager;
		this.taskManager = taskManager;
		this.tagManager = tagManager;
		this.props = props;
		this.registrar = registrar;

		if (mode == SearchMode.SPECIES)
			this.mode = SearchMode.MIQL;
		else
			this.mode = mode;
		this.createViewTaskFactory = createViewTaskFactory;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;

		setTableModel(result);

		refreshGUI();
		final TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(6, 3, 0, 3),
				"2. Select Database");
		titledBorder.setTitleFont(TITLE_FONT);
		this.setBorder(titledBorder);
		this.setOpaque(false);
		resultTable.setEnabled(false);

		this.registrar.registerService(this, TaskObserver.class, new Properties());
	}

	private void refreshGUI() {
		initComponents();
		setCoumnWidth();
	}

	public void enableComponents(final boolean enable) {
		this.resultTable.setEnabled(enable);
		this.resultScrollPane.setEnabled(enable);
		this.selectAllButton.setEnabled(enable);
		this.importNetworkButton.setEnabled(enable);
		this.clearSelectionButton.setEnabled(enable);
		this.clusterResultCheckBox.setEnabled(enable);
		this.setEnabled(enable);
	}

	public Set<String> getSelected() {
		if (cancelFlag)
			return null;

		final Set<String> selectedService = new HashSet<String>();
		final StringBuilder builder = new StringBuilder();

		TableModel model = this.resultTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			Boolean selected = (Boolean) model.getValueAt(i, IMPORT_COLUMN_INDEX);
			if (selected == null)
				selected = false;

			if (selected) {
				final String selectedSource = model.getValueAt(i, DB_NAME_COLUMN_INDEX).toString();
				builder.append(selectedSource + ",");
				selectedService.add(selectedSource);
			}
		}

		// Save selection as property
		String selectedSourceString = builder.toString();
		if (selectedSourceString.equals("") == false) {
			selectedSourceString = selectedSourceString.substring(0, selectedSourceString.length() - 1);
			props.getProperties().setProperty(PSICQUICSearchUI.PROP_NAME, selectedSourceString);
		}
		return selectedService;
	}

	private void setCoumnWidth() {

		resultTable.getTableHeader().setReorderingAllowed(false);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// Import?
		resultTable.getColumnModel().getColumn(IMPORT_COLUMN_INDEX).setPreferredWidth(60);
		// Status
		resultTable.getColumnModel().getColumn(STATUS_COLUMN_INDEX).setPreferredWidth(60);
		// Name
		resultTable.getColumnModel().getColumn(DB_NAME_COLUMN_INDEX).setPreferredWidth(120);
		// Record Number
		resultTable.getColumnModel().getColumn(RECORD_COUNT_COLUMN_INDEX).setPreferredWidth(120);
		// Tags
		resultTable.getColumnModel().getColumn(TAG_COLUMN_INDEX).setPreferredWidth(280);

		resultTable.setSelectionBackground(SELECTED_ROW);
		resultTable.setDefaultRenderer(String.class, new StringCellRenderer());
		resultTable.setDefaultRenderer(Number.class, new NumberCellRenderer());
	}

	private void setTableModel(final Map<String, Long> result) {

		final StatusTableModel model = new StatusTableModel();
		model.addColumn("Import");
		model.addColumn("Status");
		model.addColumn("Database Name");
		model.addColumn("Records Found");
		model.addColumn("Database Type (Tags)");

		// Reset counter
		this.interactionsFound = 0;

		for (final String serviceName : manager.getAllServiceNames()) {
			Integer errorID = null;
			final Object[] rowValues = new Object[5];

			rowValues[DB_NAME_COLUMN_INDEX] = serviceName;
			rowValues[TAG_COLUMN_INDEX] = convertTags(serviceName);
			if (result != null) {
				final String targetURL = manager.getActiveServices().get(serviceName);
				if (targetURL != null) {
					Long targetResult = result.get(targetURL);
					if (targetResult == null)
						rowValues[RECORD_COUNT_COLUMN_INDEX] = 0;
					else {
						final Integer count = targetResult.intValue();
						if (count > 0)
							interactionsFound = interactionsFound + count;
						else if (count < 0)
							errorID = count;

						if (count < 0)
							rowValues[RECORD_COUNT_COLUMN_INDEX] = 0;
						else
							rowValues[RECORD_COUNT_COLUMN_INDEX] = count;
					}
				} else
					rowValues[RECORD_COUNT_COLUMN_INDEX] = 0;
			} else {
				rowValues[RECORD_COUNT_COLUMN_INDEX] = manager.getCountMap().get(serviceName).intValue();
			}

			if (errorID != null) {
				if (errorID == PSICQUICRestClient.ERROR_CANCEL.intValue())
					rowValues[STATUS_COLUMN_INDEX] = "Operation canceled";
				else if (errorID == PSICQUICRestClient.ERROR_TIMEOUT.intValue()) {
					rowValues[STATUS_COLUMN_INDEX] = "Timeout.  Try again later.";
				} else if (errorID == PSICQUICRestClient.ERROR_SEARCH_FAILED.intValue()) {
					rowValues[STATUS_COLUMN_INDEX] = "Server returns error.  Try again later.";
				} else {
					rowValues[STATUS_COLUMN_INDEX] = "Unknown error.  Try again later.";
				}
			} else {
				if (manager.isActive(serviceName)) {
					rowValues[STATUS_COLUMN_INDEX] = "Active";
					if (((Integer) rowValues[RECORD_COUNT_COLUMN_INDEX]) != 0)
						rowValues[IMPORT_COLUMN_INDEX] = true;
					else
						rowValues[IMPORT_COLUMN_INDEX] = false;
				} else {
					rowValues[IMPORT_COLUMN_INDEX] = false;
					rowValues[STATUS_COLUMN_INDEX] = "Inactive";
				}
			}
			model.addRow(rowValues);

		}
		this.resultTable = new JTable(model) {

			private static final long serialVersionUID = 1804418707227880677L;

			@Override
			public String getToolTipText(MouseEvent e) {
				final int row = convertRowIndexToModel(rowAtPoint(e.getPoint()));
				final TableModel m = getModel();
				return "<html><strong>" + m.getValueAt(row, DB_NAME_COLUMN_INDEX) + "</strong><br>"
						+ m.getValueAt(row, TAG_COLUMN_INDEX) + "</html>";
			}
		};
		this.resultTable.setAutoCreateRowSorter(true);
		model.fireTableDataChanged();
		repaint();
	}

	private final String convertTags(final String serviceName) {
		final StringBuilder builder = new StringBuilder();
		final List<String> tags = manager.getTagMap().get(serviceName);
		for (final String tag : tags) {
			final String psimiName = tagManager.toName(tag);
			if (psimiName != null)
				builder.append(psimiName);
			else
				builder.append(tag);

			builder.append(", ");
		}

		final String nameString = builder.toString();
		return nameString.substring(0, nameString.length() - 2);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {
		resultScrollPane = new javax.swing.JScrollPane();

		buttonPanel = new javax.swing.JPanel();
		importNetworkButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		clearSelectionButton = new JButton();
		selectAllButton = new JButton();
		clusterResultCheckBox = new JCheckBox("Merge results into one network");
		clusterResultCheckBox.setSelected(false);

		resultScrollPane.setBackground(java.awt.Color.white);
		resultScrollPane.setViewportView(resultTable);

		buttonPanel.setBackground(java.awt.Color.white);

		importNetworkButton.setText("Import");
		importNetworkButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		importNetworkButton.setForeground(new Color(255, 0, 55));
		importNetworkButton.setPreferredSize(new java.awt.Dimension(70, 28));
		importNetworkButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				importButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Close");
		cancelButton.setPreferredSize(new java.awt.Dimension(70, 28));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		clearSelectionButton.setText("Clear");
		clearSelectionButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearButtonActionPerformed(evt);
			}
		});
		selectAllButton.setText("Select All");
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				selectAllButtonActionPerformed(evt);
			}
		});

		clusterResultCheckBox.setToolTipText("<html><h3>Cluster to single network</h3></html>");

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);
		buttonPanelLayout.setHorizontalGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						buttonPanelLayout.createSequentialGroup().addComponent(clearSelectionButton)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(selectAllButton)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(clusterResultCheckBox).addContainerGap(100, Short.MAX_VALUE)
								.addComponent(cancelButton).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(importNetworkButton).addContainerGap()));
		buttonPanelLayout.setVerticalGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						buttonPanelLayout
								.createSequentialGroup()
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(
										buttonPanelLayout
												.createParallelGroup(GroupLayout.Alignment.BASELINE)
												.addComponent(clearSelectionButton, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(selectAllButton, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(clusterResultCheckBox, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(importNetworkButton, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addContainerGap()));

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				// .addComponent(titlePanel, GroupLayout.DEFAULT_SIZE,
				// GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(resultScrollPane, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
				.addComponent(buttonPanel));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						// .addComponent(titlePanel, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.DEFAULT_SIZE,
						// GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(resultScrollPane, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)));

	}

	private void importButtonActionPerformed(ActionEvent evt) {
		final boolean mergeNetwork = clusterResultCheckBox.isSelected();
		final Set<String> targetSources = getSelected();
		final Set<String> sourceURLs = new HashSet<String>();
		// Filter out invalid selections:
		for (int i = 0; i < resultTable.getRowCount(); i++) {
			final String source = resultTable.getValueAt(i, DB_NAME_COLUMN_INDEX).toString();
			if (((Number) resultTable.getValueAt(i, RECORD_COUNT_COLUMN_INDEX)).intValue() != 0
					&& targetSources.contains(source)) {
				sourceURLs.add(manager.getActiveServices().get(source));
			}
		}

		// Execute Import Task
		final ImportNetworkFromPSICQUICTask networkTask = new ImportNetworkFromPSICQUICTask(query, client,
				networkManager, manager, sourceURLs, mode, createViewTaskFactory, vsBuilder, vmm, mergeNetwork);

		taskManager.execute(new TaskIterator(networkTask), this);

		final Window parentWindow = ((Window) getRootPane().getParent());
		parentWindow.pack();
		repaint();
		parentWindow.toFront();
	}

	private void cancelButtonActionPerformed(ActionEvent evt) {
		((Window) this.getRootPane().getParent()).dispose();
	}

	private void clearButtonActionPerformed(ActionEvent evt) {
		for (int i = 0; i < resultTable.getRowCount(); i++)
			resultTable.setValueAt(Boolean.FALSE, i, IMPORT_COLUMN_INDEX);
	}

	/**
	 * Select all check box
	 */
	private void selectAllButtonActionPerformed(ActionEvent evt) {
		for (int i = 0; i < resultTable.getRowCount(); i++) {
			if (((Number) resultTable.getValueAt(i, RECORD_COUNT_COLUMN_INDEX)).intValue() == 0)
				resultTable.setValueAt(Boolean.FALSE, i, IMPORT_COLUMN_INDEX);
			else
				resultTable.setValueAt(Boolean.TRUE, i, IMPORT_COLUMN_INDEX);
		}
	}

	void setSelected(final Set<String> sources) {
		for (int i = 0; i < resultTable.getRowCount(); i++) {
			final String dbName = resultTable.getValueAt(i, DB_NAME_COLUMN_INDEX).toString();
			if (sources.contains(dbName)) {
				// System.out.println("FOUND hit: " + dbName);
				resultTable.setValueAt(Boolean.TRUE, i, IMPORT_COLUMN_INDEX);
			} else {
				// System.out.println("DISABLE: " + dbName);
				resultTable.setValueAt(Boolean.FALSE, i, IMPORT_COLUMN_INDEX);
			}
		}
	}

	// Variables declaration - do not modify
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JButton importNetworkButton;
	private JButton clearSelectionButton;
	private JButton selectAllButton;
	private JCheckBox clusterResultCheckBox;

	private javax.swing.JScrollPane resultScrollPane;
	private javax.swing.JTable resultTable;

	private final class StatusTableModel extends DefaultTableModel {
		private static final long serialVersionUID = -7798626850196524108L;

		StatusTableModel() {
			super();
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			final int count = (Integer) getValueAt(row, RECORD_COUNT_COLUMN_INDEX);
			final String sourceName = getValueAt(row, DB_NAME_COLUMN_INDEX).toString();
			if (column == 0 && manager.isActive(sourceName) && count != 0)
				return true;
			else
				return false;
		}

		@Override
		public Class<?> getColumnClass(int colIdx) {
			if (colIdx == IMPORT_COLUMN_INDEX)
				return Boolean.class;
			else if (colIdx == RECORD_COUNT_COLUMN_INDEX)
				return Integer.class;
			else
				return String.class;
		}
	}

	private final class StringCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1694430839330920845L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {

			if (value == null) {
				this.setEnabled(false);
				return this;
			}

			final String serviceName = (String) table.getValueAt(row, DB_NAME_COLUMN_INDEX);
			final String statusString = (String) table.getValueAt(row, STATUS_COLUMN_INDEX);

			this.setText(value.toString());

			if (!manager.isActive(serviceName) || statusString.equals("Active") == false) {
				this.setForeground(Color.red);
				this.setEnabled(false);
			} else {
				this.setForeground(table.getForeground());
				this.setEnabled(true);
			}
			if (isSelected)
				this.setBackground(table.getSelectionBackground());
			else
				this.setBackground(table.getBackground());

			if (column == STATUS_COLUMN_INDEX || column == TAG_COLUMN_INDEX) {
				this.setHorizontalAlignment(SwingConstants.LEFT);
			} else {
				this.setHorizontalAlignment(SwingConstants.CENTER);
			}

			if (table.isEnabled() == false) {
				// Table is disabled. Grayed-out
				this.setForeground(Color.LIGHT_GRAY);
				this.setEnabled(false);
			}
			return this;
		}
	}

	private final class NumberCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 6889007019721032218L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {

			if (value == null || value instanceof Number == false) {
				this.setEnabled(false);
				return this;
			}
			this.setEnabled(true);

			Integer count;
			try {
				count = Integer.valueOf(value.toString());
			} catch (Exception e) {
				count = 0;
			}

			final String serviceName = (String) table.getValueAt(row, DB_NAME_COLUMN_INDEX);
			final String statusString = (String) table.getValueAt(row, STATUS_COLUMN_INDEX);

			this.setText(count.toString());

			if (!manager.isActive(serviceName) || statusString.equals("Active") == false) {
				this.setForeground(Color.red);
				this.setEnabled(false);
			} else if (count == 0) {
				this.setForeground(Color.LIGHT_GRAY);
				this.setEnabled(false);
			} else
				this.setForeground(table.getForeground());

			if (isSelected)
				this.setBackground(table.getSelectionBackground());
			else
				this.setBackground(table.getBackground());

			if (table.isEnabled() == false) {
				// Table is disabled. Grayed-out
				this.setForeground(Color.LIGHT_GRAY);
				this.setEnabled(false);
			}
			return this;
		}
	}

	/**
	 * Force to sort row
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void sort() {
		RowSorter<? extends TableModel> sorter = this.resultTable.getRowSorter();
		if (sorter instanceof DefaultRowSorter) {
			List list = new ArrayList<Object>();
			list.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
			sorter.setSortKeys(list);
			((DefaultRowSorter) sorter).sort();
		}
	}

	void setQuery(final String query) {
		this.query = query;
	}

	private Set<CyNetwork> results;

	@Override
	public void taskFinished(ObservableTask task) {
		if (task.getResults(Object.class) instanceof Set) {
			results = task.getResults(Set.class);
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		if (finishStatus.getType() == Type.SUCCEEDED) {
			final StringBuilder builder = new StringBuilder();
			builder.append("<html><h3>Networks created from the following databases:</h3><ul>");
			for (final CyNetwork network : results) {
				final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
				final Integer edgeCount = network.getEdgeCount();
				builder.append("<li>" + networkName + ", " + edgeCount + " edges</li>");
			}
			builder.append("</ul></html>");

			JOptionPane.showMessageDialog(this, builder.toString(), "Import Finished", JOptionPane.INFORMATION_MESSAGE,
					null);
		} else if (finishStatus.getType() == Type.CANCELLED) {
			final Set<String> sources = new HashSet<String>();
			final StringBuilder builder = new StringBuilder();
			builder.append("<html><h2 style=\"color:red\">Import Canceled</h2>" + 
					"<h3>Networks imported from the following databases:</h3><ul>");
			for (final CyNetwork network : results) {
				final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
				final Integer edgeCount = network.getEdgeCount();
				sources.add(network.getRow(network).get("source", String.class));
				builder.append("<li>" + networkName + ", " + edgeCount + " edges</li>");
			}
		
			builder.append("<h3 style=\"color:red\">Import canceled for the following databases:</h3><ul style=\"color:red\">");
			for (int i = 0; i < resultTable.getRowCount(); i++) {
				final String dbName = resultTable.getValueAt(i, DB_NAME_COLUMN_INDEX).toString();
				final Integer count = ((Number)resultTable.getValueAt(i, RECORD_COUNT_COLUMN_INDEX)).intValue();
				final Boolean isImport = (Boolean) resultTable.getValueAt(i, IMPORT_COLUMN_INDEX);
				if (sources.contains(dbName) == false && count != 0 && isImport) {
					builder.append("<li>" + dbName + "</li>");
				}
			}
			JOptionPane.showMessageDialog(this, builder.toString() + "</ul></html>", "Import Canceled",
					JOptionPane.ERROR_MESSAGE, null);
		} else {
			// Error!
			JOptionPane.showMessageDialog(this,  "<html>Error: Please try again later.<br><br>" + finishStatus.getException().getLocalizedMessage()
					+ "</html>", "Import Error",
					JOptionPane.ERROR_MESSAGE, null);
		}

		results = null;
	}
	
	private final String generatelist() {
			final Set<String> sources = new HashSet<String>();
			final StringBuilder builder = new StringBuilder();
			builder.append("<html><h3>Networks created from the following databases:</h3><ul>");
			for (final CyNetwork network : results) {
				final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
				final Integer edgeCount = network.getEdgeCount();
				sources.add(network.getRow(network).get("source", String.class));
				builder.append("<li>" + networkName + ", " + edgeCount + " edges</li>");
			}
			return builder.toString();
	}
}