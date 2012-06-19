package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.geom.Point2D;

import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.ContentChangeListener;
import org.cytoscape.ding.impl.DGraphView;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;

/**
 *
 * @author Avinash Thummala
 */

//A BasicAnnotation Class
//

public class AbstractAnnotation extends JComponent implements Annotation {
	private static int nextAnnotationNumber = 0;

	private boolean selected=false;

	private double globalZoom = 1.0;
	private double myZoom = 1.0;

	private DGraphView.Canvas canvasName;
	private CyAnnotator cyAnnotator;
	private UUID uuid = UUID.randomUUID();

	private Set<ArrowAnnotation> arrowList;

	protected boolean usedForPreviews=false;
	protected DGraphView view;
	protected ArbitraryGraphicsCanvas canvas;

	protected static final String ID="id";
	protected static final String ZOOM="zoom";
	protected static final String X="x";
	protected static final String Y="y";
	protected static final String CANVAS="canvas";
	protected static final String TYPE="type";
	protected static final String ANNOTATION_ID="uuid";

	public AbstractAnnotation(CyAnnotator cyAnnotator, DGraphView view) {
		this.view = view;
		this.cyAnnotator = cyAnnotator;
		arrowList = new HashSet<ArrowAnnotation>();
		this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
		this.canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
		this.globalZoom = view.getZoom();
	}

	public AbstractAnnotation(AbstractAnnotation c) {
		this.view = c.view;
		this.cyAnnotator = c.cyAnnotator;
		arrowList = new HashSet<ArrowAnnotation>(c.arrowList);
		this.canvas = c.canvas;
		this.canvasName = c.canvasName;
	}

	public AbstractAnnotation(CyAnnotator cyAnnotator, DGraphView view, 
	                          double x, double y, double zoom) {
		this.cyAnnotator = cyAnnotator;
		this.view = view;
		this.globalZoom=zoom;
		this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
		this.canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
		arrowList = new HashSet<ArrowAnnotation>();
		// super.setBackground(Color.BLUE);
		setLocation((int)x, (int)y);
	}

