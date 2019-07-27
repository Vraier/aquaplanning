package edu.kit.aquaplanning.planning.cube.finder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchQueue;
import edu.kit.aquaplanning.util.Logger;

public abstract class GenericCubeFinder extends CubeFinder {

	// Frontier gets initialized by the call of initializeFrontierWithNode()
	protected GenericSearchQueue frontier;

	public GenericCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem) {

		initializeFrontierWithNode(problem);

		// we only return cubePercent amount of nodes at the end
		while (!frontier.isEmpty() && currentCubeSize() * config.cubePercent < config.numCubes && withinTimeLimit()) {

			totalIterations++;

			GenericSearchNode node = frontier.get();

			if (node == null) {
				assert (frontier.isEmpty());
				continue;
			}

			if (node.satisfiesProblem()) {
				Logger.log(Logger.INFO,
						"Generic Cube Finder already found a plan after " + totalIterations + " steps.");
				plan = node.getPartialPlan();

				foundCubeSize = currentCubeSize();
				returnedCubeSize = 0;
				return null;
			}

			for (GenericSearchNode child : node.getPredecessors()) {
				frontier.add(child);
			}
		}
		if (!withinTimeLimit()) {
			// We return an empty list so signalize that we should stop searching for a
			// solution
			return new ArrayList<Cube>();
		}

		// We found the required amount of cubes. We now return the requested fraction
		// of them
		// get the size of cubes first because a call to extract cubes changes the size
		// to 0
		foundCubeSize = currentCubeSize();
		List<Cube> cubes = extractCubes();
		returnedCubeSize = Math.min(config.numCubes, cubes.size());

		// We also add the initial node again to stay complete
		Random random = new Random(config.seed);
		java.util.Collections.shuffle(cubes, random);
		List<Cube> result = cubes.subList(0, returnedCubeSize);
		result.add(new ForwardSearchNode(problem).getCube());
		returnedCubeSize++;
		return result;
	}

	/**
	 * depending on if we want to use open or closed nodes as cubes we return the
	 * size of the open or closed nodes.
	 */
	private int currentCubeSize() {
		switch (config.cubeNodeType) {
		case open:
			return frontier.size();
		case closed:
			return frontier.visitedSize();
		default:
			throw new IllegalArgumentException("Cube Type not supported");
		}
	}

	private List<Cube> extractCubes() {

		List<Cube> cubes;
		switch (config.cubeNodeType) {
		case open:
			cubes = frontier.getOpenCubes();
			break;
		case closed:
			cubes = frontier.getClosedCubes();
			break;
		default:
			throw new IllegalArgumentException("Cube Type not supported");
		}
		return cubes;
	}

	/**
	 * Should be overridden in child classes. Must construct the frontier and fill
	 * it with the starting node. A Backward Search will start witch a backward node
	 * and a forward search with a forward node.
	 * 
	 * @param problem
	 *            the problem to search for cubes
	 */
	protected abstract void initializeFrontierWithNode(GroundPlanningProblem problem);
}
