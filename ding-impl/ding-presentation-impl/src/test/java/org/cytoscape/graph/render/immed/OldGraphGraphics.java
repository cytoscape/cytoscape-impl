package org.cytoscape.graph.render.immed;

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


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;


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
public final class OldGraphGraphics {
	/**
	 * 
	 */
	public static final byte SHAPE_RECTANGLE = 0;

	/**
	 * 
	 */
	public static final byte SHAPE_DIAMOND = 1;

	/**
	 * 
	 */
	public static final byte SHAPE_ELLIPSE = 2;

	/**
	 * 
	 */
	public static final byte SHAPE_HEXAGON = 3;

	/**
	 * 
	 */
	public static final byte SHAPE_OCTAGON = 4;

	/**
	 * 
	 */
	public static final byte SHAPE_PARALLELOGRAM = 5;

	/**
	 * 
	 */
	public static final byte SHAPE_ROUNDED_RECTANGLE = 6;

	/**
	 * 
	 */
	public static final byte SHAPE_TRIANGLE = 7;

	/**
	 * 
	 */
	public static final byte SHAPE_VEE = 8;

	// package scoped for unit testing
	static final byte s_last_shape = SHAPE_VEE;

	/**
	 * This value is currently 100.
	 */
	public static final int CUSTOM_SHAPE_MAX_VERTICES = 100;

	/**
	 * 
	 */
	public static final byte ARROW_NONE = -1;

	/**
	 * 
	 */
	public static final byte ARROW_DELTA = -2;

	/**
	 * 
	 */
	public static final byte ARROW_DIAMOND = -3;

	/**
	 * 
	 */
	public static final byte ARROW_DISC = -4;

	/**
	 * 
	 */
	public static final byte ARROW_TEE = -5;
	private static final byte last_arrow_shape = ARROW_TEE;

	/**
	 * 
	 */
	public static final byte ARROW_BIDIRECTIONAL = -6;

	/**
	 * 
	 */
	public static final byte ARROW_MONO = -7;

	/**
	 * This value is currently 64.
	 */
	public static final int MAX_EDGE_ANCHORS = 64;

	/*
	 * A constant for controlling how cubic Bezier curves are drawn; This
	 * particular constant results in elliptical-looking curves.
	 */
	private static final double CURVE_ELLIPTICAL = (4.0d * (Math.sqrt(2.0d) - 1.0d)) / 3.0d;

	/*
	 * Added by kono: This is for returning shapes
	 */
	private static final OldGraphGraphics dummyGraphics;

	static {
		dummyGraphics = new OldGraphGraphics(null, false);
	}

	private static final double DEF_SHAPE_SIZE = 32;

	/**
	 * The image that was passed into the constructor.
	 */
	public final Image image;
	private final boolean m_debug;
	private final AffineTransform m_currXform = new AffineTransform();
	private final AffineTransform m_currNativeXform = new AffineTransform();
	private final AffineTransform m_xformUtil = new AffineTransform();
	private final Arc2D.Double m_arc2d = new Arc2D.Double();
	private final Ellipse2D.Double m_ellp2d = new Ellipse2D.Double();
	private final GeneralPath m_path2d = new GeneralPath();
	private final GeneralPath m_path2dPrime = new GeneralPath();
	private final Line2D.Double m_line2d = new Line2D.Double();
	private final double[] m_polyCoords = // I need this for extra precision.
	new double[2 * CUSTOM_SHAPE_MAX_VERTICES];
	private final HashMap<Byte, double[]> m_customShapes = new HashMap<Byte, double[]>();
	private final double[] m_ptsBuff = new double[4];

	// package scoped for unit testing
	final EdgeAnchors m_noAnchors = new EdgeAnchors() {
		public final int numAnchors() {
			return 0;
		}

		public final void getAnchor(final int inx, final float[] arr) {
		}
	};

	private final double[] m_edgePtsBuff = new double[(MAX_EDGE_ANCHORS + 1) * 6];
	private int m_polyNumPoints; // Used with m_polyCoords.
	private int m_edgePtsCount; // Number of points stored in m_edgePtsBuff.
	private Graphics2D m_g2d;
	private Graphics2D m_gMinimal; // We use mostly java.awt.Graphics methods.
	private boolean m_cleared;

	// The three following member variables shall only be referenced from
	// the scope of setStroke() definition.
	private float m_currStrokeWidth;
	private final float[] m_currDash = new float[] { 0.0f, 0.0f };
	private int m_currCapType;

	// This member variable only to be used from within defineCustomNodeShape().
	private byte m_lastCustomShapeType = s_last_shape;

	// This is only used by computeCubicPolyEdgePath().
	private final float[] m_floatBuff = new float[2];

	// The following three member variables shall only be accessed from the
	// scope of computeEdgeIntersection() definition.
	private final double[] m_fooPolyCoords = new double[CUSTOM_SHAPE_MAX_VERTICES * 4];
	private final double[] m_foo2PolyCoords = new double[CUSTOM_SHAPE_MAX_VERTICES * 4];
	private final boolean[] m_fooRoundedCorners = new boolean[CUSTOM_SHAPE_MAX_VERTICES];

	// This member variable shall only be used from within drawTextFull().
	private char[] m_charBuff = new char[20];
	private final FontRenderContext m_fontRenderContextFull = new FontRenderContext(
			null, true, true);

	/**
	 * All rendering operations will be performed on the specified image. No
	 * rendering operations are performed as a result of calling this
	 * constructor. It is safe to call this constructor from any thread.
	 * <p>
	 * The image argument passed to this constructor must support at least three
	 * methods: getGraphics(), getWidth(ImageObserver), and
	 * getHeight(ImageObserver). The image.getGraphics() method must return an
	 * instance of java.awt.Graphics2D. The hypothetical method calls
	 * image.getWidth(null) and image.getHeight(null) must return the
	 * corresponding dimension immediately.
	 * <p>
	 * Notice that it is not possible to resize the image area with this API.
	 * This is not a problem; instances of this class are very lightweight and
	 * are for the most part stateless; simply instantiate a new OldGraphGraphics
	 * when the image area changes.
	 * 
	 * @param image
	 *            an off-screen image; passing an image gotten from a call to
	 *            java.awt.Component.createImage(int, int) works well, although
	 *            experience shows that for full support of non-opaque colors,
	 *            java.awt.image.BufferedImage should be used instead.
	 * @param debug
	 *            if this is true, extra [and time-consuming] error checking
	 *            will take place in each method call; it is recommended to have
	 *            this value set to true during the testing phase; set it to
	 *            false once you are sure that code does not mis-use this
	 *            module.
	 */
	public OldGraphGraphics(final Image image, final boolean debug) {
		this.image = image;
		m_debug = debug;
		m_path2dPrime.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		m_cleared = false;
	}

