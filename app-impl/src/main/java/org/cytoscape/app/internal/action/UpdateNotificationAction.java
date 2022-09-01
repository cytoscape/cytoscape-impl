package org.cytoscape.app.internal.action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.app.internal.ui.AppManagerMediator;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2021 The Cytoscape Consortium
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
public class UpdateNotificationAction extends AbstractCyAction {
	final CyServiceRegistrar serviceRegistrar;
	private final BadgeIcon icon;

	private final DebounceTimer debounceTimer = new DebounceTimer(2000);

	private final UpdateManager updateManager;
	private final AppManagerMediator appManagerMediator;
	private static final String APP_MANAGER_DIR = "appManager/appmanager_v2.html";
	private String url = null;
	private final CytoPanel cytoPanelWest;


	public UpdateNotificationAction(
			AppManager appManager,
			UpdateManager updateManager,
			AppManagerMediator appManagerMediator,
			CyServiceRegistrar serviceRegistrar
	) {
		super("App Updates");
		this.updateManager = updateManager;
		this.appManagerMediator = appManagerMediator;
		this.serviceRegistrar = serviceRegistrar;

		icon = new BadgeIcon(serviceRegistrar.getService(IconManager.class));
		CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);
		cytoPanelWest = swingApplication.getCytoPanel(CytoPanelName.WEST);

		putValue(LARGE_ICON_KEY, icon);
		putValue(SHORT_DESCRIPTION, "App Updates");
		setIsInMenuBar(false);
		setIsInToolBar(true);
		setToolbarGravity(Float.MAX_VALUE);

