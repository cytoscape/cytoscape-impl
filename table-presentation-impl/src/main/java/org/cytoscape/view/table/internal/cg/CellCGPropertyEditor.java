package org.cytoscape.view.table.internal.cg;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

public class CellCGPropertyEditor extends AbstractPropertyEditor {

	private final CellCGValueEditor valueEditor;
	private CellCGCellRenderer label;
	private JButton button;
	private VisualProperty<CellCustomGraphics> visualProperty;
	private CellCustomGraphics customGraphics;
	private CellCustomGraphics oldCustomGraphics;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CellCGPropertyEditor(CellCGValueEditor valueEditor, CyServiceRegistrar serviceRegistrar) {
		this.valueEditor = valueEditor;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public Component getCustomEditor() {
		if (editor == null) {
			editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
			((JPanel) editor).setOpaque(false);
			
			((JPanel) editor).add("*", label = new CellCGCellRenderer());
			label.setOpaque(false);
			
			var iconManager = serviceRegistrar.getService(IconManager.class);
			
			// TODO just use double-click to open editor--remove buttons!!!	
			((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
			button.setText(IconManager.ICON_ELLIPSIS_H);
			button.setFont(iconManager.getIconFont(13.0f));
			button.addActionListener(evt -> editChart());
			
			((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
			button.setText(IconManager.ICON_REMOVE);
			button.setFont(iconManager.getIconFont(13.0f));
			button.addActionListener(evt -> {
				var old = customGraphics;
				label.setValue(null);
				customGraphics = null;
				firePropertyChange(old, null);
			});
		}
		
		return super.getCustomEditor();
	}
	
	@Override
	public Object getValue() {
		return customGraphics;
	}
	
	@Override
	public void setValue(Object value) {
		customGraphics = (CellCustomGraphics) value;
		label.setValue(value);
	}
	
	public void setVisualProperty(VisualProperty<CellCustomGraphics> visualProperty) {
		this.visualProperty = visualProperty;
	}
	
	private void editChart() {
		//TODO: set correct parent
		var newVal = valueEditor.showEditor(null, customGraphics, visualProperty);

		if (newVal != null) {
			setValue(newVal);
			firePropertyChange(null, newVal);
			oldCustomGraphics = newVal;
		}
	}
}
