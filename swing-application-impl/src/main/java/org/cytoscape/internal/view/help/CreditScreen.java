package org.cytoscape.internal.view.help;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.CySwingApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CreditScreen {

	private Timer timer;
	private ImageIcon image;
	private List<String> lines;
	private JDialog dialog; 
	private static final Logger logger = LoggerFactory.getLogger(CreditScreen.class);
	private static final String CREDIT_IMAGE = "/images/CytoscapeCredits.png";
	private static final String CREDITS = "/credits.txt";
	private final String version;
	private final JFrame parent;


	public CreditScreen(CyVersion vers, CySwingApplication cySwingApp) {
		version = vers.getVersion();
		parent = cySwingApp.getJFrame();
		try {
			image = new ImageIcon(getClass().getResource(CREDIT_IMAGE)); 
			BufferedReader br = new BufferedReader(
				new InputStreamReader(getClass().getResource(CREDITS).openStream()));
			lines = new ArrayList<String>();
			while ( br.ready() )
				lines.add( br.readLine() );
		} catch (IOException ioe) {
			logger.warn("Could not configure the credit screen.", ioe);
		}
	}

	public void showCredits() {
		dialog = new JDialog(parent,true);
		dialog.setUndecorated(true);
		final ScrollingLinesPanel panel = new ScrollingLinesPanel(image, lines);
		dialog.add(panel);
		dialog.pack();
		dialog.validate();
		dialog.setPreferredSize(panel.getPreferredSize());
		centerDialogLocation(dialog);

		Action scrollText = new AbstractAction() {
			private final static long serialVersionUID = 1202340446391603L;

			public void actionPerformed(ActionEvent e) {
				panel.incrementYPos();
				dialog.repaint();
			}
		};

		timer = new Timer(100, scrollText);

		dialog.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					hideCredits();
				}
				public void mouseEntered(MouseEvent e) { }
				public void mouseExited(MouseEvent e) { }
				public void mousePressed(MouseEvent e) { }
				public void mouseReleased(MouseEvent e) { }
			});

		timer.start();
		dialog.setVisible(true);
	}

	public void hideCredits() {
		hideScreen();
		if ( timer != null )
			timer.stop();
	}

	private class ScrollingLinesPanel extends JPanel {
		private final static long serialVersionUID = 1202339874718767L;
		int yPos;
		int xPos;
		ImageIcon background;
		List<String> lines;

		public ScrollingLinesPanel(ImageIcon background, List<String> lines) {
			super();
			this.background = background;
			this.lines = lines;
			yPos = background.getIconHeight();
			xPos = (int) ((float) background.getIconWidth() / 2.0f);
			setOpaque(false);
			setPreferredSize(new Dimension(background.getIconWidth(), background.getIconHeight()));
		}

		protected void paintComponent(Graphics g) {
			g.drawImage(background.getImage(), 0, 0, null);
			((Graphics2D) g).setPaint(Color.black);

			g.drawString(version,xPos,35);

			int i = 1;
			int y = yPos;

			for ( String sub : lines ) {
				y = yPos + (12 * i);

				if (y > 120)
					g.drawString(sub, xPos, y);

				i++;
			}

			super.paintComponent(g);
		}

		public void incrementYPos() {
			yPos -= 2;
		}
	}


	/**
	 *  DOCUMENT ME!
	 *
	 * @param dialog DOCUMENT ME!
	 */
	protected void centerDialogOnScreen(JDialog dialog) {
		centerDialogSize(dialog);
		centerDialogLocation(dialog);
		dialog.setVisible(true);
	} 

	/**
	 *  DOCUMENT ME!
	 *
	 * @param dialog DOCUMENT ME!
	 */
	protected void centerDialogSize(JDialog dialog) {
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment()
		                                                         .getDefaultScreenDevice()
		                                                         .getDefaultConfiguration();
		Insets screen_insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);

		screen_size.width -= screen_insets.left;
		screen_size.width -= screen_insets.right;
		screen_size.height -= screen_insets.top;
		screen_size.height -= screen_insets.bottom;

		Dimension frame_size = dialog.getSize();
		frame_size.width = (int) (screen_size.width * .75);
		frame_size.height = (int) (screen_size.height * .75);
		dialog.setSize(frame_size);
	} 

	/**
	 *  DOCUMENT ME!
	 *
	 * @param dialog DOCUMENT ME!
	 */
	protected void centerDialogLocation(JDialog dialog) {
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment()
		                                                         .getDefaultScreenDevice()
		                                                         .getDefaultConfiguration();
		Insets screen_insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);

		screen_size.width -= screen_insets.left;
		screen_size.width -= screen_insets.right;
		screen_size.height -= screen_insets.top;
		screen_size.height -= screen_insets.bottom;

		Dimension frame_size = dialog.getSize();
		dialog.setLocation(((screen_size.width / 2) - (frame_size.width / 2)) + screen_insets.left,
		                   ((screen_size.height / 2) - (frame_size.height / 2)) + screen_insets.top);
	}

    public void hideScreen() {
        if ((dialog != null) && dialog.isVisible())
            dialog.dispose();
    }
}
