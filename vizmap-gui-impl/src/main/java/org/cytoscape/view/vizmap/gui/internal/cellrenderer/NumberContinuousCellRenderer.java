package org.cytoscape.view.vizmap.gui.internal.cellrenderer;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;

import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.AbstractContinuousMappingEditor;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class NumberContinuousCellRenderer extends DefaultCellRenderer {

	private static final long serialVersionUID = -6734053848878359286L;
	
	private static final Logger logger = LoggerFactory.getLogger(NumberContinuousCellRenderer.class);


	private final AbstractContinuousMappingEditor<?, ?> editor;


	public NumberContinuousCellRenderer(final AbstractContinuousMappingEditor<?, ?> editor) {
		if(editor == null)
			throw new NullPointerException("Editor object is null.");
		
		this.editor = editor;
	}
	
	
	@Override public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if(value == null || value instanceof ContinuousMapping == false) {
			this.setText("Unkonown Mapping");
			return this;
		}
		
		logger.debug("!!!!!!!!! Number cell renderer called: " + value.toString());
		
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		
		final int height = table.getRowHeight(row);
		final int width = table.getColumnModel().getColumn(column).getWidth();
		final ImageIcon icon = editor.drawIcon(width, height-2, false);
		this.setIcon(icon);

		return this;
	}
}
