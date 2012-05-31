package org.cytoscape.ding.impl.cyannotator.dialogs;

import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.TextAnnotation;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectColor extends JFrame {
	private ActionListener okListener;
	
	public SelectColor() {
		initComponents();
	}
    
	public SelectColor(Paint newColor){
		initComponents();
		jColorChooser1.setColor((Color)newColor);
	}    

	private void initComponents() {
		jColorChooser1 = new javax.swing.JColorChooser();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Select Color");
		setAlwaysOnTop(true);
		setResizable(false);
		getContentPane().setLayout(null);
		getContentPane().add(jColorChooser1);
		jColorChooser1.setBounds(0, 0, 429, 340);

		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});
		getContentPane().add(okButton);
		okButton.setBounds(280, 351, 47, 23);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});
		getContentPane().add(cancelButton);
		cancelButton.setBounds(345, 351, 75, 23);

		pack();
	}

	public void setOKListener(ActionListener l) {
		okListener = l;
	}

	public Color getColor() { return jColorChooser1.getColor(); }

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (okListener != null)
			okListener.actionPerformed(evt);
		dispose();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private javax.swing.JButton cancelButton;
	private javax.swing.JColorChooser jColorChooser1;
	private javax.swing.JButton okButton;

}

