package org.cytoscape.internal.actions;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

public class FullScreenAction extends AbstractCyAction {

	private static final long serialVersionUID = 2987814408730103803L;

	private static final String MENU_NAME = "Maximize Inner Desktop";
	protected static final boolean IS_MAC = System.getProperty("os.name").startsWith("Mac OS X");

	protected final CySwingApplication desktop;

	protected boolean inFullScreenMode = false;

	private final Set<CytoPanel> panels;
	private final Map<CytoPanel, CytoPanelState> states;
	private Rectangle lastBounds;

	public FullScreenAction(final CySwingApplication desktop) {
		this(desktop, MENU_NAME);
	}

	public FullScreenAction(final CySwingApplication desktop, final String menuName) {
		super(menuName);
		setPreferredMenu("View");
		setMenuGravity(5.1f);
		this.useCheckBoxMenuItem = true;
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask()));
		this.desktop = desktop;

		panels = new HashSet<CytoPanel>();
		states = new HashMap<CytoPanel, CytoPanelState>();
		panels.add(desktop.getCytoPanel(CytoPanelName.WEST));
		panels.add(desktop.getCytoPanel(CytoPanelName.EAST));
		panels.add(desktop.getCytoPanel(CytoPanelName.SOUTH));
		panels.add(desktop.getCytoPanel(CytoPanelName.SOUTH_WEST));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		toggle();
		inFullScreenMode = !inFullScreenMode;
	}

	protected void toggle() {
		if (inFullScreenMode) {
			desktop.getJToolBar().setVisible(true);
			desktop.getStatusToolBar().setVisible(true);
		} else {
			lastBounds = desktop.getJFrame().getBounds();
			desktop.getJToolBar().setVisible(false);
			desktop.getStatusToolBar().setVisible(false);
		}

		for (CytoPanel panel : panels) {
			final CytoPanelState curState = panel.getState();

			if (!inFullScreenMode) {
				// Save current State
				states.put(panel, curState);
				if (curState != CytoPanelState.HIDE)
					panel.setState(CytoPanelState.HIDE);
			} else {
				final CytoPanelState lastState = states.get(panel);
				panel.setState(lastState);
			}
		}

		if (!IS_MAC) {
			if (inFullScreenMode) {
				desktop.getJFrame().setBounds(lastBounds);
				desktop.getJFrame().setExtendedState(JFrame.NORMAL);
			} else
				desktop.getJFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
	}
}
