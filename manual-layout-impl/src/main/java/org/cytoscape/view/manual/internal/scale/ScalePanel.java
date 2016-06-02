package org.cytoscape.view.manual.internal.scale;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
 * GUI for scale of manualLayout
 */
@SuppressWarnings("serial")
public class ScalePanel extends AbstractManualPanel implements ChangeListener, PolymorphicSlider {
	
	private JCheckBox jCheckBox;
	private JSlider jSlider;
	private JCheckBox alongXAxis;
	private JCheckBox alongYAxis;
	private JButton clearButton;
	
	private int prevValue; 

	private boolean startAdjusting = true;
//	private ViewChangeEdit currentEdit = null;

	private final CyApplicationManager appMgr;

	public ScalePanel(CyApplicationManager appMgr) {
		super("Scale");
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

		Hashtable<Integer,JLabel> labels = new Hashtable<Integer,JLabel>();
		labels.put(new Integer(-300), new JLabel("1/8"));
		labels.put(new Integer(-200), new JLabel("1/4"));
		labels.put(new Integer(-100), new JLabel("1/2"));
		labels.put(new Integer(0), new JLabel("1"));
		labels.put(new Integer(100), new JLabel("2"));
		labels.put(new Integer(200), new JLabel("4"));
		labels.put(new Integer(300), new JLabel("8"));

		jSlider.setLabelTable(labels);
		jSlider.setPreferredSize(new Dimension(300, 60));

		jCheckBox = new JCheckBox("Selected Only", /* selected = */true);
		new CheckBoxTracker( jCheckBox );

		alongXAxis = new JCheckBox("Width");
		alongYAxis = new JCheckBox("Height");
		alongXAxis.setSelected(true);
		alongYAxis.setSelected(true);

		clearButton = new JButton("Reset Scale");
		clearButton.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSlider(0);
			}
		});

		new SliderStateTracker(this);

//		final GroupLayout layout = new GroupLayout(this);
//		this.setLayout(layout);
//		layout.setAutoCreateContainerGaps(true);
//		layout.setAutoCreateGaps(true);
		
//		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
//				.addComponent(jSlider)
//				.addGroup(layout.createSequentialGroup()
//						.addComponent(jCheckBox)
//						.addGap(10, 10, Short.MAX_VALUE)
//						.addComponent(clearButton)
//				)
//				.addComponent(alongXAxis)
//				.addComponent(alongYAxis)
//		);
//		layout.setVerticalGroup(layout.createSequentialGroup()
//				.addComponent(jSlider)
//				.addPreferredGap(ComponentPlacement.UNRELATED)
//				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
//						.addComponent(jCheckBox)
//						.addComponent(clearButton)
//				)
//				.addPreferredGap(ComponentPlacement.UNRELATED)
//				.addComponent(alongXAxis)
//				.addComponent(alongYAxis)
//		);
		
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
		top.add(new JLabel("Scale"));
		top.add(Box.createHorizontalGlue()); 
		top.add(jCheckBox);

		JPanel row1 = new JPanel();
		row1.setLayout(new BoxLayout(row1, BoxLayout.LINE_AXIS));

		row1.add(alongXAxis);
		row1.add(alongYAxis);
		row1.add(Box.createHorizontalGlue()); 
		row1.add(clearButton);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(top);
		add(jSlider);
		add(row1);
		setMinimumSize(new Dimension(100,100));
		setPreferredSize(new Dimension(300,120));
		setMaximumSize(new Dimension(300,120));
		
		if (LookAndFeelUtil.isAquaLAF()) {
			setOpaque(false);
			top.setOpaque(false);
			row1.setOpaque(false);
		}
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
		
		if (currentView == null)
			return;

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

		ScaleLayouter.Direction direction = ScaleLayouter.Direction.BOTH_AXES;
		if (alongXAxis.isSelected() && alongYAxis.isSelected())
			direction = ScaleLayouter.Direction.BOTH_AXES;
		else if (alongXAxis.isSelected())
			direction = ScaleLayouter.Direction.X_AXIS_ONLY;
		else if (alongYAxis.isSelected())
			direction = ScaleLayouter.Direction.Y_AXIS_ONLY;
		
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

	@Override
	public void setEnabled(final boolean enabled) {
		jCheckBox.setEnabled(enabled);
		jSlider.setEnabled(enabled);
		alongXAxis.setEnabled(enabled);
		alongYAxis.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		
		super.setEnabled(enabled);
	}
}