	/**
	 * Clears image area with background paint specified and sets an appropriate
	 * transformation of coordinate systems. See the class description for a
	 * definition of the two coordinate systems: the node coordinate system and
	 * the image coordinate system.
	 * <p>
	 * The background paint is not blended with colors that may already be on
	 * the underlying image; if a translucent color is used in the background
	 * paint, the underlying image itself becomes translucent.
	 * <p>
	 * It is mandatory to call this method before making the first rendering
	 * call.
	 * 
	 * @param bgPaint
	 *            paint to use when clearing the image before painting a new
	 *            frame; translucency is honored, provided that the underlying
	 *            image supports it.
	 * @param xCenter
	 *            the X component of the translation transform for the frame
	 *            about to be rendered; a node whose center is at the X
	 *            coordinate xCenter will be rendered exactly in the middle of
	 *            the image going across; increasing X values (in the node
	 *            coordinate system) result in movement towards the right on the
	 *            image.
	 * @param yCenter
	 *            the Y component of the translation transform for the frame
	 *            about to be rendered; a node whose center is at the Y
	 *            coordinate yCenter will be rendered exactly in the middle of
	 *            the image going top to bottom; increasing Y values (in the
	 *            node coordinate system) result in movement towards the bottom
	 *            on the image.
	 * @param scaleFactor
	 *            the scaling that is to take place when rendering; a distance
	 *            of 1 in node coordinates translates to a distance of
	 *            scaleFactor in the image coordinate system (usually one unit
	 *            in the image coordinate system equates to one pixel width).
	 * @exception IllegalArgumentException
	 *                if scaleFactor is not positive.
	 */
	public final void clear(final Paint bgPaint, final double xCenter,
			final double yCenter, final double scaleFactor) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!(scaleFactor > 0.0d)) {
				throw new IllegalArgumentException(
						"scaleFactor is not positive");
			}
		}

		if (m_gMinimal != null) {
			m_gMinimal.dispose();
			m_gMinimal = null;
		}

		if (m_g2d != null) {
			m_g2d.dispose();
		}

		m_g2d = (Graphics2D) image.getGraphics();

		final Composite origComposite = m_g2d.getComposite();
		m_g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		m_g2d.setPaint(bgPaint);
		m_g2d.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
		m_g2d.setComposite(origComposite);
		m_g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		m_g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
		m_g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		m_g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		m_g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE);
		setStroke(0.0f, 0.0f, BasicStroke.CAP_ROUND, true);

		m_currXform.setToTranslation(0.5d * image.getWidth(null), 0.5d * image
				.getHeight(null));
		m_currXform.scale(scaleFactor, scaleFactor);
		m_currXform.translate(-xCenter, -yCenter);
		m_g2d.transform(m_currXform);
		m_currNativeXform.setTransform(m_g2d.getTransform());
		m_cleared = true;
	}

	private final void setStroke(final float width, final float dashLength,
			final int capType, final boolean ignoreCache) {
		if ((!ignoreCache) && (width == m_currStrokeWidth)
				&& (dashLength == m_currDash[0]) && (capType == m_currCapType)) {
			return;
		}

		m_currStrokeWidth = width;
		m_currDash[0] = dashLength;
		m_currDash[1] = dashLength;
		m_currCapType = capType;

		// Unfortunately, BasicStroke is not mutable. So we have to construct
		// lots of new strokes if they constantly change.
		if (m_currDash[0] == 0.0f) {
			m_g2d.setStroke(new BasicStroke(width, capType,
					BasicStroke.JOIN_ROUND, 10.0f));
		} else {
			m_g2d.setStroke(new BasicStroke(width, capType,
					BasicStroke.JOIN_ROUND, 10.0f, m_currDash, 0.0f));
		}
	}

	/**
	 * Uses the current transform to map the specified image coordinates to node
	 * coordinates. The transform used is defined by the last call to clear().
	 * It does not make sense to call this method if clear() has not been called
	 * at least once previously, and this method will cause errors in this case.
	 * 
	 * @param coords
	 *            an array of length [at least] two which acts both as the input
	 *            and as the output of this method; coords[0] is the input X
	 *            coordinate in the image coordinate system and is written as
	 *            the X coordinate in the node coordinate system by this method;
	 *            coords[1] is the input Y coordinate in the image coordinate
	 *            system and is written as the Y coordinate in the node
	 *            coordinate system by this method; the exact transform which
	 *            takes place is defined by the previous call to the clear()
	 *            method.
	 */
	public final void xformImageToNodeCoords(final double[] coords) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!m_cleared) {
				throw new IllegalStateException(
						"clear() has not been called previously");
			}
		}

		try {
			m_currXform.inverseTransform(coords, 0, coords, 0, 1);
		} catch (java.awt.geom.NoninvertibleTransformException e) {
			throw new RuntimeException("noninvertible matrix - cannot happen");
		}
	}

	/**
	 * Called to get the current AffineTransform Matrix.
	 * 
	 * @return AffineTransform
	 */
	public final AffineTransform getTransform() {
		return m_currXform;
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
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!m_cleared) {
				throw new IllegalStateException(
						"clear() has not been called previously");
			}

			if (!(xMin < xMax)) {
				throw new IllegalArgumentException("xMin not less than xMax");
			}

			if (!(yMin < yMax)) {
				throw new IllegalArgumentException("yMin not less than yMax");
			}

			if (fillColor.getAlpha() != 255) {
				throw new IllegalArgumentException("fillColor is not opaque");
			}
		}

		if (m_gMinimal == null) {
			makeMinimalGraphics();
		}

		// I'm transforming points manually because the resulting underlying
		// graphics pipeline used is much faster.
		m_ptsBuff[0] = xMin;
		m_ptsBuff[1] = yMin;
		m_ptsBuff[2] = xMax;
		m_ptsBuff[3] = yMax;
		m_currXform.transform(m_ptsBuff, 0, m_ptsBuff, 0, 2);

		// Here, double values outside of the range of ints will be cast to
		// the nearest int without overflow.
		final int xNot = (int) m_ptsBuff[0];
		final int yNot = (int) m_ptsBuff[1];
		final int xOne = (int) m_ptsBuff[2];
		final int yOne = (int) m_ptsBuff[3];
		m_gMinimal.setColor(fillColor);
		m_gMinimal.fillRect(xNot, yNot, Math.max(1, xOne - xNot), // Overflow
				// will
				Math.max(1, yOne - yNot)); // be problem.
	}

	/*
	 * Sets m_gMinimal.
	 */
	private final void makeMinimalGraphics() {
		m_gMinimal = (Graphics2D) image.getGraphics();
		m_gMinimal.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
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
	public final void drawNodeFull(final byte nodeShape, final float xMin,
			final float yMin, final float xMax, final float yMax,
			final Paint fillPaint, final float borderWidth,
			final Paint borderPaint) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!m_cleared) {
				throw new IllegalStateException(
						"clear() has not been called previously");
			}

			if (!(xMin < xMax)) {
				throw new IllegalArgumentException("xMin not less than xMax");
			}

			if (!(yMin < yMax)) {
				throw new IllegalArgumentException("yMin not less than yMax");
			}

			if (!(borderWidth >= 0.0f)) {
				throw new IllegalArgumentException(
						"borderWidth not zero or positive");
			}

			if (!((6.0d * borderWidth) <= Math.min(((double) xMax) - xMin,
					((double) yMax) - yMin))) {
				throw new IllegalArgumentException(
						"borderWidth is not less than the minimum of node width and node "
								+ "height divided by six");
			}

			if (nodeShape == SHAPE_ROUNDED_RECTANGLE) {
				final double width = ((double) xMax) - xMin;
				final double height = ((double) yMax) - yMin;

				if (!(Math.max(width, height) < (2.0d * Math.min(width, height)))) {
					throw new IllegalArgumentException(
							"rounded rectangle does not meet constraint "
									+ "max(width, height) < 2 * min(width, height)");
				}
			}
		}

		if (borderWidth == 0.0f) {
			m_g2d.setPaint(fillPaint);
			m_g2d.fill(getShape(nodeShape, xMin, yMin, xMax, yMax));
		} else { // There is a border.
			m_path2dPrime.reset();
			m_path2dPrime.append(getShape(nodeShape, xMin, yMin, xMax, yMax),
					false); // Make a copy, essentially.

			final Shape innerShape;

			if (nodeShape == SHAPE_ELLIPSE) {
				// TODO: Compute a more accurate inner area for ellipse +
				// border.
				innerShape = getShape(SHAPE_ELLIPSE, ((double) xMin)
						+ borderWidth, ((double) yMin) + borderWidth,
						((double) xMax) - borderWidth, ((double) yMax)
								- borderWidth);
			} else if (nodeShape == SHAPE_ROUNDED_RECTANGLE) {
				computeRoundedRectangle(((double) xMin) + borderWidth,
						((double) yMin) + borderWidth, ((double) xMax)
								- borderWidth, ((double) yMax) - borderWidth,
						(Math.max(((double) xMax) - xMin, ((double) yMax)
								- yMin) / 4.0d)
								- borderWidth, m_path2d);
				innerShape = m_path2d;
			} else {
				// A general [possibly non-convex] polygon with certain
				// restrictions: no two consecutive line segments can be
				// parallel,
				// each line segment must have nonzero length, the polygon
				// cannot
				// self-intersect, and the polygon must be clockwise
				// in the node coordinate system.
				m_path2d.reset();

				final double xNot = m_polyCoords[0];
				final double yNot = m_polyCoords[1];
				final double xOne = m_polyCoords[2];
				final double yOne = m_polyCoords[3];
				double xPrev = xNot;
				double yPrev = yNot;
				double xCurr = xOne;
				double yCurr = yOne;
				double xNext = m_polyCoords[4];
				double yNext = m_polyCoords[5];
				computeInnerPoint(m_ptsBuff, xPrev, yPrev, xCurr, yCurr, xNext,
						yNext, borderWidth);
				m_path2d.moveTo((float) m_ptsBuff[0], (float) m_ptsBuff[1]);

				int i = 6;

				while (true) {
					if (i == (m_polyNumPoints * 2)) {
						computeInnerPoint(m_ptsBuff, xCurr, yCurr, xNext,
								yNext, xNot, yNot, borderWidth);
						m_path2d.lineTo((float) m_ptsBuff[0],
								(float) m_ptsBuff[1]);
						computeInnerPoint(m_ptsBuff, xNext, yNext, xNot, yNot,
								xOne, yOne, borderWidth);
						m_path2d.lineTo((float) m_ptsBuff[0],
								(float) m_ptsBuff[1]);
						m_path2d.closePath();

						break;
					} else {
						xPrev = xCurr;
						yPrev = yCurr;
						xCurr = xNext;
						yCurr = yNext;
						xNext = m_polyCoords[i++];
						yNext = m_polyCoords[i++];
						computeInnerPoint(m_ptsBuff, xPrev, yPrev, xCurr,
								yCurr, xNext, yNext, borderWidth);
						m_path2d.lineTo((float) m_ptsBuff[0],
								(float) m_ptsBuff[1]);
					}
				}

				innerShape = m_path2d;
			}

			m_g2d.setPaint(fillPaint);
			m_g2d.fill(innerShape);

			// Render the border such that it does not overlap with the fill
			// region because translucent colors may be used. Don't do
			// things differently for opaque and translucent colors for the
			// sake of consistency.
			m_path2dPrime.append(innerShape, false);
			m_g2d.setPaint(borderPaint);
			m_g2d.fill(m_path2dPrime);
		}
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
	public final void getNodeShape(final byte nodeShape, final float xMin,
			final float yMin, final float xMax, final float yMax,
			final GeneralPath path) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!(xMin < xMax)) {
				throw new IllegalArgumentException("xMin not less than xMax");
			}

			if (!(yMin < yMax)) {
				throw new IllegalArgumentException("yMin not less than yMax");
			}

			if (nodeShape == SHAPE_ROUNDED_RECTANGLE) {
				final double width = ((double) xMax) - xMin;
				final double height = ((double) yMax) - yMin;

				if (!(Math.max(width, height) < (2.0d * Math.min(width, height)))) {
					throw new IllegalArgumentException(
							"rounded rectangle does not meet constraint "
									+ "max(width, height) < 2 * min(width, height)");
				}
			}
		}

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
	public final byte defineCustomNodeShape(final float[] coords,
			final int offset, final int vertexCount) {
		if (vertexCount > CUSTOM_SHAPE_MAX_VERTICES) {
			throw new IllegalArgumentException(
					"too many vertices (greater than "
							+ CUSTOM_SHAPE_MAX_VERTICES + ")");
		}

		final double[] polyCoords;

		{
			polyCoords = new double[vertexCount * 2];

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

			if (xDist == 0.0d) {
				throw new IllegalArgumentException(
						"polygon does not move in the X direction");
			}

			final double yDist = yMax - yMin;

			if (yDist == 0.0d) {
				throw new IllegalArgumentException(
						"polygon does not move in the Y direction");
			}

			final double xMid = (xMin + xMax) / 2.0d;
			final double yMid = (yMin + yMax) / 2.0d;

			for (int i = 0; i < polyCoords.length;) {
				double foo = (polyCoords[i] - xMid) / xDist;
				polyCoords[i++] = Math.min(Math.max(-0.5d, foo), 0.5d);
				foo = (polyCoords[i] - yMid) / yDist;
				polyCoords[i++] = Math.min(Math.max(-0.5d, foo), 0.5d);
			}
		}

		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}
		}

		{ // Test all criteria regardless of m_debug.

			int yInterceptsCenter = 0;

			for (int i = 0; i < vertexCount; i++) {
				final double x0 = polyCoords[i * 2];
				final double y0 = polyCoords[(i * 2) + 1];
				final double x1 = polyCoords[((i * 2) + 2) % (vertexCount * 2)];
				final double y1 = polyCoords[((i * 2) + 3) % (vertexCount * 2)];
				final double x2 = polyCoords[((i * 2) + 4) % (vertexCount * 2)];
				final double y2 = polyCoords[((i * 2) + 5) % (vertexCount * 2)];
				final double distP0P1 = Math.sqrt(((x1 - x0) * (x1 - x0))
						+ ((y1 - y0) * (y1 - y0)));

				if ((float) distP0P1 == 0.0f) { // Too close to distance zero.
					throw new IllegalArgumentException(
							"a line segment has distance [too close to] zero");
				}

				final double distP2fromP0P1 = ((((y0 - y1) * x2)
						+ ((x1 - x0) * y2) + (x0 * y1)) - (x1 * y0))
						/ distP0P1;

				if ((float) distP2fromP0P1 == 0.0f) { // Too close to
					// parallel.
					throw new IllegalArgumentException(
							"either a line segment has distance [too close to] zero or "
									+ "two consecutive line segments are [too close to] parallel");
				}

				final double distCenterFromP0P1 = ((x0 * y1) - (x1 * y0))
						/ distP0P1;

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
		}

		// polyCoords now contains a polygon spanning [-0.5, 0.5] X [-0.5, 0.5]
		// that passes all of the criteria.
		final byte nextCustomShapeType = (byte) (m_lastCustomShapeType + 1);

		if (nextCustomShapeType < 0) {
			throw new IllegalStateException(
					"too many custom node shapes are already defined");
		}

		m_lastCustomShapeType++;
		m_customShapes.put(new Byte(nextCustomShapeType), polyCoords);

		return nextCustomShapeType;
	}

	/**
	 * Determines whether the specified shape is a custom defined node shape.
	 */
	public final boolean customNodeShapeExists(final byte shape) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}
		}

		return (shape > s_last_shape) && (shape <= m_lastCustomShapeType);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final byte[] getCustomNodeShapes() {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}
		}

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
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}
		}

		final double[] dCoords = m_customShapes.get(new Byte(customShape));

		if (dCoords == null) {
			return null;
		}

		final float[] returnThis = new float[dCoords.length];

		for (int i = 0; i < returnThis.length; i++) {
			returnThis[i] = (float) dCoords[i];
		}

		return returnThis;
	}

	/**
	 * If this is a new instance, imports the custom node shapes from the
	 * OldGraphGraphics specified into this OldGraphGraphics.
	 * 
	 * @param grafx
	 *            custom node shapes will be imported from this OldGraphGraphics.
	 * @exception IllegalStateException
	 *                if at least one custom node shape is already defined in
	 *                this OldGraphGraphics.
	 */
	public final void importCustomNodeShapes(final OldGraphGraphics grafx) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}
		}

		// I define this error check outside the scope of m_debug because
		// clobbering existing custom node shape definitions could be major.
		if (m_lastCustomShapeType != s_last_shape) {
			throw new IllegalStateException(
					"a custom node shape is already defined in this OldGraphGraphics");
		}

		for (Map.Entry<Byte, double[]> entry : grafx.m_customShapes.entrySet()) {
			m_customShapes.put(entry.getKey(), entry.getValue());
			m_lastCustomShapeType++;
		}
	}

	/*
	 * This method has the side effect of setting m_ellp2d or m_path2d; if
	 * m_path2d is set (every case but the ellipse and rounded rectangle), then
	 * m_polyCoords and m_polyNumPoints are also set.
	 */
	private final Shape getShape(final byte nodeShape, final double xMin,
			final double yMin, final double xMax, final double yMax) {
		switch (nodeShape) {
		case SHAPE_ELLIPSE:
			m_ellp2d.setFrame(xMin, yMin, xMax - xMin, yMax - yMin);

			return m_ellp2d;

		case SHAPE_RECTANGLE:
			m_polyNumPoints = 4;
			m_polyCoords[0] = xMin;
			m_polyCoords[1] = yMin;
			m_polyCoords[2] = xMax;
			m_polyCoords[3] = yMin;
			m_polyCoords[4] = xMax;
			m_polyCoords[5] = yMax;
			m_polyCoords[6] = xMin;
			m_polyCoords[7] = yMax;

			break;

		case SHAPE_DIAMOND:
			m_polyNumPoints = 4;
			m_polyCoords[0] = (xMin + xMax) / 2.0d;
			m_polyCoords[1] = yMin;
			m_polyCoords[2] = xMax;
			m_polyCoords[3] = (yMin + yMax) / 2.0d;
			m_polyCoords[4] = (xMin + xMax) / 2.0d;
			m_polyCoords[5] = yMax;
			m_polyCoords[6] = xMin;
			m_polyCoords[7] = (yMin + yMax) / 2.0d;

			break;

		case SHAPE_HEXAGON:
			m_polyNumPoints = 6;
			m_polyCoords[0] = ((2.0d * xMin) + xMax) / 3.0d;
			m_polyCoords[1] = yMin;
			m_polyCoords[2] = ((2.0d * xMax) + xMin) / 3.0d;
			m_polyCoords[3] = yMin;
			m_polyCoords[4] = xMax;
			m_polyCoords[5] = (yMin + yMax) / 2.0d;
			m_polyCoords[6] = ((2.0d * xMax) + xMin) / 3.0d;
			m_polyCoords[7] = yMax;
			m_polyCoords[8] = ((2.0d * xMin) + xMax) / 3.0d;
			m_polyCoords[9] = yMax;
			m_polyCoords[10] = xMin;
			m_polyCoords[11] = (yMin + yMax) / 2.0d;

			break;

		case SHAPE_OCTAGON:
			m_polyNumPoints = 8;
			m_polyCoords[0] = ((2.0d * xMin) + xMax) / 3.0d;
			m_polyCoords[1] = yMin;
			m_polyCoords[2] = ((2.0d * xMax) + xMin) / 3.0d;
			m_polyCoords[3] = yMin;
			m_polyCoords[4] = xMax;
			m_polyCoords[5] = ((2.0d * yMin) + yMax) / 3.0d;
			m_polyCoords[6] = xMax;
			m_polyCoords[7] = ((2.0d * yMax) + yMin) / 3.0d;
			m_polyCoords[8] = ((2.0d * xMax) + xMin) / 3.0d;
			m_polyCoords[9] = yMax;
			m_polyCoords[10] = ((2.0d * xMin) + xMax) / 3.0d;
			m_polyCoords[11] = yMax;
			m_polyCoords[12] = xMin;
			m_polyCoords[13] = ((2.0d * yMax) + yMin) / 3.0d;
			m_polyCoords[14] = xMin;
			m_polyCoords[15] = ((2.0d * yMin) + yMax) / 3.0d;

			break;

		case SHAPE_PARALLELOGRAM:
			m_polyNumPoints = 4;
			m_polyCoords[0] = xMin;
			m_polyCoords[1] = yMin;
			m_polyCoords[2] = ((2.0d * xMax) + xMin) / 3.0d;
			m_polyCoords[3] = yMin;
			m_polyCoords[4] = xMax;
			m_polyCoords[5] = yMax;
			m_polyCoords[6] = ((2.0d * xMin) + xMax) / 3.0d;
			m_polyCoords[7] = yMax;

			break;

		case SHAPE_ROUNDED_RECTANGLE:
			// A condition that must be satisfied (pertaining to radius) is that
			// max(width, height) <= 2 * min(width, height).
			computeRoundedRectangle(xMin, yMin, xMax, yMax, Math.max(xMax
					- xMin, yMax - yMin) / 4.0d, m_path2d);

			return m_path2d;

		case SHAPE_TRIANGLE:
			m_polyNumPoints = 3;
			m_polyCoords[0] = (xMin + xMax) / 2.0d;
			m_polyCoords[1] = yMin;
			m_polyCoords[2] = xMax;
			m_polyCoords[3] = yMax;
			m_polyCoords[4] = xMin;
			m_polyCoords[5] = yMax;

			break;

		case SHAPE_VEE:
			m_polyNumPoints = 4;
			m_polyCoords[0] = xMin;
			m_polyCoords[1] = yMin;
			m_polyCoords[2] = (xMin + xMax) / 2.0d;
			m_polyCoords[3] = ((2.0d * yMin) + yMax) / 3.0d;
			m_polyCoords[4] = xMax;
			m_polyCoords[5] = yMin;
			m_polyCoords[6] = (xMin + xMax) / 2.0d;
			m_polyCoords[7] = yMax;

			break;

		default: // Try a custom node shape or throw an exception.

			final double[] storedPolyCoords = // To optimize don't construct
			// Byte.
			m_customShapes.get(new Byte(nodeShape));

			if (storedPolyCoords == null) {
				throw new IllegalArgumentException(
						"nodeShape is not recognized");
			}

			m_polyNumPoints = storedPolyCoords.length / 2;

			final double desiredXCenter = (xMin + xMax) / 2.0d;
			final double desiredYCenter = (yMin + yMax) / 2.0d;
			final double desiredWidth = xMax - xMin;
			final double desiredHeight = yMax - yMin;
			m_xformUtil.setToTranslation(desiredXCenter, desiredYCenter);
			m_xformUtil.scale(desiredWidth, desiredHeight);
			m_xformUtil.transform(storedPolyCoords, 0, m_polyCoords, 0,
					m_polyNumPoints);

			break;
		}

		m_path2d.reset();

		m_path2d.moveTo((float) m_polyCoords[0], (float) m_polyCoords[1]);

		for (int i = 2; i < (m_polyNumPoints * 2);)
			m_path2d.lineTo((float) m_polyCoords[i++],
					(float) m_polyCoords[i++]);

		m_path2d.closePath();

		return m_path2d;
	}

	/**
	 * get list of node shapes.
	 * 
	 * @return
	 */
	public static Map<Byte, Shape> getNodeShapes() {
		return getShapes(ShapeTypes.NODE_SHAPE);
	}

	/**
	 * Get list of arrow heads.
	 * 
	 * @return
	 */
	public static Map<Byte, Shape> getArrowShapes() {
		return getShapes(ShapeTypes.ARROW_SHAPE);
	}

	/**
	 * Actually create map of shapes.
	 * 
	 * @param type
	 * @return
	 */
	private static Map<Byte, Shape> getShapes(final ShapeTypes type) {
		final Map<Byte, Shape> shapeMap = new HashMap<Byte, Shape>();

		final int minIndex;
		final int maxIndex;

		if (type == ShapeTypes.NODE_SHAPE) {
			minIndex = 0;
			maxIndex = s_last_shape;
		} else if (type == ShapeTypes.ARROW_SHAPE) {
			minIndex = last_arrow_shape;
			maxIndex = -1;
		} else {
			minIndex = 0;
			maxIndex = s_last_shape;
		}

		Shape shape;

		for (int i = minIndex; i <= maxIndex; i++) {
			if (type == ShapeTypes.NODE_SHAPE) {
				shape = dummyGraphics.getShape((byte) i, 0, 0, DEF_SHAPE_SIZE,
						DEF_SHAPE_SIZE);
			} else {
				shape = dummyGraphics.computeUntransformedArrow((byte) i);
			}

			if ((shape != null) && (shape.getClass() == GeneralPath.class)) {
				final Shape copiedShape = (Shape) ((GeneralPath) shape).clone();
				shapeMap.put((byte) i, copiedShape);
			} else if (shape != null) {
				shapeMap.put((byte) i, shape);
			}
		}

		return shapeMap;
	}

	private final static void computeRoundedRectangle(final double xMin,
			final double yMin, final double xMax, final double yMax,
			final double radius, final GeneralPath path2d) {
		path2d.reset();
		path2d.moveTo((float) (xMax - radius), (float) yMin);
		path2d.curveTo((float) (((CURVE_ELLIPTICAL - 1.0d) * radius) + xMax),
				(float) yMin, (float) xMax,
				(float) (((1.0d - CURVE_ELLIPTICAL) * radius) + yMin),
				(float) xMax, (float) (radius + yMin));
		path2d.lineTo((float) xMax, (float) (yMax - radius));
		path2d.curveTo((float) xMax,
				(float) (((CURVE_ELLIPTICAL - 1.0d) * radius) + yMax),
				(float) (((CURVE_ELLIPTICAL - 1.0d) * radius) + xMax),
				(float) yMax, (float) (xMax - radius), (float) yMax);
		path2d.lineTo((float) (radius + xMin), (float) yMax);
		path2d.curveTo((float) (((1.0d - CURVE_ELLIPTICAL) * radius) + xMin),
				(float) yMax, (float) xMin,
				(float) (((CURVE_ELLIPTICAL - 1.0d) * radius) + yMax),
				(float) xMin, (float) (yMax - radius));
		path2d.lineTo((float) xMin, (float) (radius + yMin));
		path2d.curveTo((float) xMin,
				(float) (((1.0d - CURVE_ELLIPTICAL) * radius) + yMin),
				(float) (((1.0d - CURVE_ELLIPTICAL) * radius) + xMin),
				(float) yMin, (float) (radius + xMin), (float) yMin);
		path2d.closePath();
	}

	/*
	 * This method is used to construct an inner shape for node border.
	 * output[0] is the x return value and output[1] is the y return value. The
	 * line prev->curr cannot be parallel to curr->next.
	 */
	private final static void computeInnerPoint(final double[] output,
			final double xPrev, final double yPrev, final double xCurr,
			final double yCurr, final double xNext, final double yNext,
			final double borderWidth) {
		final double segX1 = xCurr - xPrev;
		final double segY1 = yCurr - yPrev;
		final double segLength1 = Math.sqrt((segX1 * segX1) + (segY1 * segY1));
		final double segX2 = xNext - xCurr;
		final double segY2 = yNext - yCurr;
		final double segLength2 = Math.sqrt((segX2 * segX2) + (segY2 * segY2));
		final double segX2Normal = segX2 / segLength2;
		final double segY2Normal = segY2 / segLength2;
		final double xNextPrime = (segX2Normal * segLength1) + xPrev;
		final double yNextPrime = (segY2Normal * segLength1) + yPrev;
		final double segPrimeX = xNextPrime - xCurr;
		final double segPrimeY = yNextPrime - yCurr;
		final double distancePrimeToSeg1 = (((segX1 * yNextPrime)
				- (segY1 * xNextPrime) + (xPrev * yCurr)) - (xCurr * yPrev))
				/ segLength1;
		final double multFactor = borderWidth / distancePrimeToSeg1;
		output[0] = (multFactor * segPrimeX) + xCurr;
		output[1] = (multFactor * segPrimeY) + yCurr;
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
	public final void drawEdgeLow(final float x0, final float y0,
			final float x1, final float y1, final Color edgeColor) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!m_cleared) {
				throw new IllegalStateException(
						"clear() has not been called previously");
			}

			if (edgeColor.getAlpha() != 255) {
				throw new IllegalArgumentException("edgeColor is not opaque");
			}
		}

		// This following statement has to be consistent with the full edge
		// rendering logic.
		if ((x0 == x1) && (y0 == y1)) {
			return;
		}

		if (m_gMinimal == null) {
			makeMinimalGraphics();
		}

		// I'm transforming points manually because the resulting underlying
		// graphics pipeline used is much faster.
		m_ptsBuff[0] = x0;
		m_ptsBuff[1] = y0;
		m_ptsBuff[2] = x1;
		m_ptsBuff[3] = y1;
		m_currXform.transform(m_ptsBuff, 0, m_ptsBuff, 0, 2);

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
	 * If an arrow other than ARROW_NONE is rendered, its size must be greater
	 * than or equal to edge thickness specified. The table below describes, to
	 * some extent, the nature of each arrow type. <blockquote><table
	 * border="1" cellpadding="5" cellspacing="0">
	 * <tr>
	 * <th>arrow type</th>
	 * <th>description</th>
	 * </tr>
	 * <tr>
	 * <td>ARROW_NONE</td>
	 * <td>the edge line segment has endpoint specified, and the line segment
	 * has a round end (center of round semicircle end exactly equal to endpoint
	 * specified); arrow size and arrow paint are ignored</td>
	 * </tr>
	 * <tr>
	 * <td>ARROW_DELTA</td>
	 * <td>the sharp tip of the arrowhead is exactly at the endpint specified;
	 * the delta is as wide as the arrow size specified and twice that in length</td>
	 * </tr>
	 * <tr>
	 * <td>ARROW_DIAMOND</td>
	 * <td>the sharp tip of the arrowhead is exactly at the endpoint specified;
	 * the diamond is as wide as the arrow size specified and twice that in
	 * length</td>
	 * </tr>
	 * <tr>
	 * <td>ARROW_DISC</td>
	 * <td>the disc arrowhead is placed such that its center is at the
	 * specified endpoint; the diameter of the disk is the arrow size specified</td>
	 * </tr>
	 * <tr>
	 * <td>ARROW_TEE</td>
	 * <td>the center of the tee intersection lies at the specified endpoint;
	 * the width of the top of the tee is one quarter of the arrow size
	 * specified, and the span of the top of the tee is two times the arrow size</td>
	 * </tr>
	 * <tr>
	 * <td>ARROW_BIDIRECTIONAL</td>
	 * <td>either both arrowheads must be of this type or neither one must be
	 * of this type; bidirectional edges look completely different from other
	 * edges; arrow paints are completely ignored for this type of edge; the
	 * edge arrow is drawn such that it fits snugly inside of an ARROW_DELTA of
	 * size 2s + e[sqrt(17)+5]/4 where s is the arrow size specified and e is
	 * edge thickness specified; the delta's tip is at edge endpoint specified;
	 * note that edge anchors are not supported for this type of edge</td>
	 * </tr>
	 * <tr>
	 * <td>ARROW_MONO</td>
	 * <td>either both arrowheads must be of this type or neither one must be
	 * of this type; mono edges look completely different from other edges
	 * because an arrowhead (an ARROW_DELTA) is placed such that its tip is in
	 * the middle of the edge segment, pointing from (x0,y0) to (x1,y1); the
	 * paint and size of the first arrow (arrow0) are read and the paint and
	 * size of the other arrow are completely ignored; note that edge anchors
	 * are not supported for this type of edge</td>
	 * </tr>
	 * </table></blockquote>
	 * <p>
	 * Note that if the edge segment length is zero then nothing gets rendered.
	 * <p>
	 * This method will not work unless clear() has been called at least once
	 * previously.
	 * <p>
	 * A discussion pertaining to edge anchors. Edge anchors are only supported
	 * for the primitive arrow types (ARROW_NONE, ARROW_DELTA, ARROW_DIAMOND,
	 * ARROW_DISC, and ARROW_TEE); <font color="red">ARROW_BIDIRECTIONAL and
	 * ARROW_MONO do not support edge anchors</font>. At most MAX_EDGE_ANCHORS
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
	 * @param edgePaint
	 *            the paint to use when drawing the edge segment.
	 * @param dashLength
	 *            a positive value representing the length of dashes on the
	 *            edge, or zero to indicate that the edge is solid; note that
	 *            drawing dashed segments is computationally expensive.
	 * @exception IllegalArgumentException
	 *                if edgeThickness is less than zero, if dashLength is less
	 *                than zero, if any one of the arrow configurations does not
	 *                meet specified criteria, or if more than MAX_EDGE_ANCHORS
	 *                anchors are specified.
	 */
	public final void drawEdgeFull(final byte arrow0Type,
			final float arrow0Size, final Paint arrow0Paint,
			final byte arrow1Type, final float arrow1Size,
			final Paint arrow1Paint, final float x0, final float y0,
			EdgeAnchors anchors, final float x1, final float y1,
			final float edgeThickness, final Paint edgePaint,
			final float dashLength) {
		final double curveFactor = CURVE_ELLIPTICAL;

		if (anchors == null) {
			anchors = m_noAnchors;
		}

		if (m_debug) {
			edgeFullDebug(arrow0Type, arrow0Size, arrow1Type, arrow1Size,
					edgeThickness, dashLength, anchors);
		}

		if (!computeCubicPolyEdgePath(arrow0Type,
				(arrow0Type == ARROW_NONE) ? 0.0f : arrow0Size, arrow1Type,
				(arrow1Type == ARROW_NONE) ? 0.0f : arrow1Size, x0, y0,
				anchors, x1, y1, curveFactor)) {
			// After filtering duplicate start and end points, there are less
			// than 3 total.
			if (m_edgePtsCount == 2) { // Draw an ordinary edge.
				drawSimpleEdgeFull(arrow0Type, arrow0Size, arrow0Paint,
						arrow1Type, arrow1Size, arrow1Paint,
						(float) m_edgePtsBuff[0], (float) m_edgePtsBuff[1],
						(float) m_edgePtsBuff[2], (float) m_edgePtsBuff[3],
						edgeThickness, edgePaint, dashLength);
			}

			return;
		}

		{ // Render the edge polypath.

			final boolean simpleSegment = (arrow0Type == ARROW_NONE)
					&& (arrow1Type == ARROW_NONE) && (dashLength == 0.0f);
			setStroke(edgeThickness, dashLength,
					simpleSegment ? BasicStroke.CAP_ROUND
							: BasicStroke.CAP_BUTT, false);
			// Set m_path2d to contain the cubic curves computed in
			// m_edgePtsBuff.
			m_path2d.reset();
			m_path2d.moveTo((float) m_edgePtsBuff[2], (float) m_edgePtsBuff[3]);

			int inx = 4;
			final int count = ((m_edgePtsCount - 1) * 6) - 2;

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
		}

		final double dx0 = m_edgePtsBuff[0] - m_edgePtsBuff[4];
		final double dy0 = m_edgePtsBuff[1] - m_edgePtsBuff[5];
		final double len0 = Math.sqrt((dx0 * dx0) + (dy0 * dy0));
		final double cosTheta0 = dx0 / len0;
		final double sinTheta0 = dy0 / len0;

		final double dx1 = m_edgePtsBuff[((m_edgePtsCount - 1) * 6) - 2]
				- m_edgePtsBuff[((m_edgePtsCount - 1) * 6) - 6];
		final double dy1 = m_edgePtsBuff[((m_edgePtsCount - 1) * 6) - 1]
				- m_edgePtsBuff[((m_edgePtsCount - 1) * 6) - 5];
		final double len1 = Math.sqrt((dx1 * dx1) + (dy1 * dy1));
		final double cosTheta1 = dx1 / len1;
		final double sinTheta1 = dy1 / len1;

		if (dashLength == 0.0f) { // Render arrow cap at origin of poly path.

			final Shape arrow0Cap = computeUntransformedArrowCap(arrow0Type,
					((double) arrow0Size) / edgeThickness);

			if (arrow0Cap != null) {
				m_xformUtil.setTransform(cosTheta0, sinTheta0, -sinTheta0,
						cosTheta0, m_edgePtsBuff[2], m_edgePtsBuff[3]);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(arrow0Cap);
				m_g2d.setTransform(m_currNativeXform);
			}
		}

		if (dashLength == 0.0f) { // Render arrow cap at end of poly path.

			final Shape arrow1Cap = computeUntransformedArrowCap(arrow1Type,
					((double) arrow1Size) / edgeThickness);

			if (arrow1Cap != null) {
				m_xformUtil.setTransform(cosTheta1, sinTheta1, -sinTheta1,
						cosTheta1,
						m_edgePtsBuff[((m_edgePtsCount - 1) * 6) - 4],
						m_edgePtsBuff[((m_edgePtsCount - 1) * 6) - 3]);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(arrow1Cap);
				m_g2d.setTransform(m_currNativeXform);
			}
		}

		{ // Render arrow at origin of poly path.

			final Shape arrow0 = computeUntransformedArrow(arrow0Type);

			if (arrow0 != null) {
				m_xformUtil.setTransform(cosTheta0, sinTheta0, -sinTheta0,
						cosTheta0, m_edgePtsBuff[0], m_edgePtsBuff[1]);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(arrow0Size, arrow0Size);
				m_g2d.setPaint(arrow0Paint);
				m_g2d.fill(arrow0);
				m_g2d.setTransform(m_currNativeXform);
			}
		}

		{ // Render arrow at end of poly path.

			final Shape arrow1 = computeUntransformedArrow(arrow1Type);

			if (arrow1 != null) {
				m_xformUtil.setTransform(cosTheta1, sinTheta1, -sinTheta1,
						cosTheta1,
						m_edgePtsBuff[((m_edgePtsCount - 1) * 6) - 2],
						m_edgePtsBuff[((m_edgePtsCount - 1) * 6) - 1]);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(arrow1Size, arrow1Size);
				m_g2d.setPaint(arrow1Paint);
				m_g2d.fill(arrow1);
				m_g2d.setTransform(m_currNativeXform);
			}
		}
	}

	@SuppressWarnings("fallthrough")
	private final void edgeFullDebug(final byte arrow0Type,
			final float arrow0Size, final byte arrow1Type, float arrow1Size,
			final float edgeThickness, final float dashLength,
			final EdgeAnchors anchors) {
		if (!EventQueue.isDispatchThread()) {
			throw new IllegalStateException(
					"calling thread is not AWT event dispatcher");
		}

		if (!m_cleared) {
			throw new IllegalStateException(
					"clear() has not been called previously");
		}

		if (!(edgeThickness >= 0.0f)) {
			throw new IllegalArgumentException("edgeThickness < 0");
		}

		if (!(dashLength >= 0.0f)) {
			throw new IllegalArgumentException("dashLength < 0");
		}

		switch (arrow0Type) {
		case ARROW_NONE:
			break;

		case ARROW_MONO:
			arrow1Size = arrow0Size;

			// Don't break; fall through.
		case ARROW_BIDIRECTIONAL:

			if (anchors.numAnchors() > 0) {
				throw new IllegalArgumentException(
						"ARROW_BIDIRECTIONAL and ARROW_MONO not supported for poly edges");
			}

			if (arrow1Type != arrow0Type) {
				throw new IllegalArgumentException(
						"for ARROW_BIDIRECTIONAL and ARROW_MONO, both arrows must be "
								+ "identical");
			}

			// Don't break; fall through.
		case ARROW_DELTA:
		case ARROW_DIAMOND:
		case ARROW_DISC:
		case ARROW_TEE:

			if (!(arrow0Size >= edgeThickness)) {
				throw new IllegalArgumentException(
						"arrow size must be at least as large as edge thickness");
			}

			break;

		default:
			throw new IllegalArgumentException("arrow0Type is not recognized");
		}

		switch (arrow1Type) {
		case ARROW_NONE:
			break;

		case ARROW_BIDIRECTIONAL:
		case ARROW_MONO:

			if (arrow0Type != arrow1Type) {
				throw new IllegalArgumentException(
						"for ARROW_BIDIRECTIONAL and ARROW_MONO, both arrows must be "
								+ "identical");
			}

			// Don't break; fall through.
		case ARROW_DELTA:
		case ARROW_DIAMOND:
		case ARROW_DISC:
		case ARROW_TEE:

			if (!(arrow1Size >= edgeThickness)) {
				throw new IllegalArgumentException(
						"arrow size must be at least as large as edge thickness");
			}

			break;

		default:
			throw new IllegalArgumentException("arrow1Type is not recognized");
		}

		if (anchors.numAnchors() > MAX_EDGE_ANCHORS) {
			throw new IllegalArgumentException("at most MAX_EDGE_ANCHORS ("
					+ MAX_EDGE_ANCHORS + ") edge anchors can be specified");
		}
	}

	private final void drawSimpleEdgeFull(final byte arrow0Type,
			final float arrow0Size, final Paint arrow0Paint,
			final byte arrow1Type, final float arrow1Size,
			final Paint arrow1Paint, final float x0, final float y0,
			final float x1, final float y1, final float edgeThickness,
			final Paint edgePaint, final float dashLength) {
		final double len = Math
				.sqrt(((((double) x1) - x0) * (((double) x1) - x0))
						+ ((((double) y1) - y0) * (((double) y1) - y0)));

		// If the length of the edge is zero we're going to skip completely over
		// all rendering. This check is now redundant because the code that
		// calls
		// us makes this check automatically.
		if (len == 0.0d) {
			return;
		}

		if (arrow0Type == ARROW_BIDIRECTIONAL) { // Draw and return.

			final double a = (6.0d + (Math.sqrt(17.0d) / 2.0d)) * edgeThickness;
			m_path2d.reset();

			final double f = ((double) arrow0Size) - edgeThickness;
			m_path2d.moveTo((float) (a + (4.0d * f)),
					(float) (f + (1.5d * edgeThickness)));
			m_path2d.lineTo((float) a, (float) (1.5d * edgeThickness));

			if ((2.0d * a) < len) {
				m_path2d.lineTo((float) (len - a),
						(float) (1.5d * edgeThickness));
			}

			final double g = ((double) arrow1Size) - edgeThickness;
			m_path2d.moveTo((float) (len - (a + (4.0d * g))),
					(float) (-g + (-1.5d * edgeThickness)));
			m_path2d.lineTo((float) (len - a), (float) (-1.5d * edgeThickness));

			if ((2.0d * a) < len) {
				m_path2d.lineTo((float) a, (float) (-1.5d * edgeThickness));
			}

			// I want the transform to first rotate, then translate.
			final double cosTheta = (((double) x1) - x0) / len;
			final double sinTheta = (((double) y1) - y0) / len;
			m_xformUtil.setTransform(cosTheta, sinTheta, -sinTheta, cosTheta,
					x0, y0);
			m_path2d.transform(m_xformUtil);
			setStroke(edgeThickness, dashLength,
					(dashLength == 0.0f) ? BasicStroke.CAP_ROUND
							: BasicStroke.CAP_BUTT, false);
			m_g2d.setPaint(edgePaint);
			m_g2d.draw(m_path2d);

			return;
		} // End ARROW_BIDIRECTIONAL.

		if (arrow0Type == ARROW_MONO) { // Draw and return.
			m_g2d.setPaint(edgePaint); // We're going to render at least one
			// segment.

			setStroke(edgeThickness, dashLength, BasicStroke.CAP_BUTT, false);

			final double deltaLen = getT(ARROW_DELTA) * arrow0Size;
			final double tDeltaLenFactor = 0.5d - (deltaLen / len);

			if (tDeltaLenFactor > 0.0d) { // We must render the "pre" line
				// segment.

				final double x0Prime = (tDeltaLenFactor * (((double) x1) - x0))
						+ x0;
				final double y0Prime = (tDeltaLenFactor * (((double) y1) - y0))
						+ y0;
				m_line2d.setLine(x0, y0, x0Prime, y0Prime);
				m_g2d.draw(m_line2d);
			}

			// Render the "post" segment.
			final double midX = (((double) x0) + x1) / 2.0d;
			final double midY = (((double) y0) + y1) / 2.0d;
			m_line2d.setLine(x1, y1, midX, midY);
			m_g2d.draw(m_line2d);

			final double cosTheta = (((double) x0) - x1) / len;
			final double sinTheta = (((double) y0) - y1) / len;

			if ((tDeltaLenFactor > 0.0d) && (dashLength == 0.0f)) { // Render
				// begin
				// cap.
				m_xformUtil.setTransform(cosTheta, sinTheta, -sinTheta,
						cosTheta, x0, y0);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(computeUntransformedArrowCap(ARROW_NONE, 0.0d));
				m_g2d.setTransform(m_currNativeXform);
			}

			if (dashLength == 0.0f) { // Render end cap.
				m_xformUtil.setTransform(-cosTheta, -sinTheta, sinTheta,
						-cosTheta, x1, y1);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(computeUntransformedArrowCap(ARROW_NONE, 0.0d));
				m_g2d.setTransform(m_currNativeXform);
			}

			if (dashLength == 0.0f) { // Render delta wedge cap.
				m_xformUtil.setTransform(-cosTheta, -sinTheta, sinTheta,
						-cosTheta, midX, midY);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(computeUntransformedDeltaWedgeCap());
				m_g2d.setTransform(m_currNativeXform);
			}
			// Finally, render the mono delta wedge.
			{
				m_xformUtil.setTransform(-cosTheta, -sinTheta, sinTheta,
						-cosTheta, midX, midY);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(arrow0Size, arrow0Size);
				m_g2d.setPaint(arrow0Paint);
				m_g2d.fill(computeUntransformedArrow(ARROW_DELTA));
				m_g2d.setTransform(m_currNativeXform);
			}

			return;
		} // End ARROW_MONO.

		final double x0Adj;
		final double y0Adj;
		final double x1Adj;
		final double y1Adj;
		final byte simpleSegment;

		{ // Render the line segment if necessary.

			final double t0 = (getT(arrow0Type) * arrow0Size) / len;
			x0Adj = (t0 * (((double) x1) - x0)) + x0;
			y0Adj = (t0 * (((double) y1) - y0)) + y0;

			final double t1 = (getT(arrow1Type) * arrow1Size) / len;
			x1Adj = (t1 * (((double) x0) - x1)) + x1;
			y1Adj = (t1 * (((double) y0) - y1)) + y1;

			// If the vector point0->point1 is pointing opposite to
			// adj0->adj1, then don't render the line segment.
			// Dot product determines this.
			if ((((((double) x1) - x0) * (x1Adj - x0Adj)) + ((((double) y1) - y0) * (y1Adj - y0Adj))) > 0.0d) {
				// Must render the line segment.
				if ((arrow0Type == ARROW_NONE) && (arrow1Type == ARROW_NONE)
						&& (dashLength == 0.0f)) {
					simpleSegment = 1;
				} else {
					simpleSegment = -1;
				}

				setStroke(edgeThickness, dashLength,
						(simpleSegment > 0) ? BasicStroke.CAP_ROUND
								: BasicStroke.CAP_BUTT, false);
				m_line2d.setLine(x0Adj, y0Adj, x1Adj, y1Adj);
				m_g2d.setPaint(edgePaint);
				m_g2d.draw(m_line2d);

				if (simpleSegment > 0) {
					return;
				}
			} else {
				simpleSegment = 0;
			} // Did not render segment.
		} // End rendering of line segment.

		// Using x0, x1, y0, and y1 instead of the "adjusted" endpoints is
		// accurate enough in computation of cosine and sine because the
		// length is guaranteed to be at least as large. Remember that the
		// original endpoint values are specified as float whereas the adjusted
		// points are double.
		final double cosTheta = (((double) x0) - x1) / len;
		final double sinTheta = (((double) y0) - y1) / len;

		if ((simpleSegment < 0) && (dashLength == 0.0f)) { // Arrow cap at
			// point 0.

			final Shape arrow0Cap = computeUntransformedArrowCap(arrow0Type,
					((double) arrow0Size) / edgeThickness);

			if (arrow0Cap != null) {
				m_xformUtil.setTransform(cosTheta, sinTheta, -sinTheta,
						cosTheta, x0Adj, y0Adj);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(arrow0Cap);
				m_g2d.setTransform(m_currNativeXform);
			}
		}

		if ((simpleSegment < 0) && (dashLength == 0.0f)) { // Arrow cap at
			// point 1.

			final Shape arrow1Cap = computeUntransformedArrowCap(arrow1Type,
					((double) arrow1Size) / edgeThickness);

			if (arrow1Cap != null) {
				m_xformUtil.setTransform(-cosTheta, -sinTheta, sinTheta,
						-cosTheta, x1Adj, y1Adj);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(edgeThickness, edgeThickness);
				// The paint is already set to edge paint.
				m_g2d.fill(arrow1Cap);
				m_g2d.setTransform(m_currNativeXform);
			}
		}

		{ // Render arrow at point 0.

			final Shape arrow0 = computeUntransformedArrow(arrow0Type);

			if (arrow0 != null) {
				m_xformUtil.setTransform(cosTheta, sinTheta, -sinTheta,
						cosTheta, x0, y0);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(arrow0Size, arrow0Size);
				m_g2d.setPaint(arrow0Paint);
				m_g2d.fill(arrow0);
				m_g2d.setTransform(m_currNativeXform);
			}
		}

		{ // Render arrow at point 1.

			final Shape arrow1 = computeUntransformedArrow(arrow1Type);

			if (arrow1 != null) {
				m_xformUtil.setTransform(-cosTheta, -sinTheta, sinTheta,
						-cosTheta, x1, y1);
				m_g2d.transform(m_xformUtil);
				m_g2d.scale(arrow1Size, arrow1Size);
				m_g2d.setPaint(arrow1Paint);
				m_g2d.fill(arrow1);
				m_g2d.setTransform(m_currNativeXform);
			}
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
	public final boolean getEdgePath(final byte arrow0Type,
			final float arrow0Size, final byte arrow1Type,
			final float arrow1Size, final float x0, final float y0,
			EdgeAnchors anchors, final float x1, final float y1,
			final GeneralPath path) {
		final double curveFactor = CURVE_ELLIPTICAL;

		if (anchors == null) {
			anchors = m_noAnchors;
		}

		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			switch (arrow0Type) {
			case ARROW_NONE:
			case ARROW_DELTA:
			case ARROW_DIAMOND:
			case ARROW_DISC:
			case ARROW_TEE:
				break;

			case ARROW_BIDIRECTIONAL:
			case ARROW_MONO:

				if (arrow1Type != arrow0Type) {
					throw new IllegalArgumentException(
							"for ARROW_BIDIRECTIONAL and ARROW_MONO, both arrows must be "
									+ "identical");
				}

				if (anchors.numAnchors() > 0) {
					throw new IllegalArgumentException(
							"ARROW_BIDIRECTIONAL and ARROW_MONO not supported in poly edges");
				}

				break;

			default:
				throw new IllegalArgumentException(
						"arrow0Type is not recognized");
			}

			switch (arrow1Type) {
			case ARROW_NONE:
			case ARROW_DELTA:
			case ARROW_DIAMOND:
			case ARROW_DISC:
			case ARROW_TEE:
				break;

			case ARROW_BIDIRECTIONAL:
			case ARROW_MONO:

				if (arrow0Type != arrow1Type) {
					throw new IllegalArgumentException(
							"for ARROW_BIDIRECTIONAL and ARROW_MONO, both arrows must be "
									+ "identical");
				}

				break;

			default:
				throw new IllegalArgumentException(
						"arrow1Type is not recognized");
			}

			if (anchors.numAnchors() > MAX_EDGE_ANCHORS) {
				throw new IllegalArgumentException("at most MAX_EDGE_ANCHORS ("
						+ MAX_EDGE_ANCHORS + ") edge anchors can be specified");
			}
		}

		byte arrow0 = arrow0Type;
		byte arrow1 = arrow1Type;

		if (arrow0 == ARROW_BIDIRECTIONAL) { // Assume arrow1 is also.
			// If we wanted to start our path where the bidirectional edge
			// actually started, we'd have to pass edge thickness into this
			// method.
			// So instead we do a quick and simple approximation by extending
			// the
			// bidirectional edge to the tip of the encapsulating delta.
			arrow0 = ARROW_NONE;
			arrow1 = ARROW_NONE;
		}

		if (arrow0 == ARROW_MONO) { // Assume arrow1 is also.
			arrow0 = ARROW_NONE;
			arrow1 = ARROW_NONE;
		}

		if (!computeCubicPolyEdgePath(arrow0, (arrow0 == ARROW_NONE) ? 0.0f
				: arrow0Size, arrow1, (arrow1 == ARROW_NONE) ? 0.0f
				: arrow1Size, x0, y0, anchors, x1, y1, curveFactor)) {
			// After filtering duplicate start and end points, there are less
			// then
			// 3 total.
			if (m_edgePtsCount == 2) {
				path.reset();
				path.moveTo((float) m_edgePtsBuff[0], (float) m_edgePtsBuff[1]);
				path.lineTo((float) m_edgePtsBuff[2], (float) m_edgePtsBuff[3]);

				return true;
			}

			return false;
		}

		path.reset();
		path.moveTo((float) m_edgePtsBuff[0], (float) m_edgePtsBuff[1]);
		path.lineTo((float) m_edgePtsBuff[2], (float) m_edgePtsBuff[3]);

		int inx = 4;
		final int count = ((m_edgePtsCount - 1) * 6) - 2;

		while (inx < count) {
			path.curveTo((float) m_edgePtsBuff[inx++],
					(float) m_edgePtsBuff[inx++], (float) m_edgePtsBuff[inx++],
					(float) m_edgePtsBuff[inx++], (float) m_edgePtsBuff[inx++],
					(float) m_edgePtsBuff[inx++]);
		}

		path.lineTo((float) m_edgePtsBuff[count],
				(float) m_edgePtsBuff[count + 1]);

		return true;
	}

	/*
	 * Returns non-null if and only if an arrow is necessary for the arrow type
	 * specified. m_path2d and m_ellp2d may be mangled as a side effect.
	 * arrowType must be one of the primitive arrow types or ARROW_NONE (no
	 * ARROW_BIDIRECTIONAL or ARROW_MONO allowed).
	 */
	private final Shape computeUntransformedArrow(final byte arrowType) {
		switch (arrowType) {
		case ARROW_NONE:
			return null;

		case ARROW_DELTA:
			m_path2d.reset();
			m_path2d.moveTo(-2.0f, -0.5f);
			m_path2d.lineTo(0.0f, 0.0f);
			m_path2d.lineTo(-2.0f, 0.5f);
			m_path2d.closePath();

			return m_path2d;

		case ARROW_DIAMOND:
			m_path2d.reset();
			m_path2d.moveTo(-1.0f, -0.5f);
			m_path2d.lineTo(0.0f, 0.0f);
			m_path2d.lineTo(-1.0f, 0.5f);
			m_path2d.lineTo(-2.0f, 0.0f);
			m_path2d.closePath();

			return m_path2d;

		case ARROW_DISC:
			m_ellp2d.setFrame(-0.5d, -0.5d, 1.0d, 1.0d);

			return m_ellp2d;

		default: // ARROW_TEE.
			m_path2d.reset();
			m_path2d.moveTo(-0.125f, -1.0f);
			m_path2d.lineTo(0.125f, -1.0f);
			m_path2d.lineTo(0.125f, 1.0f);
			m_path2d.lineTo(-0.125f, 1.0f);
			m_path2d.closePath();

			return m_path2d;
		}
	}

	/*
	 * The ratio parameter specifies the ratio of arrow size (disc diameter) to
	 * edge thickness (only used for some arrow types). Returns non-null if and
	 * only if a cap is necessary for the arrow type specified. If non-null is
	 * returned then m_path2d and m_arc2d may be mangled as a side effect.
	 * arrowType must be one of the primitive arrow types or ARROW_NONE (no
	 * ARROW_BIDIRECTIONAL or ARROW_MONO allowed).
	 */
	private final Shape computeUntransformedArrowCap(final byte arrowType,
			final double ratio) {
		switch (arrowType) {
		case ARROW_NONE:
			m_arc2d.setArc(-0.5d, -0.5d, 1.0d, 1.0d, 270.0d, 180.0d,
					Arc2D.CHORD);

			return m_arc2d;

		case ARROW_DELTA:
			return null;

		case ARROW_DIAMOND:
			m_path2d.reset();
			m_path2d.moveTo(0.0f, -0.5f);
			m_path2d.lineTo(1.0f, -0.5f);
			m_path2d.lineTo(0.0f, 0.0f);
			m_path2d.lineTo(1.0f, 0.5f);
			m_path2d.lineTo(0.0f, 0.5f);
			m_path2d.closePath();

			return m_path2d;

		case ARROW_DISC:

			final double theta = Math.toDegrees(Math.asin(1.0d / ratio));
			m_arc2d.setArc(0.0d, ratio / -2.0d, ratio, ratio, 180.0d - theta,
					theta * 2, Arc2D.OPEN);
			m_path2d.reset();
			m_path2d.append(m_arc2d, false);
			m_path2d.lineTo(0.0f, 0.5f);
			m_path2d.lineTo(0.0f, -0.5f);
			m_path2d.closePath();

			return m_path2d;

		default: // ARROW_TEE.

			return null;
		}
	}

	/*
	 * ---| \ | \| /| / | ---|
	 * 
	 * The same transform that was used to draw the delta arrowhead (for
	 * ARROW_MONO) can be used modulo scaling to edge thickness.
	 */
	private final Shape computeUntransformedDeltaWedgeCap() {
		m_path2d.reset();
		m_path2d.moveTo(-2.0f, -0.5f);
		m_path2d.lineTo(0.0f, -0.5f);
		m_path2d.lineTo(0.0f, 0.5f);
		m_path2d.lineTo(-2.0f, 0.5f);
		m_path2d.lineTo(0.0f, 0.0f);
		m_path2d.closePath();

		return m_path2d;
	}

	/*
	 * arrowType must be one of the primitive arrow types or ARROW_NONE (no
	 * ARROW_BIDIRECTIONAL or ARROW_MONO allowed).
	 */
	private final static double getT(final byte arrowType) { // I could
		// implement
		// this as an
		// array instead
		// of a switch
		// statement.

		switch (arrowType) {
		case ARROW_NONE:
			return 0.0d;

		case ARROW_DELTA:
			return 2.0d;

		case ARROW_DIAMOND:
			return 2.0d;

		case ARROW_DISC:
			return 0.5d;

		default: // ARROW_TEE.

			return 0.125d;
		}
	}

	/*
	 * If arrow0Type is ARROW_NONE, arrow0Size should be zero. If arrow1Type is
	 * ARROW_NONE, arrow1Size should be zero.
	 */
	private final boolean computeCubicPolyEdgePath(final byte arrow0Type,
			final float arrow0Size, final byte arrow1Type,
			final float arrow1Size, final float x0, final float y0,
			final EdgeAnchors anchors, final float x1, final float y1,
			final double curveFactor) {
		final int numAnchors = anchors.numAnchors();
		// add the start point to the edge points buffer
		m_edgePtsBuff[0] = x0;
		m_edgePtsBuff[1] = y0;
		m_edgePtsCount = 1;

		int anchorInx = 0;

		// finds the first anchor point other than the start point and
		// add it to the edge points buffer
		while (anchorInx < numAnchors) {
			anchors.getAnchor(anchorInx++, m_floatBuff);

			if (!((m_floatBuff[0] == x0) && (m_floatBuff[1] == y0))) {
				m_edgePtsBuff[2] = m_floatBuff[0];
				m_edgePtsBuff[3] = m_floatBuff[1];
				m_edgePtsCount = 2;

				break;
			}
		}

		// now fill edge points buffer with all subsequent anchors
		while (anchorInx < numAnchors) {
			anchors.getAnchor(anchorInx++, m_floatBuff);
			// Duplicate anchors are allowed.
			m_edgePtsBuff[m_edgePtsCount * 2] = m_floatBuff[0];
			m_edgePtsBuff[(m_edgePtsCount * 2) + 1] = m_floatBuff[1];
			m_edgePtsCount++;
		}

		// now add the end point to the buffer
		m_edgePtsBuff[m_edgePtsCount * 2] = x1;
		m_edgePtsBuff[(m_edgePtsCount * 2) + 1] = y1;
		m_edgePtsCount++;

		// remove duplicate end points from edge buffer
		while (m_edgePtsCount > 1) {
			// second-to-last X coord and  second-to-last Y coord.
			if ((m_edgePtsBuff[(m_edgePtsCount * 2) - 2] == m_edgePtsBuff[(m_edgePtsCount * 2) - 4]) 
					&& (m_edgePtsBuff[(m_edgePtsCount * 2) - 1] == m_edgePtsBuff[(m_edgePtsCount * 2) - 3])) { 
				m_edgePtsCount--;
			} else {
				break;
			}
		}

		// no anchors, just a straight line to draw 
		if (m_edgePtsCount < 3) {
			return false;
		}

		//
		// ok, now we're drawing a curve
		//

		final int edgePtsCount = m_edgePtsCount;

		// First set the three control points related to point 1.
		// 6 represents the offset in the buffer.
		{ 
			m_edgePtsCount--;
			// set first control point
			m_edgePtsBuff[(m_edgePtsCount * 6) - 2] = m_edgePtsBuff[m_edgePtsCount * 2];
			m_edgePtsBuff[(m_edgePtsCount * 6) - 1] = m_edgePtsBuff[(m_edgePtsCount * 2) + 1];

			double dx = m_edgePtsBuff[(m_edgePtsCount * 2) - 2]
					- m_edgePtsBuff[m_edgePtsCount * 2];
			double dy = m_edgePtsBuff[(m_edgePtsCount * 2) - 1]
					- m_edgePtsBuff[(m_edgePtsCount * 2) + 1];
			double len = Math.sqrt((dx * dx) + (dy * dy));
			// Normalized.
			dx /= len;
			dy /= len; 

			// set second control point
			m_edgePtsBuff[(m_edgePtsCount * 6) - 4] = m_edgePtsBuff[(m_edgePtsCount * 6) - 2]
					+ (dx * arrow1Size * getT(arrow1Type));
			m_edgePtsBuff[(m_edgePtsCount * 6) - 3] = m_edgePtsBuff[(m_edgePtsCount * 6) - 1]
					+ (dy * arrow1Size * getT(arrow1Type));

			// one candidate point is offset by the arrow (candX1) and 
			// the other is offset by the curvefactor (candX2)
			double candX1 = m_edgePtsBuff[(m_edgePtsCount * 6) - 4]
					+ (dx * 2.0d * arrow1Size);
			double candX2 = m_edgePtsBuff[(m_edgePtsCount * 6) - 4]
					+ (curveFactor * (m_edgePtsBuff[(m_edgePtsCount * 2) - 2] - m_edgePtsBuff[(m_edgePtsCount * 6) - 4]));

			// set third control point X coord
			// choose the candidate with max offset
			if (Math.abs(candX1 - m_edgePtsBuff[m_edgePtsCount * 2]) > Math
					.abs(candX2 - m_edgePtsBuff[m_edgePtsCount * 2])) {
				m_edgePtsBuff[(m_edgePtsCount * 6) - 6] = candX1;
			} else {
				m_edgePtsBuff[(m_edgePtsCount * 6) - 6] = candX2;
			}

			// one candidate point is offset by the arrow (candY1) and 
			// the other is offset by the curvefactor (candY2)
			double candY1 = m_edgePtsBuff[(m_edgePtsCount * 6) - 3]
					+ (dy * 2.0d * arrow1Size);
			double candY2 = m_edgePtsBuff[(m_edgePtsCount * 6) - 3]
					+ (curveFactor * (m_edgePtsBuff[(m_edgePtsCount * 2) - 1] - m_edgePtsBuff[(m_edgePtsCount * 6) - 3]));

			// set third control point Y coord
			// choose the candidate with max offset
			if (Math.abs(candY1 - m_edgePtsBuff[(m_edgePtsCount * 2) + 1]) > Math
					.abs(candY2 - m_edgePtsBuff[(m_edgePtsCount * 2) + 1])) {
				m_edgePtsBuff[(m_edgePtsCount * 6) - 5] = candY1;
			} else {
				m_edgePtsBuff[(m_edgePtsCount * 6) - 5] = candY2;
			}
		}

		// Next set the control point for each edge anchor. 
		while (m_edgePtsCount > 2) {
			m_edgePtsCount--;

			final double midX = (m_edgePtsBuff[(m_edgePtsCount * 2) - 2] + m_edgePtsBuff[m_edgePtsCount * 2]) / 2.0d;
			final double midY = (m_edgePtsBuff[(m_edgePtsCount * 2) - 1] + m_edgePtsBuff[(m_edgePtsCount * 2) + 1]) / 2.0d;
			m_edgePtsBuff[(m_edgePtsCount * 6) - 2] = midX
					+ ((m_edgePtsBuff[m_edgePtsCount * 2] - midX) * curveFactor);
			m_edgePtsBuff[(m_edgePtsCount * 6) - 1] = midY
					+ ((m_edgePtsBuff[(m_edgePtsCount * 2) + 1] - midY) * curveFactor);
			m_edgePtsBuff[(m_edgePtsCount * 6) - 4] = midX;
			m_edgePtsBuff[(m_edgePtsCount * 6) - 3] = midY;
			m_edgePtsBuff[(m_edgePtsCount * 6) - 6] = midX
					+ ((m_edgePtsBuff[(m_edgePtsCount * 2) - 2] - midX) * curveFactor);
			m_edgePtsBuff[(m_edgePtsCount * 6) - 5] = midY
					+ ((m_edgePtsBuff[(m_edgePtsCount * 2) - 1] - midY) * curveFactor);
		}

		{ // Last set the three control points related to point 0.

			double dx = m_edgePtsBuff[2] - m_edgePtsBuff[0];
			double dy = m_edgePtsBuff[3] - m_edgePtsBuff[1];
			double len = Math.sqrt((dx * dx) + (dy * dy));
			// Normalized.
			dx /= len;
			dy /= len; 

			double segStartX = m_edgePtsBuff[0]
					+ (dx * arrow0Size * getT(arrow0Type));
			double segStartY = m_edgePtsBuff[1]
					+ (dy * arrow0Size * getT(arrow0Type));
			double candX1 = segStartX + (dx * 2.0d * arrow0Size);
			double candX2 = segStartX
					+ (curveFactor * (m_edgePtsBuff[2] - segStartX));

			if (Math.abs(candX1 - m_edgePtsBuff[0]) > Math.abs(candX2
					- m_edgePtsBuff[0])) {
				m_edgePtsBuff[4] = candX1;
			} else {
				m_edgePtsBuff[4] = candX2;
			}

			double candY1 = segStartY + (dy * 2.0d * arrow0Size);
			double candY2 = segStartY
					+ (curveFactor * (m_edgePtsBuff[3] - segStartY));

			if (Math.abs(candY1 - m_edgePtsBuff[1]) > Math.abs(candY2
					- m_edgePtsBuff[1])) {
				m_edgePtsBuff[5] = candY1;
			} else {
				m_edgePtsBuff[5] = candY2;
			}

			m_edgePtsBuff[2] = segStartX;
			m_edgePtsBuff[3] = segStartY;
		}

		m_edgePtsCount = edgePtsCount;

		return true;
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
	public final boolean computeEdgeIntersection(final byte nodeShape,
			final float xMin, final float yMin, final float xMax,
			final float yMax, final float offset, final float ptX,
			final float ptY, final float[] returnVal) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!(xMin < xMax)) {
				throw new IllegalArgumentException("xMin not less than xMax");
			}

			if (!(yMin < yMax)) {
				throw new IllegalArgumentException("yMin not less than yMax");
			}

			if (!(offset >= 0.0f)) {
				throw new IllegalArgumentException("offset < 0");
			}

			if (nodeShape == SHAPE_ROUNDED_RECTANGLE) {
				final double width = ((double) xMax) - xMin;
				final double height = ((double) yMax) - yMin;

				if (!(Math.max(width, height) < (2.0d * Math.min(width, height)))) {
					throw new IllegalArgumentException(
							"rounded rectangle does not meet constraint "
									+ "max(width, height) < 2 * min(width, height)");
				}
			}
		}

		final double centerX = (((double) xMin) + xMax) / 2.0d;
		final double centerY = (((double) yMin) + yMax) / 2.0d;

		if (nodeShape == SHAPE_ELLIPSE) {
			if ((centerX == ptX) && (centerY == ptY)) {
				return false;
			}

			// First, compute the actual intersection of the edge with the
			// ellipse, if it exists. We will use this intersection point
			// regardless of whether or not offset is zero.
			// For nonzero offsets on the ellipse, use tangent lines to
			// approximate
			// intersection with offset instead of solving a quartic equation.
			final double ptPrimeX = ptX - centerX;
			final double ptPrimeY = ptY - centerY;
			final double ellpW = ((double) xMax) - xMin;
			final double ellpH = ((double) yMax) - yMin;
			final double xScaleFactor = 2.0d / ellpW;
			final double yScaleFactor = 2.0d / ellpH;
			final double xformedPtPrimeX = ptPrimeX * xScaleFactor;
			final double xformedPtPrimeY = ptPrimeY * yScaleFactor;
			final double xformedDist = Math
					.sqrt((xformedPtPrimeX * xformedPtPrimeX)
							+ (xformedPtPrimeY * xformedPtPrimeY));
			final double xsectXformedPtPrimeX = xformedPtPrimeX / xformedDist;
			final double xsectXformedPtPrimeY = xformedPtPrimeY / xformedDist;
			final double tangentXformedPtPrimeX = xsectXformedPtPrimeX
					+ xsectXformedPtPrimeY;
			final double tangentXformedPtPrimeY = xsectXformedPtPrimeY
					- xsectXformedPtPrimeX;
			final double xsectPtPrimeX = xsectXformedPtPrimeX / xScaleFactor;
			final double xsectPtPrimeY = xsectXformedPtPrimeY / yScaleFactor;
			final double tangentPtPrimeX = tangentXformedPtPrimeX
					/ xScaleFactor;
			final double tangentPtPrimeY = tangentXformedPtPrimeY
					/ yScaleFactor;
			final double vTangentX = tangentPtPrimeX - xsectPtPrimeX;
			final double vTangentY = tangentPtPrimeY - xsectPtPrimeY;
			final double tanLen = Math.sqrt((vTangentX * vTangentX)
					+ (vTangentY * vTangentY));
			final double distPtPrimeToTangent = (((vTangentX * ptPrimeY)
					- (vTangentY * ptPrimeX) + (xsectPtPrimeX * tangentPtPrimeY)) - (tangentPtPrimeX * xsectPtPrimeY))
					/ tanLen;

			if (distPtPrimeToTangent < offset) { // This includes cases where
				// distPtPrimeToTangent is negative, which means that the true
				// intersection point lies inside the ellipse (no intersection
				// even
				// with zero offset).

				return false;
			}

			if (distPtPrimeToTangent == 0.0d) { // Therefore offset is zero
				// also.
				returnVal[0] = (float) (xsectPtPrimeX + centerX);
				returnVal[1] = (float) (xsectPtPrimeY + centerY);

				return true;
			}

			// Even if offset is zero, do extra computation for sake of simple
			// code.
			final double multFactor = offset / distPtPrimeToTangent;
			returnVal[0] = (float) (centerX + (xsectPtPrimeX + (multFactor * (ptPrimeX - xsectPtPrimeX))));
			returnVal[1] = (float) (centerY + (xsectPtPrimeY + (multFactor * (ptPrimeY - xsectPtPrimeY))));

			return true;
		} else { // Not ellipse.

			final double trueOffset;

			if (nodeShape == SHAPE_ROUNDED_RECTANGLE) {
				final double radius = Math.max(((double) xMax) - xMin,
						((double) yMax) - yMin) / 4.0d;
				// One of our constraints is that for rounded rectangle,
				// max(width, height) < 2 * min(width, height) in 32 bit
				// floating
				// point world. Therefore, with 64 bits of precision, the
				// rectangle
				// calculated below does not degenerate in width or height.
				getShape(SHAPE_RECTANGLE, radius + xMin, radius + yMin, -radius
						+ xMax, -radius + yMax);
				trueOffset = radius + offset;
			} else {
				// This next method call has the side effect of setting
				// m_polyCoords
				// and m_polyNumPoints - this is all that we are going to use.
				getShape(nodeShape, xMin, yMin, xMax, yMax);
				trueOffset = offset;
			}

			if (trueOffset == 0.0d) {
				final int twicePolyNumPoints = m_polyNumPoints * 2;

				for (int i = 0; i < twicePolyNumPoints;) {
					final double x0 = m_polyCoords[i++];
					final double y0 = m_polyCoords[i++];
					final double x1 = m_polyCoords[i % twicePolyNumPoints];
					final double y1 = m_polyCoords[(i + 1) % twicePolyNumPoints];

					if (segmentIntersection(m_ptsBuff, ptX, ptY, centerX,
							centerY, x0, y0, x1, y1)) {
						returnVal[0] = (float) m_ptsBuff[0];
						returnVal[1] = (float) m_ptsBuff[1];

						return true;
					}
				}

				return false;
			}

			// The rest of this code is the polygonal case where offset is
			// nonzero.
			for (int i = 0; i < m_polyNumPoints; i++) {
				final double x0 = m_polyCoords[i * 2];
				final double y0 = m_polyCoords[(i * 2) + 1];
				final double x1 = m_polyCoords[((i * 2) + 2)
						% (m_polyNumPoints * 2)];
				final double y1 = m_polyCoords[((i * 2) + 3)
						% (m_polyNumPoints * 2)];
				final double vX = x1 - x0;
				final double vY = y1 - y0;
				final double len = Math.sqrt((vX * vX) + (vY * vY));
				final double vNormX = vX / len;
				final double vNormY = vY / len;
				m_fooPolyCoords[i * 4] = x0 + (vNormY * trueOffset);
				m_fooPolyCoords[(i * 4) + 1] = y0 - (vNormX * trueOffset);
				m_fooPolyCoords[(i * 4) + 2] = x1 + (vNormY * trueOffset);
				m_fooPolyCoords[(i * 4) + 3] = y1 - (vNormX * trueOffset);
			}

			int inx = 0;

			for (int i = 0; i < m_polyNumPoints; i++) {
				if (segmentIntersection // We could perhaps use the sign of a
				// cross
				(
						m_ptsBuff, // product to perform this test quicker.
						m_fooPolyCoords[(i * 4) + 2], // Because non-convex
						// polygons are
						m_fooPolyCoords[(i * 4) + 3], // rare, we will almost
						// never use
						m_fooPolyCoords[i * 4], // the computed intersection
						// point.
						m_fooPolyCoords[(i * 4) + 1],
						m_fooPolyCoords[((i * 4) + 4) % (m_polyNumPoints * 4)],
						m_fooPolyCoords[((i * 4) + 5) % (m_polyNumPoints * 4)],
						m_fooPolyCoords[((i * 4) + 6) % (m_polyNumPoints * 4)],
						m_fooPolyCoords[((i * 4) + 7) % (m_polyNumPoints * 4)])) {
					m_foo2PolyCoords[inx++] = m_ptsBuff[0];
					m_foo2PolyCoords[inx++] = m_ptsBuff[1];
					m_fooRoundedCorners[i] = false;
				} else {
					m_foo2PolyCoords[inx++] = m_fooPolyCoords[(i * 4) + 2];
					m_foo2PolyCoords[inx++] = m_fooPolyCoords[(i * 4) + 3];
					m_foo2PolyCoords[inx++] = m_fooPolyCoords[((i * 4) + 4)
							% (m_polyNumPoints * 4)];
					m_foo2PolyCoords[inx++] = m_fooPolyCoords[((i * 4) + 5)
							% (m_polyNumPoints * 4)];
					m_fooRoundedCorners[i] = true;
				}
			}

			final int foo2Count = inx;
			inx = 0;

			for (int i = 0; i < m_polyNumPoints; i++) {
				if (m_fooRoundedCorners[i]) {
					if (segmentIntersection(m_ptsBuff, ptX, ptY, centerX,
							centerY, m_foo2PolyCoords[inx++],
							m_foo2PolyCoords[inx++], m_foo2PolyCoords[inx],
							m_foo2PolyCoords[inx + 1])) {
						final double segXsectX = m_ptsBuff[0];
						final double segXsectY = m_ptsBuff[1];
						final int numXsections = bad_circleIntersection(
								m_ptsBuff,
								ptX,
								ptY,
								centerX,
								centerY,
								m_polyCoords[2 * ((i + 1) % m_polyNumPoints)],
								m_polyCoords[(2 * ((i + 1) % m_polyNumPoints)) + 1],
								trueOffset);

						// We don't expect tangential intersections because of
						// constraints on allowed polygons. Therefore, if the
						// circle
						// intersects the edge segment in only one point, then
						// that
						// intersection point is the "outer arc" only if the
						// edge segment
						// intersection point with the corner polygon segment
						// (the arc
						// approximation) lies between the center of the polygon
						// and
						// this one circle intersection point.
						if ((numXsections == 2)
								|| ((numXsections == 1)
										&& (Math.min(centerX, m_ptsBuff[0]) <= segXsectX)
										&& (segXsectX <= Math.max(centerX,
												m_ptsBuff[0]))
										&& (Math.min(centerY, m_ptsBuff[1]) <= segXsectY) && (segXsectY <= Math
										.max(centerY, m_ptsBuff[1])))) {
							returnVal[0] = (float) m_ptsBuff[0]; // The first
							// returnVal
							// is

							returnVal[1] = (float) m_ptsBuff[1]; // closer to
							// (ptX,
							// ptY);
							// see API.

							return true;
						} else {
							// The edge segment didn't quite make it to the
							// outer section
							// of the circle; only the inner part was
							// intersected.
							return false;
						}
					} else if (segmentIntersection // Test against the true
					// line segment
					(
							m_ptsBuff, // that comes after the arc.
							ptX, ptY, centerX, centerY,
							m_foo2PolyCoords[inx++], m_foo2PolyCoords[inx++],
							m_foo2PolyCoords[inx % foo2Count],
							m_foo2PolyCoords[(inx + 1) % foo2Count])) {
						returnVal[0] = (float) m_ptsBuff[0];
						returnVal[1] = (float) m_ptsBuff[1];

						return true;
					}
				} else { // Not a rounded corner here.

					if (segmentIntersection(m_ptsBuff, ptX, ptY, centerX,
							centerY, m_foo2PolyCoords[inx++],
							m_foo2PolyCoords[inx++], m_foo2PolyCoords[inx
									% foo2Count], m_foo2PolyCoords[(inx + 1)
									% foo2Count])) {
						returnVal[0] = (float) m_ptsBuff[0];
						returnVal[1] = (float) m_ptsBuff[1];

						return true;
					}
				}
			}

			return false;
		}
	}

	/*
	 * Computes the intersection of the line segment from (x1,y1) to (x2,y2)
	 * with the line segment from (x3,y3) to (x4,y4). If no intersection exists,
	 * returns false. Otherwise returns true, and returnVal[0] is set to be the
	 * X coordinate of the intersection point and returnVal[1] is set to be the
	 * Y coordinate of the intersection point. If more than one intersection
	 * point exists, "the intersection point" is defined to be the intersection
	 * point closest to (x1,y1). A note about overlapping line segments. Because
	 * of floating point numbers' inability to be totally accurate, it is quite
	 * difficult to represent overlapping line segments with floating point
	 * coordinates without using an absolute-precision math package. Because of
	 * this, poorly behaved outcome may result when computing the intersection
	 * of two [nearly] overlapping line segments. The only way around this would
	 * be to round intersection points to the nearest 32-bit floating point
	 * quantity. But then dynamic range is greatly compromised.
	 */
	private final static boolean segmentIntersection(final double[] returnVal,
			double x1, double y1, double x2, double y2, double x3, double y3,
			double x4, double y4) {
		// Arrange the segment endpoints such that in segment 1, y1 >= y2
		// and such that in segment 2, y3 >= y4.
		boolean s1reverse = false;

		if (y2 > y1) {
			s1reverse = !s1reverse;

			double temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		if (y4 > y3) {
			double temp = x3;
			x3 = x4;
			x4 = temp;
			temp = y3;
			y3 = y4;
			y4 = temp;
		}

		/*
		 * 
		 * Note: While this algorithm for computing an intersection is
		 * completely bulletproof, it's not a straighforward 'classic'
		 * bruteforce method. This algorithm is well-suited for an
		 * implementation using fixed-point arithmetic instead of floating-point
		 * arithmetic because all computations are constrained to a certain
		 * dynamic range relative to the input parameters.
		 * 
		 * We're going to reduce the problem in the following way:
		 * 
		 * 
		 * (x1,y1) + \ \ \ (x3,y3) x1 x3 ---------+------+----------- yMax
		 * ---------+------+----------- yMax \ | \ | \ | \ | \ | \ | \ | \ \ | \ |
		 * =====\ \ | \| > \| + =====/ + (x,y) |\ / |\ | \ | \ | \ | \
		 * ----------------+---+------- yMin ----------------+---+------ yMin |
		 * (x2,y2) x4 x2 | | + If W := (x2-x4) / ((x2-x4) + (x3-x1)) , then
		 * (x4,y4) x = x2 + W*(x1-x2) and y = yMin + W*(yMax-yMin)
		 * 
		 * 
		 */
		final double yMax = Math.min(y1, y3);
		final double yMin = Math.max(y2, y4);

		if (yMin > yMax) {
			return false;
		}

		if (y1 > yMax) {
			x1 = x1 + (((x2 - x1) * (yMax - y1)) / (y2 - y1));
			y1 = yMax;
		}

		if (y3 > yMax) {
			x3 = x3 + (((x4 - x3) * (yMax - y3)) / (y4 - y3));
			y3 = yMax;
		}

		if (y2 < yMin) {
			x2 = x1 + (((x2 - x1) * (yMin - y1)) / (y2 - y1));
			y2 = yMin;
		}

		if (y4 < yMin) {
			x4 = x3 + (((x4 - x3) * (yMin - y3)) / (y4 - y3));
			y4 = yMin;
		}

		// Handling for yMin == yMax. That is, in the reduced problem, both
		// segments are horizontal.
		if (yMin == yMax) {
			// Arrange the segment endpoints such that in segment 1, x1 <= x2
			// and such that in segment 2, x3 <= x4.
			if (x2 < x1) {
				s1reverse = !s1reverse;

				double temp = x1;
				x1 = x2;
				x2 = temp;
				temp = y1;
				y1 = y2;
				y2 = temp;
			}

			if (x4 < x3) {
				double temp = x3;
				x3 = x4;
				x4 = temp;
				temp = y3;
				y3 = y4;
				y4 = temp;
			}

			final double xMin = Math.max(x1, x3);
			final double xMax = Math.min(x2, x4);

			if (xMin > xMax) {
				return false;
			} else {
				if (s1reverse) {
					returnVal[0] = Math.max(xMin, xMax);
				} else {
					returnVal[0] = Math.min(xMin, xMax);
				}

				returnVal[1] = yMin; // == yMax

				return true;
			}
		}

		// It is now true that yMin < yMax because we've fully handled
		// the yMin == yMax case above.
		// Following if statement checks for a "twist" in the line segments.
		if (((x1 < x3) && (x2 < x4)) || ((x3 < x1) && (x4 < x2))) {
			return false;
		}

		// The segments are guaranteed to intersect.
		if ((x1 == x3) && (x2 == x4)) { // The segments overlap.

			if (s1reverse) {
				returnVal[0] = x2;
				returnVal[1] = y2;
			} else {
				returnVal[0] = x1;
				returnVal[1] = y1;
			}
		}

		// The segments are guaranteed to intersect in exactly one point.
		final double W = (x2 - x4) / ((x2 - x4) + (x3 - x1));
		returnVal[0] = x2 + (W * (x1 - x2));
		returnVal[1] = yMin + (W * (yMax - yMin));

		return true;
	}

	/*
	 * Computes the intersection of the line segment from (x1,y1) to (x2,y2)
	 * with the circle at center (cX,cY) and radius specified. Returns the
	 * number of intersection points. The returnVal parameter passed in should
	 * be of length 4, and values written to it are such: returnVal[0] - x
	 * component of first intersection point returnVal[1] - y component of first
	 * intersection point returnVal[2] - x component of second intersection
	 * point returnVal[3] - y component of second intersection point
	 * Furthermore, if more than one point is returned, then the first point
	 * returned shall be closer to (x1,y1). Note: I don't like the
	 * implementation of this method because the computation blows up when the
	 * line segment endpoints are close together. Luckily, the way that this
	 * method is used from within this class prevents such blowing up. However,
	 * I have named this method bad_*() because I don't want this code to become
	 * a generic routine that is used outside the scope of this class.
	 */
	private final static int bad_circleIntersection(final double[] returnVal,
			final double x1, final double y1, final double x2, final double y2,
			final double cX, final double cY, final double radius) {
		final double vX = x2 - x1;
		final double vY = y2 - y1;

		if ((vX == 0.0d) && (vY == 0.0d)) {
			throw new IllegalStateException(
					"the condition of both line segment endpoint being the same "
							+ "will not occur if polygons are star-shaped with no marginal "
							+ "conditions");
		}

		final double a = (vX * vX) + (vY * vY);
		final double b = 2 * ((vX * (x1 - cX)) + (vY * (y1 - cY)));
		final double c = ((cX * cX) + (cY * cY) + (x1 * x1) + (y1 * y1))
				- (2 * ((cX * x1) + (cY * y1))) - (radius * radius);
		final double sq = (b * b) - (4 * a * c);

		if (sq < 0.0d) {
			return 0;
		}

		final double sqrt = Math.sqrt(sq);

		if (sqrt == 0.0d) { // Exactly one solution for infinite line.

			final double u = -b / (2 * a);

			if (!((u <= 1.0d) && (u >= 0.0d))) {
				return 0;
			}

			returnVal[0] = x1 + (u * vX);
			returnVal[1] = y1 + (u * vY);

			return 1;
		} else { // Two solutions for infinite line.

			double u1 = (-b + sqrt) / (2 * a);
			double u2 = (-b - sqrt) / (2 * a);

			if (u2 < u1) {
				double temp = u1;
				u1 = u2;
				u2 = temp;
			}

			// Now u1 is less than or equal to u2.
			int solutions = 0;

			if ((u1 <= 1.0d) && (u1 >= 0.0d)) {
				returnVal[0] = x1 + (u1 * vX);
				returnVal[1] = y1 + (u1 * vY);
				solutions++;
			}

			if ((u2 <= 1.0d) && (u2 >= 0.0d)) {
				returnVal[solutions * 2] = x1 + (u2 * vX);
				returnVal[(solutions * 2) + 1] = y1 + (u2 * vY);
				solutions++;
			}

			return solutions;
		}
	}

	/**
	 * This method will render text very quickly. Translucent colors are not
	 * supported by the low detail rendering methods.
	 * <p>
	 * For the sake of maximum performance, this method works differently from
	 * the other rendering methods with respect to the scaling factor specified
	 * in clear(). That is, the font used to render the specified text will not
	 * be scaled; its exact size will be used when the text is rendered onto the
	 * underlying image. On the other hand, the parameters xCenter and yCenter
	 * specify coordinates in the node coordinate system, and so the point
	 * (xCenter, yCenter) will be transformed according the clear() transform in
	 * determining the location of the text on the underlying image.
	 * 
	 * @param font
	 *            the font to use in drawing text; the size of this font
	 *            specifies the actual point size of what will be rendered onto
	 *            the underlying image.
	 * @param text
	 *            the text to render.
	 * @param xCenter
	 *            the X coordinate of the center point of where to place the
	 *            rendered text; specified in the node coordinate system.
	 * @param yCenter
	 *            the Y coordinate of the center point of where to place the
	 *            rendered text; specified in the node coordinate system.
	 * @param color
	 *            the [fully opaque] color to use in rendering the text.
	 * @exception IllegalArgumentException
	 *                if color is not opaque.
	 */
	public final void drawTextLow(final Font font, final String text,
			final float xCenter, final float yCenter, final Color color) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!m_cleared) {
				throw new IllegalStateException(
						"clear() has not been called previously");
			}

			if (color.getAlpha() != 255) {
				throw new IllegalStateException("color is not opaque");
			}
		}

		if (m_gMinimal == null) {
			makeMinimalGraphics();
		}

		m_ptsBuff[0] = xCenter;
		m_ptsBuff[1] = yCenter;
		m_currXform.transform(m_ptsBuff, 0, m_ptsBuff, 0, 1);
		m_gMinimal.setFont(font);

		final FontMetrics fMetrics = m_gMinimal.getFontMetrics();
		m_gMinimal.setColor(color);
		m_gMinimal
				.drawString(
						text,
						(int) ((-0.5d * fMetrics.stringWidth(text)) + m_ptsBuff[0]),
						(int) ((0.5d * fMetrics.getHeight())
								- fMetrics.getDescent() + m_ptsBuff[1]));
	}

	/**
	 * Returns the context that is used by drawTextLow() to produce text shapes
	 * to be drawn to the screen. The transform contained in the returned
	 * context specifies the <i>only</i> scaling that will be done to fonts
	 * when rendering text using drawTextLow(). This transform does not change
	 * between frames no matter what scaling factor is specified in clear().
	 */
	public final FontRenderContext getFontRenderContextLow() {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}
		}

		if (m_gMinimal == null) {
			makeMinimalGraphics();
		}

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
	public final void drawTextFull(final Font font, final double scaleFactor,
			final String text, final float xCenter, final float yCenter,
			final float theta, final Paint paint, final boolean drawTextAsShape) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!m_cleared) {
				throw new IllegalStateException(
						"clear() has not been called previously");
			}

			if (!(scaleFactor >= 0.0d)) {
				throw new IllegalArgumentException(
						"scaleFactor must be positive");
			}
		}

		m_g2d.translate(xCenter, yCenter);
		m_g2d.scale(scaleFactor, scaleFactor);

		if (theta != 0.0f) {
			m_g2d.rotate(theta);
		}

		m_g2d.setPaint(paint);

		if (drawTextAsShape) {
			final GlyphVector glyphV;

			if (text.length() > m_charBuff.length) {
				m_charBuff = new char[Math.max(m_charBuff.length * 2, text
						.length())];
			}

			text.getChars(0, text.length(), m_charBuff, 0);
			glyphV = font.layoutGlyphVector(getFontRenderContextFull(),
					m_charBuff, 0, text.length(), Font.LAYOUT_NO_LIMIT_CONTEXT);

			final Rectangle2D glyphBounds = glyphV.getLogicalBounds();
			m_g2d.translate(-glyphBounds.getCenterX(), -glyphBounds
					.getCenterY());
			m_g2d.fill(glyphV.getOutline());
		} else {
			// Note: A new Rectangle2D is being constructed by this method call.
			// As far as I know this performance hit is unavoidable.
			final Rectangle2D textBounds = font.getStringBounds(text,
					getFontRenderContextFull());
			m_g2d.translate(-textBounds.getCenterX(), -textBounds.getCenterY());
			m_g2d.setFont(font);
			m_g2d.drawString(text, 0.0f, 0.0f);
		}

		m_g2d.setTransform(m_currNativeXform);
	}

	/**
	 * Returns the context that is used by drawTextFull() to produce text shapes
	 * to be drawn to the screen. This context always has the identity
	 * transform.
	 */
	public final FontRenderContext getFontRenderContextFull() {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}
		}

		return m_fontRenderContextFull;
	}

	/**
	 * Fills an arbitrary graphical shape with high detail.
	 * <p>
	 * This method will not work unless clear() has been called at least once
	 * previously.
	 * 
	 * @param shape
	 *            the shape to fill; the shape is specified in node coordinates.
	 * @param xOffset
	 *            in node coordinates, a value to add to the X coordinates of
	 *            the shape's definition.
	 * @param yOffset
	 *            in node coordinates, a value to add to the Y coordinates of
	 *            the shape's definition.
	 * @param paint
	 *            the paint to use when filling the shape.
	 */
	public final void drawCustomGraphicFull(final Shape shape,
			final float xOffset, final float yOffset, final Paint paint) {
		if (m_debug) {
			if (!EventQueue.isDispatchThread()) {
				throw new IllegalStateException(
						"calling thread is not AWT event dispatcher");
			}

			if (!m_cleared) {
				throw new IllegalStateException(
						"clear() has not been called previously");
			}
		}

		m_g2d.translate(xOffset, yOffset);
		m_g2d.setPaint(paint);
		m_g2d.fill(shape);
		m_g2d.setTransform(m_currNativeXform);
	}

	private enum ShapeTypes {
		NODE_SHAPE, ARROW_SHAPE, LINE_STROKE;
	}
}
