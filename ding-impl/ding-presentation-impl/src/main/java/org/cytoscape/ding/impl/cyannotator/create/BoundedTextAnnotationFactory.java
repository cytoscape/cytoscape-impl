package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Color;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.UIManager;

import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.annotations.BoundedTextAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.BoundedTextAnnotationEditor;
import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;

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

public class BoundedTextAnnotationFactory extends AbstractDingAnnotationFactory<BoundedTextAnnotation> {

	public static final String NAME = "Bounded Text";

	private Icon icon;
	
	public BoundedTextAnnotationFactory(CyServiceRegistrar serviceRegistrar) {
		super(BoundedTextAnnotation.class, serviceRegistrar);
	}
	
	@Override
	public BoundedTextAnnotationEditor createEditor() {
		return new BoundedTextAnnotationEditor(this, serviceRegistrar);
	}

	@Override
	public BoundedTextAnnotation createAnnotation(Class<? extends BoundedTextAnnotation> type, CyNetworkView view,
			Map<String, String> argMap) {
		if (!this.type.equals(type))
			return null;
		
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		
		if (re == null)
			return null;
		
		return new BoundedTextAnnotationImpl(re, argMap);
	}

	@Override
	public String getId() {
		return NAMESPACE + "BoundedText";
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Icon getIcon() {
		if (icon == null) {
			// Lazily initialize the icon here, because the LAF might not have been set yet,
			// and we need to get the correct colors
			var font = serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 16f);
			icon = new TextIcon(
					new String[] { IconUtil.ICON_ANNOTATION_BOUNDED_TEXT_1, IconUtil.ICON_ANNOTATION_BOUNDED_TEXT_2 },
					font,
					new Color[] { Color.WHITE, UIManager.getColor("Label.foreground") },
					ICON_SIZE, ICON_SIZE,
					0
			);
		}
		
		return icon;
	}
}
