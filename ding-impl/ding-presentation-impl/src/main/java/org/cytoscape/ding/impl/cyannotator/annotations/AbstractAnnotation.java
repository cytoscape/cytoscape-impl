package org.cytoscape.ding.impl.cyannotator.annotations;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.ContentChangeListener;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

@SuppressWarnings("serial")
public abstract class AbstractAnnotation extends JComponent implements DingAnnotation {
	
	private boolean selected;

	private double globalZoom = 1.0;
	private double myZoom = 1.0;


	private DGraphView.Canvas canvasName;
	private UUID uuid = UUID.randomUUID();

	private Set<ArrowAnnotation> arrowList;

	protected boolean usedForPreviews;
	protected DGraphView view;
	protected ArbitraryGraphicsCanvas canvas;
	protected GroupAnnotationImpl parent;
	protected CyAnnotator cyAnnotator;
	protected String name = null;

	protected static final String ID = "id";
	protected static final String TYPE = "type";
	protected static final String ANNOTATION_ID = "uuid";
	protected static final String PARENT_ID = "parent";

	protected Map<String, String> savedArgMap;
	protected double zOrder = 0;

	protected final Window owner;

	/**
	 * This constructor is used to create an empty annotation
	 * before adding to a specific view.  In order for this annotation
	 * to be functional, it must be added to the AnnotationManager
	 * and setView must be called.
	 */
	protected AbstractAnnotation(DGraphView view, Window owner) {
		this.owner = owner;
		this.view = view;
		this.cyAnnotator = view == null ? null : view.getCyAnnotator();
		arrowList = new HashSet<ArrowAnnotation>();
		this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
		this.canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
		this.globalZoom = view.getZoom();
	}

	protected AbstractAnnotation(AbstractAnnotation c, Window owner) {
		this(c.view, owner);
		arrowList = new HashSet<ArrowAnnotation>(c.arrowList);
		this.canvas = c.canvas;
		this.canvasName = c.canvasName;
	}

	protected AbstractAnnotation(DGraphView view, double x, double y, double zoom, Window owner) {
		this(view, owner);
		setLocation((int)x, (int)y);
	}

	protected AbstractAnnotation(DGraphView view, Map<String, String> argMap, Window owner) {
		this(view, owner);

		Point2D coords = getComponentCoordinates(argMap);
		this.globalZoom = getDouble(argMap, ZOOM, 1.0);
		this.zOrder = getDouble(argMap, Z, 0.0);
		name = argMap.containsKey(NAME) ? argMap.get(NAME) : null;
		
		String canvasString = getString(argMap, CANVAS, FOREGROUND);
		if (canvasString != null && canvasString.equals(BACKGROUND)) {
			this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS));
			this.canvasName = DGraphView.Canvas.BACKGROUND_CANVAS;
		}

		setLocation((int)coords.getX(), (int)coords.getY());
		
		if (argMap.containsKey(ANNOTATION_ID))
			this.uuid = UUID.fromString(argMap.get(ANNOTATION_ID));
		
		/*
		if (argMap.containsKey(PARENT_ID)) {
			// See if the parent already exists
			UUID parent_uuid = UUID.fromString(argMap.get(PARENT_ID));
			DingAnnotation parentAnnotation = cyAnnotator.getAnnotation(parent_uuid);
			if (parentAnnotation != null && parentAnnotation instanceof GroupAnnotation) {
				// It does -- add ourselves to it
				((GroupAnnotation)parentAnnotation).addMember((Annotation)this);
			} else {
				// It doesn't -- let the parent add us
			}
		}
		*/
	}
	//------------------------------------------------------------------------