	public AbstractAnnotation(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		this.cyAnnotator = cyAnnotator;
		this.view = view;
		Point2D coords = getComponentCoordinates(argMap);
		this.globalZoom = Double.parseDouble(argMap.get(ZOOM));
		String canvasString = argMap.get(CANVAS);
		if (canvasString != null && canvasString.equals(BACKGROUND)) {
			this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS));
			this.canvasName = DGraphView.Canvas.BACKGROUND_CANVAS;
		} else {
			this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
			this.canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
		}
		arrowList = new HashSet<ArrowAnnotation>();
		setLocation((int)coords.getX(), (int)coords.getY());
		if (argMap.containsKey(ANNOTATION_ID))
			this.uuid = UUID.fromString(argMap.get(ANNOTATION_ID));
		
	}

	public String toString() {
		Map<String,String>argMap = getArgMap();

		return argMap.get("type")+" annotation "+uuid.toString()+" at "+getX()+", "+getY()+" zoom="+globalZoom+" on canvas "+canvasName;
	}

	@Override
	public String getCanvasName() {
		if (canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS))
			return BACKGROUND;
		else
			return FOREGROUND;
	}

	@Override
	public void setCanvas(String cnvs) {
		if (cnvs.equals(BACKGROUND)) {
			canvasName = DGraphView.Canvas.BACKGROUND_CANVAS;
		} else {
			canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
		}
		this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(canvasName));
		for (ArrowAnnotation arrow: arrowList) 
			setCanvas(cnvs);
	}

	@Override
	public void changeCanvas(String cnvs) {
		// Are we really changing anything?
		if ((cnvs.equals(BACKGROUND) && canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS)) ||
		    (cnvs.equals(FOREGROUND) && canvasName.equals(DGraphView.Canvas.FOREGROUND_CANVAS)))
			return;

		for (ArrowAnnotation arrow: arrowList) 
			changeCanvas(cnvs);

		// Remove ourselves from the current canvas
		canvas.remove(this);

		canvas.repaint();  // update the canvas

		// Set the new canvas
		setCanvas(cnvs);

		// Add ourselves
		canvas.add(this);

		canvas.repaint();  // update the canvas
	}

	@Override
	public JComponent getCanvas() {
		return (JComponent)canvas;
	}

	public JComponent getComponent() {
		return (JComponent)this;
	}

	public UUID getUUID() {
		return uuid;
	}

	@Override
	public void addComponent(JComponent cnvs) {
		if (cnvs == null && canvas != null) {

		} else if (cnvs == null) {
			setCanvas(FOREGROUND);
		} else {
			if (cnvs.equals(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS)))
				setCanvas(BACKGROUND);
			else
				setCanvas(FOREGROUND);
		}
		canvas.add(this.getComponent());
		canvas.setComponentZOrder(this, 0);
	}
    
	@Override
	public CyAnnotator getCyAnnotator() {return cyAnnotator;}
    
	public void moveAnnotation(Point2D location) {
		if (!(this instanceof ArrowAnnotation)) {
			setLocation((int)location.getX(), (int)location.getY());
			cyAnnotator.moveAnnotation(this);
		} else {
			cyAnnotator.positionArrow((ArrowAnnotation)this);
		}
	}

	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		canvas.modifyComponentLocation(x, y, this);
	}

	public boolean contains(int x, int y) {
		if (x > getX() && y > getY() && x-getX() < getWidth() && y-getY() < getHeight())
			return true;
		return false;
	}

	public void removeAnnotation() {
		canvas.remove(this);
		cyAnnotator.removeAnnotation(this);
		for (ArrowAnnotation arrow: arrowList) 
			removeAnnotation();
	}

	public void resizeAnnotation(double width, double height) {};

	public double getZoom() { return globalZoom; }
	public void setZoom(double zoom) { 
		globalZoom = zoom; 
	}
      
	public double getSpecificZoom() {return myZoom; }
	public void setSpecificZoom(double zoom) {
		myZoom = zoom; 
	}

	public boolean isSelected() { return selected; }
	public void setSelected(boolean selected) {
		this.selected = selected;
		cyAnnotator.setSelectedAnnotation(this, selected);
	}

	public void addArrow(ArrowAnnotation arrow) {
		arrowList.add(arrow);
	}
	public void removeArrow(ArrowAnnotation arrow) {
		arrowList.remove(arrow);
	}
	public Set<ArrowAnnotation> getArrows() { return arrowList; }

	@Override
	public Map<String,String> getArgMap() {
		Map<String, String> argMap = new HashMap<String, String>();
		addNodeCoordinates(argMap);
		argMap.put(ZOOM,Double.toString(this.globalZoom));
		if (canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS))
			argMap.put(CANVAS, BACKGROUND);
		else
			argMap.put(CANVAS, FOREGROUND);
		argMap.put(ANNOTATION_ID, this.uuid.toString());

		return argMap;
	}

	public boolean usedForPreviews() { return usedForPreviews; }

	public void setUsedForPreviews(boolean v) { usedForPreviews = v; }

	public void drawAnnotation(Graphics g, double x,
	                           double y, double scaleFactor) {
	}

	public void update() {
		updateAnnotationAttributes();
		getCanvas().repaint();
	}

	// Component overrides
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;

		/* Set up all of our anti-aliasing, etc. here to avoid doing it redundantly */
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
		                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

    // High quality color rendering is ON.
    g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                        RenderingHints.VALUE_COLOR_RENDER_QUALITY);

		g2.setRenderingHint(RenderingHints.KEY_DITHERING,
		                    RenderingHints.VALUE_DITHER_ENABLE);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		// Text antialiasing is ON.
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
		                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
		                    RenderingHints.VALUE_STROKE_PURE);


		if (!usedForPreviews()) {
			// We need to control composite ourselves for previews...
			g2.setComposite(AlphaComposite.Src);
		}
	}

	public JFrame getModifyDialog() {return null;}

	// Protected methods
	protected void updateAnnotationAttributes() {
		if (!usedForPreviews) {
			cyAnnotator.addAnnotation(this);
			// if (arrowList != null) {
			// 	for (ArrowAnnotation annotation: arrowList) {
			// 		cyAnnotator.addAnnotation(annotation);
			// 	}
			// }
			contentChanged();
		}
	}

  protected String convertColor(Paint clr) {
		if (clr == null)
			return null;
		if (clr instanceof Color)
			return Integer.toString(((Color)clr).getRGB());
		return clr.toString();
  }

  protected Color getColor(String strColor) {
		if (strColor == null)
			return null;
		return new Color(Integer.parseInt(strColor));
  }

  protected Color getColor(Map<String, String> argMap, String key, Color defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return new Color(Integer.parseInt(argMap.get(key)));
	}

  protected Float getFloat(Map<String, String> argMap, String key, float defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Float.parseFloat(argMap.get(key));
	}

  protected Integer getInteger(Map<String, String> argMap, String key, int defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Integer.parseInt(argMap.get(key));
	}

  protected Double getDouble(Map<String, String> argMap, String key, double defValue) {
		if (!argMap.containsKey(key) || argMap.get(key) == null)
			return defValue;
		return Double.parseDouble(argMap.get(key));
	}

	// Private methods
	private void addNodeCoordinates(Map<String, String> argMap) {
		Point2D xy = getNodeCoordinates(getX(), getY());
		argMap.put(X,Double.toString(xy.getX()));
		argMap.put(Y,Double.toString(xy.getY()));
	}

	protected Point2D getComponentCoordinates(Map<String, String> argMap) {
		// Get our current transform
		double[] nextLocn = new double[2];
		nextLocn[0] = Double.parseDouble(argMap.get(X));
		nextLocn[1] = Double.parseDouble(argMap.get(Y));

		view.xformNodeToComponentCoords(nextLocn);
		
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	protected Point2D getNodeCoordinates(double x, double y) {
    // Get our current transform
		double[] nextLocn = new double[2];
		nextLocn[0] = x;
		nextLocn[1] = y;
		view.xformComponentToNodeCoords(nextLocn);
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	protected Point2D getComponentCoordinates(double x, double y) {
		double[] nextLocn = new double[2];
		nextLocn[0] = x;
		nextLocn[1] = y;

		view.xformNodeToComponentCoords(nextLocn);
		
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}

	public void contentChanged() {
		if (view == null) return;
		final ContentChangeListener lis = view.getContentChangeListener();
		if (lis != null)
			lis.contentChanged();
	}

}
