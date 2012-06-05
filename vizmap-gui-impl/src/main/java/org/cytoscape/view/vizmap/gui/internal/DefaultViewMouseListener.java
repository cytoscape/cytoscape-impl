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

package org.cytoscape.view.vizmap.gui.internal;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;

/**
 * Moulse Listener for the default view panel.
 */
public class DefaultViewMouseListener extends MouseAdapter {

	// Singleton managed by DI container.
	private final DefaultViewEditor defViewEditor;
	private final VizMapperMainPanel vizMapperMainPanel;
	private final VisualMappingManager vmm;

	public DefaultViewMouseListener(final DefaultViewEditor defViewEditor, final VizMapperMainPanel vizMapperMainPanel,
			final VisualMappingManager vmm) {
		this.defViewEditor = defViewEditor;
		this.vizMapperMainPanel = vizMapperMainPanel;
		this.vmm = vmm;
	}

	/**
	 * Creates a new DefaultViewMouseListener object. / public
	 * DefaultViewMouseListener(DefaultViewEditor defViewEditor) {
	 * this.defViewEditor = defViewEditor; }
	 * 
	 * /** DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {

			defViewEditor.showEditor(vizMapperMainPanel);

			final Dimension panelSize = vizMapperMainPanel.getDefaultViewPanel().getSize();
			final int newWidth = ((Number) (panelSize.width * 0.8)).intValue();
			final int newHeight = ((Number) (panelSize.height * 0.8)).intValue();

			final VisualStyle style = vmm.getCurrentVisualStyle();
			vizMapperMainPanel.updateDefaultImage(style, ((DefaultViewPanel) defViewEditor.getDefaultView(style))
					.getRenderingEngine(), new Dimension(newWidth, newHeight));
			vizMapperMainPanel.setDefaultViewImagePanel(vizMapperMainPanel.getDefaultImageManager().get(style), style);
		}
	}
}
