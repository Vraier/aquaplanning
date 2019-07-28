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
import edu.kit.aquaplanning.planning.cube.heuristic.GenericFroleyks;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericManhattanGoalDistance;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericRelaxedPathLength;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericTrautmann;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericWilliams;

public class PortfolioCubeFinder extends CubeFinder {

	private final int numFinder = 5;

	private Set<GenericSearchNode> currOpenNodes = new HashSet<>();
	private Set<GenericSearchNode> currFinishedNodes = new HashSet<>();
	private Set<GenericSearchNode> totalOpenNodes = new HashSet<>();
	private Set<GenericSearchNode> totalFinishedNodes = new HashSet<>();

	public PortfolioCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem) {

		// round up integer division: numCubes/numFinder
		int cubeInterval = (config.numCubes + (numFinder - 1)) / numFinder;

		for (int i = 0; i < numFinder; i++) {

			findCubes(i, problem, cubeInterval);

			foundCubeSize += currOpenNodes.size();
			totalOpenNodes.addAll(currOpenNodes);
			totalFinishedNodes.addAll(currFinishedNodes);
			currOpenNodes.clear();
			currFinishedNodes.clear();
		}
		// System.out.println("Finished all Heuristics");

		totalOpenNodes.removeAll(totalFinishedNodes);
		ArrayList<Cube> result = new ArrayList<>();

		for (GenericSearchNode n : totalOpenNodes) {
			result.add(n.getCube());
		}
		returnedCubeSize = result.size();
		return result;
	}

	@Override
	public void logInformation() {
		// TODO Auto-generated method stub

	}

	/**
	 * Does a greedy depth first search with different heuristics depending on
	 * parameter id.
	 */
	private void findCubes(int id, GroundPlanningProblem problem, int numCubes) {
		// System.out.println("Trying heuristic with id " + id);

		GenericHeuristic heuristic = getHeuristic(id);

		ArrayDeque<GenericSearchNode> history = new ArrayDeque<>();
		GenericSearchNode node = new ForwardSearchNode(problem);
		history.addLast(node);

		while (!node.satisfiesProblem() && currOpenNodes.size() < numCubes && withinTimeLimit()) {

			totalIterations++;
			System.out.println("Size of history is: " + history.size() + ", size of finished is: "
					+ currFinishedNodes.size() + ", size of open is: " + currOpenNodes.size());

			currFinishedNodes.add(node);
			currOpenNodes.remove(node);

			GenericSearchNode best = null;
			int bestValue = Integer.MAX_VALUE;

			for (GenericSearchNode c : node.getPredecessors()) {
				if (currFinishedNodes.contains(c)) {
					// nothing to do here. Node is already explored
					continue;
				} else {
					int value = heuristic.value(c);
					// node can't reach goal. don't add it to open nodes
					if (value == Integer.MAX_VALUE)
						continue;
					// node was never seen before or is open so we consider it for the next
					// iteration
					currOpenNodes.add(c);
					if (value < bestValue) {
						bestValue = value;
						best = c;
					}
				}
			}

			if (best == null) {
				if (history.size() == 0) {
					assert (currOpenNodes.size() == 0);
					return;
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
		return;
	}

	private GenericHeuristic getHeuristic(int id) {
		GenericHeuristic heuristic;
		switch (id) {
		case 0:
			heuristic = new GenericFroleyks();
			break;
		case 1:
			heuristic = new GenericManhattanGoalDistance();
			break;
		case 2:
			heuristic = new GenericRelaxedPathLength();
			break;
		case 3:
			heuristic = new GenericTrautmann();
			break;
		case 4:
			heuristic = new GenericWilliams();
			break;
		default:
			throw new UnsupportedOperationException("No such id available for heuristic.");
		}
		return heuristic;
	}
}
