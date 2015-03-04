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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.ding.impl.ObjectPositionImpl;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

/**
 * Swing implementation of Object Position editor.
 */
public class ObjectPositionValueEditor extends JDialog implements ValueEditor<ObjectPosition> {

	private static final long serialVersionUID = 7146654020668346430L;

	private String label;
	private ObjectPosition oldValue;
	private boolean canceled;
	
	private ObjectPlacerGraphic graphic;
	private ObjectPlacerControl control;
	
	public ObjectPositionValueEditor() {
		super();
		this.label = "Object";
		
		this.setModal(true);
		this.setResizable(false);
		init();
	}

	private void init() {
		setTitle("Select Position");

		// Set up and connect the gui components.
		graphic = new ObjectPlacerGraphic(null, true, label);
		
		control = new ObjectPlacerControl();
		control.addPropertyChangeListener(graphic);
		graphic.addPropertyChangeListener(control);

		final JPanel graphicPanel = new JPanel();
		graphicPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.disabledForeground")));
		graphicPanel.setLayout(new BorderLayout());
		graphicPanel.add(graphic);
		
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		okButton.addActionListener(control);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		});

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);

		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
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
		
		pack();
	}

	@Override
	public <S extends ObjectPosition> ObjectPosition showEditor(Component parent, S initialValue) {
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
			this.setLocationRelativeTo(parent);
		else
			this.setLocationByPlatform(true);

		this.setVisible(true);

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
}
