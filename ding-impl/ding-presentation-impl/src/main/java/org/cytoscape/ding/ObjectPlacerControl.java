package org.cytoscape.ding;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

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
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.ding.Position.NONE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.ding.impl.ObjectPositionImpl;

public class ObjectPlacerControl extends JPanel implements ActionListener,
		PropertyChangeListener {
	
	private static final long serialVersionUID = 3875032897986523443L;
	
	private ObjectPosition lp;
	
	private JComboBox<String> justifyCombo;
	private JTextField xoffsetBox;
	private JTextField yoffsetBox;
	private JComboBox<String> nodeAnchors;
	private JComboBox<String> objAnchors;
	private boolean ignoreEvents;

	public ObjectPlacerControl() {
		super();
		
		lp = new ObjectPositionImpl();
		final Collection<String> points = Position.getDisplayNames();

		final JLabel nodeAnchorLabel = new JLabel("Node Anchor Points:");
		nodeAnchors = new JComboBox<>(points.toArray(new String[points.size()]));
		nodeAnchors.addActionListener(this);

		final JLabel objAnchorLabel = new JLabel("Object Anchor Points:");
		objAnchors = new JComboBox<>(points.toArray(new String[points.size()]));
		objAnchors.addActionListener(this);

		final JLabel justifyLabel = new JLabel("Label Justification:");
		final String[] justifyTypes = Justification.getNames();
		justifyCombo = new JComboBox<>(justifyTypes);
		justifyCombo.addActionListener(this);

		final JLabel xoffsetLabel = new JLabel("X Offset Value (can be negative):");
		xoffsetBox = new JTextField("0", 8);
		xoffsetBox.setHorizontalAlignment(JTextField.RIGHT);
		xoffsetBox.addActionListener(this);

		final JLabel yoffsetLabel = new JLabel("Y Offset Value (can be negative):");
		yoffsetBox = new JTextField("0", 8);
		yoffsetBox.setHorizontalAlignment(JTextField.RIGHT);
		yoffsetBox.addActionListener(this);
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(TRAILING, true)
						.addComponent(nodeAnchorLabel)
						.addComponent(objAnchorLabel)
						.addComponent(justifyLabel)
						.addComponent(xoffsetLabel)
						.addComponent(yoffsetLabel)
				)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addComponent(nodeAnchors)
						.addComponent(objAnchors)
						.addComponent(justifyCombo)
						.addComponent(xoffsetBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(yoffsetBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(nodeAnchorLabel)
						.addComponent(nodeAnchors)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(objAnchorLabel)
						.addComponent(objAnchors)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(justifyLabel)
						.addComponent(justifyCombo)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(xoffsetLabel)
						.addComponent(xoffsetBox)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(yoffsetLabel)
						.addComponent(yoffsetBox)
				)
		);

		applyPosition();
	}
	
	void setPosition(ObjectPosition p) {
		this.lp = p;
		this.applyPosition();
	}

	void applyPosition() {
		ignoreEvents = true; // so that we don't pay attention to events generated from these calls

		Position nodeAnchor = lp.getTargetAnchor();
		Position labelAnchor = lp.getAnchor();

		if (nodeAnchor.equals(NONE))
			nodeAnchors.setSelectedIndex(-1);
		else
			nodeAnchors.setSelectedItem(nodeAnchor.getName());
		
		if (labelAnchor.equals(NONE))
			objAnchors.setSelectedIndex(-1);
		else
			objAnchors.setSelectedItem(labelAnchor.getName());

		justifyCombo.setSelectedItem(lp.getJustify().getName());
		xoffsetBox.setText(Integer.valueOf((int)lp.getOffsetX()).toString());
		yoffsetBox.setText(Integer.valueOf((int) lp.getOffsetY()).toString());
		ignoreEvents = false;
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// ignore events that are generated by setting values to match the graphic
		if (ignoreEvents)
			return;

		Object source = e.getSource();
		boolean changed = false;

		if (source == nodeAnchors) {
			lp.setTargetAnchor(Position.parse(nodeAnchors.getSelectedItem()
					.toString()));
			changed = true;
		}

		if (source == objAnchors) {
			lp.setAnchor(Position.parse(objAnchors.getSelectedItem()
					.toString()));
			changed = true;
		}

		if (source == justifyCombo) {
			lp.setJustify(Justification.parse(justifyCombo.getSelectedItem()
					.toString()));
			changed = true;
		}

		// handle both at the same time since people might forget to press enter
		if ((getOffset(xoffsetBox) != lp.getOffsetX())
				|| (getOffset(yoffsetBox) != lp.getOffsetY())) {
			lp.setOffsetX(getOffset(xoffsetBox));
			lp.setOffsetY(getOffset(yoffsetBox));
			changed = true;
		}

		if (!changed)
			return; // nothing we care about has changed

		firePropertyChange(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED, null, lp);
	}

	private double getOffset(JTextField jtf) {
		try {
			double d = Double.parseDouble(jtf.getText());

			return d;
		} catch (Exception ex) {
			jtf.setText("0");
			return 0.0;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String type = e.getPropertyName();

		if (type.equals(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED)
				&& e.getNewValue() instanceof ObjectPosition) {
			lp = (ObjectPosition) e.getNewValue();
			applyPosition();
		}
	}
	
	public ObjectPosition getPosition() {
		return this.lp;
	}
}
