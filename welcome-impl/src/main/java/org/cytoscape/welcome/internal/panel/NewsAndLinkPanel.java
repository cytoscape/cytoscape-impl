package org.cytoscape.welcome.internal.panel;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.OpenBrowser;

public class NewsAndLinkPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = -1685752658901305871L;

	private JLabel about;
	private JLabel manual;
	private JLabel tutorial;
	private JLabel bugReport;

	private final List<JLabel> labelSet;
	private final Map<JLabel, String> urlMap;

	private final OpenBrowser openBrowserServiceRef;
	private final CyProperty<Properties> cyProps;

	private final StatusPanel statusPanel;

	public NewsAndLinkPanel(final StatusPanel statusPanel, final OpenBrowser openBrowserServiceRef,
			CyProperty<Properties> cyProps) {
		labelSet = new ArrayList<JLabel>();
		urlMap = new HashMap<JLabel, String>();
		this.openBrowserServiceRef = openBrowserServiceRef;
		this.cyProps = cyProps;
		this.statusPanel = statusPanel;
		initComponents();
	}

	private void initComponents() {
		this.setLayout(new GridLayout(2,1));
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		
		final JPanel linkPanel = new JPanel();
		linkPanel.setOpaque(false);
		linkPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(REGULAR_FONT_COLOR, 1),
				"Web Links", TitledBorder.LEFT, TitledBorder.CENTER, REGULAR_FONT, REGULAR_FONT_COLOR));
		linkPanel.setLayout(new GridLayout(2, 2));

		
		about = new JLabel("<html><u>About Cytoscape</u></html>");
		manual = new JLabel("<html><u>Documentation</u></html>");
		tutorial = new JLabel("<html><u>Tutorials</u></html>");
		bugReport = new JLabel("<html><u>Report a bug</u></html>");

		// get Cytoscape version
		String cyversion = this.cyProps.getProperties().getProperty("cytoscape.version.number");

		// get OS string
		String os_str = System.getProperty("os.name") + "_" + System.getProperty("os.version");
		os_str = os_str.replace(" ", "_");

		labelSet.add(about);
		labelSet.add(manual);
		labelSet.add(tutorial);
		labelSet.add(bugReport);
		urlMap.put(about, "http://www.cytoscape.org/what_is_cytoscape.html");
		urlMap.put(manual, "http://www.cytoscape.org/documentation_users.html");
		urlMap.put(tutorial, "http://opentutorials.cgl.ucsf.edu/index.php/Portal:Cytoscape3");
		urlMap.put(bugReport, "http://chianti.ucsd.edu/cyto_web/bugreport/bugreport.php?cyversion=" + cyversion
				+ "&os=" + os_str);

		for (final JLabel label : labelSet) {
			label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
			label.setFont(LINK_FONT);
			label.setForeground(LINK_FONT_COLOR);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setCursor(new Cursor(Cursor.HAND_CURSOR));
			label.addMouseListener(new LabelMouseListener(label, urlMap.get(label), this));
			linkPanel.add(label);
		}

		statusPanel.setOpaque(false);
		statusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(REGULAR_FONT_COLOR, 1),
				"Latest News", TitledBorder.LEFT, TitledBorder.CENTER, REGULAR_FONT, REGULAR_FONT_COLOR));
		
		add(linkPanel);
		add(statusPanel);
		
	}

	private final class LabelMouseListener extends MouseAdapter {

		private final String url;

		LabelMouseListener(final JLabel label, final String url, final JPanel parent) {
			this.url = url;
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			closeParentWindow();
			openBrowserServiceRef.openURL(url);
		}
	}

}
