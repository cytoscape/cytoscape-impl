package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.dialogs.ArrowAnnotationDialog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;

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

public class ArrowAnnotationFactory extends AbstractDingAnnotationFactory<ArrowAnnotation> {

	public static final String NAME = "Arrow";

	private final Icon icon;

	public ArrowAnnotationFactory(final CyServiceRegistrar serviceRegistrar) {
		super(ArrowAnnotation.class, serviceRegistrar);
		
		Font font = serviceRegistrar.getService(IconManager.class).getIconFont(14f);
		icon = new TextIcon(IconManager.ICON_ARROW_UP, font, ICON_SIZE, ICON_SIZE);
	}

	@Override
	public JDialog createAnnotationDialog(DGraphView view, Point2D location) {
		// We need to be over an annotation
		CyAnnotator cyAnnotator = ((DGraphView) view).getCyAnnotator();
		DingAnnotation annotation = cyAnnotator.getAnnotationAt(location);
		
		if (annotation == null || annotation instanceof ArrowAnnotationImpl) {
			JOptionPane.showMessageDialog(view.getCanvas(), "Please click another annotation.");
			return null;
		}
		
		return new ArrowAnnotationDialog(view, location, getActiveWindow());
	}

	@Override
	public ArrowAnnotation createAnnotation(Class<? extends ArrowAnnotation> type, CyNetworkView view,
			Map<String, String> argMap) {
		if (!(view instanceof DGraphView) || !this.type.equals(type))
			return null;

		return new ArrowAnnotationImpl((DGraphView) view, argMap, getActiveWindow());
	}
	
	@Override
	public String getId() {
		return NAMESPACE + "Arrow";
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}
}
