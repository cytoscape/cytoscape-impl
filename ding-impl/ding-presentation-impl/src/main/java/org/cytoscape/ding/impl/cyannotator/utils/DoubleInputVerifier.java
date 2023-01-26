package org.cytoscape.ding.impl.cyannotator.utils;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class DoubleInputVerifier extends InputVerifier {

	@Override
	public boolean verify(JComponent input) {
        try {
            Double.parseDouble(((JTextField) input).getText().trim());
            return true; 
        } catch (NumberFormatException e) {
            return false;
        }
	}
}