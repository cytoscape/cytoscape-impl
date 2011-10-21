package org.cytoscape.task.internal.welcome;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;

public class CreateNewNetworkPanel extends JPanel {

	private JLabel loadNetwork;
	private JLabel fromDB;

	private JComboBox networkList;
	private JCheckBox layout;

	private final LoadMitabFileTaskFactory loadTF;
	private final TaskManager guiTaskManager;

	private Window parent;

	private DownloadBiogridDataTaskFactory taskFactory;

	CreateNewNetworkPanel(Window parent, final TaskManager guiTaskManager, final LoadMitabFileTaskFactory loadTF,
			final CyApplicationConfiguration config, final TaskFactory layoutTF) {
		this.loadTF = loadTF;
		this.parent = parent;
		this.guiTaskManager = guiTaskManager;
		this.networkList = new JComboBox();
		taskFactory = new DownloadBiogridDataTaskFactory(networkList, config);
		guiTaskManager.execute(taskFactory);
		initComponents();
	}

	private void initComponents() {

		this.layout = new JCheckBox();
		layout.setText("Apply default layout?");
		layout.setToolTipText("Note: This option may take minutes to finish for large networks!");

		this.loadNetwork = new JLabel("From file...");
		this.setBorder(new LineBorder(new Color(0, 0, 0, 0), 10));

		this.fromDB = new JLabel("From public data:");

		this.setLayout(new GridLayout(4, 1));
		this.add(loadNetwork);
		this.add(fromDB);
		this.add(networkList);
		this.add(layout);

		this.networkList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					loadNetwork();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private boolean firstSelection = false;

	private void loadNetwork() throws URISyntaxException {
		
		Object file = networkList.getSelectedItem();
		if(file == null || firstSelection == false) {
			firstSelection = true;
			return;
		}
		
		final URL url = taskFactory.getMap().get(file);
		if(layout.isSelected())
			loadTF.setApplyLayout(true);
		else
			loadTF.setApplyLayout(false);
		
		loadTF.setFile(new File(url.toURI()));
		guiTaskManager.execute(loadTF);
		parent.dispose();
	}
}
