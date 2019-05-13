package org.cytoscape.ding.internal.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * The Swing API does not guarantee the order of listeners called when a mouse event fires.
 * 
 * It may be the case that they are called in the expected order, but that is implementation
 * dependent and not guaranteed by the API contract.
 * 
 * This class guarantees they are called in the expected order.
 */
public class OrderedMouseAdapter implements MouseListener, MouseMotionListener {

	private final List<MouseAdapter> listeners = new ArrayList<>();
	
	public OrderedMouseAdapter() { 
	}

	public OrderedMouseAdapter(MouseAdapter ... adapters) {
		for(MouseAdapter a : adapters) {
			add(a);
		}
	}
	
	public void add(MouseAdapter mouseAdapter) {
		listeners.add(mouseAdapter);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		fire(e, MouseAdapter::mouseDragged);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		fire(e, MouseAdapter::mouseMoved);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		fire(e, MouseAdapter::mouseClicked);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		fire(e, MouseAdapter::mousePressed);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		fire(e, MouseAdapter::mouseReleased);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		fire(e, MouseAdapter::mouseEntered);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		fire(e, MouseAdapter::mouseExited);
	}

	
	private void fire(MouseEvent e, BiConsumer<MouseAdapter,MouseEvent> consumer) {
		for(MouseAdapter m : listeners) {
			if(e.isConsumed())
				return;
			consumer.accept(m, e);
		}
	}

}
