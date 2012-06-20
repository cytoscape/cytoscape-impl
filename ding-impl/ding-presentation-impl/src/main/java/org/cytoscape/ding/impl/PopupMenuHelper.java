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

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.NodeView;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.util.swing.JMenuTracker;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DynamicSubmenuListener;

import static org.cytoscape.work.ServiceProperties.*;


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

	private StaticTaskFactoryProvisioner factoryProvisioner;

	PopupMenuHelper(DGraphView v, Component inv) {
		m_view = v;
		invoker = inv;
		factoryProvisioner = new StaticTaskFactoryProvisioner();
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
					Object context = null;
					NamedTaskFactory provisioner = factoryProvisioner.createFor(evtf, ev, m_view);
					addMenuItem(ev, menu, provisioner, context, tracker, m_view.edgeViewTFs.get(evtf) );
				}
				
				for (CyEdgeViewContextMenuFactory edgeCMF: m_view.cyEdgeViewContextMenuFactory.keySet())
					menu.add(edgeCMF.createMenuItem(m_view , ev).getMenuItem());
				menu.show(invoker, x, y);

			// execute the task directly if only one factory exists
			} else if ( usableTFs.size() == 1) {
				EdgeViewTaskFactory tf  = usableTFs.iterator().next();
				m_view.manager.execute(tf.createTaskIterator(ev, m_view));
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
					Object context = null;
					NamedTaskFactory provisioner = factoryProvisioner.createFor(nvtf, nv, m_view);
					addMenuItem(nv, menu, provisioner, context, tracker, m_view.nodeViewTFs.get( nvtf ));
				}

				for (CyNodeViewContextMenuFactory nodeVMF: m_view.cyNodeViewContextMenuFactory.keySet())
					menu.add(nodeVMF.createMenuItem(m_view, nv).getMenuItem());
				menu.show(invoker, x, y);

			// execute the task directly if only one factory exists
			} else if ( usableTFs.size() == 1) {
				NodeViewTaskFactory tf  = usableTFs.iterator().next();
				m_view.manager.execute(tf.createTaskIterator(nv, m_view));
			}
		}
	}

	
	/**
	 * Creates a menu based on the NetworkView.
	 */
	void createEmptySpaceMenu(Point rawPt, Point xformPt, String action) {
		
		final JPopupMenu menu = new JPopupMenu("Double Click Menu: empty");
		final JMenuTracker tracker = new JMenuTracker(menu);

		Collection<NetworkViewTaskFactory> usableTFs = getPreferredActions(m_view.emptySpaceTFs,action);
		for ( NetworkViewTaskFactory nvtf : usableTFs ) {
			NamedTaskFactory provisioner = factoryProvisioner.createFor(nvtf, m_view);
			addMenuItem(null, menu, provisioner, null, tracker, m_view.emptySpaceTFs.get( nvtf ) );
		}
		
		Collection<NetworkViewLocationTaskFactory> usableTFs2 = getPreferredActions(m_view.networkViewLocationTfs,action);
		for ( NetworkViewLocationTaskFactory nvltf : usableTFs2 ) {
			NamedTaskFactory provisioner = factoryProvisioner.createFor(nvltf, m_view, rawPt, xformPt);
			addMenuItem(null, menu, provisioner, null, tracker, m_view.networkViewLocationTfs.get( nvltf ) );
		}
		
		menu.show(invoker,(int)(rawPt.getX()), (int)(rawPt.getY()));
	}

	/**
	 * This method creates popup menu submenus and menu items based on the
	 * "title" and "preferredMenu" keywords, depending on which are present
	 * in the service properties.
	 */
	private void addMenuItem(View<?> view, JPopupMenu popup, NamedTaskFactory tf, Object tunableContext,
	                            JMenuTracker tracker, Map props) {

		String title = (String)(props.get(TITLE));
		String pref = (String)(props.get(PREFERRED_MENU));
		String toolTip = (String) (props.get(TOOLTIP));
		String menuGravity = (String) (props.get(MENU_GRAVITY));
		String prefAction = (String)(props.get(PREFERRED_ACTION));
		double gravity;

		// We really don't want to have double-click actions in our menus
		if (prefAction != null && prefAction.equals("OPEN"))
			return;

		if (menuGravity != null) {
			gravity = Double.parseDouble(menuGravity);
		} else  {
			gravity = largeValue++;
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
			title = "Unidentified Task: " + tf.getName();
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
				gravityTracker.addMenuItem(item, gravity);
			// otherwise just use the preferred menu as the menuitem name
			} else {
				title = pref;
				popup.add( createMenuItem(tf, title, useCheckBoxMenuItem, toolTip) );
			}

		// title and preferred menu
		} else {
			final GravityTracker gravityTracker = tracker.getGravityTracker(pref);
			gravityTracker.addMenuItem(createMenuItem(tf, title,useCheckBoxMenuItem, toolTip), gravity);
		}
	}

	private JMenuItem createMenuItem(TaskFactory tf, String title, boolean useCheckBoxMenuItem, String toolTipText) {
		JMenuItem item;
		PopupAction action = new PopupAction(tf, title);
		if ( useCheckBoxMenuItem )
			item = new JCheckBoxMenuItem(action);
		else
			item = new JMenuItem(action);

		item.setEnabled(tf.isReady());

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
			m_view.manager.execute(tf.createTaskIterator());
		}
	}
}
