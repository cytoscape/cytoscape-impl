package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
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
import java.util.stream.Collectors;

import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl.ArrowType;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.ArrowEnd;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class GraphicsUtilities {
	
	private static double halfPI = Math.PI / 2.0;

	public static Shape getShape(String shapeName, double x, double y, double width, double height) {
		ShapeType shapeType = getShapeType(shapeName);
		
		switch (shapeType) {
			case RECTANGLE: return rectangleShape(x, y, width, height);
			case ROUNDEDRECTANGLE: return roundedRectangleShape(x, y, width, height);
			case ELLIPSE: return ellipseShape(x, y, width, height);
			case STAR5: return starShape(5, x, y, width, height); // 5 pointed star
			case STAR6: return starShape(6, x, y, width, height); // 6 pointed star
			case TRIANGLE: return regularPolygon(3, x, y, width, height); // Triangle
			case PENTAGON: return regularPolygon(5, x, y, width, height); // Pentagon
			case HEXAGON: return regularPolygon(6, x, y, width, height); // Hexagon
			case OCTAGON: return regularPolygon(8, x, y, width, height); // Octagon added 3.6
			case PARALLELOGRAM: return parallelogramShape(x, y, width, height); // Parallelogram added 3.7
			case DIAMOND: return diamondShape(x, y, width, height); // Diamond added 3.8
			case V: return vShape(x, y, width, height); // V added 3.8
			case CUSTOM: return null;
			default: return rectangleShape(x, y, width, height);
		}
	}

	public static ShapeType getShapeType(String shapeName) {
		for(ShapeType type : ShapeType.values()) {
			if (shapeName.equals(type.shapeName()))
				return type;
		}
		return ShapeType.RECTANGLE; // If we can't do anything else...
	}

	public static ShapeType getShapeType(Map<String, String> argMap, String key, ShapeType defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		String shapeString = argMap.get(key);
		for (ShapeType type : ShapeType.values()) {
			if (shapeString.equalsIgnoreCase(type.shapeName()) || shapeString.equalsIgnoreCase(type.name())) {
				return type;
			}
		}
		return defValue;
	}

	public static List<String> getSupportedShapes() {
		return Arrays.stream(ShapeType.values()).map(ShapeType::shapeName).collect(Collectors.toList());
	}


	/**
	 *  Given a position and a size, draw a shape.
	 *  We use the ShapeAnnotation to get the shape itself, colors, strokes, etc.
	 */
	public static void drawShape(Graphics g, double x, double y, double width, double height, double rotation,
			ShapeAnnotation annotation, boolean isPrinting) {
		var g2 = (Graphics2D) g;

		float border = (float) (annotation.getBorderWidth() * annotation.getZoom());
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
			var originalBounds = shape.getBounds2D();
			double widthScale = destW / originalBounds.getWidth();
			double heightScale = destH / originalBounds.getHeight();

			var transform = new AffineTransform();
			transform.translate(destX, destY);
			transform.scale(widthScale, heightScale);
			transform.translate(-originalBounds.getX(), -originalBounds.getY());
			// TODO This needs to be tested!
			transform.rotate(Math.toRadians(rotation), destX + destW / 2, destY + destH / 2);
			shape = transform.createTransformedShape(shape);
		} else {
			if (rotation == 0) {
				shape = getShape(annotation.getShapeType(), x, y, width, height);
			} else {
				shape = getShape(annotation.getShapeType(), x, y, width, height);
				var transform = new AffineTransform();
				transform.rotate(Math.toRadians(rotation), x + width / 2, y + height / 2);
				shape = transform.createTransformedShape(shape);
			}
		}

		// Fill Color
		if (annotation.getFillColor() != null) {
			// If we've filled with a gradient, we need to fix it up a little. We create
			// our gradients using proportional x,y values rather than absolute x,y values.
			// Fix them now.
			var fillColor = annotation.getFillColor();
			fillColor = fixGradients(fillColor, shape);
			g2.setPaint(fillColor);
			float opacity = clamp((float) (annotation.getFillOpacity() / 100.0), 0.0f, 1.0f);
			final Composite originalComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2.fill(shape);
			g2.setComposite(originalComposite);
		}

		// Border
		if (border > 0.0f) { // only paint a border if the border thickness is greater than zero
			float opacity = clamp((float) (((ShapeAnnotationImpl) annotation).getBorderOpacity() / 100.0), 0.0f, 1.0f);
			var originalComposite = g2.getComposite();

			var color = annotation.getBorderColor();
			
			if (color == null)
				color = Color.BLACK;
			
			g2.setPaint(color);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2.setStroke(new BasicStroke(border));
			g2.draw(shape);

			g2.setComposite(originalComposite);
		}
	}

	public static Shape copyCustomShape(Shape s, double width, double height) {
		Rectangle bounds = s.getBounds();
		var sx = width  / bounds.getWidth();
		var sy = height / bounds.getHeight();
		AffineTransform t = AffineTransform.getScaleInstance(sx, sy);
		
		PathIterator i = s.getPathIterator(t);
		Path2D.Double path = new Path2D.Double();
		path.setWindingRule(i.getWindingRule());
		
		double[] nums = new double[6];
		for (; !i.isDone(); i.next()) {
			int type = i.currentSegment(nums);
			switch(type) {
			case PathIterator.SEG_CLOSE:
				path.closePath();
				break;
			case PathIterator.SEG_MOVETO:
				path.moveTo(nums[0], nums[1]);
				break;
			case PathIterator.SEG_LINETO:
				path.lineTo(nums[0], nums[1]);
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(nums[0], nums[1], nums[2], nums[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				path.curveTo(nums[0], nums[1], nums[2], nums[3], nums[4], nums[5]);
				break;
			}
		}
		
		return path;
	}
	
	public static String serializeShape(final Shape s) {
		final StringBuffer buffer = new StringBuffer();
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
				buffer.append("Z ");
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

	public static Shape deserializeShape(final String str) {
		final Path2D.Double path = new Path2D.Double();

		final String[] pieces = str.split("\\p{Space}+");
		final double[] nums = new double[6];
		for (int i = 0; i < pieces.length; /* increment based on command */) {
			final String cmd = pieces[i];
			i++; // move past the command
			if (cmd.equalsIgnoreCase("z")) {
				path.closePath();
			} else if (cmd.equalsIgnoreCase("zm")) {
				path.closePath();
				i += parseDoubles(pieces, i, 2, nums);
				path.moveTo(nums[0], nums[1]);
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

		return path;
	}

	private static int parseDoubles(final String[] pieces, final int startIndex, final int expectedNum, final double[] nums) {
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

	public static Shape getArrowShape(ArrowType arrowType, double size) {
		switch (arrowType) {
			case CIRCLE:
				return circleArrow(size);
			case CLOSED:
				return closedArrow(size);
			case CONCAVE:
				return concaveArrow(size);
			case DIAMOND:
				return diamondArrow(size);
			case OPEN:
				return openArrow(size);
			case TRIANGLE:
				return triangleArrow(size);
			case TSHAPE:
				return tshapeArrow(size);
			case X:
				return xArrow(size);
			default:
				return null;
		}
	}

	public static ArrowType getArrowType(String arrowName) {
		for (var type : ArrowType.values()) {
			if (arrowName.equals(type.getName()))
				return type;
		}
		
		return ArrowType.NONE; // If we can't do anything else...
	}

	public static ArrowType getArrowType(Map<String, String> argMap, String key, ArrowType defValue) {
		if (argMap.get(key) == null)
			return defValue;
		
		int arrowNumber = Integer.parseInt(argMap.get(key));
		
		for (var type : ArrowType.values()) {
			if (arrowNumber == type.ordinal())
				return type;
		}
		
		return defValue;
	}

	public static ArrowType[] getSupportedArrowTypes() {
		return ArrowType.values();
	}

	public static List<String> getSupportedArrowTypeNames() {
		return Arrays.stream(ArrowType.values()).map(ArrowType::getName).collect(Collectors.toList());
	}

	public static void drawArrow(Graphics g, Line2D line, ArrowEnd end, Paint paint, double size, ArrowType type) {
		if (line == null)
			return;

		// Get the shape
		var arrow = getArrowShape(type, size);

		// Translate and rotate the arrow
		if (end == ArrowEnd.SOURCE)
			arrow = transformArrowShape(arrow, line.getX2(), line.getY2(), line.getX1(), line.getY1());
		else
			arrow = transformArrowShape(arrow, line.getX1(), line.getY1(), line.getX2(), line.getY2());

		var g2 = (Graphics2D) g.create();

		if (paint != null)
			g2.setPaint(paint);

		// Handle opacity
		if (paint instanceof Color) {
			int alpha = ((Color) paint).getAlpha();
			float opacity = (float) alpha / (float) 255;
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		}

		if (type != ArrowType.OPEN && type != ArrowType.TSHAPE && type != ArrowType.X)
			g2.fill(arrow);

		g2.draw(arrow); // We're relying on the stroke to be done by the caller
		g2.dispose();
	}

	// Shapes.
	static private Shape rectangleShape(double x, double y, double width, double height) {
		// System.out.println("Drawing rectangle: "+x+","+y+" "+width+"x"+height);
		return new Rectangle2D.Double(x, y, width, height);
	}

	static private Shape parallelogramShape(double x, double y, double width, double height) {
		Path2D poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, 4);
		double xMax = x + width;
		double yMax = y + height;

		poly.moveTo(x, y);
		poly.lineTo(((2.0f * xMax) + x) / 3.0f, y);
		poly.lineTo(xMax, yMax);
		poly.lineTo(((2.0f * x) + xMax) / 3.0f, yMax);
		poly.closePath();
		return poly;
	}

	static private Shape diamondShape(double x, double y, double width, double height) {
		Path2D poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, 4);
		double xMax = x + width;
		double yMax = y + height;
		double xMid = x + (width/2);
		double yMid = y + (height/2);

		poly.moveTo(xMid, y);
		poly.lineTo(xMax, yMid);
		poly.lineTo(xMid, yMax);
		poly.lineTo(x, yMid);
		poly.closePath();
		return poly;
	}

	static private Shape vShape(double x, double y, double width, double height) {
		Path2D poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, 4);
		double xMax = x + width;
		double yMax = y + height;
		double xMid = x + (width/2);
		double yMid = y + (height/2);

		poly.moveTo(x, y);
		poly.lineTo(xMid, yMid);
		poly.lineTo(xMax, y);
		poly.lineTo(xMid, yMax);
		poly.closePath();
		return poly;
	}

	static private Shape roundedRectangleShape(double x, double y, double width, double height) {
		return new RoundRectangle2D.Double(x, y, width, height, width / 10, width / 10);
	}

	static private Shape ellipseShape(double x, double y, double width, double height) {
		return new Ellipse2D.Double(x, y, width, height);
	}

	static private Shape regularPolygon(int sides, double x, double y, double width, double height) {
		Path2D poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, sides);
		width = width / 2;
		height = height / 2;
		x = x + width;
		y = y + height;
		Point2D.Double points[] = new Point2D.Double[sides];
		for (int i = 0; i < sides; i++) {
			double x1 = circleX(sides, i, true) * width + x;
			double y1 = circleY(sides, i, true) * height + y;
			points[i] = new Point2D.Double(x1, y1);
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
		Path2D poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, sides);
		width = width / 2;
		height = height / 2;
		x = x + width;
		y = y + height;
		int nPoints = sides * 2;
		Point2D.Double points[] = new Point2D.Double[nPoints];
		for (int i = 0; i < sides; i++) {
			double x1 = circleX(sides, i, false) * width + x;
			double y1 = circleY(sides, i, false) * height + y;
			double x2 = circleX(sides, (i + 2) % sides, false) * width + x;
			double y2 = circleY(sides, (i + 2) % sides, false) * height + y;
			points[i * 2] = new Point2D.Double(x1, y1);
			points[(i * 2 + 4) % nPoints] = new Point2D.Double(x2, y2);
		}

		// Fill in the intersection points
		for (int i = 0; i < nPoints; i = i + 2) {
			int p1 = i;
			int p2 = (i + 4) % nPoints;
			int p3 = (i + 2) % nPoints;
			int p4 = (p3 + nPoints - 4) % nPoints;

			points[(i + 1) % nPoints] = findIntersection(points[p1], points[p2], points[p3], points[p4]);
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
		path.moveTo(-size * 1.7, -size / 2.0);
		path.lineTo(0.0, 0.0);
		path.lineTo(-size * 1.7, size / 2.0);
		return path;
	}

	// Create a shape with a filled 30 degree arrow
	static Shape closedArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(-size * 1.7, -size / 2.0);
		path.lineTo(0.0, 0.0);
		path.lineTo(-size * 1.7, size / 2.0);
		path.closePath();
		return path;
	}

	// Create a shape with a 30 degree arrow
	static Shape concaveArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(-size, 0.0);
		path.lineTo(-size * 1.7, -size / 2.0);
		path.lineTo(0.0, 0.0);
		path.lineTo(-size * 1.7, size / 2.0);
		path.closePath();
		return path;
	}

	static Shape diamondArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(-size * 1.7 * 2, 0.0);
		path.lineTo(-size * 1.7, size);
		path.lineTo(0.0, 0.0);
		path.lineTo(-size * 1.7, -size);
		path.closePath();
		return path;
	}

	static Shape triangleArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(0.0, 0.0);
		path.lineTo(-size / 2.0, -size / 2.0);
		path.lineTo(-size / 2.0, size / 2.0);
		path.closePath();
		return path;
	}

	static Shape tshapeArrow(double size) {
		Path2D path = new Path2D.Double();
		path.moveTo(0.0, size / 2.0);
		path.lineTo(0.0, -size / 2.0);
		return path;
	}
	
	static Shape xArrow(double size) {
		Path2D path = new Path2D.Double();
		// The 'X' shape is shifted by 50% to the left to compensate the end of the arrow line,
		// which must end at the center of the 'X'.
		path.moveTo(size / 2.0, -size / 2.0);
		path.lineTo(-size / 2.0, size / 2.0);
		path.moveTo(-size / 2.0, -size / 2.0);
		path.lineTo(size / 2.0, size / 2.0);
		return path;
	}
	
