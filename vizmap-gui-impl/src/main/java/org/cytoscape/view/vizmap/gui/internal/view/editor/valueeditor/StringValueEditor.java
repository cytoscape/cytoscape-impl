package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Component;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;

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

public class StringValueEditor implements VisualPropertyValueEditor<String> {

	private static final String MESSAGE = "Enter a new text value:";

	@Override
	@SuppressWarnings("unchecked")
	public <S extends String> String showEditor(Component parent, S initialValue, VisualProperty<S> vp) {
		if (vp == BasicVisualLexicon.NETWORK_TITLE) // Network Title should not have line breaks
			return showSimpleTextDialog(parent, (String) initialValue, (VisualProperty<String>) vp);
		
		// Labels and Tooltips can have multiple lines
		return showMultiLineDialog(parent, (String) initialValue, (VisualProperty<String>) vp);
	}

	@Override
	public Class<String> getValueType() {
		return String.class;
	}

	private String showSimpleTextDialog(Component parent, String initialValue, VisualProperty<String> vp) {
		return (String) JOptionPane.showInputDialog(parent, MESSAGE, vp.getDisplayName(), JOptionPane.PLAIN_MESSAGE,
				null, null, initialValue);
	}
	
	private String showMultiLineDialog(Component parent, String initialValue, VisualProperty<String> vp) {
		JLabel label = new JLabel(MESSAGE);
		JTextArea ta = new JTextArea(initialValue, 5, 30);
		JScrollPane scrollPane = new JScrollPane(ta);
		JPanel panel = new JPanel();
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
                .addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
	            .addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
	            .addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );
		
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
				parent,
				panel,
				vp.getDisplayName(),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null))
			return ta.getText();
		
		return initialValue;
	}
}
