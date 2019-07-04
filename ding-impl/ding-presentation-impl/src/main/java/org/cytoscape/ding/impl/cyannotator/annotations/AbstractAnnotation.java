package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingComponent;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

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

public abstract class AbstractAnnotation extends DingComponent implements DingAnnotation {
	
	protected boolean selected;

	private double globalZoom = 1.0;
	private double myZoom = 1.0;

	private DRenderingEngine.Canvas canvasName;
	private UUID uuid = UUID.randomUUID();

	private Set<ArrowAnnotation> arrowList = new HashSet<>();

	protected final boolean usedForPreviews;
	protected DRenderingEngine re;
	protected ArbitraryGraphicsCanvas canvas;
	protected GroupAnnotationImpl parent;
	protected CyAnnotator cyAnnotator;
	protected String name;
	protected Point2D offset; // Offset in node coordinates
	protected Rectangle2D initialBounds;

	protected static final String ID = "id";
	protected static final String TYPE = "type";
	protected static final String ANNOTATION_ID = "uuid";
	protected static final String PARENT_ID = "parent";

	protected Map<String, String> savedArgMap;
	protected double zOrder;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * This constructor is used to create an empty annotation
	 * before adding to a specific view.  In order for this annotation
	 * to be functional, it must be added to the AnnotationManager
	 * and setView must be called.
	 */
	protected AbstractAnnotation(DRenderingEngine re, boolean usedForPreviews) {
		this.re = re;
		this.cyAnnotator = re == null ? null : re.getCyAnnotator();
		this.usedForPreviews = usedForPreviews;
		this.canvas = (ArbitraryGraphicsCanvas)(re.getCanvas(DRenderingEngine.Canvas.FOREGROUND_CANVAS));
		this.canvasName = DRenderingEngine.Canvas.FOREGROUND_CANVAS;
		this.globalZoom = re.getZoom();
		name = getDefaultName();
	}

	protected AbstractAnnotation(AbstractAnnotation c, boolean usedForPreviews) {
		this(c.re, usedForPreviews);
		arrowList = new HashSet<>(c.arrowList);
		this.canvas = c.canvas;
		this.canvasName = c.canvasName;
	}

	protected AbstractAnnotation(DRenderingEngine re, double x, double y, double zoom) {
		this(re, false);
		setLocation((int)x, (int)y);
	}

	protected AbstractAnnotation(DRenderingEngine re, Map<String, String> argMap) {
		this(re, false);

		Point2D coords = ViewUtils.getComponentCoordinates(re, argMap);
		this.globalZoom = ViewUtils.getDouble(argMap, ZOOM, 1.0);
		this.zOrder = ViewUtils.getDouble(argMap, Z, 0.0);
		
		if (argMap.get(NAME) != null)
			name = argMap.get(NAME);
		
		String canvasString = ViewUtils.getString(argMap, CANVAS, FOREGROUND);
		
		if (canvasString != null && canvasString.equals(BACKGROUND)) {
			this.canvas = (ArbitraryGraphicsCanvas)(re.getCanvas(DRenderingEngine.Canvas.BACKGROUND_CANVAS));
			this.canvasName = DRenderingEngine.Canvas.BACKGROUND_CANVAS;
		}

		setLocation((int)coords.getX(), (int)coords.getY());

		if (argMap.containsKey(ANNOTATION_ID))
			this.uuid = UUID.fromString(argMap.get(ANNOTATION_ID));
	}

	//------------------------------------------------------------------------

	protected String getDefaultName() {
		if(cyAnnotator == null)
			return "Annotation";
		return cyAnnotator.getDefaultAnnotationName(getType().getSimpleName().replace("Annotation", ""));
	}
	
//	@Override
//	public String toString() {
//		return getArgMap().get("type")+" annotation "+uuid.toString()+" at "+getX()+", "+getY()+" zoom="+globalZoom+" on canvas "+canvasName;
//	}

	@Override
	public String getCanvasName() {
		if (canvasName.equals(DRenderingEngine.Canvas.BACKGROUND_CANVAS))
			return BACKGROUND;
		return FOREGROUND;
	}

	@Override
	public void setCanvas(String cnvs) {
		canvasName = (cnvs.equals(BACKGROUND)) ? DRenderingEngine.Canvas.BACKGROUND_CANVAS : DRenderingEngine.Canvas.FOREGROUND_CANVAS;
		canvas = (ArbitraryGraphicsCanvas)(re.getCanvas(canvasName));
		for (ArrowAnnotation arrow: arrowList) 
			if (arrow instanceof DingAnnotation)
				((DingAnnotation)arrow).setCanvas(cnvs);

		update();		// Update network attributes
	}

