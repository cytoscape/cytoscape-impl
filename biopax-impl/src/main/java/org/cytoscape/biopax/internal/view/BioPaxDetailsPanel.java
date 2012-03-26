// $Id: BioPaxDetailsPanel.java,v 1.24 2006/10/09 20:48:04 cerami Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.biopax.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SizeRequirements;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ParagraphView;
import javax.swing.text.html.StyleSheet;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.cytoscape.application.CyApplicationManager;

import static org.cytoscape.biopax.internal.BioPaxMapper.*;

import org.cytoscape.biopax.internal.action.LaunchExternalBrowser;
import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * BioPAX Details Panel.
 *
 * @author Ethan Cerami.
 */
public class BioPaxDetailsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public static final Logger log = LoggerFactory.getLogger(BioPaxDetailsPanel.class);
	
	CyApplicationManager applicationManager;
	
	/**
	 * Foreground Color.
	 */
	public static final Color FG_COLOR = new Color(75, 75, 75);
	private JScrollPane scrollPane;
	private JTextPane textPane;

	/**
	 * Constructor.
	 */
	public BioPaxDetailsPanel(LaunchExternalBrowser browser) {
		textPane = new JTextPane();

		//  Set Editor Kit that is capable of handling long words
		MyEditorKit kit = new MyEditorKit();
		textPane.setEditorKit(kit);
        modifyStyleSheetForSingleDocument(textPane);

        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textPane.setBorder(new EmptyBorder (5,5,5,5));
        textPane.setContentType("text/html");
		textPane.setEditable(false);
		textPane.addHyperlinkListener(browser);
		resetText();

		scrollPane = new JScrollPane(textPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(300, 300));
		this.setMaximumSize(new Dimension(300, 300));
	}

    public static void modifyStyleSheetForSingleDocument(JTextPane textPane) {
        HTMLDocument htmlDoc = (HTMLDocument) textPane.getDocument();
        StyleSheet styleSheet = htmlDoc.getStyleSheet();
        styleSheet.addRule("h2 {color: #663333; font-size: 120%; font-weight: bold; "
                + "margin-bottom:3px}");
        styleSheet.addRule("h3 {color: #663333; font-size: 105%; font-weight: bold;"
                + "margin-bottom:7px}");
        styleSheet.addRule("ul { list-style-type: none; margin-left: 5px; "
                + "padding-left: 1em;	text-indent: -1em;}");
        styleSheet.addRule("h4 {color: #66333; font-weight: bold; margin-bottom:3px;}");
        styleSheet.addRule(".link {color:blue; text-decoration: underline;}");
        styleSheet.addRule(".description {font-size: 85%;}");
        styleSheet.addRule(".rule {font-size: 90%; font-weight:bold}");
        styleSheet.addRule(".excerpt {font-size: 90%;}");
    }

    /**
	 * Resets the Text to "Select a node to view details...";
	 */
	public void resetText() {
		StringBuffer temp = new StringBuffer();
		temp.append("<HTML><BODY>");
		temp.append("Select a node to view details...");
		temp.append("</BODY></HTML>");
		textPane.setText(temp.toString());
	}

	/**
	 * Resets the Text to the specified Text String.
	 *
	 * @param text Text String.
	 */
	public void resetText(String text) {
		StringBuffer temp = new StringBuffer();
		temp.append("<HTML><BODY>");
		temp.append(text);
		temp.append("</BODY></HTML>");
		textPane.setText(temp.toString());
	}

	/**
	 * Shows details about the BioPAX Entity with the specified RDF ID.
	 *
	 * @param nodeID RDF ID String.
	 */
	public void showDetails(CyNetwork network, CyNode node) {
        String stringRef;

		StringBuffer buf = new StringBuffer("<HTML>");

		CyRow row = network.getRow(node);
		
        // name
        stringRef = row.get(CyNetwork.NAME, String.class);
        buf.append("<h2>" + stringRef + "</h2>");

        // type (to the text buffer)
        String type = network.getRow(node).get(BIOPAX_ENTITY_TYPE, String.class);
        buf.append("<h3>" + type + "</h3>");
        
        // organism
        stringRef = null;
        stringRef = row.get("/entityReference/organism/displayName", String.class);
        if(stringRef == null)
        	stringRef = row.get("/organism/displayName", String.class);
        if (stringRef != null) {
            buf.append("<h3>" + stringRef + "</h3>");
        }

        //  Add (optional) cPath Link
        addCPathLink(network, node, buf);
        
		// cellular location
		stringRef = null;
        stringRef = row.get("/cellularLocation", String.class);
        if (stringRef != null) {
           appendHeader("Cellular Location: " + stringRef, buf);
        }
		
		// chemical modification
		addAttributeList(network, node, null,
		                 BIOPAX_CHEMICAL_MODIFICATIONS_LIST, "Chemical Modifications:", buf);

		// data source
        addAttributeList(network, node, null, "/dataSource", "Data sources:", buf);
        
		
		// links
		addLinks(network,node, buf);

		
		// excerpt from the BioPAX OWL
		stringRef = null;
        stringRef = network.getRow(node,CyNetwork.HIDDEN_ATTRS).get(BioPaxUtil.BIOPAX_DATA, String.class);
        //stringRef = row.get(BioPaxUtil.BIOPAX_DATA, String.class);
        if (stringRef != null) {
        	appendHeader("BioPAX L3 (excerpt)", buf);
            buf.append("<pre>" + StringEscapeUtils.escapeXml(stringRef) + "</pre>");
        }
		
		buf.append("</BODY></HTML>");
		textPane.setText(buf.toString());
		textPane.setCaretPosition(0);

		//  If the containing parent is a BioPaxDetailsWindow, show it.
		//  This only applies in Cytoscape 2.1 and local testing
		Container parent = this.getTopLevelAncestor();
    }

	//TODO test; remove or upgrade to PC2 api (URI based)
	@Deprecated
    private void addCPathLink(CyNetwork cyNetwork, CyNode node, StringBuffer buf) {
    	CyRow networkRow = cyNetwork.getRow(cyNetwork);
        String serverName = networkRow.get("CPATH_SERVER_NAME", String.class);
        String serverDetailsUrl = networkRow.get("CPATH_SERVER_DETAILS_URL", String.class);
        if (serverName != null && serverDetailsUrl != null) {
        	CyRow nodeRow = cyNetwork.getRow(node);
            String type = nodeRow.get(BIOPAX_ENTITY_TYPE, String.class);
            if (BioPaxUtil.getSubclassNames(PhysicalEntity.class).contains(type)) {
                String url = serverDetailsUrl + node;
                buf.append ("<h3><A href='" + url + "'>" + serverName + ": " + node + "</A>");
            }
        }
    }

    private void addLinks(CyNetwork network, CyNode node, StringBuffer buf) {
    	CyRow row = network.getRow(node);

        addAttributeList(network, node, CyNetwork.HIDDEN_ATTRS,
                BIOPAX_UNIFICATION_REFERENCES, "Links:", buf);
        addAttributeList(network, node, CyNetwork.HIDDEN_ATTRS,
                BIOPAX_RELATIONSHIP_REFERENCES, null, buf);
        addAttributeList(network, node, CyNetwork.HIDDEN_ATTRS,
                BIOPAX_PUBLICATION_REFERENCES, "Publications:", buf);
         
        addIHOPLinks(network, node, buf);
	}

	private void addAttributeList(CyNetwork network, CyNode node, String tableName, 
			String attribute, String label, StringBuffer buf) 
	{
		StringBuffer displayString = new StringBuffer();
		// use private or default table
		CyRow row = (tableName == null) ? network.getRow(node) : network.getRow(node,tableName);
		if (row.getTable().getColumn(attribute) == null) {
			return;
		}
		
		List<String> list = row.getList(attribute, String.class);
        if (list == null) {
        	return;
        }
        
        int len = list.size();
        boolean tooMany = false;
        if (len > 7) {
            len = 7;
            tooMany = true;
        }
        for (int lc = 0; lc < len; lc++) {
			String listItem = list.get(lc);

			if ((listItem != null) && (listItem.length() > 0)) {
                displayString.append("<LI> - " + listItem);
                displayString.append("</LI>"); 
			}
		}
        if (tooMany) {
            displayString.append("<LI>  ...</LI>");
        }

        // do we have a string to display ?
		if (displayString.length() > 0) {
			if(label != null)
				appendHeader(label, buf);
            buf.append ("<UL>");
            appendData(displayString.toString(), buf, false);
            buf.append ("</UL>");
        }
	}

	private void appendHeader(String header, StringBuffer buf) {
		buf.append("<h4>");
		buf.append(header);
		buf.append("</h4>");
	}

	private void appendData(String data, StringBuffer buf, boolean appendBr) {
		buf.append(data);
		if (appendBr) {
			buf.append("<BR>");
		}
	}

	private void addIHOPLinks(CyNetwork network, CyNode node, StringBuffer buf) {
		CyRow row = network.getRow(node,CyNetwork.HIDDEN_ATTRS);
		String ihopLinks = row.get(BIOPAX_IHOP_LINKS, String.class);

		if (ihopLinks != null) {
			buf.append("<UL>");
			appendData(ihopLinks, buf, false);
			buf.append("</UL>");
		}
	}
}


