package org.cytoscape.welcome.internal;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.session.CySession;
import org.cytoscape.task.creation.LoadSession;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

public class OpenPanel extends JPanel {

	private static final Font FILE_NAME_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
	private static final String ICON_LOCATION = "/images/Icons/open_session.png";
	private BufferedImage openIconImg;
	private ImageIcon openIcon;

	private static final int MAX_FILES = 5;

	private JLabel open;

	private final RecentlyOpenedTracker fileTracker;
	private final TaskManager taskManager;
	private final LoadSession openSessionTaskFactory;

	Window parent;

	OpenPanel(Window parent, final RecentlyOpenedTracker fileTracker, final TaskManager taskManager,
			final LoadSession openSessionTaskFactory) {
		this.fileTracker = fileTracker;
		this.parent = parent;
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
		final int fileCount = recentFiles.size();

		this.setLayout(new GridLayout(MAX_FILES + 1, 1));

		final LineBorder padLine = new LineBorder(new Color(0, 0, 0, 0), 20, false);

		for (int i = 0; i < fileCount; i++) {
			final URL target = recentFiles.get(i);
			final JLabel fileLabel = new JLabel(recentFiles.get(i).toString());

			fileLabel.setFont(FILE_NAME_FONT);
			fileLabel.setBorder(padLine);
			fileLabel.setHorizontalAlignment(SwingConstants.LEFT);
			fileLabel.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					 try {
						final File targetFile = new File(target.toURI());
						taskManager.execute(openSessionTaskFactory.loadSession(targetFile));
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
					parent.dispose();
				}
			});

			this.add(fileLabel);
		}
		open = new JLabel("Open other...");
		open.setIcon(openIcon);
		open.setBorder(padLine);
		open.setHorizontalAlignment(SwingConstants.LEFT);
		open.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				parent.dispose();
				taskManager.execute(openSessionTaskFactory.loadSession());
			}
		});

		this.add(open);
	}
}
