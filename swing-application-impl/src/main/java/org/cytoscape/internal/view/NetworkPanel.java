/*
 File: NetworkPanel.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.internal.task.NetworkCollectionTaskFactoryTunableAction;
import org.cytoscape.internal.task.NetworkTaskFactoryTunableAction;
import org.cytoscape.internal.task.NetworkViewCollectionTaskFactoryTunableAction;
import org.cytoscape.internal.task.NetworkViewTaskFactoryTunableAction;
import org.cytoscape.internal.task.TaskFactoryTunableAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.session.events.SetCurrentNetworkEvent;
import org.cytoscape.session.events.SetCurrentNetworkListener;
import org.cytoscape.session.events.SetCurrentNetworkViewEvent;
import org.cytoscape.session.events.SetCurrentNetworkViewListener;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.JTreeTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkPanel extends JPanel implements TreeSelectionListener,
		SetCurrentNetworkViewListener, SetCurrentNetworkListener,
		NetworkAddedListener, NetworkViewAddedListener, NetworkAboutToBeDestroyedListener,
		NetworkViewAboutToBeDestroyedListener, RowsSetListener {

	private final static long serialVersionUID = 1213748836763243L;

	static final Color WITH_VIEW = new Color(0x66, 0xCD, 0xAA, 100);
	static final Color WITH_VIEW_SELECTED = new Color(0x54, 0xFF, 0x9F, 170);
	static final Color WITHOUT_VIEW = new Color(0x69, 0x69, 0x69, 50);
	static final Color WITHOUT_VIEW_SELECTED = new Color(0x69, 0x69, 0x69, 170);
	static final Color FONT_COLOR = new Color(20, 20, 20);

	private static final int TABLE_ROW_HEIGHT = 32;
	static final Font TABLE_FONT = new Font("SansSerif", Font.PLAIN, 14);

	private static final Logger logger = LoggerFactory.getLogger(NetworkPanel.class);

	private final JTreeTable treeTable;
	private final NetworkTreeNode root;
	private JPanel navigatorPanel;
	private JSplitPane split;

	private final NetworkTreeTableModel treeTableModel;
	private final CyApplicationManager appManager;
	final CyNetworkManager netmgr;
	final CyNetworkViewManager networkViewManager;

	private final TaskManager taskManager;

	private final JPopupMenu popup;
	private final Map<TaskFactory, JMenuItem> popupMap;
	private final Map<TaskFactory, CyAction> popupActions;
	private final Map<CyTable, CyNetwork> nameTables;

	/**
	 * Constructor for the Network Panel.
	 * 
	 * @param desktop
	 */
	public NetworkPanel(final CyApplicationManager applicationManager,
			final CyNetworkManager netmgr, final CyNetworkViewManager networkViewManager,
			final BirdsEyeViewHandler bird, final TaskManager taskManager) {
		super();

		this.appManager = applicationManager;
		this.netmgr = netmgr;
		this.networkViewManager = networkViewManager;
		this.taskManager = taskManager;

		root = new NetworkTreeNode("Network Root", 0L);
		treeTableModel = new NetworkTreeTableModel(this, root);
		treeTable = new JTreeTable(treeTableModel);
		initialize();

		// create and populate the popup window
		popup = new JPopupMenu();
		popupMap = new HashMap<TaskFactory, JMenuItem>();
		popupActions = new HashMap<TaskFactory, CyAction>();
		nameTables = new HashMap<CyTable, CyNetwork>();

		setNavigator(bird.getBirdsEyeView());

		/*
		 * Remove CTR-A for enabling select all function in the main window.
		 */
		for (KeyStroke listener : treeTable.getRegisteredKeyStrokes()) {
			if (listener.toString().equals("ctrl pressed A")) {
				final InputMap map = treeTable.getInputMap();
				map.remove(listener);
				treeTable.setInputMap(WHEN_FOCUSED, map);
				treeTable.setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, map);
			}
		}
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(300, 700));

		treeTable.getTree().addTreeSelectionListener(this);
		treeTable.getTree().setRootVisible(false);

		ToolTipManager.sharedInstance().registerComponent(treeTable);

		treeTable.getTree().setCellRenderer(new TreeCellRenderer(this));

		treeTable.getColumn("Network").setPreferredWidth(100);
		treeTable.getColumn("Nodes").setPreferredWidth(45);
		treeTable.getColumn("Edges").setPreferredWidth(45);

		treeTable.setBackground(Color.WHITE);
		treeTable.setRowHeight(TABLE_ROW_HEIGHT);
		treeTable.setForeground(FONT_COLOR);

		navigatorPanel = new JPanel();
		navigatorPanel.setLayout(new BorderLayout());
		navigatorPanel.setPreferredSize(new Dimension(280, 280));
		navigatorPanel.setSize(new Dimension(280, 280));
		navigatorPanel.setBackground(Color.white);

		JScrollPane scroll = new JScrollPane(treeTable);

		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, navigatorPanel);
		split.setResizeWeight(1);
		split.setDividerLocation(300);

		add(split);

		// this mouse listener listens for the right-click event and will show
		// the pop-up window when that occurrs
		treeTable.addMouseListener(new PopupListener());
	}

	private void addFactory(TaskFactory factory, CyAction action) {
		final JMenuItem item = new JMenuItem(action);
		popupMap.put(factory, item);
		popupActions.put(factory, action);
		popup.add(item);
		popup.addPopupMenuListener(action);
	}

	private void removeFactory(TaskFactory factory) {
		JMenuItem item = popupMap.remove(factory);
		if (item != null)
			popup.remove(item);
		CyAction action = popupActions.remove(factory);
		if (action != null)
			popup.removePopupMenuListener(action);
	}

	public void addTaskFactory(TaskFactory factory, @SuppressWarnings("rawtypes") Map props) {
		addFactory(factory, new TaskFactoryTunableAction(taskManager, factory, props, appManager));
	}

	public void removeTaskFactory(TaskFactory factory, @SuppressWarnings("rawtypes") Map props) {
		removeFactory(factory);
	}

	public void addNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map props) {
		addFactory(factory, new NetworkCollectionTaskFactoryTunableAction(taskManager, factory,
				props, appManager));
	}

	public void removeNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map props) {
		removeFactory(factory);
	}

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory,
			Map props) {
		addFactory(factory, new NetworkViewCollectionTaskFactoryTunableAction(taskManager, factory,
				props, appManager));
	}

	public void removeNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory,
			Map props) {
		removeFactory(factory);
	}

	public void addNetworkTaskFactory(NetworkTaskFactory factory,
			@SuppressWarnings("rawtypes") Map props) {
		addFactory(factory, new NetworkTaskFactoryTunableAction(taskManager, factory, props,
				appManager));
	}

	public void removeNetworkTaskFactory(NetworkTaskFactory factory,
			@SuppressWarnings("rawtypes") Map props) {
		removeFactory(factory);
	}

	public void addNetworkViewTaskFactory(NetworkViewTaskFactory factory,
			@SuppressWarnings("rawtypes") Map props) {
		addFactory(factory, new NetworkViewTaskFactoryTunableAction(taskManager, factory, props,
				appManager));
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory factory,
			@SuppressWarnings("rawtypes") Map props) {
		removeFactory(factory);
	}

	public void setNavigator(final Component comp) {
		this.navigatorPanel.removeAll();
		this.navigatorPanel.add(comp, BorderLayout.CENTER);
	}

	/**
	 * This is used by Session writer.
	 * 
	 * @return
	 */
	public JTreeTable getTreeTable() {
		return treeTable;
	}

	public JPanel getNavigatorPanel() {
		return navigatorPanel;
	}

	/**
	 * Remove a network from the panel.
	 * 
	 * @param network_id
	 */
	public void removeNetwork(final Long network_id) {
		final NetworkTreeNode node = getNetworkNode(network_id);
		final Enumeration children = node.children();
		final List<NetworkTreeNode> removed_children = new ArrayList<NetworkTreeNode>();

		while (children.hasMoreElements()) {
			removed_children.add((NetworkTreeNode) children.nextElement());
		}

		for (NetworkTreeNode child : removed_children) {
			child.removeFromParent();
			root.add(child);
		}

		node.removeFromParent();
		treeTable.getTree().updateUI();
		treeTable.doLayout();
	}

	/**
	 * update a network title
	 * 
	 * @param network
	 */
	private void updateTitle(final CyNetwork network, final String name) {
		// updates the title in the network panel
		if (treeTable.getTree().getSelectionPath() != null) { // user has
			// selected
			// something
			treeTableModel.setValueAt(name, treeTable.getTree().getSelectionPath()
					.getLastPathComponent(), 0);
		} else { // no selection, means the title has been changed
			// programmatically
			NetworkTreeNode node = getNetworkNode(network.getSUID());
			treeTableModel.setValueAt(name, node, 0);
		}
		treeTable.getTree().updateUI();
		treeTable.doLayout();
	}

	// // Event handlers /////

	public void handleEvent(NetworkAboutToBeDestroyedEvent nde) {
		CyNetwork net = nde.getNetwork();
		logger.debug("Network about to be destroyed " + net.getSUID());
		removeNetwork(net.getSUID());
		nameTables.remove(net.getDefaultNetworkTable());
	}

	public void handleEvent(NetworkAddedEvent e) {
		CyNetwork net = e.getNetwork();
		logger.debug("Got NetworkAddedEvent.  Model ID = " + net.getSUID());

		addNetwork(net.getSUID(), -1l);
		nameTables.put(net.getDefaultNetworkTable(), net);
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		CyNetwork n = nameTables.get( e.getSource() );
		if ( n == null )
			return;
		
		final String title = n.getCyRow().get(CyTableEntry.NAME, String.class);
		updateTitle(n, title); // this should updated the UI regardless...
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		final CyNetworkView view = e.getNetworkView();

		if (view == null) {
			logger.warn("Current network view is set to null.");
			return;
		}

		logger.debug("Got SetCurrentNetworkViewEvent.  View ID = " + e.getNetworkView().getSUID());

		final long curr = e.getNetworkView().getModel().getSUID();

		focusNetworkNode(curr);
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		final CyNetwork cnet = e.getNetwork();
		if (cnet == null) {
			logger.warn("Got null for current network.");
			return;
		}

		logger.debug("Set current network " + cnet.getSUID());

		focusNetworkNode(cnet.getSUID());
	}

	public void handleEvent(NetworkViewAboutToBeDestroyedEvent nde) {
		logger.debug("Network view about to be destroyed "
				+ nde.getNetworkView().getModel().getSUID());
		treeTable.getTree().updateUI();
	}

	public void handleEvent(NetworkViewAddedEvent nde) {
		logger.debug("Network view added to NetworkPanel: "
				+ nde.getNetworkView().getModel().getSUID());

		// Set current network view to the new one.
		appManager.setCurrentNetworkView(nde.getNetworkView().getModel().getSUID());

		treeTable.getTree().updateUI();
	}

	private void addNetwork(final Long network_id, final Long parent_id) {
		// first see if it exists
		if (getNetworkNode(network_id) == null) {

			NetworkTreeNode dmtn = new NetworkTreeNode(netmgr.getNetwork(network_id).getCyRow()
					.get(CyTableEntry.NAME, String.class), network_id);

			if (parent_id != null && getNetworkNode(parent_id) != null) {
				getNetworkNode(parent_id).add(dmtn);
			} else {
				root.add(dmtn);
			}

			// apparently this doesn't fire valueChanged
			treeTable.getTree().collapsePath(new TreePath(new TreeNode[] { root }));

			treeTable.getTree().updateUI();
			TreePath path = new TreePath(dmtn.getPath());
			treeTable.getTree().expandPath(path);
			treeTable.getTree().scrollPathToVisible(path);
			treeTable.doLayout();

			// this is necessary because valueChanged is not fired above
			focusNetworkNode(network_id);
		} else {
			// logger.debug("addNetwork getNetworkTreeNode returned: " +
			// getNetworkNode(network_id).getNetworkID());
		}
	}

	public void focusNetworkNode(final Long networkID) {
		final DefaultMutableTreeNode node = getNetworkNode(networkID);

		if (node != null) {
			// fires valueChanged if the network isn't already selected
			treeTable.getTree().getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
			treeTable.getTree().scrollPathToVisible(new TreePath(node.getPath()));
		}
	}

	public NetworkTreeNode getNetworkNode(Long network_id) {
		Enumeration tree_node_enum = root.breadthFirstEnumeration();

		while (tree_node_enum.hasMoreElements()) {
			NetworkTreeNode node = (NetworkTreeNode) tree_node_enum.nextElement();

			if (node.getNetworkID().equals(network_id)) {
				return node;
			}
		}

		return null;
	}

	/**
	 * This method highlights a network in the NetworkPanel.
	 * 
	 * @param e DOCUMENT ME!
	 */
	public void valueChanged(TreeSelectionEvent e) {
		// logger.debug("NetworkPanel: valueChanged - " +
		// e.getSource().getClass().getName());
		JTree mtree = treeTable.getTree();

		// sets the "current" network based on last node in the tree selected
		NetworkTreeNode node = (NetworkTreeNode) mtree.getLastSelectedPathComponent();
		if (node == null || node.getUserObject() == null) {
			// logger.debug("NetworkPanel: null node - returning");
			return;
		}

		appManager.setCurrentNetwork(node.getNetworkID());

		// creates a list of all selected networks
		List<Long> networkList = new LinkedList<Long>();
		try {
			for (int i = mtree.getMinSelectionRow(); i <= mtree.getMaxSelectionRow(); i++) {
				NetworkTreeNode n = (NetworkTreeNode) mtree.getPathForRow(i).getLastPathComponent();
				if (n != null && n.getUserObject() != null && mtree.isRowSelected(i))
					networkList.add(n.getNetworkID());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (networkList.size() > 0)
			appManager.setSelectedNetworkViews(networkList);
	}

	/**
	 * This class listens to mouse events from the TreeTable, if the mouse event
	 * is one that is canonically associated with a popup menu (ie, a right
	 * click) it will pop up the menu with option for destroying view, creating
	 * view, and destroying network (this is platform specific apparently)
	 */
	private class PopupListener extends MouseAdapter {
		/**
		 * Don't know why you need both of these, but this is how they did it in
		 * the example
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * Don't know why you need both of these, but this is how they did it in
		 * the example
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * if the mouse press is of the correct type, this function will maybe
		 * display the popup
		 */
		private void maybeShowPopup(MouseEvent e) {
			// check for the popup type
			if (e.isPopupTrigger()) {
				// get the row where the mouse-click originated
				int row = treeTable.rowAtPoint(e.getPoint());

				if (row != -1) {
					JTree tree = treeTable.getTree();
					TreePath treePath = tree.getPathForRow(row);
					Long networkID = ((NetworkTreeNode) treePath.getLastPathComponent())
							.getNetworkID();

					CyNetwork cyNetwork = netmgr.getNetwork(networkID);

					if (cyNetwork != null) {
						// enable/disable any actions based on state of system
						for (CyAction action : popupActions.values())
							action.updateEnableState();

						// then popup menu
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		}
	}
}
