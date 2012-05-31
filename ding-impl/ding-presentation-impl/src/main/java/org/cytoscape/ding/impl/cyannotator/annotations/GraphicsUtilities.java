package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation.ShapeType;

class GraphicsUtilities {
		private static double halfPI = Math.PI/2.0;

		protected static final ShapeType supportedShapes[] = {
			ShapeType.RECTANGLE, ShapeType.ROUNDEDRECTANGLE, ShapeType.ELLIPSE, ShapeType.STAR5, 
			ShapeType.TRIANGLE, ShapeType.STAR6, ShapeType.HEXAGON, ShapeType.PENTAGON
		};

		static public Shape getShape(ShapeType shapeType, double x, double y, double width, double height) {
			switch(shapeType) {
				case RECTANGLE: return rectangleShape(x, y, width, height);
				case ROUNDEDRECTANGLE: return roundedRectangleShape(x, y, width, height);
				case ELLIPSE: return ellipseShape(x, y, width, height);
				case STAR5: return starShape(5, x, y, width, height); // 5 pointed star
				case STAR6: return starShape(6, x, y, width, height); // 6 pointed star
				case TRIANGLE: return regularPolygon(3, x, y, width, height); // Pentagon
				case PENTAGON: return regularPolygon(5, x, y, width, height); // Pentagon
				case HEXAGON: return regularPolygon(6, x, y, width, height); // Hexagon
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

		static public ShapeType[] getSupportedShapes() {
			return supportedShapes;
		}

		// Given a position and a size, draw a shape.  We use the ShapeAnnotation to get the
		// shape itself, colors, strokes, etc.
		static public void drawShape(Graphics g, double x, double y, double width, double height, 
		                             ShapeAnnotation annotation) {
			Graphics2D g2 = (Graphics2D)g;

			// System.out.println("drawShape: ("+x+","+y+","+width+"x"+height+")");

			// Get the shape
			Shape shape = getShape(annotation.getShapeType(), x, y, width, height);
			// System.out.println("drawShape: shape = "+shape.toString());

			// Stroke it
			float border = (float)annotation.getBorderWidth();
			if (border < 1.0f) border = 1.0f;

			// Set our fill color
			if (annotation.getFillColor() != null) {
				// System.out.println("drawShape: fill color = "+annotation.getFillColor());
				// System.out.println("drawShape: fill opacity = "+annotation.getFillOpacity());
				g2.setPaint(annotation.getFillColor());
				g2.fill(shape);
			}

			if (annotation.getBorderColor() != null && !annotation.isSelected()) {
				// System.out.println("drawShape: border color = "+annotation.getBorderColor());
				// System.out.println("drawShape: border opacity = "+annotation.getBorderOpacity());
				g2.setPaint(annotation.getBorderColor());
				g2.setStroke(new BasicStroke(border));
				g2.draw(shape);
			} else if (annotation.isSelected()) {
				// Create a yellow border around the shape
				BasicStroke stroke = new BasicStroke(border);
				Shape strokedShape = stroke.createStrokedShape(shape);
				g2.setPaint(Color.YELLOW);
				g2.draw(strokedShape);
			} else {
				g2.draw(shape);
			}
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
			
}
