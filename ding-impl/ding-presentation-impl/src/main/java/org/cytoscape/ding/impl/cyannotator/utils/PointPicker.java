package org.cytoscape.ding.impl.cyannotator.utils;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.ding.internal.util.MathUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

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
 * Editor that allows users to select a coordinate on a square with the mouse.
 */
@SuppressWarnings("serial")
public class PointPicker extends JPanel {
    
	public static final Point2D DEFAULT_VALUE = new Point2D.Double(0.5, 0.5);
    
	private static final float[] MAIN_XY = { .0f, .25f, 0.5f, .75f, 1.0f };

	private Color borderColor = UIManager.getColor("CyComponent.borderColor");
	private Color color1 = UIManager.getColor("Label.foreground");
	private Color color2 = UIManager.getColor("Table.background");
	
	private Stroke defStroke = new BasicStroke(1);
	private Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 1 }, 0);
	
	private int size;
    private float fieldWidth;
    private float fieldHeight;
    private float fieldX;
    private float fieldY;
    private float fieldCenterX;
    private float fieldCenterY;
    private float targetSize;
    private Point2D value;
    private Point2D position = new Point2D.Double();
    
    private JPanel canvas;
    private JLabel xLbl;
    private JTextField xTxt;
    private JLabel yLbl;
    private JTextField yTxt;
    private JButton resetBtn;

	private float[] fractions;
	private Color[] colors;

	private boolean shiftDown;
	
	private final CyServiceRegistrar serviceRegistrar;

    public PointPicker(int size, int targetSize, CyServiceRegistrar serviceRegistrar) {
    	this(size, targetSize, DEFAULT_VALUE, serviceRegistrar);
    }
    
    public PointPicker(int size, int targetSize, Point2D value, CyServiceRegistrar serviceRegistrar) {
    	this.serviceRegistrar = serviceRegistrar;
    	
    	if (value == null)
    		value = DEFAULT_VALUE;
    	
    	final float EXTRA_PADDING = 2.0f;
    	
    	this.value = value;
    	this.size = size;
        this.targetSize = targetSize;
        fieldHeight = fieldWidth = (size - targetSize - 2 * EXTRA_PADDING);
        
		fieldX = targetSize / 2 + EXTRA_PADDING;
		fieldY = targetSize / 2 + EXTRA_PADDING;
		fieldCenterX = fieldX + fieldWidth / 2;
		fieldCenterY = fieldY + fieldHeight / 2;
        
        position = convertToPosition((Point2D) value.clone());
        
        init();
        updateTextFields();
    }
    
    public Point2D getValue() {
        return (Point2D) value.clone();
    }

    public void setValue(Point2D value) {
        if (!this.value.equals(value)) {
            var oldValue = this.value;
            this.value = value;
            
            var p = convertToPosition((Point2D) value.clone());
            moveTarget(p.getX(), p.getY(), false);
            updateTextFields();
            
            firePropertyChange("value", oldValue, value);
        }
    }
    
    /**
     * Optional, in case you want the canvas background to show the radial gradient it's modifying.
     */
    public void update(float[] fractions, Color[] colors) {
    	this.fractions = fractions;
    	this.colors = colors;
    	repaint();
	}
    
    private void init() {
    	xLbl = new JLabel("x:");
    	yLbl = new JLabel("y:");
    	
    	setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
    	
    	var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getCanvas(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(4)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(getResetBtn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
							.addComponent(xLbl)
							.addComponent(getXTxt())
						)
						.addGroup(layout.createSequentialGroup()
							.addComponent(yLbl)
							.addComponent(getYTxt())
						)
				)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getCanvas())
				.addGroup(layout.createSequentialGroup()
						.addComponent(getResetBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(xLbl)
								.addComponent(getXTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(yLbl)
								.addComponent(getYTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
				)
		);
		
		LookAndFeelUtil.makeSmall(xLbl, yLbl, getXTxt(), getYTxt());
		ViewUtils.styleEditorButtons(getResetBtn());
	}

	private JPanel getCanvas() {
    	if (canvas == null) {
    		canvas = new JPanel() {
    			@Override
    		    protected void paintComponent(Graphics g) {
    				paintCanvas(g);
    		    }
    		};
    		
    		canvas.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
    		canvas.setMinimumSize(new Dimension(size, size));
    		canvas.setPreferredSize(new Dimension(size, size));
    		
    		var mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    mouseCheck(e);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    mouseCheck(e);
                    
                    if (SwingUtilities.isLeftMouseButton(e))
                    	setValue(convertToValue(position));
                    
                    shiftDown = false;
                }
				@Override
                public void mouseDragged(MouseEvent e) {
					shiftDown = false;
                    mouseCheck(e);
                }
            };
            
            canvas.addMouseMotionListener(mouseAdapter);
            canvas.addMouseListener(mouseAdapter);
    	}
    	
		return canvas;
	}
    
    private JTextField getXTxt() {
    	if (xTxt == null) {
    		xTxt = new JTextField();
    		xTxt.setInputVerifier(new DoubleInputVerifier());
    		xTxt.setMinimumSize(new Dimension(60, xTxt.getMinimumSize().height));
    		xTxt.setHorizontalAlignment(JTextField.TRAILING);
			
    		xTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					onTextFieldUpdated();
				}
			});
    	}
    	
		return xTxt;
	}
    
    private JTextField getYTxt() {
    	if (yTxt == null) {
    		yTxt = new JTextField();
    		yTxt.setInputVerifier(new DoubleInputVerifier());
    		yTxt.setMinimumSize(new Dimension(60, yTxt.getMinimumSize().height));
    		yTxt.setHorizontalAlignment(JTextField.TRAILING);
			
    		yTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					onTextFieldUpdated();
				}
			});
    	}
    	
		return yTxt;
	}
    
    private JButton getResetBtn() {
    	if (resetBtn == null) {
    		resetBtn = new JButton(IconManager.ICON_REFRESH);
    		resetBtn.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
    		resetBtn.setToolTipText("Reset");
    		resetBtn.addActionListener(evt -> setValue(DEFAULT_VALUE));
    		
    		if (LookAndFeelUtil.isAquaLAF())
    			resetBtn.putClientProperty("JButton.buttonType", "gradient");
    	}
    	
		return resetBtn;
	}
    
    private void mouseCheck(MouseEvent evt) {
        if (SwingUtilities.isLeftMouseButton(evt)) {
        	shiftDown = evt.isShiftDown();
        	moveTarget(evt.getX(), evt.getY(), shiftDown);
        }
    }

    private void moveTarget(double x, double y, boolean snap) {
		var line = new Line2D.Double(fieldCenterX, fieldCenterY, x, y);
		var ips = MathUtil.getIntersectionPoints(line, new Rectangle2D.Float(fieldX, fieldY, fieldWidth, fieldHeight));

		Point2D ip = null;

		for (Point2D p : ips) {
			if (p != null) {
				ip = p;
				break;
			}
		}
		
        if (ip != null) {
        	x = ip.getX();
        	y = ip.getY();
        }
        
        // Snap to the nearest main point if holding the SHIFT key
     	if (snap) {
     		x = fieldX + MathUtil.findNearestNumber(MAIN_XY, (float) (x / fieldWidth)) * fieldWidth;
     		y = fieldY + MathUtil.findNearestNumber(MAIN_XY, (float) (y / fieldHeight)) * fieldHeight;
     	}
        
        position.setLocation(x, y);
        
        SwingUtilities.getRoot(PointPicker.this).repaint();
        updateTextFields();
    }
    
    private void updateTextFields() {
    	var value = convertToValue(position);
    	getXTxt().setText("" + (Math.round(value.getX() * 100) / 100.0));
    	getYTxt().setText("" + (Math.round(value.getY() * 100) / 100.0));
	}

    private void onTextFieldUpdated() {
		try {
			double x = Double.parseDouble(getXTxt().getText());
			if (x < 0.0) x = 0.0;
			if (x > 1.0) x = 1.0;
			double y = Double.parseDouble(getYTxt().getText());
			if (y < 0.0) y = 0.0;
			if (y > 1.0) y = 1.0;
			
			setValue(new Point2D.Double(x, y));
		} catch (NumberFormatException nfe) {
		}
	}
    
    private Point2D convertToValue(Point2D position) {
    	return new Point2D.Double((position.getX() - fieldX) / fieldWidth, (position.getY() - fieldY) / fieldHeight);
	}
    
    private Point2D convertToPosition(Point2D value) {
    	return new Point2D.Double(value.getX() * fieldWidth + fieldX, value.getY() * fieldHeight + fieldY);
    }
    
	protected void paintCanvas(Graphics g) {
		super.paintComponent(g);
		
		var g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int x = (int) fieldX;
		int y = (int) fieldY;
		int w = (int) fieldWidth;
		int h = (int) fieldHeight;

		if (fractions != null && fractions.length > 0 && colors != null && colors.length > 0) {
			// Use the passed colors and fractions to paint our background with a nice radial gradient,
			// but with an updated center point, of course
			float cx = (float) position.getX();
			float cy = (float) position.getY();
			var newPaint = new RadialGradientPaint(cx, cy, Math.max(w, h), fractions, colors);
			g2.setPaint(newPaint);
			g2.fillRect(x, y, w, h);
		} else {
			g2.setColor(UIManager.getColor("Panel.background"));
			g2.fillRect(x, y, w, h);
		}
		
		if (shiftDown) {
			g2.setStroke(dashedStroke);
			g2.setColor(Color.LIGHT_GRAY);
			
			// Center/Middle lines
			int mx = (int) (x + .5f * w);
			int my = (int) (y + .5f * h);
			g2.drawLine(mx, y, mx, y + h); // vertical
			g2.drawLine(x, my, x + w, my); // horizontal
		}
		
		g2.setStroke(defStroke);
		g2.setColor(borderColor);
		g2.drawRect(x, y, w, h);

		drawTarget(g2, 3.2f, color1);
		drawTarget(g2, 1.0f, color2);
		
		g2.dispose();
	}
    
    protected void drawTarget(Graphics2D g2, float strokeWidth, Color strokeColor) {
        double cx = position.getX();
        double cy = position.getY();
        float d = targetSize;
        
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(strokeColor);
        // vertical line
        g2.drawLine((int)cx, (int)(cy - d/2), (int)cx, (int)(cy + d/2));
        // horizontal line
        g2.drawLine((int)(cx - d/2), (int)cy, (int)(cx + d/2), (int)cy);
    }
}
