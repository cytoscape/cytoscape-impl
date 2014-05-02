package org.cytoscape.ding.internal.charts.heatmap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;

public class HeatMapChartEditor extends AbstractChartEditor<HeatMapChart> {

	private static final long serialVersionUID = -8463795233540323840L;

	private JCheckBox categoryAxisVisibleCkb;
	private JCheckBox rangeAxisVisibleCkb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapChartEditor(final HeatMapChart chart, final CyApplicationManager appMgr) {
		super(chart, 10, true, appMgr); // TODO Make it easier for user to set many columns at once
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getCategoryAxisVisibleCkb())
				.addComponent(getRangeAxisVisibleCkb())
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getCategoryAxisVisibleCkb())
				.addComponent(getRangeAxisVisibleCkb())
				);
		
		return p;
	}
	
	private JCheckBox getCategoryAxisVisibleCkb() {
		if (categoryAxisVisibleCkb == null) {
			categoryAxisVisibleCkb = new JCheckBox("Show Category Axis");
			categoryAxisVisibleCkb.setSelected(chart.get(HeatMapChart.CATEGORY_AXIS_VISIBLE, Boolean.class, false));
			categoryAxisVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(HeatMapChart.CATEGORY_AXIS_VISIBLE, categoryAxisVisibleCkb.isSelected());
				}
			});
		}
		
		return categoryAxisVisibleCkb;
	}
	
	private JCheckBox getRangeAxisVisibleCkb() {
		if (rangeAxisVisibleCkb == null) {
			rangeAxisVisibleCkb = new JCheckBox("Show Range Axis");
			rangeAxisVisibleCkb.setSelected(chart.get(HeatMapChart.RANGE_AXIS_VISIBLE, Boolean.class, false));
			rangeAxisVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(HeatMapChart.RANGE_AXIS_VISIBLE, rangeAxisVisibleCkb.isSelected());
				}
			});
		}
		
		return rangeAxisVisibleCkb;
	}
}
