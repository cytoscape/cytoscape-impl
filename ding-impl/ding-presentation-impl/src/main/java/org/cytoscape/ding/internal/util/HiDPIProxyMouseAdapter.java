package org.cytoscape.ding.internal.util;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;

public class HiDPIProxyMouseAdapter implements MouseListener, MouseMotionListener {

	private AffineTransform defaultTransform = new AffineTransform();

	private final OrderedMouseAdapter delegateMouseListener;

	public HiDPIProxyMouseAdapter(OrderedMouseAdapter delegate) {
		this.delegateMouseListener = delegate;
	}

	public void setDefaultTransform(AffineTransform trans) {
		this.defaultTransform = trans == null ? new AffineTransform() : trans;
	}

	public <T> T get(Class<T> type) {
		return delegateMouseListener.get(type);
	}


	// This is side effecting, is that ok?
	private MouseEvent adjust(MouseEvent e) {
		var p = defaultTransform.transform(e.getPoint(), null);
		return new MouseEvent(
				(Component)e.getSource(), 
				e.getID(), 
				e.getWhen(), 
				e.getModifiersEx(), 
				(int)p.getX(), 
				(int)p.getY(), 
				e.getXOnScreen(), 
				e.getYOnScreen(), 
				e.getClickCount(), 
				e.isPopupTrigger(), 
				e.getButton());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		delegateMouseListener.mouseDragged(adjust(e));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		delegateMouseListener.mouseMoved(adjust(e));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		delegateMouseListener.mouseClicked(adjust(e));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		delegateMouseListener.mousePressed(adjust(e));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		delegateMouseListener.mouseReleased(adjust(e));
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		delegateMouseListener.mouseEntered(adjust(e));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		delegateMouseListener.mouseExited(adjust(e));
	}

}