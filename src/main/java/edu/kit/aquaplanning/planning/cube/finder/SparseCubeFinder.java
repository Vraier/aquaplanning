package edu.kit.aquaplanning.planning.cube.finder;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchQueue;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.util.Logger;

public class SparseCubeFinder extends CubeFinder {

	private int interval;

	private GenericSearchQueue frontier;
	
	public SparseCubeFinder(Configuration config) {
		super(config);

		Configuration tempConfig = config.copy();
		tempConfig.searchStrategy = config.cubeFindSearchStrategy;
		SearchStrategy strategy = new SearchStrategy(tempConfig);

		GenericHeuristic heuristic = GenericHeuristic.getHeuristic(config);
		frontier = new GenericSearchQueue(strategy, heuristic);

		interval = config.cubeSparseInterval;
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem) {
		ForwardSearchNode initalNode = new ForwardSearchNode(problem);
		ArrayList<GenericSearchNode> cubes = new ArrayList<>();

		frontier.add(initalNode);
		// this asserts that we are still complete
		cubes.add(initalNode);

		while (!frontier.isEmpty() && cubes.size() < config.numCubes && withinTimeLimit()) {

			totalIterations++;
			foundCubeSize++;

			GenericSearchNode node = frontier.get();

			if (node == null) {
				assert (frontier.isEmpty());
				foundCubeSize--;
				continue;
			}

			if (node.satisfiesProblem()) {
				Logger.log(Logger.INFO, "Sparse Cube Finder already found a plan after " + totalIterations + " steps.");
				plan = node.getPartialPlan();
				return null;
			}

			// add each interval steps a node to the cubes
			if (totalIterations % interval == 0) {
				cubes.add(node);
			}

			// add new open nodes to search queue
			for (GenericSearchNode child : node.getPredecessors()) {
				frontier.add(child);
			}
		}

		if (!withinTimeLimit()) {
			// We return an empty list so signalize that we should stop searching for a
			// solution
			return new ArrayList<Cube>();
		}

		// construct cubes from the search nodes
		ArrayList<Cube> result = new ArrayList<>();
		for (GenericSearchNode n : cubes) {
			result.add(n.getCube());
		}
		returnedCubeSize = result.size();
		return result;
	}
}
