package edu.kit.aquaplanning.planning.cube;

import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.planning.datastructures.SearchNode;
import edu.kit.aquaplanning.util.Logger;

public class RandomBestFirstCubeSolver extends CubeSolver {

	private PriorityQueue<GenericSearchNode> frontier;
	private GenericHeuristic heuristic;
	private Random random;

	public RandomBestFirstCubeSolver(Configuration config, Cube cube, Set<SearchNode> visitedNodes) {
		super(config, cube, visitedNodes);
		
		Configuration tempConfig = config.copy();
		tempConfig.cubeFindHeuristic = config.cubeSolveHeuristic;
		heuristic = GenericHeuristic.getHeuristic(tempConfig);
		
		frontier = new PriorityQueue<>((n1, n2) -> Double.compare(n1.randomValue, n2.randomValue));
		GenericSearchNode initialNode = new ForwardSearchNode(cube.getProblem());
		initialNode.randomValue = heuristic.value(initialNode);
		frontier.add(initialNode);

		random = new Random(config.seed);
	}

	@Override
	public Plan calculateSteps() {

		int iterations = 0;
		startSearch();

		while (withinTimeLimit() && withinComputationalBounds(iterations) && !frontier.isEmpty()) {

			GenericSearchNode node = frontier.poll();

			// Is the goal reached?
			if (node.satisfiesProblem()) {

				// Extract plan
				totalIterations += iterations;
				totalTime += System.currentTimeMillis() - searchStartMillis;
				Logger.log(Logger.INFO, "RandomSearchPlanner found a plan after " + totalIterations + " steps in "
						+ totalTime + " millisecs.");
				Plan plan = node.getPartialPlan();
				cube.finalizePlan(plan);
				return plan;
			}

			for (GenericSearchNode child : node.getPredecessors()) {
				// calculate a new random value for the child
				int value = heuristic.value(child);
				double randomFactor = 0.9 + (random.nextDouble() * 0.2);
				child.randomValue = randomFactor * (double) value;

				frontier.add(child);
			}
			iterations++;
		}

		// no plan exists
		if (frontier.isEmpty()) {
			Logger.log(Logger.INFO, "RandomSearchPlanner found no plan after " + totalIterations + " steps in "
					+ totalTime + " millisecs.");
			isExhausted = true;
		}

		// We should stop searching for cubes in this case
		if (!withinTimeLimit()) {
			Logger.log(Logger.INFO,
					"RandomSearchPlanner found no plan after exceeded his time limit or got interrupted");
			isExhausted = true;
		}

		totalIterations += iterations;
		totalTime += System.currentTimeMillis() - searchStartMillis;
		return null;
	}

	@Override
	public double getBestDistance() {
		if (heuristic == null) {
			throw new UnsupportedOperationException("Need a heuristic to provide distances");
		}
		if (isExhausted || frontier.isEmpty()) {
			return Integer.MAX_VALUE;
		}
		return frontier.peek().randomValue;
	}
}
