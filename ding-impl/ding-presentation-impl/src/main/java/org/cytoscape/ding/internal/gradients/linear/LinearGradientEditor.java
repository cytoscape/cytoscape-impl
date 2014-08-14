package org.cytoscape.ding.internal.gradients.linear;

import static org.cytoscape.ding.internal.gradients.linear.LinearGradient.ANGLE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.ding.internal.charts.AbstractChartEditor.DoubleInputVerifier;
import org.cytoscape.ding.internal.gradients.AbstractGradientEditor;

public class LinearGradientEditor extends AbstractGradientEditor<LinearGradient> {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	private JLabel angleLbl;
	private JComboBox<Double> angleCmb;
	
	private static Double[] ANGLES = new Double[] { -315.0, -270.0, -225.0, -180.0, -135.0, -90.0, -45.0,
		                                            0.0,
		                                            45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0  };
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradientEditor(final LinearGradient gradient) {
		super(gradient);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		angleLbl = new JLabel("Angle (degrees)");
	}
	
	@Override
	protected JPanel getOtherOptionsPnl() {
		final JPanel p = super.getOtherOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(angleLbl)
				.addComponent(getAngleCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(angleLbl)
				.addComponent(getAngleCmb())
		);
		
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
