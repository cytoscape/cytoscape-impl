package org.cytoscape.ding.internal.charts.line;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;

public class LineChartEditor extends AbstractChartEditor<LineChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	private JLabel lineWidthLbl;
	private JTextField lineWidthTxt;
	private JCheckBox categoryAxisVisibleCkb;
	private JCheckBox rangeAxisVisibleCkb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LineChartEditor(final LineChart chart, final CyApplicationManager appMgr) {
		super(chart, 10, true, appMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		lineWidthLbl = new JLabel("Line Width");
		
		final JSeparator sep1 = new JSeparator();
		
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(lineWidthLbl)
						.addComponent(getLineWidthTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addComponent(sep1)
				.addComponent(getCategoryAxisVisibleCkb())
				.addComponent(getRangeAxisVisibleCkb())
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(lineWidthLbl)
						.addComponent(getLineWidthTxt()))
				.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(getCategoryAxisVisibleCkb())
				.addComponent(getRangeAxisVisibleCkb())
				);
		
		return p;
	}
	
	private JTextField getLineWidthTxt() {
		if (lineWidthTxt == null) {
			lineWidthTxt = new JTextField("" + chart.get(LineChart.LINE_WIDTH, Integer.class, 2));
			lineWidthTxt.setInputVerifier(new IntInputVerifier());
			lineWidthTxt.setPreferredSize(new Dimension(40, lineWidthTxt.getMinimumSize().height));
			
			lineWidthTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
			            int width = Double.valueOf(lineWidthTxt.getText().trim()).intValue();
			            chart.set(LineChart.LINE_WIDTH, width);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return lineWidthTxt;
	}
	
	private JCheckBox getCategoryAxisVisibleCkb() {
		if (categoryAxisVisibleCkb == null) {
			categoryAxisVisibleCkb = new JCheckBox("Show Category Axis");
			categoryAxisVisibleCkb.setSelected(chart.get(LineChart.CATEGORY_AXIS_VISIBLE, Boolean.class, false));
			categoryAxisVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(LineChart.CATEGORY_AXIS_VISIBLE, categoryAxisVisibleCkb.isSelected());
				}
			});
		}
		
		return categoryAxisVisibleCkb;
	}
	
	private JCheckBox getRangeAxisVisibleCkb() {
		if (rangeAxisVisibleCkb == null) {
			rangeAxisVisibleCkb = new JCheckBox("Show Range Axis");
			rangeAxisVisibleCkb.setSelected(chart.get(LineChart.RANGE_AXIS_VISIBLE, Boolean.class, false));
			rangeAxisVisibleCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(LineChart.RANGE_AXIS_VISIBLE, rangeAxisVisibleCkb.isSelected());
				}
			});
		}
		
		return rangeAxisVisibleCkb;
	}
}
