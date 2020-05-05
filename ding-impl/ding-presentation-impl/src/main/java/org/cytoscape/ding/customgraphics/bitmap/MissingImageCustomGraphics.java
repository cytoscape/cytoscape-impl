package org.cytoscape.ding.customgraphics.bitmap;

import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.cytoscape.ding.customgraphics.Taggable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

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

public class MissingImageCustomGraphics extends URLBitmapCustomGraphics {

	private CyCustomGraphics actualCustomGraphics;
	private final String serializationString;
	
	public MissingImageCustomGraphics(
			String serializationString,
			Long id,
			String name,
			AbstractURLImageCGFactory factory
	) throws IOException {
		super(id, name, URLBitmapCustomGraphics.DEF_IMAGE);
		this.serializationString = serializationString;
		this.factory = factory;
	}

	public boolean isImageMissing() {
		return actualCustomGraphics == null;
	}
	
	public CyCustomGraphics reloadImage() {
		var cg = factory.parseSerializableString(serializationString);
		
		if (cg != null && cg instanceof MissingImageCustomGraphics == false)
			actualCustomGraphics = cg;
		
		return actualCustomGraphics;
	}
	
	@Override
	public Long getIdentifier() {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.getIdentifier();
		
		return super.getIdentifier();
	}
	
	@Override
	public void setIdentifier(Long id) {
		if (actualCustomGraphics != null)
			actualCustomGraphics.setIdentifier(id);
		
		super.setIdentifier(id);
	}
	
	@Override
	public int getWidth() {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.getWidth();
		
		return super.getWidth();
	}
	
	@Override
	public void setWidth(int width) {
		if (actualCustomGraphics != null)
			actualCustomGraphics.setWidth(width);
		
		super.setWidth(width);
	}
	
	@Override
	public int getHeight() {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.getHeight();
		
		return super.getHeight();
	}
	
	@Override
	public void setHeight(int height) {
		if (actualCustomGraphics != null)
			actualCustomGraphics.setHeight(height);
		
		super.setHeight(height);
	}
	
	@Override
	public float getFitRatio() {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.getFitRatio();
		
		return super.getFitRatio();
	}
	
	@Override
	public void setFitRatio(float fitRatio) {
		if (actualCustomGraphics != null)
			actualCustomGraphics.setFitRatio(fitRatio);
		
		super.setFitRatio(fitRatio);
	}
	
	@Override
	public String getDisplayName() {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.getDisplayName();
		
		return super.getDisplayName();
	}
	
	@Override
	public void setDisplayName(String displayName) {
		if (actualCustomGraphics != null)
			actualCustomGraphics.setDisplayName(displayName);
		
		super.setDisplayName(displayName);
	}
	
	@Override
	public Collection<String> getTags() {
		if (actualCustomGraphics instanceof Taggable)
			return ((Taggable)actualCustomGraphics).getTags();
		
		return super.getTags();
	}
	
	@Override
	public Image getRenderedImage() {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.getRenderedImage();
		
		return super.getRenderedImage();
	}
	
	@Override
	public List getLayers(CyNetworkView networkView, View graphObject) {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.getLayers(networkView, graphObject);
		
		return super.getLayers(networkView, graphObject);
	}
	
	@Override
	public Image resetImage() {
		if (actualCustomGraphics instanceof URLBitmapCustomGraphics)
			return ((URLBitmapCustomGraphics)actualCustomGraphics).resetImage();
		
		return super.resetImage();
	}
	
	@Override
	public String toSerializableString() {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.toSerializableString();
		
		return super.toSerializableString();
	}
	
	@Override
	protected String getTypeName() {
		return URLBitmapCustomGraphics.class.getCanonicalName();
	}
	
	@Override
	public String toString() {
		if (actualCustomGraphics != null)
			return actualCustomGraphics.toString();
		
		return super.toString();
	}
	
	public CyCustomGraphics getActualCustomGraphics() {
		return actualCustomGraphics;
	}
}
