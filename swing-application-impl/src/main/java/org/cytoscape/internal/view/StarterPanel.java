package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.IconManager.ICON_REMOVE;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class StarterPanel extends JPanel {
	
	public static String NAME = "__STARTER_PANEL__";
	
	public static final String TUTORIAL_URL = "http://opentutorials.cgl.ucsf.edu/index.php/Portal:Cytoscape3";
	public static final String NEWS_URL = "http://cytoscape-publications.tumblr.com/";
	
	public static final Color LINK_FONT_COLOR = UIManager.getColor("Table.focusCellBackground");
	
	private static final String GAL_FILTERED_EXAMPLE_BUTTON_LABEL = "Sample Yeast Network";
	private static final String SAMPLE_DATA_DIR = "sampleData";
	private static final String GAL_FILTERED_CYS = "galFiltered.cys";
	
	private static final int MAX_FILES = 6;
	
	private static final String MISSING_IMAGE = "/images/logo-transp-gray.png";
	
	private static final Logger logger = LoggerFactory.getLogger(StarterPanel.class);
	
	private JPanel contentPane;
	private JPanel recentSessionsPanel;
	private JPanel sampleSessionsPanel;
	private JPanel linksPanel;
	
	private JButton closeButton;
	
	private JLabel tutorialsLabel = createLinkLabel("<html><u>Tutorials</u></html>", TUTORIAL_URL);
	private JLabel newsLabel = createLinkLabel("<html><u>News</u></html>", NEWS_URL);
	
	private final Icon missingImageIcon;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public StarterPanel(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		missingImageIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(MISSING_IMAGE)));
		
		setName(NAME);
		
		init();
		update();
	}

	public void update() {
		updateRecentSessionsList();
		updateSampleSessionsList();
	}
	
	public void updateRecentSessionsList() {
		updateSessionsList(getRecentSessionsPanel(), getRecentFiles());
	}
	
	public void updateSampleSessionsList() {
		updateSessionsList(getSampleSessionsPanel(), getSampleFiles());
	}

	private void updateSessionsList(JPanel panel, List<FileInfo> files) {
		panel.removeAll();
		panel.setVisible(!files.isEmpty());
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		final SequentialGroup hGroup = layout.createSequentialGroup();
		final ParallelGroup vGroup = layout.createParallelGroup(CENTER, true);
		
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(vGroup)
		);
		
		hGroup.addGap(0, 0, Short.MAX_VALUE);
		
		for (final FileInfo fi : files) {
			SessionPanel sessionPanel = new SessionPanel(fi);

			hGroup.addComponent(sessionPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			vGroup.addComponent(sessionPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		}
		
		hGroup.addGap(0, 0, Short.MAX_VALUE);
	}
	
	private void init() {
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, UIManager.getColor("Separator.foreground")));
		setBackground(UIManager.getColor("Label.disabledForeground"));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(TRAILING, true)
				.addComponent(getContentPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(getContentPane(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
	}

	public JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")));
			
			final GroupLayout layout = new GroupLayout(contentPane);
			contentPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(TRAILING, true)
					.addComponent(getCloseButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(TRAILING, true)
									.addComponent(getRecentSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(getSampleSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(getLinksPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getCloseButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getRecentSessionsPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getSampleSessionsPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getLinksPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
		}
		
		return contentPane;
	}
	
	private JPanel getRecentSessionsPanel() {
		if (recentSessionsPanel == null) {
			recentSessionsPanel = new JPanel();
			recentSessionsPanel.setBorder(LookAndFeelUtil.createTitledBorder("Recent Sessions"));
			
			if (LookAndFeelUtil.isAquaLAF())
				recentSessionsPanel.setOpaque(false);
		}
		
		return recentSessionsPanel;
	}
	
	private JPanel getSampleSessionsPanel() {
		if (sampleSessionsPanel == null) {
			sampleSessionsPanel = new JPanel();
			sampleSessionsPanel.setBorder(LookAndFeelUtil.createTitledBorder("Sample Sessions"));
			
			if (LookAndFeelUtil.isAquaLAF())
				sampleSessionsPanel.setOpaque(false);
		}
		
		return sampleSessionsPanel;
	}
	
	private JPanel getLinksPanel() {
		if (linksPanel == null) {
			linksPanel = new JPanel();
		
			LookAndFeelUtil.equalizeSize(tutorialsLabel, newsLabel);
			
			final GroupLayout layout = new GroupLayout(linksPanel);
			linksPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(tutorialsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(100)
					.addComponent(newsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(tutorialsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(newsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			if (LookAndFeelUtil.isAquaLAF())
				linksPanel.setOpaque(false);
		}
		
		return linksPanel;
	}
	
	JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(ICON_REMOVE);
			closeButton.setToolTipText("Hide Starter Panel");
			CytoPanelUtil.styleButton(closeButton);
			closeButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(13));
			closeButton.setSelected(true);
		}
		
		return closeButton;
	}
	
	private JLabel createLinkLabel(final String text, final String url) {
		JLabel label = new JLabel(text);
		label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		label.setForeground(LINK_FONT_COLOR);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setHorizontalTextPosition(SwingConstants.LEFT);
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				serviceRegistrar.getService(OpenBrowser.class).openURL(url);
			}
		});
		
		return label;
	}
	
	/**
	 * Returns a list of the most recently opened session files.
	 */
	private List<FileInfo> getRecentFiles() {
		final List<FileInfo> files = new ArrayList<>();
		
		final RecentlyOpenedTracker fileTracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
		final List<URL> recentFiles = fileTracker.getRecentlyOpenedURLs();
		int fileCount = Math.min(recentFiles.size(), MAX_FILES);
		
		for (int i = 0; i < fileCount; i++) {
			final URL url = recentFiles.get(i);
			URI fileURI = null;
			
			try {
				fileURI = url.toURI();
			} catch (URISyntaxException e) {
				logger.error("Invalid file URL.", e);
				continue;
			}
			
			final File file = new File(fileURI);
			
			if (file.exists() && file.canRead()) {
				FileInfo fi = new FileInfo(file, file.getName(), file.getAbsolutePath());
				files.add(fi);
			} else {
				fileCount = Math.min(recentFiles.size(), fileCount + 1);
			}
		}
		
		return files;
	}
	
	/**
	 * Returns a list of example files.
	 */
	private List<FileInfo> getSampleFiles() {
		// TODO Just get all cys files from samples directory
		final List<FileInfo> files = new ArrayList<>();
		String galFilteredToolTip = "";
		final File exampleDir = getExampleDir();
		
		if (exampleDir != null && exampleDir.exists())
			galFilteredToolTip = "<html>This (<b>" + GAL_FILTERED_CYS + "</b>) and other example files can be found in:<br />"
					+ exampleDir.getAbsolutePath() + "</html>";
		
		final File sampleFile = getSampleFile(GAL_FILTERED_CYS);

		if (sampleFile != null) {
			final FileInfo fi = new FileInfo(sampleFile, GAL_FILTERED_EXAMPLE_BUTTON_LABEL, galFilteredToolTip);
			files.add(fi);
		}
		
		return files;
	}
	
	/**
	 * Get the location for "galFiltered.cys".
	 */
	private final File getSampleFile(final String filename) {
		final CyApplicationConfiguration applicationCfg = serviceRegistrar.getService(CyApplicationConfiguration.class);
		
		if (applicationCfg != null) {
			return new File(applicationCfg.getInstallationDirectoryLocation() + "/" + SAMPLE_DATA_DIR + "/" +  filename);
		} else {
			logger.error("application configuration is null, cannot find the installation directory");
			return null;
		}
	}
	
	// This returns to location of the example files.
	private final File getExampleDir() {
		final CyApplicationConfiguration applicationCfg = serviceRegistrar.getService(CyApplicationConfiguration.class);

		if (applicationCfg != null) {
			return new File(applicationCfg.getInstallationDirectoryLocation() + "/" + SAMPLE_DATA_DIR + "/");
		} else {
			logger.error("application configuration is null, cannot find the installation directory");
			return null;
		}
	}
	
	private void maybeOpenSession(final File file) {
		if (file.exists()) {
			final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
			final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
			
			if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
				openSession(file);
			else
				openSessionWithWarning(file);
		} else {
			JOptionPane.showMessageDialog(
					StarterPanel.this.getTopLevelAncestor(),
					"Session file not found:\n" + file.getAbsolutePath(),
					"File not Found",
					JOptionPane.WARNING_MESSAGE
			);
			
			final RecentlyOpenedTracker fileTracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
			
			try {
				fileTracker.remove(file.toURI().toURL());
			} catch (Exception e) {
				logger.error("Error removing session file from RecentlyOpenedTracker.", e);
			}
		}
	}
	
	private void openSession(final File file) {
		final OpenSessionTaskFactory taskFactory = serviceRegistrar.getService(OpenSessionTaskFactory.class);
		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		taskManager.execute(taskFactory.createTaskIterator(file));
	}
	
	private void openSessionWithWarning(final File file) {
		if (JOptionPane.showConfirmDialog(
				StarterPanel.this.getTopLevelAncestor(),
				"Current session (all networks and tables) will be lost.\nDo you want to continue?",
				"Open Session",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
			openSession(file);
	}
	
	private class SessionPanel extends JPanel {
		
		static final int NAME_WIDTH = 124;
		
		private JLabel thumbnailLabel;
		private JLabel nameLabel;
		
		private final FileInfo fileInfo;

		public SessionPanel(FileInfo fileInfo) {
			this.fileInfo = fileInfo;
			init();
		}
		
		private void init() {
			setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			final GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getThumbnailLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNameLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getThumbnailLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			if (LookAndFeelUtil.isAquaLAF())
				setOpaque(false);
			
			MouseListener mouseListener = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					maybeOpenSession(fileInfo.getFile());
				}
			};
			
			addMouseListener(mouseListener);
			getThumbnailLabel().addMouseListener(mouseListener);
			getNameLabel().addMouseListener(mouseListener);
		}
		
		private JLabel getThumbnailLabel() {
			if (thumbnailLabel == null) {
				thumbnailLabel = new JLabel(fileInfo.getIcon());
				thumbnailLabel.setHorizontalAlignment(SwingConstants.CENTER);
				thumbnailLabel.setHorizontalTextPosition(SwingConstants.CENTER);
				thumbnailLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
				thumbnailLabel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")));
				thumbnailLabel.setToolTipText(fileInfo.getHelp());
				thumbnailLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			
			return thumbnailLabel;
		}
		
		private JLabel getNameLabel() {
			if (nameLabel == null) {
				nameLabel = new JLabel(fileInfo.getName());
				nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
				nameLabel.setToolTipText(fileInfo.getFile().getPath());
				nameLabel.setForeground(LINK_FONT_COLOR);
				nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
				nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
				
				Dimension d = new Dimension(NAME_WIDTH, nameLabel.getPreferredSize().height);
				nameLabel.setMinimumSize(d);
				nameLabel.setPreferredSize(d);
				nameLabel.setSize(d);
			}
			
			return nameLabel;
		}
	}
	
	private final class FileInfo {
		
		private final String SESSION_EXT = ".cys";
		private final String THUMBNAIL_FILE = "/session_thumbnail.png";
		
		final private File file;
		final private String name;
		final private String help;
		private Icon icon;
		
		FileInfo(File file, String name, String help) {
			this.file = file;
			this.help = help;
			
			if (name != null && name.toLowerCase().endsWith(SESSION_EXT))
				name = name.substring(0, name.length() - SESSION_EXT.length());
			
			this.name = name;
		}

		final File getFile() {
			return file;
		}

		final String getName() {
			return name;
		}

		final String getHelp() {
			return help;
		}
		
		Icon getIcon() {
			if (icon == null) {
				Image thumbnail = loadThumbnail();
				
				if (thumbnail != null)
					icon = new ImageIcon(thumbnail);
				else
					icon = missingImageIcon;
			}
			
			return icon;
		}
		
		Image loadThumbnail() {
			Image img = null;
			ZipFile zipFile = null;

			try {
				zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();

				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();

					if (entry.getName().endsWith(THUMBNAIL_FILE)) {
						InputStream stream = zipFile.getInputStream(entry);
						img = ImageIO.read(stream);
						stream.close();
						break;
					}
				}
			} catch (Exception e) {
				logger.error("Cannot load session thumbnail from " + file.getName(), e);
			} finally {
				if (zipFile != null) {
					try {
						zipFile.close();
					} catch (final Exception ex) {
						logger.error("Unable to close file " + file.getName(), ex);
					}
				}
			}

			return img;
		}

		@Override
		public int hashCode() {
			final int prime = 17;
			int result = 7;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FileInfo other = (FileInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file)) {
				return false;
			}
			return true;
		}

		private StarterPanel getOuterType() {
			return StarterPanel.this;
		}
	}
}
