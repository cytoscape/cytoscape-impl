package org.cytoscape.ding.impl.cyannotator.annotations;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.modify.mArrow;
import org.cytoscape.ding.impl.cyannotator.modify.mTextAnnotation;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;

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

//A BasicTextAnnotation Class
//
// TODO: We need to refactor this into an Annotation base class and various Annotation
// types.

public class ArrowAnnotation extends Annotation {
	private int source = 0;
	private static final String SOURCE="source";
	private static final String ARROWCOLOR="arrowColor";
	private static final String ARROWTHICKNESS="arrowThickness";

	public static final String NAME="ARROW";

	private BasicStroke arrowStroke=new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private Color arrowColor=Color.BLACK;

	public ArrowAnnotation() { super(); }

	public ArrowAnnotation(CyAnnotator cyAnnotator, DGraphView view, int x, int y, int compCount, int source, double zoom){
		super(cyAnnotator, view, x, y, compCount, zoom);
		this.source = source;
	}

	// This constructor is used to construct a text annotation from an
	// argument map.
	public ArrowAnnotation(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		super(cyAnnotator, view,argMap);
		source = Integer.parseInt(argMap.get(SOURCE));
		arrowColor = getColor(argMap.get(ARROWCOLOR));
		float thickness = Float.parseFloat(argMap.get(ARROWTHICKNESS));
		arrowStroke = new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,NAME);
		argMap.put(SOURCE,Integer.toString(source));
		argMap.put(ARROWCOLOR,convertColor(arrowColor));
		argMap.put(ARROWTHICKNESS,Float.toString(arrowStroke.getLineWidth()));
		return argMap;
	}

	//Verification methods
	public boolean isArrowAnnotation() { return true; }
	
	//Get Methods	
	public int getTopX(){
		return getX();
	}

	public int getTopY(){
		return getY();
	}
	
	public int getAnnotationWidth(){
		return 0;
	}

	public int getAnnotationHeight(){
		return 0;
	}
	
	public BasicStroke getArrowStroke(){
		return arrowStroke;
	}

	public Color getArrowColor() {
		return arrowColor;
	}

	public int getSource() {
		return source;
	}

	//Set methods
	public void adjustSpecificZoom(double newZoom){
		font=font.deriveFont(((float)(newZoom/tempZoom))*font.getSize2D());
		tempZoom=newZoom;

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
		updateAnnotationAttributes();
	}

	public void adjustZoom(double newZoom){
		font=font.deriveFont(((float)(newZoom/zoom))*font.getSize2D());

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
		
		adjustArrowThickness(newZoom);
		
		zoom=newZoom;
		updateAnnotationAttributes();
	}
	
	public void setArrowStroke(BasicStroke newStroke) {
		arrowStroke=newStroke;
		updateAnnotationAttributes();
	}

	public void setArrowColor(Color newColor) {
		arrowColor=newColor;
		updateAnnotationAttributes();
	}

	public void setSource(int source) {
		this.source = source;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2=(Graphics2D)g;
		g2.setColor(color);
	}
	
	public void addModifyMenuItem(JPopupMenu popup) {
	}

	class modifyTextAnnotationListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
		}
	}  
}
