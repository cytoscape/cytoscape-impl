package org.cytoscape.ding.internal.gradients.linear;

import static org.cytoscape.ding.internal.gradients.linear.LinearGradient.ANGLE;
import static org.cytoscape.ding.internal.gradients.linear.LinearGradient.STOP_LIST;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;

import org.cytoscape.ding.internal.charts.AbstractChartEditor.DoubleInputVerifier;
import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.util.GradientEditor;

public class LinearGradientEditor extends JPanel {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	private GradientEditor grEditor;
	private JLabel angleLbl;
	private JTextField angleTxt;
	
	private final LinearGradient gradient;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradientEditor(final LinearGradient gradient) {
		this.gradient = gradient;
		init();
		updateGradient();
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		angleLbl = new JLabel("Angle");
		
		setOpaque(false);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getGrEditor())
				.addGroup(layout.createSequentialGroup()
						.addComponent(angleLbl)
						.addComponent(getAngleTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getGrEditor(), 100, 100, GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(angleLbl)
						.addComponent(getAngleTxt()))
		);
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
	
	public JTextField getAngleTxt() {
		if (angleTxt == null) {
			angleTxt = new JTextField("" + gradient.get(ANGLE, Double.class, 0.0));
			angleTxt.setInputVerifier(new DoubleInputVerifier());
			angleTxt.setPreferredSize(new Dimension(90, angleTxt.getMinimumSize().height));
			angleTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			angleTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
			            double angle = Double.valueOf(angleTxt.getText().trim()).doubleValue();
			            gradient.set(ANGLE, angle);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return angleTxt;
	}
	
	private void updateGradient() {
		gradient.set(STOP_LIST, getGrEditor().getControlPoints());
	}
}
