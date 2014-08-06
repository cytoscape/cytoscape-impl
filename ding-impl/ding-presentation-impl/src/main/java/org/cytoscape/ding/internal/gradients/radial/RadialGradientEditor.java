package org.cytoscape.ding.internal.gradients.radial;

import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.CENTER;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.cytoscape.ding.internal.gradients.AbstractGradientEditor;

public class RadialGradientEditor extends AbstractGradientEditor<RadialGradient> {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	private static enum Direction {
		TOP("Top", new Point2D.Double(0.5, 0)),
		TOP_LEFT("Top Left", new Point2D.Double(0, 0)),
		TOP_RIGHT("Top Right", new Point2D.Double(1, 0)),
		RIGHT("Right", new Point2D.Double(1, 0.5)),
		CENTER("Center", new Point2D.Double(0.5, 0.5)),
		LEFT("Left", new Point2D.Double(0, 0.5)),
		BOTTOM_LEFT("Bottom Left", new Point2D.Double(0, 1)),
		BOTTOM_RIGHT("Bottom Right", new Point2D.Double(1, 1)),
		BOTTOM("Bottom", new Point2D.Double(0.5, 1));
		
		private final String label;
		private final Point2D center;

		private Direction(final String label, final Point2D center) {
			this.label = label;
			this.center = center;
		}
		
		public String getLabel() {
			return label;
		}
		
		public Point2D getCenter() {
			return center;
		}

		public static Direction getValue(final Point2D center) {
			double x = snap(center.getX());
			double y = snap(center.getY());
			final Point2D newCenter = new Point2D.Double(x, y);
			
			Direction value = CENTER; // Default
			
			for (final Direction dir : Direction.values()) {
				if (dir.getCenter().equals(newCenter)) {
					value = dir;
					break;
				}
			}
			
			return value;
		}
		
		private static double snap(double d) {
			d = d >= 0.75 ? 1.0 : d;
			d = d <  0.25 ? 0.0 : d;
			d = (d == 0.0 || d == 1.0) ? d : 0.5;
			
			return d;
		}
	}
	
	private JLabel directionLbl;
	private JComboBox directionCmb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialGradientEditor(final RadialGradient gradient) {
		super(gradient);
		
		// TODO
//		final float radius = gradient.get(RADIUS, Float.class, 1.0f);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		directionLbl = new JLabel("Direction");
	}
	
	@Override
	protected JPanel getOtherOptionsPnl() {
		final JPanel p = super.getOtherOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(directionLbl)
				.addComponent(getDirectionCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(directionLbl)
				.addComponent(getDirectionCmb())
		);
		
		return p;
	}
	
	public JComboBox getDirectionCmb() {
		if (directionCmb == null) {
			directionCmb = new JComboBox(Direction.values());
			directionCmb.setRenderer(new DirectionComboBoxRenderer());
			final Point2D center = gradient.get(CENTER, Point2D.class, Direction.CENTER.getCenter());
			final Direction direction = Direction.getValue(center);
			directionCmb.setSelectedItem(direction);
			
			directionCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Direction value = (Direction) directionCmb.getSelectedItem();
		            gradient.set(CENTER, value != null ? value.getCenter() : Direction.CENTER.getCenter());
				}
			});
		}
		
		return directionCmb;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private static class DirectionComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 3041717525540256178L;

		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index,
				final boolean isSelected, final boolean cellHasFocus) {
			final DefaultListCellRenderer c = (DefaultListCellRenderer) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
			
			if (value == null)
				c.setText("-- none --");
			else if (value instanceof Direction)
				c.setText(((Direction)value).getLabel());
			else
				c.setText("[ invalid value ]"); // Should never happen
				
			return c;
		}
	}
}
