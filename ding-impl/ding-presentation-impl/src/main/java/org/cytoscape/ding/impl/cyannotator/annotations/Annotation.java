package org.cytoscape.ding.impl.cyannotator.annotations;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.modify.mArrow;
import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.ContentChangeListener;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.model.CyNetwork;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Avinash Thummala
 */

//A BasicAnnotation Class
//

public class Annotation extends Component {
	protected int componentNumber=0;
	protected boolean selected=false;

	protected double zoom, tempZoom;
	protected boolean usedForPreviews=false;

	protected ArbitraryGraphicsCanvas canvas;
	protected DGraphView.Canvas canvasName;

	protected Color color=Color.BLACK;
	protected CyNetwork network;
	protected List<ArrowAnnotation> arrowList;

	// These should migrate to the ArrowAnnotation
	protected boolean pointOnArrow=false;
	protected double arrowLength=7.0;
	protected boolean drawArrow=false, arrowDrawn=false;
	protected int arrowIndex=0;

	// These allow us to have a shared shape drawing routine
	protected int rVal=5;
	protected int shapeType=1;
	protected Color fillColor=null, edgeColor=Color.BLACK;

	// arguments that are common to more than one annotation type
	protected static final String TYPE="type";
	protected static final String X="x";
	protected static final String Y="y";
	protected static final String TEXT="text";
	protected static final String ID="id";
	protected static final String ZOOM="zoom";
	protected static final String COLOR="color";
	protected static final String FONTFAMILY="fontFamily";
	protected static final String FONTSIZE="fontSize";
	protected static final String FONTSTYLE="fontStyle";
	protected static final String CANVAS="canvas";
	protected static final String BACKGROUND="background";
	protected static final String FOREGROUND="foreground";

	protected final DGraphView view;
	protected final CyAnnotator cyAnnotator;

	public Annotation() {
		view = null;
		cyAnnotator = null;
	}

	public Annotation(CyAnnotator cyAnnotator, DGraphView view, int x, int y, int compCount, double zoom){
		this.cyAnnotator = cyAnnotator;
		this.view = view;
		this.componentNumber=compCount;
		this.zoom=zoom;
		this.setLocation(x, y);
		this.network = view.getModel(); 
		this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
	}

