package org.cytoscape.ding.internal.charts.pie;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.util.IconManager;

public class PieChartEditor extends AbstractChartEditor<PieChart> {

	private static final long serialVersionUID = -6185083260942898226L;
	
	private JLabel startAngleLbl;
	private JTextField startAngleTxt;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public PieChartEditor(final PieChart chart, final CyApplicationManager appMgr, final IconManager iconMgr) {
		super(chart, Number.class, false, 1, false, false, true, false, false, false, appMgr, iconMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		startAngleLbl = new JLabel("Start Angle (degrees)");
		
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleTxt()))
				);
		
		return p;
	}
	
	public JTextField getStartAngleTxt() {
		if (startAngleTxt == null) {
			startAngleTxt = new JTextField("" + chart.get(PieChart.START_ANGLE, Double.class, 90.0));
			startAngleTxt.setToolTipText(
					"Starting from 3 o'clock and measuring anti-clockwise (90\u00B0 = 12 o'clock)");
			startAngleTxt.setInputVerifier(new DoubleInputVerifier());
			startAngleTxt.setPreferredSize(new Dimension(60, startAngleTxt.getMinimumSize().height));
			startAngleTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			startAngleTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
			            double angle = Double.valueOf(startAngleTxt.getText().trim()).doubleValue();
			            chart.set(PieChart.START_ANGLE, angle);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return startAngleTxt;
	}
}
