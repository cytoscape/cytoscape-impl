# App Manager Rework in Cytoscape 3.10
-----------------
In Cytoscape 3.10, the App Manager GUI is redesigned, and it is replaced by a single web page (Vanilla JavaScript, HTML, CSS). We use Cytoscape built-in browser - cyBrowser (https://github.com/cytoscape/cyBrowser) to render the page inside the Cytoscape.

## What has changed
Cytoscape 3.9 and below versions open port 2607 in the local, and Cytoscape use this port to communicate with App Store (https://apps.cytoscape.org/). In Cytoscape 3.10, the port 2607 is closed, and cyREST port 1234 is used to communicate with App Store. The App Store server side is updated, and it supports  3.10 and below versions. Users use Cytoscape 3.9 and below versions are not affected.

## Manage Download sites
Since the old App Manager GUI is completely deleted. Users cannot use GUI to manage download sites. If users want to use alternative app store site (ex. https://apps-stage.cytoscape.org/), they can modify cyProperties to achieve this goal. In Cytoscape, go to ```Edit``` => ```Preferences``` => ```Properties```, and ```appStoreDownloadSiteUrl1``` value can be modified to the alternative app store site URL.

# How to Sync with js4cytoscape and Update App Manager Web Page
-----------------
In Cytoscape 3.10, the entire App Manager web pgae is embedded in Cytosacpe Java code. If you want to update the App Manager web page, you need to update two repositories simultaneously for better maintanice. The two repositories are js4yctosacpe repo (https://github.com/cytoscape/js4cytoscape) and cytoscape-impl repo (https://github.com/cytoscape/cytoscape-impl). Please follow steps below to finish the update.

1. The source file of App Manager web page is located in the js4cytoscape mono repo. It can be found under the ```develop``` branch (https://github.com/cytoscape/js4cytoscape/tree/develop/packages/js4cytoscape). The ```appmanager.html``` is the App Manager web page.

2. Before you push any changes to the Github, test it in the local first. You can modify your local appmanager.html file and save it. Then open Cytoscape and run ```cybrowser show panel=WEST url="Your App Manager html file location"``` in Cytoscape command line. The App Manager web page should be opened, and changes should also be reflected. If everything works fine, then you can push you code to js4cytoscape repo.
 
3. The Github action is configured. When you push changes to update ```appmanager.html``` (https://github.com/cytoscape/js4cytoscape/blob/develop/packages/js4cytoscape/appmanager.html), this update will also be pushed to the ```develop``` branch under ```cytoscape/cytoscape-impl``` repo. The file is located here (https://github.com/cytoscape/cytoscape-impl/blob/develop/app-impl/src/main/resources/AppManager/AppManager.html), and this is the page we are rendering in the Cytoscape.

* If the Github action is not working or you do not have permission to push, you can copy your updated App Manager web page to ```app-impl/src/main/resources/AppManager/AppManager.html``` and overwrite the AppManager.html file.

4. If you are only modifing HTML and CSS (anything above the line ```<!-- Additional JavaScript -->``` in AppManager.html), then you are done. You can rebuild ```app-impl```, and restart the Cytoscape, changes should be reflected.

5. If you are modifing JavaScript functions (anything below the line ```<!-- Additional JavaScript -->``` in AppManager.html), things are more complicated. Since the App Manager page JavaScript code is embedded in Java, you have to manually modify some Java code. There are two places you need to synchronize line by line. <br /><br />
The first one is ```AppManagerTask.java```, from line ```contentBuilder.append("<script type=\"text/javascript\">\n");``` to line 		```contentBuilder.append("</html>\n");``` , these lines are JavaScript functions of App Manager. <br /><br />
The second one is ```UpdateNotificationAction.java```, from line ```contentBuilder.append("<script type=\"text/javascript\">\n");``` to line 		```contentBuilder.append("</html>\n");``` , these lines are JavaScript functions of App Manager. <br /><br />
Search the JavaScript functions you are updating in AppManager.html, and then modify the related lines. If new lines are required, just add ```contentBuilder.append("")``` in new lines, and put new JavaScript code in quotes.

* Do not forget to escape quotes in Java.<br />
* The easiest way to do this is checking commit history in js4cytoscape repo and comparing.<br />
6. Now you can rebuild ```app-impl```, and restart the Cytoscape, changes should be reflected.
