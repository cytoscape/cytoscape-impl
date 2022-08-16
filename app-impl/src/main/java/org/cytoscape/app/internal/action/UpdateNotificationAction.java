package org.cytoscape.app.internal.action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.app.internal.ui.AppManagerMediator;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class UpdateNotificationAction extends AbstractCyAction {
	final CyServiceRegistrar serviceRegistrar;
	private final BadgeIcon icon;

	private final DebounceTimer debounceTimer = new DebounceTimer(2000);

	private final UpdateManager updateManager;
	private final AppManagerMediator appManagerMediator;
	private static final String APP_MANAGER_DIR = "appManager/appmanager.html";
	private String url = null;

	public UpdateNotificationAction(
			AppManager appManager,
			UpdateManager updateManager,
			AppManagerMediator appManagerMediator,
			CyServiceRegistrar serviceRegistrar
	) {
		super("App Updates");
		this.updateManager = updateManager;
		this.appManagerMediator = appManagerMediator;
		this.serviceRegistrar = serviceRegistrar;

		icon = new BadgeIcon(serviceRegistrar.getService(IconManager.class));

		putValue(LARGE_ICON_KEY, icon);
		putValue(SHORT_DESCRIPTION, "App Updates");
		setIsInMenuBar(false);
		setIsInToolBar(true);
		setToolbarGravity(Float.MAX_VALUE);

		appManager.addAppListener(evt -> updateEnableState(true));
		updateManager.addUpdatesChangedListener(evt -> updateEnableState(false));
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		final CyApplicationConfiguration applicationCfg = serviceRegistrar.getService(CyApplicationConfiguration.class);
		String APP_MANAGER = "file:///" + (applicationCfg.getConfigurationDirectoryLocation()).toString() + "/" + APP_MANAGER_DIR;
		url = APP_MANAGER;

			CommandExecutorTaskFactory commandTF = serviceRegistrar.getService(CommandExecutorTaskFactory.class);
			TaskManager<?,?> taskManager = serviceRegistrar.getService(TaskManager.class);
			Map<String, Object> args = new HashMap<>();
			args.put("url",url);
			args.put("id","App Manager");
			args.put("title","App Manager");
			TaskIterator ti = commandTF.createTaskIterator("cybrowser","show",args, null);
			taskManager.execute(ti);
	}

	@Override
	public void updateEnableState() {
		setEnabled(false); // to force the component to repaint later if 'count' changes

		final int count = updateManager.getUpdateCount();
		final String text;

		if (count > 0)
			text = count + " update" + (count > 1 ? "s" : "") + " available!";
		else
			text = "All your apps are up-to-date.";

		putValue(LONG_DESCRIPTION, text);
		icon.setCount(count);
		setEnabled(count > 0); // this should force the UI to repaint because we disabled this action previously
	}

	public void updateEnableState(boolean checkForUpdates) {
		// Debounce the update events, because checkForUpdates() can be expensive!
		debounceTimer.debounce(() -> {
			if (checkForUpdates)
				updateManager.checkForUpdates();

			SwingUtilities.invokeLater(() -> updateEnableState());
		});
	}

	private static class BadgeIcon extends TextIcon {

		private static float ICON_FONT_SIZE = 24f;
		private static int ICON_SIZE = 32;
		private static int BADGE_BORDER_WIDTH = 1;
		private static Color BADGE_COLOR = Color.RED;
		private static Color BADGE_BORDER_COLOR = Color.WHITE;
		private static Color BADGE_TEXT_COLOR = Color.WHITE;
		private static Color ICON_COLOR = UIManager.getColor("CyColor.complement(-2)");

		private int count;
		private final IconManager iconManager;

		public BadgeIcon(IconManager iconManager) {
			super(IconManager.ICON_BELL, iconManager.getIconFont(ICON_FONT_SIZE), ICON_COLOR, ICON_SIZE, ICON_SIZE);
			this.iconManager = iconManager;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);

			if (!c.isEnabled() || count <= 0) // Only draw a badge if there are notifications!
				return;

			Graphics2D g2d = (Graphics2D) g.create();

			RenderingHints hints = new RenderingHints(null);
			hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHints(hints);

			int w = getIconWidth();
			int h = getIconHeight();

			// Position the badge in the top-right quadrant of the icon
			float d = Math.max(w, h) / 1.75f; // diameter
			float di = d - 2 * BADGE_BORDER_WIDTH; // diameter of the internal circle (i.e. circle - border)
			float bx = x + w - d; // x of badge's upper left corner
			float by = y; // y of badge's upper left corner

			// Draw badge circle
			g2d.setColor(BADGE_BORDER_COLOR);
			g2d.fillOval(Math.round(bx), Math.round(by), Math.round(d), Math.round(d));
			g2d.setColor(BADGE_COLOR);
			g2d.fillOval(Math.round(bx + BADGE_BORDER_WIDTH), Math.round(by + BADGE_BORDER_WIDTH), Math.round(di), Math.round(di));

			// Draw badge count text inside the circle.
			String text = count > 99 ? IconManager.ICON_ELLIPSIS_H : "" + count; // just draw ELLIPSIS char if more than 2 digits

			float hr = (float) Math.sqrt((di * di) / 2.0f); // height of square inside internal circle (Pythagoras)
			float th = hr; // text height
			float tw = 0; // text width
			Font textFont = count > 99 ? iconManager.getIconFont(h)
					: UIManager.getFont("Label.font").deriveFont(Font.BOLD);
			textFont = getFont(textFont, th, g2d);

			g2d.setFont(textFont);
			g2d.setColor(BADGE_TEXT_COLOR);

			FontMetrics fm = g2d.getFontMetrics();
			th = fm.getHeight();
			tw = fm.stringWidth(text);

			float tx = bx + (d - hr) / 2.0f;
			tx += (hr - tw) / 2.0f;

			float ty = by + (d - hr) / 2.0f;
			ty += ((hr - th) / 2.0f) + fm.getAscent();

			g2d.drawString(text, tx, ty);

			g2d.dispose();
		}

		/**
		 * Sets the count value to display.
		 */
		public void setCount(int count) {
			this.count = count;
		}

		private static Font getFont(Font f, float height, Graphics g) {
			float size = height;
			Boolean up = null;

			while (true) {
				Font font = f.deriveFont(size);
				int testHeight = g.getFontMetrics(font).getHeight();

				if (testHeight < height && up != Boolean.FALSE) {
					size++;
					up = Boolean.TRUE;
				} else if (testHeight > height && up != Boolean.TRUE) {
					size--;
					up = Boolean.FALSE;
				} else {
					return font;
				}
			}
		}
	}
}
