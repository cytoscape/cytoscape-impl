package org.cytoscape.io.internal.write.json.serializer;

import org.cytoscape.view.model.VisualProperty;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;


public enum CytoscapeJsToken {
	SELECTOR("selector"), STYLE("style"), CSS("css"), TITLE("title"), NODE("node"), EDGE("edge"),SELECTED(":selected"),
	
	////////////////// Style Tags////////////////////
	
	CURSOR("cursor"), // The CSS cursor shown when the cursor is over top the element.
	COLOR("color"), // The color of the element's label.
	CONTENT("content"), //The text to display for an element's label.
	TEXT_OUTLINE_COLOR("text-outline-color"), // The color of the outline around the element's label text.
	TEXT_OUTLINE_WIDTH("text-outline-width"), // The size of the outline on label text.
	TEXT_OUTLINE_OPACITY("text-outline-opacity"), // The opacity of the outline on label text.
	TEXT_OPACITY("text-opacity"), // The opacity of the label text, including its outline.
	FONT_FAMILY("font-family"), // A comma-separated list of font names to use on the label text.
	FONT_STYLE("font-style"), //A CSS font style to be applied to the label text.
	FONT_WEIGHT("font-weight"), // A CSS font weight to be applied to the label text.
	FONT_SIZE("font-size"), // The size of the label text.
	VISIBILITY("visibility"), // Whether the element is visible; can be visible or hidden.
	OPACITY("opacity"), //The opacity of the element.
	Z_INDEX("z-index"), // A non-negative integer that specifies the z-ordering of the element. 
						// An element with a higher z-index is put on top of an element with a lower value.
	WIDTH("width", NODE_WIDTH), // The element's width; the line width for edges or the horizontal size of a node.
	
	///////////////////// For nodes //////////////////////////
	TEXT_VALIGN("text-valign"), // The vertical alignment of a label; may have value top, center, or bottom.
	TEXT_HALIGN("text-halign"), // The vertical alignment of a label; may have value left, center, or right.
	BACKGROUND_COLOR("background-color", NODE_FILL_COLOR), // The color of the node's body.
	BACKGROUND_OPACITY("background-opacity", NODE_TRANSPARENCY), // The opacity level of the node's body.
	BACKGROUND_IMAGE("background-image"), // The URL that points to the image that should be used as the node's background.
	BORDER_COLOR("border-color", NODE_BORDER_PAINT), // The color of the node's border.
	BORDER_WIDTH("border-width", NODE_BORDER_WIDTH), // The size of the node's border.
	HEIGHT("height", NODE_HEIGHT), // The height of the node's body.
	SHAPE("shape"), // The shape of the node's body; may be rectangle, roundrectangle, ellipse, or triangle.
	
	/////////////////// For edges ///////////////////////
	SOURCE_ARROW_SHAPE("source-arrow-shape"), // The shape of the edge's arrow on the source side; may be tee, triangle, square, circle, diamond, or none.
	SOURCE_ARROW_COLOR("source-arrow-color"), // The color of the edge's arrow on the source side.
	TARGET_ARROW_SHAPE("target-arrow-shape"), // The shape of the edge's arrow on the target side; may be tee, triangle, square, circle, diamond, or none.
	TARGET_ARROW_COLOR("target-arrow-color"), // The color of the edge's arrow on the target side.
	LINE_COLOR("line-color") // The color of the edge's line.
	;
	
	private String tag;
	private VisualProperty<?> visualProperty;
	
	private CytoscapeJsToken(final String tag) {
		this.tag = tag;
	}
	
	private CytoscapeJsToken(final String tag, final VisualProperty<?> vp) {
		this.tag = tag;
		this.visualProperty = vp;
	}

	public VisualProperty<?> getVisualProperty() {
		return visualProperty;
	}
	
	
	@Override
	public String toString() {
		return tag;
	}

}
