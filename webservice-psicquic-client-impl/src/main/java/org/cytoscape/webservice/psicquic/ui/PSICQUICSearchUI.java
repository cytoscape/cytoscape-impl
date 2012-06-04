package org.cytoscape.webservice.psicquic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.task.SearchRecoredsTask;
import org.cytoscape.webservice.psicquic.ui.SelectorBuilder.Species;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

public class PSICQUICSearchUI extends JPanel implements ChangeListener {

	private static final long serialVersionUID = 3163269742016489767L;
	
	private static final String MIQL_REFERENCE_PAGE_URL = "http://code.google.com/p/psicquic/wiki/MiqlReference";

	// Color Scheme
	private static final Color MIQL_COLOR = new Color(0x7f, 0xff, 0xd4);
	private static final Color ID_LIST_COLOR = new Color(0xff, 0xa5, 0x00);

	private static final Font STRONG_FONT = new Font("SansSerif", Font.BOLD, 14);

	// Fixed messages
	private static final String MIQL_MODE = "Query Text (MIQL)";
	private static final String INTERACTOR_ID_LIST = "List of Interactors (gene/protein/compound ID)";

	private static final String MIQL_QUERY_AREA_MESSAGE_STRING = "Please enter search query (MIQL) here.  \nIf you need help, please click Syntax Help button below.";
	private static final String INTERACTOR_LIST_AREA_MESSAGE_STRING = "Please enter list of genes/proteins/compounds, separated by space.";

	private final RegistryManager regManager;
	private final PSICQUICRestClient client;
	private final TaskManager<?, ?> taskManager;
	private final CyNetworkManager networkManager;
	private final CreateNetworkViewTaskFactory createViewTaskFactory;

	private JTextArea queryArea;
	private SourceStatusPanel statesPanel;
	private JScrollPane scrollPane;

	private JPanel searchPanel;
	private JLabel modeLabel;
	private JButton helpButton;
	private JButton searchButton;

	private JPanel queryBuilderPanel;

	private JComboBox speciesSelector;
	private ButtonGroup bg;

	private SearchMode mode = SearchMode.MIQL;
	private String searchAreaTitle = MIQL_MODE;

	private boolean firstClick = true;

	private final OpenBrowser openBrowserUtil;
	
	public PSICQUICSearchUI(final CyNetworkManager networkManager, final RegistryManager regManager,
			final PSICQUICRestClient client, final TaskManager<?,?> tmManager,
			final CreateNetworkViewTaskFactory createViewTaskFactory, final OpenBrowser openBrowserUtil) {
		this.regManager = regManager;
		this.client = client;
		this.taskManager = tmManager;
		this.networkManager = networkManager;
		this.createViewTaskFactory = createViewTaskFactory;
		this.openBrowserUtil = openBrowserUtil;

		init();
	}

