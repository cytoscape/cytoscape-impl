package org.cytoscape.view.table.internal.cg;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.table.internal.util.IconUtil;

/**
 * Null object for Custom Graphics. This is used to reset custom graphics on node views.
 */
public class NullCellCustomGraphics implements CellCustomGraphics {

	public static Image DEF_IMAGE = IconUtil.emptyIcon(24, 24).getImage();
	
	private static final String FACTORY_ID = "org.cytoscape.table.NullCellCustomGraphics";
	private static final long ID = 0;
	private static final String DISPLAY_NAME = "[ Remove Graphics ]";
	
	private static final NullCellCustomGraphics NULL = new NullCellCustomGraphics();
	
	public static NullCellCustomGraphics getNullObject() {
		return NULL;
	}
	
	@Override
	public Long getIdentifier() {
		return ID;
	}

	@Override
	public void setIdentifier(Long id) {
		// Ignore...
	}
	
	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}
	
	@Override
	public void setDisplayName(String displayName) {
		// Ignore...
	}

	@Override
	public String getSerializableString() {
		// TODO Can we simplify this? Or should we keep the same general CG standard?
		return FACTORY_ID + "," + ID + "," + DISPLAY_NAME;
	}

	@Override
	public Map<String, Object> getProperties() {
		return Collections.emptyMap();
	}
	
	@Override
	public Image getRenderedImage() {
		return DEF_IMAGE;
	}

	@Override
	public void draw(Graphics g, Rectangle2D bounds, CyColumn column, CyRow row) {
		// Ignore...
	}
	
	@Override
	public String toString() {
		return "None";
	}
}