		appManager.addAppListener(evt -> updateEnableState(true));
		updateManager.addUpdatesChangedListener(evt -> updateEnableState(false));
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		StringBuilder contentBuilder = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new InputStreamReader(UpdateNotificationAction.class.getClassLoader().getResourceAsStream("/appmanager_v2.html"), Charset.forName("UTF-8").newDecoder()));
		    String str;
		    while ((str = in.readLine()) != null) {
		        contentBuilder.append(str);
		    }
		    in.close();
		} catch (IOException e) {
		}
		contentBuilder.append("<script type=\"text/javascript\">\n");
		contentBuilder.append("window.addEventListener('load', function() {\n");
		contentBuilder.append("    if(window.navigator.userAgent.includes('CyBrowser')){\n");
		contentBuilder.append("        setTimeout(function(){\n");
		contentBuilder.append("            getInstalledAppsCyB();\n");
		contentBuilder.append("        }, 000);\n");
		contentBuilder.append("        setTimeout(function(){\n");
		contentBuilder.append("            getDisabledAppsCyB(); //a delay between CyBrowser calls is necessary\n");
		contentBuilder.append("        }, 100);\n");
		contentBuilder.append("        setTimeout(function(){\n");
		contentBuilder.append("            getUpdatesAppsCyB(); //a delay between CyBrowser calls is necessary\n");
		contentBuilder.append("        }, 200);\n");
		contentBuilder.append("    } else {\n");
		contentBuilder.append("        alert(\"Sorry, this page only runs in CyBrowser.\");\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("});\n");
		contentBuilder.append("const coreApps = [\"BioPAX Reader\",  \"Biomart Web Service Client\", \"CX Support\",\n");
		contentBuilder.append("                  \"Core Apps\", \"CyNDEx-2\", \"CyCL\", \"Diffusion\", \"FileTransfer\",\n");
		contentBuilder.append("                  \"ID Mapper\", \"JSON Support\", \"Network Merge\", \"NetworkAnalyzer\",\n");
		contentBuilder.append("                  \"OpenCL Prefuse Layout\", \"PSI-MI Reader\", \"PSICQUIC Web Service Client\",\n");
		contentBuilder.append("                  \"SBML Reader\", \"aMatReader\", \"copycatLayout\", \"cyBrowser\",\n");
		contentBuilder.append("                  \"cyChart\", \"cyREST\"]\n");
		contentBuilder.append("function getInstalledAppsCyB() {\n");
		contentBuilder.append("    cybrowser.executeCyCommandWithResults('apps list installed', 'renderInstalledApps' );\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function getDisabledAppsCyB() {\n");
		contentBuilder.append("    cybrowser.executeCyCommandWithResults('apps list disabled', 'renderDisabledApps' );\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function getUpdatesAppsCyB() {\n");
		contentBuilder.append("    cybrowser.executeCyCommandWithResults('apps list updates', 'renderUpdatesApps' );\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function renderInstalledApps(res) {\n");
		contentBuilder.append("    // alert(res);\n");
		contentBuilder.append("        array = JSON.parse(res);\n");
		contentBuilder.append("        array = array.sort(function(a,b){return a.appName.localeCompare(b.appName)});\n");
		contentBuilder.append("        array.forEach(app => {\n");
		contentBuilder.append("            var aname=app['appName'];\n");
		contentBuilder.append("            if (typeof aname == 'undefined') { aname = \"\";} //resolve null\n");
		contentBuilder.append("            aname = aname.replace(/\"/g,\"\");\n");
		contentBuilder.append("            var anamevar = aname.replace(/\\W/g,\"\");\n");
		contentBuilder.append("            var aver=app['version'];\n");
		contentBuilder.append("            var astat=app['status'];\n");
		contentBuilder.append("            if (aname.length > 0 && !coreApps.includes(aname)){\n");
		contentBuilder.append("                arow = '<tr><td style=\" background-color: #EEEEEE; width:25px;height:25px; cursor:pointer;\"><img src=\"https://raw.githubusercontent.com/cytoscape/cytoscape-impl/feature/app-manager/app-impl/src/main/resources/img/trash.png\" height=\"18px\" style=\"float:left;margin:0px 0 2px 3px;\" onclick=\"uninstallAndRemove(&quot;'+aname+'&quot;)\" title=\"Uninstall app\"></td>';\n");
		contentBuilder.append("                arow += '<td style=\" background-color: #EEEEEE; width:25px;height:25px; cursor:pointer;\"><input type=\"checkbox\" id=\"'+anamevar+'\" name=\"enablecheck\" tabindex=\"0\"  checked ' +\n");
		contentBuilder.append("                    'onchange=\"toggleStatus(this, &quot;'+aname+'&quot;);\" title=\"Toggle enabled status\"></td>';\n");
		contentBuilder.append("                arow += '<td style=\" background-color: #EEEEEE; width:25px;height:25px;\"><img src=\"https://raw.githubusercontent.com/cytoscape/cytoscape-impl/feature/app-manager/app-impl/src/main/resources/img/update1.png\" height=\"18px\" style=\"float:left;margin:0px 0 0 3px;\" title=\"No update available\"</td>';\n");
		contentBuilder.append("                arow += '<td>&nbsp;&nbsp;<a onClick=\"openAppStore(&quot;'+anamevar+'&quot;);\" class=\"app\" title=\"Visit App Store page\">'+aname+'</a> (v'+aver+') </td></tr>';\n");
		contentBuilder.append("                document.getElementById('enabledApps').innerHTML += arow;\n");
		contentBuilder.append("            }\n");
		contentBuilder.append("        });\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function renderDisabledApps(res) {\n");
		contentBuilder.append("    // alert(res);\n");
		contentBuilder.append("        array = JSON.parse(res);\n");
		contentBuilder.append("        array = array.sort(function(a,b){return a.appName.localeCompare(b.appName)});\n");
		contentBuilder.append("        array.forEach(app => {\n");
		contentBuilder.append("            var aname=app['appName'];\n");
		contentBuilder.append("            if (typeof aname == 'undefined') { aname = \"\";} //resolve null\n");
		contentBuilder.append("            aname = aname.replace(/\"/g,\"\");\n");
		contentBuilder.append("            var anamevar = aname.replace(/\\W/g,\"\");\n");
		contentBuilder.append("            var aver=app['version'];\n");
		contentBuilder.append("            var astat=app['status'];\n");
		contentBuilder.append("            if (aname.length > 0 && !coreApps.includes(aname)){\n");
		contentBuilder.append("                arow = '<tr><td style=\"background-color: #EEEEEE; width:25px;height:25px; cursor:pointer;\"><img src=\"https://raw.githubusercontent.com/cytoscape/cytoscape-impl/feature/app-manager/app-impl/src/main/resources/img/trash.png\" height=\"18px\" style=\"float:left;margin:0px 0 2px 3px;\" onclick=\"uninstallAndRemove(&quot;'+aname+'&quot;)\" title=\"Uninstall app\"></td>';\n");
		contentBuilder.append("                arow += '<td style=\"background-color: #EEEEEE; width:25px;height:25px; cursor:pointer;\"><input type=\"checkbox\" id=\"'+anamevar+'\" tabindex=\"0\" ' +\n");
		contentBuilder.append("                    'onchange=\"toggleStatus(this, &quot;'+aname+'&quot;);\" title=\"Toggle enabled status\"></td>';\n");
		contentBuilder.append("                arow += '<td style=\" background-color: #EEEEEE; width:25px;height:25px;\"><img src=\"https://raw.githubusercontent.com/cytoscape/cytoscape-impl/feature/app-manager/app-impl/src/main/resources/img/update1.png\" height=\"18px\" style=\"float:left;margin:0px 0 0 3px;\" title=\"No update available\"></td>';\n");
		contentBuilder.append("                arow += '<td>&nbsp;&nbsp;<a onClick=\"openAppStore(&quot;'+anamevar+'&quot;);\" class=\"app\" title=\"Visit App Store page\">'+aname+'</a> (v'+aver+') </td></tr>';\n");
		contentBuilder.append("                document.getElementById('disabledApps').innerHTML += arow;\n");
		contentBuilder.append("            }\n");
		contentBuilder.append("        });\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function toggleStatus(checkbox, app) {\n");
		contentBuilder.append("    if(checkbox.checked == true){\n");
		contentBuilder.append("        enableAppCyB(app);\n");
		contentBuilder.append("    }else{\n");
		contentBuilder.append("        disableAppCyB(app);\n");
		contentBuilder.append("   }\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function uninstallAndRemove(app) {\n");
		contentBuilder.append("    uninstallAppCyB(app);\n");
		contentBuilder.append("    // and remove from table\n");
		contentBuilder.append("    var table = document.getElementById(\"enabledApps\");\n");
		contentBuilder.append("    for (var i = 1, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        aname = getAnameByRow(row);\n");
		contentBuilder.append("        if (aname ==  app) {\n");
		contentBuilder.append("            row.remove();\n");
		contentBuilder.append("        }\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    var table = document.getElementById(\"disabledApps\");\n");
		contentBuilder.append("    for (var i = 0, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        aname = getAnameByRow(row);\n");
		contentBuilder.append("        if (aname ==  app) {\n");
		contentBuilder.append("            row.remove();\n");
		contentBuilder.append("        }\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function getAnameByRow(row){\n");
		contentBuilder.append("    var aname = row.cells[3].textContent.replace(/\\(.*\\)/g,\"\").trim();\n");
		contentBuilder.append("    return (aname);\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function getRowByAname(app){\n");
		contentBuilder.append("    var table = document.getElementById(\"enabledApps\");\n");
		contentBuilder.append("    for (var i = 1, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        aname = getAnameByRow(row);\n");
		contentBuilder.append("        if (aname ==  app) {\n");
		contentBuilder.append("            return(row);\n");
		contentBuilder.append("        }\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    var table = document.getElementById(\"disabledApps\");\n");
		contentBuilder.append("    for (var i = 0, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        aname = getAnameByRow(row);\n");
		contentBuilder.append("        if (aname ==  app) {\n");
		contentBuilder.append("            return(row);\n");
		contentBuilder.append("        }\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    var aname = row.cells[3].textContent.replace(/\\(.*\\)/g,\"\").trim();\n");
		contentBuilder.append("    return (row);\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function enableAllApps() {\n");
		contentBuilder.append("    var table = document.getElementById(\"enabledApps\");\n");
		contentBuilder.append("    for (var i = 1, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        var aname = getAnameByRow(row);\n");
		contentBuilder.append("        var anamevar = aname.replace(/\\W/g,\"\");\n");
		contentBuilder.append("        if (!document.getElementById(anamevar).checked){\n");
		contentBuilder.append("            document.getElementById(anamevar).checked = true;\n");
		contentBuilder.append("            enableAppCyB(aname);\n");
		contentBuilder.append("        }\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    var table = document.getElementById(\"disabledApps\");\n");
		contentBuilder.append("    for (var i = 0, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        var aname = getAnameByRow(row);\n");
		contentBuilder.append("        var anamevar = aname.replace(/\\W/g,\"\");\n");
		contentBuilder.append("        if (!document.getElementById(anamevar).checked){\n");
		contentBuilder.append("            document.getElementById(anamevar).checked = true;\n");
		contentBuilder.append("            enableAppCyB(aname);\n");
		contentBuilder.append("        }\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function disableAllApps() {\n");
		contentBuilder.append("    var table = document.getElementById(\"enabledApps\");\n");
		contentBuilder.append("    for (var i = 1, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        var aname = getAnameByRow(row);\n");
		contentBuilder.append("        var anamevar = aname.replace(/\\W/g,\"\");\n");
		contentBuilder.append("        if (document.getElementById(anamevar).checked){\n");
		contentBuilder.append("            document.getElementById(anamevar).checked = false;\n");
		contentBuilder.append("            disableAppCyB(aname);\n");
		contentBuilder.append("        }\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    var table = document.getElementById(\"disabledApps\");\n");
		contentBuilder.append("    for (var i = 0, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        var aname = getAnameByRow(row);\n");
		contentBuilder.append("        var anamevar = aname.replace(/\\W/g,\"\");\n");
		contentBuilder.append("        if (document.getElementById(anamevar).checked){\n");
		contentBuilder.append("            document.getElementById(anamevar).checked = false;\n");
		contentBuilder.append("            disableAppCyB(aname);\n");
		contentBuilder.append("        }\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function uninstallAllApps() {\n");
		contentBuilder.append("    var table = document.getElementById(\"enabledApps\");\n");
		contentBuilder.append("    for (var i = 1, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        var aname = getAnameByRow(row);\n");
		contentBuilder.append("        uninstallAppCyB(aname);\n");
		contentBuilder.append("        row.remove();\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    var table = document.getElementById(\"disabledApps\");\n");
		contentBuilder.append("    for (var i = 0, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        var aname = getAnameByRow(row);\n");
		contentBuilder.append("        uninstallAppCyB(aname);\n");
		contentBuilder.append("        row.remove();\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function getAppUpdatesCyB() {\n");
		contentBuilder.append("    cybrowser.executeCyCommand('apps list updates');\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function disableAppCyB(app) {\n");
		contentBuilder.append("    cybrowser.executeCyCommand('apps disable app=' + \"'\" + app + \"'\");\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function enableAppCyB(app) {\n");
		contentBuilder.append("    cybrowser.executeCyCommand('apps enable app=' + \"'\" + app + \"'\");\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function uninstallAppCyB(app) {\n");
		contentBuilder.append("    cybrowser.executeCyCommand('apps uninstall app=' + \"'\" + app + \"'\");\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function toggleMenu(){\n");
		contentBuilder.append("    var btn = document.getElementById(\"ddc\");\n");
		contentBuilder.append("    if (btn.style.display == \"\"){\n");
		contentBuilder.append("        btn.style.display = \"block\";\n");
		contentBuilder.append("    } else {\n");
		contentBuilder.append("        btn.style.display = \"\";\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function openAppStore(app=null){\n");
		contentBuilder.append("    appUrl = \"https://apps.cytoscape.org/\"\n");
		contentBuilder.append("    if (app != null){\n");
		contentBuilder.append("        appUrl += \"apps/\"+app\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    cybrowser.executeCyCommand('cybrowser dialog url=\"'+appUrl+'\" id=\"AppStore\" title=\"App Store\" ');\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function searchAppStore(){\n");
		contentBuilder.append("    var query = document.getElementById(\"search\").value\n");
		contentBuilder.append("    qUrl = \"https://apps.cytoscape.org/\"\n");
		contentBuilder.append("    if (query != null){\n");
		contentBuilder.append("        qUrl += \"search?q=\"+query\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    cybrowser.executeCyCommand('cybrowser dialog url=\"'+qUrl+'\" id=\"AppStore\" title=\"App Store\" ');\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function updateAppAndIcon(app) {\n");
		contentBuilder.append("    var cmd = 'apps update app=\"'+app+'\"';\n");
		contentBuilder.append("    cybrowser.executeCyCommand(cmd);\n");
		contentBuilder.append("    //and swap update1.png icon back in for row\n");
		contentBuilder.append("    var row = getRowByAname(app);\n");
		contentBuilder.append("    updateAppRow(row);\n");
		contentBuilder.append("}\n");
		contentBuilder.append(" function updateAllApps() {\n");
		contentBuilder.append("    cybrowser.executeCyCommand('apps update app=\"all\"');\n");
		contentBuilder.append("    //and clear all green arrows\n");
		contentBuilder.append("    var table = document.getElementById(\"enabledApps\");\n");
		contentBuilder.append("    for (var i = 0, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        updateAppRow(row);\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("    var table = document.getElementById(\"disabledApps\");\n");
		contentBuilder.append("    for (var i = 0, row; row = table.rows[i]; i++) {\n");
		contentBuilder.append("        updateAppRow(row);\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function updateAppRow(row) {\n");
		contentBuilder.append("    row.cells[2].children[0].src = \"https://raw.githubusercontent.com/cytoscape/cytoscape-impl/feature/app-manager/app-impl/src/main/resources/img/update1.png\";\n");
		contentBuilder.append("    row.cells[2].children[0].title = \"No update available\";\n");
		contentBuilder.append("    row.cells[2].children[0].style.cursor = \"default\";\n");
		contentBuilder.append("    row.cells[2].children[0].removeAttribute('onclick');\n");
		contentBuilder.append("    var newversion = row.cells[2].children[0].getAttribute('newversion');\n");
		contentBuilder.append("    if (newversion != null){\n");
		contentBuilder.append("        newversion = \"(v\"+newversion+\")\";\n");
		contentBuilder.append("        appinfo = row.cells[3].textContent;\n");
		contentBuilder.append("        newappinfo = appinfo.replace(/\\(.*\\)/,newversion);\n");
		contentBuilder.append("        row.cells[3].textContent = newappinfo;\n");
		contentBuilder.append("    }\n");
		contentBuilder.append("}\n");
		contentBuilder.append("function renderUpdatesApps(res) {\n");
		contentBuilder.append("    // alert(res);\n");
		contentBuilder.append("    //res = '[{\"appName\": \"WikiPathways\",\"version\":\"3.3.7\",\"new version\":\"3.3.10\"}]'\n");
		contentBuilder.append("    res = res.replace(/},]/,\"}]\"); //bug: https://cytoscape.atlassian.net/browse/CYTOSCAPE-12992\n");
		contentBuilder.append("    array = JSON.parse(res);\n");
		contentBuilder.append("        array.forEach(app => {\n");
		contentBuilder.append("            var aname=app['appName'];\n");
		contentBuilder.append("            if (typeof aname == 'undefined') { aname = \"\";} //resolve null\n");
		contentBuilder.append("            aname = aname.replace(/\"/g,\"\");\n");
		contentBuilder.append("            var anamevar = aname.replace(/\\W/g,\"\");\n");
		contentBuilder.append("            var newversion = app['new version'];\n");
		contentBuilder.append("            var row = getRowByAname(aname);\n");
		contentBuilder.append("            row.cells[2].children[0].src = \"https://raw.githubusercontent.com/cytoscape/cytoscape-impl/feature/app-manager/app-impl/src/main/resources/img/update2.png\";\n");
		contentBuilder.append("            row.cells[2].children[0].title = \"Update app\";\n");
		contentBuilder.append("            row.cells[2].children[0].style.cursor = \"pointer\";\n");
		contentBuilder.append("            row.cells[2].children[0].setAttribute('onclick', 'updateAppAndIcon(\"'+aname+'\")');\n");
		contentBuilder.append("            row.cells[2].children[0].setAttribute('newversion', newversion);\n");
		contentBuilder.append("        });\n");
		contentBuilder.append("}\n");
		contentBuilder.append("    </script>\n");
		contentBuilder.append("    </body>\n");
		contentBuilder.append("    </html>\n");
		String content = contentBuilder.toString();

			CommandExecutorTaskFactory commandTF = serviceRegistrar.getService(CommandExecutorTaskFactory.class);
			TaskManager<?,?> taskManager = serviceRegistrar.getService(TaskManager.class);
			Map<String, Object> args = new HashMap<>();
			//args.put("url",url);
			args.put("text", content);
			args.put("id","App Manager");
			args.put("title","App Manager");
			args.put("panel","WEST");
			TaskIterator ti = commandTF.createTaskIterator("cybrowser","show",args, null);
			taskManager.execute(ti);
	}

	@Override
	public void updateEnableState() {
		setEnabled(false); // to force the component to repaint later if 'count' changes

		final int count = updateManager.getUpdateCount();
		final String text;

		if (count > 0)
			text = count + " update" + (count > 1 ? "s" : "") + " available!";
		else
			text = "All your apps are up-to-date.";

		putValue(LONG_DESCRIPTION, text);
		icon.setCount(count);
		setEnabled(count > 0); // this should force the UI to repaint because we disabled this action previously
	}

	public void updateEnableState(boolean checkForUpdates) {
		// Debounce the update events, because checkForUpdates() can be expensive!
		debounceTimer.debounce(() -> {
			if (checkForUpdates)
				updateManager.checkForUpdates();

			SwingUtilities.invokeLater(() -> updateEnableState());
		});
	}

	private static class BadgeIcon extends TextIcon {

		private static float ICON_FONT_SIZE = 24f;
		private static int ICON_SIZE = 32;
		private static int BADGE_BORDER_WIDTH = 1;
		private static Color BADGE_COLOR = Color.RED;
		private static Color BADGE_BORDER_COLOR = Color.WHITE;
		private static Color BADGE_TEXT_COLOR = Color.WHITE;
		private static Color ICON_COLOR = UIManager.getColor("CyColor.complement(-2)");

		private int count;
		private final IconManager iconManager;

		public BadgeIcon(IconManager iconManager) {
			super(IconManager.ICON_BELL, iconManager.getIconFont(ICON_FONT_SIZE), ICON_COLOR, ICON_SIZE, ICON_SIZE);
			this.iconManager = iconManager;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);

			if (!c.isEnabled() || count <= 0) // Only draw a badge if there are notifications!
				return;

			Graphics2D g2d = (Graphics2D) g.create();

			RenderingHints hints = new RenderingHints(null);
			hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHints(hints);

			int w = getIconWidth();
			int h = getIconHeight();

			// Position the badge in the top-right quadrant of the icon
			float d = Math.max(w, h) / 1.75f; // diameter
			float di = d - 2 * BADGE_BORDER_WIDTH; // diameter of the internal circle (i.e. circle - border)
			float bx = x + w - d; // x of badge's upper left corner
			float by = y; // y of badge's upper left corner

			// Draw badge circle
			g2d.setColor(BADGE_BORDER_COLOR);
			g2d.fillOval(Math.round(bx), Math.round(by), Math.round(d), Math.round(d));
			g2d.setColor(BADGE_COLOR);
			g2d.fillOval(Math.round(bx + BADGE_BORDER_WIDTH), Math.round(by + BADGE_BORDER_WIDTH), Math.round(di), Math.round(di));

			// Draw badge count text inside the circle.
			String text = count > 99 ? IconManager.ICON_ELLIPSIS_H : "" + count; // just draw ELLIPSIS char if more than 2 digits

			float hr = (float) Math.sqrt((di * di) / 2.0f); // height of square inside internal circle (Pythagoras)
			float th = hr; // text height
			float tw = 0; // text width
			Font textFont = count > 99 ? iconManager.getIconFont(h)
					: UIManager.getFont("Label.font").deriveFont(Font.BOLD);
			textFont = getFont(textFont, th, g2d);

			g2d.setFont(textFont);
			g2d.setColor(BADGE_TEXT_COLOR);

			FontMetrics fm = g2d.getFontMetrics();
			th = fm.getHeight();
			tw = fm.stringWidth(text);

			float tx = bx + (d - hr) / 2.0f;
			tx += (hr - tw) / 2.0f;

			float ty = by + (d - hr) / 2.0f;
			ty += ((hr - th) / 2.0f) + fm.getAscent();

			g2d.drawString(text, tx, ty);

			g2d.dispose();
		}

		/**
		 * Sets the count value to display.
		 */
		public void setCount(int count) {
			this.count = count;
		}

		private static Font getFont(Font f, float height, Graphics g) {
			float size = height;
			Boolean up = null;

			while (true) {
				Font font = f.deriveFont(size);
				int testHeight = g.getFontMetrics(font).getHeight();

				if (testHeight < height && up != Boolean.FALSE) {
					size++;
					up = Boolean.TRUE;
				} else if (testHeight > height && up != Boolean.TRUE) {
					size--;
					up = Boolean.FALSE;
				} else {
					return font;
				}
			}
		}
	}
}