	private void init() {
		this.setLayout(new BorderLayout());
		this.setBackground(Color.white);
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Search Panel
		searchPanel = new JPanel();
		searchPanel.setBackground(Color.white);
		searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
		searchButton = new JButton("Search");
		searchButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				search();
				statesPanel.enableComponents(true);
			}
		});
		modeLabel = new JLabel("Query Type:");
		JRadioButton miqlMode = new JRadioButton(SearchMode.MIQL.toString(), true);
		miqlMode.setActionCommand(SearchMode.MIQL.toString());
		miqlMode.setForeground(MIQL_COLOR);
		miqlMode.setFont(STRONG_FONT);

		JRadioButton idListMode = new JRadioButton(SearchMode.INTERACTOR.toString());
		idListMode.setActionCommand(SearchMode.INTERACTOR.toString());
		idListMode.setForeground(ID_LIST_COLOR);
		idListMode.setFont(STRONG_FONT);

		bg = new ButtonGroup();
		bg.add(miqlMode);
		bg.add(idListMode);

		miqlMode.addChangeListener(this);
		idListMode.addChangeListener(this);

		searchPanel.add(modeLabel);
		searchPanel.add(miqlMode);
		searchPanel.add(idListMode);
		searchPanel.add(searchButton);

		// Query Panel
		queryArea = new JTextArea();
		queryArea.setText(MIQL_QUERY_AREA_MESSAGE_STRING);
		queryArea.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent ev) {
				if (firstClick) {
					queryArea.setText("");
					firstClick = false;
				}
			}
		});

		this.queryBuilderPanel = new JPanel();
		queryBuilderPanel.setBackground(Color.white);

		queryBuilderPanel.setLayout(new BoxLayout(queryBuilderPanel, BoxLayout.X_AXIS));
		queryBuilderPanel.setBorder(new TitledBorder("Query Helper"));
		// Help menu
		this.helpButton = new JButton("Syntax Help");
		helpButton.setToolTipText("Show MIQL Syntax Reference in Web Browser...");
		helpButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				openBrowserUtil.openURL(MIQL_REFERENCE_PAGE_URL);
				
			}
		});

		final JLabel speciesLabel = new JLabel("Species:");
		final SelectorBuilder speciesBuilder = new SelectorBuilder();
		speciesSelector = speciesBuilder.getComboBox();
		speciesSelector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addSpeciesQuery();
			}
		});

		queryBuilderPanel.add(speciesLabel);
		queryBuilderPanel.add(speciesSelector);
		queryBuilderPanel.add(helpButton);

		final JPanel basePanel = new JPanel();
		basePanel.setBackground(Color.WHITE);
		basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
		basePanel.add(queryBuilderPanel);
		basePanel.add(searchPanel);

		this.add(basePanel, BorderLayout.CENTER);

		scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.white);
		
		final TitledBorder border = new TitledBorder(searchAreaTitle);
		border.setTitleColor(MIQL_COLOR);
		border.setTitleFont(STRONG_FONT);
		scrollPane.setBorder(border);
		scrollPane.setBorder(border);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(150, 150));
		scrollPane.setViewportView(queryArea);
		this.add(scrollPane, BorderLayout.NORTH);

		// Source Status
		this.statesPanel = new SourceStatusPanel("", client, regManager, networkManager, null, taskManager, mode,
				createViewTaskFactory);
		this.add(statesPanel, BorderLayout.SOUTH);
		statesPanel.enableComponents(false);
	}

	private void addSpeciesQuery() {
		
		final Object selectedItem = speciesSelector.getSelectedItem();
		final Species species = (Species) selectedItem;

		String currentQuery = this.queryArea.getText();
		if(currentQuery.contains(MIQL_QUERY_AREA_MESSAGE_STRING)) {
			currentQuery = "";
			queryArea.setText("");
		}
		
		
		final String newQuery;
		if (species == Species.ALL) {
			newQuery = currentQuery.replaceAll("species:\".+\"", "");
		} else {

			final String speciesEntry = "species:\"" + species.toString() + "\"";

			if (currentQuery.contains("species:")) {
				newQuery = currentQuery.replaceAll("species:\".+\"", speciesEntry);
			} else {
				newQuery = currentQuery + " " + speciesEntry;
			}

			this.queryArea.setText(newQuery);
		}
	}

	private void search() {
		final SearchRecoredsTask searchTask = new SearchRecoredsTask(client, mode);
		final Map<String, String> activeSource = regManager.getActiveServices();
		final String query = this.queryArea.getText();
		searchTask.setQuery(query);
		searchTask.setTargets(activeSource.values());

		taskManager.execute(new TaskIterator(searchTask, new SetTableTask(searchTask)));
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
			statesPanel = new SourceStatusPanel(queryArea.getText(), client, regManager, networkManager, result,
					taskManager, mode, createViewTaskFactory);
			add(statesPanel, BorderLayout.SOUTH);
			
			statesPanel.sort();

			Window parentWindow = ((Window) getRootPane().getParent());
			parentWindow.pack();
			repaint();

			
			parentWindow.toFront();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		final String actionCommand = bg.getSelection().getActionCommand();
		final Color borderColor;
		if (actionCommand.equals(SearchMode.MIQL.toString())) {
			mode = SearchMode.MIQL;
			searchAreaTitle = MIQL_MODE;
			speciesSelector.setEnabled(true);
			helpButton.setEnabled(true);
			queryArea.setText(MIQL_QUERY_AREA_MESSAGE_STRING);
			borderColor = MIQL_COLOR;
		} else {
			mode = SearchMode.INTERACTOR;
			searchAreaTitle = INTERACTOR_ID_LIST;
			speciesSelector.setEnabled(false);
			helpButton.setEnabled(false);
			queryArea.setText(INTERACTOR_LIST_AREA_MESSAGE_STRING);
			borderColor = ID_LIST_COLOR;
		}

		firstClick = true;

		final TitledBorder border = new TitledBorder(searchAreaTitle);
		border.setTitleColor(borderColor);
		border.setTitleFont(STRONG_FONT);
		scrollPane.setBorder(border);
		
		remove(statesPanel);
		statesPanel = new SourceStatusPanel(queryArea.getText(), client, regManager, networkManager, null,
				taskManager, mode, createViewTaskFactory);
		add(statesPanel, BorderLayout.SOUTH);
		Window parentWindow = ((Window) getRootPane().getParent());
		parentWindow.pack();
		repaint();
		
		statesPanel.enableComponents(false);
		
	}
}
