package org.cytoscape.browser.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
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
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.GUITaskManager;

public class GlobalTableBrowser extends AbstractTableBrowser implements TableAboutToBeDeletedListener, TableAddedListener {

	private static final long serialVersionUID = 2269984225983802421L;

	private static final Class<?>[] OBJECT_TYPES = {CyNode.class, CyEdge.class, CyNetwork.class};

	static final Color GLOBAL_TABLE_COLOR = new Color(0x1E, 0x90, 0xFF);
	static final Color GLOBAL_TABLE_ENTRY_COLOR = new Color(0x1E, 0x90, 0xFF, 150);
	static final Color GLOBAL_TABLE_BACKGROUND_COLOR = new Color(0x87, 0xCE, 0xFA, 50);
	static final Font GLOBAL_FONT = new Font("SansSerif", Font.BOLD, 12);
	
	private final TableChooser tableChooser;

	public GlobalTableBrowser(String tabTitle, CyTableManager tableManager, CyNetworkTableManager networkTableManager,
			CyServiceRegistrar serviceRegistrar, EquationCompiler compiler, OpenBrowser openBrowser,
			CyNetworkManager networkManager, TableTaskFactory deleteTableTaskFactoryService,
			GUITaskManager guiTaskManagerServiceRef, PopupMenuHelper popupMenuHelper,
			CyApplicationManager applicationManager) {
		super(tabTitle, tableManager, networkTableManager, serviceRegistrar, compiler, openBrowser, networkManager,
				deleteTableTaskFactoryService, guiTaskManagerServiceRef, popupMenuHelper, applicationManager);

		tableChooser = new TableChooser();
		tableChooser.addActionListener(this);
		tableChooser.setMaximumSize(SELECTOR_SIZE);
		tableChooser.setMinimumSize(SELECTOR_SIZE);
		tableChooser.setPreferredSize(SELECTOR_SIZE);
		tableChooser.setSize(SELECTOR_SIZE);
		tableChooser.setFont(GLOBAL_FONT);
		tableChooser.setForeground(GLOBAL_TABLE_COLOR);
		tableChooser.setToolTipText("\"Global Tables\" are data tables not associated with specific networks.");
		tableChooser.setEnabled(false);
		
		browserTable.setForeground(GLOBAL_TABLE_ENTRY_COLOR);
		browserTable.setEnabled(false);
		
		attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler,
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
		
		if(comboBoxModel.getSize() == 0)
			tableChooser.setEnabled(false);
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
		
		if(isGlobalTable(newTable))
			comboBoxModel.addAndSetSelectedItem(newTable);
		
		if(tableChooser.getItemCount() != 0)
			tableChooser.setEnabled(true);
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
}
