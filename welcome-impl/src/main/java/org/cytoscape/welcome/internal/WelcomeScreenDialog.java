package org.cytoscape.welcome.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.cytoscape.property.CyProperty;
import org.cytoscape.welcome.internal.panel.CreateNewNetworkPanel;
import org.cytoscape.welcome.internal.panel.NewsAndLinkPanel;
import org.cytoscape.welcome.internal.panel.OpenPanel;
import org.cytoscape.welcome.internal.panel.WelcomeScreenChildPanel;

public class WelcomeScreenDialog extends JDialog {
	private static final long serialVersionUID = -2783045197802550425L;

	private static final String TITLE = "Welcome to Cytoscape";

	private static final Color PANEL_COLOR = new Color(0xff, 0xff, 0xff, 200);
	private static final Color LOGO_PANEL_COLOR = new Color(0xff, 0xff, 0xff, 100);

	private static final String IMAGE_LOCATION = "images/background.png";
	private BufferedImage bgImage;

	private static final Dimension DEF_SIZE = new Dimension(600, 500);

	private BackgroundImagePanel basePanel;
	private JPanel mainPanel;

	private final CyProperty<Properties> cyProps;

	private JCheckBox checkBox;

	// Child Panels
	private final CreateNewNetworkPanel importPanel;
	private final OpenPanel openPanel;
	private final NewsAndLinkPanel helpPanel;

	public WelcomeScreenDialog(final CreateNewNetworkPanel importPanel, final OpenPanel openPanel,
			final NewsAndLinkPanel helpPanel, final CyProperty<Properties> cyProps, final boolean hide) {

		this.importPanel = importPanel;
		this.openPanel = openPanel;
		this.helpPanel = helpPanel;

		this.importPanel.setParentWindow(this);
		this.openPanel.setParentWindow(this);
		this.helpPanel.setParentWindow(this);

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

		checkBox = new JCheckBox();
		checkBox.setFont(WelcomeScreenChildPanel.REGULAR_FONT);
		checkBox.setForeground(WelcomeScreenChildPanel.REGULAR_FONT_COLOR);

		mainPanel = new JPanel();
		mainPanel.setSize(DEF_SIZE);
		mainPanel.setLayout(new GridLayout(1, 2));
		mainPanel.setOpaque(false);

		basePanel.add(mainPanel, BorderLayout.CENTER);

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setBorder(new EmptyBorder(2, 10, 2, 10));
		bottomPanel.setBackground(PANEL_COLOR);
		bottomPanel.setPreferredSize(new Dimension(900, 30));

		checkBox.setText("Don't show again");
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cyProps.getProperties().setProperty(WelcomeScreenAction.DO_NOT_DISPLAY_PROP_NAME,
						((Boolean) checkBox.isSelected()).toString());
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

		final Container pane = this.getContentPane();
		pane.setLayout(new BorderLayout());
		this.add(basePanel, BorderLayout.CENTER);
		createChildPanels();

		pack();
	}

	private void createChildPanels() {
		JPanel openSessionPanel = new JPanel();
		JPanel newSessionPanel = new JPanel();
		JPanel newsPanel = new JPanel();

		openSessionPanel.setOpaque(false);
		newSessionPanel.setOpaque(false);
		newsPanel.setOpaque(false);

		Color borderPaint = new Color(0xff, 0xff, 0xff, 50);
		final LineBorder border = new LineBorder(borderPaint, 5, false);
		openSessionPanel.setBorder(border);
		newSessionPanel.setBorder(border);
		newsPanel.setBorder(border);

		openSessionPanel.setBackground(PANEL_COLOR);
		newSessionPanel.setBackground(PANEL_COLOR);
		newsPanel.setBackground(PANEL_COLOR);

		setChildPanel(openSessionPanel, openPanel, "Open Recent Session");
		setChildPanel(newSessionPanel, importPanel, "Start New Session");
		setChildPanel(newsPanel, helpPanel, "News and Links");

		final JPanel leftPanel = new JPanel();
		final JPanel rightPanel = new JPanel();
		leftPanel.setOpaque(false);
		leftPanel.setLayout(new GridLayout(2, 1));
		rightPanel.setOpaque(false);
		rightPanel.setLayout(new GridLayout(1, 1));

		leftPanel.setOpaque(false);
		leftPanel.setLayout(new GridLayout(1, 1));
		rightPanel.setOpaque(false);
		rightPanel.setLayout(new GridLayout(2, 1));

		mainPanel.setBorder(border);

		rightPanel.add(openSessionPanel);
		rightPanel.add(newsPanel);

		leftPanel.add(newSessionPanel);

		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
	}

	private void setChildPanel(JPanel panel, JPanel contentPanel, final String label) {
		JPanel titlePanel = new JPanel();
		contentPanel.setBackground(PANEL_COLOR);

		titlePanel.setLayout(new GridLayout(1, 2));
		titlePanel.setBackground(WelcomeScreenChildPanel.TITLE_BG_COLOR);
		panel.setLayout(new BorderLayout());

		final JLabel title = new JLabel();
		title.setFont(WelcomeScreenChildPanel.TITLE_FONT);
		title.setText(label);
		title.setForeground(WelcomeScreenChildPanel.TITLE_FONT_COLOR);
		title.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		titlePanel.add(title);
		panel.add(titlePanel, BorderLayout.NORTH);
		panel.add(contentPanel, BorderLayout.CENTER);
	}
}
