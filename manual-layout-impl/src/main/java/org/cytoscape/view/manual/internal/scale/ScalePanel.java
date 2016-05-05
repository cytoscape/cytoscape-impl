package org.cytoscape.view.manual.internal.scale;

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


import java.awt.event.ActionEvent;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
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
 *
 * GUI for scale of manualLayout
 *
 *      Rewrite based on the class ScaleAction       9/13/2006        Peng-Liang Wang
 */
@SuppressWarnings("serial")
public class ScalePanel extends AbstractManualPanel implements ChangeListener, PolymorphicSlider {
	
	private JCheckBox jCheckBox;
	private JSlider jSlider;
	private JRadioButton alongXAxisOnlyRadioButton;
	private JRadioButton alongYAxisOnlyRadioButton;
	private JRadioButton alongBothAxesRadioButton;
	private ButtonGroup radioButtonGroup;
	private int prevValue; 

	private boolean startAdjusting = true;
//	private ViewChangeEdit currentEdit = null;

	private final CyApplicationManager appMgr;

	public ScalePanel(CyApplicationManager appMgr) {
		super("Scale");
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		this.appMgr = appMgr;

		jSlider = new JSlider();
		jSlider.setMajorTickSpacing(100);
		jSlider.setPaintTicks(true);
		jSlider.setPaintLabels(true);
		jSlider.setMaximum(300);
		jSlider.setValue(0);
		jSlider.setMinimum(-300);
		jSlider.addChangeListener(this);

		prevValue = jSlider.getValue();

		Hashtable<Integer,JLabel> labels = new Hashtable<>();
		labels.put(new Integer(-300), new JLabel("1/8"));
		labels.put(new Integer(-200), new JLabel("1/4"));
		labels.put(new Integer(-100), new JLabel("1/2"));
		labels.put(new Integer(0), new JLabel("1"));
		labels.put(new Integer(100), new JLabel("2"));
		labels.put(new Integer(200), new JLabel("4"));
		labels.put(new Integer(300), new JLabel("8"));

		jSlider.setLabelTable(labels);

		jCheckBox = new JCheckBox("Scale Selected Nodes Only", /* selected = */true);
		new CheckBoxTracker( jCheckBox );

		alongXAxisOnlyRadioButton = new JRadioButton("along x-axis only");
		alongYAxisOnlyRadioButton = new JRadioButton("along y-axis only");
		alongBothAxesRadioButton = new JRadioButton("along both axes", /* selected = */true);
		radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(alongXAxisOnlyRadioButton);
		radioButtonGroup.add(alongYAxisOnlyRadioButton);
		radioButtonGroup.add(alongBothAxesRadioButton);

		JButton clearButton = new JButton("Reset Scale Bar");
		clearButton.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSlider(0);
			}
		});

		new SliderStateTracker(this);

		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(jSlider)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jCheckBox)
						.addGap(10, 10, Short.MAX_VALUE)
						.addComponent(clearButton)
				)
				.addComponent(alongXAxisOnlyRadioButton)
				.addComponent(alongYAxisOnlyRadioButton)
				.addComponent(alongBothAxesRadioButton)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(jSlider)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addComponent(jCheckBox)
						.addComponent(clearButton)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(alongXAxisOnlyRadioButton)
				.addComponent(alongYAxisOnlyRadioButton)
				.addComponent(alongBothAxesRadioButton)
		);
	} 

	@Override
	public void updateSlider(int x) {
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

		// TODO support undo events
		// only create the edit when we're beginning to adjust
		if ( startAdjusting ) { 
			//currentEdit = new ViewChangeEdit(currentView), "Scale");
			startAdjusting = false;
		}

		// do the scaling
		MutablePolyEdgeGraphLayout nativeGraph = GraphConverter2.getGraphReference(128.0d, true,
			                                                   jCheckBox.isSelected(),currentView);
		ScaleLayouter scale = new ScaleLayouter(nativeGraph);

		double prevAbsoluteScaleFactor = Math.pow(2, ((double) prevValue) / 100.0d);

		double currentAbsoluteScaleFactor = Math.pow(2, ((double) jSlider.getValue()) / 100.0d);

		double neededIncrementalScaleFactor = currentAbsoluteScaleFactor / prevAbsoluteScaleFactor;

		final ScaleLayouter.Direction direction;
		if (alongXAxisOnlyRadioButton.isSelected())
			direction = ScaleLayouter.Direction.X_AXIS_ONLY;
		else if (alongYAxisOnlyRadioButton.isSelected())
			direction = ScaleLayouter.Direction.Y_AXIS_ONLY;
		else
			direction = ScaleLayouter.Direction.BOTH_AXES;
		
		scale.scaleGraph(neededIncrementalScaleFactor, direction);
		currentView.updateView();
		prevValue = jSlider.getValue();

		// TODO support undo
		// only post the edit when we're finished adjusting 
		if (!jSlider.getValueIsAdjusting()) { 
			//currentEdit.post();
			startAdjusting = true;
		} 
	}
}
