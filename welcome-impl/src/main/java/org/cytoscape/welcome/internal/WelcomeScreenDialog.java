package org.cytoscape.welcome.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class WelcomeScreenDialog extends JDialog {
	private static final long serialVersionUID = -2783045197802550425L;

	private static final String TITLE = "Welcome to Cytoscape 3";

	private static final Color PANEL_COLOR = new Color(0xff, 0xff, 0xff, 220);
	private static final Color LABEL_COLOR = new Color(0xa0, 0xa0, 0xa0, 230);
	private static final Color TRANSPARENT_COLOR = new Color(0xc0, 0xc0, 0xc0, 0);

	private static final Font TITLE_FONT = new Font("SansSerif", Font.PLAIN, 14);

	private static final String IMAGE_LOCATION = "images/background.png";
	private BufferedImage bgImage;

	private static final Dimension DEF_SIZE = new Dimension(600, 500);

	private BackgroundImagePanel basePanel;
	private JPanel mainPanel;

	private final OpenBrowser openBrowserServiceRef;
	private final RecentlyOpenedTracker fileTracker;
	private final OpenSessionTaskFactory openSessionTaskFactory;

	private final DialogTaskManager guiTaskManager;

	private final CyApplicationConfiguration config;
	private final LoadNetworkURLTaskFactory loadNetworkTF;

	private final DataSourceManager dsManager;
	
	private final CyProperty<Properties> cyProps;
	
	private final TaskFactory importNetworkFileTF;
	private final BundleContext bc;
	
	final JCheckBox checkBox = new JCheckBox();

	public WelcomeScreenDialog(final BundleContext bc, OpenBrowser openBrowserServiceRef, 
	                           RecentlyOpenedTracker fileTracker, final OpenSessionTaskFactory openSessionTaskFactory, 
							   DialogTaskManager guiTaskManager, final CyApplicationConfiguration config,
	                           final TaskFactory importNetworkFileTF, final LoadNetworkURLTaskFactory importNetworkTF, 
	                           final DataSourceManager dsManager, final CyProperty<Properties> cyProps, 
							   final boolean hide) {
		this.openBrowserServiceRef = openBrowserServiceRef;
		this.fileTracker = fileTracker;
		this.config = config;
		this.loadNetworkTF = importNetworkTF;
		this.dsManager = dsManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.importNetworkFileTF = importNetworkFileTF;
		this.bc = bc;

		this.guiTaskManager = guiTaskManager;
		this.cyProps = cyProps;

		initComponents();

		this.setTitle(TITLE);

		this.setSize(DEF_SIZE);
		this.setPreferredSize(DEF_SIZE);
		this.setMinimumSize(DEF_SIZE);
		this.setMaximumSize(DEF_SIZE);
		this.setResizable(false);
		this.setModal(true);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		this.setAlwaysOnTop(true);
		
		checkBox.setSelected(hide);
	}
	
	public boolean getHideStatus() {
		return checkBox.isSelected();
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

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		bottomPanel.setBackground(PANEL_COLOR);
		
		checkBox.setText("Don't show again");
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cyProps.getProperties().setProperty(WelcomeScreenAction.DO_NOT_DISPLAY_PROP_NAME, ((Boolean)checkBox.isSelected()).toString());
			}
		});
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		bottomPanel.add(checkBox, BorderLayout.CENTER);
		bottomPanel.add(closeButton, BorderLayout.EAST);
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

		buildHelpPanel(panel1, new OpenPanel(this, fileTracker, guiTaskManager, openSessionTaskFactory),
				"Open a Recent Session");
		buildHelpPanel(panel2, new CreateNewNetworkPanel(this, bc, guiTaskManager, importNetworkFileTF, loadNetworkTF, config, dsManager, cyProps), "Create New Network");
		buildHelpPanel(panel3, new HelpPanel(openBrowserServiceRef, cyProps), "Help");
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
