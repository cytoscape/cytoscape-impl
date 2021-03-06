package org.cytoscape.ding.impl.cyannotator.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

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

public class ViewUtils {

	static public String convertColor(Paint clr) {
		if (clr == null)
			return null;
		
		if (clr instanceof LinearGradientPaint) {
			String lg = "lingrad(";
			LinearGradientPaint lingrad = (LinearGradientPaint) clr;
			Point2D start = lingrad.getStartPoint();
			Point2D end = lingrad.getEndPoint();
			lg += convertPoint(start) + ";";
			lg += convertPoint(end) + ";";
			float[] fractions = lingrad.getFractions();
			Color[] colors = lingrad.getColors();
			lg += convertStops(fractions, colors) + ")";
			
			return lg;
		} else if (clr instanceof RadialGradientPaint) {
			// System.out.println("radgrad");
			String rg = "radgrad(";
			RadialGradientPaint radgrad = (RadialGradientPaint) clr;
			Point2D center = radgrad.getCenterPoint();
			Point2D focus = radgrad.getFocusPoint();
			float radius = radgrad.getRadius();
			rg += convertPoint(center) + ";";
			rg += convertPoint(focus) + ";";
			rg += radius + ";";
			float[] fractions = radgrad.getFractions();
			Color[] colors = radgrad.getColors();
			rg += convertStops(fractions, colors) + ")";
			
			return rg;
		} else if (clr instanceof Color) {
			return String.format("#%06X", 0xFFFFFF&(((Color) clr).getRGB()));
		}
		
		return clr.toString();
	}

	static public String convertPoint(Point2D point) {
		return point == null ? "" : point.getX() + "," + point.getY();
	}

	static public String convertStops(float[] fractions, Color[] colors) {
		String stops = null;

		for (int i = 0; i < fractions.length; i++) {
			if (stops != null)
				stops += ";";
			else
				stops = "";
			
			stops += fractions[i] + "," + Integer.toString(colors[i].getRGB());
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
			float[] fractions = new float[tokens.length - 3];
			Color[] colors = new Color[tokens.length - 3];
			getStops(tokens, 3, fractions, colors);
			
			return new LinearGradientPaint(start, end, fractions, colors);
		} else if (strColor.startsWith("radgrad")) {
			String[] tokens = strColor.split("[(;)]");
			Point2D center = getPoint2D(tokens[1]);
			Point2D focus = getPoint2D(tokens[2]);
			float radius = getFloat(tokens[3]);
			float[] fractions = new float[tokens.length - 4];
			Color[] colors = new Color[tokens.length - 4];
			getStops(tokens, 4, fractions, colors);
			CycleMethod method = CycleMethod.NO_CYCLE;
			
			return new RadialGradientPaint(center, radius, focus, fractions, colors, method);
		} else if (strColor.startsWith("#")) {
      // Hex string
      String hex = strColor.substring(1);
      int clr = Integer.parseInt(hex, 16);
		  return new Color(clr);
    }
		
		return new Color(Integer.parseInt(strColor), true);
	}

	static public Paint getColor(Map<String, String> argMap, String key, Color defValue) {
		if (argMap.get(key) == null)
			return defValue;
		
		return getColor(argMap.get(key));
	}

	static public void getStops(String[] tokens, int stopStart, float[] fractions, Color[] colors) {
		for (int i = stopStart; i < tokens.length; i++) {
			String[] stop = tokens[i].split(",");
			fractions[i - stopStart] = getFloat(stop[0]);
			colors[i - stopStart] = new Color(Integer.parseInt(stop[1]), true);
		}
	}

	static public Point2D getPoint2D(String point) {
		if (point.length() == 0)
			return null;
		
		var xy = point.split(",");
		
		return new Point2D.Double(getDouble(xy[0]), getDouble(xy[1]));
	}

	static public String getString(Map<String, String> argMap, String key, String defValue) {
		if (argMap.get(key) == null)
			return defValue;
		
		try {
			return argMap.get(key);
		} catch (Exception e) {
			return defValue;
		}
	}

	static public Float getFloat(String fValue) {
		return Float.parseFloat(fValue);
	}

	static public Float getFloat(Map<String, String> argMap, String key, float defValue) {
		if (argMap.get(key) == null)
			return defValue;
		
		try {
			return Float.parseFloat(argMap.get(key));
		} catch (Exception e) {
			return defValue;
		}
	}

	static public Integer getInteger(Map<String, String> argMap, String key, int defValue) {
		if (argMap.get(key) == null)
			return defValue;
		
		try {
			return Integer.parseInt(argMap.get(key));
		} catch (Exception e) {
			return defValue;
		}
	}

