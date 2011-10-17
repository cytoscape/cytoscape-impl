
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.action.VizMapUIAction;
import org.cytoscape.view.vizmap.gui.internal.EditorWindowManager;
import org.cytoscape.view.vizmap.gui.internal.VizMapPropertySheetBuilder;
import org.cytoscape.view.vizmap.gui.internal.VizMapperMainPanel;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;

import com.l2fprod.common.propertysheet.PropertySheetPanel;


/**
 * Action class to process commands.
 */
public abstract class AbstractVizMapperAction extends AbstractAction implements VizMapUIAction,
                                                                                PropertyChangeListener {
	private static final long serialVersionUID = 1499424630636172107L;
	
	
	protected DefaultViewEditor defViewEditor;
	protected VisualMappingManager vmm;
	
	protected CyApplicationManager applicationManager;
	
	protected VizMapperMainPanel vizMapperMainPanel;
	
	protected IconManager iconManager;
	
	
	protected VizMapPropertySheetBuilder vizMapPropertySheetBuilder;
	
	
	protected PropertySheetPanel propertySheetPanel;
	
	protected EditorWindowManager editorWindowManager;
	
	protected Properties vizmapUIResource;
	
	protected String menuLabel;
	protected String iconId;
	protected JMenuItem menuItem;
	
	protected SelectedVisualStyleManager manager;
	
	public AbstractVizMapperAction() {
		
	}
	

	/**
	 *  DOCUMENT ME!
	 *
	 * @param defAppBldr DOCUMENT ME!
	 */
	public void setDefaultAppearenceBuilder(DefaultViewEditor defViewEditor) {
		this.defViewEditor = defViewEditor;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param vmm DOCUMENT ME!
	 */
	public void setVmm(VisualMappingManager vmm) {
		this.vmm = vmm;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param vizMapperMainPanel DOCUMENT ME!
	 */
	public void setVizMapperMainPanel(VizMapperMainPanel vizMapperMainPanel) {
		this.vizMapperMainPanel = vizMapperMainPanel;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param menuLabel DOCUMENT ME!
	 */
	public void setMenuLabel(final String menuLabel) {
		this.menuLabel = menuLabel;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param iconId DOCUMENT ME!
	 */
	public void setIconId(final String iconId) {
		this.iconId = iconId;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param iconManager DOCUMENT ME!
	 */
	public void setIconManager(IconManager iconManager) {
		this.iconManager = iconManager;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public JMenuItem getMenu() {
		if (menuItem == null) {
			//TODO: where should I set listener?
			//vizMapperMainPanel.getPropertyChangeSupport().addPropertyChangeListener(this);
			menuItem = new JMenuItem(menuLabel);
			//menuItem.setIcon(iconManager.getIcon(iconId));
			menuItem.addActionListener(this);
		}

		return menuItem;
	}
	
	public void execute() {
		// Should be implemented by the action classes.
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void propertyChange(PropertyChangeEvent e) {
		System.out.println(e.getSource());
	}

}
