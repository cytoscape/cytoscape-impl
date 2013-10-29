package org.cytoscape.browser.internal;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

import static org.cytoscape.browser.internal.IconManager.ICON_COG;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;


public class DefaultTableBrowser extends AbstractTableBrowser implements SetCurrentNetworkListener,
		NetworkAddedListener, NetworkAboutToBeDestroyedListener, TableAboutToBeDeletedListener,
		ColumnCreatedListener, ColumnDeletedListener {

	private static final long serialVersionUID = 627394119637512735L;

	private final JButton selectionModeButton;
	private JPopupMenu displayMode;
	
	private final JComboBox networkChooser;
	private final Class<? extends CyIdentifiable> objType;

	private BrowserTableModel.ViewMode rowSelectionMode = BrowserTableModel.ViewMode.AUTO;
	private boolean ignoreSetCurrentNetwork = true;
	
	public DefaultTableBrowser(final String tabTitle,
							   final Class<? extends CyIdentifiable> objType,
							   final CyTableManager tableManager,
							   final CyNetworkTableManager networkTableManager,
							   final CyServiceRegistrar serviceRegistrar,
							   final EquationCompiler compiler,
							   final CyNetworkManager networkManager,
							   final DeleteTableTaskFactory deleteTableTaskFactory,
							   final DialogTaskManager guiTaskManager,
							   final PopupMenuHelper popupMenuHelper,
							   final CyApplicationManager applicationManager,
							   final CyEventHelper eventHelper,
							   final IconManager iconManager) {//, final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService) {
		super(tabTitle, tableManager, networkTableManager, serviceRegistrar, compiler, networkManager,
				deleteTableTaskFactory, guiTaskManager, popupMenuHelper, applicationManager, eventHelper);

		this.objType = objType;

		networkChooser = new JComboBox();
		networkChooser.setRenderer(new NetworkChooserCustomRenderer());
		networkChooser.addActionListener(this);
		networkChooser.setMaximumSize(SELECTOR_SIZE);
		networkChooser.setMinimumSize(SELECTOR_SIZE);
		networkChooser.setPreferredSize(SELECTOR_SIZE);
		networkChooser.setSize(SELECTOR_SIZE);
		networkChooser.setEnabled(false);
		
		createPopupMenu();
		selectionModeButton = new JButton(ICON_COG);
		selectionModeButton.setToolTipText("Change Table Mode");
		AttributeBrowserToolBar.styleButton(selectionModeButton,
				iconManager.getIconFont(AttributeBrowserToolBar.ICON_FONT_SIZE * 4/5));
		
		selectionModeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DefaultTableBrowser.this.actionPerformed(e);
				displayMode.show(selectionModeButton, 0, selectionModeButton.getHeight());
			}
		});
		
		attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler,
				deleteTableTaskFactory, guiTaskManager, networkChooser, selectionModeButton, objType,
				applicationManager, iconManager);// , mapGlobalTableTaskFactoryService);
		
		add(attributeBrowserToolBar, BorderLayout.NORTH);
	}
	
	private void createPopupMenu() {
		
		displayMode = new JPopupMenu();
		final JCheckBoxMenuItem displayAuto = new JCheckBoxMenuItem("Auto");
		displayAuto.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.AUTO);
		final JCheckBoxMenuItem displayAll = new JCheckBoxMenuItem("Show all");
		displayAll.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.ALL);
		final JCheckBoxMenuItem displaySelect = new JCheckBoxMenuItem("Show selected");
		displaySelect.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.SELECTED);

		displayAuto.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rowSelectionMode = BrowserTableModel.ViewMode.AUTO;
				changeSelectionMode();

				displayAuto.setSelected(true);
				displayAll.setSelected(false);
				displaySelect.setSelected(false);
			}
		});
		
		
		displayAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rowSelectionMode = BrowserTableModel.ViewMode.ALL;
				changeSelectionMode();

				displayAuto.setSelected(false);
				displayAll.setSelected(true);
				displaySelect.setSelected(false);
			}
		});
		
		displaySelect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rowSelectionMode = BrowserTableModel.ViewMode.SELECTED;
				changeSelectionMode();
				
				displayAuto.setSelected(false);
				displayAll.setSelected(false);
				displaySelect.setSelected(true);
			}
		});
		
		displayMode.add(displayAuto);
		displayMode.add(displayAll);
		displayMode.add(displaySelect);
	}

	private void changeSelectionMode() {
		//rowSelectionMode = selectionModeButton.isSelected();
		BrowserTableModel model = (BrowserTableModel) getCurrentBrowserTable().getModel();
		model.setViewMode(rowSelectionMode);
		model.updateViewMode();
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (!ignoreSetCurrentNetwork) {
			final CyNetwork network = (CyNetwork) networkChooser.getSelectedItem();
			final CyNetwork currentNetwork = applicationManager.getCurrentNetwork();
			
			if (network != null && !network.equals(currentNetwork) && networkManager.networkExists(network.getSUID())) {
				applicationManager.setCurrentNetwork(network);
			}
		}
	}

	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		final CyNetwork currentNetwork = e.getNetwork();
		
		if (currentNetwork != null) {
			if (objType == CyNode.class) {
				currentTable = currentNetwork.getDefaultNodeTable();
			} else if (objType == CyEdge.class) {
				currentTable = currentNetwork.getDefaultEdgeTable();
			} else {
				currentTable = currentNetwork.getDefaultNetworkTable();
			}
			currentTableType = objType;
		} else {
			currentTable = null;
			currentTableType = null;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CyNetwork selectedNetwork = (CyNetwork) networkChooser.getSelectedItem();
				
				if ((currentNetwork == null && selectedNetwork != null)
						|| (currentNetwork != null && !currentNetwork.equals(selectedNetwork))) {
					ignoreSetCurrentNetwork = true;
					networkChooser.setSelectedItem(currentNetwork);
					ignoreSetCurrentNetwork = false;
				}
			}
		});
		
		final BrowserTable currentBrowserTable = getCurrentBrowserTable();
		
		if (currentBrowserTable != null) {
			final BrowserTableModel model = (BrowserTableModel) currentBrowserTable.getModel();
			model.setViewMode(rowSelectionMode);
		}
		
		showSelectedTable();
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		final CyNetwork network = e.getNetwork();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ignoreSetCurrentNetwork = true;

				try {
					networkChooser.addItem(network);
				} finally {
					ignoreSetCurrentNetwork = false;
				}

				//attributeBrowserToolBar.initializeColumns();
			}
		});
	}

	

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		final CyNetwork network = e.getNetwork();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ignoreSetCurrentNetwork = true;
				
				try {
					networkChooser.removeItem(network);
				} finally {
					ignoreSetCurrentNetwork = false;
				}
			}
		});
	}
	
	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		deleteTable(cyTable);
	}
	
	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		if (e.getSource() == currentTable)
			attributeBrowserToolBar.updateEnableState();
	}

	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		if (e.getSource() == currentTable)
			attributeBrowserToolBar.updateEnableState();
	}
	
	private static final class NetworkChooserCustomRenderer extends JLabel implements ListCellRenderer {

		private static final long serialVersionUID = 7103666112352192698L;

		@Override
		public Component getListCellRendererComponent(JList list, Object item, int index, boolean isSelected,
				boolean hasFocus) {
			
			if (item instanceof CyNetwork == false) {
				this.setText("No Network");
				return this;
			}
			
			final CyNetwork network = (CyNetwork) item;
			
			if (isSelected || hasFocus) {
				this.setBackground(list.getSelectionBackground());				
				this.setForeground(list.getSelectionForeground());
			} else {
				this.setBackground(list.getBackground());				
				this.setForeground(list.getForeground());
			}
			
			setOpaque(true);
			
			// When a network is deleted, its tables are also deleted, but it's
			// possible that we still have a reference to a network with no tables.
			try {
				this.setText(network.getRow(network).get(CyNetwork.NAME, String.class));
			} catch (NullPointerException e) {
			}
			
			return this;
		}
	}
}
