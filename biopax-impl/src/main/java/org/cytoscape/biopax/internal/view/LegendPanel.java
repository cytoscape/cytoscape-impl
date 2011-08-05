package org.cytoscape.biopax.internal.view;


import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.biopax.internal.util.WebFileConnect;
import org.cytoscape.session.CyApplicationManager;

/**
 * Displays the Default Visual Style Legend for the BioPAX Mapper.
 *
 * @author Ethan Cerami
 */
public class LegendPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
     * BioPAX Legend.
     */
    public static int BIOPAX_LEGEND = 0;

    /**
     * Binary Legend.
     */
    public static int BINARY_LEGEND = 1;

    /**
	 * Constructor.
	 *
	 */
	public LegendPanel(int mode, final CyApplicationManager applicationManager, final CySwingApplication swingApplication) {
		this.setLayout(new BorderLayout());

		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setContentType("text/html");
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        URL legendUrl;
        if (mode == BIOPAX_LEGEND) {
            legendUrl = LegendPanel.class.getResource("legend.html");
        } else {
            legendUrl = LegendPanel.class.getResource("binary_legend.html");
        }
        StringBuffer temp = new StringBuffer();
		temp.append("<HTML><BODY>");

		try {
			String legendHtml = WebFileConnect.retrieveDocument(legendUrl.toString());
			temp.append(legendHtml);
		} catch (Exception e) {
			temp.append("Could not load legend... " + e.toString());
		}

		temp.append("</BODY></HTML>");
		textPane.setText(temp.toString());

		textPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String name = hyperlinkEvent.getDescription();
                    if (name.equalsIgnoreCase("filter")) {
                        new EdgeFilterUi(applicationManager.getCurrentNetwork(), swingApplication);
                    }
                }
            }
        });
        
        BioPaxDetailsPanel.modifyStyleSheetForSingleDocument(textPane);

        JScrollPane scrollPane = new JScrollPane(textPane);
		this.add(scrollPane, BorderLayout.CENTER);
	}
}
