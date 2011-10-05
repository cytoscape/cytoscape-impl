package org.cytoscape.browser.internal;

import static org.cytoscape.browser.internal.AbstractTableBrowser.SELECTED_ITEM_BACKGROUND_COLOR;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.GUITaskManager;

public class DefaultTableBrowser extends AbstractTableBrowser implements SetCurrentNetworkListener, NetworkAddedListener, NetworkAboutToBeDestroyedListener {

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

		this.objType = objType;

		networkChooser = new JComboBox();
		networkChooser.setRenderer(new NetworkChooserCustomRenderer());
		networkChooser.addActionListener(this);
		networkChooser.setMaximumSize(SELECTOR_SIZE);
		networkChooser.setMinimumSize(SELECTOR_SIZE);
		networkChooser.setPreferredSize(SELECTOR_SIZE);
		networkChooser.setSize(SELECTOR_SIZE);
		networkChooser.setEnabled(false);
		
		this.attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler,
				deleteTableTaskFactoryService, guiTaskManagerServiceRef, networkChooser);

		add(attributeBrowserToolBar, BorderLayout.NORTH);
	}

	public void actionPerformed(final ActionEvent e) {
		final CyNetwork currentNetwork = this.applicationManager.getCurrentNetwork();
		final CyNetwork network = (CyNetwork) networkChooser.getSelectedItem();
		if (network == null || currentNetwork == network)
			return;
		
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
	}
	
	
	@Override
	public void handleEvent(NetworkAddedEvent e) {
		final CyNetwork network = e.getNetwork();
		this.networkChooser.addItem(network);
		this.networkChooser.setSelectedItem(network);
		
		if(networkChooser.isEnabled() == false)
			networkChooser.setEnabled(true);
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		final CyNetwork network = e.getNetwork();
		this.networkChooser.removeItem(network);
		
		if(networkChooser.getItemCount() == 0)
			networkChooser.setEnabled(false);
	}
	
	private static final class NetworkChooserCustomRenderer extends JLabel implements ListCellRenderer {

		private static final long serialVersionUID = 7103666112352192698L;

		@Override
		public Component getListCellRendererComponent(JList list, Object item, int index, boolean isSelected,
				boolean hasFocus) {
			
			if(item instanceof CyNetwork == false) {
				this.setText("No Network");
				return this;
			}
			
			final CyNetwork network = (CyNetwork) item;
			if(isSelected || hasFocus) {
				setBackground(SELECTED_ITEM_BACKGROUND_COLOR);
			} else {
				setBackground(Color.WHITE);
			}
			
			this.setText(network.getCyRow().get(CyTableEntry.NAME, String.class));
			return this;
		}

	}

}