	static public Double getDouble(String dValue) {
		return Double.parseDouble(dValue);
	}

	static public Double getDouble(Map<String, String> argMap, String key, double defValue) {
		if (argMap.get(key) == null)
			return defValue;

		try {
			return Double.parseDouble(argMap.get(key));
		} catch (Exception e) {
			return defValue;
		}
	}

	static public Font getArgFont(Map<String, String> argMap, String defFamily, int defStyle, int defSize) {
		var family = getString(argMap, TextAnnotation.FONTFAMILY, defFamily);
		int size = getInteger(argMap, TextAnnotation.FONTSIZE, defSize);
		int style = getFontStyle(argMap, TextAnnotation.FONTSTYLE, defStyle);

		return new Font(family, style, size);
	}

	static public void addNodeCoordinates(DRenderingEngine re, Map<String, String> argMap, double x, double y) {
		var xy = re.getTransform().getNodeCoordinates((int) x, (int) y);
		argMap.put(Annotation.X, Double.toString(xy.getX()));
		argMap.put(Annotation.Y, Double.toString(xy.getY()));
	}

	static public Point2D getComponentCoordinates(DRenderingEngine re, Map<String, String> argMap) {
		// Get our current transform
		var nextLocn = new double[2];
		nextLocn[0] = 0.0;
		nextLocn[1] = 0.0;

		if (argMap.containsKey(Annotation.X))
			nextLocn[0] = Double.parseDouble(argMap.get(Annotation.X));
		if (argMap.containsKey(Annotation.Y))
			nextLocn[1] = Double.parseDouble(argMap.get(Annotation.Y));

		re.getTransform().xformNodeToImageCoords(nextLocn);
		
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	/**
	 * Force-select the passed annotation. If the annotation selection mode is off, this also turns it on.
	 */
	public static void selectAnnotation(DRenderingEngine re, DingAnnotation annotation) {
		var lexicon = re.getVisualLexicon();
		var vp = lexicon.lookup(CyNetwork.class, "NETWORK_ANNOTATION_SELECTION");
		
		if (vp != null) {
			var view = re.getViewModel();
			
			if (!Boolean.TRUE.equals(view.getVisualProperty(vp)))
				view.setLockedValue(vp, Boolean.TRUE);
		}
		
		annotation.setSelected(true);
		
		var cyAnnotator = re.getCyAnnotator();
		cyAnnotator.addAnnotation(annotation);
		cyAnnotator.setSelectedAnnotation(annotation, true);
	}

	public static void reorder(List<DingAnnotation> annotations, Shift shift, DRenderingEngine re) {
		var cyAnnotator = re.getCyAnnotator();
		var tree = cyAnnotator.getAnnotationTree();

		var byCanvas = annotations.stream().collect(Collectors.groupingBy(DingAnnotation::getCanvasName));

		var fga = byCanvas.get(Annotation.FOREGROUND);

		if (fga != null && !fga.isEmpty())
			tree.shift(shift, Annotation.FOREGROUND, fga);

		var bga = byCanvas.get(Annotation.BACKGROUND);

		if (bga != null && !bga.isEmpty())
			tree.shift(shift, Annotation.BACKGROUND, bga);

		tree.resetZOrder();
		re.updateView(UpdateType.JUST_ANNOTATIONS);
	}

	public static String getFontStyle(int style) {
		if (style == Font.PLAIN)
			return "plain";
		if (style == Font.BOLD)
			return "bold";
		if (style == Font.ITALIC)
			return "italic";
		if (style == (Font.ITALIC | Font.BOLD))
			return "bolditalic";
		return "";
	}

	static public int getFontStyle(Map<String, String> argMap, String key, int defValue) {
		if (argMap.get(key) == null)
			return defValue;

    String style = argMap.get(key);
		if (style.equals("plain"))
		  return Font.PLAIN;
		if (style.equals("bold"))
			return Font.BOLD;
		if (style.equals("italic"))
			return Font.ITALIC;
		if (style.equals("bolditalic"))
		  return (Font.ITALIC | Font.BOLD);
		return defValue;
	}

	public static void styleWindowStateButton(AbstractButton btn) {
		final int size = 16;

		btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		btn.setMinimumSize(new Dimension(size, size));
		btn.setPreferredSize(new Dimension(size, size));
		btn.setSize(new Dimension(size, size));
		btn.setRolloverEnabled(false);
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		btn.setContentAreaFilled(false);
	}
}
