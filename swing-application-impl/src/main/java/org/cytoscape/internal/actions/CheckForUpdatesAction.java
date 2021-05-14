package org.cytoscape.internal.actions;

import org.cytoscape.application.CyVersion;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.view.updater.UpdatesDialog;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.internal.task.GetLatestVersionTask;
import org.cytoscape.work.*;
import org.cytoscape.work.swing.DialogTaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Properties;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class CheckForUpdatesAction extends AbstractCyAction implements CyStartListener {

    private static final String HIDE_UPDATES_PROP = "hideUpdatesNotification";
    private static final String TEMP_HIDE_PROP = "tempHideWelcomeScreen";
    private static final String NAME = "Check for Updates";
    private static final String PARENT_NAME = "Help";

    private final String thisVersion;
    private String latestVersion;

    private final CyServiceRegistrar serviceRegistrar;

    public CheckForUpdatesAction(final CyServiceRegistrar serviceRegistrar) {
        super(NAME);
        setPreferredMenu(PARENT_NAME);
        setMenuGravity(9.999f);

        this.serviceRegistrar = serviceRegistrar;
        thisVersion = serviceRegistrar.getService(CyVersion.class).getVersion();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        SwingUtilities.invokeLater(() -> showDialog(false));
    }

    @Override
    public void handleEvent(CyStartEvent cyStartEvent) {
        final GetLatestVersionTask task = new GetLatestVersionTask();
        runTask(task, new TaskObserver() {
            @Override
            public void taskFinished(ObservableTask task) {
            }

            @Override
            public void allFinished(FinishStatus finishStatus) {
                if (finishStatus.getType() == FinishStatus.Type.SUCCEEDED) {
                    // Always check the version when stating Cytoscape, in order to log statistics
                    latestVersion = task.getLatestVersion();

                    // Displays the dialog after startup based on whether the specified property has been set.
                    boolean hide = false;
                    final Properties props = getCyProperties();

                    // Hide if this version up to date or the current version is a pre-release (snapshot, beta, etc).
                    // We don't want to bother the user unless there is a new version to download!
                    if (latestVersion == null || latestVersion.isEmpty() || thisVersion.equals(latestVersion)
                            || isPreRelease(thisVersion))
                        hide = true;

                    if (!hide) {
                        final String hideVersion = props.getProperty(HIDE_UPDATES_PROP, "").trim();
                        // If set to "true", always hide, no matter the new version
                        hide = hideVersion.equalsIgnoreCase("true") || hideVersion.equals(latestVersion);
                    }

                    if (!hide) {
                        final String tempHideString = props.getProperty(TEMP_HIDE_PROP);
                        hide = parseBoolean(tempHideString);
                    }

                    // Remove this property regardless!
                    props.remove(TEMP_HIDE_PROP);

                    if (!hide)
                        SwingUtilities.invokeLater(() -> showDialog(true));
                }
            }
        });
    }

    private void runTask(Task task, TaskObserver observer) {
        TaskIterator iterator = new TaskIterator(task);
        serviceRegistrar.getService(DialogTaskManager.class).execute(iterator, observer);
    }

    private static boolean isPreRelease(final String version) {
        return version.contains("-");
    }

    private void showDialog(boolean hideOptionVisible) {
        JFrame owner = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
        UpdatesDialog dialog = new UpdatesDialog(owner, thisVersion, latestVersion, hideOptionVisible, serviceRegistrar);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        if (hideOptionVisible) {
            // Update property
            if (dialog.getHideStatus())
                getCyProperties().setProperty(HIDE_UPDATES_PROP, latestVersion);
            else
                getCyProperties().remove(HIDE_UPDATES_PROP);
        }
    }

    private Properties getCyProperties() {
        return (Properties) serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")
                .getProperties();
    }

    private static boolean parseBoolean(String hideString) {
        boolean lhide = false;

        if (hideString == null) {
            lhide = false;
        } else {
            try {
                // might make it true!
                lhide = Boolean.parseBoolean(hideString);
            } catch (Exception ex) {
                lhide = false;
            }
        }

        return lhide;
    }
}
