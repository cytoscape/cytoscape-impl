package org.cytoscape.ding.internal.charts.ring;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.pie.PieChart;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class RingChartEditor extends AbstractChartEditor<RingChart> {

	private static final long serialVersionUID = -1867268965571724061L;
	
	private JLabel startAngleLbl;
	private JComboBox<Double> startAngleCmb;
	private JLabel holeLbl;
	private JTextField holeTxt;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RingChartEditor(final RingChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Number.class, false, false, false, true, false, false, false, false, appMgr, iconMgr, colIdFactory);
		
		domainLabelPositionLbl.setVisible(false);
		getDomainLabelPositionCmb().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		startAngleLbl = new JLabel("Start Angle (degrees):");
		holeLbl = new JLabel("Hole Size (0.0-1.0):");
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
					.addComponent(startAngleLbl)
					.addComponent(holeLbl))
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
					.addComponent(getStartAngleCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(getHoleTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleCmb()))
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(holeLbl)
						.addComponent(getHoleTxt()))
				);
		
		return p;
	}
	
	private JComboBox<Double> getStartAngleCmb() {
		if (startAngleCmb == null) {
			startAngleCmb = createAngleComboBox(chart, PieChart.START_ANGLE, ANGLES);
		}
		
		return startAngleCmb;
	}
	
	private JTextField getHoleTxt() {
		if (holeTxt == null) {
			holeTxt = new JTextField("" + chart.get(RingChart.HOLE_SIZE, Double.class, 0.4));
			holeTxt.setToolTipText("Diameter of the ring hole, as a proportion of the entire plot");
			holeTxt.setInputVerifier(new DoubleInputVerifier());
			holeTxt.setPreferredSize(new Dimension(60, holeTxt.getMinimumSize().height));
			holeTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			holeTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
			            double angle = Double.valueOf(holeTxt.getText().trim()).doubleValue();
			            chart.set(RingChart.HOLE_SIZE, angle);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return holeTxt;
	}
}
