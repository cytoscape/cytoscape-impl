package org.cytoscape.cg.internal.gradient.linear;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.internal.gradient.linear.LinearGradient.ANGLE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.cg.internal.charts.AbstractChartEditor.DoubleInputVerifier;
import org.cytoscape.cg.internal.gradient.AbstractGradientEditor;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class LinearGradientEditor extends AbstractGradientEditor<LinearGradient> {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	private JLabel angleLbl;
	private JComboBox<Double> angleCmb;
	
	private static Double[] ANGLES = new Double[] { -315.0, -270.0, -225.0, -180.0, -135.0, -90.0, -45.0,
		                                            0.0,
		                                            45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0  };
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradientEditor(LinearGradient gradient, CyServiceRegistrar serviceRegistrar) {
		super(gradient, serviceRegistrar);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		angleLbl = new JLabel("Angle (degrees):");
	}
	
	@Override
	protected JPanel getOtherOptionsPnl() {
		final JPanel p = super.getOtherOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(angleLbl)
				.addComponent(getAngleCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(angleLbl)
				.addComponent(getAngleCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		LookAndFeelUtil.makeSmall(angleLbl, getAngleCmb());
		
		return p;
	}
	
	private JComboBox<Double> getAngleCmb() {
		if (angleCmb == null) {
			angleCmb = new JComboBox<>(ANGLES);
			angleCmb.setEditable(true);
			((JLabel)angleCmb.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			angleCmb.setSelectedItem(gradient.get(ANGLE, Double.class, 0.0));
			angleCmb.setInputVerifier(new DoubleInputVerifier());
			
			angleCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Object angle = angleCmb.getSelectedItem();
		            gradient.set(ANGLE, angle instanceof Number ? ((Number)angle).doubleValue() : 0.0);
				}
			});
		}
		
		return angleCmb;
	}
}
