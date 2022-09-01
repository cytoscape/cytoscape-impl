package org.cytoscape.app.internal.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.nio.charset.Charset;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class AppManagerTask extends AbstractAppTask implements ObservableTask {
	final CyServiceRegistrar serviceRegistrar;
	private final CytoPanel cytoPanelWest;
	private static final String APP_MANAGER_DIR = "appManager/appmanager_v2.html";
	private String url = null;

	@Tunable (description="App name", context="nogui")
	public String app;

	@Tunable (description="Use CyBrowser if installed", context="nogui")
	public boolean useCybrowser = true;

	public AppManagerTask(final AppManager appManager, CyServiceRegistrar serviceRegistrar, CySwingApplication swingApplication) {
		super(appManager);
		cytoPanelWest = swingApplication.getCytoPanel(CytoPanelName.WEST);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		StringBuilder contentBuilder = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new InputStreamReader(AppManagerTask.class.getClassLoader().getResourceAsStream("/appmanager_v2.html"), Charset.forName("UTF-8").newDecoder()));
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

		//final CyApplicationConfiguration applicationCfg = serviceRegistrar.getService(CyApplicationConfiguration.class);
		//String APP_MANAGER = "file:///" + (applicationCfg.getConfigurationDirectoryLocation()).toString() + "/" + APP_MANAGER_DIR;
		//WebApp webApp = null;
		//if (app != null) {
		//	webApp = getWebApp(app);
		//	url = APP_MANAGER+"apps/"+app;
		//} else {
		//	url = APP_MANAGER;
		//}

		App cyBrowser = getApp("cybrowser");
		if (useCybrowser == true && cyBrowser != null && cyBrowser.getStatus() == App.AppStatus.INSTALLED) {
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
		} else {
			OpenBrowser openBrowser = serviceRegistrar.getService(OpenBrowser.class);
			openBrowser.openURL(url);
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return "{}";
			};
			return (R)res;
		} else if (type.equals(String.class)) {
			String res = "Opened url: "+url;
			return (R)res;
		}
		return null;
	}

}
