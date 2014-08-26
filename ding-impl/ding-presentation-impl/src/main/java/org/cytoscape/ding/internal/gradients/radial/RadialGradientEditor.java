package org.cytoscape.ding.internal.gradients.radial;

import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.CENTER;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.ding.internal.gradients.AbstractGradientEditor;
import org.cytoscape.ding.internal.util.PointPicker;

public class RadialGradientEditor extends AbstractGradientEditor<RadialGradient> {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	private JLabel centerLbl;
	private PointPicker pointPicker;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialGradientEditor(final RadialGradient gradient) {
		super(gradient);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		centerLbl = new JLabel("Center");
	}
	
	@Override
	protected JPanel getOtherOptionsPnl() {
		final JPanel p = super.getOtherOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(centerLbl)
				.addComponent(getPointPicker(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(centerLbl)
				.addComponent(getPointPicker())
		);
		
		return p;
	}
	
	private PointPicker getPointPicker() {
		if (pointPicker == null) {
			final Point2D center = gradient.get(CENTER, Point2D.class, new Point2D.Double(0.5, 0.5));
			pointPicker = new PointPicker(100, 12, center);
			
			pointPicker.addPropertyChangeListener("value", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					final Point2D newCenter = (Point2D) e.getNewValue();
		            gradient.set(CENTER, newCenter);
				}
			});
		}
		
		return pointPicker;
	}
}
