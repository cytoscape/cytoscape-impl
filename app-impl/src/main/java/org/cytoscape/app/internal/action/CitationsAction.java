package org.cytoscape.app.internal.action;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.work.TaskManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.util.swing.OpenBrowser;

import java.awt.event.ActionEvent;


import org.cytoscape.app.internal.ui.CitationsDialog;

public class CitationsAction extends AbstractCyAction {
  final WebQuerier webQuerier;
  final AppManager appMgr;
  final TaskManager taskMgr;
  final CySwingApplication swingApp;
  final OpenBrowser openBrowser;

  CitationsDialog dialog = null;

  public CitationsAction(WebQuerier webQuerier, AppManager appMgr, TaskManager taskMgr, CySwingApplication swingApp, OpenBrowser openBrowser) {
    super("Citations...");
    super.setPreferredMenu("Help");
    super.setMenuGravity(2.0f);

    this.webQuerier = webQuerier;
    this.appMgr = appMgr;
    this.taskMgr = taskMgr;
    this.swingApp = swingApp;
    this.openBrowser = openBrowser;
  } 

  public void actionPerformed(ActionEvent e) {
    if (dialog == null)
      dialog = new CitationsDialog(webQuerier, appMgr, taskMgr, swingApp.getJFrame(), openBrowser);
    dialog.show();
  }
}