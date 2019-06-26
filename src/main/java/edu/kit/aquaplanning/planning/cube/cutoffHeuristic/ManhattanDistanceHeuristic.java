package edu.kit.aquaplanning.planning.cube.cutoffHeuristic;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;

public class ManhattanDistanceHeuristic extends CutOffHeuristic {

	private int depth;
	private double differentAtoms;

	/**
	 * COnstructs a new cut off heuristic. It cuts a node if the number of its atoms
	 * differs from an anchor by the given percentage. A node gets a anchor if it is
	 * deep enough in the search tree.
	 * 
	 * @param cutDepth
	 *            the depth to determine if this node gets an anchor
	 * @param cutDistance
	 *            the percentage of atoms that this node has to differ from an
	 *            anchor to not get cut off. Must be between 1 and 0.
	 */
	public ManhattanDistanceHeuristic(Configuration config, GroundPlanningProblem problem) {
		this.depth = config.cutDepth;
		this.differentAtoms = problem.getNumAtoms() * config.cutDistance;
	}

	@Override
	public boolean cutOff(List<GenericSearchNode> anchors, GenericSearchNode node) {
		for (GenericSearchNode a : anchors) {
			if (node.getAtomSet().and(a.getAtomSet()).numAtoms() < differentAtoms)
				return true;
		}
		return false;
	}

	@Override
	public boolean isAnchor(GenericSearchNode node) {
		return node.depth >= this.depth;
	}
}
