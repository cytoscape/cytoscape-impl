
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

package org.cytoscape.filter.internal.filters;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.filter.internal.filters.view.FilterMainPanel;


/**
 *
 */
public class FilterMenuItemAction extends AbstractCyAction {
	private final CytoPanel cytoPanelWest;
	private final FilterMainPanel filterPanel;

	/**
	 * Creates a new FilterMenuItem object.
	 *
	 * @param icon  DOCUMENT ME!
	 * @param csfilter  DOCUMENT ME!
	 */
	public FilterMenuItemAction(CyApplicationManager applicationManager, CySwingApplication application, FilterMainPanel filterPanel) {
		super("Use Filters", applicationManager);
		setPreferredMenu("Select");

		ImageIcon icon = new ImageIcon(getClass().getResource("/images/filter.png"));
		ImageIcon smallIcon = new ImageIcon(getClass().getResource("/images/filter-small.png"));
		
		putValue(LARGE_ICON_KEY, icon);
		putValue(SMALL_ICON, smallIcon);
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		cytoPanelWest = application.getCytoPanel(CytoPanelName.WEST);
		this.filterPanel = filterPanel;
	}


	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {

		// If the state of the cytoPanelEast is HIDE, show it
		if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
			cytoPanelWest.setState(CytoPanelState.DOCK);
		}	

		// Select the filter panel
		int index = cytoPanelWest.indexOfComponent(filterPanel);
		if (index == -1) {
			return;
		}
		
		cytoPanelWest.setSelectedIndex(index);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean isInToolBar() {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean isInMenuBar() {
		return true;
	}
}
