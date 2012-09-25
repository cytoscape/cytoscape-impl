package org.cytoscape.view.vizmap.gui.internal.editor;

import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.IconCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyDiscreteValuePropertyEditor;

/**
 * Editor object for all kinds of discrete values such as Node Shape, Line
 * Stroke, etc.
 * 
 * 
 * @param <T>
 */
public class DiscreteValueVisualPropertyEditor<T> extends BasicVisualPropertyEditor<T> {

	public DiscreteValueVisualPropertyEditor(final Class<T> type, final IconCellRenderer<T> cellRenderer,
			final CyDiscreteValuePropertyEditor<T> propEditor, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, propEditor, ContinuousEditorType.DISCRETE, cellRendererFactory);

		if(cellRenderer != null)
			discreteTableCellRenderer = cellRenderer;
		else
			discreteTableCellRenderer = REG.getRenderer(type);
	}
}
