package org.cytoscape.ding.impl;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;

/**
 * Level of Details object for Ding.
 * 
 * TODO: design and implement event/listeners for this.
 * 
 */
public class DingGraphLOD extends GraphLOD implements PropertyUpdatedListener {

	protected int coarseDetailThreshold;
	protected int nodeBorderThreshold;
	protected int nodeLabelThreshold;
	protected int edgeArrowThreshold;
	protected int edgeLabelThreshold;

	private final Properties props;
	private final CyProperty<Properties> cyProp;

	private final CyApplicationManager appManager;


	public DingGraphLOD(final CyProperty<Properties> defaultProps, final CyApplicationManager appManager) {
		if (defaultProps == null)
			throw new NullPointerException("CyProperty is missing.");

		this.props = defaultProps.getProperties();
		this.cyProp = defaultProps;
		this.appManager = appManager;
		init();
	}

	private void init() {

		coarseDetailThreshold = parseInt(props.getProperty("render.coarseDetailThreshold"), 4000);
		nodeBorderThreshold = parseInt(props.getProperty("render.nodeBorderThreshold"), 400);
		nodeLabelThreshold = parseInt(props.getProperty("render.nodeLabelThreshold"), 200);
		edgeArrowThreshold = parseInt(props.getProperty("render.edgeArrowThreshold"), 600);
		edgeLabelThreshold = parseInt(props.getProperty("render.edgeLabelThreshold"), 200);

	}

	private int parseInt(final String intString, final int defaultValue) {

		int value;
		try {
			value = Integer.parseInt(intString);
		} catch (Exception e) {	
			return defaultValue;
		}
		return value;
	}


	@Override
	public void handleEvent(PropertyUpdatedEvent e) {

		if (!e.getSource().equals(cyProp))
			return;

		init();
		appManager.getCurrentNetworkView().updateView();

	}

	/**
	 * Determines whether or not to render all edges in a graph, no edges, or
	 * only those edges which touch a visible node. By default this method
	 * returns zero, which leads the rendering engine to render only those edges
	 * that touch at least one visible node. If a positive value is returned,
	 * all edges in the graph will be rendered. If a negative value is returned,
	 * no edges will be rendered. This is the first method called on an instance
	 * of GraphLOD by the rendering engine; the renderEdgeCount parameter passed
	 * to other methods will have a value which reflects the decision made by
	 * the return value of this method call.
	 * <p>
	 * Note that rendering all edges leads to a dramatic performance decrease
	 * when rendering large graphs.
	 * 
	 * @param visibleNodeCount
	 *            the number of nodes visible in the current viewport; note that
	 *            a visible node is not necessarily a rendered node, because
	 *            visible nodes with zero width or height are not rendered.
	 * @param totalNodeCount
	 *            the total number of nodes in the graph that is being rendered.
	 * @param totalEdgeCount
	 *            the total number of edges in the graph that is being rendered.
	 * @return zero if only edges touching a visible node are to be rendered,
	 *         positive if all edges are to be rendered, or negative if no edges
	 *         are to be rendered.
	 */
	public byte renderEdges(final int visibleNodeCount, final int totalNodeCount, final int totalEdgeCount) {
		if (totalEdgeCount >= Math.min(edgeArrowThreshold, edgeLabelThreshold)) {
			return (byte) 0;
		} else {
			return (byte) 1;
		}
	}

	/**
	 * Determines whether or not to render a graph at full detail. By default
	 * this method returns true if and only if the sum of rendered nodes and
	 * rendered edges is less than 1200.
	 * <p>
	 * The following table describes the difference between full and low
	 * rendering detail in terms of what methods on an instance of GraphGraphics
	 * get called: <blockquote>
	 * <table border="1" cellpadding="5" cellspacing="0">
	 * <tr>
	 * <td></td>
	 * <th>full detail</th>
	 * <th>low detail</th>
	 * </tr>
	 * <tr>
	 * <th>nodes</th>
	 * <td>drawNodeFull()</td>
	 * <td>drawNodeLow()</td>
	 * </tr>
	 * <tr>
	 * <th>edges</th>
	 * <td>drawEdgeFull()</td>
	 * <td>drawEdgeLow()</td>
	 * </tr>
	 * <tr>
	 * <th>node labels</th>
	 * <td>drawTextFull()</td>
	 * <td>not rendered</td>
	 * </tr>
	 * <tr>
	 * <th>edge labels</th>
	 * <td>drawTextFull()</td>
	 * <td>not rendered</td>
	 * </tr>
	 * <tr>
	 * <th>custom node graphics</th>
	 * <td>drawCustomGraphicFull()</td>
	 * <td>not rendered</td>
	 * </tr>
	 * </table>
	 * </blockquote>
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true for full detail, false for low detail.
	 */
	public boolean detail(final int renderNodeCount, final int renderEdgeCount) {
		return (renderNodeCount + renderEdgeCount) < coarseDetailThreshold;
	}

