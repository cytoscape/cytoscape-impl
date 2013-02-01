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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.ObjectPositionImpl;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

/**
 * Swing implementation of Object Position editor.
 * 
 */
public class ObjectPositionValueEditor extends JDialog implements ValueEditor<ObjectPosition> {

	private static final long serialVersionUID = 7146654020668346430L;

	// Current position
	private String label;
	
	private ObjectPosition oldValue;
	
	private boolean canceled = false;
	
	private ObjectPlacerGraphic graphic;
	private ObjectPlacerControl control;
	
	public ObjectPositionValueEditor() {
		super();
		this.label = "Object";
		
		this.setModal(true);
		init();
	}

	private void init() {
		
		setTitle("Select Position");

		final JPanel placer = new JPanel();
		
		placer.setLayout(new BoxLayout(placer, BoxLayout.Y_AXIS));
		placer.setOpaque(true); // content panes must be opaque

		// Set up and connect the gui components.
		graphic = new ObjectPlacerGraphic(null, true, label);
		control = new ObjectPlacerControl();

		control.addPropertyChangeListener(graphic);
		//control.addPropertyChangeListener(this);

		graphic.addPropertyChangeListener(control);
		//graphic.addPropertyChangeListener(this);

		placer.add(graphic);
		placer.add(control);

		final JPanel buttonPanel = new JPanel();
		final JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				dispose();
			}
		});
		ok.addActionListener(control);

		final JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		});

		buttonPanel.add(ok);
		buttonPanel.add(cancel);
		placer.add(buttonPanel);
		add(placer);

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
