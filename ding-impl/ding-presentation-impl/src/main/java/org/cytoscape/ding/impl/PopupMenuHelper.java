/*
 Copyright (c) 2009, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.ding.impl;


import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.NodeView;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.util.swing.JMenuTracker;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskFactoryPredicate;
import org.cytoscape.work.swing.DynamicSubmenuListener;


// TODO Consider generalizing this class so that it can be used by anyone
// who needs a popup menu based on TaskFactories.

/**
 * A class that encapsulates the creation of JPopupMenus based
 * on TaskFactory services.
 */
class PopupMenuHelper {
	private double largeValue = Double.MAX_VALUE / 2.0;

	// provides access to the necessary task factories and managers
	private DGraphView m_view;

	// the component we should create the popup menu on
	private Component invoker;

	PopupMenuHelper(DGraphView v, Component inv) {
		m_view = v;
		invoker = inv;
	}

	/**
	 * Creates a menu based on the EdgeView.
	 */
	void createEdgeViewMenu(CyNetwork network, EdgeView edgeView, int x, int y, String action) {
		if (edgeView != null ) {

			Collection<EdgeViewTaskFactory> usableTFs = getPreferredActions(m_view.edgeViewTFs,action);
			View<CyEdge> ev = (DEdgeView)edgeView;

			// build a menu of actions if more than factory exists
			if ( usableTFs.size() > 1) {
				String edgeLabel = network.getRow(ev.getModel()).get("interaction",String.class);
				JPopupMenu menu = new JPopupMenu(edgeLabel);
				JMenuTracker tracker = new JMenuTracker(menu);

				for ( EdgeViewTaskFactory evtf : usableTFs ) {
					evtf.setEdgeView(ev,m_view);
					createMenuItem(ev, menu, evtf, tracker, m_view.edgeViewTFs.get(evtf) );
				}

				menu.show(invoker, x, y);

			// execute the task directly if only one factory exists
			} else if ( usableTFs.size() == 1) {
				EdgeViewTaskFactory tf  = usableTFs.iterator().next();
				tf.setEdgeView(ev,m_view);
				executeTask(tf);
			}
		}
	}

	/**
	 * Creates a menu based on a drop event on a NodeView.
	 */
	void createDropNodeViewMenu(CyNetwork network, NodeView nview, Point rawPt, Point xformPt, Transferable t, String action) {
		if (nview != null ) {
			Collection<DropNodeViewTaskFactory> usableTFs = getPreferredActions(m_view.dropNodeViewTFs,action);
			View<CyNode> nv = (DNodeView)nview;

			// build a menu of actions if more than factory exists
			if ( usableTFs.size() > 1) {
				String nodeLabel = network.getRow(nv.getModel()).get("name",String.class);
				JPopupMenu menu = new JPopupMenu(nodeLabel);
				JMenuTracker tracker = new JMenuTracker(menu);

				for ( DropNodeViewTaskFactory nvtf : usableTFs ) {
					nvtf.setNodeView(nv,m_view);
					nvtf.setDropInformation(t,rawPt,xformPt);
					createMenuItem(nv, menu, nvtf, tracker, m_view.dropNodeViewTFs.get( nvtf ));
				}

				menu.show(invoker, (int)(rawPt.getX()), (int)(rawPt.getY()));

			// execute the task directly if only one factory exists
			} else if ( usableTFs.size() == 1) {
				DropNodeViewTaskFactory tf  = usableTFs.iterator().next();
				tf.setNodeView(nv,m_view);
				tf.setDropInformation(t,rawPt,xformPt);
				executeTask(tf);
			}
		}
	}

	/**
	 * Creates a menu based on the NodeView.
	 */
	void createNodeViewMenu(CyNetwork network, NodeView nview, int x, int y , String action) {
		if (nview != null ) {
			Collection<NodeViewTaskFactory> usableTFs = getPreferredActions(m_view.nodeViewTFs,action);
			View<CyNode> nv = (DNodeView)nview;

			// build a menu of actions if more than factory exists
			if ( usableTFs.size() > 1) {
				String nodeLabel = network.getRow(nv.getModel()).get("name",String.class);
				JPopupMenu menu = new JPopupMenu(nodeLabel);
				JMenuTracker tracker = new JMenuTracker(menu);

				for ( NodeViewTaskFactory nvtf : usableTFs ) {
					nvtf.setNodeView(nv, m_view);
					createMenuItem(nv, menu, nvtf, tracker, m_view.nodeViewTFs.get( nvtf ));
				}

				menu.show(invoker, x, y);

			// execute the task directly if only one factory exists
			} else if ( usableTFs.size() == 1) {
				NodeViewTaskFactory tf  = usableTFs.iterator().next();
				tf.setNodeView(nv, m_view);
				executeTask(tf);
			}
		}
	}

