package edu.kit.aquaplanning.planning.cube.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.util.Logger;

public class GreedyCutOffCubeFinder extends CubeFinder {

	//TODO config entries for numAnchors and percent
	
	private HashSet<GenericSearchNode> openNodes = new HashSet<>();
	private HashSet<GenericSearchNode> finishedNodes = new HashSet<>();
	private ArrayDeque<GenericSearchNode> history = new ArrayDeque<>();
	private GenericHeuristic heuristic;

	private int numAnchors;
	private double percent;
	private HashSet<GenericSearchNode> cutOffNodes = new HashSet<>();
	private ArrayList<GenericSearchNode> anchors = new ArrayList<>();
	private ArrayList<Integer> anchorValues = new ArrayList<>();

	public int totalFrontierSize = 0;
	public int totalAnchorSize = 0;
	public int totalCutOffSize = 0;

	public GreedyCutOffCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem) {

		int anchorInterval = config.numCubes / numAnchors;

		GenericSearchNode node = new ForwardSearchNode(problem);

		while (!node.satisfiesProblem() && currentCubesSize() < config.numCubes && withinTimeLimit()) {
			totalIterations++;

			// make the current node an anchors. If it wasn't finished before and we visit
			// it via backtracking
			if (!finishedNodes.contains(node) && currentCubesSize() >= anchorInterval) {
				anchors.add(node);
				anchorValues.add(heuristic.stateDistance(problem.getInitialState(), node.getState(), problem));
				anchorInterval += config.numCubes / numAnchors;
			}

			finishedNodes.add(node);
			openNodes.remove(node);

			GenericSearchNode best = null;
			int bestValue = Integer.MAX_VALUE;

			for (GenericSearchNode c : node.getPredecessors()) {
				if (finishedNodes.contains(c)) {
					// nothing to do here. Node is already explored
					continue;
				} else if (shouldBeCutOff(problem, c)) {
					// node gets cut off. We add it to the finished nodes because we don't want to
					// do anything with it again.
					finishedNodes.add(c);
					openNodes.remove(c);
					cutOffNodes.add(c);
					continue;
				} else {
					// node was never seen before or is open so we consider it for the next
					// iteration
					openNodes.add(c);
					int value = heuristic.value(c);
					if (value < bestValue) {
						bestValue = value;
						best = c;
					}
				}
			}

			if (best == null) {
				if (history.size() == 0) {
					assert (openNodes.size() == 0);
					ArrayList<Cube> result = new ArrayList<>();
					for (GenericSearchNode n : cutOffNodes) {
						result.add(n.getCube());
					}
				}
				// backtracking
				GenericSearchNode newNode = history.pollLast();
				node = newNode;
			} else {
				// select the best action
				history.addLast(node);
				node = best;
			}
		}

		totalFrontierSize = currentCubesSize();
		totalAnchorSize = anchors.size();
		totalCutOffSize = cutOffNodes.size();
		
		Set<GenericSearchNode> resultSet = new HashSet<>();
		List<Cube> result = new ArrayList<>();
		resultSet.addAll(openNodes);
		resultSet.addAll(cutOffNodes);
		
		for(GenericSearchNode n: resultSet) {
			result.add(n.getCube());
		}
		return result;
	}

	@Override
	public void logInformation() {
		if (!withinTimeLimit()) {
			Logger.log(Logger.INFO, "Greedy Cut Off Cube Finder exceeded his time limit or got interrupted.");
		}
		Logger.log(Logger.INFO, "Greedy Cut Off Cube Finder stopped search after " + totalIterations + " steps.");
		Logger.log(Logger.INFO, "Greedy Cut Off Cube Finder found " + totalFrontierSize + " cubes from which "
				+ totalCutOffSize + " were cut off and " + totalAnchorSize + " were anchors.");
	}

	private boolean shouldBeCutOff(GroundPlanningProblem problem, GenericSearchNode node) {

		for (int i = 0; i < anchors.size(); i++) {
			int currDistance = heuristic.stateDistance(node.getState(), anchors.get(i).getState(), problem);
			if (currDistance < percent * anchorValues.get(i)) {
				return true;
			}
		}
		return false;
	}

	private int currentCubesSize() {
		return openNodes.size() + cutOffNodes.size();
	}
}
