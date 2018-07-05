package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JDialog;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.LoadImageDialog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;

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

public class ImageAnnotationFactory extends AbstractDingAnnotationFactory<ImageAnnotation> {
	
	public static final String NAME = "Image";

	private final Icon icon;
	
	public ImageAnnotationFactory(final CyServiceRegistrar serviceRegistrar) {
		super(ImageAnnotation.class, serviceRegistrar);
		
		Font font = serviceRegistrar.getService(IconManager.class).getIconFont(14f);
		icon = new TextIcon(IconManager.ICON_IMAGE, font, ICON_SIZE, ICON_SIZE);
	}

	@Override
	public JDialog createAnnotationDialog(DGraphView view, Point2D location) {
		final CustomGraphicsManager customGraphicsManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		return new LoadImageDialog(view, location, customGraphicsManager, getActiveWindow());
	}

	@Override
	public ImageAnnotation createAnnotation(Class<? extends ImageAnnotation> type, CyNetworkView view,
			Map<String, String> argMap) {
		if (!(view instanceof DGraphView) || !this.type.equals(type))
			return null;

		final CustomGraphicsManager customGraphicsManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		return new ImageAnnotationImpl((DGraphView) view, argMap, customGraphicsManager, getActiveWindow());
	}
	
	@Override
	public String getId() {
		return NAMESPACE + "Image";
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
