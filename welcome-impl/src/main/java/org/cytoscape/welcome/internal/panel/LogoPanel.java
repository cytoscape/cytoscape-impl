package org.cytoscape.welcome.internal.panel;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.cytoscape.welcome.internal.WelcomeScreenDialog;

public class LogoPanel extends AbstractWelcomeScreenChildPanel {
	
	private static final long serialVersionUID = -1450934154838736314L;
	private static final String IMAGE_LOCATION = "images/logo.png";
	private BufferedImage bgImage;
	
	public LogoPanel() {
		initComponents();
	}
	
	void initComponents() {
		try {
			bgImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(IMAGE_LOCATION));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.setLayout(new BorderLayout());
		final JLabel logo = new JLabel();
		logo.setIcon(new ImageIcon(bgImage));
		logo.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.add(logo, BorderLayout.CENTER);
	}

}
