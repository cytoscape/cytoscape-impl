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
package org.cytoscape.view.vizmap.gui.internal.editor;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.ContinuousMappingCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.AbstractContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.GradientEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyColorPropertyEditor;

/**
 * Manages editors for Color Visual Properties. This object can be used with any
 * VisualProperty using Color as its type.
 * 
 */
public class ColorVisualPropertyEditor extends
		BasicVisualPropertyEditor<Paint> {

	/**
	 * Constructor. Should instantiate one editor per VisualProperty.
	 */
	public ColorVisualPropertyEditor(final Class<Paint> type,
			final CyTableManager manager,
			final CyApplicationManager appManager,
			final SelectedVisualStyleManager selectedManager, final EditorManager editorManager, final VisualMappingManager vmm) {
		super(type, new CyColorPropertyEditor());

		this.discreteTableCellRenderer = REG.getRenderer(Color.class);
		
		this.continuousEditor = new GradientEditor(manager, appManager, selectedManager, editorManager, vmm);
		this.continuousTableCellRenderer = new ContinuousMappingCellRenderer((AbstractContinuousMappingEditor<?, ?>) continuousEditor);
	}
}