	@Override
	public void changeCanvas(final String cnvs) {
		// Are we really changing anything?
		if ((cnvs.equals(BACKGROUND) && canvasName.equals(DRenderingEngine.Canvas.BACKGROUND_CANVAS)) ||
		    (cnvs.equals(FOREGROUND) && canvasName.equals(DRenderingEngine.Canvas.FOREGROUND_CANVAS)))
			return;

		if (!(this instanceof ArrowAnnotationImpl)) {
			for (ArrowAnnotation arrow: arrowList) {
				if (arrow instanceof DingAnnotation)
					((DingAnnotation)arrow).changeCanvas(cnvs);
			}
		}
		
		canvas.remove(this);	// Remove ourselves from the current canvas
		setCanvas(cnvs);		// Set the new canvas
		canvas.add(this);	// Add ourselves		
	}

	@Override
	public CyNetworkView getNetworkView() {
		return (CyNetworkView)re.getViewModel();
	}

	@Override
	public ArbitraryGraphicsCanvas getCanvas() {
		return canvas;
	}

	public UUID getUUID() {
		return uuid;
	}

	public double getZOrder() {
		return zOrder;
	}

//	public void addComponent(final JComponent cnvs) {
//		ViewUtil.invokeOnEDTAndWait(() -> {
//			if (inCanvas(canvas) && (canvas == cnvs)) {
//				canvas.setComponentZOrder(this, (int)zOrder);
//				return;
//			}
//
//			if (cnvs == null && canvas != null) {
//	
//			} else if (cnvs == null) {
//				setCanvas(FOREGROUND);
//			} else {
//				if (cnvs.equals(re.getCanvas(DRenderingEngine.Canvas.BACKGROUND_CANVAS)))
//					setCanvas(BACKGROUND);
//				else
//					setCanvas(FOREGROUND);
//			}
//			canvas.add(this.getComponent());
//			canvas.setComponentZOrder(this, (int)zOrder);
//		});
//	}
    
	@Override
	public CyAnnotator getCyAnnotator() {return cyAnnotator;}

	@Override
	public void setGroupParent(GroupAnnotation parent) {
		if (parent instanceof GroupAnnotationImpl) {
			this.parent = (GroupAnnotationImpl)parent;
		} else if (parent == null) {
			this.parent = null;
		}
//		cyAnnotator.addAnnotation(this);
	}

	@Override
	public GroupAnnotation getGroupParent() {
		return (GroupAnnotation)parent;
	}
    
	// Assumes location is node coordinates
	@Override
	public void moveAnnotationRelative(Point2D location) {
		if (offset == null) {
			moveAnnotation(location);
			return;
		}

		// Get the relative move
		moveAnnotation(new Point2D.Double(location.getX()-offset.getX(), location.getY()-offset.getY()));
	}

	// Assumes location is node coordinates.
	@Override
	public void moveAnnotation(Point2D location) {
		// Location is in "node coordinates"
		Point2D coords = ViewUtils.getComponentCoordinates(re, location.getX(), location.getY());
		if (!(this instanceof ArrowAnnotationImpl)) {
			setLocation((int)coords.getX(), (int)coords.getY());
		}
	}

	@Override
	public void removeAnnotation() {
		canvas.remove(this);
		cyAnnotator.removeAnnotation(this);
		for (ArrowAnnotation arrow: arrowList) {
			if (arrow instanceof DingAnnotation)
				((DingAnnotation)arrow).removeAnnotation();
		}
		if (parent != null)
			parent.removeMember(this);
	}

	
	public void resizeAnnotationRelative(Rectangle2D initialBounds, Rectangle2D outlineBounds) {
		Rectangle2D daBounds = getInitialBounds();
		
		double deltaW = outlineBounds.getWidth()/initialBounds.getWidth();
		double deltaH = outlineBounds.getHeight()/initialBounds.getHeight();
		
		double deltaX = (daBounds.getX()-initialBounds.getX())/initialBounds.getWidth();
		double deltaY = (daBounds.getY()-initialBounds.getY())/initialBounds.getHeight();
		Rectangle2D newBounds = adjustBounds(daBounds, outlineBounds, deltaX, deltaY, deltaW, deltaH);

		// Now, switch back to component coordinates
		Rectangle2D componentBounds = ViewUtils.getComponentCoordinates(cyAnnotator.getRenderingEngine(), newBounds);
		setLocation((int)componentBounds.getX(), (int)componentBounds.getY());
		resizeAnnotation(componentBounds.getWidth(), componentBounds.getHeight());
	}
	
	
	private static Rectangle2D adjustBounds(Rectangle2D bounds, 
            Rectangle2D outerBounds,
            double dx, double dy, 
            double dw, double dh) {
		double newX = outerBounds.getX() + dx*outerBounds.getWidth();
		double newY = outerBounds.getY() + dy*outerBounds.getHeight();
		double newWidth = bounds.getWidth()*dw;
		double newHeight = bounds.getHeight()*dh;
		return new Rectangle2D.Double(newX,  newY, newWidth, newHeight);
	}
	
