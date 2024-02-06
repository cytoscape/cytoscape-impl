package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.IconManager.ICON_REMOVE;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.internal.view.util.SVGIcon;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.jdesktop.swingx.color.ColorUtil;
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
	
	public static final String PY4CYTOSCAPE_URL = "https://py4cytoscape.readthedocs.io/en/latest/";
	public static final String RCY3_URL = "https://bioconductor.org/packages/release/bioc/html/RCy3.html";
	public static final String TUTORIAL_URL = "https://tutorials.cytoscape.org";
	public static final String NEWS_URL = "https://www.ncbi.nlm.nih.gov/pmc/?term=(cytoscape+AND+network)&report=imagesdocsum&dispmax=100";
	
	private final Color BG_COLOR = UIManager.getColor("Table.background");
	private final Color CONTRAST_COLOR = UIManager.getColor("Panel.background");
	
	private static final int PANEL_PAD = 4;
	
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
	
	private JLabel py4cytoscapeLabel = createLinkLabel("py4cytoscape", "/images/python-logo.svg", PY4CYTOSCAPE_URL);
	private JLabel rcy3Label = createLinkLabel("RCy3", "/images/r-logo.svg", RCY3_URL);
	private JLabel tutorialsLabel = createLinkLabel("Tutorials", null, TUTORIAL_URL);
	private JLabel newsLabel = createLinkLabel("Published Figures", null, NEWS_URL);
	
	private final Icon missingImageIcon;
	
	/** This flag prevents opening more than one session concurrently when one is about to be loaded by an action on this panel */
	private boolean sessionMayBeLoading;
	/** This flag prevents opening another session here when another one is already being loaded from anywhere else */
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
					+ getSampleSessionsPanel().getPreferredSize().height
					+ getLinksPanel().getPreferredSize().height;
			
			return new Dimension(w, h);
		}
	}
	
	public void update() {
		var recentFiles = getRecentFiles();
		getRecentSessionsPanel().update(recentFiles);
		getRecentSessionsPanel().setVisible(!recentFiles.isEmpty());
		
		getSampleSessionsPanel().update(getSampleFiles());
	}
	
	private void init() {
		setLayout(new BorderLayout());
		add(getTitlePanel(), BorderLayout.NORTH);
		add(getContentPane(), BorderLayout.CENTER);
		add(getLinksPanel(), BorderLayout.SOUTH);
	}

	public JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BG_COLOR));
			
			var layout = new GroupLayout(contentPane);
			contentPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getRecentSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getSampleSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getRecentSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getSampleSessionsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		return contentPane;
	}
	
	private JPanel getTitlePanel() {
		if (titlePanel == null) {
			titlePanel = new JPanel();
			titlePanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")),
					BorderFactory.createEmptyBorder(3, 4, 3, 4)
			));
			
			var titleLabel = new JLabel("Welcome to Cytoscape");
			titleLabel.setHorizontalAlignment(JLabel.CENTER);
			makeSmall(titleLabel);
			
			var layout = new GroupLayout(titlePanel);
			titlePanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
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
			recentSessionsPanel = new SessionListPanel("Recent Sessions:");
		}
		
		return recentSessionsPanel;
	}
	
	private SessionListPanel getSampleSessionsPanel() {
		if (sampleSessionsPanel == null) {
			sampleSessionsPanel = new SessionListPanel("Sample Sessions:");
		}
		
		return sampleSessionsPanel;
	}
	
	private JPanel getLinksPanel() {
		if (linksPanel == null) {
			linksPanel = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
			        
					var g2d = (Graphics2D) g;
			        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			        
			        int w = getWidth(), h = getHeight();
			        var color1 = CONTRAST_COLOR;
			        var color2 = BG_COLOR;
			        var gp = new GradientPaint(0, 0, color1, 0, h * 4, color2);
			        g2d.setPaint(gp);
			        g2d.fillRect(0, 0, w, h);
				}
			};
			linksPanel.setOpaque(false);
			
			equalizeSize(py4cytoscapeLabel, rcy3Label, tutorialsLabel, newsLabel);
			
			var layout = new GroupLayout(linksPanel);
			linksPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(py4cytoscapeLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(10)
					.addComponent(rcy3Label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(40)
					.addComponent(tutorialsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(10)
					.addComponent(newsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(py4cytoscapeLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(rcy3Label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
		}
		
		return closeButton;
	}
	
	private JLabel createLinkLabel(String text, String svgPath, String url) {
		int hpad = 5;
		var icon = svgPath != null ? new LeftSVGIcon(getClass().getResourceAsStream(svgPath), 24, 24, hpad) : null;
		int vpad = icon == null ? 5 : 1;
		int bw = 1; // border width
		
		var label = new JLabel(text, SwingConstants.CENTER);
		label.setOpaque(true);
		label.setBackground(BG_COLOR);
		label.setForeground(UIManager.getColor("CyColor.complement(+1)"));
		label.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BG_COLOR, bw),
				BorderFactory.createEmptyBorder(vpad, hpad, vpad, hpad)
		));
		
		if (icon != null) {
			// Calculate the best label width before setting the icon, so the icon can be positioned
			// at the left side of the label while the text is kept centered
			int iconTextGap = 5;
			var fm = label.getFontMetrics(label.getFont());
			int w = fm.stringWidth(text) + 2 * (icon.getIconWidth() + iconTextGap + hpad + bw);
			int h = icon.getIconHeight() + 2 * (vpad + bw);
			
			label.setPreferredSize(new Dimension(w, h));
			label.setHorizontalTextPosition(SwingConstants.CENTER); // keep the text centered!
			label.setIcon(icon);
		}
		
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				serviceRegistrar.getService(OpenBrowser.class).openURL(url);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				label.setText("<html><u>" + text + "</u></html>");
			}
			@Override
			public void mouseExited(MouseEvent e) {
				label.setText(text);
			}
		});
		
		return label;
	}
	
	/**
	 * Returns a list of the most recently opened session files.
	 */
	private List<FileInfo> getRecentFiles() {
		var files = new ArrayList<FileInfo>();
		
		var fileTracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
		var recentFiles = fileTracker.getRecentlyOpenedURLs();
		int fileCount = Math.min(recentFiles.size(), MAX_FILES);
		
		for (int i = 0; i < fileCount; i++) {
			var url = recentFiles.get(i);
			File file = null;
			
			try {
				var uri = url.toURI();
				file = new File(uri);
			} catch (Exception e) {
				logger.error("Invalid file URL.", e);
				continue;
			}
			
			if (file.exists() && file.canRead()) {
				var fi = new FileInfo(file, file.getName(), file.getAbsolutePath());
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
		var list = new ArrayList<FileInfo>();
		var dir = getExampleDir();
		
		if (dir != null && dir.exists() && dir.canRead()) {
			var files = dir.listFiles();
			
			if (files != null) {
				for (File f : files) {
					if (f.canRead() && f.getName().toLowerCase().endsWith(SESSION_EXT)) {
						var toolTip = 
								"<html>This (<b>" + f.getName() + "</b>) and other example files can be found in:<br />"
								+ dir.getAbsolutePath() + "</html>";
						
						var fi = new FileInfo(f, f.getName().replace(SESSION_EXT, ""), toolTip);
						list.add(fi);
					}
				}
			}
		}
		
		return list;
	}
	
	private final File getExampleDir() {
		var applicationCfg = serviceRegistrar.getService(CyApplicationConfiguration.class);

		if (applicationCfg != null) {
			return new File(applicationCfg.getInstallationDirectoryLocation() + "/" + SAMPLE_DATA_DIR + "/");
		} else {
			logger.error("application configuration is null, cannot find the installation directory");
			return null;
		}
	}
	
	private void maybeOpenSession(File file) {
		// This flag prevents opening more than one session concurrently (e.g. if the user clicks a session thumbnail
		// repeatedly because Cytoscape is temporarily unresponsive for some reason), which could cause
		// many issues and put Cytoscape in a bad state.
		sessionMayBeLoading = true;
		
		if (file.exists()) {
			maybeOpenSession(file, StarterPanel.this.getTopLevelAncestor());
		} else {
			JOptionPane.showMessageDialog(
					StarterPanel.this.getTopLevelAncestor(),
					"Session file not found:\n" + file.getAbsolutePath(),
					"File not Found",
					JOptionPane.WARNING_MESSAGE
			);
			
			var fileTracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
			
			try {
				fileTracker.remove(file.toURI().toURL());
			} catch (Exception e) {
				logger.error("Error removing session file from RecentlyOpenedTracker.", e);
			}
			
			sessionMayBeLoading = false; // Set it to false whenever the session open action is cancelled!
		}
	}
	
	private void maybeOpenSession(File file, Component owner) {
		if (file.exists() && file.canRead()) {
			var netManager = serviceRegistrar.getService(CyNetworkManager.class);
			var tableManager = serviceRegistrar.getService(CyTableManager.class);
			
			if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
				openSession(file);
			else
				openSessionWithWarning(file, owner);
		} else {
			sessionMayBeLoading = false;
		}
	}
	
	private void openSessionWithWarning(File file, Component owner) {
		if (JOptionPane.showConfirmDialog(
				owner,
				"Current session (all networks and tables) will be lost.\nDo you want to continue?",
				"Open Session",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
			openSession(file);
		else
			sessionMayBeLoading = false;
	}
	
	private void openSession(File file) {
		var taskFactory = serviceRegistrar.getService(OpenSessionTaskFactory.class);
		var taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		
		var observer = new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				// Ignore...
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				sessionMayBeLoading = false;
			}
		};
		
		taskManager.execute(taskFactory.createTaskIterator(file), observer);
	}
	
	private class SessionListPanel extends JPanel {
		
		private JScrollPane scrollPane;
		private JLabel titleLabel;
		private ScrollableListPanel listPanel;
		
		private final String title;
		
		SessionListPanel(String title) {
			this.title = title;
			
			setOpaque(false);
			
			setLayout(new BorderLayout());
			add(getTitleLabel(), BorderLayout.NORTH);
			add(getScrollPane(), BorderLayout.CENTER);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
	        
			var g2d = (Graphics2D) g;
	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	        
	        int w = getWidth(), h = getTitleLabel().getHeight();
	        var color1 = UIManager.getColor("Panel.background");
	        var color2 = BG_COLOR;
	        var gp = new GradientPaint(0, 0, color1, 0, h, color2);
	        g2d.setPaint(gp);
	        g2d.fillRect(0, 0, w, h);
		}
		
		void update(List<FileInfo> files) {
			var panel = getListPanel();
			panel.removeAll();
			
			for (var fi : files) {
				var sessionPanel = new SessionPanel(fi);
				panel.add(sessionPanel);
			}
		}
		
		List<SessionPanel> getAllPanels() {
			var lp = getListPanel();
			int total = lp.getComponentCount();
			var list = new ArrayList<SessionPanel>(total);
			
			for (int i = 0; i < total; i++)
				list.add((SessionPanel) lp.getComponent(i));
			
			return list;
		}
		
		JLabel getTitleLabel() {
			if (titleLabel == null) {
				titleLabel = new JLabel(title);
				titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
				makeSmall(titleLabel);
			}
			
			return titleLabel;
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
						var dim = new Dimension(w, super.getPreferredSize().height + getHorizontalScrollBar().getSize().height);
						setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						
						return dim;
					}
				};
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setBackground(getListPanel().getBackground());
				scrollPane.getViewport().setBackground(getListPanel().getBackground());
				scrollPane.setBorder(BorderFactory.createEmptyBorder());
				
				// Set a minimum size that shows at least one row
				var tmpSessionPanel = new SessionPanel(new FileInfo(new File("_tmp"), "TEMP", null));
				var tmpListPanel = new ScrollableListPanel();
				tmpListPanel.add(tmpSessionPanel);
				var tmpScrollPane = new JScrollPane(tmpListPanel);
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
				listPanel.setOpaque(false);
				listPanel.setBackground(BG_COLOR);
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
		
		static final int BORDER_WIDTH = 1;
		static final int PAD = 2;
		static final int NAME_WIDTH = 124;
		
		final Color PANEL_COLOR;
		final Color BORDER_COLOR;
		final Color FOCUS_BORDER_COLOR;
		final Color FOCUS_OVERLAY_COLOR;
		
		private JLabel thumbnailLabel;
		private JLabel nameLabel;
		
		protected SessionPanel overItem;
		
		private final FileInfo fileInfo;
	
		
		SessionPanel(FileInfo fileInfo) {
			this.fileInfo = fileInfo;
			
			PANEL_COLOR = ColorUtil.setAlpha(CONTRAST_COLOR, 100);
			BORDER_COLOR = UIManager.getColor("Separator.foreground");
			FOCUS_BORDER_COLOR = UIManager.getColor("Focus.color");
			FOCUS_OVERLAY_COLOR = ColorUtil.setAlpha(UIManager.getColor("Table.selectionBackground"), 55);
			
			init();
		}
		
		private void init() {
			setOpaque(false);
			setFocusable(true);
			
			int bw = BORDER_WIDTH + PAD;
			setBorder(BorderFactory.createEmptyBorder(bw, bw, bw, bw));
			
			var layout = new GroupLayout(this);
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
			
			var mouseListener = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (!sessionMayBeLoading && !sessionLoading
							&& e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e))
						maybeOpenSession(fileInfo.getFile());
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					SessionPanel.this.requestFocusInWindow();
					overItem = SessionPanel.this;
					repaint();
				}
				@Override
				public void mouseExited(MouseEvent e) {
					overItem = null;
					repaint();
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
				thumbnailLabel.setBackground(BG_COLOR);
				thumbnailLabel.setHorizontalAlignment(SwingConstants.CENTER);
				thumbnailLabel.setHorizontalTextPosition(SwingConstants.CENTER);
				thumbnailLabel.setToolTipText(fileInfo.getHelp());
				thumbnailLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
			}
			
			return thumbnailLabel;
		}
		
		private JLabel getNameLabel() {
			if (nameLabel == null) {
				nameLabel = new JLabel(fileInfo.getName());
				makeSmall(nameLabel);
				
				if (fileInfo.getFile() != null)
					nameLabel.setToolTipText(fileInfo.getFile().getPath());
				
				nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
				
				var d = new Dimension(NAME_WIDTH, nameLabel.getPreferredSize().height);
				nameLabel.setMinimumSize(d);
				nameLabel.setPreferredSize(d);
				nameLabel.setSize(d);
			}
			
			return nameLabel;
		}
		
		@Override
		public void paint(Graphics g) {
			var g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			int w = this.getWidth();
			int h = this.getHeight();
			int arc = 10;

			g2d.setColor(PANEL_COLOR);
			g2d.fillRoundRect(BORDER_WIDTH, BORDER_WIDTH, w - 2 * BORDER_WIDTH, h - 2 * BORDER_WIDTH, arc, arc);
			
			super.paint(g);
			
			// Add a colored border and transparent overlay on top if it currently has focus
			if (overItem == this) {
				g2d.setColor(FOCUS_OVERLAY_COLOR);
				g2d.fillRect(BORDER_WIDTH, BORDER_WIDTH, w - 2 * BORDER_WIDTH, h - 2 * BORDER_WIDTH);
				
				g2d.setColor(FOCUS_BORDER_COLOR);
				g2d.setStroke(new BasicStroke(BORDER_WIDTH));
				g2d.drawRoundRect(BORDER_WIDTH, BORDER_WIDTH, w - 2 * BORDER_WIDTH, h - 2 * BORDER_WIDTH, arc, arc);
			}

			g2d.dispose();
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

				var insets = target.getInsets();
				
				if (insets == null)
					insets = new Insets(0, 0, 0, 0);
				
				int reqdWidth = 0;

				int maxwidth = w - (insets.left + insets.right + hgap * 2);
				int n = target.getComponentCount();
				int x = 0;
				int y = insets.top + vgap; // FlowLayout starts by adding vgap, so do that here too.
				int rowHeight = 0;

				for (int i = 0; i < n; i++) {
					var c = target.getComponent(i);
					
					if (c.isVisible()) {
						var d = c.getPreferredSize();
						
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
					var c = target.getComponent(i);
					
					if (c.isVisible()) {
						found_one = true;
						var d = c.getPreferredSize();
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
	
	/**
	 * A version of SVGIcon which makes sure the icon is aligned at the left edge of the component,
	 * not just positioned to the left of the text.
	 */
	private class LeftSVGIcon extends SVGIcon {
		
		private int padding;

		public LeftSVGIcon(InputStream is, int width, int height, int padding) {
			super(is, width, height);
			this.padding = padding;
		}
		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			var g2d = (Graphics2D) g.create();
			
			// Translate to close to the left border
            var insets = getInsets();
            int tx = ((c.getWidth() - getIconWidth()) / 2) - insets.left - padding;
			
            g2d.translate(-tx, 0);
			super.paintIcon(c, g2d, x, y);
			
			g2d.dispose();
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
					var entries = zipFile.entries();
	
					while (entries.hasMoreElements()) {
						var entry = entries.nextElement();
	
						if (entry.getName().endsWith(THUMBNAIL_FILE)) {
							var stream = zipFile.getInputStream(entry);
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
						} catch (Exception ex) {
							logger.error("Unable to close file " + file.getName(), ex);
						}
					}
				}
			}

			return img;
		}

		@Override
		public int hashCode() {
			int prime = 17;
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
