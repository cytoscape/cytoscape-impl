package org.cytoscape.ding.debug;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.cytoscape.view.presentation.NetworkImageFactory;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

@SuppressWarnings("serial")
public class ThumbnailPanel extends BasicCollapsiblePanel {

	private final CyServiceRegistrar registrar;
	
	
	public ThumbnailPanel(CyServiceRegistrar registrar) {
		super("NetworkImageFactory service");
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
		
		JCheckBox fitContentCheck = new JCheckBox("Fit Content");
		fitContentCheck.setSelected(true);
		LookAndFeelUtil.makeSmall(fitContentCheck);
		
		JCheckBox annotationsCheck = new JCheckBox("Include Annotations");
		annotationsCheck.setSelected(true);
		LookAndFeelUtil.makeSmall(annotationsCheck);
		
		JButton createButton = new JButton("Create");
		LookAndFeelUtil.makeSmall(createButton);
		
		
		createButton.addActionListener(e -> {
			int width  = ((SpinnerNumberModel)widthSpinner.getModel()).getNumber().intValue();
			int height = ((SpinnerNumberModel)heightSpinner.getModel()).getNumber().intValue();
			boolean fitContent = fitContentCheck.isSelected();
			boolean includeAnnotations = annotationsCheck.isSelected();
			createAndShowThumbnail(width, height, fitContent, includeAnnotations);
		});
		
		
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
			.addComponent(fitContentCheck)
			.addComponent(annotationsCheck)
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
			.addComponent(fitContentCheck)
			.addComponent(annotationsCheck)
			.addComponent(createButton)
		);
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	
	// MKTODO should also create a thumbnail of a canned hard-coded network, that's what MCODE et al would do.
	private void createAndShowThumbnail(int width, int height, boolean fitContent, boolean includeAnnotations) {
		var applicationManager = registrar.getService(CyApplicationManager.class);
		var thumbnailFactory   = registrar.getService(NetworkImageFactory.class, "(id=ding)");
		var annotationManager  = registrar.getService(AnnotationManager.class);
		
		var networkView = applicationManager.getCurrentNetworkView();
		if(networkView == null)
			return;
		
		Map<String,Object> props = new HashMap<>();
		props.put(NetworkImageFactory.WIDTH, width);
		props.put(NetworkImageFactory.HEIGHT, height);
		props.put(NetworkImageFactory.FIT_CONTENT, fitContent);
		
		Collection<Annotation> annotations = null;
		if(includeAnnotations)
			annotations = annotationManager.getAnnotations(networkView);
		
		Image image = thumbnailFactory.createImage(networkView, annotations, props);
		
		JLabel picLabel = new JLabel(new ImageIcon(image));
		JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), picLabel);
	}
	
	
}
