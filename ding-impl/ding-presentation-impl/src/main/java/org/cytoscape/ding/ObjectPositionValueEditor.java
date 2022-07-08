package org.cytoscape.ding;

import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.ObjectPositionVisualProperty;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;

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

/**
 * Swing implementation of Object Position editor.
 */
public class ObjectPositionValueEditor implements VisualPropertyValueEditor<ObjectPosition> {

	private static final String TITLE = "Position";
	
	private ObjectPosition oldValue;
	private boolean canceled;
	
	private JDialog dialog;
	private ObjectPlacerGraphic graphic;
	private ObjectPlacerControl control;
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	@Override
	public <S extends ObjectPosition> ObjectPosition showEditor(Component parent, S initialValue, VisualProperty<S> vp) {
		final ObjectPosition pos;

		if (initialValue == null) {
			oldValue = null;
			pos = new ObjectPosition();
		} else {
			oldValue = initialValue;
			pos = new ObjectPosition(initialValue);
		}
		
		dialog = createDialog(parent, pos, (ObjectPositionVisualProperty) vp);
		
		if (parent != null)
			dialog.setLocationRelativeTo(parent);
		else
			dialog.setLocationByPlatform(true);

		dialog.pack();
		dialog.setVisible(true);

		if (canceled) {
			canceled = false;
			return oldValue;
		} else {
			return control.getPosition();
		}
	}
	
	@Override
	public Class<ObjectPosition> getValueType() {
		return ObjectPosition.class;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@SuppressWarnings("serial")
	private JDialog createDialog(Component parent, ObjectPosition pos, ObjectPositionVisualProperty vp) {
		var owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
		var dialog = new JDialog(owner, ModalityType.APPLICATION_MODAL);
		dialog.setTitle(vp != null ? vp.getDisplayName() : TITLE);
		dialog.setResizable(false);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});

		// Set up and connect the gui components.
		graphic = new ObjectPlacerGraphic(pos, vp, true);
		control = new ObjectPlacerControl(pos, vp);
		
		graphic.addPropertyChangeListener(control);
		control.addPropertyChangeListener(graphic);

		var graphicPanel = new JPanel();
		graphicPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		graphicPanel.setLayout(new BorderLayout());
		graphicPanel.add(graphic);
		
		var okButton = new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		okButton.addActionListener(control);

		var cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});

		var buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), okButton.getAction(), cancelButton.getAction());
		dialog.getRootPane().setDefaultButton(okButton);

		var layout = new GroupLayout(dialog.getContentPane());
		dialog.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(graphicPanel)
				.addComponent(control)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(graphicPanel)
				.addComponent(control)
				.addComponent(buttonPanel)
		);
		
		return dialog;
	}
	
	private void cancel() {
		canceled = true;
		
		if (dialog != null) {
			dialog.dispose();
			dialog = null;
		}
	}
}
