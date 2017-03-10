package org.cytoscape.ding.impl.editor;

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

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CyCustomGraphicsPropertyEditor extends AbstractPropertyEditor {

	private final CyCustomGraphicsValueEditor valueEditor;
	private CyCustomGraphicsCellRenderer label;
	private JButton button;
	private VisualProperty<CyCustomGraphics> visualProperty;
	private CyCustomGraphics<?> customGraphics;
	private CyCustomGraphics<?> oldCustomGraphics;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CyCustomGraphicsPropertyEditor(final CyCustomGraphicsValueEditor valueEditor,
			final CyServiceRegistrar serviceRegistrar) {
		this.valueEditor = valueEditor;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public Component getCustomEditor() {System.out.println(">>>>>>>>>> " + super.getCustomEditor());
		if (editor == null) {
			editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
			((JPanel) editor).setOpaque(false);
			
			((JPanel) editor).add("*", label = new CyCustomGraphicsCellRenderer());
			label.setOpaque(false);
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
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
	public void setValue(final Object value) {
		customGraphics = (CyCustomGraphics<?>) value;
		label.setValue(value);
	}
	
	public void setVisualProperty(final VisualProperty<CyCustomGraphics> visualProperty) {
		this.visualProperty = visualProperty;
	}
	
	private void editChart() {
		//TODO: set correct parent
		final CyCustomGraphics<?> newVal = valueEditor.showEditor(null, customGraphics, visualProperty);

		if (newVal != null) {
			setValue(newVal);
			firePropertyChange(null, newVal);
			oldCustomGraphics = newVal;
		}
	}
}
