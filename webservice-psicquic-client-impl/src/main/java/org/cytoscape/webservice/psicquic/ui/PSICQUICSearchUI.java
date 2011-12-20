package org.cytoscape.webservice.psicquic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.task.SearchRecoredsTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

public class PSICQUICSearchUI extends JPanel {

	private static final long serialVersionUID = 3163269742016489767L;

	private final RegistryManager regManager;
	private final PSICQUICRestClient client;
	private final TaskManager taskManager;
	private final CyNetworkManager networkManager;
	private final NetworkTaskFactory createViewTaskFactory;

	private JComboBox searchModeBox;
	private JTextArea queryArea;
	private SourceStatusPanel statesPanel;
	private JScrollPane scrollPane;

	private JPanel searchPanel;
	private JLabel modeLabel;
	private JButton helpButton;
	private JButton searchButton;
	
	private JPanel queryBuilderPanel;
	
	private static final String MIQL_MODE = "MIQL Query";
	private static final String INTERACTOR_ID_LIST = "List of Interactors";
	
	private static final String MIQL_BUILDER_PANEL = "MIQL Query Builder";
	
	private static final String MIQL_QUERY_AREA_MESSAGE_STRING = "Please enter MIQL query here.";
	private static final String INTERACTOR_LIST_AREA_MESSAGE_STRING = "Please enter MIQL query here.";
	
	private String searchAreaTitle = MIQL_MODE;
	
	private boolean firstClick = true;

	public PSICQUICSearchUI(final CyNetworkManager networkManager, final RegistryManager regManager,
			final PSICQUICRestClient client, final TaskManager tmManager, final NetworkTaskFactory createViewTaskFactory) {
		this.regManager = regManager;
		this.client = client;
		this.taskManager = tmManager;
		this.networkManager = networkManager;
		this.createViewTaskFactory = createViewTaskFactory;

		init();
	}

	private void init() {
		this.setLayout(new BorderLayout());
		this.setBackground(Color.white);
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Search Mode box
		searchModeBox = new JComboBox();
		searchModeBox.addItem(SearchMode.MIQL);
		searchModeBox.addItem(SearchMode.INTERACTOR);
		searchModeBox.setPreferredSize(new Dimension(200, 30));

		// Search Panel
		searchPanel = new JPanel();
		searchPanel.setBackground(Color.white);
		searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
		searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				search();
			}
		});
		modeLabel = new JLabel("Search Mode:");
		
		// Help menu
		this.helpButton = new JButton("");
		
		searchPanel.add(modeLabel);
		searchPanel.add(searchModeBox);
		searchPanel.add(searchButton);

		this.add(searchPanel, BorderLayout.NORTH);

		// Query Panel
		queryArea = new JTextArea();
		queryArea.setText(MIQL_QUERY_AREA_MESSAGE_STRING);
		queryArea.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseEntered(MouseEvent ev) {
				if(firstClick) {
					queryArea.setText("");
					firstClick = false;
				}
			}
		});
		
		
		scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.white);
		scrollPane.setBorder(new TitledBorder(searchAreaTitle));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(150, 150));
		scrollPane.setViewportView(queryArea);
		this.add(scrollPane, BorderLayout.CENTER);

		// Source Status
		this.statesPanel = new SourceStatusPanel("", client, regManager, networkManager, null, taskManager, (SearchMode)searchModeBox.getSelectedItem(), createViewTaskFactory);
		this.add(statesPanel, BorderLayout.SOUTH);
	}
	
	private void switchMode() {
		
		// Disable query builder
	}

	private void search() {
		final SearchRecoredsTask searchTask = new SearchRecoredsTask(client, (SearchMode)searchModeBox.getSelectedItem());
		final Map<String, String> activeSource = regManager.getActiveServices();
		final String query = this.queryArea.getText();
		searchTask.setQuery(query);
		searchTask.setTargets(activeSource.values());

		taskManager.execute(new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return new TaskIterator(searchTask, new SetTableTask(searchTask));
			}
		});
	}

	private final class SetTableTask extends AbstractTask {

		final SearchRecoredsTask searchTask;

		public SetTableTask(final SearchRecoredsTask searchTask) {
			this.searchTask = searchTask;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			final Map<String, Long> result = searchTask.getResult();
			remove(statesPanel);
			statesPanel = new SourceStatusPanel(queryArea.getText(), client, regManager,networkManager, result, taskManager, (SearchMode)searchModeBox.getSelectedItem(), createViewTaskFactory);
			add(statesPanel, BorderLayout.SOUTH);

			Window parentWindow = ((Window) getRootPane().getParent());
			parentWindow.pack();
			repaint();
			
			parentWindow.toFront();
		}
	}
}
