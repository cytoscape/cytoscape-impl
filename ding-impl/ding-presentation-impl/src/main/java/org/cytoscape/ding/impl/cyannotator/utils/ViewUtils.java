package org.cytoscape.ding.impl.cyannotator.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class ViewUtils {

  static public String convertColor(Paint clr) {
		if (clr == null) 				return null;
		// System.out.println("ConvertColor: "+clr);
		if (clr instanceof LinearGradientPaint) {
			// System.out.println("lingrad");
			String lg = "lingrad(";
			LinearGradientPaint lingrad = (LinearGradientPaint)clr;
			Point2D start = lingrad.getStartPoint();
			Point2D end = lingrad.getEndPoint();
			lg += convertPoint(start)+";";
			lg += convertPoint(end)+";";
			float[] fractions = lingrad.getFractions();
			Color[] colors = lingrad.getColors();
			lg += convertStops(fractions, colors)+")";
			return lg;
		} else if (clr instanceof RadialGradientPaint) {
			// System.out.println("radgrad");
			String rg = "radgrad(";
			RadialGradientPaint radgrad = (RadialGradientPaint)clr;
			Point2D center = radgrad.getCenterPoint();
			Point2D focus = radgrad.getFocusPoint();
			float radius = radgrad.getRadius();
			rg += convertPoint(center)+";";
			rg += convertPoint(focus)+";";
			rg += radius+";";
			float[] fractions = radgrad.getFractions();
			Color[] colors = radgrad.getColors();
			rg += convertStops(fractions, colors)+")";
			return rg;
		} else if (clr instanceof Color) {
			// System.out.println("color");
			return Integer.toString(((Color)clr).getRGB());
		}
		return clr.toString();
  }

	static public String convertPoint(Point2D point) {
		if (point == null)  return "";
		return point.getX()+","+point.getY();
	}

	static public String convertStops(float[] fractions, Color[] colors) {
		String stops = null;
		for (int i = 0; i < fractions.length; i++) {
			if (stops != null)
				stops += ";";
			else
				stops = "";
			stops += fractions[i]+","+Integer.toString(colors[i].getRGB());
		}
		return stops;
	}

  static public Paint getColor(String strColor) {
		if (strColor == null)
			return null;
		if (strColor.startsWith("lingrad")) {
			String[] tokens = strColor.split("[(;)]");
			Point2D start = getPoint2D(tokens[1]);
			Point2D end = getPoint2D(tokens[2]);
			float[] fractions = new float[tokens.length-3];
			Color[] colors = new Color[tokens.length-3];
			getStops(tokens, 3, fractions, colors);
			return new LinearGradientPaint(start, end, fractions, colors);
		} else if (strColor.startsWith("radgrad")) {
			String[] tokens = strColor.split("[(;)]");
			Point2D center = getPoint2D(tokens[1]);
			Point2D focus = getPoint2D(tokens[2]);
			float radius = getFloat(tokens[3]);
			float[] fractions = new float[tokens.length-4];
			Color[] colors = new Color[tokens.length-4];
			getStops(tokens, 4, fractions, colors);
			CycleMethod method = CycleMethod.NO_CYCLE;
			return new RadialGradientPaint(center, radius, focus, fractions, colors, method);
		}
		return new Color(Integer.parseInt(strColor), true);
  }

  static public Paint getColor(Map<String, String> argMap, String key, Color defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return getColor(argMap.get(key));
	}

	static public void getStops(String[] tokens, int stopStart, float[] fractions, Color[] colors) {
		for (int i = stopStart; i < tokens.length; i++) {
			String[] stop = tokens[i].split(",");
			fractions[i-stopStart] = getFloat(stop[0]);
			colors[i-stopStart] = new Color(Integer.parseInt(stop[1]), true);
		}
	}

	static public Point2D getPoint2D(String point) {
		if (point.length() == 0) return null;
		String[] xy = point.split(",");
		return new Point2D.Double(getDouble(xy[0]), getDouble(xy[1]));
	}

  static public String getString(Map<String, String> argMap, String key, String defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return argMap.get(key);
	}

  static public Float getFloat(String fValue) { return Float.parseFloat(fValue);	}

  static public Float getFloat(Map<String, String> argMap, String key, float defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Float.parseFloat(argMap.get(key));
	}

  static public Integer getInteger(Map<String, String> argMap, String key, int defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Integer.parseInt(argMap.get(key));
	}

	static public Double getDouble(String dValue) { return Double.parseDouble(dValue); }

  static public Double getDouble(Map<String, String> argMap, String key, double defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Double.parseDouble(argMap.get(key));
	}

	static public Font getArgFont(Map<String, String> argMap, String defFamily, int defStyle, int defSize) {
		String family = getString(argMap, TextAnnotation.FONTFAMILY, defFamily);
		int size = getInteger(argMap, TextAnnotation.FONTSIZE, defSize);
		int style = getInteger(argMap, TextAnnotation.FONTSTYLE, defStyle);
		return new Font(family, style, size);
	}

	// Private methods
	static public void addNodeCoordinates(DRenderingEngine re, Map<String,String> argMap, double x, double y) {
		Point2D xy = getNodeCoordinates(re, x, y);
		argMap.put(Annotation.X,Double.toString(xy.getX()));
		argMap.put(Annotation.Y,Double.toString(xy.getY()));
	}

	static public Point2D getComponentCoordinates(DRenderingEngine re, Map<String, String> argMap) {
		// Get our current transform
		double[] nextLocn = new double[2];
		nextLocn[0] = 0.0;
		nextLocn[1] = 0.0;

		if (argMap.containsKey(Annotation.X))
			nextLocn[0] = Double.parseDouble(argMap.get(Annotation.X));
		if (argMap.containsKey(Annotation.Y))
			nextLocn[1] = Double.parseDouble(argMap.get(Annotation.Y));

		re.xformNodeToComponentCoords(nextLocn);
		
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	static public Rectangle2D getNodeCoordinates(DRenderingEngine re, Rectangle2D bounds) {
		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double x2 = bounds.getX()+bounds.getWidth();
		double y2 = bounds.getY()+bounds.getHeight();
		double[] nextLocn1 = new double[2];
		nextLocn1[0] = x1;
		nextLocn1[1] = y1;
		re.xformComponentToNodeCoords(nextLocn1);

		double[] nextLocn2 = new double[2];
		nextLocn2[0] = x2;
		nextLocn2[1] = y2;
		re.xformComponentToNodeCoords(nextLocn2);

		return new Rectangle2D.Double(nextLocn1[0], nextLocn1[1], nextLocn2[0]-nextLocn1[0], nextLocn2[1]-nextLocn1[1]);
	}

	static public Rectangle2D getComponentCoordinates(DRenderingEngine re, Rectangle2D bounds) {
		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double x2 = bounds.getX()+bounds.getWidth();
		double y2 = bounds.getY()+bounds.getHeight();
		double[] nextLocn1 = new double[2];
		nextLocn1[0] = x1;
		nextLocn1[1] = y1;
		re.xformNodeToComponentCoords(nextLocn1);

		double[] nextLocn2 = new double[2];
		nextLocn2[0] = x2;
		nextLocn2[1] = y2;
		re.xformNodeToComponentCoords(nextLocn2);

		return new Rectangle2D.Double(nextLocn1[0], nextLocn1[1], nextLocn2[0]-nextLocn1[0], nextLocn2[1]-nextLocn1[1]);
	}

	static public Point2D getNodeCoordinates(DRenderingEngine re, double x, double y) {
		double[] nextLocn = new double[2];
		nextLocn[0] = x;
		nextLocn[1] = y;
		re.xformComponentToNodeCoords(nextLocn);
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	static public Point2D getComponentCoordinates(DRenderingEngine re, double x, double y) {
		double[] nextLocn = new double[2];
		nextLocn[0] = x;
		nextLocn[1] = y;
		re.xformNodeToComponentCoords(nextLocn);
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

}
