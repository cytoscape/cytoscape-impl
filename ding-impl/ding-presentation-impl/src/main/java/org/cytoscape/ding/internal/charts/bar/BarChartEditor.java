package org.cytoscape.ding.internal.charts.bar;

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

public class BarChartEditor extends AbstractChartEditor<BarChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	private JCheckBox stackedCkb;
	private ButtonGroup orientationGrp;
	private JRadioButton verticalRd;
	private JRadioButton horizontalRd;
	private JCheckBox categoryAxisVisibleCkb;
	private JCheckBox rangeAxisVisibleCkb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarChartEditor(final BarChart chart, final CyApplicationManager appMgr) {
		super(chart, 10, true, appMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final Orientation orientation = chart.get(BarChart.ORIENTATION, Orientation.class, Orientation.VERTICAL);
		final JRadioButton orientRd = orientation == Orientation.HORIZONTAL ? getHorizontalRd() : getVerticalRd();
		getOrientationGrp().setSelected(orientRd.getModel(), true);
		
		final JSeparator sep1 = new JSeparator();
		
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getStackedCkb())
				.addGroup(layout.createSequentialGroup()
						.addComponent(getVerticalRd())
						.addComponent(getHorizontalRd()))
				.addComponent(sep1)
				.addComponent(getCategoryAxisVisibleCkb())
				.addComponent(getRangeAxisVisibleCkb())
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getStackedCkb())
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getVerticalRd())
						.addComponent(getHorizontalRd()))
				.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(getCategoryAxisVisibleCkb())
				.addComponent(getRangeAxisVisibleCkb())
				);
		
		return p;
	}
	
	private JCheckBox getStackedCkb() {
		if (stackedCkb == null) {
			stackedCkb = new JCheckBox("Stacked");
			stackedCkb.setSelected(chart.get(BarChart.STACKED, Boolean.class, false));
			stackedCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(BarChart.STACKED, stackedCkb.isSelected());
					updateRangeMinMax(true);
				}
			});
		}
		
		return stackedCkb;
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
			categoryAxisVisibleCkb.setSelected(chart.get(BarChart.CATEGORY_AXIS_VISIBLE, Boolean.class, false));
			categoryAxisVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(BarChart.CATEGORY_AXIS_VISIBLE, categoryAxisVisibleCkb.isSelected());
				}
			});
		}
		
		return categoryAxisVisibleCkb;
	}
	
	private JCheckBox getRangeAxisVisibleCkb() {
		if (rangeAxisVisibleCkb == null) {
			rangeAxisVisibleCkb = new JCheckBox("Show Range Axis");
			rangeAxisVisibleCkb.setSelected(chart.get(BarChart.RANGE_AXIS_VISIBLE, Boolean.class, false));
			rangeAxisVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(BarChart.RANGE_AXIS_VISIBLE, rangeAxisVisibleCkb.isSelected());
				}
			});
		}
		
		return rangeAxisVisibleCkb;
	}
	
	private void setOrientation() {
		final Orientation orientation = getHorizontalRd().isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
		chart.set(BarChart.ORIENTATION, orientation);
	}
	
	/*
attributelist
	If the values are going to be specified by node attributes, this list provides the attribute or attributes to be used as a comma-separated list.
colorlist
	The list of colors, one for each bar. colorlist also supports the following keywords:
	contrasting: Select colors base on a contrasting color scheme
	modulated: Select colors base on a modulated rainbow color scheme
	rainbow: Select colors base on the rainbow color scheme
	random: Select colors randomly
	up:color: Use color if the value is positive
	down:color: Use color if the value is negative
labellist
	This list provides the labels to be associated with each bar, point, or pie slice on the resulting graph. The values should contain a comma-separated list of labels, one for each value.
position
	The position of the graphic relative to the center of the node. This can be expressed as either an "x,y" pair, or using the main compas points: north, northeast, east, southeast, south, southwest, west, northwest or center.
range
	A comma-separated pair of values that indicates the range of the input values. This may be used to force all charts across a number of nodes to have the same value range. Otherwise each charts will utilize it's own set of values to determine the hight of the bars.
scale
	A scale factor for the size of the chart. A scale of 1.0 is the same size as the node, whereas a scale of 0.5 is 50% of the node size.
separation
	The separation between any two bars
showlabels
	A true/false value to enable or disable the creation of labels.
size
	This specifies the size of the chart as a widthXheight pair. If only one value is given, it is assumed that the chart is square, otherwise the string is assumed to contain a width followed by an "X" followed by a height.
valuelist
	The list of values (as a comma-separated list) for each point, bar, or pie slice.
ybase
	The vertical base of the chart as a proportion of the height. By default, this is 0.5 (the center of the node), to allow for both positive and negative values. If, however, you only have positive values, you might want to set this to 1.0 (the bottom of the node). Note that this goes backwards from what might be expected, with 0.0 being the top of the node and 1.0 being the bottom of the node. The keyword bottom is also supported.
	 
	 */

}
