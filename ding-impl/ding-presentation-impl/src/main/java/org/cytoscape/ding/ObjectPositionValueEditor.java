package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
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

import org.cytoscape.ding.impl.ObjectPositionImpl;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

/**
 * Swing implementation of Object Position editor.
 */
public class ObjectPositionValueEditor implements ValueEditor<ObjectPosition> {

	private String label;
	private ObjectPosition oldValue;
	private boolean canceled;
	
	private JDialog dialog;
	private ObjectPlacerGraphic graphic;
	private ObjectPlacerControl control;
	
	private boolean initialized;
	
	public ObjectPositionValueEditor() {
		this.label = "OBJECT";
	}

	@SuppressWarnings("serial")
	private void init(final Component parent) {
		final Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
		dialog = new JDialog(owner, ModalityType.APPLICATION_MODAL);
		dialog.setMinimumSize(new Dimension(400, 600));
		dialog.setTitle("Position");
		dialog.setResizable(false);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});

		// Set up and connect the gui components.
		graphic = new ObjectPlacerGraphic(null, true, label);
		
		control = new ObjectPlacerControl();
		control.addPropertyChangeListener(graphic);
		graphic.addPropertyChangeListener(control);

		final JPanel graphicPanel = new JPanel();
		graphicPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		graphicPanel.setLayout(new BorderLayout());
		graphicPanel.add(graphic);
		
		final JButton okButton = new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		okButton.addActionListener(control);

		final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), okButton.getAction(), cancelButton.getAction());
		dialog.getRootPane().setDefaultButton(okButton);

		final GroupLayout layout = new GroupLayout(dialog.getContentPane());
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
	}

	@Override
	public <S extends ObjectPosition> ObjectPosition showEditor(final Component parent, final S initialValue) {
		if (!initialized) {
			init(parent);
			initialized = true;
		}
		
		ObjectPosition pos;

		if (initialValue == null) {
			oldValue = null;
			pos = new ObjectPositionImpl();
		} else {
			oldValue = initialValue;
			pos = new ObjectPositionImpl(initialValue);
		}

		control.setPosition(pos);
		graphic.setPosition(pos);
		
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
	
	private void cancel() {
		canceled = true;
		dialog.dispose();
	}
}
