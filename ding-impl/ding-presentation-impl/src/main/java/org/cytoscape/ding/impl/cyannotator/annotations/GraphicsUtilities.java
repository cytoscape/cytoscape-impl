package org.cytoscape.ding.impl.cyannotator.annotations;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.ArrowEnd;

// import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;

import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl.ArrowType;

// import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
// import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation.ArrowEnd;
// import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation.ArrowType;

class GraphicsUtilities {
	private static double halfPI = Math.PI/2.0;

	protected static final ShapeType supportedShapes[] = {
		ShapeType.RECTANGLE, ShapeType.ROUNDEDRECTANGLE, ShapeType.ELLIPSE, ShapeType.STAR5, 
		ShapeType.TRIANGLE, ShapeType.STAR6, ShapeType.HEXAGON, ShapeType.PENTAGON
	};

	protected static final List<String> supportedShapeNames = Arrays.asList(
		ShapeType.RECTANGLE.shapeName(),
		ShapeType.ROUNDEDRECTANGLE.shapeName(),
		ShapeType.ELLIPSE.shapeName(),
		ShapeType.STAR5.shapeName(),
		ShapeType.TRIANGLE.shapeName(),
		ShapeType.STAR6.shapeName(),
		ShapeType.HEXAGON.shapeName(),
		ShapeType.PENTAGON.shapeName(),
		ShapeType.CUSTOM.shapeName());

	protected static final ArrowType supportedArrows[] = {
		ArrowType.CIRCLE, ArrowType.CLOSED, ArrowType.CONCAVE, ArrowType.DIAMOND, ArrowType.OPEN, 
		ArrowType.NONE, ArrowType.TRIANGLE, ArrowType.TSHAPE
	};

	protected static final List<String> supportedArrowNames = Arrays.asList(
		ArrowType.CIRCLE.arrowName(), 
		ArrowType.CLOSED.arrowName(), 
		ArrowType.CONCAVE.arrowName(), 
		ArrowType.DIAMOND.arrowName(), 
		ArrowType.OPEN.arrowName(), 
		ArrowType.NONE.arrowName(), 
		ArrowType.TRIANGLE.arrowName(), 
		ArrowType.TSHAPE.arrowName());

	static public Shape getShape(String shapeName, double x, double y, double width, double height) {
		ShapeType shapeType = getShapeType(shapeName);
		switch(shapeType) {
			case RECTANGLE: return rectangleShape(x, y, width, height);
			case ROUNDEDRECTANGLE: return roundedRectangleShape(x, y, width, height);
			case ELLIPSE: return ellipseShape(x, y, width, height);
			case STAR5: return starShape(5, x, y, width, height); // 5 pointed star
			case STAR6: return starShape(6, x, y, width, height); // 6 pointed star
			case TRIANGLE: return regularPolygon(3, x, y, width, height); // Pentagon
			case PENTAGON: return regularPolygon(5, x, y, width, height); // Pentagon
			case HEXAGON: return regularPolygon(6, x, y, width, height); // Hexagon
			case CUSTOM: return null;
			default: return rectangleShape(x, y, width, height);
		}
	}

	static public ShapeType getShapeType(String shapeName) {
		for (ShapeType type: supportedShapes) {
			if (shapeName.equals(type.shapeName()))
				return type;
		}
		return ShapeType.RECTANGLE; // If we can't do anything else...
	}

	static public ShapeType getShapeType(int shapeNumber) {
		for (ShapeType type: supportedShapes) {
			if (shapeNumber == type.ordinal())
				return type;
		}
		return ShapeType.RECTANGLE; // If we can't do anything else...
	}

	static public ShapeType getShapeType(Map<String, String> argMap, String key, ShapeType defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		String shapeString = argMap.get(key);
		for (ShapeType type: ShapeType.values()) {
			if (shapeString.equalsIgnoreCase(type.shapeName()) || shapeString.equalsIgnoreCase(type.name())) {
        return type;
      }
		}
		return defValue;
	}

	static public List<String> getSupportedShapes() {
		return supportedShapeNames;
	}