/**
 * Editor Kit which is capable of handling long words.
 * <p/>
 * Here is a description of the problem:
 * By default, JTextPane uses an InlineView. It was designed to avoid
 * wrapping.  Text can't be broken if it doesn't contain spaces.
 * <p/>
 * This is a real problem with the BioPaxDetailsPanel, as BioPax Unique
 * Identifiers can get really long, and this prevents the user from
 * resizing the CytoPanel to any arbitrary size.
 * <p/>
 * The solution below comes from:
 * http://joust.kano.net/weblog/archives/000074.html
 * <p/>
 * (The following code is released in the public domain.)
 *
 * @author Joust Team.
 */
class MyEditorKit extends HTMLEditorKit {
	private static final long serialVersionUID = 1L;

	/**
	 * Gets the ViewFactor Object.
	 *
	 * @return View Factor Object.
	 */
	public ViewFactory getViewFactory() {
		return new MyViewFactory(super.getViewFactory());
	}

	/**
	 * Word Splitting Paragraph View.
	 */
	private static class WordSplittingParagraphView extends ParagraphView {
		public WordSplittingParagraphView(javax.swing.text.Element elem) {
			super(elem);
		}

		protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
			SizeRequirements sup = super.calculateMinorAxisRequirements(axis, r);
			sup.minimum = 1;

			return sup;
		}
	}

	/**
	 * View Factory.
	 */
	private static class MyViewFactory implements ViewFactory {
		private final ViewFactory parent;

		/**
		 * Constructor.
		 *
		 * @param parent ViewFactory Object.
		 */
		public MyViewFactory(ViewFactory parent) {
			this.parent = parent;
		}

		/**
		 * Creates a Text Element View.
		 *
		 * @param elem Element Object.
		 * @return View Object.
		 */
		public View create(javax.swing.text.Element elem) {
			AttributeSet attr = elem.getAttributes();
			Object name = attr.getAttribute(StyleConstants.NameAttribute);

			if ((name == HTML.Tag.P) || (name == HTML.Tag.IMPLIED)) {
				return new WordSplittingParagraphView(elem);
			}

			return parent.create(elem);
		}
	}
}
