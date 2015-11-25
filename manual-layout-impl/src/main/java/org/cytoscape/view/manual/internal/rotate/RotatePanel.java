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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.manual.internal.common.AbstractManualPanel;
import org.cytoscape.view.manual.internal.common.CheckBoxTracker;
import org.cytoscape.view.manual.internal.common.GraphConverter2;
import org.cytoscape.view.manual.internal.common.PolymorphicSlider;
import org.cytoscape.view.manual.internal.common.SliderStateTracker;
import org.cytoscape.view.manual.internal.layout.algorithm.MutablePolyEdgeGraphLayout;
import org.cytoscape.view.model.CyNetworkView;


/**
 * GUI for rotation of manualLayout
 *
 *      Rewrite based on the class RotateAction       9/13/2006        Peng-Liang Wang
 */
@SuppressWarnings("serial")
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
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		// set up the user interface
		JLabel jLabel = new JLabel();
		jLabel.setText("Degrees:");

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

		new CheckBoxTracker(jCheckBox);

		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(jLabel)
				.addComponent(jSlider)
				.addComponent(jCheckBox)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(jLabel)
				.addComponent(jSlider)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(jCheckBox)
		);

		new SliderStateTracker(this);

		setMinimumSize(new Dimension(100,1200));
		setPreferredSize(new Dimension(100,1200));
		setMaximumSize(new Dimension(100,1200));
	} 

	@Override
	public void updateSlider(int x) {
		// this will prevent the state change from producing a change
		prevValue = x;
		jSlider.setValue(x);
	}

	@Override
	public int getSliderValue() {
		return jSlider.getValue();
	}

	@Override
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
