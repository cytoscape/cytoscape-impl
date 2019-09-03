package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.property.values.Position;

public class AnnotationSelection2 implements Iterable<DingAnnotation> {
	
	private static final int border = 2;
	private static final float[] dash = { 10.0f, 10.0f };
	
	private final DRenderingEngine re;
	private final CyAnnotator cyAnnotator;
	private final Set<DingAnnotation> selectedAnnotations = new HashSet<>();
	
	// node coordinates
	private Rectangle2D union; 
	private Rectangle2D savedUnion;
	private Map<DingAnnotation,Rectangle2D> savedBoundsMap;
	
	// Everything below in image coordinates
	private final Map<Position,Rectangle> anchors = new EnumMap<>(Position.class);
	private Point movingMouseOffset;
	private AnchorLocation resizingAnchor;
	
	
	public AnnotationSelection2(CyAnnotator cyAnnotator) {
		this.cyAnnotator = cyAnnotator;
		this.re = cyAnnotator.getRenderingEngine();
	}
	
	
	public void add(DingAnnotation a) {
		selectedAnnotations.add(a);
		updateBounds();
	}
	
	public void remove(Annotation a) {
		selectedAnnotations.remove(a);
		updateBounds();
	}
	
	public void clear() {
		selectedAnnotations.clear();
		updateBounds();
	}

	@Override
	public Iterator<DingAnnotation> iterator() {
		return selectedAnnotations.iterator();
	}
	
	public boolean isEmpty() {
		return selectedAnnotations.isEmpty();
	}
	
	public int count() {
		return selectedAnnotations.size();
	}
	
	public Collection<DingAnnotation> getSelectedAnnotations() {
		// This method exists for clients that want to avoid ConcurrentModificationException
		return new ArrayList<>(selectedAnnotations);
	}
	
	private void saveBounds() {
		savedUnion = union;
		savedBoundsMap = new HashMap<>();
		for(var a : this) {
			savedBoundsMap.put(a, a.getBounds());
		}
	}

	private void updateBounds() {
		union = null;
		for(var a : this) {
			var bounds = a.getBounds();
			union = (union == null) ? bounds : union.createUnion(bounds);
		}
	}
	
	public AnchorLocation overAnchor(int mouseX, int mouseY) {
		for(Position p : Position.values()) {
			Rectangle rect = anchors.get(p);
			if(rect != null && rect.contains(mouseX, mouseY)) {
				int mouseOffsetX = mouseX - rect.x;
				int mouseOffsetY = mouseY - rect.y;
				return new AnchorLocation(p, rect.x, rect.y, mouseOffsetX, mouseOffsetY);
			}
		}
		return null;
	}

	public void startResizing(AnchorLocation resizingAnchor) {
		this.resizingAnchor = resizingAnchor;
		saveBounds();
	}
	
	public void stopResizing() {
		this.resizingAnchor = null;
	}
	
	public boolean isResizing() {
		return resizingAnchor != null;
	}
	
	public void resizeAnnotationsRelative(int mouseX, int mouseY) {
		// compensate for the difference between the anchor location and the mouse location
		if(resizingAnchor.isNorth())
			mouseY += border*4 - resizingAnchor.getMouseOffsetY();
		if(resizingAnchor.isSouth())
			mouseY -= resizingAnchor.getMouseOffsetY();
		if(resizingAnchor.isWest())
			mouseX += border*4 - resizingAnchor.getMouseOffsetX();
		if(resizingAnchor.isEast())
			mouseX -= resizingAnchor.getMouseOffsetX();
		
		Point2D node = re.getTransform().getNodeCoordinates(mouseX, mouseY);
		Rectangle2D newOutlineBounds = resize(resizingAnchor, savedUnion, node.getX(), node.getY());

		for(var a : this) {
			Rectangle2D daInitialBounds = savedBoundsMap.get(a);
			resizeAnnotationRelative((AbstractAnnotation)a, daInitialBounds, savedUnion, newOutlineBounds);
			a.update();
		}
		updateBounds();
	}
	
	private static void resizeAnnotationRelative(AbstractAnnotation da, Rectangle2D daBounds, Rectangle2D initialBounds, Rectangle2D outlineBounds) {
		double deltaW = outlineBounds.getWidth()  / initialBounds.getWidth();
		double deltaH = outlineBounds.getHeight() / initialBounds.getHeight();
		
		double deltaX = (daBounds.getX() - initialBounds.getX()) / initialBounds.getWidth();
		double deltaY = (daBounds.getY() - initialBounds.getY()) / initialBounds.getHeight();
		Rectangle2D newBounds = adjustBounds(daBounds, outlineBounds, deltaX, deltaY, deltaW, deltaH);
		da.setBounds(newBounds);
	}
	
	private static Rectangle2D adjustBounds(Rectangle2D bounds, Rectangle2D outerBounds, double dx, double dy, double dw, double dh) {
		double newX = outerBounds.getX() + dx*outerBounds.getWidth();
		double newY = outerBounds.getY() + dy*outerBounds.getHeight();
		double newWidth = bounds.getWidth()*dw;
		double newHeight = bounds.getHeight()*dh;
		return new Rectangle2D.Double(newX,  newY, newWidth, newHeight);
	}
	
