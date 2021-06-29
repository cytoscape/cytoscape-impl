package org.cytoscape.graph.render.stateful;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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


/**
 * An instance of this class defines the level of detail that goes into
 * a single rendering of a graph.  This class is meant to be subclassed; its
 * methods are meant to be overridden; nonetheless, sane defaults are
 * used in the default method implementations.<p>
 * To understand the significance of each method's return value, it makes
 * sense to become familiar with the API cytoscape.render.immed.GraphGraphics.
 */
public interface GraphLOD {
	
	public enum RenderEdges {
		NONE,
		TOUCHING_VISIBLE_NODES,
		ALL
	}
	
	default public GraphLOD faster() {
		return this;
	}
	
	/**
	 * Determines whether or not to render all edges in a graph, no edges, or
	 * only those edges which touch a visible node.  By default
	 * this method returns zero, which leads the rendering engine to render
	 * only those edges that touch at least one visible node.  If a positive
	 * value is returned, all edges in the graph will be rendered.  If a negative
	 * value is returned, no edges will be rendered.  This is the first
	 * method called on an instance of GraphLOD by the rendering engine;
	 * the renderEdgeCount parameter passed to other methods
	 * will have a value which reflects the decision made by the return value
	 * of this method call.<p>
	 * Note that rendering all edges leads to a dramatic performance decrease
	 * when rendering large graphs.
	 * @param visibleNodeCount the number of nodes visible in the current
	 *   viewport; note that a visible node is not necessarily a rendered node,
	 *   because visible nodes with zero width or height are not rendered.
	 * @param totalNodeCount the total number of nodes in the graph that is
	 *   being rendered.
	 * @param totalEdgeCount the total number of edges in the graph that is
	 *   being rendered.
	 * @return zero if only edges touching a visible node are to be rendered,
	 *   positive if all edges are to be rendered, or negative if no edges
	 *   are to be rendered.
	 */
	RenderEdges renderEdges(int visibleNodeCount, int totalNodeCount, int totalEdgeCount);

	/**
	 * Determines whether or not to render a graph at full detail.
	 * By default this method returns true if and only if the sum of rendered
	 * nodes and rendered edges is less than 1200.<p>
	 * The following table describes the difference between full and low
	 * rendering detail in terms of what methods on an instance of
	 * GraphGraphics get called:
	 * <blockquote><table border="1" cellpadding="5" cellspacing="0">
	 *   <tr>  <td></td>
	 *         <th>full detail</th>
	 *         <th>low detail</th>                                          </tr>
	 *   <tr>  <th>nodes</th>
	 *         <td>drawNodeFull()</td>
	 *         <td>drawNodeLow()</td>                                       </tr>
	 *   <tr>  <th>edges</th>
	 *         <td>drawEdgeFull()</td>
	 *         <td>drawEdgeLow()</td>                                       </tr>
	 *   <tr>  <th>node labels</th>
	 *         <td>drawTextFull()</td>
	 *         <td>not rendered</td>                                        </tr>
	 *   <tr>  <th>edge labels</th>
	 *         <td>drawTextFull()</td>
	 *         <td>not rendered</td>                                        </tr>
	 *   <tr>  <th>custom node graphics</th>
	 *         <td>drawCustomGraphicFull()</td>
	 *         <td>not rendered</td>                                        </tr>
	 * </table></blockquote>
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true for full detail, false for low detail.
	 */
	boolean detail(int renderNodeCount, int renderEdgeCount);

	/**
	 * Determines whether or not to render node borders.  By default this
	 * method returns true if and only if the number of rendered nodes
	 * is less than 200.<p>
	 * It is only possible to draw node borders at the full detail
	 * level.  If low detail is chosen, the output of this method is ignored.
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true if and only if node borders are to be rendered.
	 * @see #detail(int, int)
	 */
	boolean nodeBorders(int renderNodeCount, int renderEdgeCount);

