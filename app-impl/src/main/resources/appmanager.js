<script type="text/javascript">
window.addEventListener('load', function() {
    if(window.navigator.userAgent.includes('CyBrowser')){
        setTimeout(function(){
            getInstalledAppsCyB();
        }, 000);
        setTimeout(function(){
            getDisabledAppsCyB(); //a delay between CyBrowser calls is necessary
        }, 100);
        setTimeout(function(){
            getUpdatesAppsCyB(); //a delay between CyBrowser calls is necessary
        }, 200);
    } else {
        alert("Sorry, this page only runs in CyBrowser.");
    }
});

// const coreApps = []
const coreApps = ["BioPAX Reader",  "Biomart Web Service Client", "CX Support",
                  "Core Apps", "CyNDEx-2", "CyCL", "Diffusion", "FileTransfer",
                  "ID Mapper", "JSON Support", "Network Merge", "NetworkAnalyzer",
                  "OpenCL Prefuse Layout", "PSI-MI Reader", "PSICQUIC Web Service Client",
                  "SBML Reader", "aMatReader", "copycatLayout", "cyBrowser",
                  "cyChart", "cyREST"]


function getInstalledAppsCyB() {
    cybrowser.executeCyCommandWithResults('apps list installed', 'renderInstalledApps' );
}
function getDisabledAppsCyB() {
    cybrowser.executeCyCommandWithResults('apps list disabled', 'renderDisabledApps' );
}
function getUpdatesAppsCyB() {
    cybrowser.executeCyCommandWithResults('apps list updates', 'renderUpdatesApps' );
}

