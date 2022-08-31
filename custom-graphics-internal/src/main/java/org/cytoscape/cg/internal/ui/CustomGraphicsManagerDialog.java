package org.cytoscape.cg.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.UIManager;

import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.util.ImageCustomGraphicsSelector;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * Main UI for managing on-memory library of Custom Graphics.
 * It's just a simple dialog that contains the ImageCustomGraphicsSelector.
 */
@SuppressWarnings("serial")
public class CustomGraphicsManagerDialog extends JDialog {

	private JButton closeButton;
	
	private ImageCustomGraphicsSelector imageSelector;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CustomGraphicsManagerDialog(
			Window owner,
			CustomGraphicsManager manager,
			CyServiceRegistrar serviceRegistrar
	) {
		super(owner, ModalityType.APPLICATION_MODAL);
		
		this.serviceRegistrar = serviceRegistrar;
		
		initComponents();

		pack();
		setLocationRelativeTo(owner);
	}

	private void initComponents() {
		setTitle("Image Manager");
		setMinimumSize(new Dimension(400, 400));
		setPreferredSize(new Dimension(600, 600));
		
		closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		var bottomPnl = LookAndFeelUtil.createOkCancelPanel(null, closeButton);
		
		var layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getImageSelector(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPnl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getImageSelector(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPnl)
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, closeButton.getAction());

		pack();
	}

	private ImageCustomGraphicsSelector getImageSelector() {
		if (imageSelector == null) {
			imageSelector = new ImageCustomGraphicsSelector(true, serviceRegistrar);
			imageSelector.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		}
		
		return imageSelector;
	}
}
