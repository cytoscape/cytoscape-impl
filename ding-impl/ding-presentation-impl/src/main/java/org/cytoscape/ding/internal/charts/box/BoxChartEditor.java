package org.cytoscape.ding.internal.charts.box;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.Orientation;

public class BoxChartEditor extends AbstractChartEditor<BoxChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	private ButtonGroup orientationGrp;
	private JRadioButton verticalRd;
	private JRadioButton horizontalRd;
	private JCheckBox categoryAxisVisibleCkb;
	private JCheckBox rangeAxisVisibleCkb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BoxChartEditor(final BoxChart chart, final CyApplicationManager appMgr) {
		super(chart, 10, true, appMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final Orientation orientation = chart.get(BoxChart.ORIENTATION, Orientation.class, Orientation.VERTICAL);
		final JRadioButton orientRd = orientation == Orientation.HORIZONTAL ? getHorizontalRd() : getVerticalRd();
		getOrientationGrp().setSelected(orientRd.getModel(), true);
		
		final JSeparator sep1 = new JSeparator();
		
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getVerticalRd())
						.addComponent(getHorizontalRd()))
				.addComponent(sep1)
				.addComponent(getCategoryAxisVisibleCkb())
				.addComponent(getRangeAxisVisibleCkb())
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getVerticalRd())
						.addComponent(getHorizontalRd()))
				.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(getCategoryAxisVisibleCkb())
				.addComponent(getRangeAxisVisibleCkb())
				);
		
		
		getLabelsVisibleCkb().setVisible(false); // TODO hide separator too
		
		return p;
	}
	
	private ButtonGroup getOrientationGrp() {
		if (orientationGrp == null) {
			orientationGrp = new ButtonGroup();
			orientationGrp.add(getVerticalRd());
			orientationGrp.add(getHorizontalRd());
		}
		
		return orientationGrp;
	}
	
	private JRadioButton getVerticalRd() {
		if (verticalRd == null) {
			verticalRd = new JRadioButton("Vertical");
			verticalRd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setOrientation();
				}
			});
		}
		
		return verticalRd;
	}
	
	private JRadioButton getHorizontalRd() {
		if (horizontalRd == null) {
			horizontalRd = new JRadioButton("Horizontal");
			horizontalRd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setOrientation();
				}
			});
		}
		
		return horizontalRd;
	}
	
	private JCheckBox getCategoryAxisVisibleCkb() {
		if (categoryAxisVisibleCkb == null) {
			categoryAxisVisibleCkb = new JCheckBox("Show Category Axis");
			categoryAxisVisibleCkb.setSelected(chart.get(BoxChart.CATEGORY_AXIS_VISIBLE, Boolean.class, false));
			categoryAxisVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(BoxChart.CATEGORY_AXIS_VISIBLE, categoryAxisVisibleCkb.isSelected());
				}
			});
		}
		
		return categoryAxisVisibleCkb;
	}
	
	private JCheckBox getRangeAxisVisibleCkb() {
		if (rangeAxisVisibleCkb == null) {
			rangeAxisVisibleCkb = new JCheckBox("Show Range Axis");
			rangeAxisVisibleCkb.setSelected(chart.get(BoxChart.RANGE_AXIS_VISIBLE, Boolean.class, false));
			rangeAxisVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(BoxChart.RANGE_AXIS_VISIBLE, rangeAxisVisibleCkb.isSelected());
				}
			});
		}
		
		return rangeAxisVisibleCkb;
	}
	
	private void setOrientation() {
		final Orientation orientation = getHorizontalRd().isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
		chart.set(BoxChart.ORIENTATION, orientation);
	}
}
