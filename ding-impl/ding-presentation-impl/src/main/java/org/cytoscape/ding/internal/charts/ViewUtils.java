package org.cytoscape.ding.internal.charts;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * The NodeChartViewer creates the actual custom graphics
 */
public class ViewUtils {

	public enum Position {
		CENTER ("center"),
		EAST ("east"),
		NORTH ("north"),
		NORTHEAST ("northeast"),
		NORTHWEST ("northwest"),
		SOUTH ("south"),
		SOUTHEAST ("southeast"),
		SOUTHWEST ("southwest"),
		WEST ("west");
	
		private String label;
		private static Map<String, Position>pMap;
	
		Position(String label) { 
			this.label = label; 
			addPosition(this);
		}
	
		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}
	
		private void addPosition(Position pos) {
			if (pMap == null) pMap = new HashMap<String,Position>();
			pMap.put(pos.getLabel(), pos);
		}
	
		static Position getPosition(String label) {
			if (pMap.containsKey(label))
				return pMap.get(label);
			return null;
		}
	}
	
	public static class DoubleRange {
		
		public double min;
		public double max;
		
		public DoubleRange(double min, double max) {
			this.min = min;
			this.max = max;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(max);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(min);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DoubleRange other = (DoubleRange) obj;
			if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
				return false;
			if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return min + "," + max;
		}
	}

	/**
 	 * getPosition will return either a Point2D or a Position, depending on whether
 	 * the user provided us with a position keyword or a specific value.
 	 *
 	 * @param position the position argument
 	 * @return a Point2D representing the X,Y offset specified by the user or a Position
 	 * enum that corresponds to the provided keyword.  <b>null</b> is returned if the input
 	 * is illegal.
 	 */
	public static Object getPosition(String position) {
		Position pos = Position.getPosition(position);
		if (pos != null) 
			return pos;

		String [] xy = position.split(",");
		if (xy.length != 2) {
			return null;
		}

		try {
			Double x = Double.valueOf(xy[0]);
			Double y = Double.valueOf(xy[1]);
			return new Point2D.Double(x.doubleValue(), y.doubleValue());
		} catch (NumberFormatException e) {
			return null;
		}
	}


	public static final String DEFAULT_FONT=Font.SANS_SERIF;
	public static final int DEFAULT_STYLE=Font.PLAIN;
	public static final int DEFAULT_SIZE=8;

	public static enum TextAlignment {ALIGN_LEFT, ALIGN_CENTER_TOP, ALIGN_RIGHT, ALIGN_CENTER_BOTTOM, ALIGN_MIDDLE};

	public static Shape getLabelShape(String label, Font font) {
		// Get the canvas so that we can find the graphics context
		FontRenderContext frc = new FontRenderContext(null, false, false);
		TextLayout tl = new TextLayout(label, font, frc);
		return tl.getOutline(null);
	}

	public static Shape getLabelShape(String label, String fontName, 
	                                  int fontStyle, int fontSize) {
		if (fontName == null) fontName = DEFAULT_FONT;
		if (fontStyle == 0) fontStyle = DEFAULT_STYLE;
		if (fontSize == 0) fontSize = DEFAULT_SIZE;

		Font font = new Font(fontName, fontStyle, fontSize);
		return getLabelShape(label, font);
	}

	public static Shape positionLabel(Shape lShape, Point2D position, TextAlignment tAlign, 
	                                  double maxHeight, double maxWidth, double rotation) {

		// System.out.println("  Label = "+label);

		// Figure out how to move the text to center it on the bbox
		double textWidth = lShape.getBounds2D().getWidth(); 
		double textHeight = lShape.getBounds2D().getHeight();

		// Before we go any further, scale the text, if necessary
		if (maxHeight > 0.0 || maxWidth > 0.0) {
			double scaleWidth = 1.0;
			double scaleHeight = 1.0;
			if (maxWidth > 0.0 && textWidth > maxWidth)
				scaleWidth = maxWidth/textWidth * 0.9;
			if (maxHeight > 0.0 && textHeight > maxHeight)
				scaleHeight = maxHeight/textHeight * 0.9;

			double scale = Math.min(scaleWidth, scaleHeight);

			// We don't want to scale down too far.  If scale < 20% of the font size, skip the label
			if (scale < 0.20)
				return null;
			// System.out.println("scale = "+scale);
			AffineTransform sTransform = new AffineTransform();
			sTransform.scale(scale, scale);
			lShape = sTransform.createTransformedShape(lShape);
		}

		double pointX = position.getX();
		double pointY = position.getY();

		double textStartX = pointX;
		double textStartY = pointY;

		switch (tAlign) {
		case ALIGN_CENTER_TOP:
			// System.out.println("  Align = CENTER_TOP");
			textStartX = pointX - textWidth/2;
			textStartY = pointY - textHeight/2;
			break;
		case ALIGN_CENTER_BOTTOM:
			// System.out.println("  Align = CENTER_BOTTOM");
			textStartX = pointX - textWidth/2;
			textStartY = pointY + textHeight;
			break;
		case ALIGN_RIGHT:
			// System.out.println("  Align = RIGHT");
			textStartX = pointX - textWidth;
			textStartY = pointY + textHeight/2;
			break;
		case ALIGN_LEFT:
			// System.out.println("  Align = LEFT");
			textStartX = pointX;
			textStartY = pointY + textHeight/2;
			break;
		case ALIGN_MIDDLE:
			textStartX = pointX - textWidth/2;;
			textStartY = pointY + textHeight/2;
			break;
		default:
			// System.out.println("  Align = "+tAlign);
		}

		// System.out.println("  Text bounds = "+lShape.getBounds2D());
		// System.out.println("  Position = "+position);

		// System.out.println("  Offset = ("+textStartX+","+textStartY+")");

		// Use the bounding box to create an Affine transform.  We may need to scale the font
		// shape if things are too cramped, but not beneath some arbitrary minimum
		AffineTransform trans = new AffineTransform();
		if (rotation != 0.0)
			trans.rotate(Math.toRadians(rotation), pointX, pointY);
		trans.translate(textStartX, textStartY);

		// System.out.println("  Transform: "+trans);
		return trans.createTransformedShape(lShape);
	}

	/**
 	 * This is used to draw a line from a text box to an object -- for example from a pie label to
 	 * the pie slice itself.
 	 */
	public static Shape getLabelLine(Rectangle2D textBounds, Point2D labelPosition, TextAlignment tAlign) {
		double lineStartX = 0;
		double lineStartY = 0;
		switch (tAlign) {
			case ALIGN_CENTER_TOP:
				lineStartY = textBounds.getMaxY()+1;
				lineStartX = textBounds.getCenterX();
			break;
			case ALIGN_CENTER_BOTTOM:
				lineStartY = textBounds.getMinY()-1;
				lineStartX = textBounds.getCenterX();
			break;
			case ALIGN_RIGHT:
				lineStartY = textBounds.getCenterY();
				lineStartX = textBounds.getMaxX()+1;
			break;
			case ALIGN_LEFT:
				lineStartY = textBounds.getCenterY();
				lineStartX = textBounds.getMinX()-1;
			break;
		}

		BasicStroke stroke = new BasicStroke(0.5f);
		return stroke.createStrokedShape(new Line2D.Double(lineStartX, lineStartY, labelPosition.getX(), labelPosition.getY()));
	}
	
	public static ImageIcon resizeIcon(final ImageIcon icon, int width, int height) {
		final Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		final Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		
		return new ImageIcon(bi);
	}

	private static Rectangle2D positionAdjust(Rectangle2D bbox, double nodeHeight, double nodeWidth, Object pos) {
		if (pos == null)
			return bbox;

		double height = bbox.getHeight();
		double width = bbox.getWidth();
		double x = bbox.getX();
		double y = bbox.getY();

		if (pos instanceof Position) {
			Position p = (Position) pos;

			switch (p) {
			case EAST:
				x = nodeWidth/2;
				break;
			case WEST:
				x = -nodeWidth*1.5;
				break;
			case NORTH:
				y = -nodeHeight*1.5;
				break;
			case SOUTH:
				y = nodeHeight/2;
				break;
			case NORTHEAST:
				x = nodeWidth/2;
				y = -nodeHeight*1.5;
				break;
			case NORTHWEST:
				x = -nodeWidth*1.5;
				y = -nodeHeight*1.5;
				break;
			case SOUTHEAST:
				x = nodeWidth/2;
				y = nodeHeight/2;
				break;
			case SOUTHWEST:
				x = -nodeWidth*1.5;
				y = nodeHeight/2;
				break;
			case CENTER:
			default:
			}
		} else if (pos instanceof Point2D.Double) {
			x += ((Point2D.Double)pos).getX();
			y += ((Point2D.Double)pos).getY();
		}

		return new Rectangle2D.Double(x,y,width,height);
	}

}
