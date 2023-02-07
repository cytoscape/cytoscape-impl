package org.cytoscape.cg.internal.gradient.linear;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_COLORS;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_FRACTIONS;
import static org.cytoscape.cg.internal.gradient.linear.LinearGradient.ANGLE;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.cg.internal.charts.AbstractChartEditor.IntInputVerifier;
import org.cytoscape.cg.internal.gradient.AbstractGradientEditor;
import org.cytoscape.cg.internal.util.AnglePicker;
import org.cytoscape.cg.internal.util.MathUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class LinearGradientEditor extends AbstractGradientEditor<LinearGradient> {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	private JLabel angleLbl;
	private JComboBox<Integer> angleCmb;
	private AnglePicker anglePicker;
	
	private static Integer[] ANGLES = { 0, 45, 90, 135, 180, 225, 270, 315 };
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradientEditor(LinearGradient gradient, CyServiceRegistrar serviceRegistrar) {
		super(gradient, serviceRegistrar);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JPanel getOtherOptionsPnl() {
		var p = super.getOtherOptionsPnl();
		p.setVisible(true);
		
		var layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getAngleLbl())
						.addComponent(getAngleCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getAnglePicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getAngleLbl())
					.addComponent(getAngleCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getAnglePicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		LookAndFeelUtil.makeSmall(angleLbl, getAngleCmb());
		
		return p;
	}
	
	public JLabel getAngleLbl() {
		if (angleLbl == null) {
			angleLbl = new JLabel("Angle (degrees):");
		}
		
		return angleLbl;
	}
	
	private JComboBox<Integer> getAngleCmb() {
		if (angleCmb == null) {
			angleCmb = new JComboBox<>(ANGLES);
			angleCmb.setEditable(true);
			((JLabel)angleCmb.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			angleCmb.setSelectedItem(Math.round((int) gradient.get(ANGLE, Double.class, 0.0).doubleValue()));
			angleCmb.setInputVerifier(new IntInputVerifier());
			
			angleCmb.addActionListener(e -> {
				var angle = angleCmb.getSelectedItem();
				angle = angle instanceof Number ? ((Number) angle).intValue() : 0;
				gradient.set(ANGLE, (int) Math.round(MathUtil.normalizeAngle((int) angle)));
				updateAnglePicker();
			});
		}
		
		return angleCmb;
	}
	
	private AnglePicker getAnglePicker() {
    	if (anglePicker == null) {
    		anglePicker = new AnglePicker();
    		anglePicker.setPreferredSize(new Dimension(120, 120));
    		
    		anglePicker.addPropertyChangeListener("value", evt -> {
				var angle = ((Number) evt.getNewValue()).intValue();
				getAngleCmb().setSelectedItem(angle);
			});
    	}
    	
		return anglePicker;
	}
	
	@Override
	protected void update() {
		super.update();
		updateAnglePicker();
	}
	
	private void updateAnglePicker() {
		var fractions = gradient.getList(GRADIENT_FRACTIONS, Float.class);
		var colors = gradient.getList(GRADIENT_COLORS, Color.class);
		var angle = gradient.get(ANGLE, Double.class, 0.0);
		
		var fractionArr = new float[fractions.size()];
		int i = 0;
		for (Float f : fractions)
			fractionArr[i++] = f;
		
		var colorArr = colors.toArray(new Color[colors.size()]);
		
		getAnglePicker().update(fractionArr, colorArr, (int) Math.round(angle));
	}
}
