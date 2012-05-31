package org.cytoscape.ding.impl.cyannotator.api;

import java.awt.Image;
import java.net.URL;

public interface ImageAnnotation extends Annotation {
	public void reloadImage();

	public Image getImage();
	public void setImage(Image image);
	public void setImage(URL url);

	public void setImageOpacity(float opacity);
	public float getImageOpacity();
}
