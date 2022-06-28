package org.cytoscape.ding;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_POSITION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_POSITION;
import static org.cytoscape.view.presentation.property.values.Position.NONE;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.view.presentation.property.ObjectPositionVisualProperty;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Position;

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

@SuppressWarnings("serial")
public class ObjectPlacerControl extends JPanel implements ActionListener, PropertyChangeListener {
	
	private static final String ANCHOR_POINTS = "Anchor Points";
	private static final String NONE_LABEL = "-- select --";
	
	private JLabel targetAnchorLabel;
	private JLabel objAnchorLabel;
	private JLabel justifyLabel;
	
	/** Usually a Node or an Edge */
	private JComboBox<Position> targetAnchors;
	/** Usually a Label or a Custom Graphics */
	private JComboBox<Position> objAnchors;
	
	private JComboBox<Justification> justifyCombo;
	
	private JTextField xoffsetBox;
	private JTextField yoffsetBox;
	
	private boolean ignoreEvents;

	private ObjectPosition p;

	public ObjectPlacerControl(ObjectPosition p, ObjectPositionVisualProperty vp) {
		this.p = p;
		
		var objPositions = new ArrayList<>(Arrays.asList(Position.values()));
		objPositions.remove(NONE); // The renderer cannot handle NONE!
		objPositions.sort(new Comparator<Position>() {
			@Override
			public int compare(Position p1, Position p2) {
				return p1.getName().compareTo(p2.getName());
			}
		});
		
		var tgtPositions = new ArrayList<>(objPositions);
		
		if (EDGE_LABEL_POSITION.equals(vp)) {
			tgtPositions.sort(new Comparator<Position>() {
				@Override
				public int compare(Position p1, Position p2) {
					var txt1 = getTargetPositionText(vp, p1);
					var txt2 = getTargetPositionText(vp, p2);
					
					return txt1.compareTo(txt2);
				}
			});
		}

		targetAnchorLabel = new JLabel();
		targetAnchors = new JComboBox<>(tgtPositions.toArray(new Position[tgtPositions.size()]));
		targetAnchors.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				var name = value instanceof Position ? getTargetPositionText(vp, (Position) value) : NONE_LABEL;
				setText(name);
				
				return this;
			}
		});
		targetAnchors.addActionListener(this);

		objAnchorLabel = new JLabel();
		objAnchors = new JComboBox<>(objPositions.toArray(new Position[objPositions.size()]));
		objAnchors.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				var name = NONE_LABEL;
				
				if (value instanceof Position) {
					var pos = (Position) value;
					
					if (pos != NONE)
						name = pos.getName();
				}
				
				setText(name);
				
				return this;
			}
		});
		objAnchors.addActionListener(this);

		justifyLabel = new JLabel("Text Justification:");
		justifyCombo = new JComboBox<>(Justification.values());
		justifyCombo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				var name = value instanceof Justification ? ((Justification) value).getName() :  NONE_LABEL;
				setText(name);
				
				return this;
			}
		});
		justifyCombo.addActionListener(this);

		var xoffsetLabel = new JLabel("X Offset Value (can be negative):");
		xoffsetBox = new JTextField("0", 8);
		xoffsetBox.setHorizontalAlignment(JTextField.RIGHT);
		xoffsetBox.addActionListener(this);

		var yoffsetLabel = new JLabel("Y Offset Value (can be negative):");
		yoffsetBox = new JTextField("0", 8);
		yoffsetBox.setHorizontalAlignment(JTextField.RIGHT);
		yoffsetBox.addActionListener(this);
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(TRAILING, true)
						.addComponent(targetAnchorLabel)
						.addComponent(objAnchorLabel)
						.addComponent(justifyLabel)
						.addComponent(xoffsetLabel)
						.addComponent(yoffsetLabel)
				)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addComponent(targetAnchors)
						.addComponent(objAnchors)
						.addComponent(justifyCombo)
						.addComponent(xoffsetBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(yoffsetBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(targetAnchorLabel)
						.addComponent(targetAnchors)
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
		
		// Update labels and other components
		boolean isLabel = NODE_LABEL_POSITION.equals(vp) || EDGE_LABEL_POSITION.equals(vp);
		
		targetAnchorLabel.setText((EDGE_LABEL_POSITION.equals(vp) ? "Edge " : "Node ") + ANCHOR_POINTS + ":");
		objAnchorLabel.setText((isLabel ? "Label " : "Object ") + ANCHOR_POINTS + ":");
		
		justifyLabel.setVisible(isLabel);
		justifyCombo.setVisible(isLabel);
		
		applyPosition();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// ignore events that are generated by setting values to match the graphic
		if (ignoreEvents)
			return;

		var source = e.getSource();
		boolean changed = false;

		if (source == targetAnchors) {
			p.setTargetAnchor((Position) targetAnchors.getSelectedItem());
			changed = true;
		}

		if (source == objAnchors) {
			p.setAnchor((Position) objAnchors.getSelectedItem());
			changed = true;
		}

		if (source == justifyCombo) {
			p.setJustify((Justification) justifyCombo.getSelectedItem());
			changed = true;
		}

		// handle both at the same time since people might forget to press enter
		if ((getOffset(xoffsetBox) != p.getOffsetX()) || (getOffset(yoffsetBox) != p.getOffsetY())) {
			p.setOffsetX(getOffset(xoffsetBox));
			p.setOffsetY(getOffset(yoffsetBox));
			changed = true;
		}

		if (!changed)
			return; // nothing we care about has changed

		firePropertyChange(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED, null, p);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		var type = e.getPropertyName();

		if (type.equals(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED) && e.getNewValue() instanceof ObjectPosition) {
			p = (ObjectPosition) e.getNewValue();
			applyPosition();
		}
	}
	
	public ObjectPosition getPosition() {
		return p;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void applyPosition() {
		ignoreEvents = true; // so that we don't pay attention to events generated from these calls

		var nodeAnchor = p.getTargetAnchor();
		var labelAnchor = p.getAnchor();

		if (nodeAnchor.equals(NONE))
			targetAnchors.setSelectedIndex(-1);
		else
			targetAnchors.setSelectedItem(nodeAnchor);

		if (labelAnchor.equals(NONE))
			objAnchors.setSelectedIndex(-1);
		else
			objAnchors.setSelectedItem(labelAnchor);

		justifyCombo.setSelectedItem(p.getJustify());
		
		xoffsetBox.setText(Integer.valueOf((int) p.getOffsetX()).toString());
		yoffsetBox.setText(Integer.valueOf((int) p.getOffsetY()).toString());
		
		ignoreEvents = false;

		repaint();
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
	
	private static String getTargetPositionText(ObjectPositionVisualProperty vp, Position pos) {
		String txt = NONE_LABEL;
		
		if (pos != null && pos != Position.NONE) {
			if (EDGE_LABEL_POSITION.equals(vp)) {
				var sn = pos.getShortName().toUpperCase();
				
				if (pos == Position.CENTER || pos == Position.NORTH || pos == Position.SOUTH)
					txt = "Middle";
				else if (sn.endsWith("W"))
					txt = "Source";
				else if (sn.endsWith("E"))
					txt = "Target";
				
				if (sn.startsWith("N"))
					txt += " (above)";
				else if (sn.startsWith("S"))
					txt += " (below)";
			} else {
				txt = pos.getName();
			}
		}
		
		return txt;
	}
}
