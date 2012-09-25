package org.cytoscape.view.vizmap.gui.core.internal.cellrenderer;

import javax.swing.table.TableCellRenderer;

import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;

public class ContinuousMappingCellRendererFactoryImpl implements ContinuousMappingCellRendererFactory {

	@Override
	public TableCellRenderer createTableCellRenderer(ContinuousMappingEditor<? extends Number, ?> editor) {
		return new ContinuousMappingCellRenderer(editor);
	}
}
