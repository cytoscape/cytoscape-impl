package org.cytoscape.ding.customgraphics.bitmap;

import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.cytoscape.ding.customgraphics.Taggable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;

public class MissingImageCustomGraphics extends URLImageCustomGraphics<ImageCustomGraphicLayer> {

	private CyCustomGraphics actualCustomGraphics;
	private final String serializationString;
	
	public MissingImageCustomGraphics(final String serializationString, final Long id, final String name,
			final URLImageCustomGraphicsFactory factory)
			throws IOException {
		super(id, name, URLImageCustomGraphics.DEF_IMAGE);
		this.serializationString = serializationString;
		this.factory = factory;
	}

	public boolean isImageMissing() {
		return actualCustomGraphics == null;
	}
	
	public CyCustomGraphics reloadImage() {
		final CyCustomGraphics cg = factory.parseSerializableString(serializationString);
		
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
		if (actualCustomGraphics instanceof URLImageCustomGraphics)
			return ((URLImageCustomGraphics)actualCustomGraphics).resetImage();
		
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
		return URLImageCustomGraphics.class.getCanonicalName();
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
