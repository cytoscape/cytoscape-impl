package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.IllegalAnnotationStructureException;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
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

@SuppressWarnings("serial")
public class GroupAnnotationImpl extends AbstractAnnotation implements GroupAnnotation {

	List<DingAnnotation> annotations = new ArrayList<>();


	public GroupAnnotationImpl(DRenderingEngine re, Map<String, String> argMap) {
		super(re, processArgs(argMap));
	}
	
	private static Map<String,String> processArgs(Map<String,String> argMap) {
		argMap.remove(CANVAS);
		return argMap;
	}

	@Override
	public Class<? extends Annotation> getType() {
		return GroupAnnotation.class;
	}

	
	@Override
	public void setCanvas(String cnvs) {
		// do nothing, must be on the foreground canvas
	}
	
	
	@Override
	public void addMember(Annotation member) {
		if (member instanceof DingAnnotation) {
			DingAnnotation dMember = (DingAnnotation)member;
			
			if(dMember.getGroupParent() != null && dMember.getGroupParent() != this) {
				throw new IllegalAnnotationStructureException("Annotation is already a member of another group.");
			}
			
			if(!annotations.contains(dMember)) {
				annotations.add(dMember);
				dMember.setGroupParent(this);
				try {
					getCyAnnotator().checkCycle();
				} catch(IllegalAnnotationStructureException e) {
					annotations.remove(dMember);
					dMember.setGroupParent(null);
					throw e;
				}
			}
			
			// We muck with the ZOrder directly, so we need to make sure we're on the EDT
//			ViewUtil.invokeOnEDTAndWait(() -> {
//				// First, we need to make sure that this annotation is already registered and added to the canvas
//				if (dMember.getCanvas() != null) {
//					dMember.addComponent(dMember.getCanvas());
//				} else {
//					dMember.addComponent(cyAnnotator.getForeGroundCanvas());
//				}

//				dMember.update();

//				if (!annotations.contains(dMember))
//					annotations.add(dMember);

//				dMember.setGroupParent(this);

				// Set the bounding box and our location
				updateBounds();
				
				Rectangle2D bounds = getBounds();
				setLocation((int)bounds.getX(), (int)bounds.getY());
				setSize((int)bounds.getWidth(), (int)bounds.getHeight());
				
//					dMember.getCanvas().setComponentZOrder(dMember.getComponent(), (int)((AbstractAnnotation)dMember).getZOrder());
//					// Now, update our Z-order
//					int z = dMember.getCanvas().getComponentZOrder(dMember.getComponent());
//					// System.out.println("Canvas = "+dMember.getCanvas());
//					// System.out.println("Component = "+((JComponent)dMember.getComponent()).toString());
//					if (z > getCanvas().getComponentZOrder(getComponent())) {
//						// Not sure why, but this gives me an error: 
//						//  java.lang.IllegalArgumentException: component and container should be in the same top-level window
//						// getCanvas().setComponentZOrder(getComponent(), z);
//					}
//			});
		}
	}

	@Override
	public List<Annotation> getMembers() {
		return new ArrayList<>(annotations);
	}

	@Override
	public void removeMember(Annotation member) {
		if (member instanceof DingAnnotation) {
			DingAnnotation dMember = (DingAnnotation)member;
			if (annotations != null && annotations.contains(dMember)) {
				annotations.remove(dMember);
				dMember.setGroupParent(null);
			}
		}
	}

	@Override
	public void removeAnnotation() {
		// Remove all of our children
		for (DingAnnotation a: annotations) {
			canvas.remove(a);
			cyAnnotator.removeAnnotation(a);
		}
		
		annotations.clear();

		// Now remove ourselves
		canvas.remove(this);
		cyAnnotator.removeAnnotation(this);
		
		if (parent != null)
			parent.removeMember(this);
	}
	
