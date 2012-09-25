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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyColorPropertyEditor;

/**
 * Manages editors for Color Visual Properties. This object can be used with any
 * VisualProperty using Color as its type.
 * 
 */
public class ColorVisualPropertyEditor extends BasicVisualPropertyEditor<Paint> {

	/**
	 * Constructor. Should instantiate one editor per VisualProperty.
	 * @param cellRendererFactory 
	 */
	public ColorVisualPropertyEditor(final Class<Paint> type, final CyNetworkTableManager manager,
			final CyApplicationManager appManager, final EditorManager editorManager, final VisualMappingManager vmm, final CyColorPropertyEditor colorPropEditor, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, colorPropEditor, ContinuousEditorType.COLOR, cellRendererFactory);

		this.discreteTableCellRenderer = REG.getRenderer(Color.class);
	}
}