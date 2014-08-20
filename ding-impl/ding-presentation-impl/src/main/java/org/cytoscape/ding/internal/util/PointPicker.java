package org.cytoscape.ding.internal.util;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

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
        
        setMinimumSize(new Dimension((int) size, (int) size));
        setPreferredSize(new Dimension((int) size, (int) size));
        
        fieldX = 0 + targetSize/2 + EXTRA_PADDING/2;
        fieldY = 0 + targetSize/2 + EXTRA_PADDING/2;
        fieldCenterX = fieldX + fieldWidth / 2;
        fieldCenterY = fieldY + fieldHeight / 2;
        
        position = new Point2D.Double(value.getX() * size, value.getY() * size);
        
        final MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                mouseCheck(e);
            }
            @Override
            public void mouseReleased(final MouseEvent e) {
                mouseCheck(e);
                
                if (SwingUtilities.isLeftMouseButton(e))
                	setValue(new Point2D.Double(position.getX() / size, position.getY() / size));
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseCheck(e);
            }
        };
        
        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);
        
        setOpaque(false);
    }

    public Point2D getValue() {
        return (Point2D) value.clone();
    }

    public void setValue(final Point2D value) {
        if (!this.value.equals(value)) {
            final Point2D oldValue = this.value;
            this.value = value;
            moveTarget(value.getX() * size, value.getY() * size);
            firePropertyChange("value", oldValue, value);
        }
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
    }
    
    @Override
    protected void paintComponent(final Graphics g) {
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