	public static Rectangle2D resize(AnchorLocation anchor, Rectangle2D bounds, double mouseX, double mouseY) {
		final double boundsX = bounds.getX();
		final double boundsY = bounds.getY();
		final double boundsWidth  = bounds.getWidth();
		final double boundsHeight = bounds.getHeight();
		final double boundsYBottom = boundsY + boundsHeight;
		final double boundsXLeft   = boundsX + boundsWidth;

		double x = boundsX;
		double y = boundsY;
		double width = boundsWidth;
		double height = boundsHeight;
		
		// y and height
		if(anchor.isNorth()) {
			if(mouseY > boundsYBottom) {
				y = boundsYBottom;
				height = mouseY - boundsYBottom;
			} else {
				y = mouseY;
				height = boundsYBottom - mouseY;
			}
		} else if(anchor.isSouth()) {
			if(mouseY < boundsY) {
				y = mouseY;
				height = boundsY - mouseY;
			} else {
				height = mouseY - boundsY;
			}
		}
		
		// x and width
		if(anchor.isWest()) {
			if(mouseX > boundsXLeft) {
				x = boundsXLeft;
				width = mouseX - boundsXLeft;
			} else {
				x = mouseX;
				width = boundsXLeft - mouseX;
			}
		} else if(anchor.isEast()) {
			if(mouseX < boundsX) {
				x = mouseX;
				width = boundsX - mouseX;
			} else {
				width = mouseX - boundsX; 
			}
		}
		
		return new Rectangle2D.Double(x, y, width, height);
	}

	
	
	public void setMouseOffset(Point offset) {
		this.movingMouseOffset = offset;
	}
	
	/**
	 * Assumes x and y are component (mouse) coordinates
	 */
	public void moveSelection(int x, int y) {
		// Avoid moving the same annotation twice
		Set<DingAnnotation> annotationsToMove = new HashSet<>(selectedAnnotations);
		for(DingAnnotation annotation : selectedAnnotations) {
			for(DingAnnotation ancestor : AnnotationTree.getAncestors(annotation)) {
				if(selectedAnnotations.contains(ancestor)) {
					annotationsToMove.remove(annotation);
					break;
				}
			}
		}

		NetworkTransform transform = cyAnnotator.getRenderingEngine().getTransform();
		Point2D nodePt   = transform.getNodeCoordinates(x, y);
		Point2D offsetPt = transform.getNodeCoordinates(movingMouseOffset);
		
		double dx = nodePt.getX() - offsetPt.getX();
		double dy = nodePt.getY() - offsetPt.getY();
		
		for(var a : annotationsToMove) {
			a.setLocation(a.getX() + dx, a.getY() + dy);
		}
		updateBounds();
	}
	
	public void stopMoving() {
		this.movingMouseOffset = null;
	}
	
	
	public void paint(Graphics2D g) {
		if(isEmpty())
			return;
		
		/* Set up all of our anti-aliasing, etc. here to avoid doing it redundantly */
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		// High quality color rendering is ON.
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		// Text antialiasing is ON.
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		Composite originalComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

		g.setPaint(Color.YELLOW);
		g.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f));

		// Draw the bounding box
		Rectangle shape = getShapeImageCoords();
		g.draw(shape);

		// Draw anchors
		updateAnchors(shape);
		if(annotationSelectionEnabled()) {
			g.setPaint(Color.GRAY);
			anchors.values().forEach(g::fill);
		}
		
		g.setComposite(originalComposite);
	}
	
	
	private Rectangle getShapeImageCoords() {
		var imageUnion = re.getTransform().getImageCoordinates(union);
		return new Rectangle(imageUnion.x - border, imageUnion.y - border, imageUnion.width+border*2, imageUnion.height+border*2);
	}
	
	
	private void updateAnchors(Rectangle shape) { // shape in image coords
		final int s = border*4;
		anchors.clear();
		anchors.put(Position.NORTH_WEST, new Rectangle(0,                 0,                  s, s));
		anchors.put(Position.NORTH,      new Rectangle(shape.width/2+s/2, 0,                  s, s));
		anchors.put(Position.NORTH_EAST, new Rectangle(shape.width+s,     0,                  s, s));
		anchors.put(Position.WEST,       new Rectangle(0,                 shape.height/2+s/2, s, s));
		anchors.put(Position.EAST,       new Rectangle(shape.width+s,     shape.height/2+s/2, s, s));
		anchors.put(Position.SOUTH_WEST, new Rectangle(0,                 shape.height+s,     s, s));
		anchors.put(Position.SOUTH,      new Rectangle(shape.width/2+s/2, shape.height+s,     s, s));
		anchors.put(Position.SOUTH_EAST, new Rectangle(shape.width+s,     shape.height+s,     s, s));
		
		anchors.values().forEach(r -> r.translate(shape.x-s, shape.y-s));
	}       
	
	
	private boolean annotationSelectionEnabled() {
		return re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION);
	}
}
