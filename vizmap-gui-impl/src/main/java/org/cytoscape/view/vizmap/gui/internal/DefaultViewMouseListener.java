package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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
