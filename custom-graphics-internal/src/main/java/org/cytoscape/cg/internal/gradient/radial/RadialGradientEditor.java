package org.cytoscape.cg.internal.gradient.radial;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_COLORS;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_FRACTIONS;
import static org.cytoscape.cg.internal.gradient.radial.RadialGradient.CENTER;

import java.awt.Color;
import java.awt.geom.Point2D;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.cg.internal.gradient.AbstractGradientEditor;
import org.cytoscape.cg.internal.util.PointPicker;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class RadialGradientEditor extends AbstractGradientEditor<RadialGradient> {
	
	private JLabel centerLbl;
	private PointPicker pointPicker;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialGradientEditor(RadialGradient gradient, CyServiceRegistrar serviceRegistrar) {
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
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, false)
				.addComponent(getCenterLbl())
				.addComponent(getPointPicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getCenterLbl())
				.addComponent(getPointPicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		LookAndFeelUtil.makeSmall(getCenterLbl());
		
		return p;
	}
	
	private JLabel getCenterLbl() {
		if (centerLbl == null) {
			centerLbl = new JLabel("Center:");
		}
		
		return centerLbl;
	}
	
	private PointPicker getPointPicker() {
		if (pointPicker == null) {
			var center = gradient.get(CENTER, Point2D.class, new Point2D.Double(0.5, 0.5));
			pointPicker = new PointPicker(100, 12, center, serviceRegistrar);
			
			pointPicker.addPropertyChangeListener("value", evt -> {
				var newCenter = (Point2D) evt.getNewValue();
	            gradient.set(CENTER, newCenter);
			});
		}
		
		return pointPicker;
	}
	
	@Override
	protected void update() {
		super.update();
		updatePointPicker();
	}
	
	private void updatePointPicker() {
		var fractions = gradient.getList(GRADIENT_FRACTIONS, Float.class);
		var colors = gradient.getList(GRADIENT_COLORS, Color.class);
		
		var fractionArr = new float[fractions.size()];
		int i = 0;
		for (Float f : fractions)
			fractionArr[i++] = f;
		
		var colorArr = colors.toArray(new Color[colors.size()]);
		
		getPointPicker().update(fractionArr, colorArr);
	}
}
