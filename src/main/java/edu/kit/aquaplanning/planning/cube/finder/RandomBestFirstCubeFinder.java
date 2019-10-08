package edu.kit.aquaplanning.planning.cube.finder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.util.Logger;

/**
 * This cube finder does multiple randomized best first searches
 */
public class RandomBestFirstCubeFinder extends CubeFinder {

	private PriorityQueue<GenericSearchNode> frontier;
	private Set<GenericSearchNode> allVisitedNodes;
	private GenericHeuristic heuristic;
	private Random random;
	private int numDescents;

	public RandomBestFirstCubeFinder(Configuration config) {
		super(config);
		frontier = new PriorityQueue<GenericSearchNode>((n1, n2) -> Double.compare(n1.randomValue, n2.randomValue));
		allVisitedNodes = new HashSet<>();
		heuristic = GenericHeuristic.getHeuristic(config);
		random = new Random(config.seed);
		numDescents = config.cubeFindDescents;
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem) {

		// do the requested amount of searches
		int depth = (config.numCubes + numDescents - 1) / numDescents;
		for (int i = 0; i < numDescents; i++) {
			Set<GenericSearchNode> partialNodes = randomDescent(problem, depth);
			if (partialNodes == null) {
				assert (plan != null);
				return null;
			}
			allVisitedNodes.addAll(partialNodes);
		}

		List<Cube> result = new ArrayList<>();
		for (GenericSearchNode n : allVisitedNodes) {
			result.add(n.getCube());
		}
		returnedCubeSize = result.size();
		return result;
	}

	private Set<GenericSearchNode> randomDescent(GroundPlanningProblem problem, int depth) {
		Set<GenericSearchNode> visitedNodes = new HashSet<>();
		frontier.clear();
		frontier.add(new ForwardSearchNode(problem));

		while (!frontier.isEmpty() && visitedNodes.size() < depth && withinTimeLimit()) {

			totalIterations++;

			GenericSearchNode node = frontier.poll();
			if (visitedNodes.contains(node))
				continue;
			visitedNodes.add(node);

			if (node.satisfiesProblem()) {
				Logger.log(Logger.INFO, "Random Cube Finder already found a plan after " + totalIterations + " steps.");
				plan = node.getPartialPlan();

				foundCubeSize += visitedNodes.size();
				returnedCubeSize = 0;
				return null;
			}

			for (GenericSearchNode child : node.getPredecessors()) {
				// calculate a new random value by multiplying with 0.9 <= r <= 1.1
				int value = heuristic.value(child);
				double randomFactor = 0.9 + (random.nextDouble() * 0.2);
				child.randomValue = randomFactor * (double) value;

				frontier.add(child);
			}
		}
		if (!withinTimeLimit()) {
			// We return an empty list so signalize that we should stop searching for a
			// solution
			return new HashSet<GenericSearchNode>();
		}

		foundCubeSize += visitedNodes.size();
		return visitedNodes;
	}
}
