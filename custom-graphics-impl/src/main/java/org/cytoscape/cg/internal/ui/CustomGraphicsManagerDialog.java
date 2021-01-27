package org.cytoscape.cg.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.cg.internal.image.AbstractURLImageCustomGraphics;
import org.cytoscape.cg.internal.image.BitmapCustomGraphics;
import org.cytoscape.cg.internal.image.SVGCustomGraphics;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.util.CustomGraphicsBrowser;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main UI for managing on-memory library of Custom Graphics
 */
@SuppressWarnings("serial")
public class CustomGraphicsManagerDialog extends JDialog {

	private static final String IMG_FILES_DESCRIPTION = "Image file (PNG, GIF, JPEG or SVG)";
	private static final String[] IMG_EXTENSIONS = { "jpg", "jpeg", "png", "gif", "svg" };
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private JButton addButton;
	private JButton deleteButton;
	private JButton closeButton;
	private JScrollPane leftScrollPane;
	private JSplitPane mainSplitPane;
	private JScrollPane rightScrollPane;
	private JPanel leftPanel;
	private JPanel buttonPanel;
	
	// List of graphics available
	private final CustomGraphicsBrowser browser;
	// Panel for displaying actual size image
	private final CustomGraphicsDetailPanel detail;
	private final CustomGraphicsManager manager;
	private final IconManager iconManager;

	public CustomGraphicsManagerDialog(
			Window owner,
			CustomGraphicsManager manager,
			CustomGraphicsBrowser browser,
			CyServiceRegistrar serviceRegistrar
	) {
		super(owner, ModalityType.APPLICATION_MODAL);
		
		if (browser == null)
			throw new NullPointerException("CustomGraphicsBrowser is null.");

		this.manager = manager;
		this.browser = browser;
		this.iconManager = serviceRegistrar.getService(IconManager.class);
		
		initComponents();

		detail = new CustomGraphicsDetailPanel(serviceRegistrar.getService(CyApplicationManager.class));

		this.leftScrollPane.setViewportView(browser);
		this.rightScrollPane.setViewportView(detail);
		this.setPreferredSize(new Dimension(880, 580));
		this.setTitle("Image Manager");

		this.browser.addListSelectionListener(detail);
		pack();
	}

	private void initComponents() {
		deleteButton = new JButton();
		addButton = new JButton();
		mainSplitPane = new JSplitPane();
		leftScrollPane = new JScrollPane();
		rightScrollPane = new JScrollPane();
		leftPanel = new JPanel();
		
		mainSplitPane.setBorder(null);
		rightScrollPane.setBorder(null);
		leftScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		leftPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		addButton.setText(IconManager.ICON_PLUS);
		addButton.setFont(iconManager.getIconFont(18.0f));
		addButton.setToolTipText("Add Images");
		
		if (LookAndFeelUtil.isAquaLAF()) {
			addButton.putClientProperty("JButton.buttonType", "segmentedGradient");
			addButton.putClientProperty("JButton.segmentPosition", "middle");
		}
		
		addButton.addActionListener(evt -> addButtonActionPerformed(evt));
		
		deleteButton.setText(IconManager.ICON_TRASH_O);
		deleteButton.setFont(iconManager.getIconFont(18.0f));
		deleteButton.setToolTipText("Remove Selected Images");
		
		if (LookAndFeelUtil.isAquaLAF()) {
			deleteButton.putClientProperty("JButton.buttonType", "segmentedGradient");
			deleteButton.putClientProperty("JButton.segmentPosition", "only");
		}
		
		deleteButton.addActionListener(evt -> deleteButtonActionPerformed(evt));

		closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		buttonPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton);
		
		mainSplitPane.setDividerLocation(230);
		mainSplitPane.setLeftComponent(leftPanel);
		mainSplitPane.setRightComponent(rightScrollPane);
		
		{
			var layout = new GroupLayout(leftPanel);
			leftPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(leftScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(addButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(deleteButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(leftScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
							.addComponent(addButton)
							.addComponent(deleteButton)
					)
			);
		}
		{
			var layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(mainSplitPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
							.addComponent(mainSplitPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		}
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, closeButton.getAction());

		pack();
	}

	private void addButtonActionPerformed(ActionEvent evt) {
		// Add a directory
		var chooser = new JFileChooser();
		
		var filter = new FileNameExtensionFilter(IMG_FILES_DESCRIPTION, IMG_EXTENSIONS);
		chooser.setDialogTitle("Select Image Files");
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
			processFiles(chooser.getSelectedFiles());
	}

	private void processFiles(File[] files) {
		for (var file : files) {
			BufferedImage img = null;
			String svg = null;
			
			if (file.isFile()) {
				try {
					if (file.getName().toLowerCase().endsWith(".svg"))
						svg = Files.readString(file.toPath());
					else
						img = ImageIO.read(file);
				} catch (Exception e) {
					logger.error("Could not read file: " + file.toString(), e);
					continue;
				}
			}

			try {
				var url = file.toURI().toURL();
				AbstractURLImageCustomGraphics<?> cg = null;
				
				if (svg != null)
					cg = new SVGCustomGraphics(manager.getNextAvailableID(), file.toString(), url, svg);
				else if (img != null)
					cg = new BitmapCustomGraphics(manager.getNextAvailableID(), file.toString(), url, img);

				if (cg != null) {
					manager.addCustomGraphics(cg, url);
					((CustomGraphicsListModel) browser.getModel()).addElement(cg);
				}
			} catch (Exception e) {
				logger.error("Could not create custom graphics: " + file, e);
				continue;
			}
		}
	}

	private void deleteButtonActionPerformed(ActionEvent evt) {
		var toBeRemoved = browser.getSelectedValues();
		
		for (var obj : toBeRemoved) {
			var cg = (CyCustomGraphics<?>) obj;
			
			if (!manager.isUsedInCurrentSession(cg)) {
				browser.removeCustomGraphics(cg);
				manager.removeCustomGraphics(cg.getIdentifier());
			} else {
				JOptionPane.showMessageDialog(this,
						cg.getDisplayName() + " is used in current session and cannot remove it.",
						"Custom Graphics is in Use.", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
