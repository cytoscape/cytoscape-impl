package org.cytoscape.welcome.internal.panel;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
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

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.border.LineBorder;

import org.cytoscape.application.CyVersion;
import org.cytoscape.welcome.internal.WelcomeScreenDialog;

public final class StatusPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = 54718654342142203L;
	
	private static final String UP_TO_DATE_ICON_LOCATION = "images/Icons/accept.png";
	private static final String NEW_VER_AVAILABLE_ICON_LOCATION = "images/Icons/error.png";
    private static final String NEWS_URL = "http://chianti.ucsd.edu/cytoscape-news/news.html";

	private final CyVersion cyVersion;
	
	private final Icon upToDateIcon;
	private final Icon newVersionAvailableIcon;

	public StatusPanel(final CyVersion cyVersion) {
		this.cyVersion = cyVersion;

		upToDateIcon= new ImageIcon(WelcomeScreenDialog.class.getClassLoader().getResource(UP_TO_DATE_ICON_LOCATION));
		newVersionAvailableIcon= new ImageIcon(WelcomeScreenDialog.class.getClassLoader().getResource(NEW_VER_AVAILABLE_ICON_LOCATION));
		initComponents();
	}

	private void initComponents() {
		final String versionStr = cyVersion.getVersion();

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final JLabel status = new JLabel();
		status.setOpaque(false);
		status.setFont(REGULAR_FONT);
		status.setForeground(REGULAR_FONT_COLOR);

        if(isUpToDate()) {
			status.setIcon(upToDateIcon);
			status.setText("Cytoscape " + versionStr + " is up to date.");
		} else {
			status.setIcon(newVersionAvailableIcon);
			status.setText("New version is available: " + versionStr);
		}
        this.add(status);

        final JLabel news = new JLabel();
        news.setOpaque(false);
        news.setFont(REGULAR_FONT);
        news.setForeground(REGULAR_FONT_COLOR);
        String newsContent = "";
        BufferedReader br = null;
        try {
            URL newsUrl = new URL(NEWS_URL);
            br = new BufferedReader( new InputStreamReader( newsUrl.openStream() ));
            String line = null;
            while( (line = br.readLine()) != null )
            {
                newsContent += line +"\n";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            newsContent = "<html>Cannot access news.<br>Your internet connection may be down.</html>";
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            if( br != null )
            {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        news.setText(newsContent);
        this.add(news);
	}
	
	private boolean isUpToDate() {
		// TODO: Implement this!
		return true;
	}
	
	private String getNewVersionNumber() {
		// TODO: implement this!
		return "3.1.0";
	}
	

}
