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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class GroupAnnotationImpl extends AbstractAnnotation implements GroupAnnotation {

	List<DingAnnotation> annotations = null;
	Rectangle2D bounds = null;

	private static final Logger logger = LoggerFactory.getLogger(GroupAnnotationImpl.class);

	public GroupAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, Window owner) { 
		super(cyAnnotator, view, owner); 
	}

	public GroupAnnotationImpl(GroupAnnotationImpl c, Window owner) { 
		super(c, owner);
	}

	public GroupAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, double x, double y, 
	                           List<Annotation> annotations, double zoom, Window owner) {
		super(cyAnnotator, view, owner);
		this.annotations  = new ArrayList<>();
		for (Annotation a: annotations) {
			if (a instanceof DingAnnotation)
				this.annotations.add((DingAnnotation)a);
		}
	}

	public GroupAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, 
	                           Map<String, String> argMap, Window owner) {
		super(cyAnnotator, view, argMap, owner);

		// Get the UUIDs of all of the annotations
		if (argMap.containsKey(MEMBERS)) {
			String[] members = argMap.get(MEMBERS).split(",");
			for (String uuid: members) {
				// Create the uuid
				UUID u = UUID.fromString(uuid);

				// See if this annotation already exists
				DingAnnotation a = cyAnnotator.getAnnotation(u);
				if (a != null) {
					// Yup, add it in to our list
					if (annotations == null) 
						annotations = new ArrayList<>();
					annotations.add(a);
				}
			}
		}
	}

	@Override
	public void addMember(final Annotation member) {
		// We muck with the ZOrder directly, so we need
		// to make sure we're on the EDT
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
					addMember(member);
				}
			});
			return;
		}

		if (member instanceof DingAnnotation) {
			if (annotations == null) annotations = new ArrayList<>();
			DingAnnotation dMember = (DingAnnotation)member;
			if (!annotations.contains(dMember))
				annotations.add(dMember);
			dMember.setGroupParent(this);
			// Set the bounding box and our location
			updateBounds();
			setLocation((int)bounds.getX(), (int)bounds.getY());
			setSize((int)bounds.getWidth(), (int)bounds.getHeight());
			// Now, update our Z-order
			int z = dMember.getCanvas().getComponentZOrder(dMember.getComponent());
			if (z > getCanvas().getComponentZOrder(getComponent())) {
				getCanvas().setComponentZOrder(getComponent(), z);
			}
			cyAnnotator.addAnnotation(this); // This forces an update of the argMap
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
			cyAnnotator.addAnnotation(this); // This forces an update of the argMap
		}
	}

	public Map<String,String> getArgMap() {
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

	public JDialog getModifyDialog() {
			// return new GroupAnnotationDialog(this);
			return null;
	}

	@Override
	public void moveAnnotation(Point2D location) {
		// Get our current location
		Point currentLocation = getLocation();
		double currentX = currentLocation.getX();
		double currentY = currentLocation.getY();

		// Calculate the delta
		double deltaX = currentX - location.getX();
		double deltaY = currentY - location.getY();

		for (DingAnnotation child: annotations) {
			// Move each child to it's new location
			Point childLocation = child.getLocation();
			Point2D newLocation = new Point2D.Double(childLocation.getX()-deltaX, childLocation.getY()-deltaY);
			child.moveAnnotation(newLocation);
		}

		// Set our new location
		setLocation((int)location.getX(), (int)location.getY());
		cyAnnotator.moveAnnotation(this);
	}

/*
	@Override
	public void setSelected(boolean selected) {
		// for (DingAnnotation child: annotations) {
		// 	child.setSelected(selected);
		// }
		super.setSelected(selected);
	}
*/

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);
		// We don't do anything ourselves since each of our
		// children is a component
	}

	final static float dash1[] = {10.0f};

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2=(Graphics2D)g;
		if(isSelected()) {
			updateBounds();
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
			g2.drawRect(0, 0, (int)bounds.getWidth(), (int)bounds.getHeight());
		}

	}

	@Override
	public void setSpecificZoom(double newZoom) {
		if (annotations != null && annotations.size() > 0) {
			for (DingAnnotation child: annotations)
				child.setSpecificZoom(newZoom);
		}
		super.setSpecificZoom(newZoom);		
	}

	@Override
	public void setZoom(double newZoom) {
		if (annotations != null && annotations.size() > 0) {
			for (DingAnnotation child: annotations)
				child.setZoom(newZoom);
		}
		super.setZoom(newZoom);		
	}

	public Rectangle2D getBounds2D() {
		return bounds;
	}

	private void updateBounds() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() { updateBounds(); }
			});
			return;
		}
		// Calculate the bounding box of all of our children
		double xMin = Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE;
		double yMax = Double.MIN_VALUE;

		for (DingAnnotation child: annotations) {
			Rectangle2D childBounds = child.getComponent().getBounds().getBounds2D();
			if (childBounds.getMinX() < xMin) xMin = childBounds.getMinX();
			if (childBounds.getMaxX() > xMax) xMax = childBounds.getMaxX();
			if (childBounds.getMinY() < yMin) yMin = childBounds.getMinY();
			if (childBounds.getMaxY() > yMax) yMax = childBounds.getMaxY();
		}
		bounds = new Rectangle2D.Double(xMin-1, yMin-1, xMax-xMin+2, yMax-yMin+2);
		getComponent().setSize((int)bounds.getWidth()+2, (int)bounds.getHeight()+2);
		getComponent().setLocation((int)bounds.getX(), (int)bounds.getY());
	}

	public Rectangle getBounds() {
		return getBounds2D().getBounds();
	}
}
