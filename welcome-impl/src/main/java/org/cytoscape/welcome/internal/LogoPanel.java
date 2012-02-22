package org.cytoscape.welcome.internal;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LogoPanel extends JPanel {
	
	private static final String IMAGE_LOCATION = "images/logo.png";
	private BufferedImage bgImage;
	
	LogoPanel() {
		
		
		initComponents();
	}
	
	void initComponents() {
		try {
			bgImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(IMAGE_LOCATION));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.setLayout(new BorderLayout());
		JLabel logo = new JLabel();
		logo.setIcon(new ImageIcon(bgImage));
		logo.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.add(logo, BorderLayout.CENTER);
	}

}
