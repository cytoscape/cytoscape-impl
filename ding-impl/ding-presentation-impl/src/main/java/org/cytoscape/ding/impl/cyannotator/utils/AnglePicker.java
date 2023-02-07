package org.cytoscape.ding.impl.cyannotator.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.ding.internal.util.MathUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class AnglePicker extends JPanel {
  
	private static final int POINT_RADIUS = 4;
	private static final int POINT_BORDER = 2;
	
	private static final int[] MAIN_ANGLES = { 0, 45, 90, 135, 180, 225, 270, 315 };
	/** We need the 360 value for the findClosestNumber function, or it will be hard to snap to 0 degrees. */
	private static final  int[] ALL_ANGLES = { 0, 45, 90, 135, 180, 225, 270, 315, 360 };
	
	private Color borderColor = UIManager.getColor("Separator.foreground");
	private Color color1 = UIManager.getColor("CyComponent.borderColor");
	private Color color2 = UIManager.getColor("Table.background");
	private Color selColor = UIManager.getColor("Focus.color");
	
	private Stroke defStroke = new BasicStroke(1);
	private Stroke thickStroke = new BasicStroke(2);
	private Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 1 }, 0);
	
	private int angle = -1;
	
	private float[] fractions = { 0.0f, 1.0f };
	private Color[] colors = { Color.BLACK, Color.WHITE };
	
	private boolean mouseDragging;
	
	public AnglePicker() {
		setOpaque(!LookAndFeelUtil.isAquaLAF());
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				selectAngle(evt);
			}
			@Override
			public void mouseReleased(MouseEvent evt) {
				mouseDragging = false;
				repaint();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent evt) {
				mouseDragging = true;
				selectAngle(evt);
			}
		});
	}
	
	private void selectAngle(MouseEvent evt) {
		double oldValue = angle;
		
		// Make sure we get the angle in counterclockwise orientation
		int x = evt.getX() - getWidth() / 2;
		int y = -evt.getY() + getHeight() / 2;
		angle = (int) Math.toDegrees(Math.atan2(y, x));
		
		if (angle < 0)
			angle = 360 + angle;
		
		// Snap to the nearest main angle if holding the SHIFT key
		if (evt.isShiftDown())
			angle = MathUtil.findNearestNumber(ALL_ANGLES, Math.round(angle));
		
		if (angle == 360)
			angle = 0;
		
		repaint();
		firePropertyChange("value", oldValue, angle);
	}
	
	/**
     * Optional, in case you want the canvas background to show the linear gradient it's modifying.
     */
    public void update(float[] fractions, Color[] colors, int angle) {
    	this.fractions = fractions;
    	this.colors = colors;
    	this.angle = (int) Math.round(MathUtil.normalizeAngle(angle));
    	repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		var g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		var insets = getInsets();
		
		int w = getWidth();
		int h = getHeight();
		int pr = POINT_RADIUS;
		int pb = POINT_BORDER;
		int pad = Math.max(insets.top + insets.bottom, insets.left + insets.right) / 2;
		int r = Math.min(w, h) / 2 - pr - pb / 2 - pad;
		
		g2.translate(w / 2, h / 2);
		
		if (fractions != null && fractions.length > 0 && colors != null && colors.length > 0) {
			// Use the passed colors and fractions to paint our background with a linear gradient,
			// but with an updated center point, of course
			var bounds = new Rectangle(-r, -r, 2 * r - 4 + pr, 2 * r - 4 + pr);
			var line = MathUtil.getGradientAxis(bounds, angle);
			var paint = new LinearGradientPaint(line.getP1(), line.getP2(), fractions, colors);
			g2.setPaint(paint);
			g2.fill(bounds);
			
			g2.setColor(borderColor);
			g2.draw(bounds);
		}
		
		g2.setStroke(dashedStroke);
		g2.setColor(Color.LIGHT_GRAY);
		g2.drawLine(-r, 0, r, 0);
		g2.drawLine(0, -r, 0, r);
		
		g2.setStroke(defStroke);
		g2.setColor(color1);
		g2.drawOval(-r, -r, r * 2, r * 2); // external line
		g2.setColor(color2);
		g2.drawOval(-r + 1, -r + 1, (r - 1) * 2, (r - 1) * 2); // internal line
		
		for (int angle : MAIN_ANGLES) {
			int x = (int) (r * Math.cos(Math.toRadians(angle)));
			int y = (int) (r * Math.sin(Math.toRadians(angle)));
			
			g2.setStroke(thickStroke);
			g2.setColor(color1);
			g2.drawOval(x - pr, y - pr, 2 * pr, 2 * pr);
			
			g2.setColor(color2);
			g2.fillOval(x - pr, y - pr, 2 * pr, 2 * pr);
		}
		
		if (angle >= 0) {
			int x = (int) (r * Math.cos(Math.toRadians(angle))); 
			int y = (int) (r * Math.sin(Math.toRadians(angle)));
			
			if (mouseDragging) {
				g2.setStroke(defStroke);
				g2.setColor(selColor);
				g2.drawLine(x, -y, 0, 0);
			}
			
			g2.setStroke(thickStroke);
			g2.setColor(color2);
			g2.drawOval(x - pr, -y - pr, 2 * pr, 2 * pr);
			
			g2.setStroke(defStroke);
			g2.setColor(selColor);
			g2.fillOval(x - pr, -y - pr, 2 * pr, 2 * pr);
		}
		
		g2.dispose();
	}
	
//	public static void main(String[] args) {
//		var dialog = new JDialog();
//		dialog.setTitle("Angle Picker");
//		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//		dialog.setModal(true);
//		
//		var picker = new AnglePicker();
//		picker.setPreferredSize(new Dimension(200, 200));
//		dialog.add(picker);
//		
//		dialog.pack();
//		dialog.setLocationRelativeTo(null);
//		dialog.setVisible(true);
//	}
}
