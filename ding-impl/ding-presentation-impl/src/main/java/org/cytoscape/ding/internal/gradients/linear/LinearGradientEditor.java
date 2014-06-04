package org.cytoscape.ding.internal.gradients.linear;

import static org.cytoscape.ding.internal.gradients.linear.LinearGradient.STOP_LIST;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.util.GradientEditor;

public class LinearGradientEditor extends JPanel {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	private GradientEditor grEditor;
	
	private final LinearGradient gradient;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradientEditor(final LinearGradient gradient) {
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
			grEditor.setOpaque(true);
			
			// Set current values
//			final Color start = gradient.get(START, Color.class, Color.DARK_GRAY);
//			grEditor.setStart(start);
//			final Color end = gradient.get(END, Color.class, Color.WHITE);
//			grEditor.setEnd(end);
			
			// Add listener--update gradient when user interacts with the UI
			grEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateGradient();
				}
			});
		}
		
		return grEditor;
	}
	
	private void updateGradient() {
//		gradient.set(START, getGrEditor().getStart());
//		gradient.set(END, getGrEditor().getEnd());
		gradient.set(STOP_LIST, getGrEditor().getControlPoints());
	}
}
