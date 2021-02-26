package org.cytoscape.cg.internal.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

public class CyCustomGraphicsPropertyEditor extends AbstractPropertyEditor {

	private final CyCustomGraphicsValueEditor valueEditor;
	private CyCustomGraphicsCellRenderer label;
	private JButton button;
	private VisualProperty<CyCustomGraphics> visualProperty;
	private CyCustomGraphics<?> customGraphics;
	private CyCustomGraphics<?> oldCustomGraphics;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CyCustomGraphicsPropertyEditor(CyCustomGraphicsValueEditor valueEditor,
			CyServiceRegistrar serviceRegistrar) {
		this.valueEditor = valueEditor;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public Component getCustomEditor() {
		if (editor == null) {
			editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
			((JPanel) editor).setOpaque(false);
			
			((JPanel) editor).add("*", label = new CyCustomGraphicsCellRenderer());
			label.setOpaque(false);
			
			var iconManager = serviceRegistrar.getService(IconManager.class);
			
			// TODO just use double-click to open editor--remove buttons!!!	
			((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
			button.setText(IconManager.ICON_ELLIPSIS_H);
			button.setFont(iconManager.getIconFont(13.0f));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editChart();
				}
			});
			
			((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
			button.setText(IconManager.ICON_REMOVE);
			button.setFont(iconManager.getIconFont(13.0f));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					CyCustomGraphics<?> old = customGraphics;
					label.setValue(null);
					customGraphics = null;
					firePropertyChange(old, null);
				}
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
		customGraphics = (CyCustomGraphics<?>) value;
		label.setValue(value);
	}
	
	@SuppressWarnings("rawtypes")
	public void setVisualProperty(VisualProperty<CyCustomGraphics> visualProperty) {
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
