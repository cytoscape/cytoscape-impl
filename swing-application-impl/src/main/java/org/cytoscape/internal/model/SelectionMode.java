package org.cytoscape.internal.model;

import static org.cytoscape.internal.util.IconUtil.SELECTION_MODE_ANNOTATIONS;
import static org.cytoscape.internal.util.IconUtil.SELECTION_MODE_EDGES;
import static org.cytoscape.internal.util.IconUtil.SELECTION_MODE_NODES;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public enum SelectionMode {
	
	NODE_SELECTION(
			"Toggle Node Selection",
			SELECTION_MODE_NODES,
			"Turn off this option if you don't want nodes to be selected when they are clicked or drag-selected.",
			"/images/tooltips/selection-mode-nodes.gif",
			"NETWORK_NODE_SELECTION"
	),
	EDGE_SELECTION(
			"Toggle Edge Selection",
			SELECTION_MODE_EDGES,
			"Turn off this option if you don't want edges to be selected when they are clicked or drag-selected.",
			"/images/tooltips/selection-mode-edges.gif",
			"NETWORK_EDGE_SELECTION"
	),
	ANNOTATION_SELECTION(
			"Toggle Annotation Selection",
			SELECTION_MODE_ANNOTATIONS,
			"Turn off this option if you don't want annotations to be selected when they are clicked or drag-selected.",
			"/images/tooltips/selection-mode-annotations.gif",
			"NETWORK_ANNOTATION_SELECTION"
	),
	NODE_LABEL_SELECTION(
			"Toggle Node Label Selection",
			"0",   // Character which map to the icon in the font.
			"Turn off this option if you don't want node labels to be selected when they are drag-selected.",
			"/images/tooltips/selection-mode-node-label.gif",
			"NETWORK_NODE_LABEL_SELECTION"
	);
	
	private final String text;
	private final String iconText;
	private final String toolTipText;
	private final String toolTipImage;
	private final String propertyId;

	private SelectionMode(
			String text,
			String iconText,
			String toolTipText,
			String toolTipImage,
			String propertyId
	) {
		this.text = text;
		this.iconText = iconText;
		this.toolTipText = toolTipText;
		this.toolTipImage = toolTipImage;
		this.propertyId = propertyId;
	}
	
	public String getText() {
		return text;
	}
	
	public String getIconText() {
		return iconText;
	}
	
	public String getPropertyId() {
		return propertyId;
	}
	
	public String getToolTipText() {
		return toolTipText;
	}
	
	public String getToolTipImage() {
		return toolTipImage;
	}
}
