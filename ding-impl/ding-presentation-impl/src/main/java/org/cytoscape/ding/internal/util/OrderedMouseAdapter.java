package org.cytoscape.ding.internal.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.cytoscape.model.CyDisposable;

/**
 * The Swing API does not guarantee the order of listeners called when a mouse event fires.
 * 
 * It may be the case that they are called in the expected order, but that is implementation
 * dependent and not guaranteed by the API contract.
 * 
 * This class guarantees they are called in the expected order.
 */
public class OrderedMouseAdapter implements MouseListener, MouseMotionListener, CyDisposable {

	private final List<MouseAdapter> listeners = new ArrayList<>();
	
	private int pressedX = -1;
	private int pressedY = -1;
	private boolean dragged = false;
	

	public OrderedMouseAdapter(MouseAdapter ... adapters) {
		for(var a : adapters) {
			add(a);
		}
	}
	
	public void add(MouseAdapter mouseAdapter) {
		Objects.requireNonNull(mouseAdapter);
		listeners.add(mouseAdapter);
	}
	
	public <T> T get(Class<T> type) {
		for(var l : listeners) {
			if(type.isAssignableFrom(l.getClass())) {
				return type.cast(l);
			}
		}
		return null;
	}
	
	private void fire(MouseEvent e, BiConsumer<MouseAdapter,MouseEvent> consumer) {
		for(var l : listeners) {
			if(e.isConsumed())
				return;
			consumer.accept(l, e);
		}
	}
	
	
	@Override
	public void mousePressed(MouseEvent e) {
		pressedX = e.getX();
		pressedY = e.getY();
		dragged = false;
		fire(e, MouseAdapter::mousePressed);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		dragged = true;
		fire(e, MouseAdapter::mouseDragged);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		fire(e, MouseAdapter::mouseReleased);
		if(isVerySmallMove(e)) {
			fire(e, MouseAdapter::mouseClicked);
		}
		pressedX = -1;
		pressedY = -1;
		dragged = false;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		fire(e, MouseAdapter::mouseClicked);
	}

	private boolean isVerySmallMove(MouseEvent e) {
		return dragged
			&& pressedX >= 0
			&& pressedY >= 0
			&& Math.abs(pressedX - e.getX()) <= 1 
			&& Math.abs(pressedY - e.getY()) <= 1;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		fire(e, MouseAdapter::mouseEntered);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		fire(e, MouseAdapter::mouseExited);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		fire(e, MouseAdapter::mouseMoved);
	}

	@Override
	public void dispose() {
		listeners.clear();
	}

}
