package org.cytoscape.internal.view.util;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * ToolbarWithOverflow provides a component which is useful for displaying commonly used
 * actions.  It adds an overflow button when the toolbar becomes too small to show all the
 * available actions.
 * <br>
 * Adapted from the original org.openide.awt.ToolbarWithOverflow--basically, just removed the org.openide.util.Mutex
 * and other NetBeans specific dependencies, besides some minor UI modifications.
 * 
 * @author Th. Oikonomou
 * @since 7.51
 */
@SuppressWarnings("serial")
public class ToolbarWithOverflow extends JToolBar {

	private JButton overflowButton;
	private JPopupMenu popup;
	private JToolBar overflowToolbar;
	
	private boolean displayOverflowOnHover = true;
	
	private final String PROP_DRAGGER = "_toolbar_dragger_"; // NOI18N
	
	private AWTEventListener awtEventListener;
	private ComponentAdapter componentAdapter;
	
	/**
	 *  keep track of the overflow popup that is showing, possibly from another overflow button,
	 *  in order to hide it if necessary
	 */
	private static JPopupMenu showingPopup;

	/**
	 * Creates a new tool bar; orientation defaults to <code>HORIZONTAL</code>.
	 */
	public ToolbarWithOverflow() {
		this(HORIZONTAL);
	}

	/**
	 * Creates a new tool bar with the specified <code>orientation</code>. The
	 * <code>orientation</code> must be either <code>HORIZONTAL</code> or <code>VERTICAL</code>.
	 *
	 * @param orientation the orientation desired
	 */
	public ToolbarWithOverflow(int orientation) {
		this(null, orientation);
	}

	/**
	 * Creates a new tool bar with the specified <code>name</code>. The name is used
	 * as the title of the undocked tool bar. The default orientation is <code>HORIZONTAL</code>.
	 *
	 * @param name the name of the tool bar
	 */
	public ToolbarWithOverflow(String name) {
		this(name, HORIZONTAL);
	}

