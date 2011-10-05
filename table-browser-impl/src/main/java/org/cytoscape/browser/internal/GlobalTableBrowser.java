package org.cytoscape.browser.internal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.browser.internal.TableChooser.GlobalTableComboBoxModel;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.GUITaskManager;

public class GlobalTableBrowser extends AbstractTableBrowser {

	private static final long serialVersionUID = 2269984225983802421L;

	private static final Class<?>[] OBJECT_TYPES = {CyNode.class, CyEdge.class, CyNetwork.class};

	
	private final TableChooser tableChooser;
	

	public GlobalTableBrowser(String tabTitle, CyTableManager tableManager, CyNetworkTableManager networkTableManager,
			CyServiceRegistrar serviceRegistrar, EquationCompiler compiler, OpenBrowser openBrowser,
			CyNetworkManager networkManager, TableTaskFactory deleteTableTaskFactoryService,
			GUITaskManager guiTaskManagerServiceRef, PopupMenuHelper popupMenuHelper,
			CyApplicationManager applicationManager) {
		super(tabTitle, tableManager, networkTableManager, serviceRegistrar, compiler, openBrowser, networkManager,
				deleteTableTaskFactoryService, guiTaskManagerServiceRef, popupMenuHelper, applicationManager);
		// TODO Auto-generated constructor stub

		tableChooser = new TableChooser();
		tableChooser.addActionListener(this);
		tableChooser.setSize(new Dimension(100, 20));
		browserTable.setForeground(GLOBAL_TABLE_COLOR);
		this.attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler,
				deleteTableTaskFactoryService, guiTaskManagerServiceRef, tableChooser);

		add(attributeBrowserToolBar, BorderLayout.NORTH);
	}

	public void actionPerformed(final ActionEvent e) {

		final CyTable table = (CyTable) tableChooser.getSelectedItem();
		if (table == currentTable || table == null)
			return;

		if (browserTableModel != null)
			serviceRegistrar.unregisterAllServices(browserTableModel);

		currentTable = table;

		showSelectedTable();
	}

	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		comboBoxModel.removeItem(cyTable);
		tableToMetadataMap.remove(cyTable);
	}
	
	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel)tableChooser.getModel();
		final CyNetwork currentNetwork = e.getNetwork();

		if (currentTable == null) {
			return;
			//comboBoxModel.addAndSetSelectedItem(currentNetwork.getDefaultNodeTable());
		} else {
			Class<? extends CyTableEntry> tableType = null;
			// Determine which table type we're currently displaying:
			for (Class<? extends CyTableEntry> type : new Class[] { CyNetwork.class, CyNode.class, CyEdge.class }) {
				final Map<String, CyTable> tables = networkTableManager.getTables(currentNetwork, type);
				for (final CyTable table : tables.values()) {
					if (currentTable.getSUID() == table.getSUID()) {
						tableType = type;
						break;
					}
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
	 * 
	 * Note: This combo box only displays Global Table.
	 */
	@Override
	public void handleEvent(TableAddedEvent e) {
		
		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel)tableChooser.getModel();
		final CyTable newTable = e.getTable();
		
		if(isGlobalTable(newTable)) {
			comboBoxModel.addAndSetSelectedItem(newTable);
			System.out.println("New Table Added!!");
		}
	}
	
	
	private boolean isGlobalTable(final CyTable table) {
		
		final Set<CyTable> nonGlobalTables = new HashSet<CyTable>();
		final Set<CyNetwork> networks = this.networkManager.getNetworkSet();
		for(CyNetwork network: networks) {
			for(Class<?> type:OBJECT_TYPES) {
				final Map<String, CyTable> objTables = this.networkTableManager.getTables(network, (Class<? extends CyTableEntry>) type);
				nonGlobalTables.addAll(objTables.values());
			}
		}
		
		if(nonGlobalTables.contains(table))
			return false;
		else
			return true;
		
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		// TODO Auto-generated method stub
		
	}

}
