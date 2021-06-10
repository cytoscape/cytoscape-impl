package org.cytoscape.ding.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class NetworkViewportPanel extends JPanel {

	private JLabel xMaxLabel;
	private JLabel xMinLabel;
	private JLabel yMaxLabel;
	private JLabel yMinLabel;
	private JLabel zoomLabel;
	private JLabel centerLabel;
	
	public NetworkViewportPanel() {
		setOpaque(false);
		createContents();
	}
	
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel xMaxPanel = createLabelPanel(xMaxLabel = new JLabel());
		JPanel xMinPanel = createLabelPanel(xMinLabel = new JLabel());
		JPanel yMaxPanel = createLabelPanel(yMaxLabel = new JLabel());
		JPanel yMinPanel = createLabelPanel(yMinLabel = new JLabel());
		
		centerLabel = new JLabel();
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setOpaque(false);
		centerPanel.setPreferredSize(new Dimension(140, 100));
		
		JLabel filler = new JLabel(" ");
		centerPanel.add(filler, BorderLayout.NORTH);
		centerPanel.add(createLabelPanel(centerLabel), BorderLayout.CENTER);
		zoomLabel = new JLabel("");
		centerPanel.add(createLabelPanel(zoomLabel), BorderLayout.SOUTH);
		
		JPanel squarePanel = new JPanel(new GridBagLayout());
		squarePanel.setOpaque(false);
		squarePanel.add(centerPanel);
		squarePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		squarePanel.setMinimumSize(new Dimension(140, 100));
		
		LookAndFeelUtil.makeSmall(xMaxLabel, xMinLabel, yMaxLabel, yMinLabel, centerLabel, zoomLabel, filler);
		
		add(yMinPanel, BorderLayout.NORTH);
		add(xMinPanel, BorderLayout.WEST);
		add(yMaxPanel, BorderLayout.SOUTH);
		add(xMaxPanel, BorderLayout.EAST);
		add(squarePanel, BorderLayout.CENTER);
	}

	private static JPanel createLabelPanel(JLabel label) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(label);
		panel.setOpaque(false);
		return panel;
	}
	
	public void clear() {
		yMinLabel.setText("");
		xMinLabel.setText("");
		yMaxLabel.setText("");
		xMaxLabel.setText("");
		zoomLabel.setText("");
		centerLabel.setText("");
	}
	
	public void updateTransform(NetworkTransform t) {
		var b = t.getNetworkVisibleAreaNodeCoords();
		yMinLabel.setText(String.format("y-min: %.2f", b.getMinY()));
		xMinLabel.setText(String.format("x-min: %.2f ", b.getMinX()));
		yMaxLabel.setText(String.format("y-max: %.2f", b.getMaxY()));
		xMaxLabel.setText(String.format(" x-max: %.2f", b.getMaxX()));
		zoomLabel.setText(String.format("zoom: %.4f", t.getScaleFactor()));
		centerLabel.setText(String.format("(%.2f, %.2f)", t.getCenterX(), t.getCenterY()));
	}
	
}
