package org.cytoscape.graph.render.immed;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.impl.canvas.GraphicsProvider;
import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.graph.render.immed.arrow.Arrow;
import org.cytoscape.graph.render.immed.arrow.ArrowheadArrow;
import org.cytoscape.graph.render.immed.arrow.ArrowheadArrowShort;
import org.cytoscape.graph.render.immed.arrow.CrossDeltaArrow;
import org.cytoscape.graph.render.immed.arrow.DeltaArrow;
import org.cytoscape.graph.render.immed.arrow.DeltaArrowShort1;
import org.cytoscape.graph.render.immed.arrow.DeltaArrowShort2;
import org.cytoscape.graph.render.immed.arrow.DiamondArrow;
import org.cytoscape.graph.render.immed.arrow.DiamondArrowShort1;
import org.cytoscape.graph.render.immed.arrow.DiamondArrowShort2;
import org.cytoscape.graph.render.immed.arrow.DiscArrow;
import org.cytoscape.graph.render.immed.arrow.HalfBottomArrow;
import org.cytoscape.graph.render.immed.arrow.HalfCircleArrow;
import org.cytoscape.graph.render.immed.arrow.HalfTopArrow;
import org.cytoscape.graph.render.immed.arrow.NoArrow;
import org.cytoscape.graph.render.immed.arrow.SquareArrow;
import org.cytoscape.graph.render.immed.arrow.TeeArrow;
import org.cytoscape.graph.render.immed.nodeshape.DiamondNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.EllipseNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.HexagonNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.LegacyCustomNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.NodeShape;
import org.cytoscape.graph.render.immed.nodeshape.OctagonNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.ParallelogramNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.RectangleNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.RoundedRectangleNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.TriangleNodeShape;
import org.cytoscape.graph.render.immed.nodeshape.VeeNodeShape;
import org.cytoscape.graph.render.stateful.LabelLineInfo;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;


/**
 * The purpose of this class is to make the proper calls on a Graphics2D object
 * to efficiently render nodes, labels, and edges. This is procedural
 * programming at its finest [sarcasm].
 * <p>
 * This class deals with two coordinate systems: an image coordinate system and
 * a node coordinate system. The programmer who uses this API will be dealing
 * mostly with the node coordinate system, especially when rendering individual
 * nodes and edges. The clear() method specifies the mapping from the node
 * coordinate system to the image coordinate system. The two coordinate systems
 * do have the same orientations: increasing X values point to the right and
 * increasing Y values point to the bottom. The image coordinate system dictates
 * that (0,0) is the upper left corner of the image and that each unit
 * represents a pixel width (or height). if (m_debug) { if
 * (!EventQueue.isDispatchThread()) throw new IllegalStateException( "calling
 * thread is not AWT event dispatcher");
 * 
 * if (!(xMin < xMax)) throw new IllegalArgumentException("xMin not less than
 * xMax");
 * 
 * if (!(yMin < yMax)) throw new IllegalArgumentException("yMin not less than
 * yMax");
 * 
 * if (nodeShape == SHAPE_ROUNDED_RECTANGLE) { final double width = ((double)
 * xMax) - xMin; final double height = ((double) yMax) - yMin;
 * 
 * if (!(Math.max(width, height) < (2.0d * Math.min(width, height)))) throw new
 * IllegalArgumentException( "rounded rectangle does not meet cotextnstraint " +
 * "max(width, height) < 2 * min(width, height)"); } }
 * <p>
 * NOTE: Every method on an instance of this class needs to be called by the AWT
 * event dispatching thread save the constructor. However, checks for this are
 * made only if debug is set to true (see constructur). In fact, in certain
 * situations [such as rendering to a non-image such as a vector graphic] it may
 * make sense to never call any of the methods from the AWT event dispatching
 * thread.
 */
public final class GraphGraphics {

	private static final boolean debug = false;
	
	// Node shape constants
	public static final byte SHAPE_NONE = -1;
	public static final byte SHAPE_RECTANGLE = 0;
	public static final byte SHAPE_DIAMOND = 1;
	public static final byte SHAPE_ELLIPSE = 2;
	public static final byte SHAPE_HEXAGON = 3;
	public static final byte SHAPE_OCTAGON = 4;
	public static final byte SHAPE_PARALLELOGRAM = 5;
	public static final byte SHAPE_ROUNDED_RECTANGLE = 6;
	public static final byte SHAPE_TRIANGLE = 7;
	public static final byte SHAPE_VEE = 8;

	// package scoped for unit testing
	static final byte s_last_shape = SHAPE_VEE;
	public static final int CUSTOM_SHAPE_MAX_VERTICES = 100;
	public static final int MAX_EDGE_ANCHORS = 64;
	private static final float DEF_SHAPE_SIZE = 32;
	
	// A constant for controlling how cubic Bezier curves are drawn; This particular constant results in elliptical-looking curves.
	private static final double CURVE_ELLIPTICAL = (4.0d * (Math.sqrt(2.0d) - 1.0d)) / 3.0d;
	
	// This member variable only to be used from within defineCustomNodeShape().
	private byte m_lastCustomShapeType = s_last_shape;
	
	private RenderDetailFlags renderDetailFlags;
	
	// package scoped for unit testing
	static final EdgeAnchors m_noAnchors = new EdgeAnchors() {
		public final int numAnchors() { return 0; }
		public final void getAnchor(final int inx, final float[] arr) { }
	};
		
	// Mapping from node to its border stroke object.
	private static final Map<Float,Stroke> borderStrokes = new HashMap<>();
	private static final Map<Byte,NodeShape> nodeShapes;
	private static final Map<ArrowShape, Arrow> arrows;
	static {		
		nodeShapes = new HashMap<>();
		nodeShapes.put(SHAPE_RECTANGLE, new RectangleNodeShape()); 
		nodeShapes.put(SHAPE_ELLIPSE, new EllipseNodeShape()); 
		nodeShapes.put(SHAPE_ROUNDED_RECTANGLE, new RoundedRectangleNodeShape()); 
		nodeShapes.put(SHAPE_DIAMOND, new DiamondNodeShape()); 
		nodeShapes.put(SHAPE_HEXAGON, new HexagonNodeShape()); 
		nodeShapes.put(SHAPE_OCTAGON, new OctagonNodeShape()); 
		nodeShapes.put(SHAPE_PARALLELOGRAM, new ParallelogramNodeShape()); 
		nodeShapes.put(SHAPE_TRIANGLE, new TriangleNodeShape()); 
		nodeShapes.put(SHAPE_VEE, new VeeNodeShape());

		arrows = new HashMap<>();
		arrows.put(ArrowShapeVisualProperty.NONE, new NoArrow());
		arrows.put(ArrowShapeVisualProperty.DELTA, new DeltaArrow());
		arrows.put(ArrowShapeVisualProperty.CIRCLE, new DiscArrow());
		arrows.put(ArrowShapeVisualProperty.DIAMOND, new DiamondArrow());
		arrows.put(ArrowShapeVisualProperty.T, new TeeArrow());
		arrows.put(ArrowShapeVisualProperty.ARROW, new ArrowheadArrow());
		arrows.put(ArrowShapeVisualProperty.HALF_TOP, new HalfTopArrow());
		arrows.put(ArrowShapeVisualProperty.HALF_BOTTOM, new HalfBottomArrow());
		arrows.put(ArrowShapeVisualProperty.DELTA_SHORT_1, new DeltaArrowShort1());
		arrows.put(ArrowShapeVisualProperty.DELTA_SHORT_2, new DeltaArrowShort2());
		arrows.put(ArrowShapeVisualProperty.ARROW_SHORT, new ArrowheadArrowShort());
		arrows.put(ArrowShapeVisualProperty.DIAMOND_SHORT_1, new DiamondArrowShort1());
		arrows.put(ArrowShapeVisualProperty.DIAMOND_SHORT_2, new DiamondArrowShort2());
		// added v3.6
		arrows.put(ArrowShapeVisualProperty.OPEN_CIRCLE, new DiscArrow());
		arrows.put(ArrowShapeVisualProperty.OPEN_DELTA, new DeltaArrow());
		arrows.put(ArrowShapeVisualProperty.OPEN_DIAMOND, new DiamondArrow());
		arrows.put(ArrowShapeVisualProperty.HALF_CIRCLE, new HalfCircleArrow(true));
		arrows.put(ArrowShapeVisualProperty.OPEN_HALF_CIRCLE, new HalfCircleArrow(false));
		arrows.put(ArrowShapeVisualProperty.SQUARE, new SquareArrow());
		arrows.put(ArrowShapeVisualProperty.OPEN_SQUARE, new SquareArrow());
		arrows.put(ArrowShapeVisualProperty.CROSS_DELTA, new CrossDeltaArrow());
		arrows.put(ArrowShapeVisualProperty.CROSS_OPEN_DELTA, new CrossDeltaArrow());
	}


	// Cached data, and objects that can be reused
	private final AffineTransform m_xformUtil = new AffineTransform();
	private final GeneralPath m_path2d = new GeneralPath();
	private final GeneralPath m_path2dPrime = new GeneralPath();
	private final Line2D.Double m_line2d = new Line2D.Double();
	private final double[] m_ptsBuff = new double[4];
	private final double[] m_edgePtsBuff = new double[(MAX_EDGE_ANCHORS + 1) * 6];
	// This is only used by computeCubicPolyEdgePath().
	private final float[] m_floatBuff = new float[2];
	// This member variable shall only be used from within drawTextFull().
	private final FontRenderContext m_fontRenderContextFull = new FontRenderContext(null,true,true);
	
