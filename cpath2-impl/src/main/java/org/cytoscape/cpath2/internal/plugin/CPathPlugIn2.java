package org.cytoscape.cpath2.internal.plugin;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2007 - 2013
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

// imports

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.http.HTTPServer;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;

/**
 * The cPath plugin class.  It gets called by Cytoscape's plugin manager
 * to install inself.  The main job of this guy is to instantiate our http server.
 *
 * @author Benjamin Gross.
 */
public class CPathPlugIn2 {

	/**
     * Constructor.
     */
    public CPathPlugIn2(CPath2Factory factory) throws IOException {
    	String debugProperty = System.getProperty("DEBUG");
        Boolean debug = (debugProperty != null && debugProperty.length() > 0) &&
                new Boolean(debugProperty.toLowerCase());
        initProperties();

        // create our http server and start its thread
        new HTTPServer(HTTPServer.DEFAULT_PORT, factory.createMapCPathToCytoscape(), debug).start();
    }

    private void initProperties() throws IOException {
    	// TODO: Port this
//        PluginProperties pluginProperties = new PluginProperties(this);
    	Properties pluginProperties = new Properties();
        CPathProperties cpathProperties = CPathProperties.getInstance();
        cpathProperties.initProperties(pluginProperties);
    }

    public static JScrollPane createConfigPanel() {
        JPanel configPanel = new JPanel();
        configPanel.setBorder(new TitledBorder("Retrieval Options"));
        configPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        final JRadioButton button1 = new JRadioButton("Full Model");

        JTextArea textArea1 = new JTextArea();
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);
        textArea1.setEditable(false);
        textArea1.setOpaque(false);
        Font font = textArea1.getFont();
        Font smallerFont = new Font(font.getFamily(), font.getStyle(), font.getSize() - 2);
        textArea1.setFont(smallerFont);
        textArea1.setText("Retrieve the full model, as stored in the original BioPAX "
                + "representation.  In this representation, nodes within a network can "
                + "refer to physical entities and interactions.");
        textArea1.setBorder(new EmptyBorder(5, 20, 0, 0));

        JTextArea textArea2 = new JTextArea(3, 20);
        textArea2.setLineWrap(true);
        textArea2.setWrapStyleWord(true);
        textArea2.setEditable(false);
        textArea2.setOpaque(false);
        textArea2.setFont(smallerFont);
        textArea2.setText("Retrieve a simplified binary network, as inferred from the original "
                + "BioPAX representation.  In this representation, nodes within a network refer "
                + "to physical entities only, and edges refer to inferred interactions.");
        textArea2.setBorder(new EmptyBorder(5, 20, 0, 0));


        final JRadioButton button2 = new JRadioButton("Simplified Binary Model");
        button2.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(button1);
        group.add(button2);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        c.gridx = 0;
        c.gridy = 0;
        configPanel.add(button2, c);

        c.gridy = 1;
        configPanel.add(textArea2, c);

        c.gridy = 2;
        configPanel.add(button1, c);

        c.gridy = 3;
        configPanel.add(textArea1, c);

        //  Add invisible filler to take up all remaining space
        c.gridy = 4;
        c.weighty = 1.0;
        JPanel panel = new JPanel();
        configPanel.add(panel, c);

        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CPathProperties config = CPathProperties.getInstance();
                config.setDownloadMode(CPathProperties.DOWNLOAD_FULL_BIOPAX);
            }
        });
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CPathProperties config = CPathProperties.getInstance();
                config.setDownloadMode(CPathProperties.DOWNLOAD_REDUCED_BINARY_SIF);
            }
        });
        JScrollPane scrollPane = new JScrollPane(configPanel);
        return scrollPane;
    }
}
