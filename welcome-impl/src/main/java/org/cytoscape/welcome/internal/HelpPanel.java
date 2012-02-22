package org.cytoscape.welcome.internal;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.OpenBrowser;

public class HelpPanel extends JPanel {
	
	private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 12);
	private static final Color LABEL_COLOR = new Color(0x40, 0x40, 0x40);

	private static final Color SELECTED_COLOR = new Color(0x63, 0xB8, 0xFF);
	private static final Font SELECTED_FONT = new Font("SansSerif", Font.BOLD, 12);

	
	private JLabel about;
	private JLabel manual;
	private JLabel tutorial;
	private JLabel bugReport;
	
	private final List<JLabel> labelSet;
	private final Map<JLabel, String> urlMap;
	
	private final OpenBrowser openBrowserServiceRef;
	private final CyProperty<Properties> cyProps;
	
	HelpPanel(final OpenBrowser openBrowserServiceRef, CyProperty<Properties> cyProps) {
		labelSet = new ArrayList<JLabel>();
		urlMap = new HashMap<JLabel, String>();
		this.openBrowserServiceRef = openBrowserServiceRef;
		this.cyProps = cyProps;
		initComponents();
	}

	private void initComponents() {
		this.setLayout(new GridLayout(4, 1));
		about = new JLabel("     About Cytoscape >>");
		manual = new JLabel("     User Documentation >>");
		tutorial = new JLabel("     Tutorials >>");
		bugReport = new JLabel("     Report a bug >>");
		
		// get Cytoscape version
		String cyversion = this.cyProps.getProperties().getProperty("cytoscape.version.number");
		
		// get OS string
		String os_str = System.getProperty("os.name")+ "_"+ System.getProperty("os.version");
		os_str = os_str.replace(" ", "_");
		
		labelSet.add(about);
		labelSet.add(manual);
		labelSet.add(tutorial);
		labelSet.add(bugReport);
		urlMap.put(about, "http://www.cytoscape.org/what_is_cytoscape.html");
		urlMap.put(manual, "http://www.cytoscape.org/documentation_users.html");
		urlMap.put(tutorial, "http://opentutorials.cgl.ucsf.edu/index.php/Portal:Cytoscape3");
		urlMap.put(bugReport, "http://chianti.ucsd.edu/cyto_web/bugreport/bugreport.php?cyversion="+cyversion+"&os="+os_str);
		
		for(final JLabel label: labelSet) {
			label.setFont(LABEL_FONT);
			label.setForeground(LABEL_COLOR);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setOpaque(false);
			label.addMouseListener(new LabelMouseListener(label, urlMap.get(label), this));
			add(label);
		}
	}
	
	private final class LabelMouseListener extends MouseAdapter {
		
		private final String url;
		
		LabelMouseListener(final JLabel label, final String url, final JPanel parent) {
			this.url = url;
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			openBrowserServiceRef.openURL(url);			
		}
	}
	

}
