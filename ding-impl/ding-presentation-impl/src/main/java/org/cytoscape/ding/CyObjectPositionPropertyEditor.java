package org.cytoscape.ding;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.ObjectPosition;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class CyObjectPositionPropertyEditor extends	AbstractPropertyEditor {
	
	private ObjectPositionCellRenderer label;
	private JButton button;
	private ObjectPosition position;
	private ObjectPosition oldPosition;
	
	private VisualProperty<ObjectPosition> visualProperty;
	
	private final ObjectPositionValueEditor valueEditor;
		
	/**
	 * Creates a new CyLabelPositionLabelEditor object.
	 */
	public CyObjectPositionPropertyEditor(ObjectPositionValueEditor valueEditor, CyServiceRegistrar serviceRegistrar) {
		this.valueEditor = valueEditor;
		
		var iconManager = serviceRegistrar.getService(IconManager.class);
					
		editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
		((JPanel) editor).setOpaque(false);
		
		((JPanel) editor).add("*", label = new ObjectPositionCellRenderer());
		label.setOpaque(false);
		
		((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
		button.setText(IconManager.ICON_ELLIPSIS_H);
		button.setFont(iconManager.getIconFont(13.0f));
		button.addActionListener(e -> editObjectPosition());
		
		((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
		button.setText(IconManager.ICON_REMOVE);
		button.setFont(iconManager.getIconFont(13.0f));
		button.addActionListener(e -> {
			var old = position;
			label.setValue(null);
			position = null;
			firePropertyChange(old, null);
		});
	}

	@Override
	public Object getValue() {
		return position;
	}

	@Override
	public void setValue(Object value) {
		position = (ObjectPosition) value;
		label.setValue(value);
	}
	
	public void setVisualProperty(VisualProperty<ObjectPosition> visualProperty) {
		this.visualProperty = visualProperty;
	}

	private void editObjectPosition() {
		//TODO: set correct parent
		var newVal = valueEditor.showEditor(null, position, visualProperty);

		if (newVal != null) {
			setValue(newVal);
			firePropertyChange(null, newVal);
			oldPosition = newVal;
		}
	}
}
