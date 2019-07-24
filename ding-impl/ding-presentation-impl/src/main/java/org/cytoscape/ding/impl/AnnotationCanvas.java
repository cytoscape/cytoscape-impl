package org.cytoscape.ding.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class AnnotationCanvas extends DingCanvas {
	
	private final DingAnnotation.CanvasID canvasID;
	
	private DRenderingEngine re;
//	private Map<DingAnnotation,Point> annotationToPointMap = new HashMap<>();
//	private List<DingAnnotation> annotations = new LinkedList<>();
	
	private CompositeCanvas parent;
	/*
 	 * Flag to record that we're printing since we don't use the PrinterGraphics interface
 	 */
//	private boolean isPrinting;
	private boolean dirty = true;
	


	public AnnotationCanvas(CompositeCanvas parent, DingAnnotation.CanvasID canvasID, DRenderingEngine re) {
		this.parent = parent;
		this.re = re;
		this.canvasID = canvasID;
	}

	public DingAnnotation.CanvasID getCanvasID() {
		return canvasID;
	}
//	
//	/**
//	 * Our implementation of add
//	 */
//	private void addInternal(DingAnnotation annotation) {
//		// Make sure to position the component
//		final double[] nodeCanvasCoordinates = new double[2];
//		nodeCanvasCoordinates[0] = annotation.getX();
//		nodeCanvasCoordinates[1] = annotation.getY();
//
//		image.xformImageToNodeCoords(nodeCanvasCoordinates);
//
//		Point nodePos = new Point( (int)nodeCanvasCoordinates[0], (int)nodeCanvasCoordinates[1]);
//
//		annotations.add(annotation);
//		annotationToPointMap.put(annotation, nodePos);
//	}
//    
//	public List<DingAnnotation> getAnnotations() {
//		return Collections.unmodifiableList(annotations);
//	}
//	
//	public void setZOrder(DingAnnotation annotation, int z) {
//		if(annotations.remove(annotation)) {
//			annotations.add(z, annotation);
//		}
//	}
//	
//	public int getZOrder(DingAnnotation annotation) {
//		return annotations.indexOf(annotation);
//	}
//	
//	public void add(DingAnnotation component) {
//    	addInternal(component);
//		contentChanged();
//	}
//	
//	/**
//	 * Our implementation of add
//	 */
//	public void addAnnotations(Collection<DingAnnotation> annotations) {
//		annotations.forEach(this::addInternal);
//		// call contentChanged ONCE!!!
//		contentChanged();
//	}
//
//	private void removeInternal(DingAnnotation annotation) {
//		annotations.remove(annotation);
////		annotationToPointMap.remove(annotation);
//	}
//        
//	public void remove(DingAnnotation annotation) {
//		removeInternal(annotation);
//		contentChanged();
//	}
//	
//	public void removeAnnotations(Collection<DingAnnotation> annotations) {
//		annotations.forEach(this::removeInternal);
//		// call contentChanged ONCE!!!
//		contentChanged();
//	}
//	
//	
//	@Override
//	public void setCenter(double x, double y) {
//		super.setCenter(x, y);
//		setBoundsChildren();
//	}
//	
//	@Override
//	public void setScaleFactor(double scaleFactor) {
//		super.setScaleFactor(scaleFactor);
//		setBoundsChildren();
//	}
//	
//	@Override
//	public void setViewport(int width, int height) {
//		super.setViewport(width, height);
//		setBoundsChildren();
//	}
//	
//	
//	public void modifyComponentLocation(int x, int y, DingAnnotation component){
//		final Point nodePos = annotationToPointMap.get(component);
//		if (nodePos == null) return;
//
//		final double[] nodeCanvasCoordinates = new double[2];
//		nodeCanvasCoordinates[0] = x;
//		nodeCanvasCoordinates[1] = y;
//
//		image.xformImageToNodeCoords(nodeCanvasCoordinates);
//
//		nodePos.x = (int)nodeCanvasCoordinates[0];
//		nodePos.y = (int)nodeCanvasCoordinates[1];
//
//		contentChanged();
//	}

	
//	@Override
//	public void setViewport(int width, int height) {
//		// our bounds have changed, create a new image with new size
//		if (width > 1 && height > 1) {
//			// create the buffered image
//			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//			setBoundsChildren();
//		}
//	}

//	private int getWidth() {
//		return img.getWidth(null);
//	}
//	
//	private int getHeight() {
//		return img.getHeight(null);
//	}
	
	/**
	 * An implementation of getBounds that adjusts the bounds to
	 * include all of the child elements
	 */
	public boolean adjustBoundsToIncludeAnnotations(double[] currentBounds) {
		List<DingAnnotation> annotations = re.getCyAnnotator().getAnnotations(canvasID);
		if(annotations.isEmpty())
			return false;

		// The currentBounds represents the current extents.  We're
		// going to walk our children and make sure the extents
		// are large enought to cover them

//		final double[] nodeCanvasCoordinates = new double[2];
		
		// iterate through the components
		for (DingAnnotation a : annotations) {
			// get position of this component in network coordinates
//			Point position = annotationToPointMap.get(c);
//			if (position == null) continue;			// AST

			// Adjust, if necessary
			if (a.getX() < currentBounds[0]) 
				currentBounds[0] = a.getX();
			if (a.getY() < currentBounds[1]) 
				currentBounds[1] = a.getY();
			
			// Now, get the maximum extent of the component in component coordinates
			double x2 = a.getX() + a.getWidth();
			double y2 = a.getY() + a.getHeight();

			// Transform the maximum extent to get network cooredinates
//			image.xformImageToNodeCoords(nodeCanvasCoordinates);

			// Adjust, if necessary
			if (x2 > currentBounds[2])
				currentBounds[2] = x2;
			if (y2 > currentBounds[3])
				currentBounds[3] = y2;
		}

		return true;
	}

//	public void drawCanvas(VolatileImage image, double xMin, double yMin, double xCenter, double yCenter, double scaleFactor) {
//		// get image graphics
//		final Graphics2D image2D = image.createGraphics();
//		
////		if (isOpaque())
////			clearImage(image2D);
//
//		double xOffset = (image.getWidth()  / 2) / scaleFactor - xCenter;
//		double yOffset = (image.getHeight() / 2) / scaleFactor - yCenter;
//
//		// get list of child components
////		zSort(annotations); // Since we're doing this because we're doing the draw on our own
//
//		// no components, outta here
//		if (annotations.isEmpty())
//			return;
//
//		// iterate through the components
//		for (DingAnnotation c : annotations) {
//			// get position of this component in network coordinates
//			Point position = annotationToPointMap.get(c);
//			if (position == null) 
//				continue;
//			
//			// If we're painting an annotation, set the zoom
//			if (c instanceof DingAnnotation) {
//				DingAnnotation a = (DingAnnotation)c;
//				a.drawAnnotation(image2D, position.getX()+xOffset, position.getY()+yOffset, scaleFactor);
//			}
//		}
//	}

	@Override
	public Image paintImage(RenderDetailFlags flags) {
		// only paint if we have an image to paint on
		// get image graphics
		image.clear();
		Graphics2D g = image.getGraphics();
		g.setTransform(image.getAffineTransform());
		
		Rectangle2D.Float visibleArea = image.getNetworkVisibleAreaInNodeCoords();
		List<DingAnnotation> annotations = re.getCyAnnotator().getAnnotations(canvasID);
		
		for (DingAnnotation a : annotations) {
			if(visibleArea.intersects(a.getBounds())) {
				a.paint(g);
			}
		}
		
		g.dispose();
		dirty = false;
		return image.getImage();
	}
	
//	private void setBoundsChildren() {
//		for (DingAnnotation a : annotations) {
//			Point node = annotationToPointMap.get(a);
//			if (node == null) 
//				continue;
//			
//			double[] currentNodeCoordinates = {node.getX(), node.getY()};
//			AffineTransform transform = image.getAffineTransform();
//			// Node to Image
//			transform.transform(currentNodeCoordinates, 0, currentNodeCoordinates, 0, 1);
//
//			a.setBounds((int) currentNodeCoordinates[0], (int) currentNodeCoordinates[1], a.getWidth(), a.getHeight());
//			a.setZoom(parent.getScaleFactor());
//		}
//	}


	private void contentChanged() {
		dirty = true;
	}

//	// Sort the components by z order
//	private void zSort(Component[] components) {
//		Arrays.sort(components, new ZComparator());
//	}
//
//	class ZComparator implements Comparator<Component> {
//		
//		@Override
//		public int compare(Component o1, Component o2) {
//			if (getComponentZOrder(o1) > getComponentZOrder(o2))
//				return -1;
//			else if (getComponentZOrder(o1) < getComponentZOrder(o2))
//				return 1;
//			else
//				return 0;
//		}
//
//		public boolean equals(Component o1, Component o2) {
//			return (getComponentZOrder(o1) == getComponentZOrder(o2));
//		}
//	}

	public void dispose() {
		// Bug #1178: This class is being leaked by Swing's focus subsystem
		// In order to ensure no other instances get strung along, we should
		// release them here.
		re = null;
//		networkCanvas = null;
//		annotations = null;
	}
}
