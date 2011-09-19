package org.cytoscape.ding.customgraphics;

import java.awt.Image;
import java.util.List;


/**
 * Base interface for all Custom Graphics.
 *
 */
public interface CyCustomGraphics<T> {
		
	/**
	 * Immutable session-unique identifier of image generated in constructor.
	 * 
	 * NOT globally unique.  Uniqueness is guaranteed in a session.
	 * 
	 * @return Immutable ID as Long.
	 */
	public Long getIdentifier();
	
	/**
	 * Display name is a simple description of this image object.
	 * May not be unique and mutable.
	 * 
	 * @return display name as String.
	 */
	public String getDisplayName();
	
	
	/**
	 * Set human readable display name.
	 * 
	 * @param displayName
	 */
	public void setDisplayName(final String displayName);
	
	
	/**
	 * Get layers belongs to this object.
	 * In current Implementation, ti's always Ding's CustomGraphic object.
	 * Ordered by Z-Order value.
	 * 
	 * @return Collection of layer objects (in this version, it's CustomGraphics in Ding)
	 * 
	 */
	public List<Layer<T>> getLayers();
	
	
	/**
	 * Returns width of current object.
	 * 
	 * @return
	 */
	public int getWidth();
	
	
	/**
	 * Returns height of current object.
	 * 
	 * @return
	 */
	public int getHeight();
	
	
	/**
	 * Set width of Custom Graphics.
	 * 
	 * @param width
	 */
	public void setWidth(final int width);
	
	/**
	 * Set height of Custom Graphics.
	 * 
	 * @param height
	 */
	public void setHeight(final int height);
	
	public float getFitRatio();
	public void setFitRatio(float ratio);
	
	/**
	 * From layers of graphics objects, render scaled Image object.
	 * Usually done by Java2D low level code. 
	 * 
	 * Usually, the image returned by this method is used in GUI components (as icons).
	 * 
	 * @return rendered image object.
	 */
	public Image getRenderedImage();
}
