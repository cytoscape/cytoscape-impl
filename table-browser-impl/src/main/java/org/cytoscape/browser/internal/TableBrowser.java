package org.cytoscape.browser.internal;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.GUITaskManager;


public class TableBrowser extends JPanel implements CytoPanelComponent, ActionListener, TableAboutToBeDeletedListener,
		SetCurrentNetworkListener, TableAddedListener {
	
	private static final long serialVersionUID = 1968196123280466989L;
	
	private final CyTableManager tableManager;
	private final CyServiceRegistrar serviceRegistrar;
	private final EquationCompiler compiler;
	private final BrowserTable browserTable;
	private final AttributeBrowserToolBar attributeBrowserToolBar;
	private final TableChooser tableChooser;
	private BrowserTableModel browserTableModel;
	private CyTable currentTable;
	private final TableTaskFactory deleteTableTaskFactoryService;
	private final GUITaskManager guiTaskManagerServiceRef;
	private final Map<CyTable, TableMetadata> tableToMetadataMap;
	private final CyApplicationManager applicationManager;

	TableBrowser(final CyTableManager tableManager, final CyServiceRegistrar serviceRegistrar,
		     final EquationCompiler compiler, final OpenBrowser openBrowser,
		     final CyNetworkManager networkManager,
		     final TableTaskFactory deleteTableTaskFactoryService,
		     final GUITaskManager guiTaskManagerServiceRef,
		     final PopupMenuHelper popupMenuHelper,
		     final CyApplicationManager applicationManager)
	{
		this.tableManager = tableManager;
		this.serviceRegistrar = serviceRegistrar;
		this.compiler = compiler;

		this.deleteTableTaskFactoryService = deleteTableTaskFactoryService;
		this.guiTaskManagerServiceRef = guiTaskManagerServiceRef;
		this.tableToMetadataMap = new HashMap<CyTable, TableMetadata>();
		this.applicationManager = applicationManager;

		this.browserTable = new BrowserTable(openBrowser, compiler, popupMenuHelper);
		this.attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler,
				this.deleteTableTaskFactoryService, this.guiTaskManagerServiceRef);
		this.setLayout(new BorderLayout());

		browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tableChooser = new TableChooser(tableManager, networkManager);
		tableChooser.addActionListener(this);
		add(tableChooser, BorderLayout.SOUTH);
		browserTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
		add(new JScrollPane(browserTable), BorderLayout.CENTER);
		add(attributeBrowserToolBar, BorderLayout.NORTH);
	}

	/**
	 * Returns the Component to be added to the CytoPanel.
	 * @return The Component to be added to the CytoPanel.
	 */
	public Component getComponent() { return this; }

	/**
	 * Returns the name of the CytoPanel that this component should be added to.
	 * @return the name of the CytoPanel that this component should be added to.
	 */
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	/**
	 * Returns the title of the tab within the CytoPanel for this component.
	 * @return the title of the tab within the CytoPanel for this component.
	 */
	public String getTitle() { return "Attribute Browser"; }

	/**
	 * @return null
	 */
	public Icon getIcon() { return null; }

	public void actionPerformed(final ActionEvent e) {
		final CyTable table = (CyTable)tableChooser.getSelectedItem();
		if (table == currentTable || table == null)
			return;

		if (browserTableModel != null) {
			serviceRegistrar.unregisterAllServices(browserTableModel);
		}

		currentTable = table;
		browserTableModel = new BrowserTableModel(browserTable, table, compiler);
		serviceRegistrar.registerAllServices(browserTableModel, new Properties());
		browserTable.setUpdateComparators(false);
		browserTable.setModel(browserTableModel);
		final TableRowSorter rowSorter = new TableRowSorter(browserTableModel);
		browserTable.setRowSorter(rowSorter);
		updateColumnComparators(rowSorter);
		browserTable.setUpdateComparators(true);
		attributeBrowserToolBar.setBrowserTableModel(browserTableModel);
		final TableMetadata tableMetadata = tableToMetadataMap.get(currentTable);
		if (tableMetadata != null) {
			final JTable jTable = browserTableModel.getTable();
			final TableColumnModel columnModel = jTable.getColumnModel();
			final Iterator<ColumnDescriptor> columnDescIter =
				tableMetadata.getColumnDescriptors();
			while (columnDescIter.hasNext()) {
				final ColumnDescriptor desc = columnDescIter.next();
				final int savedColumnIndex = desc.getColumnIndex();
				final TableColumn tableColumn = columnModel.getColumn(savedColumnIndex);
				tableColumn.setPreferredWidth(desc.getColumnWidth());
				final int currentColumnIndex =
					jTable.convertColumnIndexToView(
						browserTableModel.mapColumnNameToColumnIndex(desc.getColumnName()));
				if (currentColumnIndex != savedColumnIndex)
					jTable.moveColumn(currentColumnIndex, savedColumnIndex);
			}
		}

		applicationManager.setCurrentTable(currentTable);
	}

	public void updateColumnComparators(final TableRowSorter rowSorter) {
		for (int column = 0; column < browserTableModel.getColumnCount(); ++column)
			rowSorter.setComparator(
				column,
				new ValidatedObjectAndEditStringComparator(
					browserTableModel.getColumn(column).getType()));
	}

	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		final MyComboBoxModel comboBoxModel = (MyComboBoxModel)tableChooser.getModel();
		comboBoxModel.removeItem(cyTable);
		tableToMetadataMap.remove(cyTable);
	}

	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		final MyComboBoxModel comboBoxModel = (MyComboBoxModel)tableChooser.getModel();
		final CyNetwork currentNetwork = e.getNetwork();

		if (currentTable == null) {
			comboBoxModel.addAndSetSelectedItem(currentNetwork.getDefaultNodeTable());
		} else {
			// Determine which table type we're currently displaying:
			final Set<CyTableMetadata> tableMetadataSet =
				tableManager.getAllTables(/* includePrivate = */false);
			Class<?> tableType = null;
			for (final CyTableMetadata tableMetadata : tableMetadataSet) {
				if (currentTable.getSUID() == tableMetadata.getCyTable().getSUID()) {
					tableType = tableMetadata.getType();
					break;
				}
			}

			final CyTable tableToSelect;
			if (tableType == CyEdge.class)
				tableToSelect = currentNetwork.getDefaultEdgeTable();
			else if (tableType == CyNetwork.class)
				tableToSelect = currentNetwork.getDefaultNetworkTable();
			else
				tableToSelect = currentNetwork.getDefaultNodeTable();
			comboBoxModel.addAndSetSelectedItem(tableToSelect);
		}
	}

	/**
	 * Switch to new table when it is registered to the table manager.
	 */
	@Override
	public void handleEvent(TableAddedEvent e) {
		final MyComboBoxModel comboBoxModel = (MyComboBoxModel)tableChooser.getModel();
		final CyTable newTable = e.getTable();
		comboBoxModel.addAndSetSelectedItem(newTable);
	}
}