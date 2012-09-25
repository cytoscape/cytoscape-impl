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

import javax.swing.table.TableCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.NumberContinuousCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyNumberPropertyEditor;

public class NumberVisualPropertyEditor<T extends Number> extends BasicVisualPropertyEditor<T> {

	/**
	 * Creates a new DiscreteNumber object.
	 * @param cellRendererFactory 
	 */
	public NumberVisualPropertyEditor(Class<T> type, final CyNetworkTableManager manager,
			final CyApplicationManager appManager, final EditorManager editorManager, final VisualMappingManager vmm, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, new CyNumberPropertyEditor<T>(type, null), ContinuousEditorType.CONTINUOUS, cellRendererFactory);
		discreteTableCellRenderer = REG.getRenderer(type);
	}

	@Override
	public TableCellRenderer getContinuousTableCellRenderer(
			ContinuousMappingEditor<? extends Number, T> continuousMappingEditor) {
		return new NumberContinuousCellRenderer((ContinuousMappingEditor<?, ?>) continuousMappingEditor);
	}

}