	/**
	 * Creates a menu based on the NetworkView.
	 */
	void createDropEmptySpaceMenu(Point rawPt, Point xformPt, Transferable t,String action) {
		// build a menu of actions if more than factory exists
		Collection<DropNetworkViewTaskFactory> usableTFs = getPreferredActions(m_view.dropEmptySpaceTFs,action);
		if ( usableTFs.size() > 1 ) {
			JPopupMenu menu = new JPopupMenu("Double Click Menu: empty");
			JMenuTracker tracker = new JMenuTracker(menu);
			for ( DropNetworkViewTaskFactory nvtf : usableTFs ) {
				nvtf.setNetworkView(m_view);
				nvtf.setDropInformation(t,rawPt,xformPt);
				createMenuItem(null, menu, nvtf, tracker, m_view.dropEmptySpaceTFs.get( nvtf ) );
			}
			menu.show(invoker, (int)(rawPt.getX()), (int)(rawPt.getY()));
		// execute the task directly if only one factory exists
		} else if ( usableTFs.size() == 1) {
			DropNetworkViewTaskFactory tf = usableTFs.iterator().next();
			tf.setNetworkView(m_view);
			tf.setDropInformation(t,rawPt,xformPt);
			executeTask(tf);
		}
	}
	/**
	 * Creates a menu based on the NetworkView.
	 */
	void createEmptySpaceMenu(int x, int y, String action) {
		// build a menu of actions if more than factory exists
		Collection<NetworkViewTaskFactory> usableTFs = getPreferredActions(m_view.emptySpaceTFs,action);
		if ( usableTFs.size() > 1 || (usableTFs.size() == 1 && action.equals("NEW"))) {
			final JPopupMenu menu = new JPopupMenu("Double Click Menu: empty");
			final JMenuTracker tracker = new JMenuTracker(menu);
			for ( NetworkViewTaskFactory nvtf : usableTFs ) {
				nvtf.setNetworkView(m_view);
				createMenuItem(null, menu, nvtf, tracker, m_view.emptySpaceTFs.get( nvtf ) );
			}
			menu.show(invoker, x, y);
		// execute the task directly if only one factory exists
		} else if ( usableTFs.size() == 1) {
			NetworkViewTaskFactory tf = usableTFs.iterator().next();
			tf.setNetworkView(m_view);
			executeTask(tf);
		}
	}