//	public void setView(DGraphView view) {
//		this.view = view;
//		this.cyAnnotator = view.getCyAnnotator();
//		this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
//		this.canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
//		this.globalZoom = view.getZoom();
//		if (savedArgMap != null) {
//			Point2D coords = getComponentCoordinates(savedArgMap);
//			this.globalZoom = Double.parseDouble(savedArgMap.get(ZOOM));
//			String canvasString = savedArgMap.get(CANVAS);
//			if (canvasString != null && canvasString.equals(BACKGROUND)) {
//				this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS));
//				this.canvasName = DGraphView.Canvas.BACKGROUND_CANVAS;
//			}
//
//			setLocation((int)coords.getX(), (int)coords.getY());
//			if (savedArgMap.containsKey(ANNOTATION_ID))
//				this.uuid = UUID.fromString(savedArgMap.get(ANNOTATION_ID));
//		}
//	}
		

	public String toString() {
		return getArgMap().get("type")+" annotation "+uuid.toString()+" at "+getX()+", "+getY()+" zoom="+globalZoom+" on canvas "+canvasName;
	}

	@Override
	public String getCanvasName() {
		if (canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS))
			return BACKGROUND;
		return FOREGROUND;
	}

	@Override
	public void setCanvas(String cnvs) {
		canvasName = (cnvs.equals(BACKGROUND)) ? 
				DGraphView.Canvas.BACKGROUND_CANVAS : DGraphView.Canvas.FOREGROUND_CANVAS;
		canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(canvasName));
		for (ArrowAnnotation arrow: arrowList) 
			if (arrow instanceof DingAnnotation)
				((DingAnnotation)arrow).setCanvas(cnvs);

		update();		// Update network attributes
	}

	@Override
	public void changeCanvas(final String cnvs) {
		// Are we really changing anything?
		if ((cnvs.equals(BACKGROUND) && canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS)) ||
		    (cnvs.equals(FOREGROUND) && canvasName.equals(DGraphView.Canvas.FOREGROUND_CANVAS)))
			return;

		ViewUtil.invokeOnEDTAndWait(() -> {
			if (!(this instanceof ArrowAnnotationImpl)) {
				for (ArrowAnnotation arrow: arrowList) {
					if (arrow instanceof DingAnnotation)
						((DingAnnotation)arrow).changeCanvas(cnvs);
				}
			}
			
			canvas.remove(this);	// Remove ourselves from the current canvas
			canvas.repaint();  	// update the canvas
			setCanvas(cnvs);		// Set the new canvas
			canvas.add(this);	// Add ourselves		
			canvas.repaint();  	// update the canvas
		});
	}

	@Override
	public CyNetworkView getNetworkView() {
		return (CyNetworkView)view;
	}

	@Override
	public ArbitraryGraphicsCanvas getCanvas() {
		return canvas;
	}

	public JComponent getComponent() {
		return (JComponent)this;
	}

	public UUID getUUID() {
		return uuid;
	}

	public double getZOrder() {
		return zOrder;
	}

	@Override
	public void addComponent(final JComponent cnvs) {
		ViewUtil.invokeOnEDTAndWait(() -> {
			if (inCanvas(canvas) && (canvas == cnvs)) {
				canvas.setComponentZOrder(this, (int)zOrder);
				return;
			}

			if (cnvs == null && canvas != null) {
	
			} else if (cnvs == null) {
				setCanvas(FOREGROUND);
			} else {
				if (cnvs.equals(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS)))
					setCanvas(BACKGROUND);
				else
					setCanvas(FOREGROUND);
			}
			canvas.add(this.getComponent());
			canvas.setComponentZOrder(this, (int)zOrder);
		});
	}
    
	@Override
	public CyAnnotator getCyAnnotator() {return cyAnnotator;}

	@Override
	public void setGroupParent(GroupAnnotation parent) {
		if (parent instanceof GroupAnnotationImpl) {
			this.parent = (GroupAnnotationImpl)parent;
		} else if (parent == null) {
			this.parent = null;
		}
		cyAnnotator.addAnnotation(this);
	}

	@Override
	public GroupAnnotation getGroupParent() {
		return (GroupAnnotation)parent;
	}
    
	public void moveAnnotation(Point2D location) {
		// Location is in "node coordinates"
		Point2D coords = getComponentCoordinates(location.getX(), location.getY());
		if (!(this instanceof ArrowAnnotationImpl)) {
			setLocation((int)coords.getX(), (int)coords.getY());
		}
	}

	public void setLocation(final int x, final int y) {
		ViewUtil.invokeOnEDTAndWait(() -> {
			super.setLocation(x, y);
			canvas.modifyComponentLocation(x, y, this);
		});
	}

	public void setSize(final int width, final int height) {
		ViewUtil.invokeOnEDTAndWait(() -> {
			super.setSize(width, height);
		});
	}

	public Point getLocation() { return super.getLocation(); }

	public boolean contains(int x, int y) {
		if (x > getX() && y > getY() && x-getX() < getWidth() && y-getY() < getHeight())
			return true;
		return false;
	}

	public void removeAnnotation() {
		ViewUtil.invokeOnEDTAndWait(() -> {
			canvas.remove(this);
			cyAnnotator.removeAnnotation(this);
			for (ArrowAnnotation arrow: arrowList) {
				if (arrow instanceof DingAnnotation)
					((DingAnnotation)arrow).removeAnnotation();
			}
			if (parent != null)
				parent.removeMember(this);
	
			canvas.repaint();
		});
	}

	public void resizeAnnotation(double width, double height) {};

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public double getZoom() { return globalZoom; }
	public void setZoom(double zoom) {  globalZoom = zoom;  }
      
	public double getSpecificZoom() {return myZoom; }
	public void setSpecificZoom(double zoom) { myZoom = zoom;  }

	public boolean isSelected() { return selected; }
	public void setSelected(boolean selected) {
		this.selected = selected;
		cyAnnotator.setSelectedAnnotation(this, selected);
	}

	public void addArrow(ArrowAnnotation arrow) { arrowList.add(arrow); }
	public void removeArrow(ArrowAnnotation arrow) { arrowList.remove(arrow); 	}
	public Set<ArrowAnnotation> getArrows() { return arrowList; }

	
	@Override
	public Map<String,String> getArgMap() {
		Map<String, String> argMap = new HashMap<String, String>();
		if (name != null)
			argMap.put(NAME, this.name);
		addNodeCoordinates(argMap);
		argMap.put(ZOOM,Double.toString(this.globalZoom));
		if (canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS))
			argMap.put(CANVAS, BACKGROUND);
		else
			argMap.put(CANVAS, FOREGROUND);
		argMap.put(ANNOTATION_ID, this.uuid.toString());

		if (parent != null)
			argMap.put(PARENT_ID, parent.getUUID().toString());

		int zOrder = canvas.getComponentZOrder(getComponent());
		argMap.put(Z, Integer.toString(zOrder));

		return argMap;
	}

	public boolean usedForPreviews() { return usedForPreviews; }

	public void setUsedForPreviews(boolean v) { usedForPreviews = v; }

	public void drawAnnotation(Graphics g, double x,
	                           double y, double scaleFactor) {
	}

	public void update() {
		updateAnnotationAttributes();
		getCanvas().repaint();
	}

	// Component overrides
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;

		/* Set up all of our anti-aliasing, etc. here to avoid doing it redundantly */
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

    // High quality color rendering is ON.
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		// Text antialiasing is ON.
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);


		if (!usedForPreviews()) {
			// We need to control composite ourselves for previews...
			g2.setComposite(AlphaComposite.Src);
		}
	}

	public JDialog getModifyDialog() {return null;}

	// Protected methods
	protected void updateAnnotationAttributes() {
		if (!usedForPreviews) {
			cyAnnotator.addAnnotation(this);
			contentChanged();
		}
	}

  protected String convertColor(Paint clr) {
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

	protected String convertPoint(Point2D point) {
		if (point == null)  return "";
		return point.getX()+","+point.getY();
	}

	protected String convertStops(float[] fractions, Color[] colors) {
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

  protected Paint getColor(String strColor) {
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

  protected Paint getColor(Map<String, String> argMap, String key, Color defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return getColor(argMap.get(key));
	}

	protected void getStops(String[] tokens, int stopStart, float[] fractions, Color[] colors) {
		for (int i = stopStart; i < tokens.length; i++) {
			String[] stop = tokens[i].split(",");
			fractions[i-stopStart] = getFloat(stop[0]);
			colors[i-stopStart] = new Color(Integer.parseInt(stop[1]), true);
		}
	}

	protected Point2D getPoint2D(String point) {
		if (point.length() == 0) return null;
		String[] xy = point.split(",");
		return new Point2D.Double(getDouble(xy[0]), getDouble(xy[1]));
	}

  protected String getString(Map<String, String> argMap, String key, String defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return argMap.get(key);
	}

  protected Float getFloat(String fValue) { return Float.parseFloat(fValue);	}

  protected Float getFloat(Map<String, String> argMap, String key, float defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Float.parseFloat(argMap.get(key));
	}

  protected Integer getInteger(Map<String, String> argMap, String key, int defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Integer.parseInt(argMap.get(key));
	}

	protected Double getDouble(String dValue) { return Double.parseDouble(dValue); }

  protected Double getDouble(Map<String, String> argMap, String key, double defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Double.parseDouble(argMap.get(key));
	}

	protected Font getArgFont(Map<String, String> argMap, String defFamily, int defStyle, int defSize) {
		String family = getString(argMap, TextAnnotation.FONTFAMILY, defFamily);
		int size = getInteger(argMap, TextAnnotation.FONTSIZE, defSize);
		int style = getInteger(argMap, TextAnnotation.FONTSTYLE, defStyle);
		return new Font(family, style, size);
	}

	// Private methods
	private void addNodeCoordinates(Map<String, String> argMap) {
		Point2D xy = getNodeCoordinates(getX(), getY());
		argMap.put(X,Double.toString(xy.getX()));
		argMap.put(Y,Double.toString(xy.getY()));
	}

	protected Point2D getComponentCoordinates(Map<String, String> argMap) {
		// Get our current transform
		double[] nextLocn = new double[2];
		nextLocn[0] = 0.0;
		nextLocn[1] = 0.0;

		if (argMap.containsKey(X))
			nextLocn[0] = Double.parseDouble(argMap.get(X));
		if (argMap.containsKey(Y))
			nextLocn[1] = Double.parseDouble(argMap.get(Y));

		view.xformNodeToComponentCoords(nextLocn);
		
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	protected Point2D getNodeCoordinates(double x, double y) {
    // Get our current transform
		double[] nextLocn = new double[2];
		nextLocn[0] = x;
		nextLocn[1] = y;
		view.xformComponentToNodeCoords(nextLocn);
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	protected Point2D getComponentCoordinates(double x, double y) {
		double[] nextLocn = new double[2];
		nextLocn[0] = x;
		nextLocn[1] = y;
		view.xformNodeToComponentCoords(nextLocn);
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	public void contentChanged() {
		if (view == null) return;
		final ContentChangeListener lis = view.getContentChangeListener();
		if (lis != null)
			lis.contentChanged();
	}

	/**
	 * Adjust the the size to correspond to the aspect ratio of the
	 * current annotation.  This should be overloaded by annotations that
	 * have an aspect ratio (e.g. Shape, Image, etc.)
	 */
	public Dimension adjustAspectRatio(Dimension d) {
		return d;
	}

	public boolean inCanvas(ArbitraryGraphicsCanvas cnvs) {
		for (Component c: cnvs.getComponents()) {
			if (c == this) return true;
		}
		return false;
	}

}
