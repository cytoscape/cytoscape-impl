package org.cytoscape.cg.internal.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.cg.internal.charts.AbstractChartEditor.DoubleInputVerifier;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * Editor that allows users to select a coordinate on a square with the mouse.
 */
@SuppressWarnings("serial")
public class PointPicker extends JPanel {
    
	private static final Point2D DEFAULT_VALUE = new Point2D.Double(0.5, 0.5);
    
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

    public PointPicker(int size, int targetSize) {
    	this(size, targetSize, DEFAULT_VALUE);
    }
    
    public PointPicker(int size, int targetSize, Point2D value) {
    	if (value == null)
    		value = DEFAULT_VALUE;
    	
    	final float EXTRA_PADDING = 2.0f;
    	
    	this.value = value;
    	this.size = size;
        this.targetSize = targetSize;
        fieldHeight = fieldWidth = (size - targetSize - 2 * EXTRA_PADDING);
        
        fieldX = targetSize/2 + EXTRA_PADDING;
        fieldY = targetSize/2 + EXTRA_PADDING;
        fieldCenterX = fieldX + fieldWidth / 2;
        fieldCenterY = fieldY + fieldHeight / 2;
        
        position = convertToPosition(new Point2D.Double(value.getX(), value.getY()));
        
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
            
            var p = convertToPosition(new Point2D.Double(value.getX(), value.getY()));
            moveTarget(p.getX(), p.getY());
            updateTextFields();
            
            firePropertyChange("value", oldValue, value);
        }
    }
    
    private void init() {
    	xLbl = new JLabel("x:");
    	yLbl = new JLabel("y:");
    	
    	setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
    	
    	var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getCanvas(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(4)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
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
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getCanvas())
				.addGroup(layout.createSequentialGroup()
						.addGap((int) fieldY)
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
    	
        add(getCanvas());
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
    		canvas.setMinimumSize(new Dimension((int) size, (int) size));
    		canvas.setPreferredSize(new Dimension((int) size, (int) size));
    		
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
                }
				@Override
                public void mouseDragged(MouseEvent e) {
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
    
    private void mouseCheck(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e))
        	moveTarget(e.getX(), e.getY());
    }

    private void moveTarget(double x, double y) {
		var line = new Line2D.Double(fieldCenterX, fieldCenterY, x, y);
		var ips = MathUtil.getIntersectionPoints(line, new Rectangle2D.Float(fieldX, fieldY, fieldWidth, fieldHeight));

		Point2D ip = null;

		for (Point2D p : ips) {
			if (p != null) {
				ip = p;
				break;
			}
		}

        if (ip != null)
        	position.setLocation(ip.getX(), ip.getY());
        else
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

		g2.setColor(UIManager.getColor("Panel.background"));
		g2.fillRect(x, y, w, h);
		g2.setColor(UIManager.getColor("Label.disabledForeground"));
		g2.drawRect(x, y, w, h);

		drawTarget(g2, 3.2f, UIManager.getColor("Label.foreground"));
		drawTarget(g2, 1.0f, UIManager.getColor("Table.background"));
		
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
    
//    public static void main(String[] args) {
//		PointPicker pp = new PointPicker(100, 12, new Point2D.Double(0.5, 0.5));
//		
//		javax.swing.JDialog d = new javax.swing.JDialog();
//		d.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
//		d.getContentPane().add(pp, java.awt.BorderLayout.CENTER);
//		d.pack();
//		d.setVisible(true);
//	}
}

