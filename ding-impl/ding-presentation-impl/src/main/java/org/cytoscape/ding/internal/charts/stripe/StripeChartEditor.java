package org.cytoscape.ding.internal.charts.stripe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.Orientation;

public class StripeChartEditor extends AbstractChartEditor<StripeChart> {

	private static final long serialVersionUID = -7480674403722656873L;
	
	private ButtonGroup orientationGrp;
	private JRadioButton verticalRd;
	private JRadioButton horizontalRd;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public StripeChartEditor(final StripeChart chart, final CyApplicationManager appMgr) {
		super(chart, 0, false, appMgr);
		labelColumnLbl.setText("Data Column");
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final Orientation orientation = chart.get(StripeChart.ORIENTATION, Orientation.class, Orientation.VERTICAL);
		final JRadioButton orientRd = orientation == Orientation.HORIZONTAL ? getHorizontalRd() : getVerticalRd();
		getOrientationGrp().setSelected(orientRd.getModel(), true);
		
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getVerticalRd())
						.addComponent(getHorizontalRd()))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getVerticalRd())
						.addComponent(getHorizontalRd()))
				);
		
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
	
	private void setOrientation() {
		final Orientation orientation = getHorizontalRd().isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
		chart.set(StripeChart.ORIENTATION, orientation);
	}
	
}
