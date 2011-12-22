package org.cytoscape.ding.impl.visualproperty;

import java.awt.geom.Point2D;
import java.util.List;

import org.cytoscape.ding.Bend;
import org.cytoscape.ding.Handle;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.Range;

public class EdgeBendVisualProperty extends AbstractVisualProperty<Bend> {

	private static final Range<Bend> EDGE_BEND_RANGE;
	public static final Bend DEFAULT_EDGE_BEND = new DefaultBend();

	static {
		EDGE_BEND_RANGE = new ContinuousRange<Bend>(Bend.class, DEFAULT_EDGE_BEND, DEFAULT_EDGE_BEND, true, true);
	}

	public EdgeBendVisualProperty(Bend defaultValue, String id, String displayName) {
		super(defaultValue, EDGE_BEND_RANGE, id, displayName, CyEdge.class);
	}

	@Override
	public String toSerializableString(final Bend value) {
		return value.toString();
	}

	@Override
	public Bend parseSerializableString(String value) {
		// TODO: Implement parser for String representation of Bend.
		return DEFAULT_EDGE_BEND;
	}
	
	
	private static final class DefaultBend implements Bend {

		@Override
		public void setHandles(List<Point2D> bendPoints) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<Point2D> getHandles() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void moveHandle(int handleIndex, Point2D handlePosition) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Point2D getSourceHandlePoint() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Point2D getTargetHandlePoint() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addHandle(Point2D handlePosition) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addHandle(int handleIndex, Point2D handlePosition) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeHandle(Point2D handlePosition) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeHandle(int handleIndex) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeAllHandles() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean handleAlreadyExists(Point2D handlePosition) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void drawSelected() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawUnselected() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Point2D[] getDrawPoints() {
			// TODO Auto-generated method stub
			return null;
		}

		
	}


}
