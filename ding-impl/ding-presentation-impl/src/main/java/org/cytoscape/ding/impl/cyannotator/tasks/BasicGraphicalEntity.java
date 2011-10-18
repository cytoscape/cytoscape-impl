
package org.cytoscape.ding.impl.cyannotator.tasks;

import java.awt.Image;
import java.awt.dnd.DragSource;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.dnd.GraphicalEntity;


public class BasicGraphicalEntity implements GraphicalEntity {

	private final String title;
	private final String attributeName;
	private final String attributeValue;
	private final String description;
	private final Image image;
	private final Icon icon;
	private final DragSource dragSource;

	public BasicGraphicalEntity(String title, String attributeName, String attributeValue, String description, String imageLocation) {
		this.title = title;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
		this.description = description;

		ImageIcon iicon = new ImageIcon(getClass().getResource(imageLocation));
		
		this.icon = iicon;
		this.image = iicon.getImage();
		this.dragSource = new DragSource();
	}

	public String getTitle() {
		return title;
	}

	public DragSource getMyDragSource() {
		return dragSource;
	}

	public Image getImage() {
		return image;
	}

	public Icon getIcon() {
		return icon;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public String getDescription() {
		return description;
	}
}