function renderInstalledApps(res) {
    // alert(res);
        array = JSON.parse(res);
        array = array.sort(function(a,b){return a.appName.localeCompare(b.appName)});
        arrayUser = array.filter(function(a){return !coreApps.includes(a['appName'])});
        console.log(arrayUser.length + " enabled apps");
        arrayUser.forEach(app => {
            var aname=app['appName'];
            if (typeof aname == 'undefined') { aname = "";} //resolve null
            aname = aname.replace(/\"/g,"");
            var anamevar = aname.replace(/\W/g,"");
            var aver=app['version'];
            var astat=app['status'];
            if (aname.length > 0){
                arow = '<tr><td style=" background-color: #EEEEEE; width:25px;height:25px; cursor:pointer;"><img src="img/trash.png" height="18px" style="float:left;margin:0px 0 2px 3px;" onclick="uninstallAndRemove(&quot;'+aname+'&quot;)" title="Uninstall app"></td>';
                arow += '<td style=" background-color: #EEEEEE; width:25px;height:25px; cursor:pointer;"><input type="checkbox" id="'+anamevar+'" tabindex="0"  checked ' +
                    'onchange="toggleStatus(this, &quot;'+aname+'&quot;);" title="Toggle enabled status"></td>';
                arow += '<td style=" background-color: #EEEEEE; width:25px;height:25px;"><img src="img/update1.png" height="18px" style="float:left;margin:0px 0 0 3px;" title="No update available"</td>';
                arow += '<td>&nbsp;&nbsp;<a onClick="openAppStore(&quot;'+anamevar+'&quot;);" class="app" title="Visit App Store page">'+aname+'</a> (v'+aver+') </td></tr>';
                document.getElementById('appTable').innerHTML += arow;
            }
        });
        // Core Apps
        arrayCore = array.filter(function(a){return coreApps.includes(a['appName'])});
        console.log(arrayCore.length + " core apps");
        arrayCore.forEach(app => {
            var aname=app['appName'];
            if (typeof aname == 'undefined') { aname = "";} //resolve null
            aname = aname.replace(/\"/g,"");
            var anamevar = aname.replace(/\W/g,"");
            var aver=app['version'];
            var astat=app['status'];
            if (aname.length > 0){
                arow = '<tr><td style=" background-color: #EEDDDD; width:25px;height:25px; "><img src="img/trash.png" height="18px" style="float:left;margin:0px 0 2px 3px;" title="Cannot uninstall a core app"></td>';
                arow += '<td style=" background-color: #EEDDDD; width:25px;height:25px;"><input type="checkbox" id="'+anamevar+'" tabindex="0"  checked disabled' +
                    ' title="Cannot disable a core app"></td>';
                arow += '<td style=" background-color: #EEEEEE; width:25px;height:25px;"><img src="img/update1.png" height="18px" style="float:left;margin:0px 0 0 3px;" title="No update available"</td>';
                arow += '<td>&nbsp;&nbsp;<a onClick="openAppStore(&quot;'+anamevar+'&quot;);" class="app" title="Visit App Store page">'+aname+'</a> (v'+aver+') </td></tr>';
                document.getElementById('coreTable').innerHTML += arow;
            }
        });
}

function renderDisabledApps(res) {
    // alert(res);
        array = JSON.parse(res);
        console.log(array.length + " disabled apps");
        array = array.sort(function(a,b){return a.appName.localeCompare(b.appName)});
        array.forEach(app => {
            var aname=app['appName'];
            if (typeof aname == 'undefined') { aname = "";} //resolve null
            aname = aname.replace(/\"/g,"");
            var anamevar = aname.replace(/\W/g,"");
            var aver=app['version'];
            var astat=app['status'];
            if (aname.length > 0 && !coreApps.includes(aname)){
                arow = '<tr><td style="background-color: #EEEEEE; width:25px;height:25px; cursor:pointer;"><img src="img/trash.png" height="18px" style="float:left;margin:0px 0 2px 3px;" onclick="uninstallAndRemove(&quot;'+aname+'&quot;)" title="Uninstall app"></td>';
                arow += '<td style="background-color: #EEEEEE; width:25px;height:25px; cursor:pointer;"><input type="checkbox" id="'+anamevar+'" tabindex="0" ' +
                    'onchange="toggleStatus(this, &quot;'+aname+'&quot;);" title="Toggle enabled status"></td>';
                arow += '<td style=" background-color: #EEEEEE; width:25px;height:25px;"><img src="img/update1.png" height="18px" style="float:left;margin:0px 0 0 3px;" title="No update available"></td>';
                arow += '<td>&nbsp;&nbsp;<a onClick="openAppStore(&quot;'+anamevar+'&quot;);" class="app" title="Visit App Store page">'+aname+'</a> (v'+aver+') </td></tr>';
                document.getElementById('appTable').innerHTML += arow;
            }
        });
        sortData();
}
function sortData() {
        // Read table body node.
        var tableData = document.getElementById('appTable');

        // Read table row nodes.
        var rowData = tableData.getElementsByTagName('tr');

        for(var i = 0; i < rowData.length - 1; i++) {
            for(var j = 0; j < rowData.length - i; j++) {
                //Swap row nodes if short condition matches
                if(rowData.item(j).innerText.toLowerCase() > rowData.item(j+1).innerText.toLowerCase()) {
                    // console.log(rowData.item(j).innerText + " > " + rowData.item(j+1).innerText);
                    tableData.insertBefore(rowData.item(j+1).parentNode,rowData.item(j).parentNode);
                }
            }
        }
}

function toggleStatus(checkbox, app) {
    if(checkbox.checked == true){
        enableAppCyB(app);
    }else{
        disableAppCyB(app);
   }
}

function uninstallAndRemove(app) {
    uninstallAppCyB(app);
    // and remove from table
    var table = document.getElementById("appTable");
    for (var i = 0, row; row = table.rows[i]; i++) {
        aname = getAnameByRow(row);
        if (aname ==  app) {
            row.remove();
        }
    }
}

function getAnameByRow(row){
    var aname = row.cells[3].textContent.replace(/\(.*\)/g,"").trim();
    return (aname);
}
function getRowByAname(app){
    var table = document.getElementById("appTable");
    for (var i = 0, row; row = table.rows[i]; i++) {
        aname = getAnameByRow(row);
        if (aname ==  app) {
            return(row);
        }
    }
    var table = document.getElementById("coreTable");
    for (var i = 0, row; row = table.rows[i]; i++) {
        aname = getAnameByRow(row);
        if (aname ==  app) {
            return(row);
        }
    }

    var aname = row.cells[3].textContent.replace(/\(.*\)/g,"").trim();
    return (row);
}

function enableAllApps() {
    var table = document.getElementById("appTable");
    for (var i = 0, row; row = table.rows[i]; i++) {
        var aname = getAnameByRow(row);
        var anamevar = aname.replace(/\W/g,"");
        if (!document.getElementById(anamevar).checked){
            document.getElementById(anamevar).checked = true;
            enableAppCyB(aname);
        }
    }
}
function disableAllApps() {
    var table = document.getElementById("appTable");
    for (var i = 0, row; row = table.rows[i]; i++) {
        var aname = getAnameByRow(row);
        var anamevar = aname.replace(/\W/g,"");
        if (document.getElementById(anamevar).checked){
            document.getElementById(anamevar).checked = false;
            disableAppCyB(aname);
        }
    }
}
function uninstallAllApps() {
    var table = document.getElementById("appTable");
    for (var i = 0, row; row = table.rows[i]; i++) {
        var aname = getAnameByRow(row);
        uninstallAppCyB(aname);
        row.remove();
    }
}

function getAppUpdatesCyB() {
    cybrowser.executeCyCommand('apps list updates');
}
function disableAppCyB(app) {
    cybrowser.executeCyCommand('apps disable app=' + "'" + app + "'");
}
function enableAppCyB(app) {
    cybrowser.executeCyCommand('apps enable app=' + "'" + app + "'");
}
function uninstallAppCyB(app) {
    cybrowser.executeCyCommand('apps uninstall app=' + "'" + app + "'");
}

function toggleMenu(){
    var btn = document.getElementById("ddc");
    if (btn.style.display == ""){
        btn.style.display = "block";
    } else {
        btn.style.display = "";
    }
}

var coll = document.getElementsByClassName("collapsible");

for (var i = 0; i < coll.length; i++) {
  coll[i].addEventListener("click", function() {
    this.classList.toggle("active");
    var content = this.nextElementSibling;
    if (content.style.maxHeight){
      content.style.maxHeight = null;
    } else {
      content.style.maxHeight = content.scrollHeight + "px";
    }
  });
}

function expandCoreTable(){
  for (var i = 0, c; c = coll[i]; i++) {
    if (!c.classList.value.includes("active")){
        c.classList.add("active");
    }
    var content = c.nextElementSibling;
      if (content.style.maxHeight == ""){
        content.style.maxHeight = content.scrollHeight + "px";
      }
  }
}
/***************************
// FUNCTIONS FOR ONLINE ONLY
****************************/

function openAppStore(app=null){
    appUrl = "https://apps.cytoscape.org/"
    if (app != null){
        appUrl += "apps/"+app
    }
    cybrowser.executeCyCommand('cybrowser dialog url="'+appUrl+'" id="AppStore" title="App Store" ');
}
function searchAppStore(){
    var query = document.getElementById("search").value
    qUrl = "https://apps.cytoscape.org/"
    if (query != null){
        qUrl += "search?q="+query
    }
    cybrowser.executeCyCommand('cybrowser dialog url="'+qUrl+'" id="AppStore" title="App Store" ');
}

function updateAppAndIcon(app) {
    var cmd = 'apps update app="'+app+'"';
    cybrowser.executeCyCommand(cmd);
    //and swap update1.png icon back in for row
    var row = getRowByAname(app);
    updateAppRow(row);
}

 function updateAllApps() {
    cybrowser.executeCyCommand('apps update app="all"');
    //and clear all green arrows
    var table = document.getElementById("appTable");
    for (var i = 0, row; row = table.rows[i]; i++) {
        updateAppRow(row);
    }
    var table = document.getElementById("coreTable");
    for (var i = 0, row; row = table.rows[i]; i++) {
        updateAppRow(row);
    }
}

function updateAppRow(row) {
    row.cells[2].children[0].src = "img/update1.png";
    row.cells[2].children[0].title = "No update available";
    row.cells[2].children[0].style.cursor = "default";
    row.cells[2].children[0].removeAttribute('onclick');
    var newversion = row.cells[2].children[0].getAttribute('newversion');
    if (newversion != null){
        newversion = "(v"+newversion+")";
        appinfo = row.cells[3].textContent;
        newappinfo = appinfo.replace(/\(.*\)/,newversion);
        row.cells[3].textContent = newappinfo;
    }
}

function renderUpdatesApps(res) {
    // alert(res);
    //res = '[{"appName": "WikiPathways","version":"3.3.7","new version":"3.3.10"}]'
    res = res.replace(/},]/,"}]"); //bug: https://cytoscape.atlassian.net/browse/CYTOSCAPE-12992
    array = JSON.parse(res);
    console.log(array.length + " apps with updates");
        array.forEach(app => {
            var aname=app['appName'];
            if (typeof aname == 'undefined') { aname = "";} //resolve null
            aname = aname.replace(/\"/g,"");
            var anamevar = aname.replace(/\W/g,"");
            var newversion = app['new version'];
            var row = getRowByAname(aname);
            row.cells[2].children[0].src = "img/update2.png";
            row.cells[2].children[0].title = "Update app";
            row.cells[2].children[0].style.cursor = "pointer";
            row.cells[2].children[0].setAttribute('onclick', 'updateAppAndIcon("'+aname+'")');
            row.cells[2].children[0].setAttribute('newversion', newversion);
            if (coreApps.includes(aname)){ expandCoreTable();}
        });
}
    </script>
  </body>
</html>
