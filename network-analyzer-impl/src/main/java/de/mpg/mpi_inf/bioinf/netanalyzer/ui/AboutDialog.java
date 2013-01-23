package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.mpg.mpi_inf.bioinf.netanalyzer.OpenBrowser;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Utils;

/**
 * NetworkAnalyzer About box.
 * <p>
 * This dialog displays an image for its contents.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class AboutDialog extends JDialog
	implements MouseMotionListener, MouseListener {

	/**
	 * URL of the official NetworkAnalyzer web site at the time this plugin is published.
	 */
	public static final String WEB_SITE = "http://med.bioinf.mpi-inf.mpg.de/netanalyzer/";

	/**
	 * Initializes a new instance of <code></code>.
	 * 
	 * @param aOwner The <code>Frame</code> from which this dialog is displayed.
	 */
	public AboutDialog(Frame aOwner) {
		super(aOwner, Messages.DT_ABOUT, true);

		initControls();
		setResizable(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (getCursor() == onURLCursor) {
			// User clicked on the URL
			OpenBrowser.openURL(WEB_SITE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		if (URL.contains(e.getX(), e.getY())) {
			setCursor(onURLCursor);
		} else {
			setCursor(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		// Event is not processed
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 7976825262362844385L;

	/**
	 * Mouse cursor when over an URL address.
	 */
	private static final Cursor onURLCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

	/**
	 * Area on the about box that is treated as a URL.
	 */
	private static final Rectangle URL = new Rectangle(196, 252, 412, 21);

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	private void initControls() {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(Color.WHITE);
		JLabel labContents = new JLabel(Utils.getImage("AboutBox.png", ""));
		contentPane.add(labContents, BorderLayout.CENTER);
		labContents.addMouseListener(this);
		labContents.addMouseMotionListener(this);
		setContentPane(contentPane);
		pack();
	}
}
