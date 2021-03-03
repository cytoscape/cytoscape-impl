package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.UIManager;

import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.ImageAnnotationEditor;
import org.cytoscape.ding.impl.cyannotator.dialogs.LoadImageDialog;
import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.ding.internal.util.ViewUtil;
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

public class ImageAnnotationFactory extends AbstractDingAnnotationFactory<ImageAnnotation> {
	
	public static final String NAME = "Image";

	private Icon icon;
	
	public ImageAnnotationFactory(CyServiceRegistrar serviceRegistrar) {
		super(ImageAnnotation.class, serviceRegistrar);
	}

	public LoadImageDialog createLoadImageDialog(CyNetworkView view, Point2D location) {
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		
		return new LoadImageDialog(re, location, ViewUtil.getActiveWindow(re), serviceRegistrar);
	}
	
	@Override
	public ImageAnnotationEditor createEditor() {
		// TODO Move the image dialog to another place???
		return new ImageAnnotationEditor(this, serviceRegistrar);
	}

	@Override
	public ImageAnnotation createAnnotation(Class<? extends ImageAnnotation> type, CyNetworkView view,
			Map<String, String> argMap) {
		if (!this.type.equals(type))
			return null;
		
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		
		if (re == null)
			return null;
		
		var cgManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		return new ImageAnnotationImpl(re, argMap, cgManager);
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
		if (icon == null) {
			// Lazily initialize the icon here, because the LAF might not have been set yet
			// and we need to get the correct colors
			var font = serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 16f);
			icon = new TextIcon(
					new String[] { IconUtil.ICON_ANNOTATION_IMAGE_1, IconUtil.ICON_ANNOTATION_IMAGE_2 },
					font,
					new Color[] { Color.WHITE, UIManager.getColor("Label.foreground") },
					ICON_SIZE, ICON_SIZE,
					0
			);
		}
		
		return icon;
	}
}