// Use this to debug or as a template for new arrow shapes
//	static Shape squareArrow(double size) {
//		// Draws a square so we can localize the arrow's bounding box
//		Path2D path = new Path2D.Double();
//		path.moveTo(0.0, -size / 2.0);
//		path.lineTo(0.0, size / 2.0);
//		path.lineTo(-size, size / 2.0);
//		path.lineTo(-size, -size / 2.0);
//		path.closePath();
//		return path;
//	}

	static Shape circleArrow(double size) {
		Ellipse2D circle = new Ellipse2D.Double(-size, -size / 2.0, size, size);
		return circle;
	}

	static double circleX(int sides, int angle, boolean rot) {
		double coeff = (double) angle / (double) sides;
		if (rot && (sides % 2 == 0)) {
			if (sides == 8) {
				coeff += 0.5 / (double) sides;
			}
			return epsilon(Math.cos(2 * coeff * Math.PI));
		} else
			return epsilon(Math.cos(2 * coeff * Math.PI - halfPI));
	}

	static double circleY(int sides, int angle, boolean rot) {
		double coeff = (double) angle / (double) sides;
		if (rot && (sides % 2 == 0)) {
			if (sides == 8) {
				coeff += 0.5 / (double) sides;
			}
			return epsilon(Math.sin(2 * coeff * Math.PI));
		} else
			return epsilon(Math.sin(2 * coeff * Math.PI - halfPI));
	}

	static double epsilon(double v) {
		if (Math.abs(v) < 1.0E-10)
			return 0.0;
		return v;
	}

	static Point2D.Double findIntersection(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3, Point2D.Double p4) {
		double denominator = (p4.getY() - p3.getY()) * (p2.getX() - p1.getX())
				- (p4.getX() - p3.getX()) * (p2.getY() - p1.getY());

		double ua = ((p4.getX() - p3.getX()) * (p1.getY() - p3.getY())
				- (p4.getY() - p3.getY()) * (p1.getX() - p3.getX())) / denominator;

		double x = epsilon(p1.getX() + ua * (p2.getX() - p1.getX()));
		double y = epsilon(p1.getY() + ua * (p2.getY() - p1.getY()));
		return new Point2D.Double(x, y);
	}

	static Shape transformArrowShape(Shape arrow, double x1, double y1, double x2, double y2) {
		double angle = Math.atan2(y2 - y1, x2 - x1);
		
		var trans = new AffineTransform();
		trans.translate(x2, y2);
		trans.rotate(angle);
		arrow = trans.createTransformedShape(arrow);
		
		return arrow;
	}

	static private Paint mixColor(Paint p, double value) {
		if (p == null || !(p instanceof Color))
			return p;
		Color c = (Color) p;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), ((int) value) * 255 / 100);
	}

	static private float clamp(float value, float min, float max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	static private Paint fixGradients(Paint paint, Shape shape) {
		if(paint instanceof MultipleGradientPaint) {
			MultipleGradientPaint mgp = (MultipleGradientPaint) paint;
			float[] fractions = mgp.getFractions();
			Color[] colors = mgp.getColors();
			
			// the gradient is translated and scaled into the shape
			var bounds = shape.getBounds();
			var tr = mgp.getTransform();
			tr.translate(bounds.getX(), bounds.getY());
			tr.scale(bounds.getWidth(), bounds.getHeight());
			
			if (paint instanceof LinearGradientPaint) {
				LinearGradientPaint lgp = (LinearGradientPaint) paint;
				Point2D start = lgp.getStartPoint();
				Point2D end = lgp.getEndPoint();
				return new LinearGradientPaint(start, end, fractions, colors, CycleMethod.NO_CYCLE, ColorSpaceType.SRGB, tr);
			}
			if (paint instanceof RadialGradientPaint) {
				RadialGradientPaint rgp = (RadialGradientPaint) paint;
				Point2D center = rgp.getCenterPoint();
				Point2D focus = rgp.getFocusPoint();
				float radius = rgp.getRadius();
				return new RadialGradientPaint(center, radius, focus, fractions, colors, CycleMethod.NO_CYCLE, ColorSpaceType.SRGB, tr);
			}
		}
		return paint;
	}

}
