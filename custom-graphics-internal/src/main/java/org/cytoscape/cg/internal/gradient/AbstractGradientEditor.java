package org.cytoscape.cg.internal.gradient;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_COLORS;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_FRACTIONS;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.cytoscape.cg.internal.util.GradientEditor;
import org.cytoscape.cg.model.AbstractCustomGraphics2;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public abstract class AbstractGradientEditor<T extends AbstractCustomGraphics2<?>> extends JPanel {

	private JLabel colorsLbl;
	private GradientEditor grEditor;
	private JPanel otherOptionsPnl;

	protected final T gradient;
	protected final CyServiceRegistrar serviceRegistrar;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public AbstractGradientEditor(T gradient, CyServiceRegistrar serviceRegistrar) {
		this.gradient = gradient;
		this.serviceRegistrar = serviceRegistrar;
		init();
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	protected void init() {
		createLabels();
		
		setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		final JSeparator sep = new JSeparator();
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(colorsLbl)
				.addComponent(getGrEditor())
				.addComponent(sep)
				.addComponent(getOtherOptionsPnl())
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(colorsLbl)
				.addComponent(getGrEditor(), 100, 100, PREFERRED_SIZE)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getOtherOptionsPnl())
		);
	}
	
	protected void createLabels() {
		colorsLbl = new JLabel("Colors:");
	}

	protected GradientEditor getGrEditor() {
		if (grEditor == null) {
			final List<Float> fractions = gradient.getList(GRADIENT_FRACTIONS, Float.class);
			final List<Color> colors = gradient.getList(GRADIENT_COLORS, Color.class);
			grEditor = new GradientEditor(fractions, colors, serviceRegistrar);
			
			// Add listener--update gradient when user interacts with the UI
			grEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateGradient();
				}
			});
			
			if (fractions == null || fractions.size() < 2) {
				gradient.set(GRADIENT_FRACTIONS, getGrEditor().getPositions());
				gradient.set(GRADIENT_COLORS, getGrEditor().getColors());
			}
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
			otherOptionsPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			otherOptionsPnl.setVisible(false);
		}
		
		return otherOptionsPnl;
	}
	
	protected void updateGradient() {
		gradient.set(GRADIENT_FRACTIONS, getGrEditor().getPositions());
		gradient.set(GRADIENT_COLORS, getGrEditor().getColors());
	}
}
