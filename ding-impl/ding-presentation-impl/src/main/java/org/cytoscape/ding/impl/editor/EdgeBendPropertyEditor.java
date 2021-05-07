package org.cytoscape.ding.impl.editor;

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

public class EdgeBendPropertyEditor extends com.l2fprod.common.beans.editor.AbstractPropertyEditor {
	
	private JButton button;
	private Bend bend;
	private EdgeBendCellRenderer label;
	
	private final ValueEditor<Bend> valueEditor;
		
	/**
	 * Creates a new CyLabelPositionLabelEditor object.
	 */
	public EdgeBendPropertyEditor(final ValueEditor<Bend> valueEditor, final CyServiceRegistrar serviceRegistrar) {
		this.valueEditor = valueEditor;
					
		editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
		((JPanel) editor).setOpaque(false);
		
		((JPanel) editor).add("*", label = new EdgeBendCellRenderer());
		label.setOpaque(false);
		
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
		((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
		button.setText(IconManager.ICON_ELLIPSIS_H);
		button.setFont(iconManager.getIconFont(13.0f));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editBend();
			}
		});
		
		((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
		button.setText(IconManager.ICON_REMOVE);
		button.setFont(iconManager.getIconFont(13.0f));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Bend old = bend;
				bend = null;
				firePropertyChange(old, null);
			}
		});
	}

	@Override
	public Object getValue() {
		return bend;
	}
	
	@Override
	public void setValue(Object value) {
		bend = (Bend) value;
	}

	private void editBend() {
		final Bend newVal = valueEditor.showEditor(null, bend);

		if (newVal != null) {
			setValue(newVal);
			firePropertyChange(null, newVal);
		}
	}
}
