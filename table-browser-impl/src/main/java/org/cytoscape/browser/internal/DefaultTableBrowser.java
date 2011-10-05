package org.cytoscape.browser.internal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.GUITaskManager;

public class DefaultTableBrowser extends AbstractTableBrowser {

	private static final long serialVersionUID = 627394119637512735L;

	private final JComboBox networkChooser;
	private final Class<? extends CyTableEntry> objType;

	public DefaultTableBrowser(String tabTitle, Class<? extends CyTableEntry> objType, CyTableManager tableManager,
			CyNetworkTableManager networkTableManager, CyServiceRegistrar serviceRegistrar, EquationCompiler compiler,
			OpenBrowser openBrowser, CyNetworkManager networkManager, TableTaskFactory deleteTableTaskFactoryService,
			GUITaskManager guiTaskManagerServiceRef, PopupMenuHelper popupMenuHelper,
			CyApplicationManager applicationManager) {
		super(tabTitle, tableManager, networkTableManager, serviceRegistrar, compiler, openBrowser, networkManager,
				deleteTableTaskFactoryService, guiTaskManagerServiceRef, popupMenuHelper, applicationManager);
		// TODO Auto-generated constructor stub
		this.objType = objType;

		networkChooser = new JComboBox();
		networkChooser.addActionListener(this);
		networkChooser.setSize(new Dimension(100, 20));
		this.attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler,
				deleteTableTaskFactoryService, guiTaskManagerServiceRef, networkChooser);

		add(attributeBrowserToolBar, BorderLayout.NORTH);
	}

	public void actionPerformed(final ActionEvent e) {
		final CyNetwork currentNetwork = this.applicationManager.getCurrentNetwork();
		final CyNetwork network = (CyNetwork) networkChooser.getSelectedItem();
		if (network == null || currentNetwork == network)
			return;

//		if (browserTableModel != null)
//			serviceRegistrar.unregisterAllServices(browserTableModel);
//
//		if (objType == CyNode.class)
//			currentTable = network.getDefaultNodeTable();
//		else if (objType == CyEdge.class)
//			currentTable = network.getDefaultEdgeTable();
//		else
//			currentTable = network.getDefaultNetworkTable();

		//showSelectedTable();
		
		applicationManager.setCurrentNetwork(network.getSUID());
	}

	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		final CyNetwork currentNetwork = e.getNetwork();

		if (browserTableModel != null)
			serviceRegistrar.unregisterAllServices(browserTableModel);

		if (objType == CyNode.class)
			currentTable = currentNetwork.getDefaultNodeTable();
		else if (objType == CyEdge.class)
			currentTable = currentNetwork.getDefaultEdgeTable();
		else
			currentTable = currentNetwork.getDefaultNetworkTable();
		networkChooser.setSelectedItem(currentNetwork);
		showSelectedTable();
		return;
	}
	
	
	
	@Override
	public void handleEvent(NetworkAddedEvent e) {
		CyNetwork network = e.getNetwork();
		System.out.println("Adding New Network: " + network);
		this.networkChooser.addItem(network);
		this.networkChooser.setSelectedItem(network);
	}

	@Override
	public void handleEvent(TableAboutToBeDeletedEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEvent(TableAddedEvent e) {
		// TODO Auto-generated method stub
		
	}

}
