package org.cytoscape.welcome.internal.panel;

import java.awt.Cursor;
import java.awt.Dimension;
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
		if(fileCount>MAX_FILES)
			fileCount = MAX_FILES;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final Border padLine = BorderFactory.createEmptyBorder(3, 20, 3, 0);

		for (int i = 0; i<fileCount; i++) {
			final URL target = recentFiles.get(i);

			URI fileURI = null;
			try {
				fileURI = target.toURI();
			} catch (URISyntaxException e2) {
				logger.error("Invalid file URL.", e2);
				continue;
			}
			final File targetFile = new File(fileURI);
			final JLabel fileLabel = new JLabel("<html><u>" + target.toString() + "</u></html>");
			fileLabel.setMaximumSize(new Dimension(300, 18));
			fileLabel.setForeground(REGULAR_FONT_COLOR);
			fileLabel.setFont(LINK_FONT);
			fileLabel.setBorder(padLine);
			fileLabel.setHorizontalAlignment(SwingConstants.LEFT);
			fileLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			fileLabel.setToolTipText(fileURI.toString());
			fileLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					taskManager.execute(openSessionTaskFactory.createTaskIterator(targetFile));
					closeParentWindow();
				}
			});

			this.add(fileLabel);
		}
		
		open = new JLabel("Open other file...");
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
}
