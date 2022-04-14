package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
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

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
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

public abstract class AbstractAnnotation implements DingAnnotation {
	
	protected final CyAnnotator cyAnnotator;
	private UUID uuid = UUID.randomUUID();
	
	private Set<ArrowAnnotation> arrowList = new HashSet<>();
	protected final boolean usedForPreviews;
	protected DRenderingEngine re;
	protected CanvasID canvas;
	protected GroupAnnotationImpl groupParent;
	protected String name;
	
	// location in node coordinates
	protected double x;
	protected double y;
	protected double width;
	protected double height;
	protected double rotation;
	
	protected int zOrder;
	protected Rectangle2D initialBounds;
	
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
		this.canvas = CanvasID.FOREGROUND;
		this.name = getDefaultName();
	}

	protected AbstractAnnotation(AbstractAnnotation c, boolean usedForPreviews) {
		this(c.re, usedForPreviews);
		this.arrowList = new HashSet<>(c.arrowList);
		this.canvas = c.canvas;
	}

	protected AbstractAnnotation(DRenderingEngine re, double x, double y, double rotation) {
		this(re, false);
		setLocation(x, y);
		this.rotation = rotation;
	}

	protected AbstractAnnotation(DRenderingEngine re, Map<String, String> argMap) {
		this(re, false);

		if (argMap.get(X) != null) {
			try {
				x = Double.parseDouble(argMap.get(X));
			} catch (Exception e) {
				// Ignore...
			}
		}

		if (argMap.get(Y) != null) {
			try {
				y = Double.parseDouble(argMap.get(Y));
			} catch (Exception e) {
				// Ignore...
			}
		}

		if (argMap.get(ROTATION) != null) {
			try {
				rotation = Double.parseDouble(argMap.get(ROTATION));
			} catch (Exception e) {
				// Ignore...
			}
		} else {
			rotation = 0d;
		}

		try {
			zOrder = ViewUtils.getDouble(argMap, Z, 0.0).intValue();
		} catch (Exception e) {
			// Ignore...
		}

		if (argMap.get(NAME) != null)
			name = argMap.get(NAME);

		var canvasString = ViewUtils.getString(argMap, CANVAS, FOREGROUND);

		if (canvasString != null && canvasString.equals(BACKGROUND))
			canvas = CanvasID.BACKGROUND;

		if (argMap.containsKey(ANNOTATION_ID))
			uuid = UUID.fromString(argMap.get(ANNOTATION_ID));
	}

	protected static double getLegacyZoom(Map<String, String> argMap) {
		// Legacy, support for annotations created before 3.8
		@SuppressWarnings("deprecation")
		double zoom = ViewUtils.getDouble(argMap, ZOOM, 1.0);

		if (zoom == 0)
			zoom = 1.0;

		return zoom;
	}
	
	
	
	//------------------------------------------------------------------------

	protected String getDefaultName() {
		if (cyAnnotator == null)
			return "Annotation";
		
		return cyAnnotator.getDefaultAnnotationName(getType().getSimpleName().replace("Annotation", ""));
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return zOrder;
	}

	@Override
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

  @Override
	public void setZ(double z) {
		this.zOrder = (int)z;
	}
	
	@Override
	public double getWidth() {
		return width;
	}
	
	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public double getRotation() {
		return rotation;
	}

	@Override
	public void setRotation(double rotation) {
		if (this.rotation != rotation) {
			var oldValue = this.rotation;
			this.rotation = rotation;
      while (this.rotation < -180d)
        this.rotation += 360d;

      while (this.rotation > 180d)
        this.rotation -= 360d;

			update();
			
			if (isSelected())
				cyAnnotator.getAnnotationSelection().getBounds(); // This forces an update to the bounds
			
			firePropertyChange("rotation", oldValue, rotation);
		}
	}

	public void setBounds(Rectangle2D bounds) {
		this.x = bounds.getX();
		this.y = bounds.getY();
		setSize(bounds.getWidth(), bounds.getHeight());
	}

	@Override
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(x, y, width, height);
	}
	
	@Override
	public Rectangle2D getRotatedBounds() {
		Rectangle2D bounds = getBounds();
		if (getRotation() != 0d) {
			AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(getRotation()),
					bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
			bounds = transform.createTransformedShape(bounds).getBounds2D();
		}
		return bounds;
	}

	@Override
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}
	
	// Save the bounds (in node coordinates)
	@Override
	public void saveBounds() {
		initialBounds = getBounds().getBounds2D();
	}

	@Override
	public Rectangle2D getInitialBounds() {
		return initialBounds;
	}
		
	public void resizeAnnotationRelative(Rectangle2D initialBounds, Rectangle2D outlineBounds) {
		var daBounds = getInitialBounds();
		
		double deltaW = outlineBounds.getWidth()/initialBounds.getWidth();
		double deltaH = outlineBounds.getHeight()/initialBounds.getHeight();
		
		double deltaX = (daBounds.getX()-initialBounds.getX())/initialBounds.getWidth();
		double deltaY = (daBounds.getY()-initialBounds.getY())/initialBounds.getHeight();
		var newBounds = adjustBounds(daBounds, outlineBounds, deltaX, deltaY, deltaW, deltaH);

		setBounds(newBounds);
	}
	
	private static Rectangle2D adjustBounds(Rectangle2D bounds, Rectangle2D outerBounds, double dx, double dy, double dw, double dh) {
		double newX = outerBounds.getX() + dx * outerBounds.getWidth();
		double newY = outerBounds.getY() + dy * outerBounds.getHeight();
		double newWidth  = bounds.getWidth() * dw;
		double newHeight = bounds.getHeight()* dh;
		
		return new Rectangle2D.Double(newX,  newY, newWidth, newHeight);
	}
	
	@Override
	public double getZoom() {
		return 1; // Legacy
	}

	@Override
	public CanvasID getCanvas() {
		return canvas;
	}

	@Override
	public void setCanvas(String name) {
		var canvasID = CanvasID.fromArgName(name);
		changeCanvas(canvasID);
		update(); // Update network attributes
	}

	@Override
	public void changeCanvas(CanvasID canvas) {
		if (!Objects.equals(canvas, this.canvas)) {
			var oldValue = this.canvas;

			this.canvas = canvas;
			firePropertyChange("canvas", oldValue, canvas);
	
			for (var arrow : arrowList) {
				if (arrow instanceof DingAnnotation)
					((DingAnnotation) arrow).changeCanvas(canvas);
			}
		}
	}

	@Override
	public CyNetworkView getNetworkView() {
		return re.getViewModel();
	}
	
	public DRenderingEngine getRenderingEngine() {
		return re;
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public int getZOrder() {
		return zOrder;
	}
    
	@Override
	public void setZOrder(int z) {
		this.zOrder = z;
	}
	
	@Override
	public CyAnnotator getCyAnnotator() {
		return cyAnnotator;
	}

	@Override
	public void setGroupParent(GroupAnnotation parent) {
		if (parent instanceof GroupAnnotationImpl)
			groupParent = (GroupAnnotationImpl) parent;
		else if (parent == null)
			groupParent = null;
		
//		cyAnnotator.addAnnotation(this);
	}

	@Override
	public GroupAnnotation getGroupParent() {
		return groupParent;
	}
    
	// Assumes location is node coordinates.
	@Override
	public void moveAnnotation(Point2D location) {
		if (!(this instanceof ArrowAnnotationImpl))
			setLocation(location.getX(), location.getY());
	}

	@Override
	public void removeAnnotation() {
		cyAnnotator.removeAnnotation(this);

		for (ArrowAnnotation arrow : arrowList) {
			if (arrow instanceof DingAnnotation)
				((DingAnnotation) arrow).removeAnnotation();
		}

		if (groupParent != null)
			groupParent.removeMember(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (!Objects.equals(name, this.name)) {
			var oldValue = this.name;
			this.name = name;
			update();
			firePropertyChange("name", oldValue, name);
		}
	}

	@Override
	public boolean isSelected() {
		return cyAnnotator.getAnnotationSelection().contains(this);
	}

	@Override
	public void setSelected(boolean selected) {
		if (selected != isSelected()) {
			cyAnnotator.setSelectedAnnotation(this, selected);
			update();
			firePropertyChange("selected", !selected, selected);
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
	public Map<String, String> getArgMap() {
		var argMap = new HashMap<String, String>();

		if (name != null)
			argMap.put(NAME, name);

		argMap.put(X, Double.toString(getX()));
		argMap.put(Y, Double.toString(getY()));
		argMap.put(ROTATION, Double.toString(getRotation()));
		argMap.put(CANVAS, canvas.toArgName());
		argMap.put(ANNOTATION_ID, uuid.toString());

		if (groupParent != null)
			argMap.put(PARENT_ID, groupParent.getUUID().toString());

		argMap.put(Z, Integer.toString(getZOrder()));

		return argMap;
	}
	
	@Override
	public boolean isUsedForPreviews() {
		return usedForPreviews;
	}

	@Override
	public void update() {
		contentChanged();
	}

	// Component overrides
	@Override
	public void paint(Graphics g, boolean showSelected) {
		var g2 = (Graphics2D)g;

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

		// We need to control composite ourselves for previews...
		if (!isUsedForPreviews())
			g2.setComposite(AlphaComposite.Src);
	}

	@Override
	public void contentChanged() {
		if (re != null) {
			re.updateView(UpdateType.JUST_ANNOTATIONS, true);
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}
	
	@Override
	public String toString() {
		var args = getArgMap();
		String type = args.get(TYPE);
		
		if (type.endsWith("BoundedTextAnnotation"))
			return "Bounded Text annotation at " + (int) x + "," + (int) y + " named \"" + getName() + "\" with ID: "
					+ getUUID();
		if (type.endsWith("TextAnnotation"))
			return "Text annotation at " + (int) x + "," + (int) y + " named \"" + getName() + "\" with ID: "
					+ getUUID();
		if (type.endsWith("ShapeAnnotation"))
			return "Shape annotation at " + (int) x + "," + (int) y + " named \"" + getName() + "\" with ID: "
					+ getUUID();
		if (type.endsWith("ImageAnnotation"))
			return "Image annotation at " + (int) x + "," + (int) y + " named \"" + getName() + "\" with ID: "
					+ getUUID();
		if (type.endsWith("ArrowAnnotation"))
			return "Arrow annotation named \"" + getName() + "\" with ID: " + getUUID();
		if (type.endsWith("GroupAnnotation"))
			return "Group annotation at " + (int) x + "," + (int) y + " named \"" + getName() + "\" with ID: "
					+ getUUID();

		return "Unknown annotation type";
	}

	
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public int hashCode() {
		final int prime = 41;
		int result = 17;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AbstractAnnotation))
			return false;
		var other = (AbstractAnnotation) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}
}
