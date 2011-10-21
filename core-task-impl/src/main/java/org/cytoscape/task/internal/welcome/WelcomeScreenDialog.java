package org.cytoscape.task.internal.welcome;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.task.internal.session.OpenSessionTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;

public class WelcomeScreenDialog extends JDialog {
	private static final long serialVersionUID = -2783045197802550425L;

	private static final String TITLE = "Welcome to Cytoscape 3";

	private static final Color PANEL_COLOR = new Color(0xff, 0xff, 0xff, 200);
	private static final Color LABEL_COLOR = new Color(0xa0, 0xa0, 0xa0, 220);
	private static final Color TRANSPARENT_COLOR = new Color(0xc0, 0xc0, 0xc0, 0);

	private static final Font TITLE_FONT = new Font("SansSerif", Font.PLAIN, 14);

	private static final String IMAGE_LOCATION = "images/background.png";
	private BufferedImage bgImage;

	private static final Dimension DEF_SIZE = new Dimension(600, 450);

	private BackgroundImagePanel basePanel;
	private JPanel mainPanel;

	private final OpenBrowser openBrowserServiceRef;
	private final RecentlyOpenedTracker fileTracker;

	private TaskManager guiTaskManager;
	private final OpenSpecifiedSessionTaskFactory taskFactory;
	private final OpenSessionTaskFactory openTaskFactory;

	private final LoadMitabFileTaskFactory loadTF;
	private final CyApplicationConfiguration config;
	private final TaskFactory loadNetworkTF;

	WelcomeScreenDialog(Component parent, OpenBrowser openBrowserServiceRef, RecentlyOpenedTracker fileTracker,
			TaskManager guiTaskManager, OpenSpecifiedSessionTaskFactory taskFactory,
			final OpenSessionTaskFactory openTaskFactory, final LoadMitabFileTaskFactory loadTF,
			final CyApplicationConfiguration config, final TaskFactory layoutTF) {
		this.openBrowserServiceRef = openBrowserServiceRef;
		this.loadTF = loadTF;
		this.fileTracker = fileTracker;
		this.config = config;
		this.loadNetworkTF = layoutTF;

		this.guiTaskManager = guiTaskManager;
		this.taskFactory = taskFactory;
		this.openTaskFactory = openTaskFactory;

		initComponents();

		this.setTitle(TITLE);

		this.setSize(DEF_SIZE);
		this.setPreferredSize(DEF_SIZE);
		this.setMinimumSize(DEF_SIZE);
		this.setMaximumSize(DEF_SIZE);
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		this.setModal(true);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		this.setAlwaysOnTop(true);
	}

	private void initComponents() {

		try {
			bgImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(IMAGE_LOCATION));
		} catch (IOException e) {
			e.printStackTrace();
		}

		basePanel = new BackgroundImagePanel(bgImage);
		basePanel.setBackground(new Color(0xaa, 0xaa, 0xaa, 30));
		basePanel.setLayout(new BorderLayout());

		mainPanel = new JPanel();
		mainPanel.setSize(DEF_SIZE);
		mainPanel.setLayout(new GridLayout(2, 2));
		mainPanel.setOpaque(false);

		basePanel.add(mainPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(PANEL_COLOR);
		final JCheckBox checkBox = new JCheckBox();
		checkBox.setText("Don't show again");
		bottomPanel.add(checkBox);
		checkBox.setHorizontalAlignment(SwingConstants.CENTER);
		basePanel.add(bottomPanel, BorderLayout.SOUTH);

		this.add(basePanel);
		createChildPanels();

		pack();
	}

	private void createChildPanels() {
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel panel4 = new JPanel();

		panel1.setOpaque(false);
		panel2.setOpaque(false);
		panel3.setOpaque(false);
		panel4.setOpaque(false);

		Color borderPaint = new Color(0xff, 0xff, 0xff, 50);
		final LineBorder border = new LineBorder(borderPaint, 10, false);
		panel1.setBorder(border);
		panel2.setBorder(border);
		panel3.setBorder(border);
		panel4.setBorder(border);

		panel1.setBackground(PANEL_COLOR);
		panel2.setBackground(PANEL_COLOR);
		panel3.setBackground(PANEL_COLOR);
		panel4.setBackground(PANEL_COLOR);

		buildHelpPanel(panel1, new OpenPanel(this, fileTracker, guiTaskManager, taskFactory, openTaskFactory),
				"Open a Recent Session");
		buildHelpPanel(panel2, new CreateNewNetworkPanel(this, guiTaskManager, loadTF, config, loadNetworkTF), "Create New Network");
		buildHelpPanel(panel3, new HelpPanel(openBrowserServiceRef), "Help");
		buildHelpPanel(panel4, new LogoPanel(), "Latest News");

		mainPanel.setBorder(border);

		mainPanel.add(panel1);
		mainPanel.add(panel2);
		mainPanel.add(panel3);
		mainPanel.add(panel4);
	}

	private void buildHelpPanel(JPanel panel, JPanel contentPanel, final String label) {
		JPanel titlePanel = new JPanel();
		contentPanel.setBackground(PANEL_COLOR);

		// titlePanel.setOpaque(false);
		titlePanel.setLayout(new GridLayout(1, 2));
		titlePanel.setBackground(LABEL_COLOR);
		panel.setLayout(new BorderLayout());

		final JLabel title = new JLabel();
		title.setFont(TITLE_FONT);
		title.setText(label);
		title.setForeground(Color.DARK_GRAY);
		title.setBorder(new LineBorder(TRANSPARENT_COLOR, 8, false));
		titlePanel.add(title);
		panel.add(titlePanel, BorderLayout.NORTH);
		panel.add(contentPanel, BorderLayout.CENTER);
	}
}
