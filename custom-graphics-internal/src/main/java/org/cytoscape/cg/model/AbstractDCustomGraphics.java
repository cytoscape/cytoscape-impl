package org.cytoscape.cg.model;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

public abstract class AbstractDCustomGraphics<T extends CustomGraphicLayer> implements CyCustomGraphics<T>, Taggable {

	protected static final String DELIMITER = ",";
	public static final String LIST_DELIMITER = "|";
	
	protected float fitRatio = 0.9f;

	// Unique ID
	protected final Long id;
	
	// Layers of Ding Custom Graphic objects.
	protected List<T> layers;
	
	// Human readable name
	protected String displayName;
	
	protected int width = 50;
	protected int height = 50;
	
	protected CyCustomGraphicsFactory<T> factory;

	//protected ObjectPosition position;

	// For tags
	protected final SortedSet<String> tags;
	
	/**
	 * Create new object for a given ID. Used when restoring session.
	 * 
	 * @param id
	 * @param displayName
	 */
	public AbstractDCustomGraphics(Long id, String displayName) {
		this.id = id;
		
		this.layers = new ArrayList<>();
		this.displayName = displayName;

		this.tags = new TreeSet<>();
		//this.position = new ObjectPositionImpl();
	}
	
	@Override
	public Long getIdentifier() {
		return id;
	}
	
	@Override
	public void setIdentifier(Long id) {
		// For our uses, the id is always assigned in our constructor
	}
	
	@Override
	public void setWidth(int width) {
		this.width = width;
	}
	
	@Override
	public void setHeight(int height) {
		this.height = height;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}

	@Override	
	public List<T> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> graphObject) {
		return layers;
	}
	
	@Override
	public List<T> getLayers(CyTableView tableView, CyColumnView columnView, CyRow row) {
		return layers;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	abstract public Image getRenderedImage();

	@Override
	abstract public String toString();

	@Override
	public Collection<String> getTags() {
		return tags;
	}

	// This will be used prop file.
	protected String makeSerializableString(String name) {
		String tagStr = "";

		// Build tags as a string
		if (tags.size() != 0) {
			var builder = new StringBuilder();

			for (String tag : tags)
				builder.append(tag + LIST_DELIMITER);

			String temp = builder.toString();
			tagStr = temp.substring(0, temp.length() - 1);
		}

		if (name == null)
			name = displayName;
		
		if (name.contains(",")) // Replace delimiter
			name = name.replace(",", "___");

		return getTypeFullName() + DELIMITER + getIdentifier() + DELIMITER + name + DELIMITER + tagStr;
	}
	
	/**
	 * The fully-qualified name of the Custom Graphics implementation.
	 */
	public String getTypeFullName() {
		return getClass().getCanonicalName();
	}
	
	@Override
	public void setFitRatio(float fitRatio) {
		this.fitRatio = fitRatio;
	}
	
	@Override
	public float getFitRatio() {
		return fitRatio;
	}
}
