package org.cytoscape.cg.internal.gradient.radial;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.internal.gradient.radial.RadialGradient.CENTER;

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
	protected void createLabels() {
		super.createLabels();
		centerLbl = new JLabel("Center:");
	}
	
	@Override
	protected JPanel getOtherOptionsPnl() {
		var p = super.getOtherOptionsPnl();
		p.setVisible(true);
		
		var layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, false)
				.addComponent(centerLbl)
				.addComponent(getPointPicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(centerLbl)
				.addComponent(getPointPicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		LookAndFeelUtil.makeSmall(centerLbl);
		
		return p;
	}
	
	private PointPicker getPointPicker() {
		if (pointPicker == null) {
			var center = gradient.get(CENTER, Point2D.class, new Point2D.Double(0.5, 0.5));
			pointPicker = new PointPicker(100, 12, center);
			
			pointPicker.addPropertyChangeListener("value", evt -> {
				var newCenter = (Point2D) evt.getNewValue();
	            gradient.set(CENTER, newCenter);
			});
		}
		
		return pointPicker;
	}
}