	public void resizeAnnotation(double width, double height) {
		// Nothing to do here...
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (!Objects.equals(name, this.name)) {
			this.name = name;
			update();
		}
	}

	@Override
	public double getZoom() {
		return globalZoom;
	}

	@Override
	public void setZoom(double zoom) {
		if (zoom != globalZoom) {
			globalZoom = zoom;
			update();
		}
	}

	@Override
	public double getSpecificZoom() {
		return myZoom;
	}

	@Override
	public void setSpecificZoom(double zoom) {
		if (zoom != myZoom) {
			myZoom = zoom;
			update();
		}
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean selected) {
		setSelected(selected, true);
	}
	
	protected void setSelected(boolean selected, boolean firePropertyChangeEvent) {
		if (selected != this.selected) {
			this.selected = selected;
			cyAnnotator.setSelectedAnnotation(this, selected);
			
			if (firePropertyChangeEvent)
				pcs.firePropertyChange("selected", !selected, selected);
		}
	}

	@Override
	public void addArrow(ArrowAnnotation arrow) {
		arrowList.add(arrow);
		update();
	}

	@Override
	public void removeArrow(ArrowAnnotation arrow) {
		arrowList.remove(arrow);
		update();
	}

	@Override
	public Set<ArrowAnnotation> getArrows() {
		return arrowList;
	}

	@Override
	public Map<String,String> getArgMap() {
		Map<String, String> argMap = new HashMap<>();
		if (name != null)
			argMap.put(NAME, this.name);
		ViewUtils.addNodeCoordinates(re, argMap, getX(), getY());
		argMap.put(ZOOM,Double.toString(this.globalZoom));
		if (canvasName.equals(DRenderingEngine.Canvas.BACKGROUND_CANVAS))
			argMap.put(CANVAS, BACKGROUND);
		else
			argMap.put(CANVAS, FOREGROUND);
		argMap.put(ANNOTATION_ID, this.uuid.toString());

		if (parent != null)
			argMap.put(PARENT_ID, parent.getUUID().toString());

		int zOrder = canvas.getZOrder(this);
		argMap.put(Z, Integer.toString(zOrder));

		return argMap;
	}
	
	@Override
	public boolean isUsedForPreviews() {
		return usedForPreviews;
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
	}

	@Override
	public void update() {
		updateAnnotationAttributes();
//		getCanvas().repaint();
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


		if (!isUsedForPreviews()) {
			// We need to control composite ourselves for previews...
			g2.setComposite(AlphaComposite.Src);
		}
	}

	@Override
	public JDialog getModifyDialog() {
		return null;
	}

	// Protected methods
	protected void updateAnnotationAttributes() {
		if (!usedForPreviews) {
//			cyAnnotator.addAnnotation(this);
			contentChanged();
		}
	}

	// Save the bounds (in node coordinates)
	@Override
	public void saveBounds() {
		initialBounds = ViewUtils.getNodeCoordinates(re, getBounds().getBounds2D());
	}

	@Override
	public Rectangle2D getInitialBounds() {
		return initialBounds;
	}

	// Save the offset in node coordinates
	@Override
	public void setOffset(Point2D offset) {
		if (offset == null) {
			this.offset = null;
			return;
		}

		Point2D mouse   = ViewUtils.getNodeCoordinates(re, offset.getX(), offset.getY());
		Point2D current = ViewUtils.getNodeCoordinates(re, getX(), getY());

		this.offset = new Point2D.Double(mouse.getX()-current.getX(), mouse.getY()-current.getY());
	}

	@Override
	public Point2D getOffset() {
		return offset;
	}

	@Override
	public void contentChanged() {
		if (re != null)
			re.fireContentChanged();
	}

	/**
	 * Adjust the the size to correspond to the aspect ratio of the
	 * current annotation.  This should be overloaded by annotations that
	 * have an aspect ratio (e.g. Shape, Image, etc.)
	 */
	public Dimension adjustAspectRatio(Dimension d) {
		return d;
	}


	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}
}
