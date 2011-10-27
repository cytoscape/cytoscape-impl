package org.cytoscape.io.webservice.biomart.task;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.webservice.biomart.BiomartClient;
import org.cytoscape.io.webservice.biomart.ui.BiomartAttrMappingPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO: Add cancel function
 */
public class ShowBiomartGUIAction extends AbstractCyAction {

	private static final long serialVersionUID = -1329132199540543764L;

	private static final Logger logger = LoggerFactory.getLogger(ShowBiomartGUIAction.class);

	private final DialogTaskManager taskManager;
	private final CySwingApplication app;
	
	private ShowBiomartDialogTask showDialogTask;
	private final CyApplicationManager appManager;
	
	private final LoadRepositoryTask firstTask;

	public ShowBiomartGUIAction(final BiomartAttrMappingPanel panel, final BiomartClient client,
			final TaskManager taskManager,
			final CyApplicationManager appManager,
			final CySwingApplication app) {
		super("from Biomart...", appManager);
		setPreferredMenu("File.Import.Table.WebService");

		this.appManager = appManager;
		this.app = app;
		this.taskManager = (DialogTaskManager) taskManager;
		
		this.firstTask = new LoadRepositoryTask(client.getRestClient());
		this.showDialogTask = new ShowBiomartDialogTask(panel, client, app, firstTask);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		// State check: BioMart client needs at least a network to create query string.
		final CyNetwork net = appManager.getCurrentNetwork();
		if(net == null) {
			JOptionPane.showMessageDialog(app.getJFrame(), "BioMart Client needs at least one network to create query.  Please import a network first.",
					"No Network Found", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// Lazy instantiation. This process depends on network connection.
		if (showDialogTask.getDialog() == null) {			
			initDialog();
		} else {
			showDialogTask.getDialog().setLocationRelativeTo(app.getJFrame());
			showDialogTask.getDialog().setVisible(true);
		}
	}
	
	
	private void initDialog() {
		
		final BioMartTaskFactory tf = new BioMartTaskFactory(firstTask);
		tf.getTaskIterator().insertTasksAfter(firstTask, showDialogTask);

		taskManager.setExecutionContext(app.getJFrame());
		taskManager.execute(tf);
	}
}
