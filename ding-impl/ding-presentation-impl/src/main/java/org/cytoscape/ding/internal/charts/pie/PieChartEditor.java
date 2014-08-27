package org.cytoscape.ding.internal.charts.pie;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class PieChartEditor extends AbstractChartEditor<PieChart> {

	private static final long serialVersionUID = -6185083260942898226L;
	
	private JLabel startAngleLbl;
	private JComboBox<Double> startAngleCmb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public PieChartEditor(final PieChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Number.class, false, false, false, true, false, false, false, appMgr, iconMgr, colIdFactory);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		startAngleLbl = new JLabel("Start Angle (degrees)");
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleCmb()))
				);
		
		return p;
	}
	
	private JComboBox<Double> getStartAngleCmb() {
		if (startAngleCmb == null) {
			startAngleCmb = createAngleComboBox(chart, PieChart.START_ANGLE, ANGLES);
		}
		
		return startAngleCmb;
	}
}