	@Override
	public Map<String, String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,GroupAnnotation.class.getName());
		String members = "";

		if (annotations == null || annotations.size() == 0)
			return argMap;

		for (DingAnnotation annotation: annotations) {
			members+= annotation.getUUID().toString()+",";
		}
		if (members != null && members.length() > 1)
			argMap.put(MEMBERS, members.substring(0, members.length()-1));

		return argMap;
	}

	@Override
	public JDialog getModifyDialog() {
		// return new GroupAnnotationDialog(this);
		return null;
	}

	@Override
	public void moveAnnotation(Point2D location) {
		// Location is in "node coordinates"
		// Get the component coordinates of our new location
		Point2D compLocation = ViewUtils.getComponentCoordinates(re, location.getX(), location.getY());

		// Get our current location in component coordinates
		Point currentLocation = getLocation();
		double currentX = currentLocation.getX();
		double currentY = currentLocation.getY();

		// Calculate the delta
		double deltaX = currentX - compLocation.getX();
		double deltaY = currentY - compLocation.getY();

		for (DingAnnotation child: annotations) {
			// Move each child to it's new location
			Point childLocation = child.getLocation();

			Point2D moveTo = ViewUtils.getNodeCoordinates(re, Math.round(childLocation.getX()-deltaX), Math.round(childLocation.getY()-deltaY));
			((AbstractAnnotation)child).moveAnnotation(moveTo);
		}

		// Set our new location
		setLocation((int)compLocation.getX(), (int)compLocation.getY());

		updateBounds();
	}
	
	@Override
	public void resizeAnnotationRelative(Rectangle2D initialBounds, Rectangle2D outlineBounds) {
		for(DingAnnotation da : annotations) {
			((AbstractAnnotation)da).resizeAnnotationRelative(initialBounds, outlineBounds);
		}
		updateBounds();
	}
	
	@Override
	public void saveBounds() {
		super.saveBounds();
		for(DingAnnotation da : annotations) {
			((AbstractAnnotation)da).saveBounds();
		}
	}
	
	/*
	 * 1) update our bounds
	 * 2) move each child
	 * 3) reset the size of each child
	 */
	public void setSize(Dimension d) {
		// Get our width
		double width = getWidth();
		double height = getHeight();
		double dx = d.getWidth() / width;
		double dy = d.getHeight() / height;
		double x = getX();
		double y = getY();

		/*
		System.out.println("Changing size of group from: "+width+"x"+height+" to "+d.getWidth()+"x"+d.getHeight());
		System.out.println("dx = "+dx+", dy = "+dy);
		System.out.println("x = "+x+", y = "+y);
		*/

		ViewUtil.invokeOnEDTAndWait(() -> {

			// Now and move each of our children
			for (DingAnnotation child: annotations) {
				double childX = child.getX();
				double childY = child.getY();
				double childWidth = child.getWidth();
				double childHeight = child.getHeight();
				double newX = (childX-x)*dx + x;
				double newY = (childY-y)*dy + y;
				double newWidth = childWidth*dx;
				double newHeight = childHeight*dy;
				child.setLocation((int)Math.round(newX), (int)Math.round(newY));

				if (child instanceof TextAnnotationImpl) {
					TextAnnotation textChild = (TextAnnotation)child;
					double fontSize = textChild.getFontSize();
					textChild.setFontSize(fontSize*dx);
				}

				if (child instanceof BoundedTextAnnotationImpl) {
					BoundedTextAnnotationImpl textChild = (BoundedTextAnnotationImpl)child;
					double fontSize = textChild.getFontSize();
					textChild.setFontSize(fontSize*dx, false);
				}

				if (child instanceof ShapeAnnotationImpl) {
					ShapeAnnotationImpl shapeChild = (ShapeAnnotationImpl)child;
					double borderWidth = shapeChild.getBorderWidth();
					double zoom = shapeChild.getZoom();
					newWidth = newWidth - borderWidth*2*zoom;
					newHeight = newHeight - borderWidth*2*zoom;
					shapeChild.setSize(newWidth, newHeight);
				}
				child.update();
			}
			super.setSize((int)d.getWidth(), (int)d.getHeight());
		});
	}

	@Override
	public Dimension adjustAspectRatio(Dimension d) {
		double ratio = d.getWidth() / d.getHeight();
		Rectangle2D bounds = getBounds();
		double aspectRatio = bounds.getWidth() / bounds.getHeight();
		double width, height;

		if (aspectRatio >= ratio) {
			width = d.getWidth();
			height = width / aspectRatio;
		} else {
			height = d.getHeight();
			width = height * aspectRatio;
		}

		d.setSize(width, height);

		return d;
	}

	@Override
	public void changeCanvas(CanvasID canvasId) {
		for (DingAnnotation ann: annotations) {
			ann.changeCanvas(canvasId);
		}
		super.changeCanvas(canvasId);
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);
		// We don't do anything ourselves since each of our
		// children is a component
		// Make sure to update our bounds
		updateBounds();
	}

	final static float dash1[] = {10.0f};

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		// MKTODO
//		if (!canvas.isPrinting())
			updateBounds();
		
		Graphics2D g2 = (Graphics2D) g;
		
		if (isSelected() /* && !canvas.isPrinting() */) {
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
			g2.drawRect(0, 0, getWidth(), getHeight());
		}
		/*
		else {
			g2.setColor(Color.BLUE);
			g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
			g2.drawRect(-1, -1, (int)bounds.getWidth()+2, (int)bounds.getHeight()+2);
		}
		*/
	}

	@Override
	public void setSpecificZoom(double newZoom) {
		if (newZoom == getSpecificZoom())
			return;
		
		if (annotations != null && annotations.size() > 0) {
			for (DingAnnotation child: annotations)
				child.setSpecificZoom(newZoom);
		}
		
		super.setSpecificZoom(newZoom);		
	}

	@Override
	public void setZoom(double newZoom) {
		if (newZoom == getZoom())
			return;
		
		if (annotations != null && annotations.size() > 0) {
			for (DingAnnotation child: annotations)
				child.setZoom(newZoom);
		}
		
		super.setZoom(newZoom);		
	}

	private void updateBounds() {
		Rectangle2D union = null;

		for (DingAnnotation child : annotations) {
			if (union == null)
				union = child.getBounds().getBounds2D();
			else
				union = union.createUnion(child.getBounds().getBounds2D());
		}

		if(union != null) {
			setBounds(union.getBounds());
		}
	}

}
