package org.cytoscape.ding.debug;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.ThumbnailFactory;

@SuppressWarnings("serial")
public class ThumbnailPanel extends BasicCollapsiblePanel {

	private final CyServiceRegistrar registrar;
	
	
	public ThumbnailPanel(CyServiceRegistrar registrar) {
		super("ThumbnailFactory service");
		this.registrar = registrar;
		createContents();
	}
	
	private void createContents() {
		JLabel heightLabel = new JLabel("Height");
		JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 1000000, 100));
		LookAndFeelUtil.makeSmall(heightLabel, heightSpinner);
		
		JLabel widthLabel = new JLabel("Width");
		JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 1000000, 100));
		LookAndFeelUtil.makeSmall(widthLabel, widthSpinner);
		
		JButton createButton = new JButton("Create Thumbnail of Current Network");
		createButton.addActionListener(e -> {
			int width  = ((SpinnerNumberModel)widthSpinner.getModel()).getNumber().intValue();
			int height = ((SpinnerNumberModel)heightSpinner.getModel()).getNumber().intValue();
			createAndShowThumbnail(width, height);
		});
		LookAndFeelUtil.makeSmall(createButton);
		
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(heightLabel)
					.addComponent(widthLabel)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(heightSpinner)
					.addComponent(widthSpinner)
				)
			)
			.addComponent(createButton)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(heightLabel)
				.addComponent(heightSpinner, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(widthLabel)
				.addComponent(widthSpinner, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addComponent(createButton)
		);
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	
	// MKTODO should also create a thumbnail of a canned hard-coded network, that's what MCODE et al would do.
	private void createAndShowThumbnail(int width, int height) {
		Map<String,Object> props = new HashMap<>();
		props.put(ThumbnailFactory.WIDTH, width);
		props.put(ThumbnailFactory.HEIGHT, height);
		
		var applicationManager = registrar.getService(CyApplicationManager.class);
		var thumbnailFactory   = registrar.getService(ThumbnailFactory.class, "(id=ding)");
		
		var networkView = applicationManager.getCurrentNetworkView();
		if(networkView == null)
			return;
		
		Image image = thumbnailFactory.getThumbnail(networkView, props);
		
		JLabel picLabel = new JLabel(new ImageIcon(image));
		JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), picLabel);
	}
	
	
}
