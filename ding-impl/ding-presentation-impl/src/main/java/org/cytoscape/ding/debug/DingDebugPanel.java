package org.cytoscape.ding.debug;

import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

@SuppressWarnings("serial")
public class DingDebugPanel extends JPanel implements CytoPanelComponent, DebugCallback, SetCurrentNetworkViewListener {

	private final CyServiceRegistrar registrar;
	
	private DRenderingEngine re;
	
	private JLabel networkNameLabel;
	private JList<String> fastList;
	private DefaultListModel<String> fastModel;
	private JList<String> slowList;
	private DefaultListModel<String> slowModel;
	
	
	public DingDebugPanel(CyServiceRegistrar registrar) {
		this.registrar = registrar;
		createContents();
	}

	private void createContents() {
		networkNameLabel = new JLabel("Network Name");
		JLabel fastTitle = new JLabel("Fast Frames");
		JLabel slowTitle = new JLabel("Slow Frames");
		
		fastModel = new DefaultListModel<>();
		fastList = new JList<>(fastModel);
		DefaultListCellRenderer fastRenderer = (DefaultListCellRenderer)fastList.getCellRenderer();
		fastRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		JScrollPane fastScrollPane = new JScrollPane(fastList);
		
		slowModel = new DefaultListModel<>();
		slowList = new JList<>(slowModel);
		DefaultListCellRenderer slowRenderer = (DefaultListCellRenderer)slowList.getCellRenderer();
		slowRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		JScrollPane slowScrollPane = new JScrollPane(slowList);
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> clear());
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(networkNameLabel)
			.addGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(fastTitle)
				.addComponent(slowTitle)
			)
			.addGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(fastScrollPane)
				.addComponent(slowScrollPane)
			)
			.addComponent(clearButton)
		);
		
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(networkNameLabel)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(fastTitle)
					.addComponent(fastScrollPane)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(slowTitle)
					.addComponent(slowScrollPane)
				)
			)
			.addComponent(clearButton)
		);
	}
	
	private void clear() {
		fastModel.clear();
		slowModel.clear();
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		clear();
		if(re != null)
			re.setDebugCallback(null);
		
		CyNetworkView netView = e.getNetworkView();
		
		CyNetwork model = netView.getModel();
		String name = model.getRow(model).get(CyNetwork.NAME, String.class);
		networkNameLabel.setText(name);
		
		DingRenderer dingRenderer = registrar.getService(DingRenderer.class);
		re = dingRenderer.getRenderingEngine(netView);
		if(re != null)
			re.setDebugCallback(this);
	}
	

	@Override
	public void addFrameTime(boolean fast, long time) {
		DefaultListModel<String> model = fast ? fastModel : slowModel;
		JList<String> list = fast ? fastList : slowList;
		int i = model.size();
		model.add(i, String.valueOf(time));
		list.ensureIndexIsVisible(i);
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return "Ding Debug";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

}
