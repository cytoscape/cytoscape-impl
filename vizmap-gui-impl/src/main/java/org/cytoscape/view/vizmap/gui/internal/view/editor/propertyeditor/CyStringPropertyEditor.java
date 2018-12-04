package org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor;

import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.ViewUtil;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.LookAndFeelTweaks;
import com.l2fprod.common.swing.PercentLayout;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class CyStringPropertyEditor extends AbstractPropertyEditor {
	
	private JTextField textFld;
	private JButton editBtn;
	
	private String currentValue;
	private String newValue;
	
	private boolean ignoreDocumentEvents;

	@SuppressWarnings("serial")
	public CyStringPropertyEditor(ServicesUtil servicesUtil) {
		final IconManager iconManager = servicesUtil.get(IconManager.class);
		
		editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0)) {
			@Override
			public void addNotify() {
				super.addNotify();
				
				// Multiple lines already?
				if (isMultiLine(currentValue)) {
					// Don't let the user type in the text field, in order to preserve the line breaks
					textFld.setEnabled(false);
					editBtn.requestFocusInWindow();
				} else {
					// The text can be edited in the text field
					textFld.setEnabled(true);
					textFld.selectAll();
				}
			}
			@Override
			public void removeNotify() {
				checkChange();
				super.removeNotify();
			}
		};
		((JPanel) editor).setOpaque(false);
		editor.setFocusable(false);
		
		((JPanel) editor).add("*", textFld = new JTextField());
		textFld.setBorder(LookAndFeelTweaks.EMPTY_BORDER);
		makeSmall(textFld);
		textFld.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent evt) {
				onTextFieldUpdated(evt);
			}
			@Override
			public void insertUpdate(DocumentEvent evt) {
				onTextFieldUpdated(evt);
			}
			@Override
			public void changedUpdate(DocumentEvent evt) {
				onTextFieldUpdated(evt);
			}
		});
		textFld.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() >= 2 && isMultiLine(currentValue))
					showPopupEditor();
			}
		});
		
		((JPanel) editor).add(editBtn = ComponentFactory.Helper.getFactory().createMiniButton());
		editBtn.setText(IconManager.ICON_ELLIPSIS_H);
		editBtn.setToolTipText("Edit Text...");
		editBtn.setFont(iconManager.getIconFont(14.0f));
		editBtn.addActionListener(evt -> showPopupEditor());
	}
	
	@Override
	public Object getValue() {
		return currentValue;
	}
	
	@Override
	public void setValue(Object value) {
		newValue = currentValue = (String) value;
		
		ignoreDocumentEvents = true;
		textFld.setText(currentValue);
		ignoreDocumentEvents = false;
	}
	
	private void onTextFieldUpdated(DocumentEvent evt) {
		if (!ignoreDocumentEvents)
			newValue = textFld.getText();
	}
	
	private void checkChange() {
		if (!Objects.equals(currentValue, newValue)) {
			String oldValue = currentValue;
			currentValue = newValue;
			firePropertyChange(oldValue, newValue);
		}
	}

	private void showPopupEditor() {
		newValue = ViewUtil.showMultiLineTextEditor(textFld, newValue);
		checkChange();
		
		ignoreDocumentEvents = true;
		textFld.setText(newValue);
		ignoreDocumentEvents = false;
	}
	
	private boolean isMultiLine(String value) {
		return value != null && value.contains("\n");
	}
}
