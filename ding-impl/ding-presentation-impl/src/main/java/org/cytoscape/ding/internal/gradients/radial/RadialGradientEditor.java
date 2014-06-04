package org.cytoscape.ding.internal.gradients.radial;

import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.CENTER;
import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.RADIUS;
import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.STOP_LIST;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.util.GradientEditor;

public class RadialGradientEditor extends JPanel {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	private GradientEditor grEditor;
	
	private final RadialGradient gradient;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialGradientEditor(final RadialGradient gradient) {
		this.gradient = gradient;
		init();
		updateGradient();
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(getGrEditor());
		add(Box.createVerticalGlue());
	}
	
	private GradientEditor getGrEditor() {
		if (grEditor == null) {
			final List<ControlPoint> points = gradient.getList(STOP_LIST, ControlPoint.class);
			grEditor = new GradientEditor(points);
			
			// TODO
			final float radius = gradient.get(RADIUS, Float.class, 1.0f);
			final Point2D center = gradient.get(CENTER, Point2D.class, new Point2D.Float(radius/2, radius/2));
			
			// Add listener--update gradient when user interacts with the UI
			grEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateGradient();
				}
			});
			
			grEditor.setOpaque(true);
		}
		
		return grEditor;
	}
	
	private void updateGradient() {
//		gradient.set(START, getGrEditor().getStart());
//		gradient.set(END, getGrEditor().getEnd());
		gradient.set(STOP_LIST, getGrEditor().getControlPoints());
	}
}