	// This constructor is used to construct a text annotation from an
	// argument map.
	public Annotation(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		this.cyAnnotator = cyAnnotator;
		this.view = view;
		getNodeCoordinates(argMap);
		this.componentNumber = Integer.parseInt(argMap.get(ID));
		this.zoom = Double.parseDouble(argMap.get(ZOOM));
		this.network = view.getModel(); 
		String canvasString = argMap.get(CANVAS);
		if (canvasString != null && canvasString.equals(BACKGROUND)) {
			this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS));
			this.canvasName = DGraphView.Canvas.BACKGROUND_CANVAS;
		} else {
			this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
			this.canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
		}
	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = new HashMap<String, String>();
		addNodeCoordinates(argMap);
		argMap.put(ID,Integer.toString(this.componentNumber));
		argMap.put(ZOOM,Double.toString(this.zoom));
		return argMap;
	}

	public ArbitraryGraphicsCanvas getCanvas() {return this.canvas;}

	protected void addNodeCoordinates(Map<String, String> argMap) {
		Point xy = getNodeCoordinates(getX(), getY());
		argMap.put(X,Double.toString(xy.getX()));
		argMap.put(Y,Double.toString(xy.getY()));
	}

	protected void getNodeCoordinates(Map<String, String> argMap) {
		// Get our current transform
		double[] nextLocn = new double[2];
		nextLocn[0] = Double.parseDouble(argMap.get(X));
		nextLocn[1] = Double.parseDouble(argMap.get(Y));
		// Transform the coordinates
		AffineTransform t = ((InnerCanvas)view.getCanvas()).getAffineTransform();
		if (t != null) {
			t.transform(nextLocn, 0, nextLocn, 0, 1);
		}
		this.setLocation((int)nextLocn[0],(int)nextLocn[1]);

	}

	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
	}

	public void updateAnnotationAttributes() {
		if (!usedForPreviews) {
			cyAnnotator.addAnnotation(this);
			if (arrowList != null) {
				for (ArrowAnnotation annotation: arrowList) {
					cyAnnotator.addAnnotation(annotation);
				}
			}
			contentChanged();
		}
	}

	public DGraphView getView() {
		return view;
	}

	protected Point getNodeCoordinates(int x, int y) {
		// Transform our coordinates into node coordinates
		double[] nextLocn = new double[2];
		nextLocn[0] = x;
		nextLocn[1] = y;
		view.xformComponentToNodeCoords(nextLocn);
		Point p =  new Point();
		p.setLocation(nextLocn[0], nextLocn[1]);
		return p;
	}

  protected Color getColor(String strColor) {
		if (strColor == null)
			return null;
		return new Color(Integer.parseInt(strColor));
  }

  protected String convertColor(Color clr) {
		if (clr == null)
			return null;
		return Integer.toString(clr.getRGB());
  }

	public int getAnnotationHeight() { return 0; }

	public int getAnnotationWidth() { return 0; }

	public void addModifyMenuItem(JPopupMenu menu) {}

	//Verification methods
	public boolean isImageAnnotation() { return false; }

	public boolean isShapeAnnotation() { return false; }

	public boolean isTextAnnotation() { return false; }
	
	public boolean isBoundedAnnotation() { return false; }

	public boolean isArrowAnnotation() { return false; }

	public boolean isSelected() { return selected; }

	public CyNetwork getNetwork() { return network; }

	// These methods should all be overridden, if used
	public void setTextColor(Color clr) {}
	public void setFillColor(Color clr) {}
	public void setEdgeColor(Color clr) {}

	public boolean usedForPreviews() { return this.usedForPreviews; }
	public void setUsedForPreviews(boolean v) { this.usedForPreviews = v; }
	
	public boolean isPointOnLine(int x, int y, int x1, int y1, int x2, int y2)
	{		
		if( Math.abs( Math.abs( (y-y1)*(x2-x1) )-Math.abs( (y2-y1)*(x-x1) ) )<120 )
			return true;
		else
			return false;
	}
	
	public boolean isPointOnThickLine(int x, int y, int x1, 
	                                  int y1, int x2, int y2, int xCounter, int yCounter)
	{
		int numRounds=3;

		for(int i=numRounds;i>=0;i--) {
			if(isPointOnLine(x, y, x1+i*xCounter, y1+i*yCounter, x2+i*xCounter, y2+i*yCounter))
				return true;
		}
		
		xCounter*=-1;
		yCounter*=-1;
		
		for(int i=numRounds;i>=1;i--) {
			if(isPointOnLine(x, y, x1+i*xCounter, y1+i*yCounter, x2+i*xCounter, y2+i*yCounter))
				return true;
		}		
		
		return false;
	}	
	
	public boolean isPointOnArrow(int x, int y, int x1, int y1, int x2, int y2) {
		if(x2>=x1)
		{
			if(y2>=y1)
			{				
				if( isPointOnThickLine(x, y, x1, y1, x2, y2, -1, 1) )
					return true;
			}
			else
			{							
				if( isPointOnThickLine(x, y, x1, y1, x2, y2, -1, -1))
					return true;				
			}
		} else {
			if(y2>=y1)
			{				
				if( isPointOnThickLine(x, y, x2, y2, x1, y1, -1, -1))
					return true;
			}
			else
			{				
				if( isPointOnThickLine(x, y, x2, y2, x1, y1, -1, 1))
					return true;				
			}
		}
		return false;
	}
	
	public int isPointOnArrow(int x, int y) {
		if(!arrowDrawn)
			return -1;	
		
		for (int i = 0; i < arrowList.size(); i++) {
			Annotation arrowPoint = arrowList.get(i);
			Point p=getArrowStartPoint(arrowPoint);
			if( isPointOnArrow(x, y, p.x, p.y, arrowPoint.getX(), arrowPoint.getY()) )
				return i;
		}

		return -1;
	}
	
	public boolean isPointInComponentOnly(int pX, int pY){
		int x=getX(), y=getY();
		if(pX>=x && pX<=(x+getAnnotationWidth()) && pY>=y && pY<=(y+getAnnotationHeight()) )
			return true;
		else 
			return false;			
	}	

	public boolean isPointInComponent(int pX, int pY){
		if( isPointInComponentOnly(pX,pY) ) {
			return true;
		} else {
			int temp=isPointOnArrow(pX, pY);
			
			arrowIndex=temp;
			
			if(arrowIndex==-1)
				pointOnArrow=false;
			else
				pointOnArrow=true;

			return pointOnArrow;
		}
	}

	//Get Methods	
	public int getZone(int x, int y) {
		if(isPointInComponentOnly(x, y))
			return 0;

		int midX=getTopX()+getAnnotationWidth()/2, midY=getTopY();

		if(x<=midX) {
			if(y<=midY)
				return 3;
			else if(y<=midY+getAnnotationHeight())
				return 4;
			else
				return 5;
		} else {
			if(y<=midY)
				return 2;
			else if(y<=midY+getAnnotationHeight())
				return 1;
			else
				return 6;
		}
	}

	public int getQuadrant(Point p1, Point p2) {
		if(p2.x >= p1.x) {
			if(p2.y<=p1.y)
				return 1;
			else
				return 4;
		} else {
			if(p2.y<=p1.y)
				return 2;
			else
				return 3;
		}
	}

	public Point getArrowStartPoint(Annotation temp){
		int x=0, y=0, zone=getZone(temp.getX(), temp.getY());
		if(zone==1) {
			x=getTopX()+getAnnotationWidth();
			y=getTopY()+getAnnotationHeight()/2;
		} else if(zone==2 || zone==3) {
			x=getTopX()+getAnnotationWidth()/2;
			y=getTopY();
		} else if(zone==4) {
			x=getTopX();
			y=getTopY()+getAnnotationHeight()/2;
		} else {
			x=getTopX()+getAnnotationWidth()/2;
			y=getTopY()+getAnnotationHeight();
		}
		return new Point(x,y);
	}

	public int getTopX(){
		return getX();
	}

	public int getTopY(){
		return getY();
	}
	
	public boolean getArrowDrawn(){
		return arrowDrawn;
	}

	public double getZoom(){
		return zoom;
	}

	public boolean getDrawArrow(){
		return drawArrow;
	}
	
	public int getComponentNumber(){
		return componentNumber;
	}	
	
	public double getTempZoom(){
		return tempZoom;
	}

	@Override
	public Component getComponentAt(int x, int y) {
		if(isPointInComponent(x,y))
			return this;
		else
			return null;
	}

	//Set methods
	public void addArrow(ArrowAnnotation arrow) {
		if(arrowList==null)
			arrowList=new ArrayList<ArrowAnnotation>();
		arrowList.add(arrow);
		cyAnnotator.getForeGroundCanvas().add(arrow);
		arrowDrawn = true;
	}

	public void setArrowPoints(int pX, int pY) {
		int zone=getZone(pX,pY);
		if(zone==0) {
			arrowDrawn=false;
			return;
		} else {
			if(arrowList==null)
				arrowList=new ArrayList<ArrowAnnotation>();

			//The ArrowEndPoints are also set up as Annotations of null size.
			//They have been implemented this way, so as to handle the change in viewports
			ArrowAnnotation arrowEndPoint=new ArrowAnnotation(cyAnnotator, view, pX, pY, canvas.getComponentCount(), getComponentNumber(), view.getZoom());
			arrowEndPoint.setSize(0, 0);			

			canvas.add(arrowEndPoint);
			arrowList.add(arrowEndPoint);
		}
		updateAnnotationAttributes();
	}
	
	public void setDrawArrow(boolean val) {
		drawArrow=val;
		updateAnnotationAttributes();
	}

	public void setArrowDrawn(boolean val) {
		arrowDrawn=val;
		updateAnnotationAttributes();
	}

	public void setZoom(double zoom) {
		this.zoom=zoom;
		updateAnnotationAttributes();
	}

	public void setSelected(boolean val) {
		this.selected=val;
	}

	public void setComponentNumber(int val) {
		componentNumber=val;
		if (arrowList != null) {
			// Now we need to update the source of all of our arrows
			for (ArrowAnnotation arrow: arrowList) {
				arrow.setSource(val);
			}
		}
		updateAnnotationAttributes();
	}

	public void setTempZoom(double zoom) {
		this.tempZoom=zoom;
		updateAnnotationAttributes();
	}

	public void print(Graphics g) {
		paint(g);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2=(Graphics2D)g;

		//Setting up Anti-aliasing for high quality rendering
		g2.setComposite(AlphaComposite.Src);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		if(arrowDrawn) {
			//For any annotation that points to some locations
			for (ArrowAnnotation arrowPoint: arrowList) {
				Point p=getArrowStartPoint(arrowPoint);
				g2.setColor(arrowPoint.getArrowColor() );
				g2.setStroke(arrowPoint.getArrowStroke() );
				g2.drawLine(p.x, p.y, arrowPoint.getX(), arrowPoint.getY());

				drawArrow(g2, p, new Point(arrowPoint.getX(), arrowPoint.getY()) );
			}
		}
	}  

	public void drawArrow(Graphics2D g, Point p1, Point p2) {
		double angle=Math.atan(((double)(p1.y-p2.y))/((double)(p2.x-p1.x)));
		int quad=getQuadrant(p1, p2);
		
		if(angle >=0 ) {
			if(angle<=Math.PI/4) {
				double m1=Math.tan(angle + 3*Math.PI/4);
				double m2=Math.tan(angle + Math.PI/4);
				if(quad==1) {
					double x2=p2.x-arrowLength/(Math.sqrt(1+m1*m1));
					double y2=p2.y-Math.abs(arrowLength*m1/(Math.sqrt(1+m1*m1)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );

					x2=p2.x-arrowLength/(Math.sqrt(1+m2*m2));
					y2=p2.y+Math.abs(arrowLength*m2/(Math.sqrt(1+m2*m2)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
				} else if(quad==3) {

					double x2=p2.x+arrowLength/(Math.sqrt(1+m1*m1));
					double y2=p2.y+Math.abs(arrowLength*m1/(Math.sqrt(1+m1*m1)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );

					x2=p2.x+arrowLength/(Math.sqrt(1+m2*m2));
					y2=p2.y-Math.abs(arrowLength*m2/(Math.sqrt(1+m2*m2)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
				}
			} else if(angle<=Math.PI/2) {
				double m1=Math.tan(angle - Math.PI/4);
				double m2=Math.tan(angle + Math.PI/4);
				if(quad==1) {
					double x2=p2.x-arrowLength/(Math.sqrt(1+m1*m1));
					double y2=p2.y+Math.abs(arrowLength*m1/(Math.sqrt(1+m1*m1)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );

					x2=p2.x+arrowLength/(Math.sqrt(1+m2*m2));
					y2=p2.y+Math.abs(arrowLength*m2/(Math.sqrt(1+m2*m2)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
				} else if(quad==3) {
					double x2=p2.x+arrowLength/(Math.sqrt(1+m1*m1));
					double y2=p2.y-Math.abs(arrowLength*m1/(Math.sqrt(1+m1*m1)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );

					x2=p2.x-arrowLength/(Math.sqrt(1+m2*m2));
					y2=p2.y-Math.abs(arrowLength*m2/(Math.sqrt(1+m2*m2)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
				}
			}
		} else {
			if(Math.abs(angle)<=Math.PI/4){
				double m1=Math.tan(3*Math.PI/4 + angle);
				double m2=Math.tan(Math.PI/4 + angle);
								
				if(quad==4) {
					double x2=p2.x-arrowLength/(Math.sqrt(1+m1*m1));
					double y2=p2.y-Math.abs(arrowLength*m1/(Math.sqrt(1+m1*m1)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );									

					x2=p2.x-arrowLength/(Math.sqrt(1+m2*m2));
					y2=p2.y+Math.abs(arrowLength*m2/(Math.sqrt(1+m2*m2)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
				} else if(quad==2) {
					double x2=p2.x+arrowLength/(Math.sqrt(1+m1*m1));
					double y2=p2.y+Math.abs(arrowLength*m1/(Math.sqrt(1+m1*m1)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
										
					x2=p2.x+arrowLength/(Math.sqrt(1+m2*m2));
					y2=p2.y-Math.abs(arrowLength*m2/(Math.sqrt(1+m2*m2)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );									   
				}
			} else {
				double m1=Math.tan(3*Math.PI/4 + angle);
				double m2=Math.tan(5*Math.PI/4 + angle);
											   
				if(quad==4) {
					double x2=p2.x+arrowLength/(Math.sqrt(1+m1*m1));
					double y2=p2.y-Math.abs(arrowLength*m1/(Math.sqrt(1+m1*m1)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
									   
					x2=p2.x-arrowLength/(Math.sqrt(1+m2*m2));
					y2=p2.y-Math.abs(arrowLength*m2/(Math.sqrt(1+m2*m2)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
				} else if(quad==2) {
					double x2=p2.x-arrowLength/(Math.sqrt(1+m1*m1));
					double y2=p2.y+Math.abs(arrowLength*m1/(Math.sqrt(1+m1*m1)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );
									   
					x2=p2.x+arrowLength/(Math.sqrt(1+m2*m2));
					y2=p2.y+Math.abs(arrowLength*m2/(Math.sqrt(1+m2*m2)));

					g.drawLine(p2.x, p2.y, (int)Math.round(x2), (int)Math.round(y2) );										
				}
			}
		}
	}

	public void adjustSpecificZoom(double newZoom){ }

	public void adjustZoom(double newZoom){ }
	
	public void adjustArrowThickness(double newZoom){
		if(!arrowDrawn)
			return;
		
		float factor=(float)(newZoom/zoom);
		
		arrowLength*=factor;

		for (ArrowAnnotation arrowPoint: arrowList) {
			BasicStroke stroke=arrowPoint.getArrowStroke();
			arrowPoint.setArrowStroke( new BasicStroke(factor*stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin()) );
		}
		updateAnnotationAttributes();
	}
	
	public JPopupMenu createPopUp(){
		JPopupMenu popup=new JPopupMenu();
		
		if(pointOnArrow)
		{
			JMenuItem modArrow=new JMenuItem("Modify Properties");
			modArrow.addActionListener(new modifyArrowListener());
			popup.add(modArrow);
			
			popup.add(new JSeparator());
			
			JMenuItem remArrow=new JMenuItem("Remove Arrow");
			remArrow.addActionListener(new removeArrowListener());
			
			popup.add(remArrow);			
			
			pointOnArrow=false;
		} else {
			if(!isImageAnnotation())
			{
				addModifyMenuItem(popup);
				popup.add(new JSeparator());
			}

			if (canvasName.equals(DGraphView.Canvas.FOREGROUND_CANVAS)) {
				JMenuItem moveAnnotation=new JMenuItem("Move Annotation to Background");
				moveAnnotation.addActionListener(new moveAnnotationListener());
				popup.add(moveAnnotation);
			} else {
				JMenuItem moveAnnotation=new JMenuItem("Move Annotation to Foreground");
				moveAnnotation.addActionListener(new moveAnnotationListener());
				popup.add(moveAnnotation);
			}
	
			JMenuItem removeAnnotation=new JMenuItem("Remove Annotation");
			removeAnnotation.addActionListener(new removeAnnotationListener());
			popup.add(removeAnnotation);

			if (isShapeAnnotation()) {
				JMenuItem resizeAnnotation=new JMenuItem("Resize Annotation");
				resizeAnnotation.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cyAnnotator.resizeShape((ShapeAnnotation)Annotation.this);
					}
				});
				popup.add(resizeAnnotation);
			}
	
			JMenuItem addArrow=new JMenuItem("Add Arrow");
	
			addArrow.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Annotation.this.setDrawArrow(true);
				}
			});
	
			popup.add(addArrow);
		}
		return popup;
	}

	public void showChangePopup(MouseEvent e) {
		createPopUp().show(e.getComponent(), e.getX(), e.getY());
	}

	public void contentChanged() {
		if (view == null) return;
		final ContentChangeListener lis = view.getContentChangeListener();
		if (lis != null)
			lis.contentChanged();
	}

	protected void drawShape(Graphics2D g2, int x, int y, int width, int height, float stroke) {
		// System.out.println("drawShape: x,y="+x+","+y+" "+width+"x"+height);
		if(shapeType==0) {//Rectangle
			if(fillColor!=null) {
				g2.setColor(fillColor);
				g2.fillRect( x, y, width, height);
			}

			if(isSelected())
				g2.setColor(Color.YELLOW);
			else
				g2.setColor(edgeColor);

			g2.setStroke(new BasicStroke(stroke));
			g2.drawRect(x, y, width, height);
				
		} else if(shapeType==1) {//Rounded Rectangle
			if(fillColor!=null) {
				g2.setColor(fillColor);
				g2.fillRoundRect(x, y, width, height, rVal, rVal);
			}

			if(isSelected())
				g2.setColor(Color.YELLOW);
			else
				g2.setColor(edgeColor);
			
			g2.setStroke(new BasicStroke(stroke));
			g2.drawRoundRect(x, y, width, height, rVal, rVal);

		} else if(shapeType==2) {//Oval
			if(fillColor!=null) {
				g2.setColor(fillColor);
				g2.fillOval( x, y, width, height);
			}

			if(isSelected())
				g2.setColor(Color.YELLOW);
			else
				g2.setColor(edgeColor);
			
			g2.setStroke(new BasicStroke(stroke));
			g2.drawOval(x, y, width, height);
		}
		//Now draw the arrows associated with this annotation
	}
	
	class moveAnnotationListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int remPos=getComponentNumber();
			int num=canvas.getComponentCount();

			// Remove the annotation from the current canvas
			for(int i=remPos+1;i<num;i++)
				((Annotation)canvas.getComponent(i)).setComponentNumber(i-1);

			canvas.remove(Annotation.this);

			// Change our canvas to the new canvas
			if (canvasName.equals(DGraphView.Canvas.FOREGROUND_CANVAS)) {
				canvasName = DGraphView.Canvas.BACKGROUND_CANVAS;
			} else {
				canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
			}
			canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(canvasName));
			canvas.add(Annotation.this);
		}
	}

	class removeArrowListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int remPos=(arrowList.get(arrowIndex)).getComponentNumber();
			int num=canvas.getComponentCount();

			for(int i=remPos+1;i<num;i++)
				((Annotation)canvas.getComponent(i)).setComponentNumber(i-1);

			canvas.remove(arrowList.get(arrowIndex));

			cyAnnotator.removeAnnotation(arrowList.get(arrowIndex));
			arrowList.remove(arrowIndex);
			
			if(arrowList.isEmpty())
				arrowDrawn=false;

			view.updateView();
		}

	}
	
	class modifyArrowListener implements ActionListener{

		public void actionPerformed(ActionEvent e){

			mArrow modArrow=new mArrow(arrowList.get(arrowIndex));

			modArrow.setSize(375, 220);
			modArrow.setVisible(true);
		}

	}	
	
	class resizeAnnotationListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
		}
	}

	class removeAnnotationListener implements ActionListener{

		public void actionPerformed(ActionEvent e){
			//When an Annotation is removed we have to adjust the componentNumbers of the anotations added
			//after this Annotation

			int remPos=getComponentNumber();
			int num=canvas.getComponentCount();

			for(int i=remPos+1;i<num;i++)
				((Annotation)canvas.getComponent(i)).setComponentNumber(i-1);

			canvas.remove(Annotation.this);

			if(getArrowDrawn()){

				for (Annotation arrowPoint: arrowList) {
					remPos=arrowPoint.getComponentNumber()-1;
					num--;

					for(int i=remPos+1;i<num;i++)
						((Annotation)canvas.getComponent(i)).setComponentNumber(i-1);

					cyAnnotator.removeAnnotation(arrowPoint);
					canvas.remove(arrowPoint);
				}
			}

			cyAnnotator.removeAnnotation(Annotation.this);

			// Special handling for images
			if (isImageAnnotation()) {
				((ImageAnnotation)(Annotation.this)).dropImage();
			}
			view.updateView();
		}
	}
}