	// Given a position and a size, draw a shape.  We use the ShapeAnnotation to get the
	// shape itself, colors, strokes, etc.
	static public void drawShape(Graphics g, double x, double y, double width, double height, 
	                             ShapeAnnotation annotation, boolean isPrinting) {
		Graphics2D g2 = (Graphics2D)g;

		// System.out.println("drawShape: ("+x+","+y+","+width+"x"+height+")");

		// Get the stroke
		float border = (float)(annotation.getBorderWidth()*annotation.getZoom());
		//if (!isPrinting && border < 1.0f) border = 1.0f;
		// System.out.println("Border width = "+border+", isPrinting = "+isPrinting);

		Shape shape = null;
		if (annotation.getShapeType().equals(ShapeType.CUSTOM.shapeName())) {
      final double destX = x + border;
      final double destY = y + border;
      final double destW = width - border;
      final double destH = height - border;

			shape = annotation.getShape();
      if (shape == null)
        return;
			// Scale the shape appropriately
			Rectangle2D originalBounds = shape.getBounds2D();
			double widthScale = destW/originalBounds.getWidth();
			double heightScale = destH/originalBounds.getHeight();

			AffineTransform transform = new AffineTransform();
      transform.translate(destX, destY);
      transform.scale(widthScale, heightScale);
      transform.translate(-originalBounds.getX(), -originalBounds.getY());
			shape = transform.createTransformedShape(shape);
		} else {
			// Get the shape
			shape = getShape(annotation.getShapeType(), x+border, y+border, width-border, height-border);
		}

		//System.out.println("drawShape: shape = " + serializeShape(shape));

		// Set our fill color
		if (annotation.getFillColor() != null) {
			// System.out.println("drawShape: fill color = "+annotation.getFillColor());
			// If we've filled with a gradient, we need to fix it up a little.  We create
			// our gradients using proportional x,y values rather than absolute x,y values.
			// Fix them now.
			Paint fillColor = annotation.getFillColor();
			fillColor = fixGradients(fillColor, shape);
      g2.setPaint(fillColor);
      float opacity = clamp((float) (annotation.getFillOpacity() / 100.0), 0.0f, 1.0f);
      final Composite originalComposite = g2.getComposite();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2.fill(shape);
      g2.setComposite(originalComposite);
		}

    if (border > 0.0f) { // only paint a border if the border thickness is greater than zero
      float opacity = clamp((float) (((ShapeAnnotationImpl)annotation).getBorderOpacity() / 100.0), 0.0f, 1.0f);
      final Composite originalComposite = g2.getComposite();
  		if (annotation.getBorderColor() != null && !annotation.isSelected()) {
  			// System.out.println("drawShape: border color = "+annotation.getBorderColor());
  			g2.setPaint(annotation.getBorderColor());
      	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
  			g2.setStroke(new BasicStroke(border));
  			g2.draw(shape);
  		} else if (annotation.isSelected()) {
  			// Create a yellow border around the shape
  			BasicStroke stroke = new BasicStroke(border);
  			Shape strokedShape = stroke.createStrokedShape(shape);
  			g2.setPaint(Color.YELLOW);
  			g2.draw(strokedShape);
  		} else {
  			g2.setPaint(Color.BLACK);
      	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
  			g2.setStroke(new BasicStroke(border));
  			g2.draw(shape);
  		}
      g2.setComposite(originalComposite);
    }
	}

	static public String serializeShape(final Shape s) {
        final StringBuilder buffer = new StringBuilder();
        final PathIterator i = s.getPathIterator(null);
        switch (i.getWindingRule()) {
            case PathIterator.WIND_EVEN_ODD:
                buffer.append("EO ");
                break;
            case PathIterator.WIND_NON_ZERO:
                buffer.append("NZ ");
                break;
        }

        final double[] nums = new double[6];
        for (; !i.isDone(); i.next()) {
            final int type = i.currentSegment(nums);
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    buffer.append("Z");
                    break;
                case PathIterator.SEG_MOVETO:
                    buffer.append("M ");
                    buffer.append(nums[0]);
                    buffer.append(' ');
                    buffer.append(nums[1]);
                    buffer.append(' ');
                    break;
                case PathIterator.SEG_LINETO:
                    buffer.append("L ");
                    buffer.append(nums[0]);
                    buffer.append(' ');
                    buffer.append(nums[1]);
                    buffer.append(' ');
                    break;
                case PathIterator.SEG_QUADTO:
                    buffer.append("Q ");
                    buffer.append(nums[0]);
                    buffer.append(' ');
                    buffer.append(nums[1]);
                    buffer.append(' ');
                    buffer.append(nums[2]);
                    buffer.append(' ');
                    buffer.append(nums[3]);
                    buffer.append(' ');
                    break;
                case PathIterator.SEG_CUBICTO:
                    buffer.append("C ");
                    buffer.append(nums[0]);
                    buffer.append(' ');
                    buffer.append(nums[1]);
                    buffer.append(' ');
                    buffer.append(nums[2]);
                    buffer.append(' ');
                    buffer.append(nums[3]);
                    buffer.append(' ');
                    buffer.append(nums[4]);
                    buffer.append(' ');
                    buffer.append(nums[5]);
                    buffer.append(' ');
                    break;
            }
        }

