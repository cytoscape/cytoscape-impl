package org.cytoscape.view.manual.internal.rotate;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.layout.LayoutEdit;
import org.cytoscape.view.manual.internal.common.CheckBoxTracker;
import org.cytoscape.view.manual.internal.common.GraphConverter2;
import org.cytoscape.view.manual.internal.common.PolymorphicSlider;
import org.cytoscape.view.manual.internal.common.SliderStateTracker;
import org.cytoscape.view.manual.internal.layout.algorithm.MutablePolyEdgeGraphLayout;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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
 * GUI for rotation of manualLayout
 */
@SuppressWarnings("serial")
public class RotatePanel extends JPanel implements ChangeListener, PolymorphicSlider {
	
	private JLabel label;
	private JCheckBox checkBox;
	private JSlider slider;
	
	private int prevValue; 
	private boolean startAdjusting = true;

	private final CyApplicationManager appMgr;
  private final UndoSupport undoSupport;
  private LayoutEdit layoutEdit = null;

	public RotatePanel(CyApplicationManager appMgr, UndoSupport undoSupport) {
		this.appMgr = appMgr;
		this.undoSupport = undoSupport;
		
		prevValue = getSlider().getValue();
		
		// set up the user interface
		label = new JLabel("Rotate:");

		checkBox = new JCheckBox("Selected Only", true);
		new CheckBoxTracker(checkBox);

		makeSmall(label, checkBox, getSlider());
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(label)
						.addGap(20,  20, Short.MAX_VALUE)
						.addComponent(checkBox)
				)
				.addComponent(getSlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(checkBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		new SliderStateTracker(this);

		if (isAquaLAF())
			setOpaque(false);
	} 

	@Override
	public void updateSlider(int x) {
		// this will prevent the state change from producing a change
		prevValue = x;
		getSlider().setValue(x);
	}

	@Override
	public int getSliderValue() {
		return getSlider().getValue();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() != getSlider())
			return;
		
		CyNetworkView currentView = appMgr.getCurrentNetworkView();
		
		if (currentView == null)
			return;

		// only create the edit at the beginning of the adjustment
		if (startAdjusting) {
			startAdjusting = false;
      layoutEdit = new LayoutEdit("Rotate", currentView);
    }

		MutablePolyEdgeGraphLayout nativeGraph = GraphConverter2.getGraphReference(128.0d, true,
				checkBox.isSelected(), currentView);
		RotationLayouter rotation = new RotationLayouter(nativeGraph);

		double radians = (((double) (getSlider().getValue() - prevValue)) * 2.0d * Math.PI) / 360.0d;
		rotation.rotateGraph(radians);
		currentView.updateView();

		prevValue = getSlider().getValue();

		// only post edit when adjustment is complete
		if (!getSlider().getValueIsAdjusting()) {
      if (undoSupport != null && layoutEdit != null)
        undoSupport.postEdit(layoutEdit);
			startAdjusting = true;
    }
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		label.setEnabled(enabled);
		checkBox.setEnabled(enabled);
		getSlider().setEnabled(enabled);
		
		super.setEnabled(enabled);
	}
	
	public JSlider getSlider() {
		if (slider == null) {
			slider = new JSlider();
			slider.setMinimum(-180);
			slider.setMaximum(180);
			slider.setValue(0);
			slider.setMajorTickSpacing(90);
			slider.setPaintLabels(true);
			slider.setPaintTicks(true);
			slider.setMinorTickSpacing(15);
			slider.addChangeListener(this);
		}
		
		return slider;
	}
} 
