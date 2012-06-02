package org.cytoscape.ding.impl.cyannotator.api;

import java.awt.Image;
import java.net.URL;

public interface ImageAnnotation extends ShapeAnnotation {
	public void reloadImage();

	public Image getImage();
	public void setImage(Image image);
	public URL getImageURL();
	public void setImage(URL url);

	public void setImageOpacity(float opacity);
	public float getImageOpacity();

	public void setImageBrightness(int brightness);
	public int getImageBrightness();

	public void setImageContrast(int contrast);
	public int getImageContrast();
}