	/**
	 * Creates a new tool bar with a specified <code>name</code> and
	 * <code>orientation</code>. All other constructors call this constructor.
	 * If <code>orientation</code> is an invalid value, an exception will be thrown.
	 *
	 * @param name        the name of the tool bar
	 * @param orientation the initial orientation -- it must be * either <code>HORIZONTAL</code> or <code>VERTICAL</code>
	 * @exception IllegalArgumentException if orientation is neither <code>HORIZONTAL</code> nor <code>VERTICAL</code>
	 */
	public ToolbarWithOverflow(String name, int orientation) {
		super(name, orientation);
		
		setupOverflowButton();
		popup = new JPopupMenu();
		popup.setBorderPainted(false);
		popup.setBorder(BorderFactory.createEmptyBorder());
		overflowToolbar = new JToolBar("overflowToolbar", orientation == HORIZONTAL ? VERTICAL : HORIZONTAL);
		overflowToolbar.setFloatable(false);
		overflowToolbar.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1));
	}

	private ComponentListener getComponentListener() {
		if (componentAdapter == null) {
			componentAdapter = new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					maybeAddOverflow();
				}
			};
		}
		
		return componentAdapter;
	}

	private AWTEventListener getAWTEventListener() {
		if (awtEventListener == null) {
			awtEventListener = new AWTEventListener() {
				@Override
				public void eventDispatched(AWTEvent event) {
					MouseEvent e = (MouseEvent) event;
					
					if (isVisible() && !isShowing() && popup.isShowing()) {
						showingPopup = null;
						popup.setVisible(false);
						return;
					}
					
					if (event.getSource() == popup) {
						if (popup.isShowing() && e.getID() == MouseEvent.MOUSE_EXITED) {
							int minX = popup.getLocationOnScreen().x;
							int maxX = popup.getLocationOnScreen().x + popup.getWidth();
							int minY = popup.getLocationOnScreen().y;
							int maxY = popup.getLocationOnScreen().y + popup.getHeight();
							
							if (e.getXOnScreen() < minX || e.getXOnScreen() >= maxX || e.getYOnScreen() < minY
									|| e.getYOnScreen() >= maxY) {
								showingPopup = null;
								popup.setVisible(false);
							}
						}
					} else {
						if (popup.isShowing() && overflowButton.isShowing()
								&& (e.getID() == MouseEvent.MOUSE_MOVED || e.getID() == MouseEvent.MOUSE_EXITED)) {
							int minX = overflowButton.getLocationOnScreen().x;
							int maxX = getOrientation() == HORIZONTAL ? minX + popup.getWidth()
									: minX + overflowButton.getWidth() + popup.getWidth();
							int minY = overflowButton.getLocationOnScreen().y;
							int maxY = getOrientation() == HORIZONTAL
									? minY + overflowButton.getHeight() + popup.getHeight()
									: minY + popup.getHeight();
							
							if (e.getXOnScreen() < minX || e.getYOnScreen() < minY || e.getXOnScreen() > maxX
									|| e.getYOnScreen() > maxY) {
								showingPopup = null;
								popup.setVisible(false);
							}
						}
					}
				}
			};
		}
		
		return awtEventListener;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		
		addComponentListener(getComponentListener());
		Toolkit.getDefaultToolkit().addAWTEventListener(getAWTEventListener(),
				AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		
		if (componentAdapter != null)
			removeComponentListener(componentAdapter);
		
		if (awtEventListener != null)
			Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
	}

	/**
	 * Returns whether the overflow should be displayed on hover or not. The default value is <code>true</code>.
	 *
	 * @return <code>true</code> if overflow is displayed on hover;
	 *         <code>false</code> otherwise
	 */
	public boolean isDisplayOverflowOnHover() {
		return displayOverflowOnHover;
	}

	/**
	 * Sets whether the overflow should be displayed on hover or not. The default value is <code>true</code>.
	 *
	 * @param displayOverflowOnHover if <code>true</code>, the overflow will be
	 *                               displayed on hover; <code>false</code>
	 *                               otherwise
	 */
	public void setDisplayOverflowOnHover(boolean displayOverflowOnHover) {
		this.displayOverflowOnHover = displayOverflowOnHover;
		setupOverflowButton();
	}

	@Override
	public Dimension getPreferredSize() {
		var comps = getAllComponents();
		var insets = getInsets();
		int width = null == insets ? 0 : insets.left + insets.right;
		int height = null == insets ? 0 : insets.top + insets.bottom;
		
		for (int i = 0; i < comps.length; i++) {
			var c = comps[i];
			
			if (!c.isVisible())
				continue;
			
			width += getOrientation() == HORIZONTAL ? c.getPreferredSize().width : c.getPreferredSize().height;
			height = Math.max(height,
					(getOrientation() == HORIZONTAL
							? (c.getPreferredSize().height + (insets == null ? 0 : insets.top + insets.bottom))
							: (c.getPreferredSize().width) + (insets == null ? 0 : insets.left + insets.right)));
		}
			
		if (overflowToolbar.getComponentCount() > 0)
			width += getOrientation() == HORIZONTAL ? overflowButton.getPreferredSize().width
					: overflowButton.getPreferredSize().height;
		
		var dim = getOrientation() == HORIZONTAL ? new Dimension(width, height) : new Dimension(height, width);
		
		return dim;
	}

	@Override
	public void setOrientation(int o) {
		super.setOrientation(o);
		setupOverflowButton();
	}

	@Override
	public void removeAll() {
		super.removeAll();
		overflowToolbar.removeAll();
	}

	@Override
	public void validate() {
		int visibleButtons = computeVisibleButtons();
		
		if (visibleButtons == -1)
			handleOverflowRemoval();
		else
			handleOverflowAddittion(visibleButtons);
		
		super.validate();
	}

	private void setupOverflowButton() {
		overflowButton = new JButton(getOrientation() == HORIZONTAL ? ToolbarArrowIcon.INSTANCE_VERTICAL
				: ToolbarArrowIcon.INSTANCE_HORIZONTAL);
		overflowButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

		overflowButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (popup.isShowing()) {
					showingPopup = null;
					popup.setVisible(false);
				} else {
					displayOverflow();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (showingPopup != null && showingPopup != popup) {
					showingPopup.setVisible(false);
					showingPopup = null;
				}
				
				if (displayOverflowOnHover)
					displayOverflow();
			}
		});
	}

	private void displayOverflow() {
		if (!overflowButton.isShowing())
			return;
		
		int x = getOrientation() == HORIZONTAL ? overflowButton.getLocationOnScreen().x
				: overflowButton.getLocationOnScreen().x + overflowButton.getWidth();
		int y = getOrientation() == HORIZONTAL ? overflowButton.getLocationOnScreen().y + overflowButton.getHeight()
				: overflowButton.getLocationOnScreen().y;
		popup.setLocation(x, y);
		showingPopup = popup;
		popup.setVisible(true);
	}

	/**
	 * Determines if an overflow button should be added to or removed from the toolbar.
	 */
	private void maybeAddOverflow() {
		validate();
		repaint();
	}

	private int computeVisibleButtons() {
		if (isShowing()) {
			int w = getOrientation() == HORIZONTAL ? overflowButton.getIcon().getIconWidth() + 4
					: getWidth() - getInsets().left - getInsets().right;
			int h = getOrientation() == HORIZONTAL ? getHeight() - getInsets().top - getInsets().bottom
					: overflowButton.getIcon().getIconHeight() + 4;
			overflowButton.setMaximumSize(new Dimension(w, h));
			overflowButton.setMinimumSize(new Dimension(w, h));
			overflowButton.setPreferredSize(new Dimension(w, h));
		}
		
		var comps = getAllComponents();
		int sizeSoFar = 0;
		int maxSize = getOrientation() == HORIZONTAL ? getWidth() : getHeight();
		int overflowButtonSize = getOrientation() == HORIZONTAL ? overflowButton.getPreferredSize().width
				: overflowButton.getPreferredSize().height;
		int showingButtons = 0; // all that return true from isVisible()
		int visibleButtons = 0; // all visible that fit into the given space (maxSize)
		var insets = getInsets();
		
		if (null != insets)
			sizeSoFar = getOrientation() == HORIZONTAL ? insets.left + insets.right : insets.top + insets.bottom;
		
		for (int i = 0; i < comps.length; i++) {
			Component comp = comps[i];
			
			if (!comp.isVisible())
				continue;
			
			if (showingButtons == visibleButtons) {
				int size = getOrientation() == HORIZONTAL ? comp.getPreferredSize().width
						: comp.getPreferredSize().height;
				
				if (sizeSoFar + size <= maxSize) {
					sizeSoFar += size;
					visibleButtons++;
				}
			}
			
			showingButtons++;
		}
		
		if (visibleButtons < showingButtons && visibleButtons > 0 && sizeSoFar + overflowButtonSize > maxSize)
			visibleButtons--; // overflow button needed but would not have enough space, remove one more button
		
		if (visibleButtons == 0 && comps.length > 0 && comps[0] instanceof JComponent
				&& Boolean.TRUE.equals(((JComponent) comps[0]).getClientProperty(PROP_DRAGGER)))
			visibleButtons = 1; // always include the dragger if present
		
		if (visibleButtons == showingButtons)
			visibleButtons = -1;
		
		return visibleButtons;
	}

	private void handleOverflowAddittion(int visibleButtons) {
		var comps = getAllComponents();
		removeAll();
		overflowToolbar.setOrientation(getOrientation() == HORIZONTAL ? VERTICAL : HORIZONTAL);
		popup.removeAll();

		for (Component comp : comps) {
			if (visibleButtons > 0) {
				add(comp);

				if (comp.isVisible())
					visibleButtons--;
			} else {
				overflowToolbar.add(comp);
			}
		}
		
		popup.add(overflowToolbar);
		add(overflowButton);
	}

	private void handleOverflowRemoval() {
		if (overflowToolbar.getComponents().length > 0) {
			remove(overflowButton);
			
			for (Component comp : overflowToolbar.getComponents())
				add(comp);
			
			overflowToolbar.removeAll();
			popup.removeAll();
		}
	}

	private Component[] getAllComponents() {
		final Component[] toolbarComps;
		Component[] overflowComps = overflowToolbar.getComponents();
		
		if (overflowComps.length == 0) {
			toolbarComps = getComponents();
		} else {
			if (getComponentCount() > 0) {
				toolbarComps = new Component[getComponents().length - 1];
				System.arraycopy(getComponents(), 0, toolbarComps, 0, toolbarComps.length);
			} else {
				toolbarComps = new Component[0];
			}
		}
		
		Component[] comps = new Component[toolbarComps.length + overflowComps.length];
		System.arraycopy(toolbarComps, 0, comps, 0, toolbarComps.length);
		System.arraycopy(overflowComps, 0, comps, toolbarComps.length, overflowComps.length);
		
		return comps;
	}

	/**
	 * Vectorized version of {@code toolbar_arrow_horizontal.png} and {@code toolbar_arrow_vertical.png}.
	 */
	private static final class ToolbarArrowIcon extends VectorIcon {
		
		public static final Icon INSTANCE_HORIZONTAL = new ToolbarArrowIcon(true);
		public static final Icon INSTANCE_VERTICAL = new ToolbarArrowIcon(false);
		private final boolean horizontal;

		private ToolbarArrowIcon(boolean horizontal) {
			super(11, 11);
			this.horizontal = horizontal;
		}

		@Override
		protected void paintIcon(Component c, Graphics2D g, int width, int height, double scaling) {
			if (horizontal) // Rotate 90 degrees counterclockwise.
				g.rotate(-Math.PI / 2.0, width / 2.0, height / 2.0);

			// Draw two chevrons pointing downwards. Make strokes a little thicker at low scalings.
			double strokeWidth = 0.8 * scaling + 0.3;
			g.setStroke(new BasicStroke((float) strokeWidth));
			var color  = UIManager.getColor("Label.foreground");
			g.setColor(color);

			for (int i = 0; i < 2; i++) {
				final int y = round((1.4 + 4.1 * i) * scaling);
				final double arrowWidth = round(5.0 * scaling);
				final double arrowHeight = round(3.0 * scaling);
				final double marginX = (width - arrowWidth) / 2.0;
				final double arrowMidX = marginX + arrowWidth / 2.0;
				// Clip the top of the chevrons.
				g.clipRect(0, y, width, height);
				Path2D.Double arrowPath = new Path2D.Double();
				arrowPath.moveTo(arrowMidX - arrowWidth / 2.0, y);
				arrowPath.lineTo(arrowMidX, y + arrowHeight);
				arrowPath.lineTo(arrowMidX + arrowWidth / 2.0, y);
				g.draw(arrowPath);
			}
		}
	}
	
	/**
	 * A scalable icon that can be drawn at any resolution, for use with HiDPI displays. Implementations
	 * will typically use hand-crafted painting code that may take special care to align graphics to
	 * device pixels, and which may perform small tweaks to make the icon look good at all resolutions.
	 * The API of this class intends to make this straightforward.
	 *
	 * <p>HiDPI support now exists on MacOS, Windows, and Linux. On MacOS, scaling is 200% for Retina
	 * displays, while on Windows 10, the "Change display settings" panel provides the options 100%,
	 * 125%, 150%, 175%, 200%, and 225%, as well as the option to enter an arbitrary scaling factor.
	 * Non-integral scaling factors can lead to various alignment problems that makes otherwise
	 * well-aligned icons look unsharp; this class takes special care to avoid such problems.
	 *
	 * <p>Hand-crafted painting code is a good design choice for icons that are simple, ubiqutious in
	 * the UI (e.g. part of the Look-and-Feel), or highly parameterized. Swing's native Windows L&amp;F
	 * uses this approach for many of its basic icons; see
	 * {@link com.sun.java.swing.plaf.windows.WindowsIconFactory}.
	 *
	 * <p>When developing new icons, or adjusting existing ones, use the {@code VectorIconTester}
	 * utility found in
	 * {@code o.n.swing.tabcontrol/test/unit/src/org/netbeans/swing/tabcontrol/plaf/VectorIconTester.java}
	 * to preview and compare icons at different resolutions.
	 *
	 * @since 9.12
	 * @author Eirik Bakke
	 */
	private static abstract class VectorIcon implements Icon, Serializable {

		private final int width;
		private final int height;

		protected VectorIcon(int width, int height) {
			if (width < 0 || height < 0)
				throw new IllegalArgumentException();

			this.width = width;
			this.height = height;
		}

		@Override
		public final int getIconWidth() {
			return width;
		}

		@Override
		public final int getIconHeight() {
			return height;
		}

		private static Graphics2D createGraphicsWithRenderingHintsConfigured(Graphics basedOn) {
			Graphics2D ret = (Graphics2D) basedOn.create();
			Object desktopHints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
			var hints = new LinkedHashMap<Object, Object>();

			if (desktopHints != null && desktopHints instanceof Map<?, ?>)
				hints.putAll((Map<?, ?>) desktopHints);

			/*
			 * Enable antialiasing by default. Adding this is required in order to get
			 * non-text antialiasing on Windows.
			 */
			hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			/*
			 * In case a subclass decides to render text inside an icon, standardize the
			 * text antialiasing setting as well. Don't try to follow the editor's
			 * anti-aliasing setting, or to do subpixel rendering. It's more important that
			 * icons render in a predictable fashion, so the icon designer can get can
			 * review the appearance at design time.
			 */
			hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// Make stroke behavior as predictable as possible.
			hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			ret.addRenderingHints(hints);
			
			return ret;
		}

		/**
		 * Selectively enable or disable antialiasing during painting. Certain shapes
		 * may look slightly better without antialiasing, e.g. entirely regular diagonal
		 * lines in very small icons when there is no HiDPI scaling. Text antialiasing
		 * is unaffected by this setting.
		 *
		 * @param g       the graphics to set antialiasing setting for
		 * @param enabled whether antialiasing should be enabled or disabled
		 */
		protected static final void setAntiAliasing(Graphics2D g, boolean enabled) {
			var hints = new LinkedHashMap<Object, Object>();
			hints.put(RenderingHints.KEY_ANTIALIASING,
					enabled ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			g.addRenderingHints(hints);
		}

		protected static final int round(double d) {
			int ret = (int) Math.round(d);
			return d > 0 && ret == 0 ? 1 : ret;
		}

		@Override
		public final void paintIcon(Component c, Graphics g0, int x, int y) {
			var g2 = createGraphicsWithRenderingHintsConfigured(g0);

			try {
				// Make sure the subclass can't paint outside its stated dimensions.
				g2.clipRect(x, y, getIconWidth(), getIconHeight());
				g2.translate(x, y);
				/**
				 * On HiDPI monitors, the Graphics object will have a default transform that
				 * maps logical pixels, like those you'd pass to Graphics.drawLine, to a higher
				 * number of device pixels on the screen. For instance, painting a line 10
				 * pixels long on the current Graphics object would actually produce a line 20
				 * device pixels long on a MacOS retina screen, which has a DPI scaling factor
				 * of 2.0. On Windows 10, many different scaling factors may be encountered,
				 * including non-integral ones such as 1.5. Detect the scaling factor here so we
				 * can use it to inform the drawing routines.
				 */
				final double scaling;
				final AffineTransform tx = g2.getTransform();
				int txType = tx.getType();
				
				if (txType == AffineTransform.TYPE_UNIFORM_SCALE
						|| txType == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_TRANSLATION)) {
					scaling = tx.getScaleX();
				} else {
					// Unrecognized transform type. Don't do any custom scaling handling.
					paintIcon(c, g2, getIconWidth(), getIconHeight(), 1.0);
					return;
				}
				
				/*
				 * When using a non-integral scaling factor, such as 175%, preceding Swing
				 * components often end up being a non-integral number of device pixels tall or
				 * wide. This will cause our initial position to be "off the grid" with respect
				 * to device pixels, causing blurry graphics even if we subsequently take care
				 * to use only integral numbers of device pixels during painting. Fix this here
				 * by consuming a little bit of the top and left of the icon's dimensions to offset any error.
				 */
				// The initial position, in device pixels.
				final double previousDevicePosX = tx.getTranslateX();
				final double previousDevicePosY = tx.getTranslateY();
				/*
				 * The new, aligned position, after a small portion of the icon's dimensions may
				 * have been consumed to correct it.
				 */
				final double alignedDevicePosX = Math.ceil(previousDevicePosX);
				final double alignedDevicePosY = Math.ceil(previousDevicePosY);
				// Use the aligned position.
				g2.setTransform(new AffineTransform(1, 0, 0, 1, alignedDevicePosX, alignedDevicePosY));
				/*
				 * The portion of the icon's dimensions that was consumed to correct any initial
				 * translation misalignment, in device pixels. May be zero.
				 */
				final double transDeviceAdjX = alignedDevicePosX - previousDevicePosX;
				final double transDeviceAdjY = alignedDevicePosY - previousDevicePosY;
				/*
				 * Now calculate the dimensions available for painting, also aligned to an
				 * integral number of device pixels.
				 */
				final int deviceWidth = (int) Math.floor(getIconWidth() * scaling - transDeviceAdjX);
				final int deviceHeight = (int) Math.floor(getIconHeight() * scaling - transDeviceAdjY);
				paintIcon(c, g2, deviceWidth, deviceHeight, scaling);
			} finally {
				g2.dispose();
			}
		}

	    /**
	     * Paint the icon at the given width and height. The dimensions given are the device pixels onto
	     * which the icon must be drawn after it has been scaled up from its originally constant logical
	     * dimensions and aligned onto the device pixel grid. Painting onto the supplied
	     * {@code Graphics2D} instance using whole number coordinates (for horizontal and vertical
	     * lines) will encourage sharp and well-aligned icons.
	     *
	     * <p>The icon should be painted with its upper left-hand corner at position (0, 0). Icons need
	     * not be opaque. Due to rounding errors and alignment correction, the aspect ratio of the
	     * device dimensions supplied here may not be exactly the same as that of the logical pixel
	     * dimensions specified in the constructor.
	     *
	     * @param c may be used to get properties useful for painting, as in
	     *        {@link Icon#paintIcon(Component,Graphics,int,int)}
	     * @param width the target width of the icon, after scaling and alignment adjustments, in device
	     *        pixels
	     * @param height the target height of the icon, after scaling and alignment adjustments, in
	     *        device pixels
	     * @param scaling the scaling factor that was used to scale the icon dimensions up to their
	     *        stated value
	     * @param g need <em>not</em> be cleaned up or restored to its previous state after use; will
	     *        have anti-aliasing already enabled by default
	     */
		protected abstract void paintIcon(Component c, Graphics2D g, int width, int height, double scaling);
	}
}
