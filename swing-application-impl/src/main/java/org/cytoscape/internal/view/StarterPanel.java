package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.IconManager.ICON_REMOVE;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.internal.util.Util;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
	
	public static final String TUTORIAL_URL = "http://tutorials.cytoscape.org";
	public static final String NEWS_URL = "http://cytoscape-publications.tumblr.com";
	
	public final Color LIST_BG_COLOR = UIManager.getColor("Table.background");
	public final Color LIST_FOCUS_BG_COLOR = UIManager.getColor("Table.selectionBackground");
	public final Color LINK_FONT_COLOR = UIManager.getColor("Table.focusCellBackground");
	
	private static final int PANEL_PAD = 4;
	private static final int V_GAP = 10;
	private static final int BORDER_WIDTH = 2;
	
	private final Border DEF_BORDER = BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
	private final Border FOCUS_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), BORDER_WIDTH / 2),
			BorderFactory.createLineBorder(LIST_FOCUS_BG_COLOR, BORDER_WIDTH / 2));
	
	private static final String SAMPLE_DATA_DIR = "sampleData/sessions";
	private static final String SESSION_EXT = ".cys";
	
	private static final int MAX_FILES = 100;
	
	private static final String MISSING_IMAGE = "/images/logo-light-96.png";
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private JPanel contentPane;
	private SessionListPanel recentSessionsPanel;
	private SessionListPanel sampleSessionsPanel;
	private JPanel titlePanel;
	private JPanel linksPanel;
	
	private JButton closeButton;
	
	private JLabel tutorialsLabel = createLinkLabel("<html><u>Tutorials</u></html>", TUTORIAL_URL);
	private JLabel newsLabel = createLinkLabel("<html><u>News</u></html>", NEWS_URL);
	
	private final Icon missingImageIcon;
	
	private boolean ignoreClickEvents;
	private boolean sessionLoading;
	
	private final CyServiceRegistrar serviceRegistrar;

	public StarterPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		missingImageIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(MISSING_IMAGE)));
		
		setName(NAME);
		init();
	}

	public void setSessionLoading(boolean b) {
		sessionLoading = b;
	}

	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet())
			return super.getPreferredSize();
		
		synchronized (getTreeLock()) {
			int w = 2 * PANEL_PAD + Math.max(getRecentSessionsPanel().getPreferredSize().width,
					getSampleSessionsPanel().getPreferredSize().width);
			int h = 2 * PANEL_PAD
					+ getTitlePanel().getPreferredSize().height
					+ getRecentSessionsPanel().getPreferredSize().height
					+ V_GAP
					+ getSampleSessionsPanel().getPreferredSize().height
					+ getLinksPanel().getPreferredSize().height;
			
			return new Dimension(w, h);
		}
	}
	
	public void update() {
		List<FileInfo> recentFiles = getRecentFiles();
		getRecentSessionsPanel().update(recentFiles);
		getRecentSessionsPanel().setVisible(!recentFiles.isEmpty());
		
		getSampleSessionsPanel().update(getSampleFiles());
	}
	
	private void init() {
		setBorder(BorderFactory.createEmptyBorder(PANEL_PAD, PANEL_PAD, PANEL_PAD, PANEL_PAD));
		
		setLayout(new BorderLayout());
		add(getTitlePanel(), BorderLayout.NORTH);
		add(getContentPane(), BorderLayout.CENTER);
		add(getLinksPanel(), BorderLayout.SOUTH);
	}

	public JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setOpaque(false);
			
			GroupLayout layout = new GroupLayout(contentPane);
			contentPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getRecentSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getSampleSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getRecentSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(V_GAP)
					.addComponent(getSampleSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		return contentPane;
	}
	
	private JPanel getTitlePanel() {
		if (titlePanel == null) {
			titlePanel = new JPanel();
			titlePanel.setOpaque(false);
			
			JLabel titleLabel = new JLabel("Welcome to Cytoscape");
			titleLabel.setHorizontalAlignment(JLabel.CENTER);
			makeSmall(titleLabel);
			
			GroupLayout layout = new GroupLayout(titlePanel);
			titlePanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(getCloseButton().getPreferredSize().width)
					.addComponent(titleLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getCloseButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(titleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getCloseButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return titlePanel;
	}
	
	private SessionListPanel getRecentSessionsPanel() {
		if (recentSessionsPanel == null) {
			recentSessionsPanel = new SessionListPanel("Recent Sessions");
		}
		
		return recentSessionsPanel;
	}
	
	private SessionListPanel getSampleSessionsPanel() {
		if (sampleSessionsPanel == null) {
			sampleSessionsPanel = new SessionListPanel("Sample Sessions");
		}
		
		return sampleSessionsPanel;
	}
	
	private JPanel getLinksPanel() {
		if (linksPanel == null) {
			linksPanel = new JPanel();
			linksPanel.setOpaque(false);
		
			LookAndFeelUtil.equalizeSize(tutorialsLabel, newsLabel);
			
			GroupLayout layout = new GroupLayout(linksPanel);
			linksPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(tutorialsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(80)
					.addComponent(newsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(tutorialsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(newsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
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
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1)
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
			File file = null;
			
			try {
				URI uri = url.toURI();
				file = new File(uri);
			} catch (Exception e) {
				logger.error("Invalid file URL.", e);
				continue;
			}
			
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
		final List<FileInfo> list = new ArrayList<>();
		final File dir = getExampleDir();
		
		if (dir != null && dir.exists() && dir.canRead()) {
			File[] files = dir.listFiles();
			
			if (files != null) {
				for (File f : files) {
					if (f.canRead() && f.getName().toLowerCase().endsWith(SESSION_EXT)) {
						String toolTip = 
								"<html>This (<b>" + f.getName() + "</b>) and other example files can be found in:<br />"
								+ dir.getAbsolutePath() + "</html>";
						
						final FileInfo fi = new FileInfo(f, f.getName().replace(SESSION_EXT, ""), toolTip);
						list.add(fi);
					}
				}
			}
		}
		
		return list;
	}
	
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
			Util.maybeOpenSession(file, StarterPanel.this.getTopLevelAncestor(), serviceRegistrar);
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
	
	private void drawFocus(SessionPanel panel) {
		List<SessionPanel> all = getRecentSessionsPanel().getAllPanels();
		all.addAll(getSampleSessionsPanel().getAllPanels());
		
		for (SessionPanel p : all)
			drawFocus(p, panel == p);
	}
	
	private void drawFocus(SessionPanel panel, boolean hasFocus) {
		panel.setBorder(hasFocus ? FOCUS_BORDER : DEF_BORDER);
		panel.setBackground(hasFocus ? LIST_FOCUS_BG_COLOR : LIST_BG_COLOR);
	}
	
	private class SessionListPanel extends JPanel {
		
		private JScrollPane scrollPane;
		private ScrollableListPanel listPanel;
		
		SessionListPanel(String title) {
			setOpaque(false);
			
			JLabel titleLabel = new JLabel(title);
			titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 16, 2, 16));
			makeSmall(titleLabel);
			
			setLayout(new BorderLayout());
			add(titleLabel, BorderLayout.NORTH);
			add(getScrollPane(), BorderLayout.CENTER);
		}
		
		void update(List<FileInfo> files) {
			JPanel panel = getListPanel();
			panel.removeAll();
			
			for (final FileInfo fi : files) {
				SessionPanel sessionPanel = new SessionPanel(fi);
				panel.add(sessionPanel);
			}
		}
		
		List<SessionPanel> getAllPanels() {
			ScrollableListPanel lp = getListPanel();
			int total = lp.getComponentCount();
			ArrayList<SessionPanel> list = new ArrayList<>(total);
			
			for (int i = 0; i < total; i++)
				list.add((SessionPanel) lp.getComponent(i));
			
			return list;
		}
		
		JScrollPane getScrollPane() {
			if (scrollPane == null) {
				scrollPane = new JScrollPane(getListPanel()) {
					@Override
					public Dimension getPreferredSize() {
						if (getViewport().getView() == null)
							return super.getPreferredSize();
						
						// Trying to set the size of the scrollpane container so that the scrollbar does not appear
						int w = getViewport().getView().getPreferredSize().width;
						setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
						Dimension dim = new Dimension(w,
								super.getPreferredSize().height + getHorizontalScrollBar().getSize().height);
						setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						
						return dim;
					}
				};
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.getViewport().setBackground(getListPanel().getBackground());
				scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("Separator.foreground")));
				
				// Set a minimum size that shows at least one row
				SessionPanel tmpSessionPanel = new SessionPanel(new FileInfo(new File("_tmp"), "TEMP", null));
				ScrollableListPanel tmpListPanel = new ScrollableListPanel();
				tmpListPanel.add(tmpSessionPanel);
				JScrollPane tmpScrollPane = new JScrollPane(tmpListPanel);
				int w = tmpListPanel.getPreferredSize().width + 2;
				tmpScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				int vgap = ((ModifiedFlowLayout) tmpListPanel.getLayout()).getVgap() / 2;
				int h = tmpListPanel.getPreferredSize().height + tmpScrollPane.getHorizontalScrollBar().getHeight() + vgap + 2;
				scrollPane.setMinimumSize(new Dimension(w, h));
			}
			
			return scrollPane;
		}
		
		private ScrollableListPanel getListPanel() {
			if (listPanel == null) {
				listPanel = new ScrollableListPanel();
				listPanel.setBackground(LIST_BG_COLOR);
			}
			
			return listPanel;
		}
		
		private class ScrollableListPanel extends JPanel implements Scrollable {
			
			public ScrollableListPanel() {
				setLayout(new ModifiedFlowLayout());
			}
			
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				return getPreferredSize();
			}

			@Override
			public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 10;
			}

			@Override
			public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
				return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
			}

			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}

			@Override
			public boolean getScrollableTracksViewportHeight() {
				return false;
			}
		}
	}
	
	private class SessionPanel extends JPanel {
		
		static final int NAME_WIDTH = 124;
		
		private JLabel thumbnailLabel;
		private JLabel nameLabel;
		
		private final FileInfo fileInfo;

		SessionPanel(FileInfo fileInfo) {
			this.fileInfo = fileInfo;
			init();
		}
		
		private void init() {
			setFocusable(true);
			setBorder(DEF_BORDER);
			setBackground(LIST_BG_COLOR);
			
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
			
			MouseListener mouseListener = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (!ignoreClickEvents && !sessionLoading
							&& e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) {
						ignoreClickEvents = true;
						
						try {
							maybeOpenSession(fileInfo.getFile());
						} finally {
							ignoreClickEvents = false;
						}
					}
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					SessionPanel.this.requestFocusInWindow();
					drawFocus(SessionPanel.this);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					Component c = SwingUtilities.getDeepestComponentAt(e.getComponent(), e.getX(), e.getY());
					boolean inside = c != null && SwingUtilities.isDescendingFrom(c, SessionPanel.this);
					
					if (!inside)
						drawFocus(SessionPanel.this, false);
				}
			};
			
			addMouseListener(mouseListener);
			getThumbnailLabel().addMouseListener(mouseListener);
			getNameLabel().addMouseListener(mouseListener);
		}
		
		private JLabel getThumbnailLabel() {
			if (thumbnailLabel == null) {
				thumbnailLabel = new JLabel(fileInfo.getIcon());
				thumbnailLabel.setOpaque(true);
				thumbnailLabel.setBackground(LIST_BG_COLOR);
				thumbnailLabel.setHorizontalAlignment(SwingConstants.CENTER);
				thumbnailLabel.setHorizontalTextPosition(SwingConstants.CENTER);
				thumbnailLabel.setToolTipText(fileInfo.getHelp());
				thumbnailLabel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
			}
			
			return thumbnailLabel;
		}
		
		private JLabel getNameLabel() {
			if (nameLabel == null) {
				nameLabel = new JLabel(fileInfo.getName());
				nameLabel.setForeground(LINK_FONT_COLOR);
				makeSmall(nameLabel);
				
				if (fileInfo.getFile() != null)
					nameLabel.setToolTipText(fileInfo.getFile().getPath());
				
				nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
				
				Dimension d = new Dimension(NAME_WIDTH, nameLabel.getPreferredSize().height);
				nameLabel.setMinimumSize(d);
				nameLabel.setPreferredSize(d);
				nameLabel.setSize(d);
			}
			
			return nameLabel;
		}
	}
	
	/**
	 * A modified version of FlowLayout that allows containers using this Layout
	 * to behave in a reasonable manner when placed inside a JScrollPane.
	 * See http://stackoverflow.com/questions/3679886/how-can-i-let-jtoolbars-wrap-to-the-next-line-flowlayout-without-them-being-hi
	 * 
	 * @author Babu Kalakrishnan
	 * Modifications by greearb and jzd
	 */
	public class ModifiedFlowLayout extends FlowLayout {
		
		public ModifiedFlowLayout() {
			super();
		}

		public ModifiedFlowLayout(int align) {
			super(align);
		}

		public ModifiedFlowLayout(int align, int hgap, int vgap) {
			super(align, hgap, vgap);
		}

		@Override
		public Dimension minimumLayoutSize(Container target) {
			// Size of largest component, so we can resize it in either direction with something like a split-pane.
			return computeMinSize(target);
		}

		@Override
		public Dimension preferredLayoutSize(Container target) {
			return computeSize(target);
		}

		private Dimension computeSize(Container target) {
			synchronized (target.getTreeLock()) {
				int hgap = getHgap();
				int vgap = getVgap();
				int w = target.getWidth();

				// Let this behave like a regular FlowLayout (single row)
				// if the container hasn't been assigned any size yet
				if (w == 0)
					w = Integer.MAX_VALUE;

				Insets insets = target.getInsets();
				
				if (insets == null)
					insets = new Insets(0, 0, 0, 0);
				
				int reqdWidth = 0;

				int maxwidth = w - (insets.left + insets.right + hgap * 2);
				int n = target.getComponentCount();
				int x = 0;
				int y = insets.top + vgap; // FlowLayout starts by adding vgap, so do that here too.
				int rowHeight = 0;

				for (int i = 0; i < n; i++) {
					Component c = target.getComponent(i);
					
					if (c.isVisible()) {
						Dimension d = c.getPreferredSize();
						
						if ((x == 0) || ((x + d.width) <= maxwidth)) {
							// fits in current row.
							if (x > 0)
								x += hgap;
							
							x += d.width;
							rowHeight = Math.max(rowHeight, d.height);
						} else {
							// Start of new row
							x = d.width;
							y += vgap + rowHeight;
							rowHeight = d.height;
						}
						
						reqdWidth = Math.max(reqdWidth, x);
					}
				}
				
				y += rowHeight;
				y += insets.bottom;
				
				return new Dimension(reqdWidth + insets.left + insets.right, y);
			}
		}

		private Dimension computeMinSize(Container target) {
			synchronized (target.getTreeLock()) {
				int minx = Integer.MAX_VALUE;
				int miny = Integer.MIN_VALUE;
				boolean found_one = false;
				int n = target.getComponentCount();

				for (int i = 0; i < n; i++) {
					Component c = target.getComponent(i);
					
					if (c.isVisible()) {
						found_one = true;
						Dimension d = c.getPreferredSize();
						minx = Math.min(minx, d.width);
						miny = Math.min(miny, d.height);
					}
				}
				
				if (found_one)
					return new Dimension(minx, miny);
				
				return new Dimension(0, 0);
			}
		}
	}
	
	private final class FileInfo {
		
		private final String THUMBNAIL_FILE = "/session_thumbnail.png";
		
		private final File file;
		private final String name;
		private final String help;
		private Icon icon;
		
		FileInfo(File file, String name, String help) {
			this.file = file;
			this.help = help;
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
			
			if (file != null && file.canRead()) {
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
