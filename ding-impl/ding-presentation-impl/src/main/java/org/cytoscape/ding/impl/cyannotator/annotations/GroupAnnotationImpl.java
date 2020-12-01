package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.IllegalAnnotationStructureException;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

public class GroupAnnotationImpl extends AbstractAnnotation implements GroupAnnotation {

	private List<DingAnnotation> annotations = new ArrayList<>();

	public GroupAnnotationImpl(DRenderingEngine re, Map<String, String> argMap) {
		super(re, processArgs(argMap));
	}

	private static Map<String, String> processArgs(Map<String, String> argMap) {
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
			var dMember = (DingAnnotation) member;

			if (dMember.getGroupParent() != null && dMember.getGroupParent() != this)
				throw new IllegalAnnotationStructureException("Annotation is already a member of another group.");
			
			if (!annotations.contains(dMember)) {
				annotations.add(dMember);
				dMember.setGroupParent(this);
				
				try {
					getCyAnnotator().checkCycle();
				} catch (IllegalAnnotationStructureException e) {
					annotations.remove(dMember);
					dMember.setGroupParent(null);
					throw e;
				}
			}

			updateBounds();
			
			var bounds = getBounds();
			setLocation((int)bounds.getX(), (int)bounds.getY());
			setSize((int)bounds.getWidth(), (int)bounds.getHeight());
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
			
			updateBounds();
		}
	}

	@Override
	public void removeAnnotation() {
		// Remove all of our children
		for (var a : annotations)
			cyAnnotator.removeAnnotation(a);

		annotations.clear();
		// Now remove ourselves
		cyAnnotator.removeAnnotation(this);
		
		if (groupParent != null)
			groupParent.removeMember(this);
	}
	
	@Override
	public Map<String, String> getArgMap() {
		var argMap = super.getArgMap();
		argMap.put(TYPE, GroupAnnotation.class.getName());
		String members = "";

		if (annotations == null || annotations.size() == 0)
			return argMap;

		for (var a : annotations)
			members += a.getUUID().toString() + ",";

		if (members != null && members.length() > 1)
			argMap.put(MEMBERS, members.substring(0, members.length() - 1));

		return argMap;
	}

	/**
	 * Just calls the same method on all the child annotations.
	 */
	@Override
	public void setStyle(Map<String, String> argMap) {
		if (argMap != null) {
			for (var a : annotations)
				a.setStyle(argMap);
		}
	}
	
	@Override
	public void setLocation(double x, double y) {
		double deltaX = getX() - x;
		double deltaY = getY() - y;

		for (var child : annotations) {
			var x2 = child.getX() - deltaX;
			var y2 = child.getY() - deltaY;
			
			if (child.getX() != x2 || child.getY() != y2)
				child.setLocation(x2, y2);
		}

		updateBounds();
	}

	@Override
	public void resizeAnnotationRelative(Rectangle2D initialBounds, Rectangle2D outlineBounds) {
		for (var a : annotations)
			((AbstractAnnotation) a).resizeAnnotationRelative(initialBounds, outlineBounds);
		
		updateBounds();
	}
	
	@Override
	public void saveBounds() {
		super.saveBounds();
		
		for (var a : annotations)
			((AbstractAnnotation) a).saveBounds();
	}

	@Override
	public void changeCanvas(CanvasID canvasId) {
		for (var a : annotations)
			a.changeCanvas(canvasId);
		
		super.changeCanvas(canvasId);
	}

	@Override
	public void paint(Graphics g, boolean showSelected) {
		super.paint(g, showSelected);
    if (annotations != null && annotations.size() > 0) {
      Graphics2D g2 = (Graphics2D)g;
			AffineTransform savedTransform = g2.getTransform();
      AffineTransform transform = new AffineTransform();
			Rectangle2D bounds = getBounds();

      if (rotation != 0.0) {
        transform.rotate(Math.toRadians(rotation), bounds.getX() + bounds.getWidth()/2, bounds.getY() + bounds.getHeight()/2);
        g2.transform(transform);
      }

      for (DingAnnotation annotation: annotations)
        annotation.paint(g, false);

      if (rotation != 0.0) {
        g2.setTransform(savedTransform);
      }
    }

		updateBounds();
	}

	private void updateBounds() {
		Rectangle2D union = null;

		for (var child : annotations) {
			if (union == null)
				union = child.getBounds().getBounds2D();
			else
				union = union.createUnion(child.getBounds().getBounds2D());
		}

		if (union != null)
			setBounds(union.getBounds());
	}
}