	/**
	 * This method creates popup menu submenus and menu items based on the
	 * "title" and "preferredMenu" keywords, depending on which are present
	 * in the service properties.
	 */
	private void createMenuItem(View<?> view, JPopupMenu popup, TaskFactory tf,
	                            JMenuTracker tracker, Map props) {

		String title = (String)(props.get("title"));
		String pref = (String)(props.get("preferredMenu"));
		String toolTip = (String) (props.get("tooltip"));

		// check if the menus are created dynamically, and if so add the listener
		final Object preferredTaskManager = props.get("preferredTaskManager");
		if ( preferredTaskManager != null && preferredTaskManager.toString().equals("menu")) {
			if ( title == null )
				title = "Dynamic";
			DynamicSubmenuListener submenu = m_view.menuTaskManager.getConfiguration(tf);
	        submenu.setMenuTitle(title);
			popup.addPopupMenuListener( submenu );
			return;
		}

		// otherwise create our own popup menus 
		boolean useCheckBoxMenuItem = false;

		final Object useCheckBox = props.get("useCheckBoxMenuItem");
		final Object targetVisualProperty = props.get("targetVP");
		boolean isSelected = false;
		if(view != null) {
			if (targetVisualProperty != null && targetVisualProperty instanceof String ) {
				// TODO remove this at first opportunity whenever lookup gets refactored. 
				Class<?> clazz = CyNetwork.class;
				if ( view.getModel() instanceof CyNode )
					clazz = CyNode.class;
				else if ( view.getModel() instanceof CyEdge )
					clazz = CyEdge.class;

				final VisualProperty<?> vp = m_view.dingLexicon.lookup(clazz, targetVisualProperty.toString());
				if (vp == null)
					isSelected = false;
				else
					isSelected = view.isValueLocked(vp);
			} else if ( targetVisualProperty instanceof VisualProperty )
				isSelected = view.isValueLocked((VisualProperty<?>)targetVisualProperty);
		}

		if ( useCheckBox != null ) {
			try {
				useCheckBoxMenuItem = Boolean.parseBoolean(useCheckBox.toString());
			} catch (Exception e) {
				useCheckBoxMenuItem = false;
			}
		} else {
			useCheckBoxMenuItem = false;
		}

		// no title and no preferred menu
		if ( title == null && pref == null ) {
			title = "Unidentified Task: " + Integer.toString(tf.hashCode());
			popup.add( createMenuItem(tf, title, useCheckBoxMenuItem, toolTip) );

		// title, but no preferred menu
		} else if ( title != null && pref == null ) {
			popup.add( createMenuItem(tf, title, useCheckBoxMenuItem, toolTip) );

		// no title, but preferred menu
		} else if ( title == null && pref != null ) {
			int last = pref.lastIndexOf(".");

			// if the preferred menu is delimited
			if (last > 0) {
				title = pref.substring(last + 1);
				pref = pref.substring(0, last);
				final GravityTracker gravityTracker = tracker.getGravityTracker(pref);
				final JMenuItem item = createMenuItem(tf, title,useCheckBoxMenuItem, toolTip);
				if (useCheckBoxMenuItem) {
					final JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem)item; 
					checkBox.setSelected(isSelected);
				}
				gravityTracker.addMenuItem(item, ++largeValue);
			// otherwise just use the preferred menu as the menuitem name
			} else {
				title = pref;
				popup.add( createMenuItem(tf, title, useCheckBoxMenuItem, toolTip) );
			}

		// title and preferred menu
		} else {
			final GravityTracker gravityTracker = tracker.getGravityTracker(pref);
			gravityTracker.addMenuItem(createMenuItem(tf, title,useCheckBoxMenuItem, toolTip), ++largeValue);
		}
	}

	private JMenuItem createMenuItem(TaskFactory tf, String title, boolean useCheckBoxMenuItem, String toolTipText) {
		JMenuItem item;
		PopupAction action = new PopupAction(tf,title);
		if ( useCheckBoxMenuItem )
			item = new JCheckBoxMenuItem(action);
		else
			item = new JMenuItem(action);

		if ( tf instanceof TaskFactoryPredicate )
			item.setEnabled( ((TaskFactoryPredicate)tf).isReady() );

		item.setToolTipText(toolTipText);
		return item;
	}

	/**
	 * Extract and return all T's that match the defined action.  If action is null,
	 * then return everything.
	 */
	private <T> Collection<T> getPreferredActions(Map<T,Map> tfs, String action) {
		// if the action is null, return all available
		if ( action == null ) {
			return tfs.keySet();
		}

		// otherwise figure out if any TaskFactories match the specified preferred action
		java.util.List<T> usableTFs = new ArrayList<T>();
		for ( T evtf : tfs.keySet() ) {
			String prefAction = (String)(tfs.get( evtf ).get("preferredAction"));
			if ( action != null && action.equals(prefAction) )
				usableTFs.add(evtf);
		}
		return usableTFs;
	}

	/**
	 * A simple action that executes the specified TaskFactory
	 */
	private class PopupAction extends AbstractAction {
		TaskFactory tf;
		PopupAction(TaskFactory tf, String title) {
			super( title );
			this.tf = tf;
		}

		public void actionPerformed(ActionEvent ae) {
			executeTask(tf);
		}
	}

	/**
	 * A place to capture the common task execution behavior.
	 */
	private void executeTask(TaskFactory tf) {
		m_view.manager.execute(tf);
	}
}
