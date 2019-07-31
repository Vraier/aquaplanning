package edu.kit.aquaplanning.planning.cube.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;

public class GreedyDepthFirstCubeFinder extends CubeFinder {

	private HashSet<GenericSearchNode> allVisitedNodes = new HashSet<>();
	private ArrayDeque<GenericSearchNode> history = new ArrayDeque<>();
	private Random rand;
	private GenericHeuristic heuristic;
	private int numDescents;

	public GreedyDepthFirstCubeFinder(Configuration config) {
		super(config);
		rand = new Random(config.seed);
		heuristic = GenericHeuristic.getHeuristic(config);
		numDescents = config.cubeFindDescents;
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem) {

		// do the requested amount of greedy depth first searches
		int depth = (config.numCubes + numDescents - 1) / numDescents;
		for (int i = 0; i < numDescents; i++) {
			Set<GenericSearchNode> partialNodes = randomDescent(problem, depth);
			if (partialNodes == null)
				return null;
			allVisitedNodes.addAll(partialNodes);
		}

		List<Cube> result = new ArrayList<>();
		for (GenericSearchNode n : allVisitedNodes) {
			result.add(n.getCube());
		}
		foundCubeSize = result.size();
		returnedCubeSize = result.size();
		return result;
	}

	private Set<GenericSearchNode> randomDescent(GroundPlanningProblem problem, int depth) {
		int iterations = 0;
		Set<GenericSearchNode> visitedNodes = new HashSet<>();
		GenericSearchNode node = new ForwardSearchNode(problem);

		while (!node.satisfiesProblem() && iterations < depth && withinTimeLimit()) {

			iterations++;
			totalIterations++;
			visitedNodes.add(node);

			List<GenericSearchNode> feasibleChilds = new ArrayList<>();

			for (GenericSearchNode c : node.getPredecessors()) {
				int value = heuristic.value(c);
				c.heuristicValue = value;

				// We found a valid plan
				if (value == 0) {
					plan = c.getPartialPlan();
					return null;
				}
				if (value < Integer.MAX_VALUE) {
					feasibleChilds.add(c);
				}
			}

			if (feasibleChilds.isEmpty()) {
				if (history.size() == 0) {
					// all children of initial node have infinite heuristic value
					return new HashSet<>();
				}
				// backtracking
				GenericSearchNode newNode = history.pollLast();
				node = newNode;
			} else {
				history.addLast(node);
				node = getRandomNode(feasibleChilds);
			}
		}

		return visitedNodes;
	}

	private GenericSearchNode getRandomNode(List<GenericSearchNode> feasibleChilds) {
		double sum = 0;
		double weightsSum = 0;
		List<Double> weights = new ArrayList<>();

		for (GenericSearchNode n : feasibleChilds) {
			sum += n.heuristicValue;
		}
		assert (sum != 0);
		for (int i = 0; i < feasibleChilds.size(); i++) {
			double weight = 1.0 - ((double) feasibleChilds.get(i).heuristicValue / sum);
			weightsSum += weight;
			weights.add(weight);
		}

		// random number between 0 and weightsSum
		double rando = rand.nextDouble() * weightsSum;
		double prefixSum = 0;
		for (int i = 0; i < weights.size(); i++) {
			prefixSum += weights.get(i);
			if (rando <= prefixSum) {
				return feasibleChilds.get(i);
			}
		}
		// return last child
		return feasibleChilds.get(feasibleChilds.size() - 1);
	}
}
