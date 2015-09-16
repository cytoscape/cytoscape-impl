package org.cytoscape.work.internal.tunables.utils;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.swing.AbstractGUITunableHandler.TunableFieldPanel;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


/**
 * This class is for setting an initial default size for the tunable text boxes.
 * @author rozagh
 */
public final class GUIDefaults {

	public static final String ICON_INFO = IconManager.ICON_INFO_CIRCLE;
	public static final String ICON_WARN = IconManager.ICON_EXCLAMATION_TRIANGLE;
	public static final String ICON_ERROR = IconManager.ICON_MINUS_CIRCLE;
	public static final String ICON_CANCELLED = IconManager.ICON_BAN;
	public static final String ICON_FINISHED = IconManager.ICON_CHECK;
	
	public static enum TaskIcon {
		INFO(LookAndFeelUtil.INFO_COLOR, ICON_INFO),
		WARN(LookAndFeelUtil.WARN_COLOR, ICON_WARN),
		ERROR(LookAndFeelUtil.ERROR_COLOR, ICON_ERROR),
		CANCELLED(LookAndFeelUtil.ERROR_COLOR, ICON_CANCELLED),
		FINISHED(LookAndFeelUtil.GO_COLOR, ICON_FINISHED),
		TASKS(UIManager.getColor("Label.foreground"), IconManager.ICON_LIST_UL);
		
		private final Color foreground;
		private final String text;

		private TaskIcon(final Color foreground, final String text) {
			this.foreground = foreground;
			this.text = text;
		}

		public Color getForeground() {
			return foreground != null ? foreground : UIManager.getColor("Label.foreground");
		}
		
		public String getText() {
			return text;
		}
	}
	
	public static final int TEXT_BOX_WIDTH = 150;
	
	public static final Map<String, URL> ICON_URLS = new HashMap<>();
	static {
		ICON_URLS.put("info", GUIDefaults.class.getResource("/images/info-icon.png"));
		ICON_URLS.put("warn", GUIDefaults.class.getResource("/images/warn-icon.png"));
		ICON_URLS.put("error", GUIDefaults.class.getResource("/images/error-icon.png"));
		ICON_URLS.put("finished", GUIDefaults.class.getResource("/images/finished-icon.png"));
		ICON_URLS.put("cancelled", GUIDefaults.class.getResource("/images/cancelled-icon.png"));
	}

	public static final Map<String, Icon> ICONS = new HashMap<>();
	static {
		for (final Map.Entry<String, URL> entry : ICON_URLS.entrySet()) {
			ICONS.put(entry.getKey(), new ImageIcon(entry.getValue()));
		}
	}
	
	public static String getIconText(final Level level) {
		switch (level) {
			case INFO:  return ICON_INFO;
			case WARN:  return ICON_WARN;
			case ERROR: return ICON_ERROR;
			default:    return null;
		}
	}
	
	public static Color getForeground(final Level level) {
		switch (level) {
			case INFO:  return LookAndFeelUtil.INFO_COLOR;
			case WARN:  return LookAndFeelUtil.WARN_COLOR;
			case ERROR: return LookAndFeelUtil.ERROR_COLOR;
			default:    return null;
		}
	}
	
	public static void updateFieldPanel(final JPanel p, final Component control, final boolean horizontalForm) {
		updateFieldPanel(p, new JLabel(" "), control, horizontalForm);
	}
	
	public static void updateFieldPanel(final JPanel p, JLabel label, final Component control,
			final boolean horizontalForm) {
		if (label == null)
			label = new JLabel(" ");
		
		if (horizontalForm)
			label.setHorizontalAlignment(JLabel.LEFT);
		else
			label.setHorizontalAlignment(JLabel.RIGHT);
		
		updateFieldPanel(p, (Component)label, control, horizontalForm);
	}
	
	public static void updateFieldPanel(final JPanel p, final JTextArea textArea, final Component control,
			final boolean horizontalForm) {
		if (textArea != null) {
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setOpaque(false);
			textArea.setBorder(null);
			textArea.setEditable(false);
			
			updateFieldPanel(p, (Component)textArea, control, horizontalForm);
		} else {
			updateFieldPanel(p, new JLabel(" "), control, horizontalForm);
		}
	}
	
	public static void setTooltip(String text, JComponent... components) {
		if (text == null)
			return;
		
		text = text.trim();
		
		if (text.isEmpty())
			return;
		
		final ToolTipManager tipManager = ToolTipManager.sharedInstance();
		tipManager.setInitialDelay(1);
		tipManager.setDismissDelay(7500);
		
		for (final JComponent c : components) {
			if (c != null)
				c.setToolTipText(text);
		}
	}
	
	private static void updateFieldPanel(final JPanel p, final Component label, final Component control,
			final boolean horizontalForm) {
		if (p instanceof TunableFieldPanel) {
			((TunableFieldPanel)p).setControl(control);
			
			if (label instanceof JLabel)
				((TunableFieldPanel)p).setLabel((JLabel)label);
			else if (label instanceof JTextArea)
				((TunableFieldPanel)p).setMultiLineLabel((JTextArea)label);
			
			return;
		}
		
		p.removeAll();
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		final Alignment vAlign = control instanceof JPanel || control instanceof JScrollPane ? 
				Alignment.LEADING : Alignment.CENTER;
		
		if (horizontalForm) {
			p.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(control, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(vAlign, false)
					.addComponent(label)
					.addComponent(control)
			);
		} else {
			p.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
			
			int w = Math.max(label.getPreferredSize().width, control.getPreferredSize().width);
			int gap = w - control.getPreferredSize().width; // So the label and control are centered
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(label, w, w, Short.MAX_VALUE)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addGroup(layout.createSequentialGroup()
									.addComponent(control, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addGap(gap, gap, Short.MAX_VALUE)
							)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(vAlign, false)
					.addComponent(label)
					.addComponent(control)
			);
		}
	}
	
	private GUIDefaults() {
	}
}
