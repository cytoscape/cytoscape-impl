package org.cytoscape.io.internal.write.json.serializer;


/**
 * Repository for Cytoscape.js tags
 * 
 */
public enum CytoscapeJsToken {
	
	////////////// Basic tags in cytoscape.js JSON files ////////////////
	ELEMENTS("elements"),
	NODES("nodes"),
	EDGES("edges"),
	DATA("data"),

	ID("id"),
	SOURCE("source"),
	TARGET("target"),

	CLASSES("classes"),
	GROUP("group"),
	SELECTOR("selector"),

	POSITION("position"),
	POSITION_X("x"),
	POSITION_Y("y"),

	STYLE("style"), 
	CSS("css"), 
	TITLE("title"),

	// For Selectors
	NODE("node"),
	EDGE("edge"),

	//////////////////////////////////////////////////////////////////////////////////////
	// Element States
	//////////////////////////////////////////////////////////////////////////////////////
	STATE_ANIMATED(":animated"), //Matches elements that are currently being animated.
	STATE_UNANIMATED(":unanimated"), //Matches elements that are not currently being animated.
	SELECTED(":selected"), // Matches selected elements.
	UNSELECTED(":unselected"), // Matches elements that aren't selected.
	SELECTABLE(":selectable"), // Matches elements that are selectable.
	UNSELECTABLE(":unselectable"), // Matches elements that aren't selectable.
	LOCKED(":locked"), // Matches locked elements.
	UNLOCKED(":unlocked"), // Matches elements that aren't locked.
	VISIBLE(":visible"), // Matches elements that are visible.
	HIDDEN(":hidden"), // Matches elements that are hidden.
	GRABBED(":grabbed"), // Matches elements that are being grabbed by the user.
	FREE(":free"), // Matches elements that are not currently being grabbed by the user.
	GRABBABLE(":grabbable"), // Matches elements that are grabbable by the user.
	UNGRABBABLE(":ungrabbable"), // Matches elements that are not grabbable by the user.
	REMOVED(":removed"), // Matches elements that have been removed from the graph.
	INSIDE(":inside"), // Matches elements that have are in the graph (they are not removed).
	ACTIVE(":active"), // Matches elements that are active (i.e. user interaction).
	INACTIVE(":inactive"), // Matches elements that are inactive (i.e. no user interaction).


	////////////////// Visual Style Tags////////////////////
	COLOR("color"), // The color of the element's label.
	CONTENT("content"), //The text to display for an element's label.
	CURSOR("cursor"), // The CSS cursor shown when the cursor is over top the element.
	FONT_FAMILY("font-family"), // A comma-separated list of font names to use on the label text.
	FONT_SIZE("font-size"), // The size of the label text.
	FONT_STYLE("font-style"), //A CSS font style to be applied to the label text.
	FONT_WEIGHT("font-weight"), // A CSS font weight to be applied to the label text.
	OPACITY("opacity"), //The opacity of the element.
	TEXT_OPACITY("text-opacity"), // The opacity of the label text, including its outline.
	TEXT_OUTLINE_COLOR("text-outline-color"), // The color of the outline around the element's label text.
	TEXT_OUTLINE_OPACITY("text-outline-opacity"), // The opacity of the outline on label text.
	TEXT_OUTLINE_WIDTH("text-outline-width"), // The size of the outline on label text.
	VISIBILITY("visibility"), // Whether the element is visible; can be visible or hidden.
	WIDTH("width"), // The element's width; the line width for edges or the horizontal size of a node.
	Z_INDEX("z-index"), // A non-negative integer that specifies the z-ordering of the element. 


	///////////////////// For nodes //////////////////////////
	BACKGROUND_COLOR("background-color"), // The color of the node's body.
	BACKGROUND_IMAGE("background-image"), // The URL that points to the image that should be used as the node's background.
	BACKGROUND_OPACITY("background-opacity"), // The opacity level of the node's body.
	BORDER_COLOR("border-color"), // The color of the node's border.
	BORDER_WIDTH("border-width"), // The size of the node's border.
	BORDER_OPACITY("border-opacity"), // The opacity of the node's border.
	HEIGHT("height"), // The height of the node's body.
	SHAPE("shape"), // The shape of the node's body; may be rectangle, roundrectangle, ellipse, or triangle.
	TEXT_HALIGN("text-halign"), // The vertical alignment of a label; may have value left, center, or right.
	TEXT_VALIGN("text-valign"), // The vertical alignment of a label; may have value top, center, or bottom.


	/////////////////// For edges ///////////////////////
	LINE_COLOR("line-color"), // The color of the edge's line.
	LINE_STYLE("line-style"), // The style of the edge's line; may be solid, dotted, or dashed.
	SOURCE_ARROW_COLOR("source-arrow-color"), // The color of the edge's arrow on the source side.
	SOURCE_ARROW_SHAPE("source-arrow-shape"), // The shape of the edge's arrow on the source side; may be tee, triangle, square, circle, diamond, or none.
	TARGET_ARROW_COLOR("target-arrow-color"), // The color of the edge's arrow on the target side.
	TARGET_ARROW_SHAPE("target-arrow-shape") // The shape of the edge's arrow on the target side; may be tee, triangle, square, circle, diamond, or none.
	;

	private String tag;
	
	private CytoscapeJsToken(final String tag) {
		this.tag = tag;
	}
	
	
	public String getTag() {
		return tag;
	}

	@Override
	public String toString() {
		return tag;
	}
}
