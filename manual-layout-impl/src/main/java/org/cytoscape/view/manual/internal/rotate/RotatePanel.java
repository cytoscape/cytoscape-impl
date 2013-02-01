package org.cytoscape.view.manual.internal.rotate;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;


import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.view.manual.internal.layout.algorithm.MutablePolyEdgeGraphLayout;
import org.cytoscape.view.manual.internal.common.*;
import org.cytoscape.view.model.CyNetworkView;

import org.cytoscape.application.CyApplicationManager;


/**
 * GUI for rotation of manualLayout
 *
 *      Rewrite based on the class RotateAction       9/13/2006        Peng-Liang Wang
 */
public class RotatePanel extends AbstractManualPanel implements ChangeListener, PolymorphicSlider {
	private JCheckBox jCheckBox;
	private JSlider jSlider;
	private int prevValue; 

	private boolean startAdjusting = true;
	//private ViewChangeEdit currentEdit = null;

	private final CyApplicationManager appMgr;

	public RotatePanel(CyApplicationManager appMgr) {
		super("Rotate");
		this.appMgr = appMgr;
		// set up the user interface
		JLabel jLabel = new JLabel();
		jLabel.setText("Rotate in Degrees:");
		jLabel.setPreferredSize(new Dimension(120, 50));

		jSlider = new JSlider();
		jSlider.setMaximum(360);
		jSlider.setMajorTickSpacing(90);
		jSlider.setPaintLabels(true);
		jSlider.setPaintTicks(true);
		jSlider.setMinorTickSpacing(15);
		jSlider.setValue(0);
		jSlider.setPreferredSize(new Dimension(120, 50));
		jSlider.addChangeListener(this);

		prevValue = jSlider.getValue();

		jCheckBox = new JCheckBox("Rotate Selected Nodes Only", /* selected = */true);

		new CheckBoxTracker( jCheckBox );

		GridBagConstraints gbc = new GridBagConstraints();

		setLayout(new GridBagLayout());

		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 15, 0, 15);
		add(jLabel, gbc);

		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(jSlider, gbc);

		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		add(jCheckBox, gbc);

		new SliderStateTracker(this);

		setMinimumSize(new Dimension(100,1200));
		setPreferredSize(new Dimension(100,1200));
		setMaximumSize(new Dimension(100,1200));
	} 

	public void updateSlider(int x) {
		// this will prevent the state change from producing a change
		prevValue = x;
		jSlider.setValue(x);
	}

	public int getSliderValue() {
		return jSlider.getValue();
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() != jSlider)
			return;
		CyNetworkView currentView = appMgr.getCurrentNetworkView();

		// only create the edit at the beginning of the adjustment
		if ( startAdjusting ) {
			//currentEdit = new ViewChangeEdit(currentView), "Rotate");
			startAdjusting = false;
		}

		MutablePolyEdgeGraphLayout nativeGraph = GraphConverter2.getGraphReference(128.0d, true,
		                                                   jCheckBox.isSelected(), currentView);
		RotationLayouter rotation = new RotationLayouter(nativeGraph);

		double radians = (((double) (jSlider.getValue() - prevValue)) * 2.0d * Math.PI) / 360.0d;
		rotation.rotateGraph(radians);
		currentView.updateView();

		prevValue = jSlider.getValue();

		// only post edit when adjustment is complete
		if ( !jSlider.getValueIsAdjusting() ) {
			//currentEdit.post();
			startAdjusting = true;
		}
	}
} 
