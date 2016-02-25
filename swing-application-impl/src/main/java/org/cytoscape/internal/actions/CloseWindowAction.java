package org.cytoscape.internal.actions;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.internal.view.NetworkViewFrame;
import org.cytoscape.internal.view.NetworkViewMediator;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class CloseWindowAction extends AbstractCyAction {
	
	private final NetworkViewMediator netViewMediator;
	
	public CloseWindowAction(final float menuGravity, final NetworkViewMediator netViewMediator) {
		super("Close Window");
		this.netViewMediator = netViewMediator;
		
		setPreferredMenu("File");
		setMenuGravity(menuGravity);
		insertSeparatorBefore = true;
		
		final int CTRL = LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK :  InputEvent.CTRL_DOWN_MASK;
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_W, CTRL));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Window window = getActiveWindows();
		
		if (window != null && window instanceof NetworkViewFrame)
			netViewMediator.reattachNetworkView(((NetworkViewFrame) window).getNetworkView());
	}
	
	@Override
	public boolean isEnabled() {
		final Window window = getActiveWindows();
		
		return super.isEnabled() && window != null && window instanceof NetworkViewFrame;
	}
	
	private Window getActiveWindows() {
		final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		
		return keyboardFocusManager.getActiveWindow();
	}
}
