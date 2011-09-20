package org.cytoscape.ding;

import static org.cytoscape.ding.Justification.JUSTIFY_LEFT;
import static org.cytoscape.ding.Justification.JUSTIFY_RIGHT;
import static org.cytoscape.ding.Position.NONE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.cytoscape.ding.impl.ObjectPositionImpl;

public class ObjectPlacerGraphic extends JPanel implements
		PropertyChangeListener {

	private static final long serialVersionUID = -1091948204116900740L;
	
	protected static final String OBJECT_POSITION_CHANGED = "OBJECT_POSITION_CHANGED";

	private ObjectPosition p;

	// dimensions of panel
	private static final int DEFAULT_WINDOW_SIZE = 500;
	
	// "Snap" distance
	private static final double GRAVITY_DISTANCE = 10;
	
	// Color scheme for GUI
	private static final Color transparentRed = new Color(1.0f, 0.0f, 0.0f, 0.1f);
	private static final Color transparentBlue = new Color(0.0f, 0.0f, 1.0f, 0.1f);
	private static final Color transparentMagenta = new Color(0.0f, 0.0f, 1.0f, 0.05f);
	
	private int xy;
	private int center;
	private float offsetRatio;

	// dimensions for node box
	private int nxy;

	// locations of node points
	private int[] npoints;

	// dimensions for label box
	private int lx;
	private int ly;

	// locations for label points
	private int[] lxpoints;
	private int[] lypoints;

	// diameter of a point
	private int dot;

	// x/y positions for label box, initially offset
	private int xPos;
	private int yPos;

	// indices of the closest points
	private int bestLabelX = 1;
	private int bestLabelY = 1;
	private int bestNodeX = 1;
	private int bestNodeY = 1;

	// mouse drag state
	private boolean beenDragged = false;
	private boolean canOffsetDrag = false;

	// click offset
	private int xClickOffset = 0;
	private int yClickOffset = 0;

	// the x and y offsets for the label rendering
	private int xOffset;
	private int yOffset;

	// default text justify rule
	private Justification justify;

	

	// used to determine the render level of detail
	private boolean renderDetail;

	// strings for the graphic
	private String objectLabel = "LABEL";
	private String targetLabel = "NODE";
	private String click = "CLICK 'N DRAG";

	// font metrics for strings
	private int labelLen = 0;
	private int clickLen = 0;
	private int ascent = 0;
	private int detailStrokeWidth = 3;
	private int lowStrokeWidth = 1;
	private Stroke detailStroke = new BasicStroke(detailStrokeWidth);
	private Stroke lowStroke = new BasicStroke(lowStrokeWidth);
	

	/**
	 * A gui for placing a label relative to a node.
	 * 
	 * @param pos
	 *            initial label position
	 * @param windowSize
	 *            number of pixels square the that graphic should be
	 * @param fullDetail
	 *            whether or not to render at full detail or not
	 */
	public ObjectPlacerGraphic(final Integer windowSize,
			boolean fullDetail, final String objectName) {
		super();
		
		this.p = new ObjectPositionImpl();
		
		this.objectLabel = objectName;


		renderDetail = fullDetail;

		if(windowSize == null)
			initSize(DEFAULT_WINDOW_SIZE);
		else
			initSize(windowSize);

		setPreferredSize(new Dimension(xy, xy));
		setBackground(Color.white);

		addMouseListener(new MouseClickHandler());
		addMouseMotionListener(new MouseDragHandler());

		applyPosition();

		repaint();
	}
	
	public void setObjectPosition(final ObjectPosition op) {
		this.p = op;
	}

	private void initSize(int size) {
		// dimensions of panel
		xy = size;
		center = xy / 2;

		offsetRatio = (float) xy / DEFAULT_WINDOW_SIZE;

		// dimensions for node box
		nxy = (int) (0.3 * xy);

		// locations of node points
		int[] tnpoints = { center - (nxy / 2), center, center + (nxy / 2) };
		npoints = tnpoints;

		// dimensions for object box
		lx = (int) (0.4 * xy);
		ly = (int) (0.1 * xy);

		// locations for label points
		int[] tlxpoints = { 0, lx / 2, lx };
		int[] tlypoints = { 0, ly / 2, ly };
		lxpoints = tlxpoints;
		lypoints = tlypoints;

		// diameter of a point
		dot = (int) (0.02 * xy);

		// x/y positions for label box, initially offset
		xPos = dot;
		yPos = dot;
	}

	/**
	 * The method that handles the rendering of placement gui.
	 */
	public void paint(Graphics gin) {
		final Graphics2D g = (Graphics2D) gin;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// calculate the font
		if (labelLen <= 0) {
			FontMetrics fm = g.getFontMetrics();
			labelLen = fm.stringWidth(objectLabel);
			clickLen = fm.stringWidth(click);
			ascent = fm.getMaxAscent();
		}

		// clear the screen
		g.setColor(Color.white);
		g.fillRect(0, 0, xy, xy);

		// draw the node box
		int x = center - (nxy / 2);
		int y = center - (nxy / 2);

		g.setColor(transparentBlue);
		g.fillOval(x, y, nxy, nxy);

		if (renderDetail)
			g.setStroke(detailStroke);
		else
			g.setStroke(lowStroke);

		g.setColor(Color.blue);
		g.drawLine(x, y, x + nxy, y);
		g.drawLine(x + nxy, y, x + nxy, y + nxy);
		g.drawLine(x + nxy, y + nxy, x, y + nxy);
		g.drawLine(x, y + nxy, x, y);

		if (renderDetail) {
			g.drawString(targetLabel, center - (nxy / 12), center - (nxy / 6));

			// draw the node box points
			g.setColor(Color.black);
			int gd = (int) ((GRAVITY_DISTANCE * 2) + (dot / 2));

			for (int i = 0; i < npoints.length; i++)
				for (int j = 0; j < npoints.length; j++) {
					g.setColor(transparentMagenta);
					g.fillOval(npoints[i] - (gd / 2), npoints[j] - (gd / 2),
							gd, gd);
					if ((i == bestNodeX) && (j == bestNodeY) && !beenDragged)
						g.setColor(Color.yellow);
					else
						g.setColor(Color.black);
					g.fillOval(npoints[i] - (dot / 2), npoints[j] - (dot / 2),
							dot, dot);
				}
		}

		// draw the label box
		g.setColor(transparentRed);
		g.fillRect(xOffset + xPos, yOffset + yPos, lx, ly);

		g.setColor(Color.red);
		g.drawLine(xOffset + xPos, yOffset + yPos, xOffset + xPos + lx, yOffset
				+ yPos);
		g.drawLine(xOffset + xPos + lx, yOffset + yPos, xOffset + xPos + lx,
				yOffset + yPos + ly);
		g.drawLine(xOffset + xPos + lx, yOffset + yPos + ly, xOffset + xPos,
				yOffset + yPos + ly);
		g.drawLine(xOffset + xPos, yOffset + yPos + ly, xOffset + xPos, yOffset
				+ yPos);

		// draw the string in the justified location
		if (renderDetail) {
			int vspace = (ly - ascent - ascent) / 3;

			if (justify == JUSTIFY_LEFT) {
				g.drawString(objectLabel, xOffset + xPos + detailStrokeWidth, yOffset
						+ yPos + vspace + ascent);
				g.drawString(click, xOffset + xPos + detailStrokeWidth, yOffset
						+ yPos + (2 * (vspace + ascent)));
			} else if (justify == JUSTIFY_RIGHT) {
				g.drawString(objectLabel, xOffset + xPos + (lx - labelLen), yOffset
						+ yPos + vspace + ascent);
				;
				g.drawString(click, xOffset + xPos + (lx - clickLen), yOffset
						+ yPos + (2 * (vspace + ascent)));
			} else { // center
				g.drawString(objectLabel, (xOffset + xPos + ((lx - labelLen) / 2))
						- detailStrokeWidth, yOffset + yPos + vspace + ascent);
				g.drawString(click, (xOffset + xPos + ((lx - clickLen) / 2))
						- detailStrokeWidth, yOffset + yPos
						+ (2 * (vspace + ascent)));
			}
		} else {
			g.setColor(Color.gray);

			if (justify == JUSTIFY_LEFT)
				g.drawLine(xOffset + xPos + lowStrokeWidth, yOffset + yPos
						+ (ly / 2), xOffset + xPos + (lx / 3), yOffset + yPos
						+ (ly / 2));
			else if (justify == JUSTIFY_RIGHT)
				g.drawLine(xOffset + xPos + ((2 * lx) / 3), yOffset + yPos
						+ (ly / 2), xOffset + xPos + lx, yOffset + yPos
						+ (ly / 2));
			else
				g.drawLine(xOffset + xPos + (lx / 3),
						yOffset + yPos + (ly / 2),
						(xOffset + xPos + ((2 * lx) / 3)) - lowStrokeWidth,
						yOffset + yPos + (ly / 2));
		}

		if (renderDetail) {
			// draw the label box points
			g.setColor(Color.black);

			for (int i = 0; i < lxpoints.length; i++)
				for (int j = 0; j < lypoints.length; j++) {
					if ((i == bestLabelX) && (j == bestLabelY) && !beenDragged)
						g.setColor(Color.yellow);

					g.fillOval((xPos + xOffset + lxpoints[i]) - (dot / 2),
							(yPos + yOffset + lypoints[j]) - (dot / 2), dot,
							dot);

					if ((i == bestLabelX) && (j == bestLabelY))
						g.setColor(Color.black);
				}
		}
	}

	private class MouseClickHandler extends MouseAdapter {
		/**
		 * Only allows dragging if we're in the label box. Also sets the offset
		 * from where the click is and where the box is, so the box doesn't
		 * appear to jump around too much.
		 */
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();

			// click+drag within box
			if ((x >= (xPos + xOffset)) && (x <= (xPos + xOffset + lx))
					&& (y >= (yPos + yOffset)) && (y <= (yPos + yOffset + ly))) {
				canOffsetDrag = true;
				xClickOffset = x - xPos;
				yClickOffset = y - yPos;
			}
		}

		/**
		 * Finds the closest points once the dragging is finished.
		 */
		public void mouseReleased(MouseEvent e) {
			if (beenDragged) {

				int x = e.getX();
				int y = e.getY();

				// top right
				xPos = x - xClickOffset + xOffset;
				yPos = y - yClickOffset + yOffset;

				double best = Double.POSITIVE_INFINITY;
				double offX = 0;
				double offY = 0;

				// loop over each point in the node box
				for (int i = 0; i < npoints.length; i++) {
					for (int j = 0; j < npoints.length; j++) {
						Point nodePoint = new Point(npoints[i] - (dot / 2),
								npoints[j] - (dot / 2));

						// loop over each point in the label box
						for (int a = 0; a < lxpoints.length; a++) {
							for (int b = 0; b < lypoints.length; b++) {
								Point labelPoint = new Point(
										(xPos + lxpoints[a]) - (dot / 2),
										(yPos + lypoints[b]) - (dot / 2));

								double dist = labelPoint
										.distance((Point2D) nodePoint);

								if (dist < best) {
									best = dist;
									bestLabelX = a;
									bestLabelY = b;
									bestNodeX = i;
									bestNodeY = j;
									offX = labelPoint.getX() - nodePoint.getX();
									offY = labelPoint.getY() - nodePoint.getY();
								}
							}
						}
					}
				}

				xPos = npoints[bestNodeX] - lxpoints[bestLabelX];
				yPos = npoints[bestNodeY] - lypoints[bestLabelY];

				if (Math.sqrt(offX * offX + offY * offY) > (GRAVITY_DISTANCE + (dot / 2))) {
					xOffset = (int) offX;
					yOffset = (int) offY;
				} else {
					xOffset = 0;
					yOffset = 0;
				}

				p.setOffsetX(xOffset);
				p.setOffsetY(yOffset);
				p.setAnchor(Position.parse(bestLabelX + (3 * bestLabelY)));
				p.setTargetAnchor(Position.parse(bestNodeX + (3 * bestNodeY)));
				firePropertyChange(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED, null, p);

				repaint();
				beenDragged = false;
				canOffsetDrag = false;
			}
		}
	}

	private class MouseDragHandler extends MouseMotionAdapter {
		/**
		 * Handles redrawing for dragging.
		 */
		public void mouseDragged(MouseEvent e) {
			// dragging within normal box
			if (canOffsetDrag) {
				xPos = e.getX() - xClickOffset;
				yPos = e.getY() - yClickOffset;

				beenDragged = true;
				repaint();
			}
		}
	}

	/**
	 * Applies the new ObjectPosition to the graphic.
	 */
	public void applyPosition() {
		xOffset = (int) (p.getOffsetX() * offsetRatio);
		yOffset = (int) (p.getOffsetY() * offsetRatio);
		justify = p.getJustify();

		final Position nodeAnchor = p.getTargetAnchor();

		if (nodeAnchor != NONE) {
			bestNodeX = nodeAnchor.getConversionConstant() % 3;
			bestNodeY = nodeAnchor.getConversionConstant() / 3;
		}

		final Position labelAnchor = p.getAnchor();

		if (labelAnchor != NONE) {
			bestLabelX = labelAnchor.getConversionConstant() % 3;
			bestLabelY = labelAnchor.getConversionConstant() / 3;
		}

		if ((nodeAnchor != NONE) || (labelAnchor != NONE)) {
			xPos = npoints[bestNodeX] - lxpoints[bestLabelX];
			yPos = npoints[bestNodeY] - lypoints[bestLabelY];
		}
	}
	
	void setPosition(ObjectPosition p) {
		this.p = p;
		this.applyPosition();
		repaint();
	}

	/**
	 * Handles all property changes that the panel listens for.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		final String type = e.getPropertyName();

		if (type.equals(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED)) {
			p = (ObjectPosition) e.getNewValue();
			applyPosition();
			repaint();
		}
	}
}
