package org.cytoscape.cpath2.internal;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.Properties;

import org.cytoscape.cpath2.internal.view.Organism;

/**
 * Contains cPath Specific Properties
 *
 * @author Ethan Cerami.
 */
public class CPathProperties {
	/**
	 * Property:  CPath2 Read Location.
	 */
	public static final String CPATH_URL = new String("cpath2.server_url");

    /**
     * Property:  CPath2 Server Name.
     */
    public static final String CPATH_INSTANCE_BNAME = new String("cpath2.server_name");

    /**
     * Property:  CPath2 Server Blurb
     */
    public static final String CPATH_INSTANCE_BLURB = new String ("cpath2.server_blurb");

    /**
     * Property:  Icon Tool Tip
     */
    public static final String ICON_TOOL_TIP = new String ("cpath2.icon_tool_tip");

    /**
     * Property:  Icon File
     */
    public static final String ICON_FILE_NAME = new String ("cpath2.icon_file_name");

    /**
     * Download Networks in Full BioPAX Mode.
     */
    public final static int DOWNLOAD_FULL_BIOPAX = 1;

    /**
     * Download Networks in Binary SIF Reduced Mode
     */
    public final static int DOWNLOAD_REDUCED_BINARY_SIF = 2;


    private static CPathProperties cpathProperties;
    private static String cPathUrl;
    private static String serverName;
    private static String blurb;
    private static String iconToolTip;
    private static String iconFileName;
    private static ArrayList<Organism> organismList = new ArrayList<Organism>();
    private int downloadMode = DOWNLOAD_REDUCED_BINARY_SIF;

    /**
     * Gets singleton instance of cPath Properties.
     * @return CPathProperties class.
     */
    public static CPathProperties getInstance() {
        if (cpathProperties == null) {
               cpathProperties = new CPathProperties();
               cpathProperties.initProperties (new Properties());
        }
        return cpathProperties;
    }

    private CPathProperties () {
        //  no-op; private constructor;
    }

    public void initProperties (Properties pluginProperties) {
        cPathUrl = pluginProperties.getProperty(CPATH_URL);

        if (cPathUrl == null) {
            cPathUrl = "http://www.pathwaycommons.org/pc/webservice.do";
        }

        serverName = pluginProperties.getProperty(CPATH_INSTANCE_BNAME);
        if (serverName == null) {
            serverName = "Pathway Commons";
        }

        iconToolTip = pluginProperties.getProperty(ICON_TOOL_TIP);
        if (iconToolTip == null) {
            iconToolTip = "Retrieve Pathway Data from PathwayCommons.org";
        }

        iconFileName = pluginProperties.getProperty(ICON_FILE_NAME);
        if (iconFileName == null) {
            iconFileName = "pc.png";
        }

        blurb = pluginProperties.getProperty(CPATH_INSTANCE_BLURB);
        if (blurb == null) {
            blurb = "<span class='bold'>Pathway Commons</span> is a convenient point of access " +
                "to biological pathway " +
                "information collected from public pathway databases, which you can " +
                "browse or search. <BR><BR>Pathways include biochemical reactions, complex " +
                "assembly, transport and catalysis events, and physical interactions " +
                "involving proteins, DNA, RNA, small molecules and complexes.";
        }

          organismList.add(new Organism("Human", 9606));
          organismList.add(new Organism("Fruit Fly", 7227));
          organismList.add(new Organism("Mouse", 10090));
          organismList.add(new Organism("Rat", 10116));
          organismList.add(new Organism("C. elegans", 6239));
          organismList.add(new Organism("S. cerevisiae", 4932));
    }

    /**
	 * Gets URL for cPath Web Service API.
	 *
	 * @return cPath URL.
	 */
	public String getCPathUrl() {
        return cPathUrl;
	}

    /**
	 * Gets Name of cPath Instance.
	 *
	 * @return cPath URL.
	 */
	public String getCPathServerName() {
		return serverName;
	}

    /**
     * Gets the Web Services ID.
     * @return Web Service ID.
     */
    public String getWebServicesId() {
        String temp = serverName.toLowerCase();
        return temp.replaceAll(" ", "_");
    }

    /**
	 * Gets Text Blurb for cPath Instance
	 *
	 * @return cPath URL.
	 */
	public String getCPathBlurb() {
		return blurb;
	}

    /**
     * Gets the Icon Tool Tip.
     * @return Gets the Icon Tool Tip.
     */
    public String getIconToolTip() {
        return iconToolTip;
    }

    /**
     * Gets the Icon File Name.
     * @return Icon File Name.
     */
    public String getIconFileName() {
        return iconFileName;
    }

    /**
     * Gets Download Mode.
     * @return DOWNLOAD_FULL_BIOPAX or DOWNLOAD_REDUCED_BINARY_SIF.
     */
    public int getDownloadMode() {
        return downloadMode;
    }

    /**
     * Sets Download Mode.
     * @param downloadMode DOWNLOAD_FULL_BIOPAX or DOWNLOAD_REDUCED_BINARY_SIF.
     */
    public void setDownloadMode(int downloadMode) {
        this.downloadMode = downloadMode;
    }

    /**
     * Gets the Organism List.
     * @return ArrayList of <Organism> Objects.
     */
    public ArrayList<Organism> getOrganismList() {
        return organismList;
    }
}