	/**
	 * Determines whether or not to render node borders. By default this method
	 * returns true if and only if the number of rendered nodes is less than
	 * 200.
	 * <p>
	 * It is only possible to draw node borders at the full detail level. If low
	 * detail is chosen, the output of this method is ignored.
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true if and only if node borders are to be rendered.
	 * @see #detail(int, int)
	 */
	public boolean nodeBorders(final int renderNodeCount, final int renderEdgeCount) {
		return renderNodeCount < nodeBorderThreshold;
	}

	/**
	 * Determines whether or not to render node labels. By default this method
	 * returns true if and only if the number of rendered nodes is less than 60.
	 * <p>
	 * Node labels are only rendered at the full detail level. If low detail is
	 * chosen, the output of this method is ignored.
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true if and only if node labels are to be rendered.
	 * @see #detail(int, int)
	 */
	public boolean nodeLabels(final int renderNodeCount, final int renderEdgeCount) {
		return renderNodeCount < nodeLabelThreshold;
	}

	/**
	 * Determines whether or not to render custom graphics on nodes. By default
	 * this method returns true if and only if the number of rendered nodes is
	 * less than 60.
	 * <p>
	 * Custom node graphics are only rendered at the full detail level. If low
	 * detail is chosen, the output of this method is ignored.
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true if and only if custom node graphics are to be rendered.
	 * @see #detail(int, int)
	 */
	public boolean customGraphics(final int renderNodeCount, final int renderEdgeCount) {
		return renderNodeCount < nodeBorderThreshold;
	}

	/**
	 * Determines whether or not to render edge arrows. By default this method
	 * returns true if and only if the number of rendered edges is less than
	 * 300.
	 * <p>
	 * It is only possible to draw edge arrows at the full detail level. If low
	 * detail is chosen, the output of this method is ignored.
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true if and only if edge arrows are to be rendered.
	 * @see #detail(int, int)
	 */
	public boolean edgeArrows(final int renderNodeCount, final int renderEdgeCount) {
		return renderEdgeCount < edgeArrowThreshold;
	}

	/**
	 * Determines whether or not to honor dashed edges. By default this method
	 * always returns true. If false is returned, edges that claim to be dashed
	 * will be rendered as solid.
	 * <p>
	 * It is only possible to draw dashed edges at the full detail level. If low
	 * detail is chosen, the output of this method is ignored. Note that drawing
	 * dashed edges is computationally expensive; the default implementation of
	 * this method does not make a very performance-minded decision if a lot of
	 * edges happen to be dashed.
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true if and only if dashed edges are to be honored.
	 * @see #detail(int, int)
	 */
	public boolean dashedEdges(final int renderNodeCount, final int renderEdgeCount) {
		return true;
	}

	/**
	 * Determines whether or not to honor edge anchors. By default this method
	 * always returns true. If false is returned, edges that claim to have edge
	 * anchors will be rendered as simple straight edges.
	 * <p>
	 * It is only possible to draw poly-edges at the full detail level. If low
	 * detail is chosen, the output of this method is ignored.
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true if and only if edge anchors are to be honored.
	 * @see #detail(int, int)
	 */
	public boolean edgeAnchors(final int renderNodeCount, final int renderEdgeCount) {
		return true;
	}

	/**
	 * Determines whether or not to render edge labels. By default this method
	 * returns true if and only if the number of rendered edges is less than 80.
	 * <p>
	 * Edge labels are only rendered at the full detail level. If low detail is
	 * chosen, the output of this method is ignored.
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true if and only if edge labels are to be rendered.
	 * @see #detail(int, int)
	 */
	public boolean edgeLabels(final int renderNodeCount, final int renderEdgeCount) {
		return renderEdgeCount < edgeLabelThreshold;
	}

	/**
	 * Determines whether or not to draw text as shape when rendering node and
	 * edge labels. By default this method always returns false.
	 * <p>
	 * This method affects the boolean parameter drawTextAsShape in the method
	 * call GraphGraphics.drawTextFull(). If neither node nor edge labels are
	 * rendered then the output of this method is ignored.
	 * 
	 * @param renderNodeCount
	 *            the number of nodes that are about to be rendered.
	 * @param renderEdgeCount
	 *            the number of edges that are about to be rendered.
	 * @return true if and only if rendered label text should be drawn as
	 *         primitive shapes.
	 * @see #nodeLabels(int, int)
	 * @see #edgeLabels(int, int)
	 */
	public boolean textAsShape(final int renderNodeCount, final int renderEdgeCount) {
		return false;
	}

	public double getNestedNetworkImageScaleFactor() {
		final String scaleFactor = props.getProperty("nestedNetwork.imageScaleFactor", "1.0");
		try {
			final double d = Double.valueOf(scaleFactor);
			return d <= 0.0 ? 1.0 : d;
		} catch (final Exception e) {
			return 1.0;
		}
	}

}