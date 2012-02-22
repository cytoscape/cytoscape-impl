package org.cytoscape.welcome.internal;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class BackgroundImagePanel extends JPanel {
	
	private static final long serialVersionUID = 3969531543044198032L;
	
	private final BufferedImage image;
	
	public BackgroundImagePanel(final BufferedImage image) {
		this.image = image;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;

		int panelWidth = this.getWidth();
		int panelHeight = this.getHeight();
		
		g2D.setColor(getBackground());
		g2D.fillRect(0, 0, panelWidth, panelHeight);

		g2D.drawImage(image, null, 0, 0);
	}

}
