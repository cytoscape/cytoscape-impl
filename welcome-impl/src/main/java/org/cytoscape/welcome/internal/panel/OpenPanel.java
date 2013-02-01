package org.cytoscape.welcome.internal.panel;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.welcome.internal.WelcomeScreenDialog;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OpenPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = 591882944100485039L;

	private static final Logger logger = LoggerFactory.getLogger(OpenPanel.class);

	private static final String ICON_LOCATION = "/images/Icons/open_session.png";
	private BufferedImage openIconImg;
	private ImageIcon openIcon;

	// Display up to 7 files due to space.
	private static final int MAX_FILES = 7;

	private JLabel open;

	private final RecentlyOpenedTracker fileTracker;
	private final DialogTaskManager taskManager;
	private final OpenSessionTaskFactory openSessionTaskFactory;

	public OpenPanel(final RecentlyOpenedTracker fileTracker, final DialogTaskManager taskManager,
			final OpenSessionTaskFactory openSessionTaskFactory) {
		this.fileTracker = fileTracker;
		this.taskManager = taskManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		initComponents();
	}

	private void initComponents() {
		try {
			openIconImg = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(ICON_LOCATION));
		} catch (IOException e) {
			e.printStackTrace();
		}

		openIcon = new ImageIcon(openIconImg);

		final List<URL> recentFiles = fileTracker.getRecentlyOpenedURLs();
		int fileCount = recentFiles.size();
		
		if (fileCount > MAX_FILES)
			fileCount = MAX_FILES;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final Border padLine = BorderFactory.createEmptyBorder(3, 5, 3, 0);

		for (int i = 0; i < fileCount; i++) {
			final URL target = recentFiles.get(i);

			URI fileURI = null;
			try {
				fileURI = target.toURI();
			} catch (URISyntaxException e2) {
				logger.error("Invalid file URL.", e2);
				continue;
			}
			
			final File targetFile = new File(fileURI);
			final JLabel fileLabel = new JLabel();
			FontMetrics fm = fileLabel.getFontMetrics(REGULAR_FONT);
			fileLabel.setMaximumSize(new Dimension(300, 18));
			fileLabel.setText(getTruncatedPath( target.toString(),250, fm ) );
			fileLabel.setForeground(REGULAR_FONT_COLOR);
			fileLabel.setFont(LINK_FONT);
			fileLabel.setBorder(padLine);
			fileLabel.setHorizontalAlignment(SwingConstants.LEFT);
			fileLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			fileLabel.setToolTipText(fileURI.toString());
			fileLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (targetFile.exists()) {
						taskManager.execute(openSessionTaskFactory.createTaskIterator(targetFile));
						closeParentWindow();
					} else {
						JOptionPane.showMessageDialog(OpenPanel.this.getTopLevelAncestor(),
								"Session file not found:\n" + targetFile.getAbsolutePath(),
								"File not Found",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			});

			this.add(fileLabel);
		}
		
		open = new JLabel("Open file...");
		open.setFont(REGULAR_FONT);
		open.setForeground(REGULAR_FONT_COLOR);
		open.setIcon(openIcon);
		open.setBorder(padLine);
		open.setHorizontalAlignment(SwingConstants.LEFT);
		open.setCursor(new Cursor(Cursor.HAND_CURSOR));
		open.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				closeParentWindow();
				taskManager.execute(openSessionTaskFactory.createTaskIterator());
			}
		});

		this.add(open);
	}

	private String getTruncatedPath(String path, double width, FontMetrics fm ) {

		String fileName = path.substring(path.lastIndexOf('/'));
		if (fm.stringWidth("..."+fileName) > width){
			int startIndex = 4;
			fileName = "..." + fileName.substring(startIndex);
			while (fm.stringWidth(fileName) > width){
				fileName = "..." + fileName.substring(startIndex);
			}
			return fileName;
		}
		
		String address = path.substring(0, path.lastIndexOf('/'));
		while (fm.stringWidth(address + fileName) > width){
			address = address.substring(0, address.length() - 4) + "...";
		}
		
		return address + fileName;
	}
}
