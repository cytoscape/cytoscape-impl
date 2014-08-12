package org.cytoscape.ding.internal.gradients;

import static org.cytoscape.ding.internal.gradients.linear.LinearGradient.STOP_LIST;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.util.GradientEditor;

public abstract class AbstractGradientEditor<T extends AbstractCustomGraphics2<?>> extends JPanel {

	private static final long serialVersionUID = 8197649738217133935L;
	
	private GradientEditor grEditor;
	private JPanel otherOptionsPnl;

	protected final T gradient;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public AbstractGradientEditor(final T gradient) {
		this.gradient = gradient;
		init();
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	protected void init() {
		createLabels();
		
		setOpaque(false);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getGrEditor())
				.addComponent(getOtherOptionsPnl())
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getGrEditor(), 100, 100, GroupLayout.PREFERRED_SIZE)
				.addComponent(getOtherOptionsPnl())
		);
	}
	
	protected abstract void createLabels();

	protected GradientEditor getGrEditor() {
		if (grEditor == null) {
			final List<ControlPoint> points = gradient.getList(STOP_LIST, ControlPoint.class);
			grEditor = new GradientEditor(points);
			grEditor.setOpaque(true);
			
			// Add listener--update gradient when user interacts with the UI
			grEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateGradient();
				}
			});
			
			if (points.isEmpty())
				gradient.set(STOP_LIST, getGrEditor().getControlPoints());
		}
		
		return grEditor;
	}
	
	/**
	 * Should be overridden by the concrete subclass if it provides extra fields.
	 * @return
	 */
	protected JPanel getOtherOptionsPnl() {
		if (otherOptionsPnl == null) {
			otherOptionsPnl = new JPanel();
			otherOptionsPnl.setOpaque(false);
			otherOptionsPnl.setVisible(false);
		}
		
		return otherOptionsPnl;
	}
	
	protected void updateGradient() {
		gradient.set(STOP_LIST, getGrEditor().getControlPoints());
	}
}
