package org.cytoscape.ding.internal.util;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;

import org.cytoscape.ding.internal.charts.AbstractChartEditor.DoubleInputVerifier;

/**
 * Editor that allows users to select a coordinate on a square with the mouse.
 */
public class PointPicker extends JPanel {
    
	private static final long serialVersionUID = 1152583166571339876L;

	private static final float EXTRA_PADDING = 4.0f;
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

    public PointPicker(final int size, final int targetSize) {
    	this(size, targetSize, DEFAULT_VALUE);
    }
    
    public PointPicker(final int size, final int targetSize, Point2D value) {
    	if (value == null)
    		value = DEFAULT_VALUE;
    	
    	this.value = value;
    	this.size = size;
        this.targetSize = targetSize;
        fieldHeight = fieldWidth = size - targetSize - EXTRA_PADDING;
        
        fieldX = 0 + targetSize/2 + EXTRA_PADDING/2;
        fieldY = 0 + targetSize/2 + EXTRA_PADDING/2;
        fieldCenterX = fieldX + fieldWidth / 2;
        fieldCenterY = fieldY + fieldHeight / 2;
        
        position = convertToPosition(new Point2D.Double(value.getX(), value.getY()));
        
        init();
        updateTextFields();
    }
    
    public Point2D getValue() {
        return (Point2D) value.clone();
    }

    public void setValue(final Point2D value) {
        if (!this.value.equals(value)) {
            final Point2D oldValue = this.value;
            this.value = value;
            
            final Point2D p = convertToPosition(new Point2D.Double(value.getX(), value.getY()));
            moveTarget(p.getX(), p.getY());
            updateTextFields();
            
            firePropertyChange("value", oldValue, value);
        }
    }
    
    private void init() {
    	xLbl = new JLabel("x");
    	yLbl = new JLabel("y");
    	
    	setOpaque(false);
    	
    	final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getCanvas(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
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
						.addGap((int)fieldY)
						.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
								.addComponent(xLbl)
								.addComponent(getXTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								          GroupLayout.PREFERRED_SIZE)
						)
						.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
								.addComponent(yLbl)
								.addComponent(getYTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								          GroupLayout.PREFERRED_SIZE)
						)
				)
		);
    	
        add(getCanvas());
	}

    @SuppressWarnings("serial")
	private JPanel getCanvas() {
    	if (canvas == null) {
    		canvas = new JPanel() {
    			@Override
    		    protected void paintComponent(final Graphics g) {
    				paintCanvas(g);
    		    }
    		};
    		canvas.setOpaque(false);
    		
    		// TODO
    		canvas.setMinimumSize(new Dimension((int) size, (int) size));
    		canvas.setPreferredSize(new Dimension((int) size, (int) size));
    		
    		final MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent e) {
                    mouseCheck(e);
                }
                @Override
                public void mouseReleased(final MouseEvent e) {
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
				public void focusLost(final FocusEvent e) {
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
				public void focusLost(final FocusEvent e) {
					onTextFieldUpdated();
				}
			});
    	}
    	
		return yTxt;
	}
    
    private void mouseCheck(final MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e))
        	moveTarget(e.getX(), e.getY());
    }

    private void moveTarget(final double x, final double y) {
        final Line2D line = new Line2D.Double(fieldCenterX, fieldCenterY, x, y);
        final Point2D[] ips =
        		MathUtil.getIntersectionPoints(line, new Rectangle2D.Float(fieldX, fieldY, fieldWidth, fieldHeight));
        
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
    	final Point2D value = convertToValue(position);
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
    
    private Point2D convertToValue(final Point2D position) {
    	double f = fieldX + fieldWidth;
    	return new Point2D.Double(position.getX() / f, position.getY() / f);
	}
    
    private Point2D convertToPosition(final Point2D value) {
    	double f = fieldX + fieldWidth;
    	return new Point2D.Double(value.getX() * f, value.getY() * f);
    }
    
	protected void paintCanvas(final Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int x = (int) fieldX;
		final int y = (int) fieldY;
		final int w = (int) fieldWidth;
		final int h = (int) fieldHeight;

		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(x, y, w, h);
		g2.setColor(Color.DARK_GRAY);
		g2.drawRect(x, y, w, h);

		drawTarget(g2, 3.2f, Color.DARK_GRAY);
		drawTarget(g2, 1.0f, Color.WHITE);
	}
    
    protected void drawTarget(final Graphics2D g2, final float strokeWidth, final Color strokeColor) {
        final double cx = position.getX(); // value x
        final double cy = position.getY(); // value y
        final float d1 = targetSize;
        final float d2 = targetSize * 0.8f;
        final float cg = targetSize * 0.2f; // value gap
        
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(strokeColor);
        // circle
        g2.drawOval((int)Math.round(cx - d2/2), (int)Math.round(cy - d2/2), (int)d2, (int)d2);
        // vertical lines
        g2.drawLine((int)cx, (int)(cy - d1/2), (int)cx, (int)(cy - cg));
        g2.drawLine((int)cx, (int)(cy + cg), (int)cx, (int)(cy + d1/2));
        // horizontal lines
        g2.drawLine((int)(cx - d1/2), (int)cy, (int)(cx - cg), (int)cy);
        g2.drawLine((int)(cx + cg), (int)cy, (int)(cx + d1/2), (int)cy);
    }
    
    public static void main(String[] args) {
		final PointPicker pp = new PointPicker(100, 16, new Point2D.Double(0.5, 0.5));
		final JLabel lb = new JLabel("Value: ");
		
		pp.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				Point2D p = (Point2D)e.getNewValue();
				lb.setText("Value: " + p.getX() + ", " + p.getY());
			}
		});
		
		JDialog d = new JDialog();
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		d.getContentPane().add(pp, BorderLayout.CENTER);
		d.getContentPane().add(lb, BorderLayout.SOUTH);
		d.pack();
		d.setVisible(true);
	}
}

