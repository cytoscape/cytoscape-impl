/*
 * @(#)DialogFooter.java
 *
 * Copyright (c) 2008 Jeremy Wood. All Rights Reserved.
 *
 * You have a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) You do not utilize the software in a manner
 * which is disparaging to the original author.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. THE AUTHOR SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL THE
 * AUTHOR BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package org.cytoscape.internal.prefs.lib;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/** This is a row of buttons, intended to be displayed at the
 * bottom of a dialog.
 * <P>On the left are controls that should apply to the dialog itself,
 * such as "Help" button, or a "Reset Preferences" button.
 * On the far right are buttons that should dismiss this dialog.  They
 * may be presented in different orders on different platforms based
 * on the <code>reverseButtonOrder</code> boolean.
 * <P>Buttons are also generally normalized, so the widths of buttons
 * are equal.
 * 
 * <P>This object will "latch onto" the RootPane that contains it.  It is assumed
 * two DialogFooters will not be contained in the same RootPane. It is also
 * assumed the same DialogFooter will not be passed around to several
 * different RootPanes.
 * 
 * @version 1.01
 */
public class DialogFooter extends JPanel
{	
	private static final long serialVersionUID = 1L;

	/** This action calls <code>button.doClick()</code>. */
	public static class ClickAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		JButton button;
		