        return buffer.toString();
    }

	static public Shape deserializeShape(final String str) {
        final Path2D.Double path = new Path2D.Double();

        final String[] pieces = str.split("\\p{Space}+");
        final double[] nums = new double[6];
        for (int i = 0; i < pieces.length; /* increment based on command */) {
            final String cmd = pieces[i];
            i++; // move past the command
            if (cmd.equalsIgnoreCase("z")) {
                path.closePath();
            } else if (cmd.equalsIgnoreCase("eo")) {
              path.setWindingRule(Path2D.WIND_EVEN_ODD);
            } else if (cmd.equalsIgnoreCase("nz")) {
              path.setWindingRule(Path2D.WIND_NON_ZERO);
            } else if (cmd.equalsIgnoreCase("m")) {
                i += parseDoubles(pieces, i, 2, nums);
                path.moveTo(nums[0], nums[1]);
            } else if (cmd.equalsIgnoreCase("l")) {
                i += parseDoubles(pieces, i, 2, nums);
                path.lineTo(nums[0], nums[1]);
            } else if (cmd.equalsIgnoreCase("q")) {
                i += parseDoubles(pieces, i, 4, nums);
                path.quadTo(nums[0], nums[1], nums[2], nums[3]);
            } else if (cmd.equalsIgnoreCase("c")) {
                i += parseDoubles(pieces, i, 6, nums);
                path.curveTo(nums[0], nums[1], nums[2], nums[3], nums[4], nums[5]);
            } else {
                throw new IllegalArgumentException(String.format("Unknown command '%s': %s", cmd, str));
            }
        }

        return (Shape)path;
    }

	private static int parseDoubles(final String[] pieces,  final int startIndex, final int expectedNum, final double[] nums) {
        if (startIndex + expectedNum > pieces.length) {
            throw new IllegalArgumentException(String.format("Command expects at least %d arguments", expectedNum));
        }

        for (int i = 0; i < expectedNum; i++) {
            final String num = pieces[i + startIndex];
            try {
                nums[i] = Double.parseDouble(num);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("'%s' is not a valid number", num));
            }
        }

        return expectedNum;
	}

	static public Shape getArrowShape(ArrowType arrowType, double size) {
		switch(arrowType) {
			case CIRCLE: return circleArrow(size);
			case CLOSED: return closedArrow(size);
			case CONCAVE: return concaveArrow(size);
			case DIAMOND: return diamondArrow(size);
			case OPEN: return openArrow(size);
			case TRIANGLE: return triangleArrow(size);
			case TSHAPE: return tshapeArrow(size);
			default: return null;
		}
	}

	static public ArrowType getArrowType(String arrowName) {
		for (ArrowType type: supportedArrows) {
			if (arrowName.equals(type.arrowName()))
				return type;
		}
		return ArrowType.NONE; // If we can't do anything else...
	}

	static public ArrowType getArrowType(int arrowNumber) {
		for (ArrowType type: supportedArrows) {
			if (arrowNumber == type.ordinal())
				return type;
		}
		return ArrowType.NONE; // If we can't do anything else...
	}

	static public ArrowType getArrowType(Map<String, String> argMap, String key, ArrowType defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		int arrowNumber = Integer.parseInt(argMap.get(key));
		for (ArrowType type: supportedArrows) {
			if (arrowNumber == type.ordinal())
				return type;
		}
		return defValue;
	}

	static public ArrowType[] getSupportedArrowTypes() {
		return supportedArrows;
	}

	static public List<String> getSupportedArrowTypeNames() {
		return supportedArrowNames;
	}

	static public void drawArrow(Graphics g, Line2D line, ArrowEnd end, Paint paint, double size, 
	                             ArrowType type) {
		if (line == null)
			return;

		// Get the shape
		Shape arrow = getArrowShape(type, size);

		// Figure out the angle
		double angle = calculateAngle(line, end);

		AffineTransform trans = new AffineTransform();
		if (end == ArrowEnd.SOURCE) {
			trans.translate(line.getX1(), line.getY1());
			Shape t1 = trans.createTransformedShape(arrow);
			trans.rotate(angle);
			trans = new AffineTransform();
			arrow = trans.createTransformedShape(t1);
		} else {
			trans.translate(line.getX2(), line.getY2());
			Shape t1 = trans.createTransformedShape(arrow);
			trans = new AffineTransform();
			trans.rotate(angle, line.getX2(), line.getY2());
			arrow = trans.createTransformedShape(t1);
		}

		Graphics2D g2 = (Graphics2D)g;

		if (paint != null)
			g2.setPaint(paint);

		// Handle opacity
		final Composite originalComposite = g2.getComposite();
		if (paint instanceof Color) {
			int alpha = ((Color)paint).getAlpha();
			float opacity = (float)alpha/(float)255;
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		}

		if (type != ArrowType.OPEN && type != ArrowType.TSHAPE)
			g2.fill(arrow); 

		g2.draw(arrow);	// We're relying on the stroke to be done by the caller
		g2.setComposite(originalComposite);
	}

	// Shapes.
	static private Shape rectangleShape(double x, double y, double width, double height) {
		// System.out.println("Drawing rectangle: "+x+","+y+" "+width+"x"+height);
		return new Rectangle2D.Double(x, y, width, height);
		
	}

	static private Shape roundedRectangleShape(double x, double y, double width, double height) {
		return new RoundRectangle2D.Double(x, y, width, height, width/10, width/10);
	}

	static private Shape ellipseShape(double x, double y, double width, double height) {
		return new Ellipse2D.Double(x, y, width, height);
	}

	static private Shape regularPolygon(int sides, double x, double y, double width, double height) {
		Path2D poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, 12);
		width = width/2;
		height = height/2;
		x = x+width;
		y = y+height;
		Point2D.Double points[] = new Point2D.Double[sides];
		for (int i = 0; i < sides; i++) {
			double x1 = circleX(sides, i) * width + x;
			double y1 = circleY(sides, i) * height + y;
			double x2 = circleX(sides, (i+1)%sides) * width + x;
			double y2 = circleY(sides, (i+1)%sides) * height + y;
			points[i] = new Point2D.Double(x1, y1);
			points[(i+1)%sides] = new Point2D.Double(x2, y2);
		}
		// Now, add the points
		poly.moveTo(points[0].getX(), points[0].getY());
		for (int i = 1; i < sides; i++) {
			poly.lineTo(points[i].getX(), points[i].getY());
		}
		poly.closePath();
		return poly;
	}

	static private Shape starShape(int sides, double x, double y, double width, double height) {
		Path2D poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, 12);
		width = width/2;
		height = height/2;
		x = x+width;
		y = y+height;
		int nPoints = sides*2;
		Point2D.Double points[] = new Point2D.Double[nPoints];
		for (int i = 0; i < sides; i++) {
			double x1 = circleX(sides, i) * width + x;
			double y1 = circleY(sides, i) * height + y;
			double x2 = circleX(sides, (i+2)%sides) * width + x;
			double y2 = circleY(sides, (i+2)%sides) * height + y;
			points[i*2] = new Point2D.Double(x1, y1);
			points[(i*2+4)%nPoints] = new Point2D.Double(x2, y2);
		}
	
		// Fill in the intersection points
		for (int i = 0; i < nPoints; i=i+2) {
			int p1 = i;
			int p2 = (i+4)%nPoints;
			int p3 = (i+2)%nPoints;
			int p4 = (p3+nPoints-4)%nPoints;

			points[(i+1)%nPoints] = findIntersection(points[p1],points[p2],
			                                         points[p3], points[p4]);
		}

		// Now, add the points
		poly.moveTo(points[0].getX(), points[0].getY());
		for (int i = 1; i < nPoints; i++) {
			poly.lineTo(points[i].getX(), points[i].getY());
		}
		poly.closePath();
		return poly;
	}

	// Create a shape with a 30 degree arrow
	static Shape openArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(-size*1.7, -size/2.0);
		path.lineTo(0.0, 0.0);
		path.lineTo(-size*1.7,size/2.0);
		return path;
	}

	// Create a shape with a filled 30 degree arrow
	static Shape closedArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(-size*1.7, -size/2.0);
		path.lineTo(0.0, 0.0);
		path.lineTo(-size*1.7,size/2.0);
		path.closePath();
		return path;
	}

	// Create a shape with a 30 degree arrow
	static Shape concaveArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(-size, 0.0);
		path.lineTo(-size*1.7, -size/2.0);
		path.lineTo(0.0, 0.0);
		path.lineTo(-size*1.7,size/2.0);
		path.closePath();
		return path;
	}

	static Shape diamondArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(-size*1.7*2,0.0);
		path.lineTo(-size*1.7, size);
		path.lineTo(0.0,0.0);
		path.lineTo(-size*1.7, -size);
		path.closePath();
		return path;
	}

	static Shape triangleArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(0.0,0.0);
		path.lineTo(-size/2.0, -size/2.0);
		path.lineTo(-size/2.0, size/2.0);
		path.closePath();
		return path;
	}

	static Shape tshapeArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(0.0,size/2.0);
		path.lineTo(0.0,-size/2.0);
		return path;
	}

	static Shape circleArrow(double size) {
		Ellipse2D circle = new Ellipse2D.Double(-size, -size/2.0, size, size);
		return circle;
	}

	static double circleX(int sides, int angle) {
		double coeff = (double)angle/(double)sides;
		return epsilon(Math.cos(2*coeff*Math.PI-halfPI));
	}
		
	static double circleY(int sides, int angle) {
		double coeff = (double)angle/(double)sides;
		return epsilon(Math.sin(2*coeff*Math.PI-halfPI));
	}

	static double epsilon(double v) {
		if (Math.abs(v) < 1.0E-10) return 0.0;
		return v;
	}

	static Point2D.Double findIntersection(Point2D.Double p1, Point2D.Double p2,
	                                       Point2D.Double p3, Point2D.Double p4) {

		double denominator = (p4.getY()-p3.getY())*(p2.getX() - p1.getX()) -
		                     (p4.getX()-p3.getX())*(p2.getY() - p1.getY());

		double ua = ((p4.getX()-p3.getX())*(p1.getY() - p3.getY()) -
		            (p4.getY()-p3.getY())*(p1.getX() - p3.getX()))/denominator;

		double x = epsilon(p1.getX() + ua * (p2.getX() - p1.getX()));
		double y = epsilon(p1.getY() + ua * (p2.getY() - p1.getY()));
		return new Point2D.Double(x, y);
	}	

	// Return the angle in radians
	static double calculateAngle(Line2D line, ArrowEnd end) {
		// Translate the line to 0,0
		double x1 = line.getX1();
		double y1 = line.getY1();
		double x2 = line.getX2();
		double y2 = line.getY2();
		double opposite = y2-y1;
		double adjacent = x2-x1;

		double radians = Math.atan(opposite/adjacent);

		if (adjacent < 0) radians += Math.PI;

		// TODO: Flip for other end
		return radians;
	}

	static private Paint mixColor(Paint p, double value) {
		if (p == null || !(p instanceof Color)) return p;
		Color c = (Color)p;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), ((int)value)*255/100);
	}

	static private float clamp(float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static private Paint fixGradients(Paint paint, Shape shape) {
		if (paint instanceof LinearGradientPaint) {
			LinearGradientPaint lgp = (LinearGradientPaint)paint;
			Point2D start = fixPoint(lgp.getStartPoint(), shape);
			Point2D end = fixPoint(lgp.getEndPoint(), shape);
			float[] fractions = lgp.getFractions();
			Color[] colors = lgp.getColors();
			/*
			System.out.println("Stops: ");
			for (int i = 0; i < fractions.length; i++) {
				System.out.println(""+fractions[i]+" "+colors[i]);
			}
			*/
			return new LinearGradientPaint(start, end, fractions, colors);
		}
		if (paint instanceof RadialGradientPaint) {
			RadialGradientPaint rgp = (RadialGradientPaint)paint;
			Point2D center = fixPoint(rgp.getCenterPoint(), shape);
			Point2D focus = fixPoint(rgp.getFocusPoint(), shape);
			float radius = rgp.getRadius()*(float)(shape.getBounds2D().getWidth()/2.0);
			return new RadialGradientPaint(center, radius, focus, rgp.getFractions(), rgp.getColors(), 
			                               MultipleGradientPaint.CycleMethod.NO_CYCLE);
		}
		return paint;
	}

	static private Point2D fixPoint(Point2D point, Shape shape) {
		Rectangle2D bounds = shape.getBounds2D();
		double height = bounds.getHeight();
		double width = bounds.getWidth();
		float x = (float)bounds.getX();
		float y = (float)bounds.getY();
		float xPoint = (float)(point.getX()*width);
		float yPoint = (float)(point.getY()*height);
		return new Point2D.Float(xPoint, yPoint);
	}

}
