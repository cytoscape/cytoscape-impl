package org.cytoscape.biopax.internal.view;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.cytoscape.biopax.internal.action.LaunchExternalBrowser;


/**
 * Displays information "About this App...".
 *
 * @author Ethan Cerami
 */
public class AboutPanel extends JPanel {
	/**
	 * Constructor.
	 *
	 * @param title        App Title.
	 * @param majorVersion App Major Version.
	 * @param minorVersion App Minor Version.
	 */
	public AboutPanel(String title, int majorVersion, int minorVersion, LaunchExternalBrowser browser) {
		this.setLayout(new BorderLayout());

		JTextPane textPane = new JTextPane();
        textPane.setBorder(new EmptyBorder(5,5,5,5));
        textPane.setEditable(false);
		textPane.setContentType("text/html");
        textPane.addHyperlinkListener(browser);

        StringBuffer temp = new StringBuffer();
		temp.append("<HTML><BODY>");
        temp.append("<h2>");
        temp.append(title + ", Version:  " + majorVersion + ". " + minorVersion);
        temp.append("</h2>");
        temp.append("<P>");

		temp.append("<BR><BR>App released by:  Sander Group, "
		            + "<A class=\"link\" HREF=\"http://www.cbio.mskcc.org/\">"
		            + "Computational Biology Center</A>, "
		            + "<A class=\"link\" HREF=\"http://www.mskcc.org\">Memorial Sloan-Kettering "
		            + "Cancer Center</A>.");
		temp.append("<BR><BR>For any questions concerning this app, please "
		            + "contact:<BR><BR><UL><LI> - Gary Bader:  baderg AT mskcc.org"
		            + "<LI> - Ethan Cerami:  cerami AT cbio.mskcc.org"
		            + "<LI> - Benjamin Gross:  grossb AT cbio.mskcc.org"
		            + "<BR><BR>This software is made available under the "
		            + "<A class=\"link\" HREF=\"http://www.gnu.org/licenses/lgpl.html\">LGPL "
		            + "(Lesser General Public License)</A>.");
		temp.append("</BODY></HTML>");
		textPane.setText(temp.toString());
		textPane.setText(temp.toString());

		JScrollPane scrollPane = new JScrollPane(textPane);
		this.add(scrollPane, BorderLayout.CENTER);
	}
}