	/**
	 * Determines whether or not to render node labels.  By default this method
	 * returns true if and only if the number of rendered nodes is less than
	 * 60.<p>
	 * Node labels are only rendered at the full detail level.  If low detail is
	 * chosen, the output of this method is ignored.
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true if and only if node labels are to be rendered.
	 * @see #detail(int, int)
	 */
	boolean nodeLabels(int renderNodeCount, int renderEdgeCount);

	/**
	 * Determines whether or not to render custom graphics on nodes.
	 * By default this method returns true if and only if the number of rendered
	 * nodes is less than 60.<p>
	 * Custom node graphics are only rendered at the full detail level.  If low
	 * detail is chosen, the output of this method is ignored.
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true if and only if custom node graphics are to be rendered.
	 * @see #detail(int, int)
	 */
	boolean customGraphics(int renderNodeCount, int renderEdgeCount);

	/**
	 * Determines whether or not to render edge arrows.  By default this
	 * method returns true if and only if the number of rendered edges is less
	 * than 300.<p>
	 * It is only possible to draw edge arrows at the full detail
	 * level.  If low detail is chosen, the output of this method is ignored.
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true if and only if edge arrows are to be rendered.
	 * @see #detail(int, int)
	 */
	boolean edgeArrows(int renderNodeCount, int renderEdgeCount);
	
	/**
	 * Determines whether or not to honor dashed edges.  By default this
	 * method always returns true.  If false is returned, edges that
	 * claim to be dashed will be rendered as solid.<p>
	 * It is only possible to draw dashed edges at the full detail
	 * level.  If low detail is chosen, the output of this method is ignored.
	 * Note that drawing dashed edges is computationally expensive;
	 * the default implementation of this method does not make a very
	 * performance-minded decision if a lot of edges happen to be dashed.
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true if and only if dashed edges are to be honored.
	 * @see #detail(int, int)
	 */
	boolean dashedEdges(int renderNodeCount, int renderEdgeCount);

	/**
	 * Determines whether or not to honor edge anchors.  By default this
	 * method always returns true.  If false is returned, edges that
	 * claim to have edge anchors will be rendered as simple straight
	 * edges.<p>
	 * It is only possible to draw poly-edges at the full detail
	 * level.  If low detail is chosen, the output of this method is ignored.
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true if and only if edge anchors are to be honored.
	 * @see #detail(int, int)
	 */
	boolean edgeAnchors(int renderNodeCount, int renderEdgeCount);

	/**
	 * Determines whether or not to render edge labels.  By default this method
	 * returns true if and only if the number of rendered edges is less than
	 * 80.<p>
	 * Edge labels are only rendered at the full detail level.  If low detail is
	 * chosen, the output of this method is ignored.
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true if and only if edge labels are to be rendered.
	 * @see #detail(int, int)
	 */
	boolean edgeLabels(int renderNodeCount, int renderEdgeCount);

	/**
	 * Determines whether or not to draw text as shape when rendering node and
	 * edge labels.  By default this method always returns false.<p>
	 * This method affects the boolean parameter drawTextAsShape in the method
	 * call GraphGraphics.drawTextFull().  If neither node nor edge labels are
	 * rendered then the output of this method is ignored.
	 * @param renderNodeCount the number of nodes that are about to be rendered.
	 * @param renderEdgeCount the number of edges that are about to be rendered.
	 * @return true if and only if rendered label text should be drawn as
	 *   primitive shapes.
	 * @see #nodeLabels(int, int)
	 * @see #edgeLabels(int, int)
	 */
	boolean textAsShape(int renderNodeCount, int renderEdgeCount);

	/**
	 * Returns true if the edge buffer panning optimization should be turned on.
	 */
	boolean isEdgeBufferPanEnabled();

	/**
	 * Returns true if the label buffer cache optimization should be turned on.
	 */
	boolean isLabelCacheEnabled();
	
	/**
	 * Returns true if the hidpi mode should be enabled.
	 */
	boolean isHidpiEnabled();
	
	double getNestedNetworkImageScaleFactor();

}