		public ClickAction(JButton button) {
			this.button = button;
		}
		public void actionPerformed(ActionEvent e) {
			button.doClick();
		}
	}
	
	public static final boolean isMac = System.getProperty("os.name").toLowerCase().indexOf("mac")!=-1;
	public static final boolean isVista = System.getProperty("os.name").toLowerCase().indexOf("vista")!=-1;
	
	/** This client property is used to impose a meta-shortcut to click a button.
	 * This should map to a Character.
	 */
	public static final String PROPERTY_META_SHORTCUT = "Dialog.meta.shortcut";
	
	/** This client property is used to indicate a button is "unsafe".  Apple
	 * guidelines state that "unsafe" buttons (such as "discard changes") should
	 * be several pixels away from "safe" buttons.
	 */
	public static final String PROPERTY_UNSAFE = "Dialog.Unsafe.Action";
	
	/** This indicates whether the dismiss controls should be displayed in reverse
	 * order.  When you construct a DialogFooter, the dismiss controls should be listed
	 * in order of priority (with the most preferred listed first, the least preferred last).
	 * If this boolean is false, then those components will be listed in that order.  If this is
	 * true, then those components will be listed in the reverse order.
	 * <P>By default on Mac this is true, because Macs put the default button on the right
	 * side of dialogs.  On all other platforms this is false by default.
	 * <P>Window's <A HREF="http://msdn.microsoft.com/en-us/library/ms997497.aspx">guidelines</A>
	 * advise to, "Position the most important button -- typically the default command --
	 * as the first button in the set."
	 */
	public static boolean reverseButtonOrder = isMac;
	
	protected JComponent[] leftControls;
	protected JComponent[] dismissControls;
	protected JComponent lastSelectedComponent;
	protected boolean autoClose = false;
	protected JButton defaultButton = null;
	protected JLabel errorLabel;
	public JLabel getErrorLabel(){	return errorLabel;	}
	
	private final ActionListener innerActionListener = new ActionListener() 
	{
		public void actionPerformed(ActionEvent e) 
		{
			lastSelectedComponent = (JComponent)e.getSource();
			fireActionListeners(e);

			if(autoClose)
				closeDialogAndDisposeAction.actionPerformed(e);
		}
	};
	
	/** Clones an array of JComponents */
	private static JComponent[] copy(JComponent[] c) 
	{
		JComponent[] newArray = new JComponent[c.length];
		for(int a = 0; a<c.length; a++) 
			newArray[a] = c[a];
		return newArray;
	}

	/** This addresses code that must involve the RootPane. */
	private final HierarchyListener hierarchyListener = new HierarchyListener() 
	{
		public void hierarchyChanged(HierarchyEvent e)
		{
			JRootPane root = SwingUtilities.getRootPane(DialogFooter.this);
			if(root==null) return;
			root.setDefaultButton(defaultButton);
			
			for(int a = 0; a<dismissControls.length; a++) 
				if(dismissControls[a] instanceof JButton) 
				{
					Character ch = (Character)dismissControls[a].getClientProperty(PROPERTY_META_SHORTCUT);
					if(ch!=null) 
					{
						KeyStroke keyStroke = KeyStroke.getKeyStroke(ch.charValue(), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put( keyStroke, keyStroke);
						root.getActionMap().put(keyStroke, new ClickAction((JButton)dismissControls[a]));
					}
				}
		}
	};
	
	/** Create a new <code>DialogFooter</code>.
	 * 
	 * @param leftControls the controls on the left side of this dialog, such as a help component, or a "Reset" button.
	 * @param dismissControls the controls on the right side of this dialog that should dismiss this dialog.  Also
	 * called "action" buttons.
	 * @param autoClose whether the dismiss buttons should automatically close the containing window.
	 * If this is <code>false</code>, then it is assumed someone else is taking care of closing/disposing the
	 * containing dialog
	 * @param defaultButton the optional button in <code>dismissControls</code> to make the default button in this dialog.
	 * (May be null.)
	 */
	public DialogFooter(JComponent[] leftControls, JComponent[] dismissControls, boolean autoClose, JButton defaultButton, boolean useError) 
	{
		super(new GridBagLayout());
		this.autoClose = autoClose;
		//this may be common:
		if(leftControls==null) leftControls = new JComponent[] {};
		//erg, this shouldn't be, but let's not throw an error because of it?
		if(dismissControls==null) dismissControls = new JComponent[] {};
		this.leftControls = copy(leftControls);
		this.dismissControls = copy(dismissControls);
		this.defaultButton = defaultButton;
//		if(leftControls==null) leftControls = new JComponent[] {};
//		if(dismissControls==null) dismissControls = new JComponent[] {};
		
		GridBagConstraints c = new GridBagConstraints();
		int buttonPadding = getButtonPadding();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 0; c.weighty = 1;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0,0,0,0);
		c.anchor = GridBagConstraints.CENTER;
		
		
		//Constructor flag adds on error label
		if(useError){
			c.anchor= GridBagConstraints.LINE_START;
			c.gridwidth=3;
			errorLabel = new JLabel();
			add(errorLabel,c);
			c.gridwidth=1;
			c.gridx++;
			add(Box.createRigidArea(new Dimension(0,20)),c); //force formatting for empty label
			c.gridy++;
			c.gridx--;
		}
		
		for(int a = 0; a<leftControls.length; a++) {
			add(leftControls[a],c);
			c.gridx++;
			c.insets = new Insets(0,0,0,buttonPadding);
		}
		c.weightx = 1;
		c.insets = new Insets(0,0,0,0);
		JPanel fluff = new JPanel();
		fluff.setOpaque(false);
		add(fluff,c); //fluff to enforce the left and right sides
		c.gridx++;
		c.weightx = 0;
		
		
		int unsafeCtr = 0;
		int safeCtr = 0;
		for(int a = 0; a<dismissControls.length; a++) {
			JComponent comp;
			if(reverseButtonOrder) {
				comp = (dismissControls[dismissControls.length-1-a]);
			} else {
				comp = (dismissControls[a]);
			}
			add(comp,c);
			
			if(isMac && isUnsafe(comp)) {
				c.insets = new Insets(0,24,0,0);
				if(comp instanceof JButton)
					unsafeCtr++;
			} else {
				c.insets = new Insets(0,buttonPadding,0,0);
				if(comp instanceof JButton)
					safeCtr++;
			}
			c.gridx++;
		}
		
		JButton[] unsafeButtons = new JButton[unsafeCtr];
		JButton[] safeButtons = new JButton[safeCtr];
		unsafeCtr = 0;
		safeCtr = 0;
		for(int a = 0; a<dismissControls.length; a++) {
			if(dismissControls[a] instanceof JButton) {
				if(isMac && isUnsafe(dismissControls[a])) {
					unsafeButtons[unsafeCtr++] = (JButton)dismissControls[a];
				} else {
					safeButtons[safeCtr++] = (JButton)dismissControls[a];
				}
			}
		}

		normalizeButtons(unsafeButtons);
		normalizeButtons(safeButtons);
		
		for(int a = 0; a<dismissControls.length; a++) {
			dismissControls[a].putClientProperty("dialog.footer.index", new Integer(a));
			if(dismissControls[a] instanceof JButton) {
				((JButton)dismissControls[a]).addActionListener(innerActionListener);
			} else 
			{
				try {
					Class cl = dismissControls[a].getClass();
					Method m = cl.getMethod("addActionListener", new Class[] {ActionListener.class});
					m.invoke(dismissControls[a], new Object[] {innerActionListener});
				} catch(Throwable t) {	}//do nothing
			}
		}
		
		addHierarchyListener(hierarchyListener);
	}

	public DialogFooter(JComponent[] leftControls2, JButton[] rightButtons, boolean autoClose2, JButton defaultButton2, boolean useError, boolean FIXME)
	{
		// TODO JAMIE --PLEASE FIX
	}

	/** This takes a set of buttons and gives them all the width/height
	 * of the largest button among them.
	 * <P>(More specifically, this sets the <code>preferredSize</code>
	 * of each button to the largest preferred size in the list of buttons.
	 * 
	 * @param buttons an array of buttons.
	 */
	public static void normalizeButtons(JButton[] buttons) {
		int maxWidth = 0;
		int maxHeight = 0;
		for(int a = 0; a<buttons.length; a++) {
			Dimension d = buttons[a].getPreferredSize();
			maxWidth = Math.max(d.width, maxWidth);
			maxHeight = Math.max(d.height, maxHeight);
		}
		for(int a = 0; a<buttons.length; a++) {
			buttons[a].setPreferredSize(new Dimension(maxWidth,maxHeight));
		}
	}
	public int getButtonPadding()
	{
		if(isMac)  		return 12;
		if(isVista) 	return 8;
		return 6;
	}
	
	private static boolean isUnsafe(JComponent c) {
		Boolean b = (Boolean)c.getClientProperty(PROPERTY_UNSAFE);
		if(b==null) b = Boolean.FALSE;
		return b.booleanValue();
	}
	
	private Vector<ActionListener> listeners;
	
	/** Adds an <code>ActionListener</code>.
	 * 
	 * @param l this listener will be notified when a <code>dismissControl</code> is activated.
	 */
	public void addActionListener(ActionListener l) {
		if(listeners==null) listeners = new Vector<ActionListener>();
		if(listeners.contains(l))
			return;
		listeners.add(l);
	}
	
	/** Removes an <code>ActionListener</code>.
	 */
	public void removeActionListener(ActionListener l) {
		if(listeners==null) return;
		listeners.remove(l);
	}
	
	private void fireActionListeners(ActionEvent e) {
		if(listeners==null) return;
		for(int a = 0; a<listeners.size(); a++) {
			ActionListener l = listeners.get(a);
			try {
				l.actionPerformed(e);
			} catch(Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	/** Returns the component last used to dismiss the dialog.
	 * <P>Note the components on the left side of this footer
	 * (such as a "Help" button or a "Reset Preferences" button)
	 * do NOT dismiss the dialog, and so this method has nothing
	 * to do with those components.  This only related to the components on the
	 * right side of dialog.
	 * @return the component last used to dismiss the dialog.
	 */
	public JComponent getLastSelectedComponent() {
		return lastSelectedComponent;
	}
	
	/** This resets the value of <code>lastSelectedComponent</code> to null.
	 * <P>If this footer is recycled in different dialogs, then you may
	 * need to nullify this value for <code>getLastSelectedComponent()</code>
	 * to remain relevant.
	 */
	public void reset() {
		lastSelectedComponent = null;
	}
	
	/** Returns a copy of the <code>dismissControls</code> array used to construct this footer. */
	public JComponent[] getDismissControls() {
		return copy(dismissControls);
	}

	/** Returns a copy of the <code>leftControls</code> array used to construct this footer. */
	public JComponent[] getLeftControls() {
		return copy(leftControls);
	}
	
	/** This action takes the Window associated with the source of this event,
	 * hides it, and then calls <code>dispose()</code> on it.
	 * <P>(This will not throw an exception if there is no parent window,
	 * but it does nothing in that case...)
	 */
	public static Action closeDialogAndDisposeAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			Component src = (Component)e.getSource();
			Window w = SwingUtilities.getWindowAncestor(src);
			if(w==null) return;
			
			w.setVisible(false);
			w.dispose();
		}
	};
}
