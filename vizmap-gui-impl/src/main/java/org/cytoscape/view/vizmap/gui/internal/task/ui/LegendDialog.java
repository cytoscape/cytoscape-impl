/*
 * LegendDialog.java
 */
package org.cytoscape.view.vizmap.gui.internal.task.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;


/**
 * Dialog for legend
 */

//TODO: not working! Should create utility class to generate legend from given mapping.

public class LegendDialog extends JDialog {
	private final static long serialVersionUID = 1202339876783665L;
	
	private VisualStyle visualStyle;
	private JPanel jPanel1;
	private JButton jButton1;
	private JButton jButton2;
	private JScrollPane jScrollPane1;

	/**
	 * Creates a new LegendDialog object.
	 *
	 * @param parent  DOCUMENT ME!
	 * @param vs  DOCUMENT ME!
	 */
	public LegendDialog(final VisualStyle vs) {
		super();
		this.setModal(true);
		
		visualStyle = vs;
		initComponents();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	public void showDialog(final Component parent) {
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param visualStyle DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	private JPanel generateLegendPanel(final VisualStyle visualStyle) {
		final JPanel legend = new JPanel();

		Collection<VisualMappingFunction<?, ?>> mappings = visualStyle.getAllVisualMappingFunctions();

		/*
		 * Set layout
		 */
		legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
		legend.setBackground(Color.white);

		legend.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY, 2),
		                                  "Visual Legend for " + visualStyle.getTitle(),
		                                  TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.CENTER,
		                                  new Font("SansSerif", Font.BOLD, 16), Color.DARK_GRAY));

//		for (VisualMappingFunction<?, ?> map: mappings) {
//
//			
//			JPanel mleg = map.getLegend();
//
//			// Add passthrough mappings to the top since they don't
//			// display anything besides the title.
//			if (om instanceof PassthroughMappingCalculator)
//				legend.add(mleg, 0);
//			else
//				legend.add(mleg);
//
//			// Set padding
//			mleg.setBorder(new EmptyBorder(15, 30, 15, 30));
//		}
//
//
//		for (Calculator calc : edgeCalcs) {
//			om = calc.getMapping(0);
//
//			JPanel mleg = om.getLegend(calc.getVisualProperty());
//
//			// Add passthrough mappings to the top since they don't
//			// display anything besides the title.
//			if (om instanceof PassthroughMappingCalculator)
//				legend.add(mleg, 0);
//			else
//				legend.add(mleg);
//
//			//			 Set padding
//			mleg.setBorder(new EmptyBorder(15, 30, 15, 30));
//		}

		return legend;
	}

	private void initComponents() {
		this.setTitle("Visual Legend for " + visualStyle.getTitle());

		jPanel1 = generateLegendPanel(visualStyle);

		jScrollPane1 = new JScrollPane();
		jScrollPane1.setViewportView(jPanel1);

		jButton1 = new JButton();
		jButton1.setText("Export");
		jButton1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					export();
				}
			});

		jButton2 = new JButton();
		jButton2.setText("Done");
		jButton2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					dispose();
				}
			});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(jButton1);
		buttonPanel.add(jButton2);

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(jScrollPane1);
		containerPanel.add(buttonPanel);

		setContentPane(containerPanel);
		setPreferredSize(new Dimension(650, 500));
		pack();
		repaint();
	}

	private void export() {
//		ExportDialog export = new ExportDialog();
//		export.showExportDialog(parent, "Export legend as ...", jPanel1, "export");
//		dispose();
	}
}