	private final GraphicsProvider graphicsProvider;
	private Graphics2D m_g2d;
	private Graphics2D m_gMinimal; // We use mostly java.awt.Graphics methods.
	private final AffineTransform m_currNativeXform = new AffineTransform();
	

	public GraphGraphics(GraphicsProvider graphicsProvider) {
		this.graphicsProvider = graphicsProvider;
		this.m_path2dPrime.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		update(null);
	}
	
	public NetworkTransform getTransform() {
		return graphicsProvider.getTransform();
	}
	
	
	public final void update(RenderDetailFlags flags) {
		this.update(flags, true);
	}

	public final void update(RenderDetailFlags flags, boolean clear) {
		this.renderDetailFlags = flags;
		if (m_gMinimal != null) {
			m_gMinimal.dispose();
			m_gMinimal = null;
		}
		if (m_g2d != null) {
			m_g2d.dispose();
		}

		m_g2d = graphicsProvider.getGraphics(clear);
		m_gMinimal = graphicsProvider.getGraphics(clear);

		setRenderingHints(m_g2d);
		setRenderingHintsMinimal(m_gMinimal);

		m_g2d.transform(getTransform().getPaintAffineTransform());
		m_currNativeXform.setTransform(m_g2d.getTransform()); // save the current transform
	}

	
	public static void setRenderingHints(Graphics2D g) {
		// Antialiasing is ON
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// Rendering quality is HIGH.
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		// High quality alpha blending is ON.
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		// High quality color rendering is ON.
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		// Text antialiasing is ON.
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f));
	}
	
	private static void setRenderingHintsMinimal(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}
	
	
	/**
	 * This is the method that will render a node very quickly. The node shape
	 * used by this method is SHAPE_RECTANGLE. Translucent colors are not
	 * supported by the low detail rendering methods.
	 * <p>
	 * xMin, yMin, xMax, and yMax specify the extents of the node in the node
	 * coordinate space, not the image coordinate space. Thus, these values will
	 * likely not change from frame to frame, as zoom and pan operations are
	 * performed.
	 * <p>
	 * This method will not work unless clear() has been called at least once
	 * previously.
	 * 
	 * @param xMin
	 *            an extent of the node to draw, in node coordinate space.
	 * @param yMin
	 *            an extent of the node to draw, in node coordinate space.
	 * @param xMax
	 *            an extent of the node to draw, in node coordinate space.
	 * @param yMax
	 *            an extent of the node to draw, in node coordinate space.
	 * @param fillColor
	 *            the [fully opaque] color to use when drawing the node.
	 * @exception IllegalArgumentException
	 *                if xMin is not less than xMax, if yMin is not less than
	 *                yMax, or if fillColor is not opaque.
	 */
	public final void drawNodeLow(final float xMin, final float yMin,
			final float xMax, final float yMax, final Color fillColor) {
		if (debug) {
			checkOrder(xMin,xMax,"x");
			checkOrder(yMin,yMax,"y");
			if (fillColor.getAlpha() != 255) {
				throw new IllegalArgumentException("fillColor is not opaque");
			}
		}

		// I'm transforming points manually because the resulting underlying
		// graphics pipeline used is much faster.
		m_ptsBuff[0] = xMin;
		m_ptsBuff[1] = yMin;
		m_ptsBuff[2] = xMax;
		m_ptsBuff[3] = yMax;
		getTransform().getPaintAffineTransform().transform(m_ptsBuff, 0, m_ptsBuff, 0, 2);

		// Here, double values outside of the range of ints will be cast to
		// the nearest int without overflow.
		final int xNot = (int) m_ptsBuff[0];
		final int yNot = (int) m_ptsBuff[1];
		final int xOne = (int) m_ptsBuff[2];
		final int yOne = (int) m_ptsBuff[3];

		m_gMinimal.setColor(fillColor);
		m_gMinimal.fillRect(xNot, yNot, Math.max(1, xOne - xNot), // Overflow will
		                                Math.max(1, yOne - yNot)); // be problem.
	}


	/**
	 * Draws a node with medium to high detail, depending on parameters
	 * specified. The xMin, yMin, xMax, and yMax parameters specify the extents
	 * of the node shape (in the node coordinate system), including the border
	 * width. That is, the drawn border won't extend beyond the extents
	 * specified.
	 * <p>
	 * There is an imposed constraint on borderWidth which, using the
	 * implemented algorithms, prevents strange-looking borders. The constraint
	 * is that borderWidth may not exceed the minimum of the node width and node
	 * height divided by six. In addition, for custom node shapes, this
	 * requirement may be more constrained, depending on the kinks in the custom
	 * node shape.
	 * <p>
	 * There is a constraint that only applies to SHAPE_ROUNDED_RECTANGLE which
	 * imposes that the maximum of the width and height be strictly less than
	 * twice the minimum of the width and height of the node.
	 * <p>
	 * This method will not work unless clear() has been called at least once
	 * previously.
	 * 
	 * @param nodeShape
	 *            the shape of the node to draw (one of the SHAPE_* constants or
	 *            a custom node shape).
	 * @param xMin
	 *            an extent of the node shape to draw, in node coordinate space;
	 *            the drawn shape will theoretically contain a point that lies
	 *            on this X coordinate.
	 * @param yMin
	 *            an extent of the node shape to draw, in node coordinate space;
	 *            the drawn shape will theoretically contain a point that lies
	 *            on this Y coordinate.
	 * @param xMax
	 *            an extent of the node shape to draw, in node coordinate space;
	 *            the drawn shape will theoretically contain a point that lies
	 *            on this X coordinate.
	 * @param yMax
	 *            an extent of the node shape to draw, in node coordinate space;
	 *            the drawn shape will theoretically contain a point that lies
	 *            on this Y coordinate.
	 * @param fillPaint
	 *            the paint to use when drawing the node area minus the border
	 *            (the "interior" of the node).
	 * @param borderWidth
	 *            the border width, in node coordinate space; if this value is
	 *            zero, the rendering engine skips over the process of rendering
	 *            the border, which gives a significant performance boost.
	 * @param borderPaint
	 *            if borderWidth is not zero, this paint is used for rendering
	 *            the node border; otherwise, this parameter is ignored (and may
	 *            be null).
	 * @exception IllegalArgumentException
	 *                if xMin is not less than xMax or if yMin is not less than
	 *                yMax, if borderWidth is negative or is greater than
	 *                Math.min(xMax - xMin, yMax - yMin) / 6 (for custom node
	 *                shapes borderWidth may be even more limited, depending on
	 *                the specific shape), if nodeShape is
	 *                SHAPE_ROUNDED_RECTANGLE and the condition max(width,
	 *                height) < 2 * min(width, height) does not hold, or if
	 *                nodeShape is neither one of the SHAPE_* constants nor a
	 *                previously defined custom node shape.
	 */
	public final Shape drawNodeFull(final byte nodeShape, final float xMin,
			final float yMin, final float xMax, final float yMax,
			final Paint fillPaint, final float borderWidth, final Stroke borderStroke,
			final Paint borderPaint) {
		if (debug) {
			checkOrder(xMin,xMax,"x");
			checkOrder(yMin,yMax,"y");
			if (!(borderWidth >= 0.0f)) {
				throw new IllegalArgumentException("borderWidth not zero or positive");
			}
			if (!((6.0d * borderWidth) <= Math.min(((double) xMax) - xMin, ((double) yMax) - yMin))) {
				throw new IllegalArgumentException(
						"borderWidth is not less than the minimum of node width and node height divided by six");
			}
		}

		final float off = borderWidth/2.0f; // border offset
		final Shape sx = getShape(nodeShape,xMin+off,yMin+off,xMax-off,yMax-off);

		// Draw border only when width is not zero.
		if (borderWidth > 0.0f) {
			m_g2d.setPaint(borderPaint);
			if(borderStroke != null)
				m_g2d.setStroke(borderStroke);
			else
				m_g2d.setStroke(getStroke(borderWidth));
			
			m_g2d.draw(sx);
		}

		// System.out.println("Node shape = "+sx);
		// System.out.println("Node bounds = "+sx.getBounds2D());
		// System.out.println("Paint = "+fillPaint);

		m_g2d.setPaint(fillPaint);
		m_g2d.fill(sx);
		return sx;
	}

	/**
	 * Computes the path a node shape takes; this method is useful if a user
	 * interface would allow user selection of nodes, for example. Use the same
	 * parameter values that were used to render corresponding node.
	 * 
	 * @param nodeShape
	 *            the shape (SHAPE_* constant or custom shape) of the node in
	 *            question.
	 * @param xMin
	 *            an extent of the node in question, in node coordinate space.
	 * @param yMin
	 *            an extent of the node in question, in node coordinate space.
	 * @param xMax
	 *            an extent of the node in question, in node coordinate space.
	 * @param yMax
	 *            an extent of the node in question, in node coordinate space.
	 * @param path
	 *            the computed path is returned in this parameter; the computed
	 *            path's coordinate system is the node coordinate system; the
	 *            computed path is closed.
	 */
	public static void getNodeShape(byte nodeShape, float xMin, float yMin, float xMax, float yMax, GeneralPath path) {
		path.reset();
		path.append(getShape(nodeShape, xMin, yMin, xMax, yMax), false);
	}

	/**
	 * The custom node shape that is defined is a polygon specified by the
	 * coordinates supplied. The polygon must meet several constraints listed
	 * below.
	 * <p>
	 * If we define the value xCenter to be the average of the minimum and
	 * maximum X values of the vertices and if we define yCenter likewise, then
	 * the specified polygon must meet the following constraints:
	 * <ol>
	 * <li>Each polygon line segment must have nonzero length.</li>
	 * <li>No two consecutive polygon line segments can be parallel (this
	 * essentially implies that the polygon must have at least three vertices).</li>
	 * <li>No two distinct non-consecutive polygon line segments may intersect
	 * (not even at the endpoints); this makes possible the notion of interior
	 * of the polygon.</li>
	 * <li>The polygon must be star-shaped with respect to the point (xCenter,
	 * yCenter); a polygon is said to be <i>star-shaped with respect to a point
	 * (a,b)</i> if and only if for every point (x,y) in the interior or on the
	 * boundary of the polygon, the interior of the segment (a,b)->(x,y) lies in
	 * the interior of the polygon.</li>
	 * <li>The path traversed by the polygon must be clockwise where +X points
	 * right and +Y points down.</li>
	 * </ol>
	 * <p>
	 * In addition to these constraints, when rendering custom nodes with
	 * nonzero border width, possible problems may arise if the border width is
	 * large with respect to the kinks in the polygon.
	 * 
	 * @param coords
	 *            vertexCount * 2 consecutive coordinate values are read from
	 *            this array starting at coords[offset]; coords[offset],
	 *            coords[offset + 1], coords[offset + 2], coords[offset + 3] and
	 *            so on are interpreted as x0, y0, x1, y1, and so on; the
	 *            initial vertex need not be repeated as the last vertex
	 *            specified.
	 * @param offset
	 *            the starting index of where to read coordinates from in the
	 *            coords parameter.
	 * @param vertexCount
	 *            the number of vertices to read from coords; vertexCount * 2
	 *            entries in coords are read.
	 * @return the node shape identifier to be used in future rendering calls
	 *         (to be used as parameter nodeShape in method drawNodeFull()).
	 * @exception IllegalArgumentException
	 *                if any of the constraints are not met, or if the specified
	 *                polygon has more than CUSTOM_SHAPE_MAX_VERTICES vertices.
	 * @exception IllegalStateException
	 *                if too many custom node shapes are already defined; a
	 *                little over one hundered custom node shapes can be
	 *                defined.
	 */
	public final byte defineCustomNodeShape(final float[] coords, final int offset, final int vertexCount) {
		if (vertexCount > CUSTOM_SHAPE_MAX_VERTICES) {
			throw new IllegalArgumentException( "too many vertices (greater than "
							+ CUSTOM_SHAPE_MAX_VERTICES + ")");
		}

		final double[] polyCoords = new double[vertexCount * 2];

		for (int i = 0; i < polyCoords.length; i++)
			polyCoords[i] = coords[offset + i];

		// Normalize the polygon so that it spans [-0.5, 0.5] x [-0.5, 0.5].
		double xMin = Double.POSITIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < polyCoords.length;) {
			xMin = Math.min(xMin, coords[i]);
			xMax = Math.max(xMax, coords[i++]);
			yMin = Math.min(yMin, coords[i]);
			yMax = Math.max(yMax, coords[i++]);
		}

		final double xDist = xMax - xMin;

		if (xDist == 0.0d) 
			throw new IllegalArgumentException( "polygon does not move in the X direction");

		final double yDist = yMax - yMin;

		if (yDist == 0.0d) 
				throw new IllegalArgumentException( "polygon does not move in the Y direction");

		final double xMid = (xMin + xMax) / 2.0d;
		final double yMid = (yMin + yMax) / 2.0d;

		for (int i = 0; i < polyCoords.length;) {
			double foo = (polyCoords[i] - xMid) / xDist;
			polyCoords[i++] = Math.min(Math.max(-0.5d, foo), 0.5d);
			foo = (polyCoords[i] - yMid) / yDist;
			polyCoords[i++] = Math.min(Math.max(-0.5d, foo), 0.5d);
		}


		int yInterceptsCenter = 0;

		for (int i = 0; i < vertexCount; i++) {
			final double x0 = polyCoords[i * 2];
			final double y0 = polyCoords[(i * 2) + 1];
			final double x1 = polyCoords[((i * 2) + 2) % (vertexCount * 2)];
			final double y1 = polyCoords[((i * 2) + 3) % (vertexCount * 2)];
			final double x2 = polyCoords[((i * 2) + 4) % (vertexCount * 2)];
			final double y2 = polyCoords[((i * 2) + 5) % (vertexCount * 2)];
			final double distP0P1 = Math.sqrt(((x1 - x0) * (x1 - x0)) + ((y1 - y0) * (y1 - y0)));

			// Too close to distance zero.
			if ((float) distP0P1 == 0.0f) { 
				throw new IllegalArgumentException(
						"a line segment has distance [too close to] zero");
			}

			final double distP2fromP0P1 = ((((y0 - y1) * x2)
					+ ((x1 - x0) * y2) + (x0 * y1)) - (x1 * y0)) / distP0P1;

			// Too close to parallel.
			if ((float) distP2fromP0P1 == 0.0f) { 
				throw new IllegalArgumentException(
						"either a line segment has distance [too close to] zero or "
								+ "two consecutive line segments are [too close to] parallel");
			}

			final double distCenterFromP0P1 = ((x0 * y1) - (x1 * y0)) / distP0P1;

			if (!((float) distCenterFromP0P1 > 0.0f)) {
				throw new IllegalArgumentException(
						"polygon is going counter-clockwise or is not star-shaped with "
								+ "respect to center");
			}

			if ((Math.min(y0, y1) < 0.0d) && (Math.max(y0, y1) >= 0.0d)) {
				yInterceptsCenter++;
			}
		}

		if (yInterceptsCenter != 2) {
			throw new IllegalArgumentException(
					"the polygon self-intersects (we know this because the winding "
							+ "number of the center is not one)");
		}

		// polyCoords now contains a polygon spanning [-0.5, 0.5] X [-0.5, 0.5]
		// that passes all of the criteria.
		final byte nextCustomShapeType = (byte) (m_lastCustomShapeType + 1);

		if (nextCustomShapeType < 0) 
			throw new IllegalStateException( "too many custom node shapes are already defined");

		m_lastCustomShapeType++;
		nodeShapes.put(nextCustomShapeType, new LegacyCustomNodeShape(polyCoords,nextCustomShapeType));

		return nextCustomShapeType;
	}

	/**
	 * Determines whether the specified shape is a custom defined node shape.
	 */
	public final boolean customNodeShapeExists(final byte shape) {
		return (shape > s_last_shape) && (shape <= m_lastCustomShapeType);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final byte[] getCustomNodeShapes() {
		final byte[] returnThis = new byte[m_lastCustomShapeType - s_last_shape];

		for (int i = 0; i < returnThis.length; i++) {
			returnThis[i] = (byte) (s_last_shape + 1 + i);
		}

		return returnThis;
	}

	/**
	 * Returns the vertices of a previously defined custom node shape. The
	 * polygon will be normalized to fit within the [-0.5, 0.5] x [-0.5, 0.5]
	 * square. Returns null if specified shape is not a previously defined
	 * custom shape.
	 */
	public final float[] getCustomNodeShape(final byte customShape) {
		if ( !customNodeShapeExists(customShape) )
			return null;

		LegacyCustomNodeShape ns = (LegacyCustomNodeShape)(nodeShapes.get(customShape));
		return ns.getCoords();
	}

	/**
	 * If this is a new instance, imports the custom node shapes from the
	 * GraphGraphics specified into this GraphGraphics.
	 * 
	 * @param grafx
	 *            custom node shapes will be imported from this GraphGraphics.
	 * @exception IllegalStateException
	 *                if at least one custom node shape is already defined in
	 *                this GraphGraphics.
	 */
	public final void importCustomNodeShapes(final GraphGraphics grafx) {
		// I define this error check outside the scope of m_debug because
		// clobbering existing custom node shape definitions could be major.
		if (m_lastCustomShapeType != s_last_shape) {
			throw new IllegalStateException(
					"a custom node shape is already defined in this GraphGraphics");
		}

		for (Map.Entry<Byte, NodeShape> entry : grafx.nodeShapes.entrySet()) {
			nodeShapes.put(entry.getKey(), entry.getValue());
			m_lastCustomShapeType++;
		}
	}

	private static Shape getShape(byte nodeShape, float xMin, float yMin, float xMax, float yMax) {
		NodeShape ns = nodeShapes.get(nodeShape);
		return ns == null ? null : ns.getShape(xMin, yMin, xMax, yMax);
	}

	/**
	 * get list of node shapes.
	 * 
	 * @return A map of node shape bytes to Shape objects.
	 */
	public static Map<Byte, Shape> getNodeShapes() {
		final Map<Byte, Shape> shapeMap = new HashMap<>();

		for ( NodeShape ns : nodeShapes.values() ) {
			final Shape shape = ns.getShape(0f, 0f, DEF_SHAPE_SIZE, DEF_SHAPE_SIZE);
			shapeMap.put(ns.getType(), new GeneralPath( shape ));
		}

		return shapeMap;
	}

	/**
	 * Get map that contains the AWT shape of each arrow head.
	 */
	public static Map<ArrowShape, Shape> getArrowShapes() {
		final Map<ArrowShape, Shape> shapeMap = new HashMap<>();

		for (final ArrowShape key : arrows.keySet())
			shapeMap.put(key, arrows.get(key).getArrowShape());

		return shapeMap;
	}

	/**
	 * This is the method that will render an edge very quickly. Translucent
	 * colors are not supported by the low detail rendering methods.
	 * <p>
	 * The points (x0, y0) and (x1, y1) specify the endpoints of edge to be
	 * rendered in the node coordinate space, not the image coordinate space.
	 * Thus, these values will likely not change from frame to frame, as zoom
	 * and pan operations are performed. If these two points are identical,
	 * nothing is drawn.
	 * <p>
	 * This method will not work unless clear() has been called at least once
	 * previously.
	 * 
	 * @param x0
	 *            the X coordinate of the begin point of edge to render.
	 * @param y0
	 *            the Y coordinate of the begin point of edge to render.
	 * @param x1
	 *            the X coordinate of the end point of edge to render.
	 * @param y1
	 *            the Y coordinate of the end point of edge to render.
	 * @param edgeColor
	 *            the [fully opaque] color to use when drawing the edge.
	 * @exception IllegalArgumentException
	 *                if edgeColor is not opaque.
	 */
	public final void drawEdgeLow(final float x0, final float y0, final float x1, final float y1, final Color edgeColor) {
		if (debug) {
			if (edgeColor.getAlpha() != 255) {
				throw new IllegalArgumentException("edgeColor is not opaque");
			}
		}

		// This following statement has to be consistent with the full edge
		// rendering logic.
		if ((x0 == x1) && (y0 == y1)) {
			return;
		}

		// I'm transforming points manually because the resulting underlying
		// graphics pipeline used is much faster.
		m_ptsBuff[0] = x0;
		m_ptsBuff[1] = y0;
		m_ptsBuff[2] = x1;
		m_ptsBuff[3] = y1;
		getTransform().getPaintAffineTransform().transform(m_ptsBuff, 0, m_ptsBuff, 0, 2);

		final int xNot = (int) m_ptsBuff[0];
		final int yNot = (int) m_ptsBuff[1];
		final int xOne = (int) m_ptsBuff[2];
		final int yOne = (int) m_ptsBuff[3];
		m_gMinimal.setColor(edgeColor);
		m_gMinimal.drawLine(xNot, yNot, xOne, yOne);
	}

	/**
	 * Draws an edge with medium to high detail, depending on parameters
	 * specified. Something is rendered in all cases except where the length of
	 * the edge is zero (because in that case directionality cannot be
	 * determined for at least some arrowheads).
	 * <p>
	 * The arrow types must each be one of the ARROW_* constants. The arrow at
	 * endpoint 1 is always "on top of" the arrow at endpoint 0 if they overlap
	 * because the arrow at endpoint 0 gets rendered first.
	 * <p>
	 * If an arrow other than NONE is rendered, its size must be greater
	 * than or equal to edge thickness specified. The table below describes, to
	 * some extent, the nature of each arrow type. <blockquote><table
	 * border="1" cellpadding="5" cellspacing="0">
	 * <tr>
	 * <th>arrow type</th>
	 * <th>description</th>
	 * </tr>
	 * <tr>
	 * <td>NONE</td>
	 * <td>the edge line segment has endpoint specified, and the line segment
	 * has a round end (center of round semicircle end exactly equal to endpoint
	 * specified); arrow size and arrow paint are ignored</td>
	 * </tr>
	 * <tr>
	 * <td>DELTA</td>
	 * <td>the sharp tip of the arrowhead is exactly at the endpint specified;
	 * the delta is as wide as the arrow size specified and twice that in length</td>
	 * </tr>
	 * <tr>
	 * <td>DIAMOND</td>
	 * <td>the sharp tip of the arrowhead is exactly at the endpoint specified;
	 * the diamond is as wide as the arrow size specified and twice that in
	 * length</td>
	 * </tr>
	 * <tr>
	 * <td>CIRCLE</td>
	 * <td>the disc arrowhead is placed such that its center is at the
	 * specified endpoint; the diameter of the disk is the arrow size specified</td>
	 * </tr>
	 * <tr>
	 * <td>T</td>
	 * <td>the center of the tee intersection lies at the specified endpoint;
	 * the width of the top of the tee is one quarter of the arrow size
	 * specified, and the span of the top of the tee is two times the arrow size</td>
	 * </tr>
	 * <tr>
	 * <td>HALF_TOP</td>
	 * <td>Draws a line the width of the stroke away from the node at the midpoint 
	 * between the edge and the node on the "top" of the edge.</td>
	 * </tr>
	 * <tr>
	 * <td>HALF_BOTTOM</td>
	 * <td>Draws a line the width of the stroke away from the node at the midpoint 
	 * between the edge and the node on the "bottom" of the edge.</td>
	 * </tr>
	 * <tr>
	 * <td>NEW: OPEN_CIRCLE & OPEN_DELTA</td>
	 * <td>Unfilled versions of existing arrows.</td>
	 * </tr>
	 * <tr>
	 * <td>NEW: SQUARE & OPEN_SQUARE</td>
	 * <td>Draws a square at the end of the edge.</td>
	 * </tr>
	 * <tr>
	 * <td>NEW: HALF_CIRCLE & OPEN_HALF_CIRCLE</td>
	 * <td>Draws a 180 degree arc, filled or open</td>
	 * </tr>
	 * <tr>
	 * <td>NEW: CROSS_DELTA & OPEN_CROSS_DELTA</td>
	 * <td>Draws a cross hatch line behind the arrow head.  Comes in flavors: filled or open</td>
	 * </tr>
	 * </table></blockquote>
	 * <p>
	 * Note that if the edge segment length is zero then nothing gets rendered.
	 * <p>
	 * This method will not work unless clear() has been called at least once
	 * previously.
	 * <p>
	 * A discussion pertaining to edge anchors. At most MAX_EDGE_ANCHORS
	 * edge anchors may be specified. The edge anchors are used to define cubic
	 * Bezier curves. The exact algorithm for determining the Bezier curves from
	 * the input parameters is too complicated to describe in this Javadoc. Some
	 * parts of the algorithm: <blockquote>
	 * <ul>
	 * <li>the conglomerated curve is [probably] not going to pass through the
	 * edge anchors points specified; the curve will pass through the midpoint
	 * between every consecutive pair of anchors</li>
	 * <li>when determining the edge path as a whole, an ordered list of points
	 * is created by putting point (x0, y0) at the beginning of the list,
	 * followed by the anchor points, followed by point (x1, y1); then,
	 * duplicate points are removed from the beginning and end of this list</li>
	 * <li>from the list described above, the first two points define the arrow
	 * direction at point (x0, y0) and the initial curve direction; likewise,
	 * the last two points in this list define the arrow direction at point (x1,
	 * y1) and the ending curve direction</li>
	 * </ul>
	 * </blockquote> In order to specify a straight-line edge path, simply
	 * duplicate each edge anchor in the EdgeAnchors instance. For example, a
	 * smooth curve would be drawn by specifying consecutive-pairwise disctinct
	 * points {(x0,y0), A0, A1, A2, (x1,y1)}; a straight-line edge path would be
	 * drawn by specifying {(x0, y0), A0, A0, A1, A1, A2, A2, (x1, y1)}.
	 * 
	 * @param arrow0Type
	 *            the type of arrow shape to use for drawing the arrow at point
	 *            (x0, y0); this value must be one of the ARROW_* constants.
	 * @param arrow0Size
	 *            the size of arrow at point (x0, y0); how size is interpreted
	 *            for different arrow types is described in the table above.
	 * @param arrow0Paint
	 *            the paint to use when drawing the arrow at point (x0, y0).
	 * @param arrow1Type
	 *            the type of arrow shape to use for drawing the arrow at point
	 *            (x1, y1); this value must be one of the ARROW_* constants.
	 * @param arrow1Size
	 *            the size of arrow at point (x1, y1); how size is interpreted
	 *            for different arrow types is described in the table above.
	 * @param arrow1Paint
	 *            the paint to use when drawing the arrow at point (x1, y1).
	 * @param x0
	 *            the X coordinate of the first edge endpoint.
	 * @param y0
	 *            the Y coordinate of the first edge endpoint.
	 * @param anchors
	 *            anchor points between the two edge endpoints; null is an
	 *            acceptable value to indicate no edge anchors.
	 * @param x1
	 *            the X coordinate of the second edge endpoint.
	 * @param y1
	 *            the Y coordinate of the second edge endpoint.
	 * @param edgeThickness
	 *            the thickness of the edge segment; the edge segment is the
	 *            part of the edge between the two endpoint arrows.
	 * @param edgeStroke
	 *            the Stroke to use when drawing the edge segment.
	 * @param edgePaint
	 *            the paint to use when drawing the edge segment.
	 * @exception IllegalArgumentException
	 *                if edgeThickness is less than zero, if any one of the arrow 
	 *                configurations does not meet specified criteria, or if more 
	 *                than MAX_EDGE_ANCHORS anchors are specified.
	 */
	public final void drawEdgeFull(final ArrowShape arrow0Type,
			final float arrow0Size, final Paint arrow0Paint,
			final ArrowShape arrow1Type, final float arrow1Size,
			final Paint arrow1Paint, final float x0, final float y0,
			EdgeAnchors anchors, final float x1, final float y1,
			final float edgeThickness, Stroke edgeStroke, final Paint edgePaint) {
		final double curveFactor = CURVE_ELLIPTICAL;
		final boolean simpleSegment = arrow0Type == ArrowShapeVisualProperty.NONE && arrow1Type == ArrowShapeVisualProperty.NONE;
		
		// when rendering arrows we need the stroke cap to be CAP_BUTT 
		if(!simpleSegment && edgeStroke instanceof BasicStroke && ((BasicStroke)edgeStroke).getEndCap() != BasicStroke.CAP_BUTT) {
			BasicStroke bs = (BasicStroke) edgeStroke;
			edgeStroke = new BasicStroke(bs.getLineWidth(), BasicStroke.CAP_BUTT, 
					bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase());
		}

		if (anchors == null) {
			anchors = m_noAnchors;
		}

		if (debug) {
			edgeFullDebug(arrow0Type, arrow0Size, arrow1Type, arrow1Size, edgeStroke, edgeThickness, anchors);
		}

		final int edgePtsCount = computeCubicPolyEdgePath(m_edgePtsBuff, m_floatBuff, arrow0Type,
				(arrow0Type == ArrowShapeVisualProperty.NONE) ? 0.0f : arrow0Size, arrow1Type,
				(arrow1Type == ArrowShapeVisualProperty.NONE) ? 0.0f : arrow1Size, x0, y0,
				anchors, x1, y1, curveFactor);
		
		if (edgePtsCount < 3) {
			if (edgePtsCount == 2) { // Draw an ordinary edge.
				drawSimpleEdgeFull(arrow0Type, arrow0Size, arrow0Paint,
						arrow1Type, arrow1Size, arrow1Paint,
						(float) m_edgePtsBuff[0], (float) m_edgePtsBuff[1],
						(float) m_edgePtsBuff[2], (float) m_edgePtsBuff[3],
						edgeThickness, edgeStroke, edgePaint );
			}
			return;
		}

		// Render the edge polypath.
		
		m_g2d.setStroke(edgeStroke);

		// Set m_path2d to contain the cubic curves computed in
		// m_edgePtsBuff.
		m_path2d.reset();
		m_path2d.moveTo((float) m_edgePtsBuff[2], (float) m_edgePtsBuff[3]);

		int inx = 4;
		final int count = ((edgePtsCount - 1) * 6) - 2;

		while (inx < count) {
			m_path2d.curveTo((float) m_edgePtsBuff[inx++],
					(float) m_edgePtsBuff[inx++],
					(float) m_edgePtsBuff[inx++],
					(float) m_edgePtsBuff[inx++],
					(float) m_edgePtsBuff[inx++],
					(float) m_edgePtsBuff[inx++]);
		}

		m_g2d.setPaint(edgePaint);
		m_g2d.draw(m_path2d);

		if (simpleSegment) {
			return;
		}

		// We need to figure out the phase at the end of the cubic poly-path
		// for dashed segments. I cannot find a Java API to do this; our
		// best
		// bet would be to implement our own cubic curve length calculating
		// function, but our computation may not agree with BasicStroke's
		// computation. So what we're going to do is never render the arrow
		// caps for dashed edges.

		final double dx0 = m_edgePtsBuff[0] - m_edgePtsBuff[4];
		final double dy0 = m_edgePtsBuff[1] - m_edgePtsBuff[5];
		final double len0 = Math.sqrt((dx0 * dx0) + (dy0 * dy0));
		final double cosTheta0 = dx0 / len0;
		final double sinTheta0 = dy0 / len0;

		final double dx1 = m_edgePtsBuff[((edgePtsCount - 1) * 6) - 2] - m_edgePtsBuff[((edgePtsCount - 1) * 6) - 6];
		final double dy1 = m_edgePtsBuff[((edgePtsCount - 1) * 6) - 1] - m_edgePtsBuff[((edgePtsCount - 1) * 6) - 5];
		final double len1 = Math.sqrt((dx1 * dx1) + (dy1 * dy1));
		final double cosTheta1 = dx1 / len1;
		final double sinTheta1 = dy1 / len1;

		// Only draw the edge caps if the stroke is a BasicStroke, which is to
		// say, don't worry about how fancy strokes intersect the arrow. 
		if ( edgeStroke instanceof BasicStroke ) {

			// Render arrow cap at origin of poly path.
			final Shape arrow0Cap = computeUntransformedArrowCap(arrow0Type, ((double) arrow0Size) / edgeThickness);
			if (arrow0Cap != null) {
				m_xformUtil.setTransform(cosTheta0, sinTheta0, -sinTheta0, cosTheta0, m_edgePtsBuff[2], m_edgePtsBuff[3]);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(arrow0Cap);
				m_g2d.setTransform(m_currNativeXform);
			}
	
			// Render arrow cap at end of poly path.
			final Shape arrow1Cap = computeUntransformedArrowCap(arrow1Type, ((double) arrow1Size) / edgeThickness);
	
			if (arrow1Cap != null) {
				m_xformUtil.setTransform(cosTheta1, sinTheta1, -sinTheta1, cosTheta1,
						m_edgePtsBuff[((edgePtsCount - 1) * 6) - 4],
						m_edgePtsBuff[((edgePtsCount - 1) * 6) - 3]);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(arrow1Cap);
				m_g2d.setTransform(m_currNativeXform);
			}
		}

		// Render arrow at origin of poly path.
		final Shape arrow0 = computeUntransformedArrow(arrow0Type);

		if (arrow0 != null) {
			m_xformUtil.setTransform(cosTheta0, sinTheta0, -sinTheta0, cosTheta0, m_edgePtsBuff[0], m_edgePtsBuff[1]);
			m_g2d.transform(m_xformUtil);
			m_g2d.scale(arrow0Size, arrow0Size);
			m_g2d.setPaint(arrow0Paint);
			boolean filled = arrow0Type.isFilled();
			if (filled)
				m_g2d.fill(arrow0);
			else 
			{
				float strokeWidth = 0.25f;
				m_g2d.setStroke(new BasicStroke(0.25f));
				m_g2d.draw(arrow0);
			}
			m_g2d.setTransform(m_currNativeXform);
		}

		// Render arrow at end of poly path.
		final Shape arrow1 = computeUntransformedArrow(arrow1Type);

		if (arrow1 != null) {
				
			m_xformUtil.setTransform(cosTheta1, sinTheta1, -sinTheta1, cosTheta1,
				m_edgePtsBuff[((edgePtsCount - 1) * 6) - 2],
				m_edgePtsBuff[((edgePtsCount - 1) * 6) - 1]);
			m_g2d.transform(m_xformUtil);
			m_g2d.scale(arrow1Size, arrow1Size);
			m_g2d.setPaint(arrow1Paint);
			boolean filled = arrow1Type.isFilled();
			if (filled)
				m_g2d.fill(arrow1);
			else 
			{
				m_g2d.setStroke(new BasicStroke(0.025f));
				m_g2d.draw(arrow1);
			}
			m_g2d.setTransform(m_currNativeXform);
		}
	}

	@SuppressWarnings("fallthrough")
	private final void edgeFullDebug(final ArrowShape arrow0Type,
			final float arrow0Size, final ArrowShape arrow1Type, float arrow1Size,
			final Stroke edgeStroke,
			final float edgeThickness, 
			final EdgeAnchors anchors) {
		if (!(edgeThickness >= 0.0f)) {
			throw new IllegalArgumentException("edgeThickness < 0");
		}

		if ( !arrows.containsKey( arrow0Type ) )
			throw new IllegalArgumentException("arrow0Type is not recognized");

		if ( arrow0Type != ArrowShapeVisualProperty.NONE )
			if (!(arrow0Size >= edgeThickness)) 
				throw new IllegalArgumentException(
						"arrow size must be at least as large as edge thickness");

		if ( !arrows.containsKey( arrow1Type ) )
			throw new IllegalArgumentException("arrow1Type is not recognized");

		if ( arrow1Type != ArrowShapeVisualProperty.NONE )
			if (!(arrow1Size >= edgeThickness)) 
				throw new IllegalArgumentException(
						"arrow size must be at least as large as edge thickness");

		if (anchors.numAnchors() > MAX_EDGE_ANCHORS) {
			throw new IllegalArgumentException("at most MAX_EDGE_ANCHORS ("
					+ MAX_EDGE_ANCHORS + ") edge anchors can be specified");
		}
	}

	private final void drawSimpleEdgeFull(final ArrowShape arrow0Type,
			final float arrow0Size, final Paint arrow0Paint,
			final ArrowShape arrow1Type, final float arrow1Size,
			final Paint arrow1Paint, final float x0, final float y0,
			final float x1, final float y1, final float edgeThickness,
			final Stroke edgeStroke,
			final Paint edgePaint) {
		final double len = Math.sqrt(((((double) x1) - x0) * (((double) x1) - x0))
		                           + ((((double) y1) - y0) * (((double) y1) - y0)));

		// If the length of the edge is zero we're going to skip completely over
		// all rendering. This check is now redundant because the code that
		// calls us makes this check automatically.
		if (len == 0.0d) 
			return;

		final double x0Adj;
		final double y0Adj;
		final double x1Adj;
		final double y1Adj;
		final byte simpleSegment;

		// Render the line segment if necessary.

		final double t0 = (getT(arrow0Type) * arrow0Size) / len;
		x0Adj = (t0 * (((double) x1) - x0)) + x0;
		y0Adj = (t0 * (((double) y1) - y0)) + y0;

		final double t1 = (getT(arrow1Type) * arrow1Size) / len;
		x1Adj = (t1 * (((double) x0) - x1)) + x1;
		y1Adj = (t1 * (((double) y0) - y1)) + y1;

		// If the vector point0->point1 is pointing opposite to
		// adj0->adj1, then don't render the line segment.
		// Dot product determines this.
		if ((((((double) x1) - x0) * (x1Adj - x0Adj)) + 
		     ((((double) y1) - y0) * (y1Adj - y0Adj))) > 0.0d) {
				                
			if (arrow0Type == ArrowShapeVisualProperty.NONE && arrow1Type == ArrowShapeVisualProperty.NONE) {
				simpleSegment = 1; 
			} else { 
				simpleSegment = -1; 
			}

			m_g2d.setStroke(edgeStroke);
			m_line2d.setLine(x0Adj, y0Adj, x1Adj, y1Adj);
			m_g2d.setPaint(edgePaint);
			m_g2d.draw(m_line2d);

			if ( simpleSegment > 0 )
				return;

		} else {
			simpleSegment = 0; // Did not render segment.
		}

		// End rendering of line segment."

		// Using x0, x1, y0, and y1 instead of the "adjusted" endpoints is
		// accurate enough in computation of cosine and sine because the
		// length is guaranteed to be at least as large. Remember that the
		// original endpoint values are specified as float whereas the adjusted
		// points are double.
		final double cosTheta = (((double) x0) - x1) / len;
		final double sinTheta = (((double) y0) - y1) / len;
	
		if ( simpleSegment < 0 && edgeStroke instanceof BasicStroke ) { 
			// Arrow cap at point 0.
			final Shape arrow0Cap = computeUntransformedArrowCap(arrow0Type, ((double) arrow0Size) / edgeThickness);

			if (arrow0Cap != null) {
				m_xformUtil.setTransform(cosTheta, sinTheta, -sinTheta, cosTheta, x0Adj, y0Adj);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(arrow0Cap);
				m_g2d.setTransform(m_currNativeXform);
			}

			// Arrow cap at point 1.
			final Shape arrow1Cap = computeUntransformedArrowCap(arrow1Type, ((double) arrow1Size) / edgeThickness);

			if (arrow1Cap != null) {
				m_xformUtil.setTransform(-cosTheta, -sinTheta, sinTheta, -cosTheta, x1Adj, y1Adj);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(arrow1Cap);
				m_g2d.setTransform(m_currNativeXform);
			}
		}

		// Render arrow at point 0.
		final Shape arrow0 = computeUntransformedArrow(arrow0Type);

		if (arrow0 != null) {
			m_xformUtil.setTransform(cosTheta, sinTheta, -sinTheta, cosTheta, x0, y0);
			m_g2d.transform(m_xformUtil);
			m_g2d.scale(arrow0Size, arrow0Size);
			m_g2d.setPaint(arrow0Paint);
			boolean filled = arrow0Type.isFilled();
			if (filled)
				m_g2d.fill(arrow0);
			else
			{
				m_g2d.setStroke(new BasicStroke(0.25f));
				if ( arrow0Type == ArrowShapeVisualProperty.OPEN_CIRCLE )
					m_g2d.setStroke(new BasicStroke(0.1f));
				m_g2d.draw(arrow0);
			}
			m_g2d.setTransform(m_currNativeXform);
		}

		// Render arrow at point 1.
		final Shape arrow1 = computeUntransformedArrow(arrow1Type);

		if (arrow1 != null) {
			m_xformUtil.setTransform(-cosTheta, -sinTheta, sinTheta, -cosTheta, x1, y1);
			m_g2d.transform(m_xformUtil);
			m_g2d.scale(arrow1Size, arrow1Size);
			m_g2d.setPaint(arrow1Paint);
			boolean filled = arrow1Type.isFilled();
			if (filled)	
				m_g2d.fill(arrow1);
			else
			{
				m_g2d.setStroke(new BasicStroke(0.25f));
				if ( arrow1Type  == ArrowShapeVisualProperty.OPEN_CIRCLE )
					m_g2d.setStroke(new BasicStroke(0.1f));
				m_g2d.draw(arrow1);
			}
			m_g2d.setTransform(m_currNativeXform);
		}
	}

	/**
	 * Computes the path that an edge takes; this method is useful if a user
	 * interface would allow user selection of edges, for example. The returned
	 * path is the path along the center of the edge segment, extending to the
	 * points which specify the arrow locations. Note that this path therefore
	 * disregards edge thickness and arrow outline. Use the same parameter
	 * values that were used to render corresponding edge.
	 * 
	 * @param arrow0Type
	 *            the type of arrow shape used for drawing the arrow at point
	 *            (x0, y0); this value must be one of the ARROW_* constants.
	 * @param arrow0Size
	 *            the size of arrow at point (x0, y0).
	 * @param arrow1Type
	 *            the type of arrow shape used for drawing the arrow at point
	 *            (x1, y1); this value must be one of the ARROW_* constants.
	 * @param arrow1Size
	 *            the size of arrow at point (x1, y1).
	 * @param x0
	 *            the X coordinate of the first edge endpoint.
	 * @param y0
	 *            the Y coordinate of the first edge endpoint.
	 * @param anchors
	 *            anchor points between the two edge endpoints; null is an
	 *            acceptable value to indicate no edge anchors.
	 * @param x1
	 *            the X coordinate of the second edge endpoint.
	 * @param y1
	 *            the Y coordinate of the second edge endpoint.
	 * @param path
	 *            the computed path is returned in this parameter; the computed
	 *            path's coordinate system is the node coordinate system; the
	 *            computed path is not closed.
	 * @return true if and only if the specified edge would be drawn (which is
	 *         if and only if any two points from the edge anchor set plus the
	 *         beginning and end point are distinct); if false is returned, the
	 *         path parameter is not modified.
	 * @exception IllegalArgumentException
	 *                if any one of the edge arrow criteria specified in
	 *                drawEdgeFull() is not satisfied.
	 */
	// MKTODO does this still need to be static after the refactoring???
	public static boolean getEdgePath(ArrowShape arrow0Type, float arrow0Size, ArrowShape arrow1Type,
			float arrow1Size, float x0, float y0, EdgeAnchors anchors, float x1, float y1, GeneralPath path) {
		final double curveFactor = CURVE_ELLIPTICAL;

		if (anchors == null) {
			anchors = m_noAnchors;
		}

		ArrowShape arrow0 = arrow0Type;
		ArrowShape arrow1 = arrow1Type;

		final double[] edgePtsBuff = new double[(MAX_EDGE_ANCHORS + 1) * 6];
		final float[] floatBuff = new float[2];
		
		int edgePtsCount = computeCubicPolyEdgePath(edgePtsBuff, floatBuff, arrow0, (arrow0 == ArrowShapeVisualProperty.NONE) ? 0.0f
				: arrow0Size, arrow1, (arrow1 == ArrowShapeVisualProperty.NONE) ? 0.0f
				: arrow1Size, x0, y0, anchors, x1, y1, curveFactor);
		
		if (edgePtsCount < 3) {
			// After filtering duplicate start and end points, there are less then 3 total.
			if (edgePtsCount == 2) {
				path.reset();
				path.moveTo((float) edgePtsBuff[0], (float) edgePtsBuff[1]);
				path.lineTo((float) edgePtsBuff[2], (float) edgePtsBuff[3]);
				return true;
			}
			return false;
		}

		path.reset();
		path.moveTo((float) edgePtsBuff[0], (float) edgePtsBuff[1]);
		path.lineTo((float) edgePtsBuff[2], (float) edgePtsBuff[3]);

		int inx = 4;
		final int count = ((edgePtsCount - 1) * 6) - 2;

		while (inx < count) {
			path.curveTo((float) edgePtsBuff[inx++],
					(float) edgePtsBuff[inx++], (float) edgePtsBuff[inx++],
					(float) edgePtsBuff[inx++], (float) edgePtsBuff[inx++],
					(float) edgePtsBuff[inx++]);
		}

		path.lineTo((float) edgePtsBuff[count], (float) edgePtsBuff[count + 1]);
		return true;
	}

	/*
	 * Returns non-null if and only if an arrow is necessary for the arrow type
	 * specified. 
	 */
	private final Shape computeUntransformedArrow(final ArrowShape arrowType) {
		Arrow a = arrows.get(arrowType);
		return a == null ? null : a.getArrowShape();
	}

	/*
	 * The ratio parameter specifies the ratio of arrow size (disc diameter) to
	 * edge thickness (only used for some arrow types). Returns non-null if and
	 * only if a cap is necessary for the arrow type specified. 
	 */
	private final Shape computeUntransformedArrowCap(final ArrowShape arrowType, final double ratio) {
		Arrow a = arrows.get(arrowType);
		return a == null ? null : a.getCapShape(ratio);
	}

	/*
	 * 
	 */
	private final static double getT(final ArrowShape arrowType) { 
		Arrow a = arrows.get(arrowType);
		return a == null ? 0.125 : a.getTOffset();
	}

	/*
	 * If arrow0Type is NONE, arrow0Size should be zero. If arrow1Type is
	 * NONE, arrow1Size should be zero.
	 */
	private static int computeCubicPolyEdgePath(double[] edgePtsBuff, float[] floatBuff,
			ArrowShape arrow0Type, float arrow0Size, ArrowShape arrow1Type, float arrow1Size, float x0, float y0,
			EdgeAnchors anchors, float x1, float y1, double curveFactor) {
		
		final int numAnchors = anchors.numAnchors();
		
		// add the start point to the edge points buffer
		edgePtsBuff[0] = x0;
		edgePtsBuff[1] = y0;
		int edgePtsCount = 1;

		int anchorInx = 0;

		// finds the first anchor point other than the start point and
		// add it to the edge points buffer
		while (anchorInx < numAnchors) {
			anchors.getAnchor(anchorInx++, floatBuff);
			if (!((floatBuff[0] == x0) && (floatBuff[1] == y0))) {
				edgePtsBuff[2] = floatBuff[0];
				edgePtsBuff[3] = floatBuff[1];
				edgePtsCount = 2;
				break;
			}
		}

		// now fill edge points buffer with all subsequent anchors
		while (anchorInx < numAnchors) {
			anchors.getAnchor(anchorInx++, floatBuff);
			// Duplicate anchors are allowed.
			edgePtsBuff[edgePtsCount * 2] = floatBuff[0];
			edgePtsBuff[edgePtsCount * 2 + 1] = floatBuff[1];
			edgePtsCount++;
		}

		// now add the end point to the buffer
		edgePtsBuff[edgePtsCount * 2] = x1;
		edgePtsBuff[edgePtsCount * 2 + 1] = y1;
		edgePtsCount++;

		// remove duplicate end points from edge buffer
		while (edgePtsCount > 1) {
			// second-to-last X coord and  second-to-last Y coord.
			if ((edgePtsBuff[edgePtsCount * 2 - 2] == edgePtsBuff[edgePtsCount * 2 - 4]) 
					&& (edgePtsBuff[edgePtsCount * 2 - 1] == edgePtsBuff[edgePtsCount * 2 - 3])) { 
				edgePtsCount--;
			} else {
				break;
			}
		}

		// no anchors, just a straight line to draw 
		if (edgePtsCount < 3) {
			return edgePtsCount;
		}

		//
		// ok, now we're drawing a curve
		//

		final int edgePtsCountToReturn = edgePtsCount;

		// First set the three control points related to point 1.
		// 6 represents the offset in the buffer.
		{ 
			edgePtsCount--;
			// set first control point
			edgePtsBuff[(edgePtsCount * 6) - 2] = edgePtsBuff[edgePtsCount * 2];
			edgePtsBuff[(edgePtsCount * 6) - 1] = edgePtsBuff[edgePtsCount * 2 + 1];

			double dx = edgePtsBuff[edgePtsCount * 2 - 2] - edgePtsBuff[edgePtsCount * 2];
			double dy = edgePtsBuff[edgePtsCount * 2 - 1] - edgePtsBuff[edgePtsCount * 2 + 1];
			double len = Math.sqrt((dx * dx) + (dy * dy));
			// Normalized.
			dx /= len;
			dy /= len; 

			// set second control point
			edgePtsBuff[edgePtsCount * 6 - 4] = edgePtsBuff[edgePtsCount * 6 - 2] + (dx * arrow1Size * getT(arrow1Type));
			edgePtsBuff[edgePtsCount * 6 - 3] = edgePtsBuff[edgePtsCount * 6 - 1] + (dy * arrow1Size * getT(arrow1Type));

			// one candidate point is offset by the arrow (candX1) and 
			// the other is offset by the curvefactor (candX2)
			double candX1 = edgePtsBuff[edgePtsCount * 6 - 4] + (dx * 2.0d * arrow1Size);
			double candX2 = edgePtsBuff[edgePtsCount * 6 - 4] + (curveFactor * (edgePtsBuff[edgePtsCount * 2 - 2] - edgePtsBuff[edgePtsCount * 6 - 4]));

			// set third control point X coord
			// choose the candidate with max offset
			if (Math.abs(candX1 - edgePtsBuff[edgePtsCount * 2]) > Math.abs(candX2 - edgePtsBuff[edgePtsCount * 2])) {
				edgePtsBuff[(edgePtsCount * 6) - 6] = candX1;
			} else {
				edgePtsBuff[(edgePtsCount * 6) - 6] = candX2;
			}

			// one candidate point is offset by the arrow (candY1) and 
			// the other is offset by the curvefactor (candY2)
			double candY1 = edgePtsBuff[edgePtsCount * 6 - 3] + (dy * 2.0d * arrow1Size);
			double candY2 = edgePtsBuff[edgePtsCount * 6 - 3] + (curveFactor * (edgePtsBuff[edgePtsCount * 2 - 1] - edgePtsBuff[edgePtsCount * 6 - 3]));

			// set third control point Y coord
			// choose the candidate with max offset
			if (Math.abs(candY1 - edgePtsBuff[edgePtsCount * 2 + 1]) > Math.abs(candY2 - edgePtsBuff[edgePtsCount * 2 + 1])) {
				edgePtsBuff[edgePtsCount * 6 - 5] = candY1;
			} else {
				edgePtsBuff[edgePtsCount * 6 - 5] = candY2;
			}
		}

		// Next set the control point for each edge anchor. 
		while (edgePtsCount > 2) {
			edgePtsCount--;

			final double midX = (edgePtsBuff[edgePtsCount * 2 - 2] + edgePtsBuff[edgePtsCount * 2]) / 2.0d;
			final double midY = (edgePtsBuff[edgePtsCount * 2 - 1] + edgePtsBuff[edgePtsCount * 2 + 1]) / 2.0d;
			edgePtsBuff[edgePtsCount * 6 - 2] = midX + ((edgePtsBuff[edgePtsCount * 2] - midX) * curveFactor);
			edgePtsBuff[edgePtsCount * 6 - 1] = midY + ((edgePtsBuff[edgePtsCount * 2 + 1] - midY) * curveFactor);
			edgePtsBuff[edgePtsCount * 6 - 4] = midX;
			edgePtsBuff[edgePtsCount * 6 - 3] = midY;
			edgePtsBuff[edgePtsCount * 6 - 6] = midX + ((edgePtsBuff[edgePtsCount * 2 - 2] - midX) * curveFactor);
			edgePtsBuff[edgePtsCount * 6 - 5] = midY + ((edgePtsBuff[edgePtsCount * 2 - 1] - midY) * curveFactor);
		}

		{ // Last set the three control points related to point 0.

			double dx = edgePtsBuff[2] - edgePtsBuff[0];
			double dy = edgePtsBuff[3] - edgePtsBuff[1];
			double len = Math.sqrt((dx * dx) + (dy * dy));
			// Normalized.
			dx /= len;
			dy /= len; 

			double segStartX = edgePtsBuff[0] + (dx * arrow0Size * getT(arrow0Type));
			double segStartY = edgePtsBuff[1] + (dy * arrow0Size * getT(arrow0Type));
			double candX1 = segStartX + (dx * 2.0d * arrow0Size);
			double candX2 = segStartX + (curveFactor * (edgePtsBuff[2] - segStartX));

			if (Math.abs(candX1 - edgePtsBuff[0]) > Math.abs(candX2 - edgePtsBuff[0])) {
				edgePtsBuff[4] = candX1;
			} else {
				edgePtsBuff[4] = candX2;
			}

			double candY1 = segStartY + (dy * 2.0d * arrow0Size);
			double candY2 = segStartY + (curveFactor * (edgePtsBuff[3] - segStartY));

			if (Math.abs(candY1 - edgePtsBuff[1]) > Math.abs(candY2 - edgePtsBuff[1])) {
				edgePtsBuff[5] = candY1;
			} else {
				edgePtsBuff[5] = candY2;
			}

			edgePtsBuff[2] = segStartX;
			edgePtsBuff[3] = segStartY;
		}
		
		return edgePtsCountToReturn;
	}

	/**
	 * Computes the intersection point between a node outline and a line
	 * segment; one point of the line segment lies at the center of the node
	 * outline.
	 * <p>
	 * There is a constraint that only applies to SHAPE_ROUNDED_RECTANGLE which
	 * imposes that the maximum of the width and height be strictly less than
	 * twice the minimum of the width and height of the node.
	 * 
	 * @param nodeShape
	 *            the shape of the node in question; this must be one of the
	 *            SHAPE_* constants or a custom node shape.
	 * @param xMin
	 *            an extent of the node in question, in node coordinate space.
	 * @param yMin
	 *            an extent of the node in question, in node coordinate space.
	 * @param xMax
	 *            an extent of the node in question, in node coordinate space.
	 * @param yMax
	 *            an extent of the node in question, in node coordinate space.
	 * @param offset
	 *            most of the time this value will be zero, in which case the
	 *            point computed is the exact intersection point of line segment
	 *            and node outline; if this value is greater than zero, the
	 *            point computed is one that lies on the line segment, is
	 *            "outside" of the node outline, and is distance offset from the
	 *            node outline.
	 * @param ptX
	 *            specifies the X coordinate of the endpoint of the line segment
	 *            that is not the endpoint lying in the center of the node
	 *            shape.
	 * @param ptY
	 *            specifies the Y coordinate of the endpoint of the line segment
	 *            that is not the endpoint lying in the center of the node
	 *            shape.
	 * @param returnVal
	 *            if true is returned, returnVal[0] is set to be the X
	 *            coordinate of the computed point and returnVal[1] is set to be
	 *            the Y coordinate of the computed point; if false is returned,
	 *            this array is not modified.
	 * @return true if and only if a point matching our criteria exists.
	 * @exception IllegalArgumentException
	 *                if xMin is not less than xMax or if yMin is not less than
	 *                yMax, if offset is negative, if nodeShape is
	 *                SHAPE_ROUNDED_RECTANGLE and the condition max(width,
	 *                height) < 2 * min(width, height) does not hold, or if
	 *                nodeShape is neither one of the SHAPE_* constants nor a
	 *                previously defined custom node shape.
	 */
	public static final boolean computeEdgeIntersection(byte nodeShape, float xMin, float yMin, float xMax,
			float yMax, float offset, float ptX, float ptY, float[] returnVal) {
		NodeShape ns = nodeShapes.get(nodeShape);
		return ns == null ? false : ns.computeEdgeIntersection(xMin, yMin, xMax, yMax, ptX, ptY, returnVal);
	}


	/**
	 * Returns the context that is used by drawTextLow() to produce text shapes
	 * to be drawn to the screen. The transform contained in the returned
	 * context specifies the <i>only</i> scaling that will be done to fonts
	 * when rendering text using drawTextLow(). This transform does not change
	 * between frames no matter what scaling factor is specified in clear().
	 */
	public final FontRenderContext getFontRenderContextLow() {
		return m_gMinimal.getFontRenderContext();
	}

	/**
	 * Renders text onto the underlying image with medium to high detail,
	 * depending on parameters specified.
	 * 
	 * @param font
	 *            the font to be used when rendering specified text; the
	 *            rendering of glyph shapes generated by this font (using the
	 *            context returned by getFontRenderContextFull()) is subjected
	 *            to the same transformation as are nodes and edges (the
	 *            transformation is defined by the clear() method); therefore,
	 *            it is the point size of the font in addition to the scaling
	 *            factor defined by clear() that determines the resulting size
	 *            of text rendered to the graphics context.
	 * @param scaleFactor
	 *            in order to prevent very small fonts from "degenerating" it is
	 *            necessary to allow users to specify a scale factor in addition
	 *            to the font size and transform defined by the clear() method;
	 *            if this value is 1.0, no additional scaling is performed;
	 *            otherwise, the size of the font is multiplied by this value to
	 *            yield a new virtual font size.
	 * @param text
	 *            the text to render.
	 * @param xCenter
	 *            the text string is drawn such that its logical bounds
	 *            rectangle with specified font is centered on this X coordinate
	 *            (in the node coordinate system).
	 * @param yCenter
	 *            the text string is drawn such that its logical bounds
	 *            rectangle with specified font is centered on this Y coordinate
	 *            (in the node coordinate system).
	 * @param theta
	 *            in radians, specifies the angle of the text.
	 * @param paint
	 *            the paint to use in rendering the text.
	 * @param drawTextAsShape
	 *            this flag controls the way that text is drawn to the
	 *            underlying graphics context; by default, all text rendering
	 *            operations involve calling the operation
	 *            Graphics2D.drawString(String, float, float) after setting
	 *            specified font in the underlying graphics context; however, if
	 *            this flag is set, the text to be rendered is converted to a
	 *            primitive shape using font specified, and this shape is then
	 *            rendered using Graphics2D.fill(Shape); on some systems, the
	 *            shape filling method produces better-looking results when the
	 *            graphics context is associated with an image that is to be
	 *            rendered to the screen; however, on all systems tested, the
	 *            shape filling method results in a substantial performance hit
	 *            when the graphics context is associated with an image that is
	 *            to be rendered to the screen; it is recommended to set this
	 *            flag to true when either not much text is being rendered or
	 *            when the zoom level is very high; the Graphics2D.drawString()
	 *            operation has a difficult time when it needs to render text
	 *            under a transformation with a very large scale factor.
	 */

	public final void drawTextFull(LabelLineInfo labelLineInfo,
			final float xCenter, final float yCenter,
			final double xAnchor, final double yAnchor,
			final float theta, final Paint paint, 
			final Paint backgroundPaint, final byte backgroundShape, 
			final boolean drawTextAsShape) {

		if (theta != 0.0f) {
			// m_g2d.rotate(theta, (double)xCenter-xAnchor, (double)yCenter-yAnchor);
			m_g2d.rotate(theta, xAnchor, yAnchor);
		}
		
		m_g2d.translate(xCenter, yCenter);
		m_g2d.setPaint(paint);

		// NOTE: Java 7 seems to have broken the antialiasing of text
		// on translucent backgrounds.  In our case, the network canvas
		// is transparent, so we fall into this category.  For the monment
		// the "drawTextAsShape" path is the default path as it avoids
		// this problem.
		if (drawTextAsShape) {
			GlyphVector glyphV = labelLineInfo.getGlyphVector();
			Rectangle2D glyphBounds = glyphV.getLogicalBounds();
			m_g2d.translate(-glyphBounds.getCenterX(), -glyphBounds.getCenterY());
			drawLabelBackground(glyphBounds, backgroundPaint, backgroundShape);
			m_g2d.fill(labelLineInfo.getShape());
		} else {
			// Note: A new Rectangle2D is being constructed by this method call.
			// As far as I know this performance hit is unavoidable.
			String text = labelLineInfo.getText();
			Font font = labelLineInfo.getFont();
			Rectangle2D textBounds = font.getStringBounds(text, getFontRenderContextFull());
			m_g2d.translate(-textBounds.getCenterX(), -textBounds.getCenterY());
			drawLabelBackground(textBounds, backgroundPaint, backgroundShape);
			m_g2d.setFont(font);
			m_g2d.drawString(text, 0.0f, 0.0f);
		}
		
		m_g2d.setTransform(m_currNativeXform);
	}
	
	private void drawLabelBackground(Rectangle2D bounds, Paint paint, byte shape) {
		if(shape >= 0) {
			Graphics2D g_back = (Graphics2D) m_g2d.create();
			float padding = 0.0f; 
			Shape bgs = getShape(shape, 
					(float)bounds.getMinX()-padding, (float)bounds.getMinY()-padding,
					(float)bounds.getMaxX()+padding, (float)bounds.getMaxY()+padding);
			g_back.setPaint(paint);
			g_back.fill(bgs);
			g_back.dispose();
		}
	}
	

	/**
	 * Returns the context that is used by drawTextFull() to produce text shapes
	 * to be drawn to the screen. This context always has the identity
	 * transform.
	 */
	public final FontRenderContext getFontRenderContextFull() {
		return m_fontRenderContextFull;
	}

	public final void drawCustomGraphicImage(final Shape shape,
			final float xOffset, final float yOffset, final TexturePaint paint) {
		m_g2d.translate(xOffset, yOffset);
		if(paint instanceof TexturePaint) {
			final BufferedImage bImg = ((TexturePaint) paint).getImage();
			Rectangle bounds = shape.getBounds2D().getBounds();
			m_g2d.drawImage(bImg, bounds.x, bounds.y, bounds.width, bounds.height, null);
		}
		m_g2d.setTransform(m_currNativeXform);
	}

	/**
	 * Fills an arbitrary graphical shape with high detail.
	 * <p>
	 * This method will not work unless clear() has been called at least once
	 * previously.
	 * 
	 * @param nodeShape
	 *            the node shape
	 * @param cg
	 *            the CustomGraphicLayer
	 * @param xOffset
	 *            in node coordinates, a value to add to the X coordinates of
	 *            the shape's definition.
	 * @param yOffset
	 *            in node coordinates, a value to add to the Y coordinates of
	 *            the shape's definition.
	 */
	public final void drawCustomGraphicFull(
			CyNetworkView netView,
			View<CyNode> node,
			Shape nodeShape,
			CustomGraphicLayer cg,
			float xOffset,
			float yOffset
	) {
		m_g2d.translate(xOffset, yOffset);
		
		if (cg instanceof PaintedShape) {
			var ps = (PaintedShape) cg;
			var shape = ps.getShape();

			if (ps.getStroke() != null) {
				var strokePaint = ps.getStrokePaint();

				if (strokePaint == null)
					strokePaint = Color.BLACK;

				m_g2d.setPaint(strokePaint);
				m_g2d.setStroke(ps.getStroke());
				m_g2d.draw(shape);
			}

			m_g2d.setPaint(ps.getPaint());
			m_g2d.fill(shape);
		} else if (cg instanceof Cy2DGraphicLayer) {
			var layer = (Cy2DGraphicLayer) cg;
			layer.draw(m_g2d, nodeShape, netView, node);
		} else if (cg instanceof ImageCustomGraphicLayer) {
			var bounds = cg.getBounds2D().getBounds();
			var img = ((ImageCustomGraphicLayer) cg).getPaint(bounds).getImage();
			m_g2d.drawImage(img, bounds.x, bounds.y, bounds.width, bounds.height, null);
		} else {
			var bounds = nodeShape.getBounds2D();
			m_g2d.setPaint(cg.getPaint(bounds));
			m_g2d.fill(nodeShape);
		}

		m_g2d.setTransform(m_currNativeXform);
	}

	/**
	 * Create border stroke for given width value.
	 * 
	 * @param borderWidth
	 * @return Actual node border stroke
	 */
	private final Stroke getStroke(final float borderWidth) {
		Stroke s = borderStrokes.get(borderWidth);
		if ( s == null ) {
			s = new BasicStroke(borderWidth);
			borderStrokes.put(borderWidth, s);
		}
		return s; 
	}


	private void checkOrder(float min, float max, String id) {
		if (!(min < max)) 
			throw new IllegalArgumentException( id + "Min not less than " + id + "Max");
	}
	
}
