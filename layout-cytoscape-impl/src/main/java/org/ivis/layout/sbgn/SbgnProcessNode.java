package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.util.IGeometry;
import org.ivis.util.PointD;

public class SbgnProcessNode extends SbgnPDNode
{
	protected SbgnPDNode parentCompound;
	protected SbgnPDNode inputPort;
	protected SbgnPDNode outputPort;

	/**
	 * Current orientation of the process
	 */
	protected Orientation orientation;

	/**
	 * Indicates if the process is eligible for rotation (90 - 180) (or not)
	 */
	private RotationPriority rotationPriority;

	/**
	 * remember the ideal edge length in the layout algorithm/quick access
	 */
	private double idealEdgeLength;

	public ArrayList<SbgnPDEdge> consumptionEdges;
	public ArrayList<SbgnPDEdge> productEdges;
	public ArrayList<SbgnPDEdge> effectorEdges;

	/**
	 * This variable stores the values of the perpendicular components of the
	 * forces acting on port nodes. (90-degree rotation)
	 */
	protected double netRotationalForce;

	/**
	 * Total number of properly oriented edges of this process.
	 */
	public double properEdgeCount;

	public SbgnProcessNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		this.netRotationalForce = 0;

		consumptionEdges = new ArrayList<SbgnPDEdge>();
		productEdges = new ArrayList<SbgnPDEdge>();
		effectorEdges = new ArrayList<SbgnPDEdge>();
	}

	public SbgnProcessNode(LGraphManager gm, Point loc, Dimension size,
			LNode vNode, String type)
	{
		super(gm, loc, size, vNode, type);
		this.netRotationalForce = 0;
		consumptionEdges = new ArrayList<SbgnPDEdge>();
		productEdges = new ArrayList<SbgnPDEdge>();
		effectorEdges = new ArrayList<SbgnPDEdge>();
	}

	/**
	 * Connect the port node to its process node (parent) and connect the edges
	 * of neighbor nodes to the port node by considering their types (for both
	 * input port and output port)
	 */
	public void reconnectEdges(double idealEdgeLength)
	{
		this.idealEdgeLength = idealEdgeLength;
		// change connections from process node&neighbors to port&neighbors.
		for (int i = 0; i < this.getEdges().size(); i++)
		{
			SbgnPDEdge sEdge = (SbgnPDEdge) this.getEdges().get(i);

			if (sEdge.type.equals(SbgnPDConstants.CONSUMPTION))
			{
				this.getEdges().remove(sEdge);

				sEdge.setTarget(inputPort);
				inputPort.getEdges().add(sEdge);
				i--;
			}
			else if (sEdge.type.equals(SbgnPDConstants.PRODUCTION))
			{
				this.getEdges().remove(sEdge);

				sEdge.setSource(outputPort);
				outputPort.getEdges().add(sEdge);
				i--;
			}
			else if (sEdge.isEffector())
			{
				this.getEdges().remove(sEdge);

				sEdge.setTarget(this.parentCompound);
				this.parentCompound.getEdges().add(sEdge);
				i--;
			}
		}
	}

	public void connectNodes(SbgnPDNode parentCompound, SbgnPDNode inputPort,
			SbgnPDNode outputPort)
	{
		this.parentCompound = parentCompound;
		this.parentCompound.isDummyCompound = true;
		this.inputPort = inputPort;
		this.outputPort = outputPort;
		this.orientation = Orientation.LEFT_TO_RIGHT;

		// initial placement. place input to the left of the process node,
		// output to the right
		outputPort.setCenter(this.getCenterX()
				+ SbgnPDConstants.RIGID_EDGE_LENGTH, this.getCenterY());
		inputPort.setCenter(this.getCenterX()
				- SbgnPDConstants.RIGID_EDGE_LENGTH, this.getCenterY());
	}

	/**
	 * Check if the process is eligible for rotation. First check if a
	 * 180-degree is possible (as it is more critical).
	 */
	public boolean isRotationNecessary()
	{
		// normalize the amount (per iteration)
		netRotationalForce /= (SbgnPDConstants.ROTATIONAL_FORCE_ITERATION_COUNT * (this.consumptionEdges
				.size() + this.productEdges.size() + this.effectorEdges.size()));

		if (isSwapAvailable())
		{
			rotationPriority = RotationPriority.SWAP;
			return true;
		}
		else if (Math.abs(netRotationalForce) > SbgnPDConstants.ROTATION_90_DEGREE)
		{
			rotationPriority = RotationPriority.NINETY_DEGREE;
			return true;
		}
		else
		{
			rotationPriority = RotationPriority.NO_ROTATION;
			return false;
		}
	}

	/**
	 * If the percentage of obtuse angles exceeds the threshold, swap is
	 * required.
	 */
	private boolean isSwapAvailable()
	{
		double obtuseAngleCnt = 0.0;
		double acuteAngleCnt = 0.0;

		for (SbgnPDEdge edge : consumptionEdges)
		{
			if (Math.abs(edge.correspondingAngle) > 90)
				obtuseAngleCnt++;
			else
				acuteAngleCnt++;
		}

		for (SbgnPDEdge edge : productEdges)
		{
			if (Math.abs(edge.correspondingAngle) > 90)
				obtuseAngleCnt++;
			else
				acuteAngleCnt++;
		}

		if (obtuseAngleCnt / (obtuseAngleCnt + acuteAngleCnt) > SbgnPDConstants.ROTATION_180_DEGREE)
			return true;
		else
			return false;
	}

	/**
	 * This method rotates the associated compound (and its children: process
	 * and ports).
	 */
	public void applyRotation()
	{
		if (rotationPriority == RotationPriority.NINETY_DEGREE)
		{
			if (orientation.equals(Orientation.TOP_TO_BOTTOM))
			{
				if (netRotationalForce > SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(90);
					orientation = Orientation.RIGHT_TO_LEFT;
				}
				else if (netRotationalForce < -SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(-90);
					orientation = Orientation.LEFT_TO_RIGHT;
				}
			}
			else if (orientation.equals(Orientation.BOTTOM_TO_TOP))
			{
				if (netRotationalForce < -SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(90);
					orientation = Orientation.LEFT_TO_RIGHT;
				}
				else if (netRotationalForce > SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(-90);
					orientation = Orientation.RIGHT_TO_LEFT;
				}
			}
			else if (orientation.equals(Orientation.RIGHT_TO_LEFT))
			{
				if (netRotationalForce > SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(90);
					orientation = Orientation.BOTTOM_TO_TOP;
				}
				else if (netRotationalForce < -SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(-90);
					orientation = Orientation.TOP_TO_BOTTOM;
				}
			}
			else if (orientation.equals(Orientation.LEFT_TO_RIGHT))
			{
				if (netRotationalForce < -SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(-90);
					orientation = Orientation.BOTTOM_TO_TOP;
				}
				else if (netRotationalForce > SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(90);
					orientation = Orientation.TOP_TO_BOTTOM;
				}
			}
		}

		else if (rotationPriority == RotationPriority.SWAP)
		{
			PointD tempCenter = inputPort.getCenter();
			inputPort.setCenter(outputPort.getCenterX(),
					outputPort.getCenterY());
			outputPort.setCenter(tempCenter.x, tempCenter.y);

			if (orientation.equals(Orientation.TOP_TO_BOTTOM))
				orientation = Orientation.BOTTOM_TO_TOP;
			else if (orientation.equals(Orientation.BOTTOM_TO_TOP))
				orientation = Orientation.TOP_TO_BOTTOM;
			else if (orientation.equals(Orientation.LEFT_TO_RIGHT))
				orientation = Orientation.RIGHT_TO_LEFT;
			else if (orientation.equals(Orientation.RIGHT_TO_LEFT))
				orientation = Orientation.LEFT_TO_RIGHT;
		}

		this.netRotationalForce = 0;
	}

	/**
	 * Given a compound node, this method recursively rotates the compound node
	 * and its members.
	 */
	private void rotateCompound(int rotationDegree)
	{
		this.rotateNode(this.getCenter(), rotationDegree);
		inputPort.rotateNode(this.getCenter(), rotationDegree);
		outputPort.rotateNode(this.getCenter(), rotationDegree);
		this.parentCompound.updateBounds();
	}

	public void calcRotationalForces()
	{
		this.netRotationalForce += this.calcProperlyOrientedEdges();
	}

	/**
	 * This method calculates all angles between process and its edges (prod,
	 * cons, eff) and marks them as properly oriented or not. Returned value is
	 * the amount of desire to rotate at this step. The returned value should be
	 * then manually added to netRotationalForce (if aim is to calculate
	 * netrotationalforce)
	 */
	public double calcProperlyOrientedEdges()
	{
		double inputRotSum = 0;
		double outputRotSum = 0;
		double effectorRotSum = 0;
		double stepSum = 0;
		double result;
		this.properEdgeCount = 0;

		// if the neighbors of port nodes have not been detected yet, find them.
		if (consumptionEdges.size() == 0 && productEdges.size() == 0)
			initLists();

		// find ideal positions
		PointD inputPortTarget = findPortTargetPoint(true, this.orientation);
		PointD outputPortTarget = findPortTargetPoint(false, this.orientation);

		for (int nodeIndex = 0; nodeIndex < consumptionEdges.size(); nodeIndex++)
		{
			result = calcRotationalForce(true, nodeIndex, inputPortTarget);
			if (Math.abs(result) <= SbgnPDConstants.ANGLE_TOLERANCE)
				this.properEdgeCount++;
			inputRotSum += result;
		}
		for (int nodeIndex = 0; nodeIndex < productEdges.size(); nodeIndex++)
		{
			result = calcRotationalForce(false, nodeIndex, outputPortTarget);
			if (Math.abs(result) <= SbgnPDConstants.ANGLE_TOLERANCE)
				this.properEdgeCount++;
			outputRotSum += result;
		}

		for (int nodeIndex = 0; nodeIndex < effectorEdges.size(); nodeIndex++)
		{
			result = calcEffectorAngle(nodeIndex);
			if (Math.abs(result) <= SbgnPDConstants.EFFECTOR_ANGLE_TOLERANCE)
				this.properEdgeCount++;

			effectorRotSum += Math.abs(result);
		}

		// add total effector rotational force with the same sign of
		// step sum because it does not matter for an effector node
		// either rotate to left or right. therefore support the rotation
		// direction of each iteration.
		stepSum = inputRotSum - outputRotSum;
		stepSum = stepSum + Math.signum(stepSum) * Math.abs(effectorRotSum);

		return stepSum;
	}

	/**
	 * This method returns the signed angle between a node and its corresponding
	 * port and the target point.
	 */
	private double calcRotationalForce(boolean isInputPort, int nodeIndex,
			PointD targetPoint)
	{
		SbgnPDNode node;
		PointD centerPoint;

		if (isInputPort)
		{
			node = (SbgnPDNode) consumptionEdges.get(nodeIndex).getSource();
			centerPoint = inputPort.getCenter();
		}
		else
		{
			node = (SbgnPDNode) productEdges.get(nodeIndex).getTarget();
			centerPoint = outputPort.getCenter();
		}

		double angle = IGeometry.calculateAngle(targetPoint, centerPoint,
				node.getCenter());

		if (isInputPort)
			angle *= isLeft(targetPoint, centerPoint, node.getCenter(),
					SbgnPDConstants.INPUT_PORT);
		else
			angle *= isLeft(targetPoint, centerPoint, node.getCenter(),
					SbgnPDConstants.OUTPUT_PORT);

		saveInformation(isInputPort, nodeIndex, angle);

		return angle;
	}

	/**
	 * Calculates the angle between an effector edge and its process node. An
	 * effector edge has process (in this case the dummy compound) as its target
	 * node and the effector itself as the source.
	 */
	private double calcEffectorAngle(int nodeIndex)
	{
		SbgnPDNode eff = (SbgnPDNode) effectorEdges.get(nodeIndex).getSource();
		PointD targetPnt = new PointD();
		PointD centerPnt = this.getCenter();

		// find target point
		if (this.isHorizontal())
		{
			targetPnt.x = this.getCenterX();

			if (eff.getCenterY() > this.getCenterY())
				targetPnt.y = this.getCenterY() + this.idealEdgeLength;
			else
				targetPnt.y = this.getCenterY() - this.idealEdgeLength;
		}
		else if (this.isVertical())
		{
			targetPnt.y = this.getCenterY();

			if (eff.getCenterX() > this.getCenterX())
				targetPnt.x = this.getCenterX() + this.idealEdgeLength;
			else
				targetPnt.x = this.getCenterX() - this.idealEdgeLength;
		}

		double angle = IGeometry.calculateAngle(targetPnt, centerPnt,
				eff.getCenter());

		effectorEdges.get(nodeIndex).correspondingAngle = (int) angle;

		if (Math.abs(angle) <= SbgnPDConstants.EFFECTOR_ANGLE_TOLERANCE)
			effectorEdges.get(nodeIndex).isProperlyOriented = true;
		else
			effectorEdges.get(nodeIndex).isProperlyOriented = false;

		return angle;
	}

	public void applyApproximations()
	{
		// if there is only one single-edge consumption, move it to ideal
		// otherwise move towards multiedge node

		if (this.consumptionEdges.size() == 1
				&& this.consumptionEdges.get(0).getSource().getEdges().size() == 1)
			approximateForSingleNodes(this.inputPort,
					(SbgnPDNode) this.consumptionEdges.get(0).getSource());
		else
			approximateForMultipleNodes(this.inputPort);

		if (this.productEdges.size() == 1
				&& this.productEdges.get(0).getTarget().getEdges().size() == 1)
			approximateForSingleNodes(this.outputPort,
					(SbgnPDNode) this.productEdges.get(0).getTarget());
		else
			approximateForMultipleNodes(this.outputPort);

		approximateEffectors();
	}

	private void approximateForSingleNodes(SbgnPDNode port, SbgnPDNode node)
	{
		PointD targetPt = new PointD(), newPoint = new PointD();
		if (port.isInputPort())
			targetPt = findPortTargetPoint(true, this.orientation);
		else if (port.isOutputPort())
			targetPt = findPortTargetPoint(false, this.orientation);

		newPoint.x = targetPt.x
				+ (Math.random() * SbgnPDConstants.APPROXIMATION_DISTANCE * 2)
				- SbgnPDConstants.APPROXIMATION_DISTANCE;
		newPoint.y = targetPt.y
				+ (Math.random() * SbgnPDConstants.APPROXIMATION_DISTANCE * 2)
				- SbgnPDConstants.APPROXIMATION_DISTANCE;

		node.setCenter(newPoint.x, newPoint.y);
	}

	/**
	 * Given the port node, this method finds all consumption(or production)
	 * nodes of this port node. The idea is to move each single-edge
	 * consumptions(products) of a process closer to neighbor multi-edge nodes
	 * to keep them close to each other.
	 */
	private void approximateForMultipleNodes(SbgnPDNode port)
	{
		LinkedList<SbgnPDNode> oneEdgeNodes = new LinkedList<SbgnPDNode>();
		LinkedList<SbgnPDNode> multiEdgeNodes = new LinkedList<SbgnPDNode>();
		SbgnPDNode nodeOfInterest = null;
		PointD targetPt = new PointD();

		// get all non-rigid edges of port node
		for (Object e : port.getEdges())
		{
			SbgnPDEdge edge = (SbgnPDEdge) e;

			if (edge.isRigidEdge())
				continue;

			// node of interest depends on the direction of the edge
			if (port.isInputPort())
				nodeOfInterest = (SbgnPDNode) edge.getSource();
			else if (port.isOutputPort())
				nodeOfInterest = (SbgnPDNode) edge.getTarget();

			if (nodeOfInterest.getEdges().size() == 1) // single node
				oneEdgeNodes.add(nodeOfInterest);
			else if (nodeOfInterest.getEdges().size() > 1) // multiedge node
				multiEdgeNodes.add(nodeOfInterest);
		}

		if (port.isInputPort())
			targetPt = findPortTargetPoint(true, this.orientation);
		else if (port.isOutputPort())
			targetPt = findPortTargetPoint(false, this.orientation);

		// move
		if (oneEdgeNodes.size() > 0)
			moveOneEdgeNodes(oneEdgeNodes, multiEdgeNodes, targetPt);
	}

	/**
	 * Single-edge nodes are moved around the center point of a multi-edge node.
	 * If all the neighbor nodes of that port node are single-edged, then one of
	 * them is chosen randomly and the others are placed around it.
	 * 
	 * @param targetPt
	 */
	private void moveOneEdgeNodes(LinkedList<SbgnPDNode> oneEdgeNodes,
			LinkedList<SbgnPDNode> multiEdgeNodes, PointD targetPt)
	{
		PointD approximationPnt = new PointD(0, 0);
		int randomIndex = -1;
		SbgnPDNode approximationNode = null;
		PointD newPoint = new PointD();

		// if there are more than one multi edge nodes, select the highly
		// connected one
		if (multiEdgeNodes.size() > 0)
		{
			approximationNode = multiEdgeNodes.get(0);
			for (SbgnPDNode node : multiEdgeNodes)
			{
				if (node.getEdges().size() > approximationNode.getEdges()
						.size())
					approximationNode = node;
			}
		}

		// if there are no multi edge nodes, randomly select one
		else if (multiEdgeNodes.size() == 0)
		{
			randomIndex = (int) (Math.random() * oneEdgeNodes.size());
			approximationNode = oneEdgeNodes.get(randomIndex);
		}

		approximationPnt = approximationNode.getCenter();

		// move single nodes around the approximation point
		for (SbgnPDNode s : oneEdgeNodes)
		{
			// if they belong to different graphs, do not move
			// if (approximationNode.getOwner() != s.getOwner())
			// continue;

			newPoint.x = approximationPnt.x
					+ (Math.random() * SbgnPDConstants.APPROXIMATION_DISTANCE * 2)
					- SbgnPDConstants.APPROXIMATION_DISTANCE;
			newPoint.y = approximationPnt.y
					+ (Math.random() * SbgnPDConstants.APPROXIMATION_DISTANCE * 2)
					- SbgnPDConstants.APPROXIMATION_DISTANCE;

			s.setCenter(newPoint.x, newPoint.y);
		}
	}

	/**
	 * Identify single-edge effectors. Note that a process may have a number of
	 * effectors. Find the location of each effector. If the orientation of
	 * process node is vertical, ideal position of effectors is on the
	 * horizontal directions. (or vice versa)
	 */
	public void approximateEffectors()
	{
		PointD newPoint = new PointD();
		PointD approximationPnt = new PointD();

		// identify the effectors
		for (SbgnPDEdge edge : effectorEdges)
		{
			// source node of each effector edge is the effector itself. only
			// move single effectors
			if (edge.getSource().getEdges().size() != 1)
				continue;

			approximationPnt = findEffectorTargetPoint((SbgnPDNode) edge
					.getSource());

			// place effector in a circular area using some randomness
			newPoint.x = approximationPnt.x
					+ (Math.random() * SbgnPDConstants.APPROXIMATION_DISTANCE * 2)
					- SbgnPDConstants.APPROXIMATION_DISTANCE;
			newPoint.y = approximationPnt.y
					+ (Math.random() * SbgnPDConstants.APPROXIMATION_DISTANCE * 2)
					- SbgnPDConstants.APPROXIMATION_DISTANCE;

			edge.getSource().setCenter(newPoint.x, newPoint.y);
		}
	}

	/**
	 * Given the effector and its corresponding process, the method returns the
	 * ideal position of the effector node, which has a distance of ideal edge
	 * length from its process.
	 */
	private PointD findEffectorTargetPoint(SbgnPDNode eff)
	{
		PointD approximationPnt = new PointD();
		// find target point
		if (this.isHorizontal())
		{
			approximationPnt.x = this.getCenterX();

			if (eff.getCenterY() > this.getCenterY())
				approximationPnt.y = this.getCenterY() + this.idealEdgeLength;
			else
				approximationPnt.y = this.getCenterY() - this.idealEdgeLength;
		}
		else if (this.isVertical())
		{
			approximationPnt.y = this.getCenterY();

			if (eff.getCenterX() > this.getCenterX())
				approximationPnt.x = this.getCenterX() + this.idealEdgeLength;
			else
				approximationPnt.x = this.getCenterX() - this.idealEdgeLength;
		}

		return approximationPnt;
	}

	public PointD findPortTargetPoint(boolean isInputPort, Orientation orient)
	{
		if (orient.equals(Orientation.LEFT_TO_RIGHT))
		{
			if (isInputPort)
				return new PointD(
						(inputPort.getCenterX() - this.idealEdgeLength),
						inputPort.getCenterY());
			else
				return new PointD(
						(outputPort.getCenterX() + this.idealEdgeLength),
						outputPort.getCenterY());
		}
		else if (orient.equals(Orientation.RIGHT_TO_LEFT))
		{
			if (isInputPort)
				return new PointD(
						(inputPort.getCenterX() + this.idealEdgeLength),
						inputPort.getCenterY());
			else
				return new PointD(
						(outputPort.getCenterX() - this.idealEdgeLength),
						outputPort.getCenterY());
		}
		else if (orient.equals(Orientation.TOP_TO_BOTTOM))
		{
			if (isInputPort)
				return new PointD(inputPort.getCenterX(),
						(inputPort.getCenterY() - this.idealEdgeLength));
			else
				return new PointD(outputPort.getCenterX(),
						(outputPort.getCenterY() + this.idealEdgeLength));
		}
		else if (orient.equals(Orientation.BOTTOM_TO_TOP))
		{
			if (isInputPort)
				return new PointD(inputPort.getCenterX(),
						(inputPort.getCenterY() + this.idealEdgeLength));
			else
				return new PointD(outputPort.getCenterX(),
						(outputPort.getCenterY() - this.idealEdgeLength));
		}

		return null;
	}

	private void initLists()
	{
		for (Object o : inputPort.getEdges())
		{
			SbgnPDEdge edge = (SbgnPDEdge) o;
			if (!edge.isRigidEdge())
			{
				consumptionEdges.add(edge);
			}
		}

		for (Object o : outputPort.getEdges())
		{
			SbgnPDEdge edge = (SbgnPDEdge) o;
			if (!edge.isRigidEdge())
			{
				productEdges.add(edge);
			}
		}

		// detect all effector nodes connected to this process node (if
		// there are any)
		for (Object o : parentCompound.getEdges())
		{
			SbgnPDEdge edge = (SbgnPDEdge) o;
			if (edge.isEffector())
			{
				effectorEdges.add(edge);
			}
		}
	}

	private void saveInformation(boolean isInputPort, int nodeIndex,
			double angle)
	{

		// remember angles especially for debug purposes
		if (isInputPort)
			consumptionEdges.get(nodeIndex).correspondingAngle = (int) angle;
		else
			productEdges.get(nodeIndex).correspondingAngle = (int) angle;

		// note if the edges are properly oriented
		if (Math.abs(angle) <= SbgnPDConstants.ANGLE_TOLERANCE)
		{
			if (isInputPort)
				consumptionEdges.get(nodeIndex).isProperlyOriented = true;
			else
				productEdges.get(nodeIndex).isProperlyOriented = true;
		}
		else
		{
			if (isInputPort)
				consumptionEdges.get(nodeIndex).isProperlyOriented = false;
			else
				productEdges.get(nodeIndex).isProperlyOriented = false;
		}
	}

	public int isLeft(PointD a, PointD b, PointD c, String type)
	{
		if (((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) > 0)
		{
			// left turn
			if (this.orientation.equals(Orientation.TOP_TO_BOTTOM))
			{
				if (type.equals(SbgnPDConstants.INPUT_PORT))
					return -1;
				else if (type.equals(SbgnPDConstants.OUTPUT_PORT))
					return 1;
			}
			else if (this.orientation.equals(Orientation.BOTTOM_TO_TOP))
			{
				if (type.equals(SbgnPDConstants.INPUT_PORT))
					return 1;
				else if (type.equals(SbgnPDConstants.OUTPUT_PORT))
					return -1;
			}
			else if (this.orientation.equals(Orientation.LEFT_TO_RIGHT))
			{
				if (type.equals(SbgnPDConstants.INPUT_PORT))
					return 1;
				else if (type.equals(SbgnPDConstants.OUTPUT_PORT))
					return -1;

			}
			else if (this.orientation.equals(Orientation.RIGHT_TO_LEFT))
			{
				if (type.equals(SbgnPDConstants.INPUT_PORT))
					return -1;
				else if (type.equals(SbgnPDConstants.OUTPUT_PORT))
					return 1;
			}
		}
		else
		{
			// right turn
			if (this.orientation.equals(Orientation.TOP_TO_BOTTOM))
			{
				if (type.equals(SbgnPDConstants.INPUT_PORT))
					return 1;
				else if (type.equals(SbgnPDConstants.OUTPUT_PORT))
					return -1;
			}
			else if (this.orientation.equals(Orientation.BOTTOM_TO_TOP))
			{
				if (type.equals(SbgnPDConstants.INPUT_PORT))
					return -1;
				else if (type.equals(SbgnPDConstants.OUTPUT_PORT))
					return 1;
			}
			else if (this.orientation.equals(Orientation.LEFT_TO_RIGHT))
			{
				if (type.equals(SbgnPDConstants.INPUT_PORT))
					return -1;
				else if (type.equals(SbgnPDConstants.OUTPUT_PORT))
					return 1;

			}
			else if (this.orientation.equals(Orientation.RIGHT_TO_LEFT))
			{
				if (type.equals(SbgnPDConstants.INPUT_PORT))
					return 1;
				else if (type.equals(SbgnPDConstants.OUTPUT_PORT))
					return -1;
			}
		}
		return 0;
	}

	public void copyNode(SbgnProcessNode s, LGraphManager graphManager)
	{
		this.type = s.type;
		this.label = s.label;
		this.parentCompound = s.parentCompound;
		this.inputPort = s.inputPort;
		this.outputPort = s.outputPort;
		this.setCenter(s.getCenterX(), s.getCenterY());
		this.setChild(s.getChild());
		this.setHeight(s.getHeight());
		this.setLocation(s.getLocation().x, s.getLocation().y);
		this.setNext(s.getNext());
		this.setOwner(s.getOwner());
		this.setPred1(s.getPred1());
		this.setPred2(s.getPred2());
		this.setWidth(s.getWidth());
	}

	public void copyFromSBGNPDNode(SbgnPDNode s, LGraphManager graphManager)
	{
		this.type = s.type;
		this.label = s.label;
		this.setCenter(s.getCenterX(), s.getCenterY());
		this.setChild(s.getChild());
		this.setHeight(s.getHeight());
		this.setLocation(s.getLocation().x, s.getLocation().y);
		this.setNext(s.getNext());
		this.setOwner(s.getOwner());
		this.setPred1(s.getPred1());
		this.setPred2(s.getPred2());
		this.setWidth(s.getWidth());

		// copy edges
		for (Object o : s.getEdges())
		{
			SbgnPDEdge edge = (SbgnPDEdge) o;
			SbgnPDEdge newEdge = new SbgnPDEdge((SbgnPDNode) edge.getSource(),
					(SbgnPDNode) edge.getTarget(), null, edge.type);

			newEdge.copy(edge);

			if (edge.getSource().equals(s))
			{
				newEdge.setSource(this);
			}
			else if (edge.getTarget().equals(s))
			{
				newEdge.setTarget(this);
			}

			// add new edge to the graph manager.
			graphManager.add(newEdge, newEdge.getSource(), newEdge.getTarget());
		}
	}

	public boolean isVertical()
	{
		if (this.orientation.equals(Orientation.TOP_TO_BOTTOM)
				|| this.orientation.equals(Orientation.BOTTOM_TO_TOP))
			return true;
		return false;
	}

	public boolean isHorizontal()
	{
		if (this.orientation.equals(Orientation.LEFT_TO_RIGHT)
				|| this.orientation.equals(Orientation.RIGHT_TO_LEFT))
			return true;
		return false;
	}

	public void setOrientation(Orientation orient)
	{
		this.orientation = orient;
		if (this.orientation.equals(Orientation.LEFT_TO_RIGHT))
		{
			this.inputPort.setCenter(this.getCenterX()
					- SbgnPDConstants.RIGID_EDGE_LENGTH, this.getCenterY());
			this.outputPort.setCenter(this.getCenterX()
					+ SbgnPDConstants.RIGID_EDGE_LENGTH, this.getCenterY());
		}
		else if (this.orientation.equals(Orientation.RIGHT_TO_LEFT))
		{
			this.inputPort.setCenter(this.getCenterX()
					+ SbgnPDConstants.RIGID_EDGE_LENGTH, this.getCenterY());
			this.outputPort.setCenter(this.getCenterX()
					- SbgnPDConstants.RIGID_EDGE_LENGTH, this.getCenterY());
		}
		else if (this.orientation.equals(Orientation.BOTTOM_TO_TOP))
		{
			this.inputPort.setCenter(this.getCenterX(), this.getCenterY()
					+ SbgnPDConstants.RIGID_EDGE_LENGTH);
			this.outputPort.setCenter(this.getCenterX(), this.getCenterY()
					- SbgnPDConstants.RIGID_EDGE_LENGTH);
		}
		else if (this.orientation.equals(Orientation.TOP_TO_BOTTOM))
		{
			this.inputPort.setCenter(this.getCenterX(), this.getCenterY()
					- SbgnPDConstants.RIGID_EDGE_LENGTH);
			this.outputPort.setCenter(this.getCenterX(), this.getCenterY()
					+ SbgnPDConstants.RIGID_EDGE_LENGTH);
		}
	}

	/**
	 * Transfer forces acting on process node to its parent compound to make
	 * sure process does not move.
	 */
	public void transferForces()
	{
		parentCompound.springForceX += this.springForceX
				+ inputPort.springForceX + outputPort.springForceX;
		parentCompound.springForceY += this.springForceY
				+ inputPort.springForceY + outputPort.springForceY;
	}

	public SbgnPDNode getInputPort()
	{
		return this.inputPort;
	}

	public SbgnPDNode getOutputPort()
	{
		return this.outputPort;
	}

	public SbgnPDNode getParentCompound()
	{
		return parentCompound;
	}

	public enum Orientation
	{
		BOTTOM_TO_TOP, TOP_TO_BOTTOM, LEFT_TO_RIGHT, RIGHT_TO_LEFT
	};

	public enum RotationPriority
	{
		NINETY_DEGREE, SWAP, NO_ROTATION
	}
}
