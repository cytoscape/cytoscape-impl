/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.view.vizmap.gui.internal.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;

import com.l2fprod.common.propertysheet.Property;

/**
 *
 */
public class LockNodeSizeAction extends AbstractVizMapperAction {
	private static final long serialVersionUID = 4408544581539176156L;
	private VizMapperProperty nodeWidth;
	private VizMapperProperty nodeHeight;
	private VizMapperProperty nodeSize;

	public LockNodeSizeAction() {
		super();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param arg0
	 *            DOCUMENT ME!
	 */
	// TODO: fix this!
	public void actionPerformed(ActionEvent arg0) {
		// if (menuItem.isSelected()) {
		// this.vizMapperMainPanel.getSelectedVisualStyle().getNodeAppearanceCalculator().setNodeSizeLocked(true);
		// switchNodeSizeLock(true);
		// } else {
		// vmm.getVisualStyle().getNodeAppearanceCalculator().setNodeSizeLocked(false);
		// switchNodeSizeLock(false);
		// }

		// Cytoscape.redrawGraph(cyNetworkManager.getCurrentNetworkView());
	}

	/**
	 * Will be used to show/hide node size props.
	 * 
	 * @param isLock
	 */
	private void switchNodeSizeLock(boolean isLock) {
		final Property[] props = propertySheetPanel.getProperties();
//FIXME
//		if (isLock && (menuItem != null)) {
//			// Case 1: Locked. Need to remove width/height props.
//			boolean isNodeSizeExist = false;
//
//			// TODO: fix this
//			// for (Property prop : props) {
//			// if
//			// (prop.getDisplayName().equals(VisualProperty.NODE_SIZE.getName()))
//			// isNodeSizeExist = true;
//			//
//			// if
//			// (prop.getDisplayName().equals(VisualProperty.NODE_HEIGHT.getName()))
//			// {
//			// nodeHeight = (VizMapperProperty) prop;
//			// propertySheetPanel.removeProperty(prop);
//			// } else if
//			// (prop.getDisplayName().equals(VisualProperty.NODE_WIDTH.getName()))
//			// {
//			// nodeWidth = (VizMapperProperty) prop;
//			// propertySheetPanel.removeProperty(prop);
//			// }
//			// }
//
//			if (isNodeSizeExist == false)
//				propertySheetPanel.addProperty(nodeSize);
//		} else {
//			// Case 2: Unlocked. Need to add W/H.
//			boolean isNodeWExist = false;
//			boolean isNodeHExist = false;
//
//			// for (Property prop : props) {
//			// if
//			// (prop.getDisplayName().equals(VisualProperty.NODE_SIZE.getName()))
//			// {
//			// nodeSize = (VizMapperProperty) prop;
//			// propertySheetPanel.removeProperty(prop);
//			// }
//			//
//			// if
//			// (prop.getDisplayName().equals(VisualProperty.NODE_WIDTH.getName()))
//			// isNodeWExist = true;
//			//
//			// if
//			// (prop.getDisplayName().equals(VisualProperty.NODE_HEIGHT.getName()))
//			// isNodeHExist = true;
//			// }
//
//			if (isNodeHExist == false) {
//				if (nodeHeight != null)
//					propertySheetPanel.addProperty(nodeHeight);
//			}
//
//			if (isNodeWExist == false) {
//				if (nodeHeight != null)
//					propertySheetPanel.addProperty(nodeWidth);
//			}
//		}
//
//		propertySheetPanel.repaint();
//
//		final VisualStyle targetStyle = this.vizMapperMainPanel
//				.getSelectedVisualStyle();
//
//		vizMapperMainPanel.updateDefaultImage(targetStyle,
//				(CyNetworkView) ((DefaultViewPanel) defViewEditor
//						.getDefaultView(targetStyle)).getView(),
//				vizMapperMainPanel.getDefaultPanel().getSize());
//		vizMapperMainPanel.setDefaultViewImagePanel(vizMapperMainPanel
//				.getDefaultImageManager().get(targetStyle));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public JMenuItem getMenu() {
		if (menuItem == null) {
			menuItem = new JCheckBoxMenuItem(menuLabel);
			menuItem.setIcon(iconManager.getIcon(iconId));
			menuItem.addActionListener(this);
			menuItem.setSelected(true);
		}

		return menuItem;
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("UPDATE_LOCK")) {
			Boolean lockState = (Boolean) e.getNewValue();
			menuItem.setSelected(lockState);
			switchNodeSizeLock(lockState);
		}
	}
}
