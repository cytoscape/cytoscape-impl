package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

public class ColorButton extends JButton {

	private static final long serialVersionUID = -2587889131838377519L;
	
	private Color color;
	private Color borderColor;
	
	private static final JColorChooser colorChooser = new JColorChooser();

	public ColorButton(final Color color) {
		super(" ");
		setHorizontalTextPosition(JButton.CENTER);
		setVerticalTextPosition(JButton.CENTER);
		borderColor = getContrastingColor(getBackground());
		setIcon(new ColorIcon());
		setColor(color);
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Open color chooser
				final JDialog dialog = JColorChooser.createDialog(
						ColorButton.this,
						Messages.DI_SELECTCOLOR,
						true,
						colorChooser, 
						new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								final Color c = colorChooser.getColor();
								ColorButton.this.setColor(c);
							}
						}, null);
				dialog.setVisible(true);
			}
		});
	}

	public void setColor(final Color color) {
		final Color oldColor = this.color;
		this.color = color;
		repaint();
		firePropertyChange("color", oldColor, color);
	}
	
	public Color getColor() {
		return color;
	}
	
	class ColorIcon implements Icon {

		@Override
		public int getIconHeight() {
			return 16;
		}

		@Override
		public int getIconWidth() {
			return 44;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int w = getIconWidth();
			int h = getIconHeight();
			
			g.setColor(color);
			g.fillRect(x, y, w, h);
			g.setColor(borderColor);
			g.drawRect(x, y, w, h);
		}
	}
	
	private static Color getContrastingColor(final Color color) {
		int d = 0;
		// Counting the perceptive luminance - human eye favors green color...
		final double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

		if (a < 0.5)
			d = 0; // bright colors - black font
		else
			d = 255; // dark colors - white font

		return new Color(d, d, d);
	}
}
